/*
 * Copyright 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.canal.evaluation

import io.pivotal.canal.extensions.*
import io.pivotal.canal.extensions.nestedstages.stages
import io.pivotal.canal.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class PipelineTemplateExpressionEvaluatorTest {

    val template = PipelineTemplate(
            "newSpelTemplate",
            Metadata(
                    "Variable Wait",
                    "A demonstrative Wait PipelineModel.",
                    "example@example.com"
            ),
            listOf(
                    IntegerVariable(
                            "waitTime",
                            "The time a wait stage shall pauseth",
                            42
                    )
            ),
            pipeline("test") {
                stages = stages {
                    stage(Wait("\${ templateVariables.waitTime }"))
                }
            }
    )

    val pipelineConfig = PipelineConfiguration(
            "app",
            "name",
            TemplateSource("template")
    )

    @Test
    fun `evaluate template expression with error`() {
        val evaluator = ExpressionEvaluator()

        val thrown = catchThrowable {
            evaluator.evaluate(template, PipelineTemplateInstance(pipelineConfig))
        }

        assertThat(thrown.message).isEqualTo("Failed to evaluate expressions!")
        assertThat((thrown as IllegalExpressionException).summary.expressionResult.values.first().first().description)
                .contains("templateVariables.waitTime not found")
    }

    @Test
    fun `evaluate template expression`() {
        val pipelineConfigWithVariable = pipelineConfig.copy(
                variables = mapOf(
                        "waitTime" to 4
                )
        )
        val evaluator = ExpressionEvaluator()

        val evaluatedTemplate = evaluator.evaluate(template,
                PipelineTemplateInstance(pipelineConfigWithVariable))

        assertThat(evaluatedTemplate).isEqualTo(PipelineTemplate(
                "newSpelTemplate",
                Metadata(
                        "Variable Wait",
                        "A demonstrative Wait PipelineModel.",
                        "example@example.com"
                ),
                listOf(
                        IntegerVariable(
                                "waitTime",
                                "The time a wait stage shall pauseth",
                                42
                        )
                ),
                pipeline("test") {
                    stages = stages { stage(Wait("4")) }
                }
        ))
    }

}

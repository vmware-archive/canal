/*
 * Copyright 2018 Pivotal Software, Inc.
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

package io.pivotal.kanal.evaluation

import io.pivotal.kanal.extensions.*
import io.pivotal.kanal.extensions.fluentstages.addStage
import io.pivotal.kanal.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class PipelineTemplateExpressionEvaluatorTest {

    val template = PipelineTemplate(
            "newSpelTemplate",
            Metadata(
                    "Variable Wait",
                    "A demonstrative Wait Pipeline.",
                    "example@example.com"
            ),
            listOf(
                    IntegerVariable(
                            "waitTime",
                            "The time a wait stage shall pauseth",
                            42
                    )
            ),
            Pipeline().with {
                stages = StageGraph().addStage(WaitStage(
                        "\${ templateVariables.waitTime }"
                ))
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
                        "A demonstrative Wait Pipeline.",
                        "example@example.com"
                ),
                listOf(
                        IntegerVariable(
                                "waitTime",
                                "The time a wait stage shall pauseth",
                                42
                        )
                ),
                Pipeline().with {
                    stages = StageGraph().addStage(WaitStage(
                            "4"
                    ))
                }
        ))
    }

}

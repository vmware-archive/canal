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
import io.pivotal.canal.model.cloudfoundry.CloudFoundryCloudProvider
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class PipelineExpressionEvaluatorTest {

    @Test
    fun `evaluate expression with helper functions and properties`() {
        val pipelineExecution = PipelineExecution(
                mapOf(
                        "parameters" to mapOf(
                                "account" to "account-1"
                        )
                )
        )
        val evaluator = ExpressionEvaluator(pipelineExecution)
        val pipeline = pipeline("test") {
            description = "desc1"
            stages = stages {
                stage(
                        CheckPreconditions(ExpressionPrecondition(
                                "hello \${#alphanumerical(trigger['parameters']['account'])}")),
                        name = "Check Preconditions")
            }
        }

        val result = evaluator.evaluate(pipeline)

        assertThat(result).isEqualTo(pipeline("test") {
            description = "desc1"
            stages = stages {
                stage(
                        CheckPreconditions(ExpressionPrecondition("hello account1")),
                        name = "Check Preconditions"
                )
            }
        })
    }

    @Test
    fun `evaluate pipeline expression with error`() {
        val pipelineExecution = PipelineExecution()
        val evaluator = ExpressionEvaluator(pipelineExecution)
        val pipeline = pipeline("test") {
            description = "desc1"
            stages = stages {
                stage(
                        CheckPreconditions(ExpressionPrecondition("\${#alphanumerical('missing paren'}")),
                        name = "Check Preconditions"
                )
            }
        }

        val thrown = catchThrowable {
            evaluator.evaluate(pipeline)
        }

        assertThat(thrown.message).isEqualTo("Failed to evaluate expressions!")
        assertThat((thrown as IllegalExpressionException).summary.expressionResult.values.first().first().description)
                .contains("Found closing '}' at position 33 but most recent opening is '(' at position 17")
    }

    @Test
    fun `evaluate expression in pipeline`() {
        val pipelineExecution = PipelineExecution(
                mapOf(
                        "parameters" to mapOf(
                                "account" to "account-1",
                                "region" to "region-1",
                                "destroyServicesBefore" to "true",
                                "serviceName1" to "One",
                                "serviceName2" to "Two",
                                "serviceName3" to ""
                        )
                )
        )
        val evaluator = ExpressionEvaluator(pipelineExecution)

        val pipeline = pipeline("test") {
            description = "desc1"
            stages = stages {
                    stage(
                            CheckPreconditions(
                                    ExpressionPrecondition("\${true}"),
                                    ExpressionPrecondition("\${2 < 1}")
                            ),
                            name = "Check Preconditions"
                    ) then {
                        (1..3).map {
                            stage(
                                    DestroyService(
                                            CloudFoundryCloudProvider("\${trigger['parameters']['account'] }"),
                                            "\${trigger['parameters']['region'] }",
                                            "\${trigger['parameters']['serviceName$it']}"
                                    ),
                                    name = "Destroy Service $it Before",
                                    stageEnabled = ExpressionCondition("\${trigger['parameters']['destroyServicesBefore']=='true' && trigger['parameters']['serviceName$it']!='none' && trigger['parameters']['serviceName$it']!=\"\"}")
                            )
                        }
                    }
            }
        }

            val evaluatedPipeline = evaluator.evaluate(pipeline)
            val cloudProvider = CloudFoundryCloudProvider("account-1")

            assertThat(evaluatedPipeline).isEqualTo(PipelineModel(
                    name = "test",
                    description ="desc1",
                    stageGraph = stages {
                        stage(
                                CheckPreconditions(
                                        ExpressionPrecondition("true"),
                                        ExpressionPrecondition("false")
                                ),
                                name = "Check Preconditions"
                        ) then {
                            stage(
                                    DestroyService(
                                            cloudProvider,
                                            "region-1",
                                            "One"
                                    ),
                                    name = "Destroy Service 1 Before",
                                    stageEnabled = ExpressionCondition("true")
                            )
                            stage(
                                    DestroyService(
                                            cloudProvider,
                                            "region-1",
                                            "Two"
                                    ),
                                    name = "Destroy Service 2 Before",
                                    stageEnabled = ExpressionCondition("true")
                            )
                            stage(
                                    DestroyService(
                                            cloudProvider,
                                            "region-1",
                                            ""
                                    ),
                                    name = "Destroy Service 3 Before",
                                    stageEnabled = ExpressionCondition("false")
                            )
                        }
                    }
            ))
    }

}

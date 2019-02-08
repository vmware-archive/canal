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
import io.pivotal.kanal.extensions.fluentstages.parallel
import io.pivotal.kanal.model.*
import io.pivotal.kanal.model.cloudfoundry.CloudFoundryCloudProvider
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
        val pipeline = Pipeline().with {
            description = "desc1"
            stages = StageGraph().addStage(
                    CheckPreconditions(
                            ExpressionPrecondition("hello \${#alphanumerical(trigger['parameters']['account'])}")
                    ),
                    BaseStage("Check Preconditions")
            )
        }

        val result = evaluator.evaluate(pipeline)

        assertThat(result).isEqualTo(Pipeline().with {
            description = "desc1"
            stages = StageGraph().addStage(
                    CheckPreconditions(
                            ExpressionPrecondition("hello account1")
                    ),
                    BaseStage("Check Preconditions")
            )
        })
    }

    @Test
    fun `evaluate pipeline expression with error`() {
        val pipelineExecution = PipelineExecution()
        val evaluator = ExpressionEvaluator(pipelineExecution)
        val pipeline = Pipeline().with {
            description = "desc1"
            stages = StageGraph().addStage(
                    CheckPreconditions(
                            ExpressionPrecondition("\${#alphanumerical('missing paren'}")
                    ),
                    BaseStage("Check Preconditions")
            )
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

        val pipeline = Pipeline().with {
            description = "desc1"
            stages = StageGraph().addStage(
                    CheckPreconditions(
                            ExpressionPrecondition("\${true}"),
                            ExpressionPrecondition("\${2 < 1}")
                    ),
                    BaseStage("Check Preconditions")
            ).parallel(
                    (1..3).map {
                        StageGraph().addStage(
                                DestroyService(
                                        CloudFoundryCloudProvider("\${trigger['parameters']['account'] }"),
                                        "\${trigger['parameters']['region'] }",
                                        "\${trigger['parameters']['serviceName$it']}"
                                ),
                                BaseStage("Destroy Service $it Before",
                                    stageEnabled = ExpressionCondition("\${trigger['parameters']['destroyServicesBefore']=='true' && trigger['parameters']['serviceName$it']!='none' && trigger['parameters']['serviceName$it']!=\"\"}")
                                )
                        )
                    }
            )
        }

            val evaluatedPipeline = evaluator.evaluate(pipeline)
            val cloudProvider = CloudFoundryCloudProvider("account-1")

            assertThat(evaluatedPipeline).isEqualTo(Pipeline(
                    description ="desc1",
                    stages = StageGraph().addStage(
                            CheckPreconditions(
                                    ExpressionPrecondition("true"),
                                    ExpressionPrecondition("false")
                            ),
                            BaseStage("Check Preconditions")
                    ).parallel(
                            StageGraph().addStage(
                                    DestroyService(
                                            cloudProvider,
                                            "region-1",
                                            "One"
                                    ),
                                    BaseStage("Destroy Service 1 Before",
                                            stageEnabled = ExpressionCondition("true")
                                    )
                            ),
                            StageGraph().addStage(
                                    DestroyService(
                                            cloudProvider,
                                            "region-1",
                                            "Two"
                                    ),
                                    BaseStage("Destroy Service 2 Before",
                                            stageEnabled = ExpressionCondition("true")
                                    )
                            ),
                            StageGraph().addStage(
                                    DestroyService(
                                            cloudProvider,
                                            "region-1",
                                            ""
                                    ),
                                    BaseStage("Destroy Service 3 Before",
                                            stageEnabled = ExpressionCondition("false")
                                    )
                            )
                    )
            ))
    }

}

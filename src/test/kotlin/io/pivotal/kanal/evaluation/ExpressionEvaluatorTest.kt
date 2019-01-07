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

import io.pivotal.kanal.fluent.Stages
import io.pivotal.kanal.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

class ExpressionEvaluatorTest {

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

        val result = evaluator.evaluate("hello \${#alphanumerical(trigger['parameters']['account'])}")

        assertThat(result).isEqualTo("hello account1")
    }

    @Test
    fun `evaluate expression with error`() {
        val pipelineExecution = PipelineExecution(
                mapOf()
        )
        val evaluator = ExpressionEvaluator(pipelineExecution)

        val thrown = catchThrowable {
            evaluator.evaluate("\${#alphanumerical('missing paren'}")
        }

        assertThat(thrown.message).isEqualTo("Failed to evaluate expressions!")
        // why is com.netflix.spinnaker.orca.pipeline.expressions.ExpressionEvaluationSummary.Result not public?
        assertThat((thrown as IllegalExpressionException).summary.expressionResult.values.first().first().toString())
                .contains("Found closing '}' at position 33 but most recent opening is '(' at position 17'")
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

        val pipeline = Pipeline(
                description = "desc1",
                stageGraph = Stages.first(CheckPreconditionsStage(
                        "Check Preconditions",
                        listOf(
                                ExpressionPrecondition("\${true}"),
                                ExpressionPrecondition("\${2 < 1}")
                        )
                )).fanOut(
                        (1..3).map {
                            Stages.first(DestroyServiceStage(
                                    "Destroy Service $it Before",
                                    "cloudfoundry",
                                    "\${trigger['parameters']['account'] }",
                                    "\${trigger['parameters']['region'] }",
                                    "\${trigger['parameters']['serviceName$it']}",
                                    ExpressionCondition("\${trigger['parameters']['destroyServicesBefore']=='true' && trigger['parameters']['serviceName$it']!='none' && trigger['parameters']['serviceName$it']!=\"\"}")
                            ))
                        }
                ).stageGraph
        )

        val evaluatedPipeline = evaluator.evaluate(pipeline)

        assertThat(evaluatedPipeline).isEqualTo(Pipeline(
                description ="desc1",
                stageGraph = Stages.first(CheckPreconditionsStage(
                        "Check Preconditions",
                        listOf(
                            ExpressionPrecondition("true"),
                            ExpressionPrecondition("false")
                        )
                )).fanOut(
                        listOf(
                                Stages.first(DestroyServiceStage(
                                        "Destroy Service 1 Before",
                                        "cloudfoundry",
                                        "account-1",
                                        "region-1",
                                        "One",
                                        ExpressionCondition("true")
                                )),
                                Stages.first(DestroyServiceStage(
                                        "Destroy Service 2 Before",
                                        "cloudfoundry",
                                        "account-1",
                                        "region-1",
                                        "Two",
                                        ExpressionCondition("true")
                                )),
                                Stages.first(DestroyServiceStage(
                                        "Destroy Service 3 Before",
                                        "cloudfoundry",
                                        "account-1",
                                        "region-1",
                                        "",
                                        ExpressionCondition("false")
                                ))
                        )
                ).stageGraph
        ))
    }

}

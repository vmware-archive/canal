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

package io.pivotal.kanal.extensions

import io.pivotal.kanal.extensions.fluentstages.addStage
import io.pivotal.kanal.extensions.fluentstages.andThen
import io.pivotal.kanal.extensions.fluentstages.parallel
import io.pivotal.kanal.extensions.nestedstages.*
import io.pivotal.kanal.model.*
import io.pivotal.kanal.model.cloudfoundry.CloudFoundryCloudProvider
import io.pivotal.kanal.model.cloudfoundry.ManifestSourceDirect
import org.junit.jupiter.api.Test

import org.assertj.core.api.Assertions.assertThat

class StageGraphExtensionJsonGenerationTest {

    @Test
    fun `fluent stages DSL with fan out and fan in`() {
        val cloudProvider = CloudFoundryCloudProvider("creds1")

        val nestedStages = StageGraph() with {
            stage(CheckPreconditionsStage()) {
                stage(WaitStage(420)) {
                    (1..3).map {
                        stage(
                                DestroyServiceStage(
                                        cloudProvider,
                                        "dev > dev",
                                        "serviceName$it"
                                ),
                                name = "Destroy Service $it Before",
                                stageEnabled = ExpressionCondition("exp1")
                        ) {
                            stage(
                                    DeployServiceStage(
                                            cloudProvider.copy(manifest = ManifestSourceDirect(
                                                    "serviceType$it",
                                                    "serviceName$it",
                                                    "servicePlan$it",
                                                    listOf("serviceTags$it"),
                                                    "serviceParam$it"
                                            )),
                                            "dev > dev"
                                    ),
                                    name = "Deploy Service $it",
                                    comments = "deploy comment",
                                    stageEnabled = ExpressionCondition("exp2")
                            )
                        }
                    }
                } then {
                    stage(ManualJudgmentStage("Give a thumbs up if you like it."))
                }
            }
        }

        val fluentStages = StageGraph().addStage(CheckPreconditionsStage(
        )).andThen(WaitStage(
                420
        )).parallel(
                (1..3).map {
                    StageGraph().addStage(
                            DestroyServiceStage(
                                    cloudProvider,
                                    "dev > dev",
                                    "serviceName$it"
                            ),
                            BaseStage("Destroy Service $it Before",
                                    stageEnabled = ExpressionCondition("exp1")
                            )
                    ).andThen(
                            DeployServiceStage(
                                    cloudProvider.copy(manifest = ManifestSourceDirect(
                                            "serviceType$it",
                                            "serviceName$it",
                                            "servicePlan$it",
                                            listOf("serviceTags$it"),
                                            "serviceParam$it"
                                    )),
                                    "dev > dev"
                            ),
                            BaseStage(
                                    "Deploy Service $it",
                                    "deploy comment",
                                    ExpressionCondition("exp2")
                            )
                    )
                }
        ).andThen(ManualJudgmentStage(
                "Give a thumbs up if you like it."
        ))

        assertThat(nestedStages).isEqualTo(fluentStages)
    }

}

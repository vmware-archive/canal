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

package io.pivotal.canal.extensions.nestedstages

import io.pivotal.canal.json.JsonAdapterFactory
import io.pivotal.canal.model.*
import io.pivotal.canal.model.cloudfoundry.DeployService
import io.pivotal.canal.model.cloudfoundry.DestroyService
import io.pivotal.canal.model.cloudfoundry.ManifestSourceDirect
import io.pivotal.canal.model.cloudfoundry.cloudFoundryCloudProvider
import org.junit.jupiter.api.Test

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.intellij.lang.annotations.Language

class NestedStageGraphJsonGenerationTest {

    companion object {
        @Language("JSON")
        @JvmStatic
        val json = """
    [
        {
            "name": "Check Preconditions",
            "preconditions": [{
              "type":"expression",
              "context":{"expression":"true"}
            }],
            "refId": "checkPreconditions1",
            "requisiteStageRefIds": [],
            "type": "checkPreconditions"
        },
        {
            "refId": "wait2",
            "requisiteStageRefIds": ["checkPreconditions1"],
            "type": "wait",
            "waitTime": "420"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Destroy Service 1 Before",
            "refId": "destroyService3",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait2"],
            "serviceName": "serviceName1",
            "stageEnabled": {
              "expression": "exp1",
              "type": "expression"
            },
            "type": "destroyService"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "comments": "deploy comment",
            "credentials": "creds1",
            "name": "Deploy Service 1",
            "refId": "deployService4",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService3"],
            "manifest": {
              "service": "serviceType1",
              "serviceName": "serviceName1",
              "servicePlan": "servicePlan1",
              "parameters": "serviceParam1",
              "tags": ["serviceTags1"],
              "type": "direct"
            },
            "stageEnabled": {
            "expression": "exp2",
              "type": "expression"
            },
            "type": "deployService"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Destroy Service 2 Before",
            "refId": "destroyService5",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait2"],
            "serviceName": "serviceName2",
            "stageEnabled": {
            "expression": "exp1",
              "type": "expression"
            },
            "type": "destroyService"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "comments": "deploy comment",
            "credentials": "creds1",
            "name": "Deploy Service 2",
            "refId": "deployService6",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService5"],
            "manifest": {
              "service": "serviceType2",
              "serviceName": "serviceName2",
              "servicePlan": "servicePlan2",
              "parameters": "serviceParam2",
              "tags": ["serviceTags2"],
              "type": "direct"
            },
            "stageEnabled": {
              "expression": "exp2",
              "type": "expression"
            },
            "type": "deployService"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Destroy Service 3 Before",
            "refId": "destroyService7",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait2"],
            "serviceName": "serviceName3",
            "stageEnabled": {
              "expression": "exp1",
              "type": "expression"
            },
            "type": "destroyService"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "comments": "deploy comment",
            "credentials": "creds1",
            "name": "Deploy Service 3",
            "refId": "deployService8",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService7"],
            "manifest": {
              "service": "serviceType3",
              "serviceName": "serviceName3",
              "servicePlan": "servicePlan3",
              "parameters": "serviceParam3",
              "tags": ["serviceTags3"],
              "type": "direct"
            },
            "stageEnabled": {
              "expression": "exp2",
              "type": "expression"
            },
            "type": "deployService"
        },
        {
            "instructions": "Give a thumbs up if you like it.",
            "judgmentInputs": [],
            "refId": "manualJudgment9",
            "requisiteStageRefIds": [
                "deployService4",
                "deployService6",
                "deployService8"
            ],
            "type": "manualJudgment"
        }
    ]
        """.trimMargin()
    }

    @Test
    fun `fluent stages DSL with fan out and fan in`() {
        val cloudProvider = cloudFoundryCloudProvider("creds1")
        val stages = stages {
            stage(CheckPreconditions(ExpressionPrecondition(true)),
                    name = "Check Preconditions"
            ) then {
                stage(Wait(420)) then {
                    (1..3).map {
                        stage(
                                DestroyService(
                                        cloudProvider,
                                        "dev > dev",
                                        "serviceName$it"
                                ),
                                name = "Destroy Service $it Before",
                                stageEnabled = ExpressionCondition("exp1")
                        ) then {
                            stage(
                                    DeployService(
                                            cloudProvider,
                                            "dev > dev",
                                            ManifestSourceDirect(
                                                    "serviceType$it",
                                                    "serviceName$it",
                                                    "servicePlan$it",
                                                    listOf("serviceTags$it"),
                                                    "serviceParam$it"
                                            )
                                    ),
                                    name = "Deploy Service $it",
                                    comments = "deploy comment",
                                    stageEnabled = ExpressionCondition("exp2")
                            )
                        }
                    }
                } then {
                    stage(ManualJudgment("Give a thumbs up if you like it."))
                }
            }
        }

        val stagesJson = JsonAdapterFactory().createAdapter<Stages>().toJson(stages)
        assertThatJson(stagesJson).isEqualTo(json)
    }

}

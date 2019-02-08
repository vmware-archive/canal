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

package io.pivotal.kanal.fluent

import io.pivotal.kanal.extensions.fluentstages.addStage
import io.pivotal.kanal.extensions.fluentstages.andThen
import io.pivotal.kanal.extensions.fluentstages.parallel
import io.pivotal.kanal.json.JsonAdapterFactory
import io.pivotal.kanal.model.*
import io.pivotal.kanal.model.cloudfoundry.CloudFoundryCloudProvider
import io.pivotal.kanal.model.cloudfoundry.ManifestSourceDirect
import org.junit.jupiter.api.Test

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.intellij.lang.annotations.Language

class FluentPipelineJsonGenerationTest {

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
        val cloudProvider = CloudFoundryCloudProvider("creds1")
        val stages = StageGraph().addStage(CheckPreconditionsStage(
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

        val json = JsonAdapterFactory().createAdapter<StageGraph>().toJson(stages)
        assertThatJson(json).isEqualTo(json)
    }

}

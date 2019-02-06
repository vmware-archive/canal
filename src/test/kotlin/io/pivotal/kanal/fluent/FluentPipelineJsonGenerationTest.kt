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

import io.pivotal.kanal.extensions.*
import io.pivotal.kanal.json.JsonAdapterFactory
import io.pivotal.kanal.model.*
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
              "context":{"expression":"true"},
              "failPipeline":true
            }],
            "refId": "checkPreconditions1",
            "requisiteStageRefIds": [],
            "type": "checkPreconditions"
        },
        {
            "comments": "woah",
            "name": "Server Group Timeout",
            "refId": "wait2",
            "requisiteStageRefIds": ["checkPreconditions1"],
            "type": "wait",
            "waitTime": "420"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "completeOtherBranchesThenFail": false,
            "continuePipeline": true,
            "credentials": "creds1",
            "failPipeline": false,
            "name": "Destroy Service 1 Before",
            "refId": "destroyService1_3",
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
            "parameters": "serviceParam1",
            "refId": "deployService2_4",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService1_3"],
            "service": "serviceType1",
            "serviceName": "serviceName1",
            "servicePlan": "servicePlan1",
            "stageEnabled": {
            "expression": "exp2",
              "type": "expression"
            },
            "tags": "serviceTags1",
            "type": "deployService"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "completeOtherBranchesThenFail": false,
            "continuePipeline": true,
            "credentials": "creds1",
            "failPipeline": false,
            "name": "Destroy Service 2 Before",
            "refId": "destroyService1_5",
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
            "parameters": "serviceParam2",
            "refId": "deployService2_6",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService1_5"],
            "service": "serviceType2",
            "serviceName": "serviceName2",
            "servicePlan": "servicePlan2",
            "stageEnabled": {
              "expression": "exp2",
              "type": "expression"
            },
            "tags": "serviceTags2",
            "type": "deployService"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "completeOtherBranchesThenFail": false,
            "continuePipeline": true,
            "credentials": "creds1",
            "failPipeline": false,
            "name": "Destroy Service 3 Before",
            "refId": "destroyService1_7",
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
            "parameters": "serviceParam3",
            "refId": "deployService2_8",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService1_7"],
            "service": "serviceType3",
            "serviceName": "serviceName3",
            "servicePlan": "servicePlan3",
            "stageEnabled": {
              "expression": "exp2",
              "type": "expression"
            },
            "tags": "serviceTags3",
            "type": "deployService"
        },
        {
            "failPipeline": true,
            "instructions": "Give a thumbs up if you like it.",
            "judgmentInputs": [],
            "name": "Thumbs Up?",
            "notifications": [],
            "refId": "manualJudgment9",
            "requisiteStageRefIds": [
                "deployService2_4",
                "deployService2_6",
                "deployService2_8"
            ],
            "type": "manualJudgment"
        }
    ]
        """.trimMargin()
    }

    @Test
    fun `fluent stages DSL with fan out and fan in`() {
        val stages = StageGraph().with(CheckPreconditionsStage(
                "Check Preconditions",
                emptyList()
        )).andThen(WaitStage(
                420,
                "woah",
                "Server Group Timeout"
        )).parallel(
                (1..3).map {
                    StageGraph().with(DestroyServiceStage(
                            "Destroy Service $it Before",
                            "cloudfoundry",
                            "creds1",
                            "dev > dev",
                            "serviceName$it",
                            ExpressionCondition("exp1")
                    )).andThen(DeployServiceStage(
                            "Deploy Service $it",
                            "cloudfoundry",
                            "deploy comment",
                            "creds1",
                            "serviceParam$it",
                            "dev > dev",
                            "serviceType$it",
                            "serviceName$it",
                            "servicePlan$it",
                            ExpressionCondition("exp2"),
                            "serviceTags$it"
                    ))
                }
        ).andThen(ManualJudgmentStage(
                "Thumbs Up?",
                "Give a thumbs up if you like it."
        ))

        val json = JsonAdapterFactory().createAdapter<StageGraph>().toJson(stages)
        assertThatJson(json).isEqualTo(json)
    }

}

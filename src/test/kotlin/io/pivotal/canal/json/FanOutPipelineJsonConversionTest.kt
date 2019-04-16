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

package io.pivotal.canal.json

import io.pivotal.canal.extensions.pipeline
import io.pivotal.canal.model.*
import io.pivotal.canal.model.cloudfoundry.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class FanOutPipelineJsonConversionTest {

    @Language("JSON")
    val json = """
        {
          "name": "test",
          "description": "desc1",
          "expectedArtifacts": [],
          "keepWaitingPipelines": false,
          "limitConcurrent": true,
          "notifications": [],
          "parameterConfig": [
            {
              "default": "1",
              "description": "a description of the parameter",
              "hasOptions": true,
              "label": "Parameter One",
              "name": "param1",
              "options": [
                {
                  "value": "1"
                },
                {
                  "value": "2"
                }
              ],
              "required": true
            }
          ],
          "stages": [
            {
              "preconditions": [
                {
                  "context": {
                    "expression": "2 > 1"
                  },
                  "type": "expression"
                }
              ],
              "refId": "1",
              "requisiteStageRefIds": [],
              "type": "checkPreconditions"
            },
            {
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "cluster": "cluster1",
              "credentials": "creds1",
              "name": "Destroy Server Group Before",
              "refId": "2",
              "regions": [
                "dev > dev"
              ],
              "requisiteStageRefIds": [
                "1"
              ],
              "stageEnabled": {
                "expression": "execution['trigger']['parameters']['destroyServerGroupBefore']=='true'",
                "type": "expression"
              },
              "target": "current_asg_dynamic",
              "type": "destroyServerGroup"
            },
            {
              "refId": "3",
              "requisiteStageRefIds": [
                "2"
              ],
              "type": "wait",
              "waitTime": "420"
            },
            {
              "refId": "4",
              "requisiteStageRefIds": [],
              "type": "wait",
              "waitTime": "7"
            },
            {
              "action": "destroyService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "credentials": "creds1",
              "name": "Destroy Service 1 Before",
              "refId": "5",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "4"
              ],
              "serviceName": "serviceName1",
              "stageEnabled": {
                "expression": "exp1",
                "type": "expression"
              },
              "type": "destroyService"
            },
            {
              "action": "destroyService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "credentials": "creds1",
              "name": "Destroy Service 2 Before",
              "refId": "6",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "4"
              ],
              "serviceName": "serviceName2",
              "stageEnabled": {
                "expression": "exp1",
                "type": "expression"
              },
              "type": "destroyService"
            },
            {
              "action": "destroyService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "credentials": "creds1",
              "name": "Destroy Service 3 Before",
              "refId": "7",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "4"
              ],
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
              "name": "Deploy Service 1",
              "refId": "8",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "5"
              ],
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
              "action": "deployService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "comments": "deploy comment",
              "credentials": "creds1",
              "name": "Deploy Service 2",
              "refId": "9",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "6"
              ],
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
              "action": "deployService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "comments": "deploy comment",
              "credentials": "creds1",
              "name": "Deploy Service 3",
              "refId": "10",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "7"
              ],
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
              "clusters": [
                {
                  "account": "account1",
                  "application": "app1",
                  "artifact": {
                    "account": "account2",
                    "reference": "s3://bucket1",
                    "type": "artifact"
                  },
                  "capacity": {
                    "desired": "1",
                    "max": "1",
                    "min": "1"
                  },
                  "cloudProvider": "cloudfoundry",
                  "detail": "ffd",
                  "manifest": {
                    "account": "account3",
                    "reference": "s3://bucket2",
                    "type": "artifact"
                  },
                  "provider": "cloudfoundry",
                  "region": "dev > dev",
                  "stack": "stack1",
                  "strategy": "redblack"
                }
              ],
              "comments": "Deployment Strategy is RedBlack",
              "name": "Deploy",
              "refId": "11",
              "requisiteStageRefIds": [
                "8",
                "9",
                "10"
              ],
              "stageEnabled": {
                "expression": "execution['trigger']['parameters']['deployServerGroup'] == 'true'",
                "type": "expression"
              },
              "type": "deploy"
            },
            {
              "instructions": "Give a thumbs up if you like it.",
              "judgmentInputs": [],
              "refId": "12",
              "requisiteStageRefIds": [
                "11"
              ],
              "type": "manualJudgment"
            },
            {
              "method": "POST",
              "refId": "13",
              "requisiteStageRefIds": [
                "11"
              ],
              "type": "webhook",
              "url": "https://github.com/spinnaker/clouddriver",
              "user": "cmccoy@pivotal.io",
              "waitForCompletion": true
            },
            {
              "analysisType": "realTimeAutomatic",
              "canaryConfig": {
                "lifetimeDuration": "PT1H0M",
                "metricsAccountName": "man",
                "scoreThresholds": {
                  "marginal": "1",
                  "pass": "2"
                },
                "storageAccountName": "san"
              },
              "refId": "14",
              "requisiteStageRefIds": [
                "12"
              ],
              "type": "kayentaCanary"
            }
          ],
          "triggers": [
            {
              "branch": "master",
              "enabled": true,
              "project": "project1",
              "secret": "secret1",
              "slug": "slug1",
              "source": "github",
              "type": "git"
            },
            {
              "enabled": true,
              "job": "does-nothing",
              "master": "my-jenkins-master",
              "type": "jenkins"
            }
          ]
        }
        """.trimMargin()

    val model = pipeline("test") {
        description = "desc1"
        parameters(
                Parameter(
                        "param1",
                        true,
                        "Parameter One",
                        "a description of the parameter",
                        listOf(Value("1"), Value("2")),
                        "1"
                )
        )
        triggers(
                GitHubTrigger(
                        "project1",
                        "slug1",
                        "master",
                        "secret1"
                ),
                JenkinsTrigger(
                        "does-nothing",
                        "my-jenkins-master"
                )
        )
        val cloudProvider = CloudFoundryCloudProvider("creds1")
        stages = StageGraph(
                listOf(
                        PipelineStage(1,
                                CheckPreconditions(
                                        ExpressionPrecondition("2 > 1")
                                )
                        ),
                        PipelineStage(2,
                                DestroyServerGroup(
                                        cloudProvider,
                                        listOf("dev > dev"),
                                        "cluster1",
                                        TargetServerGroup.Newest
                                ),
                                BaseStage("Destroy Server Group Before",
                                        stageEnabled = ExpressionCondition("execution['trigger']['parameters']['destroyServerGroupBefore']=='true'")
                                )
                        ),
                        PipelineStage(3,
                                Wait(420)
                        ),
                        PipelineStage(4,
                                Wait(7)
                        ),
                        PipelineStage(5,
                                DestroyService(
                                        cloudProvider,
                                        "dev > dev",
                                        "serviceName1"
                                ),
                                BaseStage("Destroy Service 1 Before",
                                        stageEnabled = ExpressionCondition("exp1")
                                )
                        ),
                        PipelineStage(6,
                                DestroyService(
                                        cloudProvider,
                                        "dev > dev",
                                        "serviceName2"
                                ),
                                BaseStage("Destroy Service 2 Before",
                                        stageEnabled = ExpressionCondition("exp1")
                                )
                        ),
                        PipelineStage(7,
                                DestroyService(
                                        cloudProvider,
                                        "dev > dev",
                                        "serviceName3"
                                ),
                                BaseStage("Destroy Service 3 Before",
                                        stageEnabled = ExpressionCondition("exp1")
                                )
                        ),
                        PipelineStage(8,
                                DeployService(
                                        cloudProvider.copy(manifest = ManifestSourceDirect(
                                                "serviceType1",
                                                "serviceName1",
                                                "servicePlan1",
                                                listOf("serviceTags1"),
                                                "serviceParam1"
                                        )),
                                        "dev > dev"
                                ),
                                BaseStage("Deploy Service 1",
                                        "deploy comment",
                                        ExpressionCondition("exp2")
                                )
                        ),
                        PipelineStage(9,
                                DeployService(
                                        cloudProvider.copy(manifest = ManifestSourceDirect(
                                                "serviceType2",
                                                "serviceName2",
                                                "servicePlan2",
                                                listOf("serviceTags2"),
                                                "serviceParam2"
                                        )),
                                        "dev > dev"
                                ),
                                BaseStage("Deploy Service 2",
                                        "deploy comment",
                                        ExpressionCondition("exp2")
                                )
                        ),
                        PipelineStage(10,
                                DeployService(
                                        cloudProvider.copy(manifest = ManifestSourceDirect(
                                                "serviceType3",
                                                "serviceName3",
                                                "servicePlan3",
                                                listOf("serviceTags3"),
                                                "serviceParam3"
                                        )),
                                        "dev > dev"
                                ),
                                BaseStage("Deploy Service 3",
                                        "deploy comment",
                                        ExpressionCondition("exp2")
                                )
                        ),
                        PipelineStage(11,
                                Deploy(
                                        CloudFoundryCluster(
                                                "app1",
                                                "account1",
                                                "dev > dev",
                                                DeploymentStrategy.RedBlack,
                                                ReferencedArtifact(
                                                        "account2",
                                                        "s3://bucket1"
                                                ),
                                                Capacity(1),
                                                ArtifactManifest(
                                                        "account3",
                                                        "s3://bucket2"
                                                ),
                                                "stack1",
                                                "ffd"
                                        )

                                ),
                                BaseStage("Deploy",
                                        "Deployment Strategy is RedBlack",
                                        ExpressionCondition("execution['trigger']['parameters']['deployServerGroup'] == 'true'")
                                )
                        ),
                        PipelineStage(12,
                                ManualJudgment(
                                        "Give a thumbs up if you like it."
                                )
                        ),
                        PipelineStage(13,
                                Webhook(
                                        "POST",
                                        "https://github.com/spinnaker/clouddriver"
                                )
                        ),
                        PipelineStage(14,
                                Canary(
                                        "realTimeAutomatic",
                                        CanaryConfig(
                                                "PT1H0M",
                                                ScoreThresholds(1, 2),
                                                "san",
                                                "man"

                                        )
                                )
                        )
                ),
                mapOf(
                        "2" to listOf("1"),
                        "3" to listOf("2"),
                        "5" to listOf("4"),
                        "6" to listOf("4"),
                        "7" to listOf("4"),
                        "8" to listOf("5"),
                        "9" to listOf("6"),
                        "10" to listOf("7"),
                        "11" to listOf("8", "9", "10"),
                        "12" to listOf("11"),
                        "13" to listOf("11"),
                        "14" to listOf("12")
                )
        )
    }

    @Test
    fun `JSON pipeline with fan out and fan in should convert to Pipeline object`() {
        val pipeline = JsonAdapterFactory().createAdapter<PipelineModel>().fromJson(json)
        Assertions.assertThat(pipeline).isEqualTo(model)
    }

    @Test
    fun `generate pipeline JSON with stages that fan out and back in`() {
        val pipeline = JsonAdapterFactory().createAdapter<PipelineModel>().toJson(model)
        JsonAssertions.assertThatJson(pipeline).isEqualTo(json)
    }

}

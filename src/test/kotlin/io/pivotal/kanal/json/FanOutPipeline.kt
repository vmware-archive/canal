package io.pivotal.kanal.json

import io.pivotal.kanal.model.*
import org.intellij.lang.annotations.Language

@Language("JSON")
val fanOutPipelineJson = """
        {
          "appConfig": {},
          "description": "desc1",
          "expectedArtifacts": [],
          "keepWaitingPipelines": false,
          "lastModifiedBy": "anonymous",
          "limitConcurrent": false,
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
              "name": "Check Preconditions",
              "preconditions": [
                {
                  "context": {
                    "expression": "2 > 1"
                  },
                  "failPipeline": true,
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
              "completeOtherBranchesThenFail": false,
              "continuePipeline": true,
              "credentials": "creds1",
              "failPipeline": false,
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
              "comments": "woah",
              "name": "Server Group Timeout",
              "refId": "3",
              "requisiteStageRefIds": [
                "2"
              ],
              "type": "wait",
              "waitTime": "420"
            },
            {
              "comments": "Wait on other service to reset",
              "name": "External service Wait",
              "refId": "4",
              "requisiteStageRefIds": [],
              "type": "wait",
              "waitTime": "7"
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
              "completeOtherBranchesThenFail": false,
              "continuePipeline": true,
              "credentials": "creds1",
              "failPipeline": false,
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
              "completeOtherBranchesThenFail": false,
              "continuePipeline": true,
              "credentials": "creds1",
              "failPipeline": false,
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
              "parameters": "serviceParam1",
              "refId": "8",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "5"
              ],
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
              "action": "deployService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "comments": "deploy comment",
              "credentials": "creds1",
              "name": "Deploy Service 2",
              "parameters": "serviceParam2",
              "refId": "9",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "6"
              ],
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
              "action": "deployService",
              "cloudProvider": "cloudfoundry",
              "cloudProviderType": "cloudfoundry",
              "comments": "deploy comment",
              "credentials": "creds1",
              "name": "Deploy Service 3",
              "parameters": "serviceParam3",
              "refId": "10",
              "region": "dev > dev",
              "requisiteStageRefIds": [
                "7"
              ],
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
              "clusters": [
                {
                  "account": "account1",
                  "application": "app1",
                  "artifact": {
                    "account": "account2",
                    "reference": "s3://bucket1",
                    "type": "artifact"
                  },
                  "cloudProvider": "cloudfoundry",
                  "freeFormDetails": "ffd",
                  "manifest": {
                    "account": "account3",
                    "reference": "s3://bucket2",
                    "type": "artifact"
                  },
                  "provider": "cloudfoundry",
                  "region": "dev > dev",
                  "stack": "stack1",
                  "startApplication": true,
                  "strategy": "RedBlack"
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
              "failPipeline": true,
              "instructions": "Give a thumbs up if you like it.",
              "judgmentInputs": [],
              "name": "Thumbs Up?",
              "notifications": [],
              "refId": "12",
              "requisiteStageRefIds": [
                "11"
              ],
              "type": "manualJudgment"
            },
            {
              "failPipeline": true,
              "method": "POST",
              "name": "Do that nonstandard thing",
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
              "name": "Canary Analysis",
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

val fanOutPipelineModel = Pipeline(
        "desc1",
        listOf(
                Parameter(
                        "param1",
                        "Parameter One",
                        true,
                        "a description of the parameter",
                        listOf("1", "2"),
                        "1"
                )
        ),
        emptyList(),
        listOf(
                GitTrigger(
                        true,
                        "master",
                        "project1",
                        "secret1",
                        "slug1",
                        "github"
                ),
                JenkinsTrigger(
                        true,
                        "does-nothing",
                        "my-jenkins-master"
                )
        ),
        StageGraph(
                listOf(
                        PipelineStage(1,
                                CheckPreconditionsStage(
                                        "Check Preconditions",
                                        listOf(ExpressionPrecondition("2 > 1"))
                                )
                        ),
                        PipelineStage(2,
                                DestroyServerGroupStage(
                                        "Destroy Server Group Before",
                                        "cloudfoundry",
                                        "cluster1",
                                        "creds1",
                                        listOf("dev > dev"),
                                        ExpressionCondition("execution['trigger']['parameters']['destroyServerGroupBefore']=='true'"),
                                        "current_asg_dynamic"
                                )
                        ),
                        PipelineStage(3,
                                WaitStage(
                                        "Server Group Timeout",
                                        "woah",
                                        420
                                )
                        ),
                        PipelineStage(4,
                                WaitStage(
                                        "External service Wait",
                                        "Wait on other service to reset",
                                        7
                                )
                        ),
                        PipelineStage(5,
                                DestroyServiceStage(
                                        "Destroy Service 1 Before",
                                        "cloudfoundry",
                                        "creds1",
                                        "dev > dev",
                                        "serviceName1",
                                        ExpressionCondition("exp1")
                                )
                        ),
                        PipelineStage(6,
                                DestroyServiceStage(
                                        "Destroy Service 2 Before",
                                        "cloudfoundry",
                                        "creds1",
                                        "dev > dev",
                                        "serviceName2",
                                        ExpressionCondition("exp1")
                                )
                        ),
                        PipelineStage(7,
                                DestroyServiceStage(
                                        "Destroy Service 3 Before",
                                        "cloudfoundry",
                                        "creds1",
                                        "dev > dev",
                                        "serviceName3",
                                        ExpressionCondition("exp1")
                                )
                        ),
                        PipelineStage(8,
                                DeployServiceStage(
                                        "Deploy Service 1",
                                        "cloudfoundry",
                                        "deploy comment",
                                        "creds1",
                                        "serviceParam1",
                                        "dev > dev",
                                        "serviceType1",
                                        "serviceName1",
                                        "servicePlan1",
                                        ExpressionCondition("exp2"),
                                        "serviceTags1"
                                )
                        ),
                        PipelineStage(9,
                                DeployServiceStage(
                                        "Deploy Service 2",
                                        "cloudfoundry",
                                        "deploy comment",
                                        "creds1",
                                        "serviceParam2",
                                        "dev > dev",
                                        "serviceType2",
                                        "serviceName2",
                                        "servicePlan2",
                                        ExpressionCondition("exp2"),
                                        "serviceTags2"
                                )
                        ),
                        PipelineStage(10,
                                DeployServiceStage(
                                        "Deploy Service 3",
                                        "cloudfoundry",
                                        "deploy comment",
                                        "creds1",
                                        "serviceParam3",
                                        "dev > dev",
                                        "serviceType3",
                                        "serviceName3",
                                        "servicePlan3",
                                        ExpressionCondition("exp2"),
                                        "serviceTags3"
                                )
                        ),
                        PipelineStage(11,
                                DeployStage(
                                        "Deploy",
                                        "Deployment Strategy is RedBlack",
                                        listOf(CloudFoundryCluster(
                                                "account1",
                                                "dev > dev",
                                                "stack1",
                                                "RedBlack",
                                                true,
                                                "app1",
                                                "ffd",
                                                ReferencedArtifact(
                                                        "account2",
                                                        "s3://bucket1"
                                                ),
                                                ArtifactManifest(
                                                        "account3",
                                                        "s3://bucket2"
                                                )
                                        )),
                                        ExpressionCondition("execution['trigger']['parameters']['deployServerGroup'] == 'true'")
                                )
                        ),
                        PipelineStage(12,
                                ManualJudgmentStage(
                                        "Thumbs Up?",
                                        "Give a thumbs up if you like it.",
                                        emptyList(),
                                        emptyList()
                                )
                        ),
                        PipelineStage(13,
                                WebhookStage(
                                        "Do that nonstandard thing",
                                        "POST",
                                        "https://github.com/spinnaker/clouddriver",
                                        "cmccoy@pivotal.io",
                                        true
                                )
                        ),
                        PipelineStage(14,
                                CanaryStage(
                                        "Canary Analysis",
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
                        2 to listOf(1),
                        3 to listOf(2),
                        5 to listOf(4),
                        6 to listOf(4),
                        7 to listOf(4),
                        8 to listOf(5),
                        9 to listOf(6),
                        10 to listOf(7),
                        11 to listOf(8, 9, 10),
                        12 to listOf(11),
                        13 to listOf(11),
                        14 to listOf(12)
                )
        )
)

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

import org.intellij.lang.annotations.Language

class StageGraphJson {

    companion object {

        @Language("JSON")
        @JvmStatic
        val basicStagesWithFanOutAndFanIn = """
{
    "name": "test",
    "description":"",
    "parameterConfig":[],
    "notifications":[],
    "triggers":[],
    "stages": [
        {
            "refId": "wait_1",
            "requisiteStageRefIds": [],
            "type": "wait",
            "waitTime": "60"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Mongo",
            "refId": "deployService_2",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mongo}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Rabbit",
            "refId": "deployService_3",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_rabbit}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy MySQL",
            "refId": "deployService_4",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mysql}",
              "type": "artifact"
            }
        },
        {
            "refId": "deploy_5",
            "requisiteStageRefIds": ["deployService_2", "deployService_3", "deployService_4"],
            "type": "deploy",
            "name": "Deploy to Dev",
            "clusters":[{
                "account": "montclair",
                "application": "app1",
                "applicationArtifact": {
                  "artifactId": "0"
                },
                "capacity": {"desired": "1", "max": "1", "min": "1"},
                "cloudProvider": "cloudfoundry",
                "detail": "",
                "manifest": {"account": "montclair", "reference": ".*", "type": "artifact"},
                "provider": "cloudfoundry",
                "region": "dev > dev",
                "stack": "",
                "strategy": ""
            }]
        },
        {
            "refId": "wait_6",
            "requisiteStageRefIds": ["deploy_5"],
            "type": "wait",
            "waitTime": "1+1",
            "name": "cool off"
        },
        {
            "refId": "rollbackCluster_7",
            "type": "rollbackCluster",
            "name":"Rollback",
            "requisiteStageRefIds": ["wait_6"],
            "cluster": "cluster1",
            "moniker": {
                "app": "cluster1",
                "cluster": "cluster1"
            },
            "regions": ["dev > dev"],
            "targetHealthyRollbackPercentage": 100,
            "credentials": "creds1",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry"
        }
    ],
    "expectedArtifacts": [
      {
        "defaultArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:latest.release",
          "type": "maven/file"
        },
        "displayName": "AppJar",
        "id": "0",
        "matchArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:.*",
          "type": "maven/file"
        },
        "useDefaultArtifact": true,
        "usePriorArtifact": true
      }
    ],
    "keepWaitingPipelines": false,
    "limitConcurrent": true
}
        """.trimMargin()


        @Language("JSON")
        @JvmStatic
        val basicStagesWithFanOutAndFanInWithTrigger = """
{
    "name": "test",
    "description":"",
    "parameterConfig":[],
    "notifications":[],
    "triggers":[
      {
        "artifactorySearchName": "spring-artifactory",
        "enabled": true,
        "expectedArtifactIds": [
          "0"
        ],
        "type": "artifactory"
      }
    ],
    "stages": [
        {
            "refId": "wait_1",
            "requisiteStageRefIds": [],
            "type": "wait",
            "waitTime": "60"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Mongo",
            "refId": "deployService_2",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mongo}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Rabbit",
            "refId": "deployService_3",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_rabbit}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy MySQL",
            "refId": "deployService_4",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mysql}",
              "type": "artifact"
            }
        },
        {
            "refId": "deploy_5",
            "requisiteStageRefIds": ["deployService_2", "deployService_3", "deployService_4"],
            "type": "deploy",
            "name": "Deploy to Dev",
            "clusters":[{
                "account": "montclair",
                "application": "app1",
                "applicationArtifact": {
                  "artifactId": "0"
                },
                "capacity": {"desired": "1", "max": "1", "min": "1"},
                "cloudProvider": "cloudfoundry",
                "detail": "",
                "manifest": {"account": "montclair", "reference": ".*", "type": "artifact"},
                "provider": "cloudfoundry",
                "region": "dev > dev",
                "stack": "",
                "strategy": ""
            }]
        },
        {
            "refId": "wait_6",
            "requisiteStageRefIds": ["deploy_5"],
            "type": "wait",
            "waitTime": "1+1",
            "name": "cool off"
        },
        {
            "refId": "rollbackCluster_7",
            "type": "rollbackCluster",
            "name":"Rollback",
            "requisiteStageRefIds": ["wait_6"],
            "cluster": "cluster1",
            "moniker": {
                "app": "cluster1",
                "cluster": "cluster1"
            },
            "regions": ["dev > dev"],
            "targetHealthyRollbackPercentage": 100,
            "credentials": "creds1",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry"
        }
    ],
    "expectedArtifacts": [
      {
        "defaultArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:latest.release",
          "type": "maven/file"
        },
        "displayName": "AppJar",
        "id": "0",
        "matchArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:.*",
          "type": "maven/file"
        },
        "useDefaultArtifact": true,
        "usePriorArtifact": true
      }
    ],
    "keepWaitingPipelines": false,
    "limitConcurrent": true
}
        """.trimMargin()


        @Language("JSON")
        @JvmStatic
        val basicStagesWithNestedDefaults = """
{
    "name": "test",
    "description":"",
    "parameterConfig":[],
    "notifications":[],
    "triggers":[],
    "stages": [
        {
            "refId": "wait_1",
            "requisiteStageRefIds": [],
            "type": "wait",
            "waitTime": "60"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Mongo",
            "refId": "deployService_2",
            "region": "dev1 > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mongo}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Rabbit",
            "refId": "deployService_3",
            "region": "dev2 > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_rabbit}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy MySQL",
            "refId": "deployService_4",
            "region": "dev3 > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mysql}",
              "type": "artifact"
            }
        },
        {
            "refId": "deploy_5",
            "requisiteStageRefIds": ["deployService_2", "deployService_3", "deployService_4"],
            "type": "deploy",
            "name": "Deploy to Dev",
            "clusters":[{
                "account": "montclair",
                "application": "app1",
                "applicationArtifact": {
                  "artifactId": "0"
                },
                "capacity": {"desired": "1", "max": "1", "min": "1"},
                "cloudProvider": "cloudfoundry",
                "detail": "",
                "manifest": {"account": "montclair", "reference": ".*", "type": "artifact"},
                "provider": "cloudfoundry",
                "region": "dev > dev",
                "stack": "",
                "strategy": ""
            }]
        },
        {
            "refId": "wait_6",
            "requisiteStageRefIds": ["deploy_5"],
            "type": "wait",
            "waitTime": "1+1",
            "name": "cool off"
        },
        {
            "refId": "rollbackCluster_7",
            "type": "rollbackCluster",
            "name":"Rollback",
            "requisiteStageRefIds": ["wait_6"],
            "cluster": "cluster1",
            "moniker": {
                "app": "cluster1",
                "cluster": "cluster1"
            },
            "regions": ["dev > dev"],
            "targetHealthyRollbackPercentage": 100,
            "credentials": "creds1",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry"
        }
    ],
    "expectedArtifacts": [
      {
        "defaultArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:latest.release",
          "type": "maven/file"
        },
        "displayName": "AppJar",
        "id": "0",
        "matchArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:.*",
          "type": "maven/file"
        },
        "useDefaultArtifact": true,
        "usePriorArtifact": true
      }
    ],
    "keepWaitingPipelines": false,
    "limitConcurrent": true
}
        """.trimMargin()


        @Language("JSON")
        @JvmStatic
        val basicStagesWithMultiLevelNestedDefaults = """
{
    "name": "test",
    "description":"",
    "parameterConfig":[],
    "notifications":[],
    "triggers":[],
    "stages": [
        {
            "refId": "wait_1",
            "requisiteStageRefIds": [],
            "type": "wait",
            "waitTime": "60"
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Mongo",
            "refId": "deployService_2",
            "region": "dev1 > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mongo}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy Rabbit",
            "refId": "deployService_3",
            "region": "dev0 > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_rabbit}",
              "type": "artifact"
            }
        },
        {
            "action": "deployService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Deploy MySQL",
            "refId": "deployService_4",
            "region": "dev0 > dev",
            "requisiteStageRefIds": ["wait_1"],
            "type": "deployService",
            "manifest": {
              "account":"public",
              "reference":  "$\\{service_manifest_mysql}",
              "type": "artifact"
            }
        },
        {
            "refId": "deploy_5",
            "requisiteStageRefIds": ["deployService_2", "deployService_3", "deployService_4"],
            "type": "deploy",
            "name": "Deploy to Dev",
            "clusters":[{
                "account": "montclair",
                "application": "app1",
                "applicationArtifact": {
                  "artifactId": "0"
                },
                "capacity": {"desired": "1", "max": "1", "min": "1"},
                "cloudProvider": "cloudfoundry",
                "detail": "",
                "manifest": {"account": "montclair", "reference": ".*", "type": "artifact"},
                "provider": "cloudfoundry",
                "region": "dev0 > dev",
                "stack": "",
                "strategy": ""
            }]
        },
        {
            "refId": "wait_6",
            "requisiteStageRefIds": ["deploy_5"],
            "type": "wait",
            "waitTime": "1+1",
            "name": "cool off"
        },
        {
            "refId": "rollbackCluster_7",
            "type": "rollbackCluster",
            "name":"Rollback",
            "requisiteStageRefIds": ["wait_6"],
            "cluster": "cluster1",
            "moniker": {
                "app": "cluster1",
                "cluster": "cluster1"
            },
            "regions": ["dev > dev"],
            "targetHealthyRollbackPercentage": 100,
            "credentials": "creds1",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry"
        }
    ],
    "expectedArtifacts": [
      {
        "defaultArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:latest.release",
          "type": "maven/file"
        },
        "displayName": "AppJar",
        "id": "0",
        "matchArtifact": {
          "artifactAccount": "spring-artifactory-maven",
          "reference": "io.pivotal.spinnaker:multifoundationmetrics:.*",
          "type": "maven/file"
        },
        "useDefaultArtifact": true,
        "usePriorArtifact": true
      }
    ],
    "keepWaitingPipelines": false,
    "limitConcurrent": true
}
        """.trimMargin()

        @Language("JSON")
        @JvmStatic
        val nestedStageGraphs = """
{
    "name": "test",
    "description":"",
    "parameterConfig":[],
    "notifications":[],
    "triggers":[],
    "stages": [
        {
            "refId": "wait_1",
            "requisiteStageRefIds": [],
            "type": "wait",
            "waitTime": "60"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "refId": "destroyService_2",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "serviceName": "service1",
            "type": "destroyService"
        },
        {
            "refId": "wait_3",
            "name": "deploy service 1",
            "requisiteStageRefIds": ["destroyService_2"],
            "type": "wait",
            "waitTime": "1"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "refId": "destroyService_4",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_1"],
            "serviceName": "service2",
            "type": "destroyService"
        },
        {
            "refId": "wait_5",
            "name": "deploy service 2",
            "requisiteStageRefIds": ["destroyService_4"],
            "type": "wait",
            "waitTime": "2"
        },
        {
            "refId": "wait_6",
            "name": "cool off",
            "requisiteStageRefIds": ["wait_1"],
            "type": "wait",
            "waitTime": "60"
        },
        {
            "instructions": "Approve?",
            "judgmentInputs": [],
            "refId": "manualJudgment_7",
            "requisiteStageRefIds": [
                "wait_3",
                "wait_5",
                "wait_6"
            ],
            "type": "manualJudgment"
        }
    ],
    "expectedArtifacts": [],
    "keepWaitingPipelines": false,
    "limitConcurrent": true
}
        """.trimMargin()

        @Language("JSON")
        @JvmStatic
        val fanOutToMultipleDeployThenDestroys = """
{
    "name": "test",
    "description":"",
    "parameterConfig":[],
    "notifications":[],
    "triggers":[],
    "stages": [
        {
            "preconditions": [{
              "type":"expression",
              "context":{"expression":"true"}
            }],
            "refId": "checkPreconditions_1",
            "requisiteStageRefIds": [],
            "type": "checkPreconditions"
        },
        {
            "refId": "wait_2",
            "requisiteStageRefIds": ["checkPreconditions_1"],
            "type": "wait",
            "waitTime": "420"
        },
        {
            "action": "destroyService",
            "cloudProvider": "cloudfoundry",
            "cloudProviderType": "cloudfoundry",
            "credentials": "creds1",
            "name": "Destroy Service 1 Before",
            "refId": "destroyService_3",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_2"],
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
            "refId": "deployService_4",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService_3"],
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
            "refId": "destroyService_5",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_2"],
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
            "refId": "deployService_6",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService_5"],
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
            "refId": "destroyService_7",
            "region": "dev > dev",
            "requisiteStageRefIds": ["wait_2"],
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
            "refId": "deployService_8",
            "region": "dev > dev",
            "requisiteStageRefIds": ["destroyService_7"],
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
            "refId": "manualJudgment_9",
            "requisiteStageRefIds": [
                "deployService_4",
                "deployService_6",
                "deployService_8"
            ],
            "type": "manualJudgment"
        }
    ],
    "expectedArtifacts": [],
    "keepWaitingPipelines": false,
    "limitConcurrent": true
}
        """.trimMargin()

    }

}

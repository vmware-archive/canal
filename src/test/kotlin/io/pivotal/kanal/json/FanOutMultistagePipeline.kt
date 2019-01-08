package io.pivotal.kanal.json

import org.intellij.lang.annotations.Language

object FanOutMultistagePipeline {
    @Language("JSON")
    @JvmStatic
    val json = """
    [
        {
            "name": "Check Preconditions",
            "preconditions": [],
            "refId": "1",
            "requisiteStageRefIds": [],
            "type": "checkPreconditions"
        },
        {
            "comments": "woah",
                "name": "Server Group Timeout",
                "refId": "2",
                "requisiteStageRefIds": [
            "1"
              ],
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
                "refId": "3",
                "region": "dev > dev",
                "requisiteStageRefIds": [
            "2"
              ],
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
                "refId": "4",
                "region": "dev > dev",
                "requisiteStageRefIds": [
            "3"
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
            "action": "destroyService",
                "cloudProvider": "cloudfoundry",
                "cloudProviderType": "cloudfoundry",
                "completeOtherBranchesThenFail": false,
                "continuePipeline": true,
                "credentials": "creds1",
                "failPipeline": false,
                "name": "Destroy Service 2 Before",
                "refId": "5",
                "region": "dev > dev",
                "requisiteStageRefIds": [
            "2"
              ],
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
                "refId": "6",
                "region": "dev > dev",
                "requisiteStageRefIds": [
            "5"
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
            "2"
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
                "name": "Deploy Service 3",
                "parameters": "serviceParam3",
                "refId": "8",
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
            "failPipeline": true,
                "instructions": "Give a thumbs up if you like it.",
                "judgmentInputs": [],
            "name": "Thumbs Up?",
                "notifications": [],
            "refId": "9",
                "requisiteStageRefIds": [
            "4",
                    "6",
                    "8"
              ],
            "type": "manualJudgment"
        }
    ]
        """.trimMargin()
}
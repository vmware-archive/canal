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
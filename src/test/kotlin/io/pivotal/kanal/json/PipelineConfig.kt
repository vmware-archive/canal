package io.pivotal.kanal.json

import io.pivotal.kanal.fluent.Stages
import io.pivotal.kanal.model.*
import org.intellij.lang.annotations.Language

object PipelineConfig {

    @Language("JSON")
    @JvmStatic
    val json = """
    {
        "schema": "v2",
        "application": "waze",
        "name": "My First SpEL Pipeline",
        "template": {
            "source": "spinnaker://newSpelTemplate"
        },
        "variables": {
            "waitTime": 6
        },
        "inherit": [],
        "triggers": [
            {
                "type": "pubsub",
                "enabled": true,
                "pubsubSystem": "google",
                "subscription": "super-derp",
                "subscriptionName": "super-derp",
                "source": "jack",
                "attributeConstraints": {},
                "payloadConstraints": {}
            }
        ],
        "parameters": [],
        "notifications": [],
        "description": "",
        "stages": [
            {
                "refId": "wait2",
                "name": "",
                "requisiteStageRefIds": ["wait1"],
                "type": "wait",
                "waitTime": "67",
                "comments": ""
            },
            {
                "refId": "wait0",
                "name": "",
                "inject": {
                    "type": "first",
                    "first": true
                },
                "type": "wait",
                "waitTime": "2",
                "comments": "",
                "requisiteStageRefIds": []
            }
        ]
    }
    """.trimMargin()

    @JvmStatic
    val model = PipelineConfig(
            "waze",
            "My First SpEL Pipeline",
            TemplateSource("spinnaker://newSpelTemplate"),
            Pipeline(
                    triggers = listOf(
                            PubSubTrigger(
                                    "google",
                                    "super-derp",
                                    "jack"
                            )
                    ),
                    stageGraph = Stages.of(
                            WaitStage(67),
                            refId = "wait2",
                            requisiteStageRefIds = listOf("wait1")
                    ).and(
                            WaitStage(2),
                            refId = "wait0",
                            inject = Inject.First()
                    ).stageGraph
            ),
            mapOf("waitTime" to 6)
    )
}


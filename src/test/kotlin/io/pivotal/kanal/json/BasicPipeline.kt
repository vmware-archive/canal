package io.pivotal.kanal.json

import io.pivotal.kanal.model.*
import org.intellij.lang.annotations.Language

@Language("JSON")
val basicPipelineJson = """
        {
            "appConfig": {},
            "description": "desc1",
            "expectedArtifacts": [],
            "keepWaitingPipelines": false,
            "lastModifiedBy": "anonymous",
            "limitConcurrent": false,
            "notifications": [],
            "parameterConfig": [],
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
                    "enabled":true,
                    "job":"does-nothing",
                    "master":"my-jenkins-master",
                    "type":"jenkins"
                }
            ],
            "stages": [
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
                  "failPipeline": true,
                  "instructions": "Give a thumbs up if you like it.",
                  "judgmentInputs": [],
                  "name": "Thumbs Up?",
                  "notifications": [],
                  "refId": "3",
                  "requisiteStageRefIds": [
                    "1"
                  ],
                  "type": "manualJudgment"
                },
                {
                  "failPipeline": true,
                  "method": "POST",
                  "name": "Do that nonstandard thing",
                  "refId": "4",
                  "requisiteStageRefIds": [
                    "2",
                    "3"
                  ],
                  "type": "webhook",
                  "url": "https://github.com/spinnaker/clouddriver",
                  "user": "cmccoy@pivotal.io",
                  "waitForCompletion": true
                }
            ]
        }
        """.trimMargin()

val basicPipelineModel = Pipeline(
        "desc1",
        listOf(),
        listOf(),
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
                                        listOf()
                                )
                        ),
                        PipelineStage(2,
                                WaitStage(
                                        "Server Group Timeout",
                                        "woah",
                                        420
                                )
                        ),
                        PipelineStage(3,
                                ManualJudgmentStage(
                                        "Thumbs Up?",
                                        "Give a thumbs up if you like it.",
                                        listOf(),
                                        listOf()
                                )
                        ),
                        PipelineStage(4,
                                WebhookStage(
                                        "Do that nonstandard thing",
                                        "POST",
                                        "https://github.com/spinnaker/clouddriver",
                                        "cmccoy@pivotal.io",
                                        true
                                )
                        )
                ),
                mapOf(
                        2 to listOf(1),
                        3 to listOf(1),
                        4 to listOf(2, 3)
                )
        )
)

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

package io.pivotal.kanal.json

import io.pivotal.kanal.extensions.addStage
import io.pivotal.kanal.model.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class BasicPipelineJsonConversionTest {

    @Language("JSON")
    val json = """
        {
            "appConfig": {},
            "description": "desc1",
            "expectedArtifacts": [],
            "keepWaitingPipelines": false,
            "lastModifiedBy": "anonymous",
            "limitConcurrent": true,
            "notifications": [],
            "parameterConfig": [],
            "updateTs" : "0",
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
                  "preconditions": [],
                  "refId": "1",
                  "requisiteStageRefIds": [],
                  "type": "checkPreconditions"
                },
                {
                  "refId": "2",
                  "requisiteStageRefIds": [
                    "1"
                  ],
                  "type": "wait",
                  "waitTime": "420"
                },
                {
                  "instructions": "Give a thumbs up if you like it.",
                  "judgmentInputs": [],
                  "refId": "3",
                  "requisiteStageRefIds": [
                    "1"
                  ],
                  "type": "manualJudgment"
                },
                {
                  "method": "POST",
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

    val model = Pipeline().addStage {
        description = "desc1"
        triggers(
                GitTrigger(
                        "master",
                        "project1",
                        "secret1",
                        "slug1",
                        "github"
                ),
                JenkinsTrigger(
                        "does-nothing",
                        "my-jenkins-master"
                )
        )
        stages = StageGraph(
                listOf(
                        PipelineStage(1,
                                CheckPreconditionsStage()
                        ),
                        PipelineStage(2,
                                WaitStage(420)
                        ),
                        PipelineStage(3,
                                ManualJudgmentStage(
                                        "Give a thumbs up if you like it."
                                )
                        ),
                        PipelineStage(4,
                                WebhookStage(
                                        "POST",
                                        "https://github.com/spinnaker/clouddriver",
                                        "cmccoy@pivotal.io"
                                )
                        )
                ),
                mapOf(
                        "2" to listOf("1"),
                        "3" to listOf("1"),
                        "4" to listOf("2", "3")
                )
        )
    }

    @Test
    fun `pipeline model should convert to JSON with execution details placed in stage`() {
        val json = JsonAdapterFactory().createAdapter<Pipeline>().toJson(model)
        JsonAssertions.assertThatJson(json).isEqualTo(json)
    }

    @Test
    fun `JSON pipeline should convert to Pipeline object`() {
        val pipeline = JsonAdapterFactory().createAdapter<Pipeline>().fromJson(json)
        assertThat(pipeline).isEqualTo(model)
    }

}

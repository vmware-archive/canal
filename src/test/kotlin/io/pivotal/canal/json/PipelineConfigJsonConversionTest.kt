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

import io.pivotal.canal.extensions.nestedstages.stages
import io.pivotal.canal.extensions.pipeline
import io.pivotal.canal.model.*
import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class PipelineConfigJsonConversionTest  {

    companion object {

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
                "requisiteStageRefIds": ["wait1"],
                "type": "wait",
                "waitTime": "67"
            },
            {
                "refId": "wait0",
                "inject": {
                    "type": "first",
                    "first": true
                },
                "type": "wait",
                "waitTime": "2",
                "requisiteStageRefIds": []
            }
        ]
    }
    """.trimMargin()

        @JvmStatic
        val model = io.pivotal.canal.model.PipelineTemplateInstance(
                PipelineConfiguration(
                        "waze",
                        "My First SpEL Pipeline",
                        TemplateSource("spinnaker://newSpelTemplate"),
                        mapOf("waitTime" to 6)
                ),
                pipeline {
                    triggers = listOf(
                            PubSubTrigger(
                                    "google",
                                    "super-derp",
                                    "jack"
                            )
                    )
                    stages = stages {
                        stage(
                                Wait(67),
                                execution = StageExecution(
                                        refId = "wait2",
                                        requisiteStageRefIds = listOf("wait1")
                                )
                        )
                        stage(
                                Wait(2),
                                execution = StageExecution(
                                        refId = "wait0",
                                        inject = Inject.First()
                                )
                        )
                    }
                }
        )
    }

    @Test
    fun `JSON pipeline template to model`() {
        val pipeline = JsonAdapterFactory().createAdapter<PipelineTemplateInstance>().fromJson(json)
        Assertions.assertThat(pipeline).isEqualTo(model)
    }

    @Test
    fun `generate pipeline template JSON`() {
        val json = JsonAdapterFactory().createAdapter<PipelineTemplateInstance>().toJson(model)
        JsonAssertions.assertThatJson(json).isEqualTo(json)
    }

}

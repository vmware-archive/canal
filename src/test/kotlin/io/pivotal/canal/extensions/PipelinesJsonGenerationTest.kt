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

package io.pivotal.canal.extensions

import io.pivotal.canal.extensions.nestedstages.stages
import io.pivotal.canal.json.JsonAdapterFactory
import io.pivotal.canal.model.*
import org.junit.jupiter.api.Test

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.intellij.lang.annotations.Language

class PipelinesJsonGenerationTest {

    companion object {
        @Language("JSON")
        @JvmStatic
        val json = """
        {
        	"app1": [{
        		"description": "",
        		"expectedArtifacts": [],
        		"keepWaitingPipelines": false,
        		"limitConcurrent": true,
        		"name": "just waiting",
        		"notifications": [],
        		"parameterConfig": [],
        		"stages": [{
        			"refId": "wait1",
        			"requisiteStageRefIds": [],
        			"type": "wait",
        			"waitTime": "420"
        		}],
        		"triggers": []
        	}],
        	"app2": [{
        		"description": "",
        		"expectedArtifacts": [],
        		"keepWaitingPipelines": false,
        		"limitConcurrent": true,
        		"name": "just judging",
        		"notifications": [],
        		"parameterConfig": [],
        		"stages": [{
        			"instructions": "Judge me.",
        			"judgmentInputs": [],
        			"refId": "manualJudgment1",
        			"requisiteStageRefIds": [],
        			"type": "manualJudgment"
        		}],
        		"triggers": []
        	}, {
        		"description": "",
        		"expectedArtifacts": [],
        		"keepWaitingPipelines": false,
        		"limitConcurrent": true,
        		"name": "waiting then judging",
        		"notifications": [],
        		"parameterConfig": [],
        		"stages": [{
        			"comments": "Wait before judging me.",
        			"refId": "wait1",
        			"requisiteStageRefIds": [],
        			"type": "wait",
        			"waitTime": "420"
        		}, {
        			"instructions": "Okay, Judge me now.",
        			"judgmentInputs": [],
        			"refId": "manualJudgment2",
        			"requisiteStageRefIds": ["wait1"],
        			"type": "manualJudgment"
        		}],
        		"triggers": []
        	}]
        }
        """.trimMargin()
    }

    @Test
    fun `create pipelines for apps`() {

        val pipelines = pipelines {
            app("app1") {
                pipeline("just waiting") {
                    stages = stages {
                        stage(Wait(420))
                    }
                }
            }
            app("app2") {
                pipeline("just judging") {
                    stages = stages {
                        stage(ManualJudgment("Judge me."))
                    }
                }
                pipeline("waiting then judging") {
                    stages = stages {
                        stage(
                                Wait(420),
                                comments = "Wait before judging me."
                        ) then {
                            stage(ManualJudgment("Okay, Judge me now."))
                        }
                    }
                }
            }
        }

        assertThatJson(pipelines.toJson()).isEqualTo(json)
    }

}

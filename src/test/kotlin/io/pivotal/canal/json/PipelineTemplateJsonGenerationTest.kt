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

class PipelineTemplateJsonGenerationTest {

    companion object {

        @Language("JSON")
        @JvmStatic
        val json = """
    {
        "schema" : "v2",
        "variables" : [
            {
                "type" : "int",
                "defaultValue" : 42,
                "description" : "The time a wait stage shall pauseth",
                "name" : "waitTime",
                "merge" : false,
                "nullable" : false,
                "remove" : false
            }
        ],
        "id" : "newSpelTemplate",
        "protect" : false,
        "metadata" : {
            "name" : "Variable Wait",
            "description" : PipelineModeldel.",
            "owner" : "example@example.com",
            "scopes" : ["global"]
        },
        "pipeline": {
            "name" : "test",
            "parameterConfig" : [],
            "limitConcurrent": true,
            "keepWaitingPipelines": false,
            "description" : "",
            "triggers" : [],
            "notifications" : [],
            "stages" : [
                {
                    "waitTime" : "$\\{ templateVariables.waitTime }",
                    "type" : "wait",
                    "refId" : "wait1",
                    "requisiteStageRefIds": []
                }
            ],
            "expectedArtifacts": []
        }
    }
        """.trimMargin()

        @JvmStatic
        val model = PipelineTemplate(
                "newSpelTemplate",
                Metadata(
                        "Variable Wait",
                        "A demonstrative Wait PipelineModel.",
                        "example@example.com"
                ),
                listOf(
                        IntegerVariable(
                                "waitTime",
                                "The time a wait stage shall pauseth",
                                42
                        )
                ),
                pipeline("test") {
                    stages = stages { stage(Wait("$\\{ templateVariables.waitTime }")) }
                }
        )
    }

    @Test
    fun `generate pipeline template JSON`() {
        val pipelineTemplate = JsonAdapterFactory().createAdapter<PipelineTemplate>().toJson(model)
        JsonAssertions.assertThatJson(pipelineTemplate).isEqualTo(json)
    }

    @Test
    fun `JSON pipeline template to model`() {
        val pipeline = JsonAdapterFactory().createAdapter<PipelineTemplate>().fromJson(json)
        Assertions.assertThat(pipeline).isEqualTo(model)
    }

}

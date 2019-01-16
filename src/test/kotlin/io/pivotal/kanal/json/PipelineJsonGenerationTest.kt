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

import com.squareup.moshi.Moshi
import io.pivotal.kanal.model.*
import org.junit.jupiter.api.Test

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson

class PipelineJsonGenerationTest {

    @Test
    fun `JSON stage should have flattened object structure`() {
        val dStage = WaitStage("w", 1, "c")
        val stageWithExecution = OrcaStage(dStage, StageExecution("0", listOf("1", "2", "3")))

        val orcaStageAdapter = JsonAdapterFactory().jsonAdapterBuilder(Moshi.Builder()).build().adapter(OrcaStage::class.java)
        val json = orcaStageAdapter.toJson(stageWithExecution)
        assertThatJson(json).isEqualTo("""
        {
            "refId": "0",
            "requisiteStageRefIds": ["1", "2", "3"],
            "type": "wait",
            "name": "w",
            "comments": "c",
            "waitTime": "1"
        }""".trimMargin())
    }

    @Test
    fun `JSON pipeline should have execution details placed in stage`() {
        val json = JsonAdapterFactory().pipelineAdapter().toJson(basicPipelineModel)
        assertThatJson(json).isEqualTo(basicPipelineJson)
    }

    @Test
    fun `generate pipeline json with stages that fan out and back in`() {
        val json = JsonAdapterFactory().pipelineAdapter().toJson(fanOutPipelineModel)
        assertThatJson(json).isEqualTo(fanOutPipelineJson)
    }

}

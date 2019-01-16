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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PipelineModelFromJsonTest {

    @Test
    fun `JSON stage should convert to stage object`() {
        val stageWithExecutionJson = """
        {
            "refId": "0",
            "requisiteStageRefIds": ["1", "2", "3"],
            "type": "wait",
            "name": "w",
            "comments": "c",
            "waitTime": "1"
        }""".trimMargin()

        val orcaStageAdapter = JsonAdapterFactory().jsonAdapterBuilder(Moshi.Builder()).build().adapter(OrcaStage::class.java)
        val stage = orcaStageAdapter.fromJson(stageWithExecutionJson)
        assertThat(stage).isEqualTo(OrcaStage(
                WaitStage("w", 1, "c"),
                StageExecution("0", listOf("1", "2", "3"))
        ))
    }

    @Test
    fun `JSON pipeline should convert to Pipeline object`() {
        val pipeline = JsonAdapterFactory().pipelineAdapter().fromJson(basicPipelineJson)
        assertThat(pipeline).isEqualTo(basicPipelineModel)
    }

    @Test
    fun `JSON pipeline with fan out and fan in should convert to Pipeline object`() {
        val pipeline = JsonAdapterFactory().pipelineAdapter().fromJson(fanOutPipelineJson)
        assertThat(pipeline).isEqualTo(fanOutPipelineModel)
    }

}

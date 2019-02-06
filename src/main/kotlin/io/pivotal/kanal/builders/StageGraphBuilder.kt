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

package io.pivotal.kanal.builders

import io.pivotal.kanal.model.Inject
import io.pivotal.kanal.model.Stage
import io.pivotal.kanal.model.StageGraph

import io.pivotal.kanal.extensions.*

open class StageGraphBuilder(
        var stageGraph: StageGraph = StageGraph()
) {
    companion object Factory {
        @JvmStatic @JvmOverloads fun of(stage: Stage,
               inject: Inject? = null,
               refId: String? = null,
               requisiteStageRefIds: List<String> = emptyList()
        ): StageGraphBuilder {
            return StageGraphBuilder(StageGraph().with(
                    stage,
                    inject,
                    refId,
                    requisiteStageRefIds
            ))
        }
    }

    @JvmOverloads fun with(stage: Stage,
            inject: Inject? = null,
            refId: String? = null,
            requisiteStageRefIds: List<String> = emptyList()
    ): StageGraphBuilder {
        stageGraph = stageGraph.with(stage, inject, refId, requisiteStageRefIds)
        return this
    }

    @JvmOverloads fun andThen(stage: Stage,
                inject: Inject? = null,
                refId: String? = null,
                requisiteStageRefIds: List<String> = emptyList()
    ): StageGraphBuilder {
        stageGraph = stageGraph.andThen(stage, inject, refId, requisiteStageRefIds)
        return this
    }

    @JvmOverloads fun parallelStages(vararg stages: Stage): StageGraphBuilder {
        return parallelStages(stages.toList())
    }

    @JvmOverloads fun parallelStages(stages: List<Stage>): StageGraphBuilder {
        stageGraph = stageGraph.parallelStages(stages)
        return this
    }

    @JvmOverloads fun parallel(vararg stageGraphs: StageGraphBuilder): StageGraphBuilder {
        return parallel(stageGraphs.toList())
    }

    @JvmOverloads fun parallel(stageGraphs: List<StageGraphBuilder>): StageGraphBuilder {
        stageGraph = stageGraph.parallel(stageGraphs.map { it.stageGraph })
        return this
    }

}

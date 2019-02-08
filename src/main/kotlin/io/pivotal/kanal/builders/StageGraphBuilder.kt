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

import io.pivotal.kanal.extensions.fluentstages.addStage
import io.pivotal.kanal.extensions.fluentstages.andThen
import io.pivotal.kanal.extensions.fluentstages.parallel
import io.pivotal.kanal.extensions.fluentstages.parallelStages
import io.pivotal.kanal.model.*

open class StageGraphBuilder(
        var stageGraph: StageGraph = StageGraph()
) {
    companion object Factory {
        @JvmStatic @JvmOverloads fun of(stage: Stage,
                                        base: BaseStage? = null,
                                        execution: StageExecution = StageExecution()
        ): StageGraphBuilder {
            return StageGraphBuilder(StageGraph().addStage(
                    stage,
                    base,
                    execution
            ))
        }
    }

    @JvmOverloads fun with(stage: Stage,
                           base: BaseStage? = null,
                           execution: StageExecution = StageExecution()
    ): StageGraphBuilder {
        stageGraph = stageGraph.addStage(stage, base, execution)
        return this
    }

    @JvmOverloads fun andThen(stage: Stage,
                              base: BaseStage? = null,
                              execution: StageExecution = StageExecution()
    ): StageGraphBuilder {
        stageGraph = stageGraph.andThen(stage, base, execution)
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

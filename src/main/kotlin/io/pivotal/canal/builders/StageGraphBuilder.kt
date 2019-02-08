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

package io.pivotal.canal.builders

import io.pivotal.canal.extensions.fluentstages.addStage
import io.pivotal.canal.extensions.fluentstages.andThen
import io.pivotal.canal.extensions.fluentstages.parallel
import io.pivotal.canal.extensions.fluentstages.parallelStages
import io.pivotal.canal.model.*

open class StageGraphBuilder(
        var stageGraph: StageGraph = StageGraph()
) {
    companion object Factory {
        @JvmStatic @JvmOverloads fun of(stageConfig: StageConfig,
                                        base: BaseStage? = null,
                                        execution: StageExecution = StageExecution()
        ): StageGraphBuilder {
            return StageGraphBuilder(StageGraph().addStage(
                    stageConfig,
                    base,
                    execution
            ))
        }
    }

    @JvmOverloads fun with(stageConfig: StageConfig,
                           base: BaseStage? = null,
                           execution: StageExecution = StageExecution()
    ): StageGraphBuilder {
        stageGraph = stageGraph.addStage(stageConfig, base, execution)
        return this
    }

    @JvmOverloads fun andThen(stageConfig: StageConfig,
                              base: BaseStage? = null,
                              execution: StageExecution = StageExecution()
    ): StageGraphBuilder {
        stageGraph = stageGraph.andThen(stageConfig, base, execution)
        return this
    }

    @JvmOverloads fun parallelStages(vararg stageConfigs: StageConfig): StageGraphBuilder {
        return parallelStages(stageConfigs.toList())
    }

    @JvmOverloads fun parallelStages(stageConfigs: List<StageConfig>): StageGraphBuilder {
        stageGraph = stageGraph.parallelStages(stageConfigs)
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

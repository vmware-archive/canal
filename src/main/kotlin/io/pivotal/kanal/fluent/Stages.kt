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

package io.pivotal.kanal.fluent

import io.pivotal.kanal.model.PipelineStage
import io.pivotal.kanal.model.Stage
import io.pivotal.kanal.model.StageGraph

class Stages(
        val stageCount: Int = 0,
        val firstStages: List<PipelineStage> = listOf(),
        val lastStages: List<PipelineStage> = listOf(),
        val stageGraph: StageGraph = StageGraph(listOf(), mapOf())
) {
    companion object Factory {
        fun first(stage: Stage): Stages {
            val initialRefId = 1
            val initialStage = listOf(PipelineStage(initialRefId, stage))
            return Stages(initialRefId,
                    initialStage,
                    initialStage,
                    StageGraph(initialStage, mapOf())
            )
        }
    }

    fun andThen(stage: Stage): Stages {
        val nextRefId = stageCount + 1
        val nextStage = listOf(PipelineStage(nextRefId, stage))
        val allStages = stageGraph.stages + nextStage
        val allStageRequirements = stageGraph.stageRequirements + mapOf(nextRefId to lastStages.map(PipelineStage::refId))
        return Stages(nextRefId,
                firstStages,
                nextStage,
                StageGraph(allStages, allStageRequirements)
        )
    }

    fun fanOut(stageGroups: List<Stages>): Stages {
        var nextRefId = stageCount
        var allTerminalStages: List<PipelineStage> = listOf()
        var newStages: List<PipelineStage> = listOf()
        var newStageRequirements: Map<Int, List<Int>> = mapOf()
        stageGroups.forEach {
            val initialStages = it.firstStages
            val terminalStages = it.lastStages
            var nextStages: List<PipelineStage> = listOf()
            it.stageGraph.stages.forEach {
                val pStage = PipelineStage(it.refId + nextRefId, it.attrs)
                newStages += pStage
                if (terminalStages.contains(it)) {
                    allTerminalStages += pStage
                }
                if (initialStages.contains(it)) {
                    nextStages += pStage
                }
            }
            val incrementedStageRequirements = it.stageGraph.stageRequirements.map {
                (it.key + nextRefId to it.value.map { it + nextRefId })
            }
            newStageRequirements += incrementedStageRequirements
            val requiredStagesForFanOut: Map<Int, List<Int>> = nextStages.map {
                (it.refId to lastStages.map { it.refId })
            }.toMap()
            newStageRequirements += requiredStagesForFanOut
            val largestRefId = it.stageGraph.stages.last().refId
            nextRefId = nextRefId + largestRefId
        }
        val allStages = stageGraph.stages + newStages
        val allStageRequirements = stageGraph.stageRequirements + newStageRequirements
        return Stages(nextRefId,
                firstStages,
                allTerminalStages,
                StageGraph(allStages, allStageRequirements)
        )
    }

    fun fanIn(stage: Stage): Stages {
        return andThen(stage)
    }

}
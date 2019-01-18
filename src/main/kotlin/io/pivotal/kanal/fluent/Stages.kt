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

import io.pivotal.kanal.json.Inject
import io.pivotal.kanal.model.PipelineStage
import io.pivotal.kanal.model.Stage
import io.pivotal.kanal.model.StageGraph
import java.lang.IllegalStateException

class Stages(
        val stageCount: Int = 0,
        val stageGraph: StageGraph = StageGraph(emptyList(), mapOf())
) {
    companion object Factory {
        @JvmStatic @JvmOverloads fun of(stage: Stage,
               inject: Inject? = null,
               refId: String? = null,
               requisiteStageRefIds: List<String> = emptyList()
        ): Stages {
            return Stages().insertStage(
                    stage,
                    inject,
                    refId,
                    requisiteStageRefIds
            )
        }
    }

    val firstStages by lazy {
        val stagesThatRequireStages = stageGraph.stageRequirements.keys
        stageGraph.stages.filter {
            !(stagesThatRequireStages.contains(it.refId))
        }
    }

    val lastStages by lazy {
        val stagesThatAreRequiredByStages = stageGraph.stageRequirements.values.flatten().distinct()
        stageGraph.stages.filter {
            !(stagesThatAreRequiredByStages.contains(it.refId))
        }
    }

    @JvmOverloads fun and(stage: Stage,
            inject: Inject? = null,
            refId: String? = null,
            requisiteStageRefIds: List<String> = emptyList()
    ): Stages {
        return insertStage(
                stage,
                inject,
                refId,
                requisiteStageRefIds
        )
    }

    @JvmOverloads fun andThen(stage: Stage,
                inject: Inject? = null,
                refId: String? = null,
                requisiteStageRefIds: List<String> = emptyList()
    ): Stages {
        return insertStage(
                stage,
                inject,
                refId,
                requisiteStageRefIds + lastStages.map(PipelineStage::refId)
        )
    }

    private fun insertStage(stage: Stage,
                              inject: Inject?,
                              refId: String?,
                              requisiteStageRefIds: List<String>
    ): Stages {
        val nextStageCount = stageCount + 1
        val nextRefId = refId ?: stage.type + nextStageCount.toString()
        val nextStage = listOf(PipelineStage(nextRefId, stage, inject))
        val allStages = stageGraph.stages + nextStage
        val allStageRequirements = if (requisiteStageRefIds.isEmpty()) {
            stageGraph.stageRequirements
        } else {
            stageGraph.stageRequirements + mapOf(nextRefId to requisiteStageRefIds)
        }
        return Stages(nextStageCount,
                StageGraph(allStages, allStageRequirements)
        )
    }

    @JvmOverloads fun parallel(vararg stages: Stage): Stages {
        val stageGroups: List<Stages> = stages.map { Stages.of(it) }
        return parallel(stageGroups)
    }

    @JvmOverloads fun parallel(vararg stageGroups: Stages): Stages {
        return parallel(stageGroups.asList())
    }

    @JvmOverloads fun parallel(stageGroups: List<Stages>): Stages {
        var currentStageCount = stageCount
        var newStages: List<PipelineStage> = emptyList()
        var newStageRequirements: Map<String, List<String>> = mapOf()
        stageGroups.forEach {
            val initialStages = it.firstStages
            var nextStages: List<PipelineStage> = emptyList()
            val currentStageGraph = it.stageGraph
            var subStageGraphStageRequirements = currentStageGraph.stageRequirements
            it.stageGraph.stages.forEach {
                val oldRefId = it.refId
                val newRefId = "${oldRefId}_${++currentStageCount}"
                val pStage = it.copy(refId = newRefId)
                newStages += pStage
                if (initialStages.contains(it)) {
                    nextStages += pStage
                }
                if (currentStageGraph.stageRequirements.containsKey(newRefId)) {
                    throw IllegalStateException("New RefId '$newRefId' is already used as a key in stage graph: $currentStageGraph")
                }
                if (currentStageGraph.stageRequirements.values.flatten().contains(newRefId)) {
                    throw IllegalStateException("New RefId '$newRefId' is already used as a value in stage graph: $currentStageGraph")
                }
                subStageGraphStageRequirements = subStageGraphStageRequirements.entries.associate {
                    val key = if (it.key == oldRefId) newRefId else it.key
                    val value = it.value.map { if (it == oldRefId) newRefId else it}
                    key to value
                }
            }
            newStageRequirements += subStageGraphStageRequirements
            val requiredStagesForFanOut = nextStages.map {
                (it.refId to lastStages.map { it.refId })
            }.toMap()
            newStageRequirements += requiredStagesForFanOut
        }
        val allStages = stageGraph.stages + newStages
        val allStageRequirements = stageGraph.stageRequirements + newStageRequirements
        return Stages(currentStageCount,
                StageGraph(allStages, allStageRequirements)
        )
    }

}
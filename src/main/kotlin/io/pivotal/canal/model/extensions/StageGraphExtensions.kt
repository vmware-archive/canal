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

package io.pivotal.canal.model.extensions

import io.pivotal.canal.extensions.builder.SpecificStage
import io.pivotal.canal.model.*
import java.lang.IllegalStateException


fun stageGraphFor(specificStage: SpecificStage): StageGraph {
    val initialRefId = specificStage.execution.refId ?: specificStage.stageConfig.type + "_1"
    val pipelineStage = PipelineStage(initialRefId, specificStage.stageConfig, specificStage.base, specificStage.execution.inject)
    return StageGraph(listOf(pipelineStage), emptyMap())
}

val StageGraph.initialStages: List<PipelineStage> get() {
    val stagesThatRequireStages = this.stageRequirements.keys
    return this.stages.filter {
        !(stagesThatRequireStages.contains(it.refId))
    }
}

val StageGraph.terminalStages: List<PipelineStage> get() {
    val stagesThatAreRequiredByStages = this.stageRequirements.values.flatten().distinct()
    return this.stages.filter {
        !(stagesThatAreRequiredByStages.contains(it.refId))
    }
}

fun StageGraph.concat(stageGraphs: List<StageGraph>): StageGraph {
    var currentStageCount = this.stages.size
    var newStages: List<PipelineStage> = emptyList()
    var newStageRequirements: Map<String, List<String>> = mapOf()
    stageGraphs.forEach {
        val initialStages = it.initialStages
        var nextStages: List<PipelineStage> = emptyList()
        val currentStageGraph = it
        var subStageGraphStageRequirements = currentStageGraph.stageRequirements
        it.stages.forEach {
            val oldRefId = it.refId
            val oldRefIdWithoutStageCountSuffix = oldRefId.substring(0, oldRefId.lastIndexOf("_"))
            val newRefId = "${oldRefIdWithoutStageCountSuffix}_${++currentStageCount}"
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
            (it.refId to terminalStages.map { it.refId })
        }.toMap()
        newStageRequirements += requiredStagesForFanOut
    }
    val allStages = this.stages + newStages
    val allStageRequirements = this.stageRequirements + newStageRequirements
    return StageGraph(allStages, allStageRequirements)
}

fun StageGraph.union(stageGraphs: List<StageGraph>): StageGraph {
    var currentStageCount = this.stages.size
    var newStages: List<PipelineStage> = emptyList()
    var newStageRequirements: Map<String, List<String>> = mapOf()
    stageGraphs.forEach {
        val currentStageGraph = it
        var subStageGraphStageRequirements = currentStageGraph.stageRequirements
        it.stages.forEach {
            val oldRefId = it.refId
            val oldRefIdWithoutStageCountSuffix = oldRefId.substring(0, oldRefId.lastIndexOf("_"))
            val newRefId = "${oldRefIdWithoutStageCountSuffix}_${++currentStageCount}"
            val pStage = it.copy(refId = newRefId)
            newStages += pStage
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
    }
    val allStages = this.stages + newStages
    val allStageRequirements = this.stageRequirements + newStageRequirements
    return StageGraph(allStages, allStageRequirements)
}

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

import io.pivotal.canal.extensions.builder.SpecificStageBuilder
import io.pivotal.canal.extensions.builder.StageGrapher
import io.pivotal.canal.model.*
import java.lang.IllegalStateException

class StageGraphExtensions {
    companion object {
        @JvmStatic
        fun stageGraphFor(vararg stages: SpecificStageBuilder<*, *>): StageGrapher {
            return stageGraphFor(stages.toList())
        }
        @JvmStatic
        fun stageGraphFor(stages: List<SpecificStageBuilder<*, *>>): StageGrapher {
            return stageGraph(stages)
        }
    }
}

fun stageGraph(vararg stages: SpecificStageBuilder<*, *>): StageGrapher {
    return stageGraph(stages.toList())
}

fun stageGraph(stages: List<SpecificStageBuilder<*, *>>): StageGrapher {
    val pipelineStages = stages.map {
        val stage = it.build()
        val refId = stage.execution.refId ?: stage.stageConfig.type + "_1"
        PipelineStage(refId, stage.stageConfig, stage.base, stage.execution.inject)
    }
    return StageGrapher(StageGraph(pipelineStages, emptyMap()))
}

val StageGraph.initialStages: List<PipelineStage> get() {
    val stagesThatRequireStages = this.stageRequirements.keys.filter { !stageRequirements.get(it)!!.isEmpty() }
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
    stageGraphs.forEach { stageGraph ->
        val initialStages = stageGraph.initialStages
        var nextStages: List<PipelineStage> = emptyList()
        var subStageGraphStageRequirements = stageGraph.stageRequirements
        stageGraph.stages.forEach {
            val oldRefId = it.refId
            val newRefId = "${oldRefId}_${++currentStageCount}"
            val pStage = it.copy(refId = newRefId)
            newStages += pStage
            if (initialStages.contains(it)) {
                nextStages += pStage
            }
            if (stageGraph.stageRequirements.containsKey(newRefId)) {
                throw IllegalStateException("New RefId '$newRefId' is already used as a key in appending stage graph: $stageGraph")
            }
            if (stageGraph.stageRequirements.values.flatten().contains(newRefId)) {
                throw IllegalStateException("New RefId '$newRefId' is already used as a value in appending stage graph: $stageGraph")
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
    var refIdTransforms: Map<String, String> = mapOf()
    newStages.forEach {
        val oldRefId = it.refId
        val oldRefIdWithoutStageCountSuffix = oldRefId.substring(0, oldRefId.indexOf("_"))
        val stageCountSuffix = oldRefId.substring(oldRefId.lastIndexOf("_"), oldRefId.length)
        val newRefId = oldRefIdWithoutStageCountSuffix + stageCountSuffix
        if (oldRefId != newRefId) {
            refIdTransforms += (oldRefId to newRefId)
        }
    }
    val normalizedNewStages: List<PipelineStage> = newStages.map {
        val newRefId = refIdTransforms.get(it.refId)
        if (newRefId != null) {
            it.copy(refId = newRefId)
        } else {
            it
        }
    }
    val normalizedNewStageRequirements: Map<String, List<String>> = newStageRequirements.entries.associate {
        val newKey = refIdTransforms.get(it.key)
        val key = if (newKey != null) newKey else it.key
        val value = it.value.map {
            val newValue = refIdTransforms.get(it)
            if (newValue != null) newValue else it
        }
        key to value
    }
    val allStages = this.stages + normalizedNewStages
    val allStageRequirements = this.stageRequirements + normalizedNewStageRequirements
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

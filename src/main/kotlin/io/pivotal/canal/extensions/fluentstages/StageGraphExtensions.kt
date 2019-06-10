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

package io.pivotal.canal.extensions.fluentstages

import io.pivotal.canal.model.*
import java.lang.IllegalStateException

val Stages.firstStages: List<PipelineStage> get() {
    val stagesThatRequireStages = this.stageRequirements.keys
    return this.stages.filter {
        !(stagesThatRequireStages.contains(it.refId))
    }
}

val Stages.lastStages: List<PipelineStage> get() {
    val stagesThatAreRequiredByStages = this.stageRequirements.values.flatten().distinct()
    return this.stages.filter {
        !(stagesThatAreRequiredByStages.contains(it.refId))
    }
}

val Stages.stageCount: Int get() {
    return this.stages.size
}

private fun Stages.insertStage(stageConfig: SpecificStageConfig,
                               base: BaseStage? = BaseStage(),
                               execution: StageExecution = StageExecution()
): Stages {
    val nextStageCount = stageCount + 1
    val nextRefId = execution.refId ?: stageConfig.type + nextStageCount.toString()
    val nextStage = listOf(PipelineStage(nextRefId, stageConfig, base, execution.inject))
    val allStages = this.stages + nextStage
    val allStageRequirements = if (execution.requisiteStageRefIds.isEmpty()) {
        this.stageRequirements
    } else {
        this.stageRequirements + mapOf(nextRefId to execution.requisiteStageRefIds)
    }
    return Stages(allStages, allStageRequirements)
}

fun Stages.addStage(stageConfig: SpecificStageConfig,
                    base: BaseStage? = BaseStage(),
                    execution: StageExecution = StageExecution()
): Stages {
    return insertStage(
            stageConfig,
            base,
            execution
    )
}

fun Stages.andThen(stageConfig: SpecificStageConfig,
                   base: BaseStage? = BaseStage(),
                   execution: StageExecution = StageExecution()
): Stages {
    return insertStage(
            stageConfig,
            base,
            execution.copy(requisiteStageRefIds = execution.requisiteStageRefIds  + lastStages.map(PipelineStage::refId))
    )
}

fun Stages.parallelStages(vararg stageConfigs: SpecificStageConfig): Stages {
    return parallelStages(stageConfigs.toList())
}

fun Stages.parallelStages(stageConfigs: List<SpecificStageConfig>): Stages {
    val stageGroups: List<Stages> = stageConfigs.map { Stages().insertStage(it) }
    return parallel(stageGroups)
}

fun Stages.parallel(vararg stages: Stages): Stages {
    return parallel(stages.asList())
}

fun Stages.parallel(stages: List<Stages>): Stages {
    var currentStageCount = stageCount
    var newStages: List<PipelineStage> = emptyList()
    var newStageRequirements: Map<String, List<String>> = mapOf()
    stages.forEach {
        val initialStages = it.firstStages
        var nextStages: List<PipelineStage> = emptyList()
        val currentStageGraph = it
        var subStageGraphStageRequirements = currentStageGraph.stageRequirements
        it.stages.forEach {
            val oldRefId = it.refId
            val newRefId = "${oldRefId.subSequence(0, oldRefId.length-1)}${++currentStageCount}"
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
    val allStages = this.stages + newStages
    val allStageRequirements = this.stageRequirements + newStageRequirements
    return Stages(allStages, allStageRequirements)
}

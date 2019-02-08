package io.pivotal.kanal.extensions.fluentstages

import io.pivotal.kanal.model.*
import java.lang.IllegalStateException

val StageGraph.firstStages: List<PipelineStage> get() {
    val stagesThatRequireStages = this.stageRequirements.keys
    return this.stages.filter {
        !(stagesThatRequireStages.contains(it.refId))
    }
}

val StageGraph.lastStages: List<PipelineStage> get() {
    val stagesThatAreRequiredByStages = this.stageRequirements.values.flatten().distinct()
    return this.stages.filter {
        !(stagesThatAreRequiredByStages.contains(it.refId))
    }
}

val StageGraph.stageCount: Int get() {
    return this.stages.size
}

private fun StageGraph.insertStage(stageConfig: StageConfig,
                                   base: BaseStage? = BaseStage(),
                                   execution: StageExecution = StageExecution()
): StageGraph {
    val nextStageCount = stageCount + 1
    val nextRefId = execution.refId ?: stageConfig.type + nextStageCount.toString()
    val nextStage = listOf(PipelineStage(nextRefId, stageConfig, base, execution.inject))
    val allStages = this.stages + nextStage
    val allStageRequirements = if (execution.requisiteStageRefIds.isEmpty()) {
        this.stageRequirements
    } else {
        this.stageRequirements + mapOf(nextRefId to execution.requisiteStageRefIds)
    }
    return StageGraph(allStages, allStageRequirements)
}

fun StageGraph.addStage(stageConfig: StageConfig,
                        base: BaseStage? = BaseStage(),
                        execution: StageExecution = StageExecution()
): StageGraph {
    return insertStage(
            stageConfig,
            base,
            execution
    )
}

fun StageGraph.andThen(stageConfig: StageConfig,
                       base: BaseStage? = BaseStage(),
                       execution: StageExecution = StageExecution()
): StageGraph {
    return insertStage(
            stageConfig,
            base,
            execution.copy(requisiteStageRefIds = execution.requisiteStageRefIds  + lastStages.map(PipelineStage::refId))
    )
}

fun StageGraph.parallelStages(vararg stageConfigs: StageConfig): StageGraph {
    return parallelStages(stageConfigs.toList())
}

fun StageGraph.parallelStages(stageConfigs: List<StageConfig>): StageGraph {
    val stageGroups: List<StageGraph> = stageConfigs.map { StageGraph().insertStage(it) }
    return parallel(stageGroups)
}

fun StageGraph.parallel(vararg stageGraphs: StageGraph): StageGraph {
    return parallel(stageGraphs.asList())
}

fun StageGraph.parallel(stageGraphs: List<StageGraph>): StageGraph {
    var currentStageCount = stageCount
    var newStages: List<PipelineStage> = emptyList()
    var newStageRequirements: Map<String, List<String>> = mapOf()
    stageGraphs.forEach {
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
    return StageGraph(allStages, allStageRequirements)
}

package io.pivotal.kanal.extensions

import io.pivotal.kanal.model.Inject
import io.pivotal.kanal.model.PipelineStage
import io.pivotal.kanal.model.Stage
import io.pivotal.kanal.model.StageGraph
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

private fun StageGraph.insertStage(stage: Stage,
                                   inject: Inject? = null,
                                   refId: String? = null,
                                   requisiteStageRefIds: List<String> = emptyList()
): StageGraph {
    val nextStageCount = stageCount + 1
    val nextRefId = refId ?: stage.type + nextStageCount.toString()
    val nextStage = listOf(PipelineStage(nextRefId, stage, inject))
    val allStages = this.stages + nextStage
    val allStageRequirements = if (requisiteStageRefIds.isEmpty()) {
        this.stageRequirements
    } else {
        this.stageRequirements + mapOf(nextRefId to requisiteStageRefIds)
    }
    return StageGraph(allStages, allStageRequirements)
}

fun StageGraph.with(stage: Stage,
                   inject: Inject? = null,
                   refId: String? = null,
                   requisiteStageRefIds: List<String> = emptyList()
): StageGraph {
    return insertStage(
            stage,
            inject,
            refId,
            requisiteStageRefIds
    )
}

fun StageGraph.andThen(stage: Stage,
                          inject: Inject? = null,
                          refId: String? = null,
                          requisiteStageRefIds: List<String> = emptyList()
): StageGraph {
    return insertStage(
            stage,
            inject,
            refId,
            requisiteStageRefIds + lastStages.map(PipelineStage::refId)
    )
}

fun StageGraph.parallelStages(vararg stages: Stage): StageGraph {
    return parallelStages(stages.toList())
}

fun StageGraph.parallelStages(stages: List<Stage>): StageGraph {
    val stageGroups: List<StageGraph> = stages.map { StageGraph().insertStage(it) }
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
    val allStages = this.stages + newStages
    val allStageRequirements = this.stageRequirements + newStageRequirements
    return StageGraph(allStages, allStageRequirements)
}

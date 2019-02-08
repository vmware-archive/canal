package io.pivotal.kanal.extensions.nestedstages

import io.pivotal.kanal.model.*

infix fun StageGraph.with(stageDefOperation: StageDef.() -> Unit): StageGraph {
    val currentStageGraph = MutableRefStageGraph(this)
    val nsg = StageDef(currentStageGraph, emptyList())
    nsg.stageDefOperation()
    return currentStageGraph.stageGraph
}

private val StageGraph.terminalStages: List<PipelineStage> get() {
    val stagesThatAreRequiredByStages = this.stageRequirements.values.flatten().distinct()
    return this.stages.filter {
        !(stagesThatAreRequiredByStages.contains(it.refId))
    }
}

class MutableRefStageGraph(var stageGraph: StageGraph)

class StageDef(val current: MutableRefStageGraph, specifiedTerminalIds : List<String>? = null ) {

    val currentTerminalIds = specifiedTerminalIds ?: current.stageGraph.terminalStages.map { it.refId }

    fun stage(stage: Stage,
              name: String? = null,
              comments: String? = null,
              stageEnabled: Condition? = null,
              execution: StageExecution = StageExecution()): SingleStage {
        val newStageRequirements = execution.requisiteStageRefIds + currentTerminalIds
        val newStage = current.stageGraph.newStage(stage,
                BaseStage(name, comments, stageEnabled),
                execution.refId,
                execution.inject
        )
        current.stageGraph = current.stageGraph.insertStage(
                newStage,
                newStageRequirements)
        return SingleStage(current, newStage.refId)
    }

    private fun StageGraph.newStage(stage: Stage,
                                    base: BaseStage?,
                                    refId: String?,
                                    inject: Inject?

    ): PipelineStage {
        val nextStageCount = stages.size + 1
        val nextRefId = refId ?: stage.type + nextStageCount.toString()
        return PipelineStage(nextRefId, stage, base, inject)
    }

    private fun StageGraph.insertStage(stage: PipelineStage,
                                       requisiteStageRefIds: List<String>
    ): StageGraph {
        val allStages = this.stages + listOf(stage)
        val allStageRequirements = if (requisiteStageRefIds.isEmpty()) {
            this.stageRequirements
        } else {
            this.stageRequirements + mapOf(stage.refId to requisiteStageRefIds)
        }
        return StageGraph(allStages, allStageRequirements)
    }
}

interface StageDefInvoker {
    infix fun then(stageDef: StageDef.() -> Unit): ParallelStages
}

class ParallelStages(val current: MutableRefStageGraph, specifiedTerminalIds : List<String>? = null) : StageDefInvoker {

    val currentTerminalIds = specifiedTerminalIds ?: current.stageGraph.terminalStages.map { it.refId }

    override infix fun then(stageDefOperation: StageDef.() -> Unit): ParallelStages {
        val nsg = StageDef(current, currentTerminalIds)
        nsg.stageDefOperation()
        return ParallelStages(current)
    }

}

class SingleStage(val current: MutableRefStageGraph, val terminalId: String) : StageDefInvoker {

    override infix fun then(stageDefOperation: StageDef.() -> Unit): ParallelStages {
        val nsg = StageDef(current, listOf(terminalId))
        nsg.stageDefOperation()
        return ParallelStages(current)
    }

}

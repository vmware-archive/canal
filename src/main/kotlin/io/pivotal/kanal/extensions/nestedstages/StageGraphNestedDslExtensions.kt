package io.pivotal.kanal.extensions.nestedstages

import io.pivotal.kanal.model.*

infix fun StageGraph.with(
        nestedStageGraph: NestedStageGraph.() -> Unit): StageGraph {
    val nsg = NestedStageGraph(this, emptyList())
    nsg.nestedStageGraph()
    return nsg.currentStageGraph
}


class NestedStageGraph(val initialStageGraph: StageGraph, val terminalIds : List<String>) {

    var currentStageGraph = initialStageGraph

    fun stage(stage: Stage,
              name: String? = null,
              comments: String? = null,
              stageEnabled: Condition? = null,
              execution: StageExecution = StageExecution(),
              nestedStageGraph: NestedStageGraph.() -> Unit = {}): NestedStageGraph {
        val newStageTerminalIds = execution.requisiteStageRefIds + terminalIds
        val newStage = currentStageGraph.newStage(stage,
                BaseStage(name, comments, stageEnabled),
                execution.refId,
                execution.inject
        )
        currentStageGraph = currentStageGraph.insertStage(
                newStage,
                newStageTerminalIds)
        val nsg = NestedStageGraph(currentStageGraph, listOf(newStage.refId))
        nsg.nestedStageGraph()
        currentStageGraph = nsg.currentStageGraph
        return this
    }

    infix fun then(nestedStageGraph: NestedStageGraph.() -> Unit): NestedStageGraph {
        val nsg = NestedStageGraph(currentStageGraph, currentStageGraph.terminalStages.map{ it.refId })
        nsg.nestedStageGraph()
        currentStageGraph = nsg.currentStageGraph
        return this
    }

    val StageGraph.terminalStages: List<PipelineStage> get() {
        val stagesThatAreRequiredByStages = this.stageRequirements.values.flatten().distinct()
        return this.stages.filter {
            !(stagesThatAreRequiredByStages.contains(it.refId))
        }
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
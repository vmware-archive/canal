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

package io.pivotal.canal.extensions.nestedstages

import io.pivotal.canal.model.*

fun stages (stageDefOperation: StageDef.() -> Unit): Stages {
    val currentStageGraph = MutableRefStageGraph(Stages())
    val nsg = StageDef(currentStageGraph, emptyList())
    nsg.stageDefOperation()
    return currentStageGraph.stages
}

private val Stages.terminalStages: List<PipelineStage> get() {
    val stagesThatAreRequiredByStages = this.stageRequirements.values.flatten().distinct()
    return this.stages.filter {
        !(stagesThatAreRequiredByStages.contains(it.refId))
    }
}

class MutableRefStageGraph(var stages: Stages)

class StageFlags(
        val completeOtherBranchesThenFail: Boolean? = null,
        val continuePipeline: Boolean? = null,
        val failPipeline: Boolean? = null,
        val failOnFailedExpressions: Boolean? = null
)

class StageDef(val current: MutableRefStageGraph, specifiedTerminalIds : List<String>? = null ) {

    val currentTerminalIds = specifiedTerminalIds ?: current.stages.terminalStages.map { it.refId }

    fun stage(stageConfig: SpecificStageConfig,
              name: String? = null,
              comments: String? = null,
              stageEnabled: Condition? = null,
              notifications: List<Notification>? = null,
              stageFlags: StageFlags = StageFlags(),
              restrictedExecutionWindow: RestrictedExecutionWindow? = null,
              execution: StageExecution = StageExecution()): SingleStage {
        val newStageRequirements = execution.requisiteStageRefIds + currentTerminalIds
        val newStage = current.stages.newStage(stageConfig,
                BaseStage(
                        name,
                        comments,
                        stageEnabled,
                        notifications,
                        stageFlags.completeOtherBranchesThenFail,
                        stageFlags.continuePipeline,
                        stageFlags.failPipeline,
                        stageFlags.failOnFailedExpressions,
                        restrictedExecutionWindow
                ),
                execution.refId,
                execution.inject
        )
        current.stages = current.stages.insertStage(
                newStage,
                newStageRequirements)
        return SingleStage(current, newStage.refId)
    }

    private fun Stages.newStage(stageConfig: SpecificStageConfig,
                                base: BaseStage?,
                                refId: String?,
                                inject: Inject?

    ): PipelineStage {
        val nextStageCount = stages.size + 1
        val nextRefId = refId ?: stageConfig.type + nextStageCount.toString()
        return PipelineStage(nextRefId, stageConfig, base, inject)
    }

    private fun Stages.insertStage(stage: PipelineStage,
                                   requisiteStageRefIds: List<String>
    ): Stages {
        val allStages = this.stages + listOf(stage)
        val allStageRequirements = if (requisiteStageRefIds.isEmpty()) {
            this.stageRequirements
        } else {
            this.stageRequirements + mapOf(stage.refId to requisiteStageRefIds)
        }
        return Stages(allStages, allStageRequirements)
    }
}

interface StageDefInvoker {
    infix fun then(stageDef: StageDef.() -> Unit): ParallelStages
}

class ParallelStages(val current: MutableRefStageGraph, specifiedTerminalIds : List<String>? = null) : StageDefInvoker {

    val currentTerminalIds = specifiedTerminalIds ?: current.stages.terminalStages.map { it.refId }

    override infix fun then(stageDef: StageDef.() -> Unit): ParallelStages {
        val nsg = StageDef(current, currentTerminalIds)
        nsg.stageDef()
        return ParallelStages(current)
    }

}

class SingleStage(val current: MutableRefStageGraph, val terminalId: String) : StageDefInvoker {

    override infix fun then(stageDef: StageDef.() -> Unit): ParallelStages {
        val nsg = StageDef(current, listOf(terminalId))
        nsg.stageDef()
        return ParallelStages(current)
    }

}

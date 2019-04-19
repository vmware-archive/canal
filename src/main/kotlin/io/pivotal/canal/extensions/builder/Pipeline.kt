package io.pivotal.canal.extensions.builder

import io.pivotal.canal.model.*

data class Defaults(var delegate: PipelineDefaults = PipelineDefaults()) {
    fun region(region: String) = apply { this.delegate = delegate.copy(region = region) }
    fun account(account: String) = apply { this.delegate = delegate.copy(account = account) }
    fun cloudProvider(cloudProvider: CloudProvider) = apply { this.delegate = delegate.copy(cloudProvider = cloudProvider) }
}

data class PipelineDefaults(val region: String? = null,
                            val account: String? = null,
                            val cloudProvider: CloudProvider? = null)

interface NextStageGraphObject

class StageGrapher : NextStageGraphObject {

    private var stageGraph = StageGraph()

    fun then(vararg nextStageGraphObjects: NextStageGraphObject) : StageGrapher {
        return then(nextStageGraphObjects.toList())
    }

    fun then(nextStageGraphObjects: List<NextStageGraphObject>) : StageGrapher {

        return this
    }

    fun graph() : StageGraph {
        return stageGraph
    }
}

open abstract class SpecificStageBuilder<T : SpecificStageConfig, U : SpecificStageBuilder<T, U>>(val defaults: PipelineDefaults) : NextStageGraphObject {
    abstract fun specificStageConfig(): T
    var common: BaseStage = BaseStage()
    var execution: StageExecution = StageExecution()

    fun name(name: String): U = apply { common = common.copy(name = name) } as U
    fun comments(comments: String): U = apply { common = common.copy(comments = comments) } as U
    fun stageEnabled(stageEnabled: Condition): U = apply { common = common.copy(stageEnabled = stageEnabled) } as U
    fun notifications(notifications: List<Notification>): U = apply { common = common.copy(notifications = notifications) } as U
    fun completeOtherBranchesThenFail(b: Boolean): U = apply { common = common.copy(completeOtherBranchesThenFail = b) } as U
    fun continuePipeline(b: Boolean): U = apply { common = common.copy(continuePipeline = b) } as U
    fun failPipeline(b: Boolean): U = apply { common = common.copy(failPipeline = b) } as U
    fun failOnFailedExpressions(b: Boolean): U = apply { common = common.copy(failOnFailedExpressions = b) } as U
    fun restrictedExecutionWindow(restrictedExecutionWindow: RestrictedExecutionWindow): U = apply { common = common.copy(restrictedExecutionWindow = restrictedExecutionWindow) } as U

    fun execution(execution: StageExecution): U = apply { this.execution = execution } as U

    fun build(): CompleteStage {
        return CompleteStage(
                specificStageConfig(),
                common,
                execution
        )
    }
}

data class CompleteStage(
        val stageConfig: SpecificStageConfig,
        val base: BaseStage,
        val execution: StageExecution
)

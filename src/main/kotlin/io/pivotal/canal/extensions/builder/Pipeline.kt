package io.pivotal.canal.extensions.builder

import io.pivotal.canal.model.*
import io.pivotal.canal.model.extensions.*
import java.util.*

data class Defaults(var delegate: PipelineDefaults = PipelineDefaults()) {
    fun application(application: String) = apply { this.delegate = delegate.copy(application = application) }
    fun region(region: String) = apply { this.delegate = delegate.copy(region = region) }
    fun account(account: String) = apply { this.delegate = delegate.copy(account = account) }
}

class DefaultsForStages() {
    var stageCatalogs = emptyList<CloudStageCatalog>()
    var currentDefaults = PipelineDefaults()
    val scopes: Stack<PipelineDefaults> = Stack()
    init {
        scopes.push(currentDefaults)
    }

    fun application(application: String) = apply { currentDefaults = currentDefaults.copy(application = application) }
    fun region(region: String) = apply { currentDefaults = currentDefaults.copy(region = region) }
    fun account(account: String) = apply { currentDefaults = currentDefaults.copy(account = account) }

    fun forStages(stagesDef: () -> StageGraphable) : StageGrapher {
        scopes.push(currentDefaults)
        stageCatalogs.forEach{ it.defaults = currentDefaults }
        val stageGrapher = StageGrapher(stagesDef().toGraph().graph())
        scopes.pop()
        stageCatalogs.forEach{ it.defaults = scopes.peek() }
        return stageGrapher
    }

    fun registerCatalog(stageCatalog: CloudStageCatalog) {
        stageCatalogs = stageCatalogs + stageCatalog
    }

}

data class PipelineDefaults(val application: String? = null,
                            val region: String? = null,
                            val account: String? = null)

interface StageGraphable {
    fun toGraph(): StageGrapher
}

class StageGrapher(var currentStages: Stages = Stages()) : StageGraphable {

    fun then(vararg stageGraphables: StageGraphable) : StageGrapher {
        return then(stageGraphables.toList())
    }

    fun then(stageGraphables: List<StageGraphable>) : StageGrapher {
        return StageGrapher(currentStages.concat(stageGraphables.map { it.toGraph().currentStages }))
    }

    fun graph() : Stages {
        return currentStages
    }

    override fun toGraph(): StageGrapher {
        return this
    }
}

open abstract class SpecificStageBuilder<T : SpecificStageConfig, U : SpecificStageBuilder<T, U>> : StageGraphable {
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

    fun build(): SpecificStage {
        return SpecificStage(
                specificStageConfig(),
                common,
                execution
        )
    }

    override fun toGraph(): StageGrapher {
        return stageGraph(this)
    }
}

data class SpecificStage(
        val stageConfig: SpecificStageConfig,
        val base: BaseStage,
        val execution: StageExecution
)

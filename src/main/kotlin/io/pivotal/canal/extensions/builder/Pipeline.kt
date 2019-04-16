package io.pivotal.canal.extensions.builder

import io.pivotal.canal.json.JsonAdapterFactory
import io.pivotal.canal.model.*

import io.pivotal.canal.extensions.fluentstages.*

abstract class Pipeline(val name: String,
                        val defaults: PipelineDefaults = PipelineDefaults()) {
    constructor(name: String, defaults: Defaults) : this(name, defaults.delegate)

    abstract fun stages(): StageGraph

    fun stage(): StageCatalog {
        return StageCatalog(stageConfig())
    }

    fun stage(name: String): StageCatalog {
        return StageCatalog(stageConfig().name(name))
    }

    fun stage(config: StageConfig): StageCatalog {
        return StageCatalog(StageConfig(defaults, config.config, config.execution))
    }

    fun stageGraph(vararg specificStageBuilders: SpecificStageBuilder<*>): StageGrapher {
        return StageGrapher()
    }

    fun stageConfig(): StageConfig {
        return StageConfig(defaults)
    }

    fun toJson(): String {
        val adapter = JsonAdapterFactory().createAdapter<PipelineModel>()
        val model = PipelineModel(
                name,
                stageGraph = stages()
        )
        return adapter.toJson(model)
    }
}

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

    var stageGraph = StageGraph()

    fun then(vararg nextStageGraphObjects: NextStageGraphObject) : StageGrapher {
        return then(nextStageGraphObjects.toList())
    }

    fun then(nextStageGraphObjects: List<NextStageGraphObject>) : StageGrapher {
        stageGraph.parallelStages(nextStageGraphObjects)
        stageGraph.parallel(nextStageGraphObjects)
        return this
    }

    fun graph() : StageGraph {
        stageGraph
    }
}

open abstract class SpecificStageBuilder<T : SpecificStageConfig>(val config: StageConfig) : NextStageGraphObject {
    abstract fun specificStageConfig(): T

    fun build(): CompleteStage {
        return CompleteStage(
                specificStageConfig(),
                config.config,
                config.execution
        )
    }
}

data class CompleteStage(
        val stageConfig: SpecificStageConfig,
        val base: BaseStage? = null,
        val execution: StageExecution = StageExecution()
)

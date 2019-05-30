package io.pivotal.canal.extensions.builder

import io.pivotal.canal.model.*
import io.pivotal.canal.model.cloudfoundry.Artifact
import io.pivotal.canal.model.cloudfoundry.CloudFoundryStageCatalog
import io.pivotal.canal.model.cloudfoundry.Manifest
import java.time.Duration

abstract class CloudStageCatalog {
    abstract val cloudProvider: CloudProvider
    abstract var defaults: PipelineDefaults

    @JvmOverloads fun destroyServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            assign: (DestroyServerGroupStageBuilder) -> DestroyServerGroupStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(DestroyServerGroupStageBuilder(defaults, cloudProvider, clusterName, target)).build())
    }

    @JvmOverloads fun disableServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            assign: (DisableServerGroupStageBuilder) -> DisableServerGroupStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(DisableServerGroupStageBuilder(defaults, cloudProvider, clusterName, target)).build())
    }

    @JvmOverloads fun enableServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            assign: (EnableServerGroupStageBuilder) -> EnableServerGroupStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(EnableServerGroupStageBuilder(defaults, cloudProvider, clusterName, target)).build())
    }

    @JvmOverloads fun resizeServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            resizeAction: ResizeAction,
            assign: (ResizeServerGroupStageBuilder) -> ResizeServerGroupStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(ResizeServerGroupStageBuilder(defaults, cloudProvider, clusterName, target, resizeAction)).build())
    }

    @JvmOverloads fun rollback(
            clusterName: String,
            assign: (RollbackStageBuilder) -> RollbackStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(RollbackStageBuilder(defaults, cloudProvider, clusterName)).build())
    }

}

class DestroyServerGroupStageBuilder(val defaults: PipelineDefaults,
                                     val provider: CloudProvider,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var regions: List<String>? = null) : SpecificStageBuilder<DestroyServerGroup, DestroyServerGroupStageBuilder>() {

    override fun specificStageConfig() = DestroyServerGroup(
            provider,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target
    )

    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class DisableServerGroupStageBuilder(val defaults: PipelineDefaults,
                                     val provider: CloudProvider,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var regions: List<String>? = null) : SpecificStageBuilder<DisableServerGroup, DisableServerGroupStageBuilder>() {

    override fun specificStageConfig() = DisableServerGroup(
            provider,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target
    )

    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class EnableServerGroupStageBuilder(val defaults: PipelineDefaults,
                                    val provider: CloudProvider,
                                    val clusterName: String,
                                    val target: TargetServerGroup,
                                    var regions: List<String>? = null) : SpecificStageBuilder<EnableServerGroup, EnableServerGroupStageBuilder>() {

    override fun specificStageConfig() = EnableServerGroup(
            provider,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target
    )

    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class ResizeServerGroupStageBuilder(val defaults: PipelineDefaults,
                                    val provider: CloudProvider,
                                    val clusterName: String,
                                    val target: TargetServerGroup,
                                    val resizeAction: ResizeAction,
                                    var regions: List<String>? = null,
                                    var memory: Int = 1024,
                                    var diskQuota: Int = 1024) : SpecificStageBuilder<ResizeServerGroup, ResizeServerGroupStageBuilder>() {

    override fun specificStageConfig() = ResizeServerGroup(
            provider,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target,
            resizeAction,
            memory,
            diskQuota
    )

    fun regions(regions: List<String>) = apply { this.regions = regions }
    fun memory(memory: Int) = apply { this.memory = memory }
    fun diskQuota(diskQuota: Int) = apply { this.diskQuota = diskQuota }
}

abstract class DeployStageBuilder<T : SpecificStageBuilder<Deploy, T>>(var application: String? = null,
                                                                       var account: String? = null,
                                                                       var region: String? = null,
                                                                       var strategy: DeploymentStrategy = DeploymentStrategy.None,
                                                                       var capacity: Capacity = Capacity(1),
                                                                       var stack: String = "",
                                                                       var detail: String = "",
                                                                       var startApplication: Boolean? = null
                                                                   ) : SpecificStageBuilder<Deploy, T>() {

    fun application(application: String) = apply { this.application = application } as T
    fun account(account: String) = apply { this.account = account } as T
    fun region(region: String) = apply { this.region = region } as T
    fun strategy(strategy: DeploymentStrategy) = apply { this.strategy = strategy } as T
    fun capacity(capacity: Capacity) = apply { this.capacity = capacity } as T
    fun stack(stack: String) = apply { this.stack = stack } as T
    fun detail(detail: String) = apply { this.detail = detail } as T
    fun startApplication(startApplication: Boolean) = apply { this.startApplication = startApplication } as T
}

class RollbackStageBuilder(val defaults: PipelineDefaults,
                           val provider: CloudProvider,
                           val clusterName: String,
                           val targetHealthyRollbackPercentage: Int = 100,
                           var regions: List<String>? = null) : SpecificStageBuilder<Rollback, RollbackStageBuilder>() {

    override fun specificStageConfig() = Rollback(
            provider,
            regions ?: listOf(defaults.region!!),
            clusterName,
            targetHealthyRollbackPercentage
    )

    fun regions(regions: List<String>) = apply { this.regions = regions }
}

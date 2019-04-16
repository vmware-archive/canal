package io.pivotal.canal.extensions.builder

import io.pivotal.canal.model.*
import java.time.Duration

data class StageCatalog(val config: StageConfig) {
    fun checkPreconditions(vararg preconditions: Precondition): CheckPreconditionsStageBuilder {
        return CheckPreconditionsStageBuilder(config, preconditions.asList())
    }

    fun wait(duration: Duration): WaitStageBuilder {
        return WaitStageBuilder(config, duration.seconds.toString())
    }

    fun wait(expression: String): WaitStageBuilder {
        return WaitStageBuilder(config, expression)
    }

    fun jenkins(job: String, master: String): JenkinsStageBuilder {
        return JenkinsStageBuilder(config, job, master)
    }

    fun manualJudgment(): ManualJudgmentStageBuilder {
        return ManualJudgmentStageBuilder(config)
    }

    fun webhook(method: String, url: String): WebhookStageBuilder {
        return WebhookStageBuilder(config, method, url)
    }

    fun canary(analysisType: String, canaryConfig: CanaryConfig): CanaryStageBuilder {
        return CanaryStageBuilder(config, analysisType, canaryConfig)
    }

    fun destroyServerGroup(clusterName: String, target: TargetServerGroup): DestroyServerGroupStageBuilder {
        return DestroyServerGroupStageBuilder(config, clusterName, target)
    }

    fun deployService(): DeployServiceStageBuilder {
        return DeployServiceStageBuilder(config)
    }

    fun destroyService(serviceName: String): DestroyServiceStageBuilder {
        return DestroyServiceStageBuilder(config, serviceName)
    }

    fun disableServerGroup(clusterName: String, target: TargetServerGroup): DisableServerGroupStageBuilder {
        return DisableServerGroupStageBuilder(config, clusterName, target)
    }

    fun enableServerGroup(clusterName: String, target: TargetServerGroup): EnableServerGroupStageBuilder {
        return EnableServerGroupStageBuilder(config, clusterName, target)
    }

    fun resizeServerGroup(clusterName: String,
                          target: TargetServerGroup,
                          resizeAction: ResizeAction): ResizeServerGroupStageBuilder {
        return ResizeServerGroupStageBuilder(config, clusterName, target, resizeAction)
    }

    fun deploy(vararg clusters: Cluster): DeployStageBuilder {
        return DeployStageBuilder(config, clusters.asList())
    }

    fun rollback(clusterName: String): RollbackStageBuilder {
        return RollbackStageBuilder(config, clusterName)
    }


}

class CheckPreconditionsStageBuilder(config: StageConfig, val preconditions: List<Precondition>) : SpecificStageBuilder<CheckPreconditions>(config) {
    override fun specificStageConfig() = CheckPreconditions(preconditions)
}

class WaitStageBuilder(config: StageConfig, val expression: String) : SpecificStageBuilder<Wait>(config) {
    override fun specificStageConfig() = Wait(expression)
}

class JenkinsStageBuilder (config: StageConfig,
                          val job: String,
                          val master: String,
                          var parameters: Map<String, String> = emptyMap(),
                          var waitForCompletion: Boolean = true) : SpecificStageBuilder<Jenkins>(config) {
    override fun specificStageConfig() = Jenkins(job, master, parameters, waitForCompletion)

    fun parameters(parameters: Map<String, String>) = apply { this.parameters = parameters }
    fun waitForCompletion(b: Boolean) = apply { waitForCompletion = b }
}

class ManualJudgmentStageBuilder(config: StageConfig,
                                 var instructions: String? = null,
                                 var judgmentInputs: List<String> = emptyList()) : SpecificStageBuilder<ManualJudgment>(config) {
    override fun specificStageConfig() = ManualJudgment(instructions, judgmentInputs)

    fun instructions(instructions: String) = apply { this.instructions = instructions }
    fun judgmentInputs(judgmentInputs: List<String>) = apply { this.judgmentInputs = judgmentInputs }
}

class WebhookStageBuilder(config: StageConfig,
                          val method: String,
                          val url: String,
                          var waitForCompletion: Boolean = true) : SpecificStageBuilder<Webhook>(config) {
    override fun specificStageConfig() = Webhook(method, url, waitForCompletion)

    fun waitForCompletion(b: Boolean) = apply { waitForCompletion = waitForCompletion }
}

class CanaryStageBuilder(config: StageConfig,
                         val analysisType: String,
                         val canaryConfig: CanaryConfig) : SpecificStageBuilder<Canary>(config) {
    override fun specificStageConfig() = Canary(analysisType, canaryConfig)
}

class DestroyServerGroupStageBuilder(config: StageConfig,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var provider: CloudProvider? = null,
                                     var regions: List<String>? = null) : SpecificStageBuilder<DestroyServerGroup>(config) {

    override fun specificStageConfig() = DestroyServerGroup(
            provider ?: config.defaults.cloudProvider!!,
            regions ?: listOf(config.defaults.region!!),
            clusterName,
            target
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}


class DeployServiceStageBuilder(config: StageConfig,
                                var provider: CloudProvider? = null,
                                var region: String? = null) : SpecificStageBuilder<DeployService>(config) {
    override fun specificStageConfig() = DeployService(
            provider ?: config.defaults.cloudProvider!!,
            region ?: config.defaults.region!!)

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun region(region: String) = apply { this.region = region }
}

class DestroyServiceStageBuilder(config: StageConfig,
                                 val serviceName: String,
                                 var provider: CloudProvider? = null,
                                 var region: String? = null,
                                 var timeout: String? = null) : SpecificStageBuilder<DestroyService>(config) {
    override fun specificStageConfig() = DestroyService(
            provider ?: config.defaults.cloudProvider!!,
            region ?: config.defaults.region!!,
            serviceName,
            timeout)

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun region(region: String) = apply { this.region = region }
    fun timeout(timeout: String) = apply { this.timeout = timeout }
}

class DisableServerGroupStageBuilder(config: StageConfig,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var provider: CloudProvider? = null,
                                     var regions: List<String>? = null) : SpecificStageBuilder<DisableServerGroup>(config) {

    override fun specificStageConfig() = DisableServerGroup(
            provider ?: config.defaults.cloudProvider!!,
            regions ?: listOf(config.defaults.region!!),
            clusterName,
            target
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class EnableServerGroupStageBuilder(config: StageConfig,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var provider: CloudProvider? = null,
                                     var regions: List<String>? = null) : SpecificStageBuilder<EnableServerGroup>(config) {

    override fun specificStageConfig() = EnableServerGroup(
            provider ?: config.defaults.cloudProvider!!,
            regions ?: listOf(config.defaults.region!!),
            clusterName,
            target
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class ResizeServerGroupStageBuilder(config: StageConfig,
                                    val clusterName: String,
                                    val target: TargetServerGroup,
                                    val resizeAction: ResizeAction,
                                    var provider: CloudProvider? = null,
                                    var regions: List<String>? = null,
                                    var memory: Int = 1024,
                                    var diskQuota: Int = 1024) : SpecificStageBuilder<ResizeServerGroup>(config) {

    override fun specificStageConfig() = ResizeServerGroup(
            provider ?: config.defaults.cloudProvider!!,
            regions ?: listOf(config.defaults.region!!),
            clusterName,
            target,
            resizeAction,
            memory,
            diskQuota
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
    fun memory(memory: Int) = apply { this.memory = memory }
    fun diskQuota(diskQuota: Int) = apply { this.diskQuota = diskQuota }
}

class DeployStageBuilder(config: StageConfig,
                         val clusters: List<Cluster>) : SpecificStageBuilder<Deploy>(config) {
    override fun specificStageConfig() = Deploy(clusters)
}

class RollbackStageBuilder(config: StageConfig,
                                    val clusterName: String,
                                    val targetHealthyRollbackPercentage: Int = 100,
                                    var provider: CloudProvider? = null,
                                    var regions: List<String>? = null) : SpecificStageBuilder<Rollback>(config) {

    override fun specificStageConfig() = Rollback(
            provider ?: config.defaults.cloudProvider!!,
            regions ?: listOf(config.defaults.region!!),
            clusterName,
            targetHealthyRollbackPercentage
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class StageConfig(val defaults: PipelineDefaults,
                  var config: BaseStage = BaseStage(),
                  var execution: StageExecution = StageExecution()) {

    fun name(name: String) = apply { config = config.copy(name = name) }
    fun comments(comments: String) = apply { config = config.copy(comments = comments) }
    fun stageEnabled(condition: Condition) = apply { config = config.copy(stageEnabled = condition) }
    fun notifications(notifications: List<Notification>) = apply { config = config.copy(notifications = notifications) }
    fun completeOtherBranchesThenFail(b: Boolean) = apply { config = config.copy(completeOtherBranchesThenFail = b) }
    fun continuePipeline(b: Boolean) = apply { config = config.copy(continuePipeline = b) }
    fun failPipeline(b: Boolean) = apply { config = config.copy(failPipeline = b) }
    fun failOnFailedExpressions(b: Boolean) = apply { config = config.copy(failOnFailedExpressions = b) }
    fun restrictedExecutionWindow(restrictedExecutionWindow: RestrictedExecutionWindow) = apply { config = config.copy(restrictedExecutionWindow = restrictedExecutionWindow) }

    fun execution(execution: StageExecution) = apply { this.execution = execution }
}
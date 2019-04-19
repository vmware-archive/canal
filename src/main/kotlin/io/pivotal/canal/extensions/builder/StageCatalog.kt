package io.pivotal.canal.extensions.builder

import io.pivotal.canal.model.*
import java.time.Duration

data class StageCatalog(val defaults: PipelineDefaults, val stageGrapher: StageGrapher) {
    fun checkPreconditions(assign: (CheckPreconditionsStageBuilder) -> CheckPreconditionsStageBuilder = { it }): StageGrapher {
        assign(CheckPreconditionsStageBuilder(defaults))
        return stageGrapher
    }

    @JvmOverloads fun wait(
            duration: Duration,
            assign: (WaitStageBuilder) -> WaitStageBuilder = { it }): StageGrapher {
        assign(WaitStageBuilder(defaults, duration.seconds.toString()))
        return stageGrapher
    }

    @JvmOverloads fun wait(
            expression: String,
            assign: (WaitStageBuilder) -> WaitStageBuilder = { it }): StageGrapher {
        assign(WaitStageBuilder(defaults, expression))
        return stageGrapher
    }

    @JvmOverloads fun jenkins(
            job: String,
            master: String,
            assign: (JenkinsStageBuilder) -> JenkinsStageBuilder = { it }): StageGrapher {
        assign(JenkinsStageBuilder(defaults, job, master))
        return stageGrapher
    }

    @JvmOverloads fun manualJudgment(
            assign: (ManualJudgmentStageBuilder) -> ManualJudgmentStageBuilder = { it }): StageGrapher {
        assign(ManualJudgmentStageBuilder(defaults))
        return stageGrapher
    }

    @JvmOverloads fun webhook(
            method: String, url: String,
            assign: (WebhookStageBuilder) -> WebhookStageBuilder = { it }): StageGrapher {
        assign(WebhookStageBuilder(defaults, method, url))
        return stageGrapher
    }

    @JvmOverloads fun canary(
            analysisType: String,
            canaryConfig: CanaryConfig,
            assign: (CanaryStageBuilder) -> CanaryStageBuilder = { it }): StageGrapher {
        assign(CanaryStageBuilder(defaults, analysisType, canaryConfig))
        return stageGrapher
    }

    @JvmOverloads fun destroyServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            assign: (DestroyServerGroupStageBuilder) -> DestroyServerGroupStageBuilder = { it }): StageGrapher {
        assign(DestroyServerGroupStageBuilder(defaults, clusterName, target))
        return stageGrapher
    }

    @JvmOverloads fun deployService(
            assign: (DeployServiceStageBuilder) -> DeployServiceStageBuilder = { it }): StageGrapher {
        assign(DeployServiceStageBuilder(defaults))
        return stageGrapher
    }

    @JvmOverloads fun destroyService(
            serviceName: String,
            assign: (DestroyServiceStageBuilder) -> DestroyServiceStageBuilder = { it }): StageGrapher {
        assign(DestroyServiceStageBuilder(defaults, serviceName))
        return stageGrapher
    }

    @JvmOverloads fun disableServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            assign: (DisableServerGroupStageBuilder) -> DisableServerGroupStageBuilder = { it }): StageGrapher {
        assign(DisableServerGroupStageBuilder(defaults, clusterName, target))
        return stageGrapher
    }

    @JvmOverloads fun enableServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            assign: (EnableServerGroupStageBuilder) -> EnableServerGroupStageBuilder = { it }): StageGrapher {
        assign(EnableServerGroupStageBuilder(defaults, clusterName, target))
        return stageGrapher
    }

    @JvmOverloads fun resizeServerGroup(
            clusterName: String,
            target: TargetServerGroup,
            resizeAction: ResizeAction,
            assign: (ResizeServerGroupStageBuilder) -> ResizeServerGroupStageBuilder = { it }): StageGrapher {
        assign(ResizeServerGroupStageBuilder(defaults, clusterName, target, resizeAction))
        return stageGrapher
    }

    @JvmOverloads fun deploy(
            assign: (DeployStageBuilder) -> DeployStageBuilder = { it }): StageGrapher {
        assign(DeployStageBuilder(defaults))
        return stageGrapher
    }

    @JvmOverloads fun rollback(
            clusterName: String,
            assign: (RollbackStageBuilder) -> RollbackStageBuilder = { it }): StageGrapher {
        assign(RollbackStageBuilder(defaults, clusterName))
        return stageGrapher
    }


}

class CheckPreconditionsStageBuilder(
        defaults: PipelineDefaults,
        var preconditions: List<Precondition>? = null) : SpecificStageBuilder<CheckPreconditions, CheckPreconditionsStageBuilder>(defaults) {
    override fun specificStageConfig() = CheckPreconditions(preconditions!!)

    fun preconditions(vararg preconditions: Precondition) = apply { this.preconditions = preconditions.toList() }
}

class WaitStageBuilder(defaults: PipelineDefaults, val expression: String) : SpecificStageBuilder<Wait, WaitStageBuilder>(defaults) {
    override fun specificStageConfig() = Wait(expression)
}

class JenkinsStageBuilder (defaults: PipelineDefaults,
                           val job: String,
                           val master: String,
                           var parameters: Map<String, String> = emptyMap(),
                           var waitForCompletion: Boolean = true) : SpecificStageBuilder<Jenkins, JenkinsStageBuilder>(defaults) {
    override fun specificStageConfig() = Jenkins(job, master, parameters, waitForCompletion)

    fun parameters(parameters: Map<String, String>) = apply { this.parameters = parameters }
    fun waitForCompletion(b: Boolean) = apply { waitForCompletion = b }
}

class ManualJudgmentStageBuilder(defaults: PipelineDefaults,
                                 var instructions: String? = null,
                                 var judgmentInputs: List<String> = emptyList()) : SpecificStageBuilder<ManualJudgment, ManualJudgmentStageBuilder>(defaults) {
    override fun specificStageConfig() = ManualJudgment(instructions, judgmentInputs)

    fun instructions(instructions: String) = apply { this.instructions = instructions }
    fun judgmentInputs(judgmentInputs: List<String>) = apply { this.judgmentInputs = judgmentInputs }
}

class WebhookStageBuilder(defaults: PipelineDefaults,
                          val method: String,
                          val url: String,
                          var waitForCompletion: Boolean = true) : SpecificStageBuilder<Webhook, WebhookStageBuilder>(defaults) {
    override fun specificStageConfig() = Webhook(method, url, waitForCompletion)

    fun waitForCompletion(b: Boolean) = apply { waitForCompletion = waitForCompletion }
}

class CanaryStageBuilder(defaults: PipelineDefaults,
                         val analysisType: String,
                         val canaryConfig: CanaryConfig) : SpecificStageBuilder<Canary, CanaryStageBuilder>(defaults) {
    override fun specificStageConfig() = Canary(analysisType, canaryConfig)
}

class DestroyServerGroupStageBuilder(defaults: PipelineDefaults,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var provider: CloudProvider? = null,
                                     var regions: List<String>? = null) : SpecificStageBuilder<DestroyServerGroup, DestroyServerGroupStageBuilder>(defaults) {

    override fun specificStageConfig() = DestroyServerGroup(
            provider ?: defaults.cloudProvider!!,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}


class DeployServiceStageBuilder(defaults: PipelineDefaults,
                                var provider: CloudProvider? = null,
                                var region: String? = null) : SpecificStageBuilder<DeployService, DeployServiceStageBuilder>(defaults) {
    override fun specificStageConfig() = DeployService(
            provider ?: defaults.cloudProvider!!,
            region ?: defaults.region!!)

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun region(region: String) = apply { this.region = region }
}

class DestroyServiceStageBuilder(defaults: PipelineDefaults,
                                 val serviceName: String,
                                 var provider: CloudProvider? = null,
                                 var region: String? = null,
                                 var timeout: String? = null) : SpecificStageBuilder<DestroyService, DestroyServiceStageBuilder>(defaults) {
    override fun specificStageConfig() = DestroyService(
            provider ?: defaults.cloudProvider!!,
            region ?: defaults.region!!,
            serviceName,
            timeout)

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun region(region: String) = apply { this.region = region }
    fun timeout(timeout: String) = apply { this.timeout = timeout }
}

class DisableServerGroupStageBuilder(defaults: PipelineDefaults,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var provider: CloudProvider? = null,
                                     var regions: List<String>? = null) : SpecificStageBuilder<DisableServerGroup, DisableServerGroupStageBuilder>(defaults) {

    override fun specificStageConfig() = DisableServerGroup(
            provider ?: defaults.cloudProvider!!,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class EnableServerGroupStageBuilder(defaults: PipelineDefaults,
                                     val clusterName: String,
                                     val target: TargetServerGroup,
                                     var provider: CloudProvider? = null,
                                     var regions: List<String>? = null) : SpecificStageBuilder<EnableServerGroup, EnableServerGroupStageBuilder>(defaults) {

    override fun specificStageConfig() = EnableServerGroup(
            provider ?: defaults.cloudProvider!!,
            regions ?: listOf(defaults.region!!),
            clusterName,
            target
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class ResizeServerGroupStageBuilder(defaults: PipelineDefaults,
                                    val clusterName: String,
                                    val target: TargetServerGroup,
                                    val resizeAction: ResizeAction,
                                    var provider: CloudProvider? = null,
                                    var regions: List<String>? = null,
                                    var memory: Int = 1024,
                                    var diskQuota: Int = 1024) : SpecificStageBuilder<ResizeServerGroup, ResizeServerGroupStageBuilder>(defaults) {

    override fun specificStageConfig() = ResizeServerGroup(
            provider ?: defaults.cloudProvider!!,
            regions ?: listOf(defaults.region!!),
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

class DeployStageBuilder(defaults: PipelineDefaults,
                         var clusters: List<Cluster>? = null) : SpecificStageBuilder<Deploy, DeployStageBuilder>(defaults) {
    override fun specificStageConfig() = Deploy(clusters!!)

    fun clusters(clusters: List<Cluster>) = apply { this.clusters = clusters }
}

class RollbackStageBuilder(defaults: PipelineDefaults,
                                    val clusterName: String,
                                    val targetHealthyRollbackPercentage: Int = 100,
                                    var provider: CloudProvider? = null,
                                    var regions: List<String>? = null) : SpecificStageBuilder<Rollback, RollbackStageBuilder>(defaults) {

    override fun specificStageConfig() = Rollback(
            provider ?: defaults.cloudProvider!!,
            regions ?: listOf(defaults.region!!),
            clusterName,
            targetHealthyRollbackPercentage
    )

    fun provider(provider: CloudProvider) = apply { this.provider = provider }
    fun regions(regions: List<String>) = apply { this.regions = regions }
}

class StageConfig(var config: BaseStage = BaseStage(),
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
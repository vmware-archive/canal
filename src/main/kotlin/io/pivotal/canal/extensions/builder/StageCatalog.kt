package io.pivotal.canal.extensions.builder

import io.pivotal.canal.model.*
import java.time.Duration

class StageCatalog {

    fun checkPreconditions(): CheckPreconditionsStageBuilder {
        return CheckPreconditionsStageBuilder()
    }

    @JvmOverloads fun wait(
            duration: Duration): WaitStageBuilder {
        return wait(duration.seconds.toString())
    }

    @JvmOverloads fun wait(expression: String): WaitStageBuilder {
        return WaitStageBuilder(expression)
    }

    @JvmOverloads fun jenkins(
            job: String,
            master: String): JenkinsStageBuilder {
        return JenkinsStageBuilder(job, master)
    }

    @JvmOverloads fun manualJudgment(): ManualJudgmentStageBuilder {
        return ManualJudgmentStageBuilder()
    }

    @JvmOverloads fun webhook(method: String, url: String): WebhookStageBuilder {
        return WebhookStageBuilder(method, url)
    }

    @JvmOverloads fun canary(
            analysisType: String,
            canaryConfig: CanaryConfig): CanaryStageBuilder {
        return CanaryStageBuilder(analysisType, canaryConfig)
    }

}

class CheckPreconditionsStageBuilder(
        var preconditions: List<Precondition>? = null) : SpecificStageBuilder<CheckPreconditions, CheckPreconditionsStageBuilder>() {
    override fun specificStageConfig() = CheckPreconditions(preconditions!!)

    fun preconditions(vararg preconditions: Precondition) = apply { this.preconditions = preconditions.toList() }
}

class WaitStageBuilder(val expression: String) : SpecificStageBuilder<Wait, WaitStageBuilder>() {
    override fun specificStageConfig() = Wait(expression)
}

class JenkinsStageBuilder (val job: String,
                           val master: String,
                           var parameters: Map<String, String> = emptyMap(),
                           var waitForCompletion: Boolean = true) : SpecificStageBuilder<Jenkins, JenkinsStageBuilder>() {
    override fun specificStageConfig() = Jenkins(job, master, parameters, waitForCompletion)

    fun parameters(parameters: Map<String, String>) = apply { this.parameters = parameters }
    fun waitForCompletion(b: Boolean) = apply { waitForCompletion = b }
}

class ManualJudgmentStageBuilder(var instructions: String? = null,
                                 var judgmentInputs: List<String> = emptyList()) : SpecificStageBuilder<ManualJudgment, ManualJudgmentStageBuilder>() {
    override fun specificStageConfig() = ManualJudgment(instructions, judgmentInputs)

    fun instructions(instructions: String) = apply { this.instructions = instructions }
    fun judgmentInputs(judgmentInputs: List<String>) = apply { this.judgmentInputs = judgmentInputs }
}

class WebhookStageBuilder(val method: String,
                          val url: String,
                          var waitForCompletion: Boolean = true) : SpecificStageBuilder<Webhook, WebhookStageBuilder>() {
    override fun specificStageConfig() = Webhook(method, url, waitForCompletion)

    fun waitForCompletion(b: Boolean) = apply { waitForCompletion = waitForCompletion }
}

class CanaryStageBuilder(val analysisType: String,
                         val canaryConfig: CanaryConfig) : SpecificStageBuilder<Canary, CanaryStageBuilder>() {
    override fun specificStageConfig() = Canary(analysisType, canaryConfig)
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
package io.pivotal.kanal.model

interface Stage : Typed

data class CheckPreconditionsStage(
        val preconditions: List<Precondition>
) : Stage {
    constructor(vararg preconditions: Precondition) : this(preconditions.toList())
    override val type = "checkPreconditions"
}

data class WaitStage(
        val waitTime: String
) : Stage {
    constructor(waitTime: Long) : this(waitTime.toString())
    override val type = "wait"
}

data class JenkinsStage @JvmOverloads constructor(
        val job: String,
        val master: String,
        val parameters: Map<String, String> = emptyMap(),
        val waitForCompletion: Boolean = true
) : Stage {
    override val type = "jenkins"
}

data class ManualJudgmentStage @JvmOverloads constructor(
        val instructions: String? = null,
        val judgmentInputs: List<String> = emptyList()
) : Stage {
    override val type = "manualJudgment"
}

data class WebhookStage @JvmOverloads constructor(
        val method: String,
        val url: String,
        val user: String,
        val waitForCompletion: Boolean = true
) : Stage {
    override val type = "webhook"
}

data class CanaryStage(
        val analysisType: String,
        val canaryConfig: CanaryConfig

) : Stage {
    override val type = "kayentaCanary"
}

data class CanaryConfig(
        val lifetimeDuration: String,
        val scoreThresholds: ScoreThresholds,
        val storageAccountName: String,
        val metricsAccountName: String
)

data class ScoreThresholds(
        val marginal: String,
        val pass: String
) {
    constructor(marginal: Int,
                pass: Int) : this(marginal.toString(), pass.toString())
}

interface Region {
    val region: String
}

interface MultiRegion {
    val regions: List<String>
}

interface CloudSpecific {
    val provider: CloudProvider
}

interface CloudProvider {
    val cloudProvider: String
    val cloudProviderType: String
    val credentials: String
}

data class DestroyServerGroupStage(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String
) : Stage, CloudSpecific, MultiRegion {
    override val type = "destroyServerGroup"
}

data class DeployServiceStage(
        override val provider: CloudProvider,
        override val region: String
) : Stage, CloudSpecific, Region {
    override val type = "deployService"
    var action = type
}

data class DestroyServiceStage @JvmOverloads constructor(
        override val provider: CloudProvider,
        override val region: String,
        val serviceName: String,
        val timeout: String? = null
) : Stage, CloudSpecific, Region {
    override val type = "destroyService"
    var action = type
}

data class DisableServerGroupStage(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String
) : Stage, CloudSpecific, MultiRegion {
    override val type = "disableServerGroup"
}

data class EnableServerGroupStage(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String
) : Stage, CloudSpecific, MultiRegion {
    override val type = "enableServerGroup"
}

data class ResizeServerGroupStage @JvmOverloads constructor(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String,
        val resizeAction: ResizeAction,
        val memory: Int = 1024,
        val diskQuota: Int = 1024
) : Stage, CloudSpecific, MultiRegion {
    override val type = "resizeServerGroup"
}

interface ResizeAction

data class ScaleExactResizeAction(
        val instanceCount: Int
) : ResizeAction {
    var action = "scale_exact"
    var capacity =  Capacity(instanceCount)
}

data class DeployStage(
        val clusters: List<Cluster>
) : Stage {
    constructor(cluster: Cluster) : this(listOf(cluster))
    override val type = "deploy"
}

interface Cluster {
    val capacity: Capacity
    val cloudProvider: String
}

data class Capacity(
        val desired: String,
        val max: String,
        val min: String
) {
    constructor(desired: Int,
                max: Int,
                min: Int) : this(desired.toString(), desired.toString(), desired.toString())
    constructor(desired: Int) : this(desired, desired, desired)
}

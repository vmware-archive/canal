package io.pivotal.canal.model

interface StageConfig : Typed

data class CheckPreconditions(
        val preconditions: List<Precondition>
) : StageConfig {
    constructor(vararg preconditions: Precondition) : this(preconditions.toList())
    override val type = "checkPreconditions"
}

data class Wait(
        val waitTime: String
) : StageConfig {
    constructor(waitTime: Long) : this(waitTime.toString())
    override val type = "wait"
}

data class Jenkins @JvmOverloads constructor(
        val job: String,
        val master: String,
        val parameters: Map<String, String> = emptyMap(),
        val waitForCompletion: Boolean = true
) : StageConfig {
    override val type = "jenkins"
}

data class ManualJudgment @JvmOverloads constructor(
        val instructions: String? = null,
        val judgmentInputs: List<String> = emptyList()
) : StageConfig {
    override val type = "manualJudgment"
}

data class Webhook @JvmOverloads constructor(
        val method: String,
        val url: String,
        val user: String,
        val waitForCompletion: Boolean = true
) : StageConfig {
    override val type = "webhook"
}

data class Canary(
        val analysisType: String,
        val canaryConfig: CanaryConfig

) : StageConfig {
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

data class DestroyServerGroup(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String
) : StageConfig, CloudSpecific, MultiRegion {
    override val type = "destroyServerGroup"
}

data class DeployService(
        override val provider: CloudProvider,
        override val region: String
) : StageConfig, CloudSpecific, Region {
    override val type = "deployService"
    var action = type
}

data class DestroyService @JvmOverloads constructor(
        override val provider: CloudProvider,
        override val region: String,
        val serviceName: String,
        val timeout: String? = null
) : StageConfig, CloudSpecific, Region {
    override val type = "destroyService"
    var action = type
}

data class DisableServerGroup(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String
) : StageConfig, CloudSpecific, MultiRegion {
    override val type = "disableServerGroup"
}

data class EnableServerGroup(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String
) : StageConfig, CloudSpecific, MultiRegion {
    override val type = "enableServerGroup"
}

data class ResizeServerGroup @JvmOverloads constructor(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: String,
        val resizeAction: ResizeAction,
        val memory: Int = 1024,
        val diskQuota: Int = 1024
) : StageConfig, CloudSpecific, MultiRegion {
    override val type = "resizeServerGroup"
}

interface ResizeAction

data class ScaleExactResizeAction(
        val instanceCount: Int
) : ResizeAction {
    var action = "scale_exact"
    var capacity =  Capacity(instanceCount)
}

data class Deploy(
        val clusters: List<Cluster>
) : StageConfig {
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

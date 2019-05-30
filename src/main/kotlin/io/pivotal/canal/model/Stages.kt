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

package io.pivotal.canal.model

import com.squareup.moshi.Json

interface SpecificStageConfig : Typed

data class CheckPreconditions(
        val preconditions: List<Precondition>
) : SpecificStageConfig {
    constructor(vararg preconditions: Precondition) : this(preconditions.toList())
    override val type = "checkPreconditions"
}

data class Wait(
        val waitTime: String
) : SpecificStageConfig {
    constructor(waitTime: Long) : this(waitTime.toString())
    override val type = "wait"
}

data class Jenkins (
        val job: String,
        val master: String,
        val parameters: Map<String, String> = emptyMap(),
        val waitForCompletion: Boolean = true
) : SpecificStageConfig {
    override val type = "jenkins"
}

data class ManualJudgment @JvmOverloads constructor(
        val instructions: String? = null,
        val judgmentInputs: List<String> = emptyList()
) : SpecificStageConfig {
    override val type = "manualJudgment"
}

data class Webhook (
        val method: String,
        val url: String,
        val waitForCompletion: Boolean = true
) : SpecificStageConfig {
    override val type = "webhook"
}

data class Canary(
        val analysisType: String,
        val canaryConfig: CanaryConfig

) : SpecificStageConfig {
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

data class CloudProvider constructor(
        val credentials: String,
        val cloudProvider: String
) {
    var cloudProviderType = cloudProvider
}

data class DestroyServerGroup(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: TargetServerGroup
) : SpecificStageConfig, CloudSpecific, MultiRegion {
    override val type = "destroyServerGroup"
}

enum class TargetServerGroup {
    @Json(name = "current_asg_dynamic") Newest,
    @Json(name = "ancestor_asg_dynamic") Previous,
    @Json(name = "oldest_asg_dynamic") Oldest
}

data class DisableServerGroup(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: TargetServerGroup
) : SpecificStageConfig, CloudSpecific, MultiRegion {
    override val type = "disableServerGroup"
}

data class EnableServerGroup(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: TargetServerGroup
) : SpecificStageConfig, CloudSpecific, MultiRegion {
    override val type = "enableServerGroup"
}

data class ResizeServerGroup (
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val target: TargetServerGroup,
        val resizeAction: ResizeAction,
        val memory: Int = 1024,
        val diskQuota: Int = 1024
) : SpecificStageConfig, CloudSpecific, MultiRegion {
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
) : SpecificStageConfig {
    constructor(cluster: Cluster) : this(listOf(cluster))
    override val type = "deploy"
}

interface Cluster {
    val application: String
    val account: String
    val region: String
    val stack: String
    val detail: String
    val startApplication: Boolean?
    val capacity: Capacity
    val cloudProvider: String
    val strategy: DeploymentStrategy
}

data class Capacity(
        val desired: String,
        val max: String,
        val min: String
) {
    constructor(desired: Int,
                max: Int,
                min: Int) : this(desired.toString(), max.toString(), min.toString())
    constructor(desired: Int) : this(desired, desired, desired)
}

data class Rollback(
        override val provider: CloudProvider,
        override val regions: List<String>,
        val cluster: String,
        val targetHealthyRollbackPercentage: Int
) : SpecificStageConfig, CloudSpecific, MultiRegion {
    override val type = "rollbackCluster"
    val moniker = Moniker(cluster, cluster)
}

data class Moniker(
        val app: String,
        val cluster: String,
        val sequence: String? = null
)
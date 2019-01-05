/*
 * Copyright 2018 Pivotal Software, Inc.
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

package io.pivotal.kanal.model

data class Pipeline(
        val description: String,
        val parameters: List<Parameter>,
        val notifications: List<Notification>,
        val triggers: List<Trigger>,
        val stageGraph: StageGraph
)

data class PipelineStage(
        val refId: Int,
        val attrs: Stage
)

data class StageGraph (
        val stages: List<PipelineStage>,
        val stageRequirements: Map<Int, List<Int>>
)

interface Named {
    val name: String
}

data class Parameter(
        override val name: String,
        val required: Boolean,
        val label: String,
        val description: String,
        val options: List<Value>,
        val default: String?
) : Named {
    constructor(name: String,
                label: String,
                required: Boolean,
                description: String,
                options: List<String> = listOf(),
                default: String?) : this(name, required, label, description, options.map { Value(it) }, default)
    var hasOptions = options.isNotEmpty()
}

data class Value (val value: String)

interface Typed {
    val type: String
}

interface HasCloudProvider {
    val cloudProvider: String
}

interface Stage : Named, Typed {
}

data class CheckPreconditionsStage(
        override val name: String,
        val preconditions: List<Precondition>
) : Stage {
    override val type = "checkPreconditions"
}

data class DestroyServerGroupStage(
        override val name: String,
        override val cloudProvider: String,
        val cluster: String,
        val credentials: String,
        val regions: List<String>,
        val stageEnabled: Condition,
        val target: String
) : Stage, HasCloudProvider {
    override val type = "destroyServerGroup"
    var cloudProviderType = cloudProvider
    var completeOtherBranchesThenFail = false
    var continuePipeline = true
    var failPipeline = false
}

data class DeployServiceStage(
        override val name: String,
        override val cloudProvider: String,
        val comments: String,
        val credentials: String,
        val parameters: String,
        val region: String,
        val service: String,
        val serviceName: String,
        val servicePlan: String,
        val stageEnabled: Condition,
        val tags: String
) : Stage, HasCloudProvider {
    override val type = "deployService"
    var action = type
    var cloudProviderType = cloudProvider
}

data class DestroyServiceStage(
        override val name: String,
        override val cloudProvider: String,
        val credentials: String,
        val region: String,
        val serviceName: String,
        val stageEnabled: Condition
) : Stage, HasCloudProvider {
    override val type = "destroyService"
    var action = type
    var cloudProviderType = cloudProvider
    var completeOtherBranchesThenFail = false
    var continuePipeline = true
    var failPipeline = false
}

data class WaitStage(
        override val name: String,
        val comments: String,
        val waitTime: String
) : Stage {
    constructor(name: String, comments: String, waitTime: Long) : this(name, comments, waitTime.toString())
    override val type = "wait"
}

interface Condition : Typed

data class ExpressionCondition(
        val expression: String
) : Condition {
    constructor(expression: Boolean) : this(expression.toString())
    override val type = "expression"
}

interface Precondition : Typed

data class ExpressionPrecondition(
        val context: ExpressionContext
) : Precondition {
    constructor(expression: String) : this(ExpressionContext(expression))

    override val type = "expression"
    var failPipeline = true
}

data class ExpressionContext(
        val expression: String
)

data class ManualJudgmentStage(
        override val name: String,
        val instructions: String,
        val notifications: List<Notification> = listOf(),
        val judgmentInputs: List<String> = listOf(),
        val failPipeline: Boolean = true
) : Stage {
    override val type = "manualJudgment"
}

data class WebhookStage(
        override val name: String,
        val method: String,
        val url: String,
        val user: String,
        val waitForCompletion: Boolean,
        val failPipeline: Boolean = true
) : Stage {
    override val type = "webhook"
}

data class CanaryStage(
        override val name: String,
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
        val marginal: Int,
        val pass: Int
)

data class DeployStage(
        override val name: String,
        val comments: String,
        val clusters: List<CloudFoundryCluster>,
        val stageEnabled: Condition
) : Stage {
    override val type = "deploy"
}

data class CloudFoundryCluster(
        val account: String,
        val region: String,
        val stack: String,
        val strategy: String,
        val startApplication: Boolean,
        val application: String,
        val freeFormDetails: String,
        val artifact: Artifact,
        val manifest: Manifest
) : HasCloudProvider {
    override var cloudProvider = "cloudfoundry"
    var provider = cloudProvider
}

interface Manifest : Typed {
    val account: String
}

data class ArtifactManifest(
        override val account: String,
        val reference: String
) : Manifest {
    override val type = "artifact"
}

interface Artifact : Typed

data class ReferencedArtifact(
        val account: String,
        val reference: String
) : Artifact {
    override val type = "artifact"
}


interface Notification : Typed

data class EmailNotification(
        val address: String,
        val level: String
) : Notification {
    override val type = "email"
}

interface Trigger : Typed {
    val enabled: Boolean
}

data class JenkinsTrigger(
        override val enabled: Boolean,
        val job: String,
        val master: String
) : Trigger {
    override val type = "jenkins"
}

data class GitTrigger(
        override val enabled: Boolean,
        val branch: String,
        val project: String,
        val secret: String,
        val slug: String,
        val source: String
) : Trigger {
    override val type = "git"
}
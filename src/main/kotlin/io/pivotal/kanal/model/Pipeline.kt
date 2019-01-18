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

@file:Suppress("unused")

package io.pivotal.kanal.model

import io.pivotal.kanal.json.Inject

data class Pipeline(
        val description: String = "",
        val parameters: List<Parameter> = emptyList(),
        val notifications: List<Notification> = emptyList(),
        val triggers: List<Trigger> = emptyList(),
        val stageGraph: StageGraph = StageGraph()
)

data class PipelineStage(
        val refId: String,
        val attrs: Stage,
        val inject: Inject? = null
) {
    constructor(refId: Int,
                attrs: Stage,
                inject: Inject? = null) : this(refId.toString(), attrs, inject)
}

data class StageGraph (
        val stages: List<PipelineStage> = emptyList(),
        val stageRequirements: Map<String, List<String>> = emptyMap()
)

interface Named {
    val name: String
}

data class Parameter(
        override val name: String,
        val required: Boolean,
        val label: String,
        val description: String,
        val options: List<Value> = emptyList(),
        val default: String? = null
) : Named {
    constructor(name: String,
                label: String,
                required: Boolean,
                description: String,
                options: List<String> = emptyList(),
                default: String? = null) : this(name, required, label, description, options.map { Value(it) }, default)
    var hasOptions = options.isNotEmpty()
}

data class Value (val value: String)

interface Typed {
    val type: String
}

interface HasCloudProvider {
    val cloudProvider: String
}

interface Stage : Named, Typed

data class CheckPreconditionsStage(
        override val name: String = "",
        val preconditions: List<Precondition>
) : Stage {
    override val type = "checkPreconditions"
}

data class DestroyServerGroupStage(
        override val name: String = "",
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
        override val name: String = "",
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
        override val name: String = "",
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
        val waitTime: String,
        val comments: String = "",
        override val name: String = ""
) : Stage {
    constructor(waitTime: Long, comments: String = "", name: String = "") : this(waitTime.toString(), comments, name)
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

data class ManualJudgmentStage @JvmOverloads constructor(
        override val name: String = "",
        val instructions: String,
        val notifications: List<Notification> = emptyList(),
        val judgmentInputs: List<String> = emptyList(),
        val failPipeline: Boolean = true
) : Stage {
    override val type = "manualJudgment"
}

data class WebhookStage(
        override val name: String = "",
        val method: String,
        val url: String,
        val user: String,
        val waitForCompletion: Boolean = true,
        val failPipeline: Boolean = true
) : Stage {
    override val type = "webhook"
}

data class CanaryStage(
        override val name: String = "",
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
        override val name: String = "",
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
        val job: String,
        val master: String,
        override val enabled: Boolean = true
) : Trigger {
    override val type = "jenkins"
}

data class GitTrigger(
        val branch: String,
        val project: String,
        val secret: String,
        val slug: String,
        val source: String,
        override val enabled: Boolean = true
) : Trigger {
    override val type = "git"
}

data class PubSubTrigger(
        val pubsubSystem: String,
        val subscription: String,
        val source: String,
        val attributeConstraints: Map<String, Any> = emptyMap(),
        val payloadConstraints: Map<String, Any> = emptyMap(),
        override val enabled: Boolean = true
) : Trigger {
    override val type = "pubsub"
    var subscriptionName = subscription
}
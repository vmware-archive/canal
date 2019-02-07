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

import com.squareup.moshi.Json

data class Pipeline(
        val description: String = "",
        @Json(name = "parameterConfig") val parameters: List<Parameter> = emptyList(),
        val notifications: List<Notification> = emptyList(),
        val triggers: List<Trigger> = emptyList(),
        val stages: StageGraph = StageGraph(),
        val expectedArtifacts: List<ExpectedArtifact> = emptyList(),
        val keepWaitingPipelines: Boolean = false,
        val limitConcurrent: Boolean = true
)

data class PipelineStage(
        val refId: String,
        val stage: Stage,
        val common: BaseStage? = null,
        val inject: Inject? = null
) {
    constructor(refId: Int,
                attrs: Stage,
                common: BaseStage? = BaseStage(),
                inject: Inject? = null) : this(refId.toString(), attrs, common, inject)
}

data class BaseStage @JvmOverloads constructor (
        val name: String? = null,
        val comments: String? = null,
        val stageEnabled: Condition? = null,
        val notifications: List<Notification>? = null,
        val completeOtherBranchesThenFail: Boolean? = null,
        val continuePipeline: Boolean? = null,
        val failPipeline: Boolean? = null,
        val failOnFailedExpressions: Boolean? = null,
        val restrictedExecutionWindow: RestrictedExecutionWindow? = null
) {
    val restrictExecutionDuringTimeWindow = restrictedExecutionWindow != null
}

data class StageExecution(
        val refId: String? = null,
        val requisiteStageRefIds: List<String> = emptyList(),
        val inject: Inject? = null
)

data class RestrictedExecutionWindow(
        val days: List<Int>,
        val whitelist: List<Whitelist>
)

data class Whitelist(
        val endHour: Int,
        val endMin: Int,
        val startHour: Int,
        val startMin: Int
)

sealed class Inject {
    data class Before(val before: String) : Inject() {
        val type = "before"
    }
    data class After(val after: String) : Inject() {
        val type = "after"
    }
    data class First(val first: Boolean = true) : Inject() {
        val type = "first"
    }
    data class Last(val last: Boolean = true) : Inject() {
        val type = "last"
    }
}

data class StageGraph (
        val stages: List<PipelineStage> = emptyList(),
        val stageRequirements: Map<String, List<String>> = emptyMap()
)

data class Parameter(
        val name: String,
        val required: Boolean = true,
        val label: String = "",
        val description: String = "",
        val options: List<Value> = emptyList(),
        val default: String? = null
) {
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

interface Condition : Typed

data class ExpressionCondition(
        val expression: String
) : Condition {
    constructor(expression: Boolean) : this(expression.toString())
    override val type = "expression"
}

val trueCondition = ExpressionCondition(true)
val falseCondition = ExpressionCondition(false)

interface Precondition : Typed

data class ExpressionPrecondition(
        val context: ExpressionContext
) : Precondition {
    constructor(expression: String) : this(ExpressionContext(expression))
    constructor(expression: Boolean) : this(expression.toString())

    override var type = "expression"
}

data class ExpressionContext(
        val expression: String
)

enum class DeploymentStrategy {
    @Json(name = "") None,
    @Json(name = "highlander") Highlander,
    @Json(name = "redblack") RedBlack
}

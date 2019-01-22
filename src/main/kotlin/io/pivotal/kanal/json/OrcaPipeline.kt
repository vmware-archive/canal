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

package io.pivotal.kanal.json

import com.squareup.moshi.*
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.pivotal.kanal.model.*
import io.pivotal.kanal.model.cloudfoundry.*
import java.lang.reflect.Type

data class OrcaPipeline (
        val description: String = "",
        val parameterConfig: List<Parameter> = emptyList(),
        val notifications: List<Notification> = emptyList(),
        val triggers: List<Trigger> = emptyList(),
        val stages: List<OrcaStage> = emptyList(),
        val appConfig: Map<String, Any> = mapOf(),
        val expectedArtifacts: List<Any> = emptyList(),
        val keepWaitingPipelines: Boolean = false,
        val lastModifiedBy: String = "anonymous",
        val limitConcurrent: Boolean = false,
        val updateTs: String = "0"
)

data class OrcaPipelineConfig (
        val application: String,
        val name: String,
        val template: TemplateSource,
        val description: String = "",
        val parameters: List<Parameter> = emptyList(),
        val notifications: List<Notification> = emptyList(),
        val triggers: List<Trigger> = emptyList(),
        val stages: List<OrcaStage>,
        val variables: Map<String, Any> = emptyMap(),
        val inherit: List<String> = emptyList(),
        val schema: String = "v2"
)

data class OrcaVariable(
        val name: String,
        val description: String,
        val type: String,
        val defaultValue: Any?,
        val example: String? = null,
        val nullable: Boolean = false,
        val merge: Boolean = false,
        val remove: Boolean = false
)

data class OrcaStage(
        val stage: Stage,
        val execution: StageExecution
)

data class StageExecution(
        val refId: String,
        val requisiteStageRefIds: List<String>,
        val inject: Inject? = null
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

class PipelineAdapter {
    @ToJson
    fun toJson(pipeline: Pipeline): OrcaPipeline {
        val stages = StageGraphAdapter().toJson(pipeline.stageGraph)
        return OrcaPipeline(
                pipeline.description,
                pipeline.parameters,
                pipeline.notifications,
                pipeline.triggers,
                stages,
                limitConcurrent = pipeline.limitConcurrent
        )
    }

    @FromJson
    fun fromJson(orcaPipeline: OrcaPipeline): Pipeline? {
        val stageGraph = StageGraphAdapter().fromJson(orcaPipeline.stages)
        return Pipeline(
                orcaPipeline.description,
                orcaPipeline.parameterConfig,
                orcaPipeline.notifications,
                orcaPipeline.triggers,
                stageGraph,
                limitConcurrent = orcaPipeline.limitConcurrent
        )
    }
}

class PipelineConfigAdapter {
    @ToJson
    fun toJson(pipelineConfig: PipelineConfig): OrcaPipelineConfig {
        val stages = StageGraphAdapter().toJson(pipelineConfig.pipeline.stageGraph)
        return OrcaPipelineConfig(
                pipelineConfig.application,
                pipelineConfig.name,
                pipelineConfig.template,
                pipelineConfig.pipeline.description,
                pipelineConfig.pipeline.parameters,
                pipelineConfig.pipeline.notifications,
                pipelineConfig.pipeline.triggers,
                stages,
                pipelineConfig.variables,
                pipelineConfig.inherit,
                pipelineConfig.schema
        )
    }

    @FromJson
    fun fromJson(orcaPipelineConfig: OrcaPipelineConfig): PipelineConfig? {
        val stageGraph = StageGraphAdapter().fromJson(orcaPipelineConfig.stages)
        return PipelineConfig(
                orcaPipelineConfig.application,
                orcaPipelineConfig.name,
                orcaPipelineConfig.template,
                Pipeline(
                        orcaPipelineConfig.description,
                        orcaPipelineConfig.parameters,
                        orcaPipelineConfig.notifications,
                        orcaPipelineConfig.triggers,
                        stageGraph
                ),
                orcaPipelineConfig.variables,
                orcaPipelineConfig.inherit,
                orcaPipelineConfig.schema
        )
    }
}

class VariableAdapter {
    @ToJson
    fun toJson(variable: Variable): OrcaVariable {
        return OrcaVariable(
                variable.name,
                variable.description,
                variable.typeAttrs.type,
                variable.typeAttrs.defaultValue,
                variable.example,
                variable.nullable,
                variable.merge,
                variable.remove
        )
    }

    @FromJson
    fun fromJson(orcaVariable: OrcaVariable): Variable? {
        val typeAttrs = when(orcaVariable.type) {
            "int" -> if (orcaVariable.defaultValue is Float) {
                IntegerType((orcaVariable.defaultValue as Float).toInt())
            } else {
                IntegerType(orcaVariable.defaultValue as Int)
            }
            "float" -> FloatType(orcaVariable.defaultValue as Float)
            "string" -> StringType(orcaVariable.defaultValue as String)
            "boolean" -> BooleanType(orcaVariable.defaultValue as Boolean)
            "list" -> ListType(orcaVariable.defaultValue as List<Any>)
            else -> ObjectType(orcaVariable.defaultValue)
        }
        return Variable(
                orcaVariable.name,
                orcaVariable.description,
                typeAttrs,
                orcaVariable.example,
                orcaVariable.nullable,
                orcaVariable.merge,
                orcaVariable.remove
        )
    }
}

class OrcaStageAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, value: OrcaStage) {
        val stageAdapter = JsonAdapterFactory().stageAdapter()
        val executionDetailsAdapter = JsonAdapterFactory().stageExecutionAdapter()
        writer.beginObject()
        val token = writer.beginFlatten()
        executionDetailsAdapter.toJson(writer, value.execution)
        stageAdapter.toJson(writer, value.stage)
        writer.endFlatten(token)
        writer.endObject()
    }

    @FromJson
    fun fromJson(map: Map<String, @JvmSuppressWildcards Any>): OrcaStage {
        val stageAdapter = JsonAdapterFactory().stageAdapter()
        val executionDetailsAdapter = JsonAdapterFactory().stageExecutionAdapter()
        val stage = stageAdapter.fromJsonValue(map)!!
        val execution = executionDetailsAdapter.fromJsonValue(map)!!
        return OrcaStage(stage, execution)
    }
}


class ExpressionConditionAdapter {
    @FromJson
    fun fromJson(map: Map<String, @JvmSuppressWildcards Any>): ExpressionCondition {
        val expression = map["expression"]
        return when(expression) {
            is Boolean -> ExpressionCondition(expression)
            else -> ExpressionCondition(expression.toString())
        }
    }
}

class ExpressionPreconditionAdapter {
    @FromJson
    fun fromJson(map: Map<String, @JvmSuppressWildcards Any>): ExpressionPrecondition {
        val context = map["context"]
        val expression = (context as Map<String, Any>)["expression"]
        return when(expression) {
            is Boolean -> ExpressionPrecondition(expression.toString())
            else -> ExpressionPrecondition(expression.toString())
        }
    }
}

class StageGraphAdapter {
    @ToJson
    fun toJson(stageGraph: StageGraph): List<OrcaStage> {
        return stageGraph.stages.map {
            val stageRequirements = stageGraph.stageRequirements[it.refId].orEmpty().map{ it }
            OrcaStage(it.attrs, StageExecution(it.refId, stageRequirements, it.inject))
        }
    }

    @FromJson
    fun fromJson(orcaStages: List<OrcaStage>): StageGraph {
        var stages: List<PipelineStage> = emptyList()
        var stageRequirements: Map<String, List<String>> = mapOf()
        orcaStages.map {
            val refId = it.execution.refId
            stages += PipelineStage(refId, it.stage, it.execution.inject)
            if (it.execution.requisiteStageRefIds.isNotEmpty()) {
                stageRequirements += (refId to it.execution.requisiteStageRefIds.map { it })
            }
        }
        return StageGraph(stages, stageRequirements)
    }
}

class ScoreThresholdsAdapter {
    @ToJson
    fun toJson(scoreThresholds: ScoreThresholds): Map<String, String> {
        return mapOf(
                "marginal" to scoreThresholds.marginal.toString(),
                "pass" to scoreThresholds.pass.toString()
        )
    }
}

val jsonNumberAdapter = object : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type !== Any::class.java) return null

        val delegate = moshi.nextAdapter<Any>(this, Any::class.java, annotations)
        return object : JsonAdapter<Any>() {
            override fun fromJson(reader: JsonReader): Any? {
                return if (reader.peek() !== JsonReader.Token.NUMBER) {
                    delegate.fromJson(reader)
                } else {
                    val s = reader.nextString()
                    try {
                        Integer.parseInt(s)
                    } catch (e: NumberFormatException) {
                        s.toFloat()
                    }
                }
            }

            override fun toJson(writer: JsonWriter, value: Any?) {
                delegate.toJson(writer, value)
            }
        }
    }
}

class JsonAdapterFactory {
    fun jsonAdapterBuilder(builder: Moshi.Builder): Moshi.Builder {
        builder
                .add(StageGraphAdapter())
                .add(OrcaStageAdapter())
                .add(PipelineAdapter())
                .add(ScoreThresholdsAdapter())
                .add(ExpressionConditionAdapter())
                .add(ExpressionPreconditionAdapter())
                .add(PipelineConfigAdapter())
                .add(VariableAdapter())
                .add(jsonNumberAdapter)
                .add(PolymorphicJsonAdapterFactory.of(Trigger::class.java, "type")
                        .withSubtype(JenkinsTrigger::class.java, "jenkins")
                        .withSubtype(GitTrigger::class.java, "git")
                        .withSubtype(PubSubTrigger::class.java, "pubsub")
                )
                .add(PolymorphicJsonAdapterFactory.of(Condition::class.java, "type")
                        .withSubtype(ExpressionCondition::class.java, "expression")
                )
                .add(PolymorphicJsonAdapterFactory.of(Precondition::class.java, "type")
                        .withSubtype(ExpressionPrecondition::class.java, "expression")
                )
                .add(PolymorphicJsonAdapterFactory.of(Notification::class.java, "type")
                        .withSubtype(EmailNotification::class.java, "email")
                )
                .add(PolymorphicJsonAdapterFactory.of(Cluster::class.java, "cloudProvider")
                        .withSubtype(CloudFoundryCluster::class.java, "cloudfoundry")
                )
                .add(PolymorphicJsonAdapterFactory.of(Artifact::class.java, "type")
                        .withSubtype(TriggerArtifact::class.java, "trigger")
                        .withSubtype(ReferencedArtifact::class.java, "artifact")
                )
                .add(PolymorphicJsonAdapterFactory.of(Manifest::class.java, "type")
                        .withSubtype(DirectManifest::class.java, "direct")
                        .withSubtype(ArtifactManifest::class.java, "artifact")
                )
                .add(PolymorphicJsonAdapterFactory.of(Stage::class.java, "type")
                        .withSubtype(DestroyServerGroupStage::class.java, "destroyServerGroup")
                        .withSubtype(DeployServiceStage::class.java, "deployService")
                        .withSubtype(DestroyServiceStage::class.java, "destroyService")
                        .withSubtype(WaitStage::class.java, "wait")
                        .withSubtype(ManualJudgmentStage::class.java, "manualJudgment")
                        .withSubtype(WebhookStage::class.java, "webhook")
                        .withSubtype(CanaryStage::class.java, "kayentaCanary")
                        .withSubtype(DeployStage::class.java, "deploy")
                        .withSubtype(CheckPreconditionsStage::class.java, "checkPreconditions")
                        .withSubtype(JenkinsStage::class.java, "jenkins")
                )
                .add(PolymorphicJsonAdapterFactory.of(VariableType::class.java, "type")
                        .withSubtype(IntegerType::class.java, "int")
                        .withSubtype(StringType::class.java, "string")
                        .withSubtype(FloatType::class.java, "float")
                        .withSubtype(BooleanType::class.java, "boolean")
                        .withSubtype(ListType::class.java, "list")
                        .withSubtype(ObjectType::class.java, "object")
                )
                .add(PolymorphicJsonAdapterFactory.of(Inject::class.java, "type")
                        .withSubtype(Inject.Before::class.java, "before")
                        .withSubtype(Inject.After::class.java, "after")
                        .withSubtype(Inject.First::class.java, "first")
                        .withSubtype(Inject.Last::class.java, "last")
                )
                .add(KotlinJsonAdapterFactory())
        return builder
    }

    fun pipelineAdapter(): JsonAdapter<Pipeline> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(Pipeline::class.java)
    }

    fun pipelineTemplateAdapter(): JsonAdapter<PipelineTemplate> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(PipelineTemplate::class.java)
    }

    fun pipelineConfigAdapter(): JsonAdapter<PipelineConfig> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(PipelineConfig::class.java)
    }

    fun stageGraphAdapter(): JsonAdapter<StageGraph> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(StageGraph::class.java)
    }

    fun stageAdapter(): JsonAdapter<Stage> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(Stage::class.java)
    }

    fun stageExecutionAdapter(): JsonAdapter<StageExecution> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(StageExecution::class.java)
    }

    fun injectAdapter(): JsonAdapter<Inject> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(Inject::class.java)
    }
}

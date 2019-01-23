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
import io.pivotal.kanal.model.*
import java.lang.reflect.Type

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
        val stageAdapter = JsonAdapterFactory().createAdapter<Stage>()
        val executionDetailsAdapter = JsonAdapterFactory().createAdapter<StageExecution>()
        writer.beginObject()
        val token = writer.beginFlatten()
        executionDetailsAdapter.toJson(writer, value.execution)
        stageAdapter.toJson(writer, value.stage)
        writer.endFlatten(token)
        writer.endObject()
    }

    @FromJson
    fun fromJson(map: Map<String, @JvmSuppressWildcards Any>): OrcaStage {
        val stageAdapter = JsonAdapterFactory().createAdapter<Stage>()
        val executionDetailsAdapter = JsonAdapterFactory().createAdapter<StageExecution>()
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


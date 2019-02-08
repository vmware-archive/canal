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
import kotlin.reflect.full.memberProperties

class PipelineTemplateInstanceAdapter {

    val pipelineConfigurationAdapter by lazy {
        JsonAdapterFactory().createAdapter<PipelineConfiguration>()
    }
    val pipelineAdapter by lazy {
        JsonAdapterFactory().createAdapter<Pipeline>()
    }

    @ToJson
    fun toJson(writer: JsonWriter, pipelineTemplateInstance: PipelineTemplateInstance) {
        writer.beginObject()
        val token = writer.beginFlatten()
        pipelineConfigurationAdapter.toJson(writer, pipelineTemplateInstance.config)
        pipelineAdapter.toJson(writer, pipelineTemplateInstance.pipeline)
        writer.endFlatten(token)
        writer.endObject()
    }

    @FromJson
    fun fromJson(pipelineTemplateInstance: Map<String, @JvmSuppressWildcards Any>): PipelineTemplateInstance {
        return PipelineTemplateInstance(
                pipelineConfigurationAdapter.fromJsonValue(pipelineTemplateInstance)!!,
                pipelineAdapter.fromJsonValue(pipelineTemplateInstance)!!
        )
    }

}

class StageGraphAdapter {
    val stageAdapter by lazy {
        JsonAdapterFactory().createAdapter<StageConfig>()
    }
    val executionDetailsAdapter by lazy {
        JsonAdapterFactory().createAdapter<StageExecution>()
    }
    val commonStageAttributesAdapter by lazy {
        JsonAdapterFactory().createAdapter<BaseStage>()
    }

    @ToJson
    fun toJson(writer: JsonWriter, stageGraph: StageGraph) {
        writer.beginArray()
        stageGraph.stages.forEach {
            val stageRequirements = stageGraph.stageRequirements[it.refId].orEmpty().map{ it }
            val execution = StageExecution(it.refId, stageRequirements, it.inject)
            writer.beginObject()
            val token = writer.beginFlatten()
            executionDetailsAdapter.toJson(writer, execution)
            stageAdapter.toJson(writer, it.stageConfig)
            if (it.common != null) {
                commonStageAttributesAdapter.toJson(writer, it.common)
            }
            writer.endFlatten(token)
            writer.endObject()
        }
        writer.endArray()
    }

    @FromJson
    fun fromJson(stageMaps: List<Map<String, @JvmSuppressWildcards Any>>): StageGraph {
        var stages: List<PipelineStage> = emptyList()
        var stageRequirements: Map<String, List<String>> = mapOf()
        stageMaps.map {
            val stage = stageAdapter.fromJsonValue(it)!!
            val execution = executionDetailsAdapter.fromJsonValue(it)!!
            val common = commonStageAttributesAdapter.fromJsonValue(it)
            val refId = execution.refId!!
            stages += PipelineStage(refId, stage, common, execution.inject)
            if (execution.requisiteStageRefIds.isNotEmpty()) {
                stageRequirements += (refId to execution.requisiteStageRefIds.map { it })
            }
        }
        return StageGraph(stages, stageRequirements)
    }
}

class CloudSpecificToJsonAdapter {
    val cloudProviderAdapter by lazy {
        JsonAdapterFactory().createAdapter<CloudProvider>()
    }
    val stageAdapter by lazy {
        JsonAdapterFactory().createNonCloudSpecificAdapter<StageConfig>()
    }
    val providerPropertyName = "provider"

    @ToJson
    fun toJson(stageConfig: StageConfig): Map<String, Any?> {
        val stageClass = stageConfig.javaClass.kotlin
        val properties = stageClass.memberProperties
        val propertiesMap = properties.map { it.name to it.get(stageConfig) }.toMap()
        return when (stageConfig) {
            is CloudSpecific -> {
                val provider = propertiesMap.get(providerPropertyName) as CloudProvider
                val providerProperties = provider.javaClass.kotlin.memberProperties
                val providerPropertiesMap = providerProperties.map { it.name to it.get(provider) }.toMap()
                propertiesMap.minus(providerPropertyName) + providerPropertiesMap
            }
            else -> propertiesMap
        }
    }

    @FromJson
    fun fromJson(stageJson: Map<String, @JvmSuppressWildcards Any>): StageConfig {
        val stageMap = try {
            val cloudProvider = cloudProviderAdapter.fromJsonValue(stageJson)!!
            val cloudProviderPropertyNames = cloudProvider.javaClass.kotlin.memberProperties.map { it.name }
            val cloudProviderMap = stageJson.filter { cloudProviderPropertyNames.contains(it.key) }
            stageJson + (providerPropertyName to cloudProviderMap)
        } catch (e: JsonDataException) {
            stageJson
        }
        val stage = stageAdapter.fromJsonValue(stageMap)!!
        return stage
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


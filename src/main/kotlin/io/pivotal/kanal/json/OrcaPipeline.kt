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

data class OrcaPipeline (
        val description: String,
        val parameterConfig: List<Parameter>,
        val notifications: List<Notification>,
        val triggers: List<Trigger>,
        val stages: List<OrcaStage>,
        val appConfig: Map<String, Any> = mapOf(),
        val expectedArtifacts: List<Any> = emptyList(),
        val keepWaitingPipelines: Boolean = false,
        val lastModifiedBy: String = "anonymous",
        val limitConcurrent: Boolean = false
)

data class OrcaStage(
        val stage: Stage,
        val execution: StageExecution
)

data class StageExecution(
        val refId: String,
        val requisiteStageRefIds: List<String>
)

class PipelineAdapter {
    @ToJson
    fun toJson(pipeline: Pipeline): OrcaPipeline {
        val stages = StageGraphAdapter().toJson(pipeline.stageGraph)
        return OrcaPipeline(
                pipeline.description,
                pipeline.parameters,
                pipeline.notifications,
                pipeline.triggers,
                stages
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
                stageGraph
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
            val stageRequirements = stageGraph.stageRequirements[it.refId].orEmpty().map{ it.toString() }
            OrcaStage(it.attrs, StageExecution(it.refId.toString(), stageRequirements))
        }
    }

    @FromJson
    fun fromJson(orcaStages: List<OrcaStage>): StageGraph {
        var stages: List<PipelineStage> = emptyList()
        var stageRequirements: Map<Int, List<Int>> = mapOf()
        orcaStages.map {
            val refId = it.execution.refId.toInt()
            stages += PipelineStage(refId, it.stage)
            if (it.execution.requisiteStageRefIds.isNotEmpty()) {
                stageRequirements += (refId to it.execution.requisiteStageRefIds.map { it.toInt() })
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

class JsonAdapterFactory {
    fun jsonAdapterBuilder(builder: Moshi.Builder): Moshi.Builder {
        builder
                .add(StageGraphAdapter())
                .add(OrcaStageAdapter())
                .add(PipelineAdapter())
                .add(ScoreThresholdsAdapter())
                .add(ExpressionConditionAdapter())
                .add(ExpressionPreconditionAdapter())
                .add(PolymorphicJsonAdapterFactory.of(Trigger::class.java, "type")
                        .withSubtype(JenkinsTrigger::class.java, "jenkins")
                        .withSubtype(GitTrigger::class.java, "git")
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
                .add(PolymorphicJsonAdapterFactory.of(Artifact::class.java, "type")
                        .withSubtype(ReferencedArtifact::class.java, "artifact")
                )
                .add(PolymorphicJsonAdapterFactory.of(Manifest::class.java, "type")
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
                )
                .add(KotlinJsonAdapterFactory())
        return builder
    }

    fun pipelineAdapter(): JsonAdapter<Pipeline> {
        return jsonAdapterBuilder(Moshi.Builder()).build().adapter(Pipeline::class.java)
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
}

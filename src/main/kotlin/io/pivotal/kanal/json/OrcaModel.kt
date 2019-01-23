package io.pivotal.kanal.json

import io.pivotal.kanal.model.*


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

package io.pivotal.kanal.model

data class PipelineExecution(
        val trigger: Map<String, Any> = emptyMap(),
        val templateVariables: Map<String, Any> = emptyMap()
)
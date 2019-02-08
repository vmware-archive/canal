package io.pivotal.canal.model

class Pipelines(private val pipelineBuildersForApp: Map<String, List<Pipeline>> = mapOf()) {

    fun withPipelinesForApp(application: String, vararg pipelineBuilders: Pipeline): Pipelines {
        return withPipelinesForApp(application, pipelineBuilders.asList())
    }

    fun withPipelinesForApp(application: String, pipelineBuilders: List<Pipeline>): Pipelines {
        val existingPipelinesForApp = pipelineBuildersForApp.get(application).orEmpty()
        return Pipelines(pipelineBuildersForApp + (application to existingPipelinesForApp + pipelineBuilders))
    }



}
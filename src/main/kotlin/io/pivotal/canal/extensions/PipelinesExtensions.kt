package io.pivotal.canal.extensions

import io.pivotal.canal.builders.PipelineBuilder
import io.pivotal.canal.model.Pipeline
import io.pivotal.canal.model.Pipelines


fun pipelines(appendApps: AppPipelineAppender.() -> Unit): Pipelines {
    val appender = AppPipelineAppender(Pipelines())
    appender.appendApps()
    return appender.pipelines
}

class AppPipelineAppender(var pipelines: Pipelines) {

    fun app(name: String, appendPipelines: PipelineAppender.() -> Unit) {
        val appender = PipelineAppender()
        appender.appendPipelines()
        val newPipelines = appender.pipelines
        pipelines = pipelines.withPipelinesForApp(name, newPipelines)
    }

}

class PipelineAppender(var pipelines: List<Pipeline> = emptyList()) {

    fun pipeline(name: String, withinPipeline: PipelineBuilder.() -> Unit) {
        val pb = PipelineBuilder(Pipeline(name))
        pb.withinPipeline()
        pipelines += pb.pipeline
    }

}

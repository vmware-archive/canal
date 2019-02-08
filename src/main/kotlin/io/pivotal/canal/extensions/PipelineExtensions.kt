package io.pivotal.canal.extensions

import io.pivotal.canal.builders.PipelineBuilder
import io.pivotal.canal.model.Pipeline

fun pipeline(withinPipeline: PipelineBuilder.() -> Unit): Pipeline {
    val pw = PipelineBuilder(Pipeline())
    pw.withinPipeline()
    return pw.pipeline
}

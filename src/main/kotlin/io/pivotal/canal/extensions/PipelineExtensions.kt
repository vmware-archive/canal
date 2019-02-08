package io.pivotal.canal.extensions

import io.pivotal.canal.builders.PipelineBuilder
import io.pivotal.canal.model.Pipeline

fun Pipeline.with(withinPipeline: PipelineBuilder.() -> Unit): Pipeline {
    val pw = PipelineBuilder(this)
    pw.withinPipeline()
    return pw.pipeline
}

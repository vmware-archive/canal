package io.pivotal.kanal.extensions

import io.pivotal.kanal.builders.PipelineBuilder
import io.pivotal.kanal.model.Pipeline

fun Pipeline.with(withinPipeline: PipelineBuilder.() -> Unit): Pipeline {
    val pw = PipelineBuilder(this)
    pw.withinPipeline()
    return pw.pipeline
}

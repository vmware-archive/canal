package io.pivotal.kanal.evaluation

import com.netflix.spinnaker.orca.config.UserConfiguredUrlRestrictions
import com.netflix.spinnaker.orca.pipeline.expressions.ExpressionEvaluationSummary
import com.netflix.spinnaker.orca.pipeline.expressions.PipelineExpressionEvaluator
import com.netflix.spinnaker.orca.pipeline.util.ContextFunctionConfiguration
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.pivotal.kanal.json.JsonAdapterFactory
import io.pivotal.kanal.model.Pipeline
import io.pivotal.kanal.model.PipelineTemplate

class ExpressionEvaluator() {

    val expressionEvaluator = PipelineExpressionEvaluator(
            ContextFunctionConfiguration(UserConfiguredUrlRestrictions.Builder().build()))

    fun evaluate(pipeline: Pipeline, pipelineExecution: PipelineExecution): Pipeline {
        return evaluateWithAdapter(pipeline, JsonAdapterFactory().pipelineAdapter(), pipelineExecution)
    }

    fun evaluate(template: PipelineTemplate, templateContext: TemplateContext): PipelineTemplate {
        return evaluateWithAdapter(template, JsonAdapterFactory().pipelineTemplateAdapter(), templateContext)
    }

    private fun <T>evaluateWithAdapter(m: T, adapter: JsonAdapter<T>, context: Any): T {
        val json = adapter.toJson(m)
        val moshi = Moshi.Builder().build()
        val mapAdapter = moshi.adapter(Map::class.java)
        val pipelineMap = mapAdapter.fromJson(json)
        val summary = ExpressionEvaluationSummary()
        val evaluatedPipelineMap =  expressionEvaluator.evaluate(
                pipelineMap?.toMutableMap() as MutableMap<String, Any>?,
                context,
                summary,
                true
        )
        if (summary.expressionResult.isNotEmpty()) {
            throw IllegalExpressionException(summary)
        }
        val e = adapter.fromJsonValue(evaluatedPipelineMap)!!
        return e;
    }

}

data class PipelineExecution(
        val trigger: Map<String, Any>
)

data class TemplateContext(
        val templateVariables: Map<String, Any>
)

class IllegalExpressionException(val summary: ExpressionEvaluationSummary) : Exception("Failed to evaluate expressions!")

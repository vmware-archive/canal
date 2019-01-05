package io.pivotal.kanal.evaluation

import com.netflix.spinnaker.orca.config.UserConfiguredUrlRestrictions
import com.netflix.spinnaker.orca.pipeline.expressions.ExpressionEvaluationSummary
import com.netflix.spinnaker.orca.pipeline.expressions.PipelineExpressionEvaluator
import com.netflix.spinnaker.orca.pipeline.util.ContextFunctionConfiguration
import com.squareup.moshi.Moshi
import io.pivotal.kanal.json.JsonAdapterFactory
import io.pivotal.kanal.model.Pipeline

class ExpressionEvaluator(val pipelineExecution: PipelineExecution) {

    val expressionEvaluator = PipelineExpressionEvaluator(
            ContextFunctionConfiguration(UserConfiguredUrlRestrictions.Builder().build()))

    fun evaluate(expression: String): String {
        val expressions = mapOf(
                "expression" to expression
        )
        val summary = ExpressionEvaluationSummary()
        val result = expressionEvaluator.evaluate(
                expressions,
                pipelineExecution,
                summary,
                true
        )
        if (summary.expressionResult.isNotEmpty()) {
            throw IllegalExpressionException(summary)
        }
        return result["expression"].toString()
    }

    fun evaluate(pipeline: Pipeline): Pipeline {
        val pipelineAdapter = JsonAdapterFactory().pipelineAdapter()
        val pipelineJson = pipelineAdapter.toJson(pipeline)
        val moshi = Moshi.Builder().build()
        val mapAdapter = moshi.adapter(Map::class.java)
        val pipelineMap = mapAdapter.fromJson(pipelineJson)
        val summary = ExpressionEvaluationSummary()
        val evaluatedPipelineMap = expressionEvaluator.evaluate(
                pipelineMap?.toMutableMap() as MutableMap<String, Any>?,
                pipelineExecution,
                summary,
                true
        )
        return pipelineAdapter.fromJsonValue(evaluatedPipelineMap)!!
    }

}

data class PipelineExecution(
        val trigger: Map<String, Any>
)

class IllegalExpressionException(val summary: ExpressionEvaluationSummary) : Exception("Failed to evaluate expressions!")

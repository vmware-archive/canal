package io.pivotal.kanal.model

import com.squareup.moshi.Json
import java.math.BigDecimal

data class PipelineConfig(
        val application: String,
        val name: String,
        val template: TemplateSource,
        val pipeline: Pipeline = Pipeline(),
        val variables: Map<String, Any> = emptyMap(),
        val inherit: List<String> = emptyList(),
        val schema: String = "v2"
)

data class TemplateSource(
        val source: String
)

data class PipelineTemplate(
        val id: String,
        val metadata: Metadata,
        val variables: List<Variable>,
        val pipeline: Pipeline,
        val protect: Boolean = false,
        val schema: String = "v2"
)

data class Variable(
    val name: String,
    val description: String,
    val typeAttrs: VariableType<Any> = ObjectType(null),
    val example: String? = null,
    val nullable: Boolean = false,
    val merge: Boolean = false,
    val remove: Boolean = false
)

interface VariableType<out T> {
    val type: String
    val defaultValue: T?
}

data class IntegerType(override val defaultValue: Int?) : VariableType<Int> {
    override val type = "int"
}

data class StringType(override val defaultValue: String?) : VariableType<String> {
    override val type = "string"
}

data class FloatType(override val defaultValue: Float?) : VariableType<Float> {
    override val type = "float"
}

data class BooleanType(override val defaultValue: Boolean?) : VariableType<Boolean> {
    override val type = "boolean"
}

data class ListType(override val defaultValue: List<Any>?) : VariableType<List<Any>> {
    override val type = "list"
}

data class ObjectType(override val defaultValue: Any?) : VariableType<Any> {
    override val type = "object"
}

data class Metadata(
        val name: String,
        val description: String,
        val owner: String,
        val scopes: List<Scope> = listOf(Scope.GLOBAL)
)

enum class Scope() {
    @Json(name = "global") GLOBAL
}

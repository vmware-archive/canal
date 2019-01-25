package io.pivotal.kanal.model

import com.squareup.moshi.Json

data class PipelineTemplateInstance(
        val config: PipelineConfiguration,
        val pipeline: Pipeline = Pipeline()
)

data class PipelineConfiguration(
        val application: String,
        val name: String,
        val template: TemplateSource,
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
        val variables: List<Variable<Any>>,
        val pipeline: Pipeline,
        val protect: Boolean = false,
        val schema: String = "v2"
)

interface Variable<out T> {
    val name: String
    val description: String
    val type: String
    val defaultValue: T?
    val example: String?
    val nullable: Boolean
    val merge: Boolean
    val remove: Boolean
}


data class IntegerVariable(override val name: String,
                           override val description: String,
                           override val defaultValue: Int? = null,
                           override val example: String? = null,
                           override val nullable: Boolean = false,
                           override val merge: Boolean = false,
                           override val remove: Boolean = false) : Variable<Int> {
    override val type = "int"
}

data class StringVariable(override val name: String,
                           override val description: String,
                           override val defaultValue: String? = null,
                           override val example: String? = null,
                           override val nullable: Boolean = false,
                           override val merge: Boolean = false,
                           override val remove: Boolean = false) : Variable<String> {
    override val type = "string"
}



data class FloatVariable(override val name: String,
                          override val description: String,
                          override val defaultValue: Float? = null,
                          override val example: String? = null,
                          override val nullable: Boolean = false,
                          override val merge: Boolean = false,
                          override val remove: Boolean = false) : Variable<Float> {
    override val type = "float"
}

data class BooleanVariable(override val name: String,
                          override val description: String,
                          override val defaultValue: Boolean? = null,
                          override val example: String? = null,
                          override val nullable: Boolean = false,
                          override val merge: Boolean = false,
                          override val remove: Boolean = false) : Variable<Boolean> {
    override val type = "boolean"
}

data class ListVariable(override val name: String,
                          override val description: String,
                          override val defaultValue: List<Any>? = null,
                          override val example: String? = null,
                          override val nullable: Boolean = false,
                          override val merge: Boolean = false,
                          override val remove: Boolean = false) : Variable<List<Any>> {
    override val type = "list"
}

data class ObjectVariable(override val name: String,
                          override val description: String,
                          override val defaultValue: Any? = null,
                          override val example: String? = null,
                          override val nullable: Boolean = false,
                          override val merge: Boolean = false,
                          override val remove: Boolean = false) : Variable<Any> {
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

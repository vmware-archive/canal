package io.pivotal.kanal.model

import com.squareup.moshi.Json

data class PipelineTemplate(
        val id: String,
        val metadata: Metadata,
        val variables: List<Variable<Any>>,
        val pipeline: Pipeline,
        val protect: Boolean = false,
        val schema: String = "v2"
)

interface Variable<out T> {
    val type: String
    val name: String
    val description: String
    val defaultValue : T?
}

data class IntegerType(override val name: String,
                       override val description: String,
                       override val defaultValue: Int?) : Variable<Int> {
    override val type = "int"
}

data class StringType(override val name: String,
                      override val description: String,
                      override val defaultValue: String?) : Variable<String> {
    override val type = "string"
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
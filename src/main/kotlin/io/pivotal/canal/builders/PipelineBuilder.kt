package io.pivotal.canal.builders

import io.pivotal.canal.model.*

open class PipelineBuilder @JvmOverloads constructor(var pipeline: Pipeline = Pipeline()) {
    var description: String = ""
        set(value) {
            this.pipeline = this.pipeline.copy(description = value)
        }

    var limitConcurrent: Boolean = false
        set(value) {
            this.pipeline = this.pipeline.copy(limitConcurrent = value)
        }

    var keepWaitingPipelines: Boolean = false
        set(value) {
            this.pipeline = this.pipeline.copy(keepWaitingPipelines = value)
        }

    var stages: StageGraph = StageGraph()
        set(value) {
            this.pipeline = this.pipeline.copy(stages = value)
        }

    var parameters: List<Parameter> = emptyList()
        set(value) {
            this.pipeline = this.pipeline.copy(parameters = value)
        }

    fun parameters(vararg parameters: Parameter) {
        this.parameters = parameters.toList()
    }

    var notifications: List<Notification> = emptyList()
        set(value) {
            this.pipeline = this.pipeline.copy(notifications = value)
        }

    fun notifications(vararg notifications: Notification) {
        this.notifications = notifications.toList()
    }

    var triggers: List<Trigger> = emptyList()
        set(value) {
            this.pipeline = this.pipeline.copy(triggers = value)
        }

    fun triggers(vararg triggers: Trigger) {
        this.triggers = triggers.toList()
    }

    var expectedArtifacts: List<ExpectedArtifact> = emptyList()
        set(value) {
            this.pipeline = this.pipeline.copy(expectedArtifacts = value)
        }

    fun expectedArtifacts(vararg expectedArtifacts: ExpectedArtifact) {
        this.expectedArtifacts = expectedArtifacts.toList()
    }

}

/*
 * Copyright 2019 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.canal.builders

import io.pivotal.canal.model.*

open class PipelineBuilder constructor(var pipeline: PipelineModel) {
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
            this.pipeline = this.pipeline.copy(stageGraph = value)
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

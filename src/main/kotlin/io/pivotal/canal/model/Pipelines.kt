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

package io.pivotal.canal.model

import io.pivotal.canal.json.JsonAdapterFactory

class Pipelines(val pipelinesForApp: Map<String, List<Pipeline>> = mapOf()) {

    fun withPipelinesForApp(application: String, vararg pipelineBuilders: Pipeline): Pipelines {
        return withPipelinesForApp(application, pipelineBuilders.asList())
    }

    fun withPipelinesForApp(application: String, pipelineBuilders: List<Pipeline>): Pipelines {
        val existingPipelinesForApp = pipelinesForApp.get(application).orEmpty()
        return Pipelines(pipelinesForApp + (application to existingPipelinesForApp + pipelineBuilders))
    }

    fun toJson(): String {
        return JsonAdapterFactory().createAdapter<Map<String, List<Pipeline>>>().toJson(pipelinesForApp)
    }

}

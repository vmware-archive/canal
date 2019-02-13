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

import com.squareup.moshi.Json

interface Trigger : Typed {
    val enabled: Boolean
}

data class JenkinsTrigger(
        val job: String,
        val master: String,
        val propertyFile: String? = null,
        override val enabled: Boolean = true
) : Trigger {
    override val type = "jenkins"
}

interface GitTrigger : Trigger {
    val source: String
}

data class GitHubTrigger(
        @Json(name = "project") val org: String,
        @Json(name = "slug") val repo: String,
        val branch: String? = null,
        val secret: String? = null,
        override val enabled: Boolean = true
) : GitTrigger {
    override val type = "git"
    override var source = "github"
}

data class PubSubTrigger(
        val pubsubSystem: String,
        val subscription: String,
        val source: String,
        val attributeConstraints: Map<String, Any> = emptyMap(),
        val payloadConstraints: Map<String, Any> = emptyMap(),
        override val enabled: Boolean = true
) : Trigger {
    override val type = "pubsub"
    var subscriptionName = subscription
}

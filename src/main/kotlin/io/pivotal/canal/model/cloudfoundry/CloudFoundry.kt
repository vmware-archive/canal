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

package io.pivotal.canal.model.cloudfoundry

import io.pivotal.canal.model.*

data class CloudFoundryCloudProvider @JvmOverloads constructor(
        override val credentials: String,
        val manifest: ManifestSource? = null
) : CloudProvider {
    override var cloudProvider = "cloudfoundry"
    override var cloudProviderType = cloudProvider
}

data class CloudFoundryCluster @JvmOverloads constructor(
        val application: String,
        val account: String,
        val region: String,
        val strategy: DeploymentStrategy,
        val artifact: Artifact,
        override val capacity: Capacity,
        val manifest: Manifest,
        val stack: String = "",
        val detail: String = "",
        val startApplication: Boolean? = null
) : Cluster {
    override var cloudProvider = "cloudfoundry"
    var provider = cloudProvider
}

interface Manifest : Typed

data class ArtifactManifest(
        val account: String,
        val reference: String
) : Manifest {
    override var type = "artifact"
}

data class DirectManifest @JvmOverloads constructor(
        val services: List<String>,
        val routes: List<String> = emptyList(),
        val diskQuota: String = "1024M",
        val memory: String = "1024M",
        @Transient val instanceCount: Int = 1,
        val env: List<String> = emptyList()
) : Manifest {
    override var type: String = "direct"
    var instances = instanceCount
}

interface ManifestSource : Typed

data class ManifestSourceArtifact @JvmOverloads constructor(
        val account: String,
        val reference: String,
        val timeout: String? = null
) : ManifestSource {
    override var type: String = "artifact"
}

data class ManifestSourceUserProvided @JvmOverloads constructor(
        val credentials: String,
        val routeServiceUrl: String,
        val serviceName: String,
        val syslogDrainUrl: String,
        val tags: List<String>  = emptyList()
) : ManifestSource {
    override var type: String = "userProvided"
}

data class ManifestSourceDirect @JvmOverloads constructor(
        val service: String,
        val serviceName: String,
        val servicePlan: String,
        val tags: List<String>  = emptyList(),
        val parameters: String? = null,
        val timeout: String? = null
) : ManifestSource {
    override var type: String = "direct"
}

interface Artifact : Typed

data class TriggerArtifact(
        val account: String,
        val pattern: String
) : Artifact {
    override var type = "trigger"
}

data class ReferencedArtifact(
        val account: String,
        val reference: String
) : Artifact {
    override var type = "artifact"
}

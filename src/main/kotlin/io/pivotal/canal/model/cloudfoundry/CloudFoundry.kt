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

import io.pivotal.canal.extensions.builder.*
import io.pivotal.canal.model.*

class CloudFoundryStageCatalog @JvmOverloads constructor(credentials: String) : CloudStageCatalog() {

    override var defaults: PipelineDefaults = PipelineDefaults()

    fun withDefaults(defaults: Defaults) : CloudFoundryStageCatalog {
        return this.apply { this.defaults = defaults.delegate }
    }

    override val cloudProvider = cloudFoundryCloudProvider(credentials)

    fun deploy(assign: (CloudFoundryDeployStageBuilder) -> CloudFoundryDeployStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(CloudFoundryDeployStageBuilder(defaults)).build())
    }
    fun deployService(assign: (DeployServiceStageBuilder) -> DeployServiceStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(DeployServiceStageBuilder(defaults, cloudProvider)).build())
    }
    @JvmOverloads fun destroyService(serviceName: String, assign: (DestroyServiceStageBuilder) -> DestroyServiceStageBuilder = { it }): StageGrapher {
        return StageGrapher(assign(DestroyServiceStageBuilder(defaults, cloudProvider, serviceName)).build())
    }
}

fun cloudFoundryCloudProvider(credentials: String) : CloudProvider {
    return CloudProvider(credentials, "cloudfoundry")
}

class DeployServiceStageBuilder(val defaults: PipelineDefaults,
                                val provider: CloudProvider,
                                var region: String? = null,
                                var manifest: ManifestSource? = null) : SpecificStageBuilder<DeployService, DeployServiceStageBuilder>() {
    override fun specificStageConfig() = DeployService(
            provider,
            region ?: defaults.region!!,
            manifest!!)

    fun manifest(manifest: ManifestSource) = apply { this.manifest = manifest }
    fun region(region: String) = apply { this.region = region }
}

data class DeployService(
        override val provider: CloudProvider,
        override val region: String,
        val manifest: ManifestSource
) : SpecificStageConfig, CloudSpecific, Region {
    override val type = "deployService"
    var action = type
}

class DestroyServiceStageBuilder(val defaults: PipelineDefaults,
                                 val provider: CloudProvider,
                                 val serviceName: String,
                                 var region: String? = null,
                                 var timeout: String? = null) : SpecificStageBuilder<DestroyService, DestroyServiceStageBuilder>() {
    override fun specificStageConfig() = DestroyService(
            provider,
            region ?: defaults.region!!,
            serviceName,
            timeout)

    fun region(region: String) = apply { this.region = region }
    fun timeout(timeout: String) = apply { this.timeout = timeout }
}

data class DestroyService (
        override val provider: CloudProvider,
        override val region: String,
        val serviceName: String,
        val timeout: String? = null
) : SpecificStageConfig, CloudSpecific, Region {
    override val type = "destroyService"
    var action = type
}

class CloudFoundryDeployStageBuilder(val defaults: PipelineDefaults,
                                     var artifact: Artifact? = null,
                                     var manifest: Manifest? = null) : DeployStageBuilder<CloudFoundryDeployStageBuilder>() {

    override fun specificStageConfig() = Deploy(listOf(CloudFoundryCluster(
            application ?: defaults.application!!,
            account ?: defaults.account!!,
            region ?: defaults.region!!,
            strategy,
            capacity,
            stack,
            detail,
            startApplication,
            artifact!!,
            manifest!!
    )))

    fun artifact(artifact: Artifact) = apply { this.artifact = artifact }
    fun manifest(manifest: Manifest) = apply { this.manifest = manifest }
}

data class CloudFoundryCluster @JvmOverloads constructor(
        override val application: String,
        override val account: String,
        override val region: String,
        override val strategy: DeploymentStrategy,
        override val capacity: Capacity,
        override val stack: String,
        override val detail: String,
        override val startApplication: Boolean?,
        val artifact: Artifact,
        val manifest: Manifest
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
        val services: List<String> = emptyList(),
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

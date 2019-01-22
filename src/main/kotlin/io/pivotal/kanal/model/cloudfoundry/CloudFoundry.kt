package io.pivotal.kanal.model.cloudfoundry

import io.pivotal.kanal.model.Capacity
import io.pivotal.kanal.model.Cluster
import io.pivotal.kanal.model.Typed


data class CloudFoundryCluster(
        val application: String,
        val account: String,
        val region: String,
        val strategy: String,
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
    override val type = "artifact"
}

data class DirectManifest(
        val services: List<String>,
        val routes: List<String> = emptyList(),
        val diskQuota: String = "1024M",
        val instances: Int = 1,
        val memory: String = "1024M",
        val env: List<String> = emptyList()
) : Manifest {
    override val type: String = "direct"
}

interface Artifact : Typed

data class TriggerArtifact(
        val account: String,
        val pattern: String
) : Artifact {
    override val type = "trigger"
}

data class ReferencedArtifact(
        val account: String,
        val reference: String
) : Artifact {
    override val type = "artifact"
}

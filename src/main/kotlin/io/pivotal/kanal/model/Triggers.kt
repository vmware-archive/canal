package io.pivotal.kanal.model

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

data class GitTrigger(
        val branch: String,
        val project: String,
        val secret: String,
        val slug: String,
        val source: String,
        override val enabled: Boolean = true
) : Trigger {
    override val type = "git"
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

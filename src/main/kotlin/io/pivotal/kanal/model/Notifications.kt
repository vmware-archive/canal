package io.pivotal.kanal.model

interface Notification : Typed

data class EmailNotification(
        val address: String,
        val level: String
) : Notification {
    override val type = "email"
}

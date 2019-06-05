import io.spring.gradle.bintray.SpringBintrayExtension
import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension
import nl.javadude.gradle.plugins.license.LicenseExtension
import java.util.*

group = "io.pivotal"
description = "Software-defined delivery model for generating Spinnaker pipelines"

plugins {
    maven
    id("nebula.kotlin") version "1.3.21"
    id("io.spring.release") version "0.20.1"
    kotlin("jvm") version "1.3.30"
}

apply(plugin = "io.spring.license")
apply(plugin = "io.spring.publishing")

tasks.test {
    useJUnitPlatform()
}

configure<LicenseExtension> {
    extra["year"] = Calendar.getInstance().get(Calendar.YEAR)
    skipExistingHeaders = true
}

configure<ContactsExtension> {
    val contact = Contact("cmccoy@pivotal.io")
    contact.moniker = "Clay McCoy"
    contact.github = "cmccoy"
    people["cmccoy@pivotal.io"] = contact
}

configure<SpringBintrayExtension> {
    labels = listOf("canal", "spinnaker", "pipelines")
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.squareup.moshi:moshi:latest.release")
    implementation("com.squareup.moshi:moshi-kotlin:latest.release")
    implementation("com.squareup.moshi:moshi-adapters:latest.release")
    implementation("net.javacrumbs.json-unit:json-unit-assertj:latest.release")
    implementation("com.netflix.spinnaker.orca:orca-core:latest.release")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.assertj:assertj-core:latest.release")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")
    implementation(kotlin("stdlib-jdk8"))
}

dependencyLocking {
    lockAllConfigurations()
}

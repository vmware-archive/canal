import java.util.Calendar
import nl.javadude.gradle.plugins.license.LicenseExtension
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "io.pivotal"

plugins {
    maven
    id("nebula.kotlin") version "1.3.21"
    id("io.spring.release") version "0.20.1"
}

apply(plugin = "io.spring.license")

tasks.test {
    useJUnitPlatform()
}

configure<LicenseExtension> {
    extra["year"] = Calendar.getInstance().get(Calendar.YEAR)
    skipExistingHeaders = true
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
}

dependencyLocking {
    lockAllConfigurations()
}

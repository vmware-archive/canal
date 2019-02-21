import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "io.pivotal"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
        mavenLocal()
    }
    dependencies {
        classpath("org.ow2.asm:asm:5.0.3")
        classpath("io.spring.gradle:spring-release-plugin:0.20.1")
        classpath("com.netflix.nebula:gradle-extra-configurations-plugin:3.2.0")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
        classpath("com.netflix.nebula:nebula-project-plugin:3.4.0")
    }
}

plugins {
    maven
    kotlin("jvm") version "1.2.51"
    id("io.spring.release") version "0.20.1"
//    id("io.spring.license")
}

//license {
//    ext.year = Calendar.getInstance().get(Calendar.YEAR)
//    skipExistingHeaders = true
//}

repositories {
    mavenCentral()
    maven(url = "https://dl.bintray.com/spinnaker/spinnaker/")
    mavenLocal()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("com.squareup.moshi:moshi:1.8.0")
    compile("com.squareup.moshi:moshi-kotlin:1.8.0")
    compile("com.squareup.moshi:moshi-adapters:1.8.0")
    compile("net.javacrumbs.json-unit:json-unit-assertj:2.2.0")
    compile("com.netflix.spinnaker.orca:orca-core:6.126.0")

    testCompile("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.2.0")
    testCompile("org.assertj:assertj-core:3.8.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

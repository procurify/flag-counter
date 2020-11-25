import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val arrowVersion = "0.11.0"
val jackSonVersion = "2.11.0"
val fuelVersion = "2.3.0"
val kotlinVersion = "1.4.20"

plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("kapt") version "1.4.20"
    `maven-publish`
    id("io.spring.dependency-management") version ("1.0.9.RELEASE")
    id("com.github.johnrengelman.shadow") version ("6.0.0")
}

group = "com.procurify"
version = "dev"

description = "Application for counting and posting updates about configuration flag counts"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar> {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))

    api("com.amazonaws:aws-lambda-java-core:1.2.1")
    api("com.amazonaws:aws-lambda-java-log4j2:1.2.0")
    api("com.fasterxml.jackson.core:jackson-core:$jackSonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jackSonVersion")
    api("com.fasterxml.jackson.core:jackson-annotations:$jackSonVersion")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jackSonVersion")

    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")

    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")

    kapt("io.arrow-kt:arrow-meta:$arrowVersion")

    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.10.0")
}

tasks.build {
    finalizedBy(getTasksByName("shadowJar", false))
}

task<Exec>("deploy") {
    dependsOn("shadowJar")
    commandLine("serverless", "deploy")
}

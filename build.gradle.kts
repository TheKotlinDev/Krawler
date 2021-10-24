import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.31"
}

group = "me.johnmone"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
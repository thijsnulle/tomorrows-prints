plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.sksamuel.scrimage:scrimage-core:4.1.0")
    implementation("com.aallam.openai:openai-client:3.4.0")
    runtimeOnly("io.ktor:ktor-client-java-jvm:2.3.4")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}
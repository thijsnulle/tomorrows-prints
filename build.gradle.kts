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
    implementation("com.google.cloud:google-cloud-storage:2.28.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.sksamuel.scrimage:scrimage-core:4.1.1")
    implementation("com.aallam.openai:openai-client:3.4.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.seleniumhq.selenium:selenium-java:4.13.0")
    implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")

    runtimeOnly("io.ktor:ktor-client-java-jvm:2.3.4")

    testImplementation(kotlin("test"))
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
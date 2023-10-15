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
    implementation("com.sksamuel.scrimage:scrimage-core:4.1.0")
    implementation("com.aallam.openai:openai-client:3.4.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
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
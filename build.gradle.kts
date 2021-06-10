import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
    kotlin("plugin.spring") version "1.5.10"
}

group = "no.nav.cv"
version = "0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven")
    maven(url = "https://github-package-registry-mirror.gc.nav.no/cached/maven-release" )
    maven(url = "https://jitpack.io" )
}

object Version {
    val brukernotifikasjoner = "1.2021.01.18-11.12-b9c8c40b98d1"
    val avro = "1.9.2"
    val confluent = "5.3.0"
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.h2database:h2")
    implementation("com.github.navikt:brukernotifikasjon-schemas:${Version.brukernotifikasjoner}")
    implementation("io.confluent:kafka-streams-avro-serde:${Version.confluent}")
    implementation("javax.annotation:javax.annotation-api")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.12.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.24.0")
    implementation("net.logstash.logback:logstash-logback-encoder:6.2")
    implementation("no.nav:vault-jdbc:1.3.7")
    implementation("org.apache.avro:avro:${Version.avro}")
    implementation("org.flywaydb:flyway-core")
    implementation("org.hibernate:hibernate-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.json:json:20190722")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("org.testcontainers:junit-jupiter:1.15.3")
    testImplementation("org.testcontainers:kafka:1.15.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.8"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    kotlin("plugin.spring") version "1.9.21"
}

ext {
    set("snakeyaml.version", "2.2") // Spring 3.1.8 uses vulnerable version 1.33. Can possibly be removed on spring bump
}

group = "no.nav.cv"
version = "0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven")
    maven(url = "https://github-package-registry-mirror.gc.nav.no/cached/maven-release" )
    maven(url = "https://jitpack.io" )
}

object Version {
    val brukernotifikasjoner = "2.5.2"
    val avro = "1.11.3"
    val confluent = "7.5.1"
    val token_support = "2.1.9"
}

dependencies {
//    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator") // Mildertidig kun fo å fikse migrering av properties ved Spring oppgradering
    compileOnly("jakarta.platform:jakarta.jakartaee-api:10.0.0")

    implementation("com.h2database:h2:2.1.214")
    implementation("com.github.navikt:brukernotifikasjon-schemas:${Version.brukernotifikasjoner}")
    implementation("io.confluent:kafka-streams-avro-serde:${Version.confluent}")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.46.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.46.0")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    implementation("org.apache.avro:avro:${Version.avro}")
    implementation("org.flywaydb:flyway-core")
    implementation("org.hibernate.orm:hibernate-core")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.json:json:20231013")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")

    implementation("no.nav.security:token-validation-spring:${Version.token_support}")
    implementation("no.nav.security:token-client-spring:${Version.token_support}")

    testImplementation("com.ninja-squad:springmockk:3.1.1")
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

    testImplementation("org.testcontainers:junit-jupiter:1.17.4")
    testImplementation("org.testcontainers:kafka:1.17.4")
    testImplementation("org.mock-server:mockserver-netty:5.15.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

}

configurations {
    all {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
}

ext {
    set("snakeyaml.version", "1.32") // Spring 2.7.4 uses vulnerable version 1.30. Can possibly be removed on spring bump
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
    val brukernotifikasjoner = "2.5.1"
    val avro = "1.11.1"
    val confluent = "5.3.0"
    val token_support = "2.1.6"
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.h2database:h2:1.4.200")
    implementation("com.github.navikt:brukernotifikasjon-schemas:${Version.brukernotifikasjoner}")
    implementation("io.confluent:kafka-streams-avro-serde:${Version.confluent}")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("javax.annotation:javax.annotation-api")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.42.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.42.0")
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")
    implementation("no.nav:vault-jdbc:1.3.10")
    implementation("org.apache.avro:avro:${Version.avro}")
    implementation("org.flywaydb:flyway-core")
    implementation("org.hibernate:hibernate-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("org.json:json:20220924")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("no.nav.security:token-validation-spring:${Version.token_support}")
    implementation("no.nav.security:token-client-spring:${Version.token_support}")
    implementation("javax.inject:javax.inject:1")

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
    testImplementation("org.mock-server:mockserver-netty:5.14.0")

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
    testLogging.showStandardStreams = true
}

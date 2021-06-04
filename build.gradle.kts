import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.spring") version "1.5.0"
    id("org.springframework.boot") version "2.4.1"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

version = "0.1"
group = "no.nav.cv"
java.sourceCompatibility = JavaVersion.VERSION_11

/*repositories {
    mavenCentral()
    maven { url "https://jcenter.bintray.com" }
    maven { url "https://packages.confluent.io/maven/" }
    maven { url "https://github-package-registry-mirror.gc.nav.no/cached/maven-release" }
    maven { url 'https://jitpack.io' }
}*/

repositories {
    mavenCentral()
    maven(url = "http://packages.confluent.io/maven")
    maven(url = "https://github-package-registry-mirror.gc.nav.no/cached/maven-release" )
    maven(url = "https://jitpack.io" )
}

object Version {
    val kotlin = "1.5.0"
    val brukernotifikasjoner = "1.2021.01.18-11.12-b9c8c40b98d1"
    val avro = "1.9.2"
    val confluent = "5.3.0"
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    // SNYK fixes
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("org.postgresql:postgresql:42.2.13")
    implementation("org.hibernate:hibernate-core:5.4.18.Final")

    //implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    //implementation("io.micronaut.sql:micronaut-hibernate-jpa")
    //implementation("io.micronaut.sql:micronaut-hibernate-jpa-spring")
    //implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    //implementation("io.micronaut.kafka:micronaut-kafka:2.0.0")
    //implementation("io.micronaut:micronaut-runtime")
    //implementation("io.micronaut:micronaut-http-server-netty")
    //implementation("io.micronaut:micronaut-http-client")
    //implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("net.javacrumbs.shedlock:shedlock-spring:4.24.0")

    implementation( "org.flywaydb:flyway-core")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.12.0")

    implementation("javax.annotation:javax.annotation-api")
    implementation("net.logstash.logback:logstash-logback-encoder:6.2")

    implementation("io.confluent:kafka-streams-avro-serde:${Version.confluent}")
    implementation("org.apache.avro:avro:${Version.avro}")
    implementation("org.json:json:20190722")
    implementation("com.github.navikt:brukernotifikasjon-schemas:${Version.brukernotifikasjoner}")
    implementation("no.nav:vault-jdbc:1.3.7")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.h2database:h2")
    // implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micrometer:micrometer-registry-prometheus:1.6.4")

    //testImplementation("io.micronaut.test:micronaut-test-kotlintest")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    //testImplementation("io.micronaut.test:micronaut-test-junit5:1.1.3")
    testImplementation("org.testcontainers:junit-jupiter:1.15.3")
    testImplementation("org.testcontainers:kafka:1.15.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }

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

import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.0"
    `maven-publish`
    signing
}

group = "com.bamboo.assur.partner-insurers"
version = "0.1.0"
description = "Infrastructure DDD partagée pour les microservices des assureurs partenaires dans le cadre du projet Bamboo Assur "

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY")}")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
            }
        }
    }
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter:4.0.0-RC1")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux:4.0.0-RC1")
    compileOnly("org.springframework.boot:spring-boot-starter-webmvc:4.0.0-RC1")
    compileOnly("org.springframework.boot:spring-boot-starter-data-r2dbc:4.0.0-RC1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.20")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.0-RC1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

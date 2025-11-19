plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("org.jetbrains.dokka") version "1.9.20"
    `maven-publish`
    signing
}

group = "com.bamboo.assur.partner-insurers"
version = project.property("version") as String
description = """
    Infrastructure DDD partagée pour les microservices des assureurs partenaires dans le cadre du projet Bamboo Assur
    """.trimIndent()

apply(from = "publish.gradle.kts")

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    // Generate a Javadoc JAR from Dokka output so that KDoc is visible in consuming IDEs
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
            // Ensure Dokka-generated Javadoc is attached as the javadoc artifact
            // Gradle creates a javadocJar task via withJavadocJar(); we repoint its contents to Dokka
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            val repoSlug = System.getenv("GITHUB_REPOSITORY")
                ?: "${project.property("repository.owner")}/${project.property("repository.name")}"
            url = uri("https://maven.pkg.github.com/$repoSlug")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String?
            }
        }
    }
}

dependencies {
    val springBootVersion = "4.0.0-RC2"

    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    compileOnly("org.springframework.boot:spring-boot-starter:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-data-r2dbc:$springBootVersion")
    // Jakarta Bean Validation API (for ConstraintViolationException, etc.)
    compileOnly("jakarta.validation:jakarta.validation-api")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.20")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.8.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    testImplementation("org.springframework.data:spring-data-commons")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("jakarta.validation:jakarta.validation-api")
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

// Configure the javadocJar task to package Dokka's Javadoc output
// This makes Kotlin KDoc appear as JavaDoc in consumer IDEs (e.g., IntelliJ, Eclipse)
val dokkaJavadoc by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
tasks.named<org.gradle.jvm.tasks.Jar>("javadocJar") {
    dependsOn(dokkaJavadoc)
    from(layout.buildDirectory.dir("dokka/javadoc"))
}

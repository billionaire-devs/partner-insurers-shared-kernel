tasks.register("publishSharedKernelLocal") {
    dependsOn("publishToMavenLocal")
    doLast {
        println("✓ Published shared-kernel to Maven Local")
    }
}

tasks.register("publishSharedKernelRemote") {
    dependsOn("publish")
    doLast {
        println("✓ Published shared-kernel to GitHub Packages")
    }
}

tasks.register("buildAndPublishSharedKernel") {
    dependsOn("clean", "build", "publishSharedKernelLocal")
    doLast {
        println("✓ Built and published shared-kernel library")
    }
}

tasks.register("updateVersionProperty") {
    val versionPropertyFile = project.layout.projectDirectory.file("gradle.properties").asFile
    inputs.file(versionPropertyFile)
    val releaseVersion = project.provider { project.version.toString() }
    outputs.file(versionPropertyFile)

    doLast {
        val properties = java.util.Properties().apply {
            versionPropertyFile.inputStream().use { inputStream ->
                load(inputStream)
            }
        }
        properties.setProperty("version", releaseVersion.get())
        versionPropertyFile.outputStream().use { outputStream ->
            properties.store(outputStream, "Updated by updateVersionProperty task")
        }
        println("✓ gradle.properties version updated to ${releaseVersion.get()}")
    }
}

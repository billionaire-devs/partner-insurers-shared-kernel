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

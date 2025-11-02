tasks.register("publishToMavenLocal") {
    dependsOn("publishToMavenLocal")
    doLast {
        println("✓ Published to Maven Local")
    }
}

tasks.register("publishToGitHubPackages") {
    dependsOn("publish")
    doLast {
        println("✓ Published to GitHub Packages")
    }
}

tasks.register("buildAndPublish") {
    dependsOn("clean", "build", "publishToMavenLocal")
    doLast {
        println("✓ Built and published shared-kernel library")
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
}

allprojects {
    group = "com.enaven.compism"
    version = "0.1.2"
}

subprojects {
    // Only configure published library modules
    if (name == "compism-core" || name == "compism-compose") {

        // Enables publishing (used by mavenLocal + JitPack)
        apply(plugin = "maven-publish")

        afterEvaluate {
            extensions.configure<PublishingExtension>("publishing") {
                publications.withType<MavenPublication>().configureEach {
                    if (name == "kotlinMultiplatform") {
                        artifactId = project.name
                    }
                }
            }
        }
    }
}
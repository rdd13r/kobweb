import com.varabyte.kobweb.gradle.publish.set

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    id("com.varabyte.kobweb.internal.publish")
}

group = "com.varabyte.kobweb"
version = libs.versions.kobweb.libs.get()

dependencies {
    implementation(kotlin("stdlib"))
    api(project(":common:kobweb-common"))
    api(libs.kaml)
}

kobwebPublication {
    artifactId.set("kobweb-common-yaml")
    description.set("YAML-specific extensions on top of the `kobweb-common` module.")
}

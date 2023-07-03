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
    implementation(libs.kotlinx.serialization.core.jvm)
    implementation(libs.kotlinx.coroutines)

    testImplementation(kotlin("test"))
    testImplementation(libs.truthish)
    testImplementation(libs.kotlinx.serialization.json)
}

kobwebPublication {
    artifactId.set("kobweb-common")
    description.set("A collection of utility classes for interacting with a Kobweb project needed by both frontend and backend codebases.")
}

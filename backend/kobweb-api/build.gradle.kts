import com.varabyte.kobweb.gradle.publish.set

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.varabyte.kobweb.internal.publish")
}

group = "com.varabyte.kobweb"
version = libs.versions.kobweb.libs.get()

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

//dependencies {
//    api(project(":frontend:kobweb-streams"))
//}

kobwebPublication {
    artifactId.set("kobweb-api")
    description.set("Classes related to extending API routes handled by a Kobweb server")
}

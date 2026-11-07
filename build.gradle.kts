plugins {
    `java-library`
    `maven-publish`
}

allprojects {
    group = "de.kevin_stefan.minecraft"
    version = "1.0.0"

    repositories {
        mavenCentral()
    }
}

tasks.forEach { task ->
    task.dependsOn(gradle.includedBuild("gradle-plugin").task(":" + task.name))
}

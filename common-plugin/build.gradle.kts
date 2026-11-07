plugins {
    `java-library`
    `maven-publish`
    id("de.kevin_stefan.minecraft.shared-gradle-plugin")
}

dependencies {
    api(projects.sharedCommon)
    sharedLibrary(libs.boostedyaml)
    compileOnly(libs.adventue.api)
    compileOnly(libs.adventue.minimessage)
}

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TheIdealMatrix/MinecraftShared")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

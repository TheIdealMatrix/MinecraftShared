plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "de.kevin_stefan.minecraft"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(gradleTestKit())
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}

gradlePlugin {
    plugins {
        register("shared-gradle-plugin") {
            id = "de.kevin_stefan.minecraft.shared-gradle-plugin"
            implementationClass = "de.kevin_stefan.minecraft.shared.gradle.SharedPlugin"
            displayName = "Shared library plugin"
            description = "Declares sharedLibrary dependencies and writes them to the libraries section of plugin.yml"
        }
    }
}

publishing {
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

tasks.test {
    useJUnitPlatform()
}

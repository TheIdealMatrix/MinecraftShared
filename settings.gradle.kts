pluginManagement {
    includeBuild("gradle-plugin")
}

rootProject.name = "MinecraftShared"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

sequenceOf(
    "common",
    "common-plugin",
    "paper"
).forEach {
    include("shared-$it")
    project(":shared-$it").projectDir = file(it)
}

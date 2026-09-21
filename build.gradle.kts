allprojects {
    group = "com.jlogicsoftware.kimeria"
    version = "0.0.1"

    repositories {
        mavenCentral()
        maven("https://s01.oss.sonatype.org")
    }
}

subprojects {
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

// Alias so `./gradlew run` (from the repo root, no module path to remember)
// launches the desktop build, same as `./gradlew :lwjgl3:run`.
tasks.register("run") {
    dependsOn(":lwjgl3:run")
}

// Same idea for the web build's plugin-generated task names, which are long
// because they encode module + platform + JS-vs-Wasm + variant.
tasks.register("runWeb") {
    description = "Starts the web build's dev server with auto-reload, same as ./gradlew :web:gdx_teavm_web_js_run."
    dependsOn(":web:gdx_teavm_web_js_run")
}

tasks.register("buildWeb") {
    description = "Builds the static, minified web output, same as ./gradlew :web:gdx_teavm_web_js_release_build."
    dependsOn(":web:gdx_teavm_web_js_release_build")
}

val gdxVersion: String by project
val appName: String by project

plugins {
    id("com.github.xpenatan.gdx-teavm") version "1.6.2"
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.github.xpenatan.gdx-teavm:gdx-controllers-web:1.6.2")
}

gdxTeaVM {
    assets(rootProject.file("assets"))

    webDefaults {
        mainClass = "com.jlogicsoftware.kimeria.web.WebLauncher"
        htmlTitle = appName
    }

    // Unnamed target: `./gradlew :web:gdx_teavm_web_js_run` for local dev,
    // with the TeaVM dev server rebuilding and reloading on change.
    js {
        devServer {
            enabled = true
            autoReload = true
        }
    }

    // Named "release" target: `./gradlew :web:gdx_teavm_web_js_release_build`
    // produces the static, minified output the GitHub Pages workflow deploys.
    js("release") {
        obfuscated = true
    }
}

val gdxVersion: String by project
val appName: String by project

plugins {
    application
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx-controllers:gdx-controllers-desktop:2.2.4")
}

// libGDX 1.14.2's gdx-backend-lwjgl3 bundles LWJGL 3.3.3, which fails to
// enumerate monitors (glfwGetPrimaryMonitor() returns NULL) on very new
// macOS releases. Force a newer LWJGL that has the GLFW/Cocoa fix.
val lwjglVersion = "3.4.3"
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.lwjgl") {
            useVersion(lwjglVersion)
        }
    }
}

application {
    mainClass.set("com.jlogicgames.kimeria.lwjgl3.Lwjgl3Launcher")
}

sourceSets {
    main {
        resources.srcDirs("../assets")
    }
}

tasks.jar {
    archiveBaseName.set(appName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.jlogicgames.kimeria.lwjgl3.Lwjgl3Launcher"
    }
    dependsOn(configurations.runtimeClasspath)
    from({ configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } })
}

tasks.named<JavaExec>("run") {
    // macOS needs -XstartOnFirstThread for LWJGL3/GLFW to create a window.
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        jvmArgs("-XstartOnFirstThread")

        // On some very new macOS builds, LWJGL's bundled GLFW native fails
        // to enumerate monitors (glfwGetPrimaryMonitor() returns NULL,
        // crashing window creation with an NPE in Checks.check). If a
        // system GLFW is installed (e.g. `brew install glfw`), prefer it --
        // see README "Troubleshooting" for details.
        val systemGlfw = listOf("/opt/homebrew/lib/libglfw.dylib", "/usr/local/lib/libglfw.dylib")
            .map(::File).firstOrNull { it.exists() }
        if (systemGlfw != null) {
            jvmArgs("-Dorg.lwjgl.glfw.libname=${systemGlfw.absolutePath}")
        }
    }
    standardInput = System.`in`
    workingDir = rootProject.file("assets")
}

val gdxVersion: String by project
val appName: String by project

plugins {
    application
}

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx-controllers:gdx-controllers-desktop:2.2.4")
}

// A separate source set (and classpath) for the one-off font-baking tool
// below, kept off the app's own dependencies -- and so out of the shipped
// jar: the GWT/web build can't run FreeTypeFontGenerator at all, so fonts
// are pre-baked to bitmap files once on desktop here and shipped as plain
// assets for every platform, including this one. gdx-freetype itself isn't
// a runtime dependency of the game anymore, see Assets.font().
sourceSets {
    create("fontBaker") {
        java.srcDir("src/fontBaker/java")
    }
}

val fontBakerImplementation: Configuration by configurations.getting

dependencies {
    fontBakerImplementation(project(":core"))
    fontBakerImplementation("com.badlogicgames.gdx:gdx-backend-headless:$gdxVersion")
    fontBakerImplementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    fontBakerImplementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    fontBakerImplementation("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    fontBakerImplementation("com.badlogicgames.gdx:gdx-tools:$gdxVersion") {
        exclude(group = "com.badlogicgames.gdx", module = "gdx-backend-lwjgl")
    }
}

tasks.register<JavaExec>("bakeFonts") {
    description = "Regenerates assets/fonts/generated/*.fnt+.png from the .ttf sources. Run after changing a font, its sizes, or the baked charset."
    classpath = sourceSets["fontBaker"].runtimeClasspath
    mainClass.set("com.jlogicsoftware.kimeria.tools.FontBaker")
    workingDir = rootProject.file("assets")
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
    mainClass.set("com.jlogicsoftware.kimeria.lwjgl3.Lwjgl3Launcher")
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
        attributes["Main-Class"] = "com.jlogicsoftware.kimeria.lwjgl3.Lwjgl3Launcher"
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

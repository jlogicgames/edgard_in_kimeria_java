package com.jlogicgames.kimeria.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.jlogicgames.kimeria.KimeriaGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Edgard in Kimeria");
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setWindowedMode(1280, 720);
        config.setResizable(true);
        new Lwjgl3Application(new KimeriaGame(), config);
    }
}

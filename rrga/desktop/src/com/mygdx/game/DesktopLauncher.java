package com.mygdx.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Gale - 8venture Games");
		config.setResizable(false);
		// Default size for Physics Lab 4
		config.setWindowedMode(1024, 576);
		if (arg.length > 0) {
			new Lwjgl3Application(new GDXRoot(arg[0]), config);
		}
		else {
			// standard, use this branch for shipping
			new Lwjgl3Application(new GDXRoot(), config);
		}

	}
}
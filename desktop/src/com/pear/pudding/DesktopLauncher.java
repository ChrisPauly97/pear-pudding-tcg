package com.pear.pudding;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import static com.pear.pudding.model.Constants.WINDOW_HEIGHT;
import static com.pear.pudding.model.Constants.WINDOW_WIDTH;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("Pear Pudding TCG");
		config.useVsync(true);
		config.setWindowedMode((int)WINDOW_WIDTH,(int)WINDOW_HEIGHT	);
		config.setIdleFPS(10);
		config.setInitialVisible(true);
		new Lwjgl3Application(new MyGame(), config);
	}
}

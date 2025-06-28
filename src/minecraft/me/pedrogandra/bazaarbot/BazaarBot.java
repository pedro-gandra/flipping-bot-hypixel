package me.pedrogandra.bazaarbot;

import org.lwjgl.opengl.Display;

import me.pedrogandra.bazaarbot.module.ModuleManager;

public class BazaarBot {

	public static String name = "BazaarBot", version = "1.8.8b1", creator = "Pedro Gandra"; 
	
	public static ModuleManager moduleManager;
	
	public static void startClient() {
		moduleManager = new ModuleManager();
		Display.setTitle(name + " v" + version + " by " + creator);
	}
}

package me.pedrogandra.bazaarbot;

import java.util.List;

import org.lwjgl.opengl.Display;

import me.pedrogandra.bazaarbot.auth.*;
import me.pedrogandra.bazaarbot.module.ModuleManager;

public class BazaarBot {
	
	public static BazaarBot instance = new BazaarBot();
	public static String name = "BazaarBot", version = "1.0", creator = "Pedro Gandra"; 
	public static ModuleManager moduleManager;
	
	public static void startClient() {
		moduleManager = new ModuleManager();
		Display.setTitle(name + " v" + version + " by " + creator);
		List<AuthAccount> loaded = AuthStorage.loadAccounts();
		for (AuthAccount acc : loaded) {
		    AuthManager.addAccount(acc);
		}
		if (!loaded.isEmpty()) {
	        AuthManager.setCurrentAccount(loaded.get(0));
	    }
	}
}

package me.pedrogandra.bazaarbot;

import java.util.List;

import org.lwjgl.opengl.Display;

import me.pedrogandra.bazaarbot.auth.*;
import me.pedrogandra.bazaarbot.commands.CommandManager;
import me.pedrogandra.bazaarbot.module.AutoBazaar;
import me.pedrogandra.bazaarbot.module.ModuleManager;
import me.pedrogandra.bazaarbot.utils.ResetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.WorldClient;

public class BazaarBot {
	
	public static BazaarBot instance = new BazaarBot();
	public static String name = "BazaarBot", version = "1.0", creator = "Pedro Gandra"; 
	public static ModuleManager moduleManager;
	public static CommandManager commandManager;
	public static WorldClient lastWorld = null;
	private final Minecraft mc = Minecraft.getMinecraft();
	private ResetManager rm = new ResetManager(); 
	
	public static void startClient() {
		moduleManager = new ModuleManager();
		commandManager = new CommandManager();
		Display.setTitle(name + " v" + version + " by " + creator);
		List<AuthAccount> loaded = AuthStorage.loadAccounts();
		for (AuthAccount acc : loaded) {
		    AuthManager.addAccount(acc);
		}
		if (!loaded.isEmpty()) {
	        AuthManager.setCurrentAccount(loaded.get(0));
	    }
	}
	
	public void checkReset() {
		if(AutoBazaar.instance != null && AutoBazaar.instance.isToggled()) {
			if (mc.currentScreen instanceof GuiDisconnected) {
			    rm.reconnectReset();
			} else {
				if(mc.theWorld!=null && mc.theWorld!=lastWorld) {
					if(lastWorld!=null)
						rm.inGameReset();
					lastWorld = mc.theWorld;
				}
			}
		}
	}
}

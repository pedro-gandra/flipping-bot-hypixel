package me.pedrogandra.flippingbot;

import java.util.List;

import org.lwjgl.opengl.Display;

import me.pedrogandra.flippingbot.auction.data.LogCache;
import me.pedrogandra.flippingbot.commands.CommandManager;
import me.pedrogandra.flippingbot.module.AutoBazaar;
import me.pedrogandra.flippingbot.module.ModuleManager;
import me.pedrogandra.flippingbot.utils.ResetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.WorldClient;

public class FlippingBot {
	
	public static FlippingBot instance = new FlippingBot();
	public static String name = "FlippingBot", version = "1.0", creator = "Pedro Gandra"; 
	public static ModuleManager moduleManager;
	public static CommandManager commandManager;
	public static WorldClient lastWorld = null;
	public static boolean failedReset = false;
	private final Minecraft mc = Minecraft.getMinecraft();
	private ResetManager rm = new ResetManager();
	public static double currentPurse;
	public static final String DATA_FOLDER = "C:/Dev/minecraft/BazaarBot/data-files/";
	//public static final String DATA_FOLDER = "/home/pedro/dev/minecraft/flipping-bot-hypixel/data-files/";
	
	public static void startClient() {
		moduleManager = new ModuleManager();
		commandManager = new CommandManager();
		Display.setTitle(name + " v" + version + " by " + creator);
		LogCache.cleanAll();
	}
	
	public void checkReset() {
		if(AutoBazaar.instance != null && (AutoBazaar.instance.isToggled() || failedReset)) {
			if (mc.currentScreen instanceof GuiDisconnected) {
				failedReset = false;
			    rm.reconnectReset();
			} else {
				if(mc.theWorld!=null && mc.theWorld!=lastWorld) {
					if(lastWorld!=null) {
						failedReset = false;
						rm.inGameReset();
					}
				}
			}
		}
		
		if(mc.theWorld!=null)
			lastWorld = mc.theWorld;
	}
}

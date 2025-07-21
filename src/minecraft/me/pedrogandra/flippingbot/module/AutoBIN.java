package me.pedrogandra.flippingbot.module;

import org.lwjgl.input.Keyboard;

import me.pedrogandra.flippingbot.bazaar.OrderManager;
import me.pedrogandra.flippingbot.commands.tests.TestString;
import me.pedrogandra.flippingbot.gui.GuiIngameHook;
import me.pedrogandra.flippingbot.utils.ChestManager;
import me.pedrogandra.flippingbot.utils.IOManager;
import me.pedrogandra.flippingbot.utils.MCUtils;
import me.pedrogandra.flippingbot.utils.ResetManager;

public class AutoBIN extends Module {

	public static AutoBIN instance;
	private ChestManager cm = new ChestManager();
	private IOManager io = new IOManager();
	private ResetManager rm = new ResetManager();
	private MCUtils mcu = new MCUtils();
	
	public AutoBIN() {
		super("AutoBIN", Keyboard.KEY_G);
		instance = this;
	}
	
}

package me.pedrogandra.flippingbot.module;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.lang.reflect.Type;

import org.lwjgl.input.Keyboard;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.pedrogandra.flippingbot.auction.AuctionItem;
import me.pedrogandra.flippingbot.bazaar.OrderManager;
import me.pedrogandra.flippingbot.commands.tests.TestString;
import me.pedrogandra.flippingbot.gui.GuiIngameHook;
import me.pedrogandra.flippingbot.utils.ChestManager;
import me.pedrogandra.flippingbot.utils.IOManager;
import me.pedrogandra.flippingbot.utils.KeyboardManager;
import me.pedrogandra.flippingbot.utils.MCUtils;
import me.pedrogandra.flippingbot.utils.ResetManager;
import net.minecraft.client.Minecraft;

public class AutoBIN extends Module {

	public static AutoBIN instance;
	private ChestManager cm = new ChestManager();
	private IOManager io = new IOManager();
	private ResetManager rm = new ResetManager();
	private MCUtils mcu = new MCUtils();
	private Minecraft mc = Minecraft.getMinecraft();
	public static ArrayList<AuctionItem> itemList = new ArrayList();
	public static int displayListStart = 0;
	
	private File configDir = new File(mc.mcDataDir, "config");
	private File file = new File(configDir, "auction_items.json");
    private static final Gson gson = new Gson();
    private final Type itemListType = new TypeToken<ArrayList<AuctionItem>>(){}.getType();
	
	public AutoBIN() {
		super("AutoBIN", Keyboard.KEY_G);
		instance = this;
		GuiIngameHook.bin = this;
		System.out.println("mcDataDir: " + mc.mcDataDir.getAbsolutePath());
		System.out.println("Expected file path: " + file.getAbsolutePath());
		loadItemList();
	}
	
	public void onEnable() {
		for (AuctionItem item : new ArrayList<>(itemList)) {
		    AuctionItem clone = gson.fromJson(gson.toJson(item), AuctionItem.class);
		    itemList.add(clone);
		}
	}
	
	public void onUpdate() {
		if(itemList != null && !itemList.isEmpty()) {
			if (KeyboardManager.isKeyJustPressed(Keyboard.KEY_RIGHT))
				displayListStart = Math.min(displayListStart+6, itemList.size() - 1);		 
			else if(KeyboardManager.isKeyJustPressed(Keyboard.KEY_LEFT)) {
				displayListStart = Math.max(displayListStart-6, 0);
			}
		}
	}
	
	public void saveItemList() {
	    try {
	        itemList.sort(Comparator.comparing(AuctionItem::getName, String.CASE_INSENSITIVE_ORDER));

	        if (!configDir.exists()) {
	            configDir.mkdirs();
	        }

	        try (Writer writer = new FileWriter(file)) {
	            gson.toJson(itemList, writer);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

    private void loadItemList() {
        if (!file.exists()) return;
        try (Reader reader = new FileReader(file)) {
            itemList = gson.fromJson(reader, itemListType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
}

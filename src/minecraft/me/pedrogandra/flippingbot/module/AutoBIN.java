package me.pedrogandra.flippingbot.module;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.lang.reflect.Type;

import org.lwjgl.input.Keyboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import me.pedrogandra.flippingbot.api.HypixelApiClient;
import me.pedrogandra.flippingbot.api.util.AuctionDataCache;
import me.pedrogandra.flippingbot.auction.AuctionInfo;
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
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class AutoBIN extends Module {

	public static AutoBIN instance;
	private HypixelApiClient api = new HypixelApiClient();
	private ChestManager cm = new ChestManager();
	private IOManager io = new IOManager();
	private ResetManager rm = new ResetManager();
	private MCUtils mcu = new MCUtils();
	private Minecraft mc = Minecraft.getMinecraft();
	private ArrayList<AuctionInfo> currentAuctionPage = new ArrayList();
	private AuctionDataCache auctionData = new AuctionDataCache();
	public static ArrayList<AuctionItem> itemList = new ArrayList();
	public static int displayListStart = 0;
	
	private File configDir = new File(mc.mcDataDir, "config");
	private File file = new File(configDir, "auction_items.json");
    private static final Gson gson = new Gson();
    private final Type itemListType = new TypeToken<ArrayList<AuctionItem>>(){}.getType();
    
    private boolean displayedItems;
    private boolean updateReady;
	
	public AutoBIN() {
		super("AutoBIN", Keyboard.KEY_G);
		instance = this;
		GuiIngameHook.bin = this;
		loadItemList();
	}
	
	public void onEnable() {
		this.setToggled(true);
		displayedItems = false;
		updateReady = false;
		new Thread(() -> {
			try {
				getCurrentPage();
				updateReady = true;
			} catch(Exception e) {
				io.sendError("Error inicializing auction bot: " + e.toString());
			}
		}).start();
	}
	
	
	public void onDisable() {
		this.setToggled(false);
		currentAuctionPage.clear();
	}
	
	public void onUpdate() {
		if(mc.currentScreen instanceof GuiChest && itemList != null && !itemList.isEmpty()) {
			if (KeyboardManager.isKeyJustPressed(Keyboard.KEY_RIGHT))
				displayListStart = Math.min(displayListStart+6, itemList.size() - 1);		 
			else if(KeyboardManager.isKeyJustPressed(Keyboard.KEY_LEFT)) {
				displayListStart = Math.max(displayListStart-6, 0);
			}
		}
	}
	
	private void displayOnChest() {
		IInventory inv = cm.getChestInventory();
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			AuctionInfo info = currentAuctionPage.get(i);
			ItemStack item = info.getItem();
			inv.setInventorySlotContents(i, item);
		}
	}
	
	private void getCurrentPage() {	
		try {
			JsonArray json = api.getAuctionData();
			auctionData.updateFromJson(json);
			this.currentAuctionPage = auctionData.getItemList();
			io.sendChat("Items fetched");
		} catch(Exception e) {
			io.sendError("Error getting auction latest info: " + e.toString());
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

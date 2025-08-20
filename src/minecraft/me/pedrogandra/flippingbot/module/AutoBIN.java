package me.pedrogandra.flippingbot.module;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.reflect.Type;

import org.lwjgl.input.Keyboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.api.HypixelApiClient;
import me.pedrogandra.flippingbot.api.util.AuctionDataCache;
import me.pedrogandra.flippingbot.auction.AuctionFlip;
import me.pedrogandra.flippingbot.auction.AuctionInfo;
import me.pedrogandra.flippingbot.auction.AuctionItem;
import me.pedrogandra.flippingbot.auction.AuctionLog;
import me.pedrogandra.flippingbot.auction.ml.HistoryManager;
import me.pedrogandra.flippingbot.auction.ml.PricePredictor;
import me.pedrogandra.flippingbot.auction.ml.categories.PetData;
import me.pedrogandra.flippingbot.auction.ml.utils.ItemParser;
import me.pedrogandra.flippingbot.bazaar.OrderManager;
import me.pedrogandra.flippingbot.commands.tests.TestString;
import me.pedrogandra.flippingbot.gui.GuiIngameHook;
import me.pedrogandra.flippingbot.utils.ChestManager;
import me.pedrogandra.flippingbot.utils.IOManager;
import me.pedrogandra.flippingbot.utils.IndexedMap;
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
	private HistoryManager hm = new HistoryManager();
	private ItemParser ip = new ItemParser();
	private PricePredictor pp = new PricePredictor();
	private ArrayList<AuctionLog> currentAuctionPage = new ArrayList();
	private AuctionDataCache auctionData = new AuctionDataCache();
	public static ArrayList<AuctionItem> itemList = new ArrayList();
	public static int displayListStart = 0;
	private long lastApiChange;
	
	private File configDir = new File(mc.mcDataDir, "config");
	private File file = new File(configDir, "auction_items.json");
    private static final Gson gson = new Gson();
    private final Type itemListType = new TypeToken<ArrayList<AuctionItem>>(){}.getType();
    
    private boolean updateData;
    private boolean isExecuting;
    
    public static double minPercentageProfit = 15;
    public static double minProfit = 500000;
	
	public AutoBIN() {
		super("AutoBIN", Keyboard.KEY_G);
		instance = this;
		GuiIngameHook.bin = this;
		loadItemList();
	}
	
	public void onEnable() {
		this.setToggled(true);
		
		/*
		updateData = false;
		isExecuting = true;
		getCurrentPage();
		*/
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
		
		if(this.isToggled() && !isExecuting) {
			isExecuting = true;
			if(updateData) {
				updateData = false;
				getCurrentPage();
			} else {
				updateData = true;
				checkItems();
			}
		}
	}
	
	private void checkItems() {
		
		new Thread(() -> {
			
			try {
				
				List<AuctionFlip> buyList = new ArrayList<>();
				for(AuctionLog entry : currentAuctionPage) {
					try {
						long sellPrice = entry.getSellPrice();
						if(sellPrice > FlippingBot.currentPurse * 0.5) continue;
						String type = hm.classifyItem(entry.getItem());
						if(type.equals("")) continue;
						long value = 0;
						if(type.equals("PET")) {
							PetData pet = ip.getAsPet(entry);
							value = (long) pp.pricePet(pet);
							value = roundPrice(value);
						}
						long profit = value - sellPrice;
						if(profit > minProfit && (double) profit/sellPrice > minPercentageProfit/100) {
							buyList.add(new AuctionFlip(entry.getId(), profit, value));
						}
					} catch(Exception e) {
						io.sendChat("Error analysing an item: " + e.toString());
					}
				}
				if(!buyList.isEmpty()) {
					buyList.sort((a, b) -> Long.compare(b.getProfit(), a.getProfit()));
					buyItems(buyList);
				}
				collectCoins();
				isExecuting = false;
				
			} catch(Exception e) {
				io.sendChat("General error when checking items: " + e.toString());
				isExecuting = false;
			}
			
		}).start();
	}
	
	private void collectCoins() throws Exception {
		Thread.sleep(1000);
		mc.thePlayer.sendChatMessage("/ah");
		if(cm.getItemInSlot(cm.slotCollectBIN).getTooltip(mc.thePlayer, false).toString().contains("Your auctions have")) {
			cm.clickSlot(cm.slotCollectBIN, 0, 0, true);
			if(cm.getItemInSlot(cm.slotClaimItems).getDisplayName().contains("Claim All"))
				cm.clickSlot(cm.slotClaimItems, 0, 0, true);
			else {
				cm.clickSlot(10, 0, 0, true);
				cm.clickSlot(cm.slotBuyBIN , 0, 0, true);
			}
		}
	}
	
	private void buyItems(List<AuctionFlip> buyList) {
		for(AuctionFlip f : buyList) {
			try {
				
				mc.thePlayer.sendChatMessage("/viewauction " + f.getId());
				Thread.sleep(800);
				cm.clickSlot(cm.slotBuyBIN, 0, 0, true);
				f.setItem(cm.getItemInSlot(13));
				cm.clickSlot(cm.slotConfirmBIN, 0, 0, true);
				Thread.sleep(1500);
				
			} catch(Exception e) {
				io.sendChat("Error when buying item: " + e.toString());
			}
		}
		
		sellItems(buyList);
	}
	
	private void sellItems(List<AuctionFlip> buyList) {
		try {
			
			mc.thePlayer.sendChatMessage("/ah");
			Thread.sleep(1000);
			if(cm.getItemInSlot(cm.slotManageBIN).getTooltip(mc.thePlayer, false).toString().contains("You don't have any outstading bids")) return;
			cm.clickSlot(cm.slotManageBIN, 0, 0, true);
			if(cm.getItemInSlot(cm.slotClaimItems).getDisplayName().contains("Claim All"))
				cm.clickSlot(cm.slotClaimItems, 0, 0, true);
			else {
				cm.clickSlot(10, 0, 0, true);
				cm.clickSlot(cm.slotBuyBIN , 0, 0, true);
			}
			
			IInventory inv = cm.getPlayerInventory();
			for(int i  = 0; i < inv.getSizeInventory(); i++) {
				ItemStack inSlot = cm.getItemInSlot(i);
				if(inSlot == null) continue;
				for(AuctionFlip f : buyList) {
					if(cm.equalItems(inSlot, f.getItem())) {
						Thread.sleep(1000);
						mc.thePlayer.sendChatMessage("/ah");
						Thread.sleep(1000);
						cm.clickSlot(i, 0, 0, false);
						cm.clickSlot(cm.slotBINDuration, 0, 0, true);
						cm.clickSlot(cm.slot24Hours, 0, 0, true);
						cm.clickSlot(cm.slotPrice, 0, 0, true);
						cm.writeSign(Long.toString(f.getValue()));
						cm.clickSlot(cm.slotBINCreate, 0, 0, true);
						cm.clickSlot(cm.slotConfirmBIN, 0, 0, true);
						break;
					}
				}
			}
				
		} catch(Exception e) {
			io.sendChat("Error selling items: " + e.toString());
		}
		
	}
	
	private void getCurrentPage() {
		new Thread(() -> {			
			try {
				long timeUpdate = this.lastApiChange;
				while (timeUpdate == lastApiChange) {
					currentAuctionPage.clear();
					JsonObject res = api.getAuctionData();
					timeUpdate = res.get("lastUpdated").getAsLong();
					if(res == null || timeUpdate == lastApiChange) {
						Thread.sleep(2000);
						continue;
					}
					JsonArray json = res.getAsJsonArray("auctions");
					auctionData.updateFromJson(json);
					this.currentAuctionPage = auctionData.getItemList();
					io.sendChat("Items fetched");
				}
				this.lastApiChange = timeUpdate;
				isExecuting = false;
			} catch(Exception e) {
				io.sendError("Error getting auction latest info: " + e.toString());
				isExecuting = false;
				updateData = true;
			}	
		}).start();
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
    
    private long roundPrice(long value) {
    	long base = (value / 100_000) * 100_000;
        return base - 4;
    }
	
}

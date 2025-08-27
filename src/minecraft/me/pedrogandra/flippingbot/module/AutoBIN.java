package me.pedrogandra.flippingbot.module;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.reflect.Type;
import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import me.pedrogandra.flippingbot.auction.AuctionLog;
import me.pedrogandra.flippingbot.auction.AuctionPreferences;
import me.pedrogandra.flippingbot.auction.data.ActiveAuctionCache;
import me.pedrogandra.flippingbot.auction.data.HistoryManager;
import me.pedrogandra.flippingbot.auction.data.LogCache;
import me.pedrogandra.flippingbot.auction.data.PricePredictor;
import me.pedrogandra.flippingbot.auction.data.categories.ArmorData;
import me.pedrogandra.flippingbot.auction.data.categories.ItemData;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;
import me.pedrogandra.flippingbot.auction.data.utils.ItemParser;
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
	private ArrayList<AuctionLog> currentAuctionPage = new ArrayList<>();
	private AuctionDataCache auctionData = new AuctionDataCache();
	private List<String> alreadyBought = new LinkedList();
	private ActiveAuctionCache fullAuction = new ActiveAuctionCache();
	public static int displayListStart = 0;
	private long lastApiChange;
	
	private File configDir = new File(mc.mcDataDir, "config");
	private File file = new File(configDir, "auction_items.json");
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = LogManager.getLogger();
    
    private boolean updateData;
    private boolean isExecuting;
    
    private static Map<String, AuctionPreferences> prefs = new HashMap<>();
	
	public AutoBIN() {
		super("AutoBIN", Keyboard.KEY_G);
		instance = this;
		GuiIngameHook.bin = this;
		prefs.put("PET", new AuctionPreferences(500_000, 15));
		prefs.put("REGULAR", new AuctionPreferences(500_000, 15));
		prefs.put("ARMOR", new AuctionPreferences(500_000, 15));
	}
	
	public void onEnable() {
		this.setToggled(true);
		updateData = true;
		isExecuting = false;
		alreadyBought.clear();
	}
	
	
	public void onDisable() {
		this.setToggled(false);
		currentAuctionPage.clear();
	}
	
	public void onUpdate() {
		
		if(this.isToggled() && !isExecuting) {
			isExecuting = true;
			if(updateData) {
				updateData = false;
				getCurrentPage();
			} else {
				updateData = true;
				cleanBoughtList();
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
						AuctionPreferences p = prefs.get(type);
						long value = 0;
						if(HistoryManager.updatingCache) {
							io.sendChat("Esperando 2s para evitar concorrencia de threads");
							Thread.sleep(2000);
						}
						value = (long) pp.priceItem(entry);
						value = roundPrice(value);
						long profit = value - sellPrice;
						if(profit > p.getMinProfit() && (double) profit/sellPrice > p.getMinMargin()/100 && !alreadyBought.contains(entry.getId())) {
							long cheapest = fullAuction.cheapestEquivalent(entry);
							cheapest = roundPrice(cheapest);
							if(cheapest != -1 && cheapest < value) {
								value = cheapest;
								profit = value - sellPrice;
								if(profit < p.getMinProfit() || (double) profit/sellPrice < p.getMinMargin()/100) continue;
							}
							buyList.add(new AuctionFlip(entry.getId(), profit, value));
							io.sendChat("Adicionado a lista de compra: " + entry.getItem().getDisplayName() + " - " + sn(sellPrice) + " | " + sn(value));
							alreadyBought.add(entry.getId());
						}
					} catch(Exception e) {
						io.sendChat("Error analysing an item: " + e.toString());
						LOGGER.error("Error analysing an item: ", e);
						e.printStackTrace();
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
				LOGGER.error("General error when checking items: ", e);
				e.printStackTrace();
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
		Iterator<AuctionFlip> it = buyList.iterator();
		while(it.hasNext()) {
			AuctionFlip f = it.next();
			try {			
				mc.thePlayer.sendChatMessage("/viewauction " + f.getId());
				Thread.sleep(800);
				if(mc.currentScreen instanceof GuiChest) {
					if(!cm.getItemInSlot(31).getDisplayName().contains("Collect")) {
						f.setItem(cm.getItemInSlot(13));
						cm.clickSlot(cm.slotBuyBIN, 0, 0, true);
						cm.clickSlot(cm.slotConfirmBIN, 0, 0, true);
						Thread.sleep(1500);
					} else it.remove();
				} else it.remove();
			} catch(Exception e) {
				io.sendChat("Error when buying item: " + e.toString());
				LOGGER.error("Error when buying item: ", e);
				e.printStackTrace();
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
			if(cm.getItemInSlot(cm.slotClaimItems).getDisplayName().contains("Claim All")) {
				cm.clickSlot(cm.slotClaimItems, 0, 0, true);
			} else {
				cm.clickSlot(10, 0, 0, true);
				cm.clickSlot(cm.slotBuyBIN , 0, 0, true);
			}
			
			Thread.sleep(1000);
			mc.thePlayer.sendChatMessage("/ah");
			Thread.sleep(1000);
			
			IInventory inv = cm.getPlayerInventory();
			for(int i  = 0; i < inv.getSizeInventory(); i++) {
				ItemStack inSlot = inv.getStackInSlot(i);
				if(inSlot == null) continue;
				Iterator<AuctionFlip> it = buyList.iterator();
				while(it.hasNext()) {
					AuctionFlip f = it.next();
					if(cm.equalItems(inSlot, f.getItem())) {
						cm.clickSlot(i, 0, 0, false);
						cm.clickSlot(cm.slotBINDuration, 0, 0, true);
						cm.clickSlot(cm.slot2Days, 0, 0, true);
						cm.clickSlot(cm.slotPrice, 0, 0, true);
						cm.writeSign(Long.toString(f.getValue()));
						cm.clickSlot(cm.slotBINCreate, 0, 0, true);
						cm.clickSlot(cm.slotConfirmBIN, 0, 0, true);
						it.remove();
						Thread.sleep(1000);
						mc.thePlayer.sendChatMessage("/ah");
						Thread.sleep(1000);
						break;
					}
				}
			}
				
		} catch(Exception e) {
			io.sendChat("Error selling items: " + e.toString());
			LOGGER.error("Error selling items: ", e);
			e.printStackTrace();
		}
		
	}
	
	private void cleanBoughtList() {
		Iterator<String> it = alreadyBought.iterator();
		while(it.hasNext() && alreadyBought.size() > 10) {
			it.next();
			it.remove();
		}
	}
	
	private void getCurrentPage() {
		new Thread(() -> {			
			try {
				long now = System.currentTimeMillis();
				long diff = now - fullAuction.lastUpdated;
				if(diff > 20*60*1000) {
					LogCache.updateAll();
					fullAuction.updateCache();
				}
				
				long timeUpdate = this.lastApiChange;
				while (timeUpdate == lastApiChange) {
					currentAuctionPage.clear();
					JsonObject res = api.getAuctionData(0);
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
				e.printStackTrace();
				isExecuting = false;
				updateData = true;
			}	
		}).start();
	}
    
    private long roundPrice(long value) {
    	if(value == -1) return value;
    	long base = (value / 100_000) * 100_000;
        return base - 2000;
    }
    
    private String sn(double n) {
    	DecimalFormat df = new DecimalFormat("#,###.##");
    	return df.format(n);
    }
	
}

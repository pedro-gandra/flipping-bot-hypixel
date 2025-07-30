package me.pedrogandra.flippingbot.module;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.lang.reflect.Type;

import org.lwjgl.input.Keyboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.api.HypixelApiClient;
import me.pedrogandra.flippingbot.api.util.AuctionDataCache;
import me.pedrogandra.flippingbot.auction.AuctionInfo;
import me.pedrogandra.flippingbot.auction.AuctionItem;
import me.pedrogandra.flippingbot.auction.ml.HistoryManager;
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
	private long lastApiChange;
	
	private File configDir = new File(mc.mcDataDir, "config");
	private File file = new File(configDir, "auction_items.json");
    private static final Gson gson = new Gson();
    private final Type itemListType = new TypeToken<ArrayList<AuctionItem>>(){}.getType();
    
    private boolean updateData;
    private boolean isExecuting;
	
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
		
		//currentAuctionPage.clear();
	}
	
	public void onUpdate() {
		if(mc.currentScreen instanceof GuiChest && itemList != null && !itemList.isEmpty()) {
			if (KeyboardManager.isKeyJustPressed(Keyboard.KEY_RIGHT))
				displayListStart = Math.min(displayListStart+6, itemList.size() - 1);		 
			else if(KeyboardManager.isKeyJustPressed(Keyboard.KEY_LEFT)) {
				displayListStart = Math.max(displayListStart-6, 0);
			}
		}
		
		/*if(mc.currentScreen instanceof GuiChest && !currentAuctionPage.isEmpty() && isExecuting == false) {
			displayOnChest();
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
		}*/
	}
	
	private void checkItems(){
		new Thread(()-> {
			try {
				for(AuctionInfo info : currentAuctionPage) {
					for(AuctionItem item : itemList) {
						try {
							if(info.getName().toLowerCase().contains(item.getName().toLowerCase()) && info.getRarity().equalsIgnoreCase(item.getRarity()) && info.getPrice() <= item.getPrice() && info.getPrice() <= FlippingBot.currentPurse) {
								ItemStack stack = info.getItem();
								List<String> tt = stack.getTooltip(mc.thePlayer, false);
								int ttSize = tt.size();
								if(item.isExcludeRecomb()) {
									String rarityInfo = tt.get(ttSize-1);
									if(mcu.cleanText(rarityInfo).contains("a")) continue;
								}
								if(item.getLevel() > 0) {
									int level = 0;
									int start = info.getName().indexOf("[");
									int end = info.getName().indexOf("]");
									if(start < 0 || end < 0) continue;
									String lvlString = mcu.getNumber(info.getName().substring(start+1, end));
									level = Integer.parseInt(lvlString);
									if(level < item.getLevel()) continue;
								}
								if(!(item.getGearScore().isEmpty())) {
									ArrayList<Integer> gs = item.getGearScore();
									boolean found = false;
									for(String t: tt) {
										t = mcu.cleanText(t);
										if(t.contains("Gear Score:")) {
											found = true;
											int divisor = t.indexOf("(");
											Double first = Double.parseDouble(mcu.getNumber(t.substring(0, divisor)));
											Double second = Double.parseDouble(mcu.getNumber(t.substring(divisor)));
											if(first < gs.get(0) || second < gs.get(1)) {
												found = false;
												break;
											}
										}
									}
									if(!found) continue;
								}
								if(!(item.getSpecs().isEmpty())) {
									String ttStr = mcu.cleanText(tt.toString()).toLowerCase();
									for(String spec : item.getSpecs()) {
										if(!(ttStr.contains(spec.toLowerCase()))) continue;
									}
								}
								mc.thePlayer.sendChatMessage("/viewauction " + info.getId());
								Thread.sleep(1000);
								buyItem(info, item);
								Thread.sleep(1500);
							}		
						} catch(Exception e) {
							io.sendError("Error when checking this item: "+ info.getName() + " - " + e.toString());
						}
					}
				}
				io.sendChat("Finished checking");
				isExecuting = false;
			} catch(Exception e) {
				io.sendError("Unexpected error checking items: " + e.toString());
			}
		}).start();
	}
	
	private void buyItem(AuctionInfo info, AuctionItem alvo) throws Exception {
		IInventory inv = cm.getChestInventory();
		ItemStack item = inv.getStackInSlot(cm.slotItemBIN);
		if(item == null) return;
		String name = mcu.cleanText(item.getDisplayName());
		if(!(name.equals(info.getName()))) return;
		List<String> listTT = item.getTooltip(mc.thePlayer, false);
		String tt = mcu.cleanText(listTT.toString());
		int s = tt.indexOf("Buy it now:");
		String temp = tt.substring(s);
		int e = temp.indexOf("coins");
		double price = Double.parseDouble(mcu.getNumber(temp.substring(0, e)));
		if(price != info.getPrice()) return;
		List<String> infoTT = info.getItem().getTooltip(mc.thePlayer, false);
		if(alvo.isExcludeRecomb()) {
			String rarityInfo = listTT.get(listTT.size()-1);
			if(mcu.cleanText(rarityInfo).contains("a")) return;
		}
		if(!(alvo.getGearScore().isEmpty())) {
			ArrayList<Integer> gs = alvo.getGearScore();
			boolean found = false;
			for(String t: listTT) {
				t = mcu.cleanText(t);
				if(t.contains("Gear Score:")) {
					found = true;
					int divisor = t.indexOf("(");
					Double first = Double.parseDouble(mcu.getNumber(t.substring(0, divisor)));
					Double second = Double.parseDouble(mcu.getNumber(t.substring(divisor)));
					if(first < gs.get(0) || second < gs.get(1)) {
						found = false;
						break;
					}
				}
			}
			if(!found) return;
		}
		if(!(alvo.getSpecs().isEmpty())) {
			tt = tt.toLowerCase();
			for(String spec : alvo.getSpecs()) {
				if(!(tt.contains(spec.toLowerCase()))) return;
			}
		}
		cm.clickSlot(cm.slotBuyBIN, 0, 0, true);
		Thread.sleep(500);
		cm.clickSlot(cm.slotConfirmBIN, 0, 0, true);
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
		new Thread(() -> {			
			try {
				long timeUpdate = this.lastApiChange;
				while (timeUpdate == lastApiChange) {
					currentAuctionPage.clear();
					JsonObject res = api.getAuctionData();
					timeUpdate = res.get("lastUpdated").getAsLong();
					if(timeUpdate == lastApiChange) continue;
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
	
}

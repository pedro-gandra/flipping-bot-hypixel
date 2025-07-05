package me.pedrogandra.bazaarbot.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.pedrogandra.bazaarbot.BazaarBot;
import me.pedrogandra.bazaarbot.api.HypixelApiClient;
import me.pedrogandra.bazaarbot.api.util.*;
import me.pedrogandra.bazaarbot.bazaar.BazaarItem;
import me.pedrogandra.bazaarbot.bazaar.OrderManager;
import me.pedrogandra.bazaarbot.commands.tests.TestString;
import me.pedrogandra.bazaarbot.gui.GuiIngameHook;
import me.pedrogandra.bazaarbot.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

public class AutoBazaar extends Module {
	
	public static AutoBazaar instance;
	private boolean refreshReady = false;
	private boolean isExecuting = false;
	private BazaarDataCache bazaarData = new BazaarDataCache();
	private HypixelApiClient api = new HypixelApiClient();
	private IndexedMap<String, BazaarItem> currentItems;
	private int currentIndex = 0;
	private ChestManager cm = new ChestManager();
	private IOManager io = new IOManager();
	private TestString ts = TestString.instance;
	private DelayManager dm = DelayManager.instance;
	private OrderManager om = OrderManager.instance;
	private MCUtils mcu = new MCUtils();
	
	public static boolean readPurseNow = false;
	
	
	public AutoBazaar() {
		super("AutoBazaar", Keyboard.KEY_F);
		instance = this;
		om.bz = this;
		GuiIngameHook.bz = this;
	}
	
	public IndexedMap<String, BazaarItem> getCurrentItems() {
		return currentItems;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}
	
	public BazaarItem getItemAt(int i) {
		if (currentItems == null || currentItems.isEmpty()) return null;
		return currentItems.getByIndex(i);
	}
	
	public BazaarItem getItemNamed(String name) {
		if (currentItems == null || currentItems.isEmpty()) return null;
		return currentItems.getByKey(name);
	}
	
	public void onEnable() {
		isExecuting = true;
		currentItems = new IndexedMap<>();
		new Thread(() -> {
            try {
            	om.initialPurse = 26000000;
            	readPurseNow = true;
            	JsonArray itemJson = api.getItemData();
        		bazaarData.loadDisplayNamesFromJson(itemJson);
        		refreshReady = true;
        		isExecuting = false;
            } catch (Exception e) {
            	io.sendError("Falha ao inicializar bot: " + e.toString());
                e.printStackTrace();
            }
        }).start();
	}
	
	public void onDisable() {
		refreshReady = false;
		isExecuting = false;
		currentIndex = 0;
		currentItems.clear();
		om.currentOrders.clear();
	}
	
	public void onUpdate() {
		
		if(this.isToggled()) {
			//main logic
			if(!isExecuting) {
				isExecuting = true;
				if (refreshReady) {
				    refreshReady = false;
				    callApiBazaar();
				} else {
					manageOrders();
				}
			}
			
			//navigate cards
			if(currentItems != null && !currentItems.isEmpty()) {
				if (KeyboardManager.isKeyJustPressed(Keyboard.KEY_RIGHT))
					 currentIndex = (currentIndex + 1) % currentItems.size();		 
				else if(KeyboardManager.isKeyJustPressed(Keyboard.KEY_LEFT)) {
					if(currentIndex == 0)
						currentIndex = currentItems.size()-1;
					else
						currentIndex -= 1;
				}
			}
			
		}
		
		if (KeyboardManager.isKeyJustPressed(Keyboard.KEY_G)) {

		}
		
	}
	
	private void callApiBazaar() {
		new Thread(() -> {
		    try {
		        JsonObject json = api.getBazaarData();
		        bazaarData.updateFromJson(json);
		        filterItems();
		        isExecuting = false;
		    } catch (Exception e) {
		    	io.sendError("Falha ao coletar dados da API: " + e.toString() + " - " + e.getMessage());
		        e.printStackTrace();
		    }
		}).start();
	}
	
	private void manageOrders() {
		
		new Thread(() -> {
			try {
				mc.thePlayer.sendChatMessage("/bz");
				Thread.sleep(500);
				om.checkOrders();
				Thread.sleep(500);
				om.createOrders();
				refreshReady = true;
				isExecuting = false;
			} catch (Exception e) {
				io.sendError("Falha ao trabalhar no bazaar: " + e.toString() + " - " + e.getMessage());
				e.printStackTrace();
			}
			
		}).start();
		
	}
	
	private void filterItems() {
		ArrayList<BazaarItem> list = bazaarData.getAllItems();
		currentItems.clear();
		for(BazaarItem item : list) {
			BazaarItem.QuickStatus q = item.getQuickStatus();
			List<BazaarItem.OrderSummary> b = item.getBuySummary();
			List<BazaarItem.OrderSummary> s = item.getSellSummary();		
			if(b != null && !b.isEmpty() && s != null && !s.isEmpty()) {
				long liquidity = Math.min(q.getBuyMovingWeek(), q.getSellMovingWeek());
				double hourlyLiquidity = item.getHourlyLiquidity();
				double bestBuy = item.getBestBuy();
				double bestSell = item.getBestSell();
				double spread = bestBuy - bestSell;
				double margin = bestBuy/bestSell;
				if(hourlyLiquidity*spread > 10000000 && margin > 1.3 && margin < 3 && hourlyLiquidity > 80 && hourlyLiquidity < 10000 && bestSell > 100000) {
					currentItems.put(item.getDisplayName(), item);
				}
			}
		}
		sortItems();
		IndexedMap<String, BazaarItem> top7 = new IndexedMap<>();
		int limit = Math.min(7, currentItems.size());
		for (int i = 0; i < limit; i++) {
		    String key = currentItems.getKeyByIndex(i);
		    BazaarItem value = currentItems.getByIndex(i);
		    top7.put(key, value);
		}
		currentItems = top7;
	}
	
	private void sortItems() {
	    currentItems.sort((a, b) -> {
	        double profitA = a.getHourlyLiquidity() * (a.getBestBuy() - a.getBestSell());
	        double profitB = b.getHourlyLiquidity() * (b.getBestBuy() - b.getBestSell());
	        return Double.compare(profitB, profitA);
	    });
	}
	

}

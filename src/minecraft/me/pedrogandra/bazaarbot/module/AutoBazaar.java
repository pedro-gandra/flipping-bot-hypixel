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
import me.pedrogandra.bazaarbot.commands.tests.TestString;
import me.pedrogandra.bazaarbot.gui.GuiIngameHook;
import me.pedrogandra.bazaarbot.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

public class AutoBazaar extends Module {
	
	public static AutoBazaar instance = new AutoBazaar();
	private boolean refreshReady = false;
	private BazaarDataCache bazaarData = new BazaarDataCache();
	private HypixelApiClient api = new HypixelApiClient();
	private IndexedMap<String, BazaarItem> currentItems;
	private int currentIndex = 0;
	private ChestManager cm = new ChestManager();
	private IOManager io = new IOManager();
	private TestString ts = TestString.instance;
	private DelayManager dm = DelayManager.instance;
	private MCUtils mcu = new MCUtils();
	
	
	public AutoBazaar() {
		super("AutoBazaar", Keyboard.KEY_F);
		instance = this;
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
		currentItems = new IndexedMap<>();
		new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	            	JsonArray itemJson = api.getItemData();
	        		bazaarData.loadDisplayNamesFromJson(itemJson);
	        		refreshReady = true;
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }).start();
	}
	
	public void onDisable() {
		refreshReady = false;
		currentIndex = 0;
		currentItems.clear();;
	}
	
	public void onUpdate() {
		
		//main logic
		if (refreshReady) {
		    refreshReady = false;
		    callApiBazaar();
		} else {
			
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
	
	private void callApiBazaar() {
		new Thread(() -> {
		    try {
		        JsonObject json = api.getBazaarData();
		        bazaarData.updateFromJson(json);
		        filterItems();
		    } catch (Exception e) {
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
				double spread = item.getBestBuy() - item.getBestSell();
				double margin = item.getBestBuy()/item.getBestSell();
				if(hourlyLiquidity*spread > 10000000 && margin > 1.3 && margin < 1.9 && hourlyLiquidity > 80 && hourlyLiquidity < 10000) {
					currentItems.put(item.getDisplayName(), item);
				}
			}
		}
		sortItems();
	}
	
	private void sortItems() {
	    currentItems.sort((a, b) -> {
	        double profitA = a.getHourlyLiquidity() * (a.getBestBuy() - a.getBestSell());
	        double profitB = b.getHourlyLiquidity() * (b.getBestBuy() - b.getBestSell());
	        return Double.compare(profitB, profitA);
	    });
	}
	

}

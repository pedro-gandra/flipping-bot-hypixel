package me.pedrogandra.bazaarbot.module;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.pedrogandra.bazaarbot.BazaarBot;
import me.pedrogandra.bazaarbot.api.HypixelApiClient;
import me.pedrogandra.bazaarbot.api.util.*;
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
	private ArrayList<BazaarItem> currentItems;
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
	
	public ArrayList<BazaarItem> getCurrentItems() {
		return currentItems;
	}
	
	public void onEnable() {
		currentItems = new ArrayList<BazaarItem>();
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
		currentItems.clear();
	}

	public BazaarItem getSpecificItem(int i) {
		if (currentItems == null || currentItems.isEmpty()) return null;
		return currentItems.get(i);
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
		new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	                JsonObject json = api.getBazaarData();
	                bazaarData.updateFromJson(json);
	                filterItems();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
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
				if(hourlyLiquidity*spread > 10000000 && margin > 1.04) {
					currentItems.add(item);
				}
			}
		}
	}
	

}

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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

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
	private long inicio = 0, fim;
	
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
				    readPurseNow = true;
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
		
	}
	
	private void callApiBazaar() {
		new Thread(() -> {
		    try {
		        JsonObject json = api.getBazaarData();
		        bazaarData.updateFromJson(json);
		        filterItems();
		        io.sendChat("Dados da API atualizados");
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
				int ciclos = 0;
				boolean buy = true;
				mc.thePlayer.sendChatMessage("/bz");
				Thread.sleep(500);
				while(ciclos < 100) {
					if(ciclos==60) {
						logTime("Operated with purchases for: ");
						buy = false;
						om.processOrders(buy, true, false);
					} else {
						om.processOrders(buy, false, false);
					}
					Thread.sleep(500);
					ciclos++;
				}
				logTime("Operated with only sells for: ");
				om.processOrders(false, false, true);
				om.currentOrders.clear();
				refreshReady = true;
				isExecuting = false;
			} catch (Exception e) {
				io.sendError("Falha ao trabalhar no bazaar: " + e.toString() + " - " + e.getMessage());
				e.printStackTrace();
			}
			
		}).start();
		
	}
	
	private void logTime(String mensagem) {
		if(inicio == 0)
			inicio = System.nanoTime();
		else {
			fim = System.nanoTime();
			double segundos = (fim-inicio)/1_000_000_000;
			io.sendChat(mensagem + segundos + " seconds");
			inicio = System.nanoTime();
		}
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
				if(hourlyLiquidity*spread > 10000000 && margin > 1.3 && margin < 3 && hourlyLiquidity > 60 && hourlyLiquidity < 10000 && bestSell > 100000) {
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
		    if(key.contains("cinder")) {
		    	key = "Cinderbat";
		    	value.setDisplayName(key);
		    }
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
	
	private void printItems() {
		for(int i = 0; i < currentItems.size(); i++) {
			BazaarItem item = currentItems.getByIndex(i);
			io.sendChat(item.getDisplayName() + ": " + item.getBestSell() + " - " + item.getBestBuy());
		}
	}
	

}

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
		    	io.sendError("Falha ao inicializar os items: " + e.toString() + " - " + e.getMessage());
		        e.printStackTrace();
		    }
		}).start();
	}
	
	private void manageOrders() {
		
		new Thread(() -> {
			try {
				boolean buy = true, finished = false;
				mc.thePlayer.sendChatMessage("/bz");
				Thread.sleep(800);
				long start = System.currentTimeMillis();
				double minutesPassed = 0;
				while(this.isToggled() && minutesPassed < 30) {
					if(om.updateOrderInfo() && !buy) {
						finished = true;
						break;
					}
					if(minutesPassed > 25 && buy) {
						buy = false;
						om.processOrders(buy, true, false);
					} else {
						om.processOrders(buy, false, false);
					}
					minutesPassed = (double) (System.currentTimeMillis() - start)/60000;
					io.sendChat("Minutes passed: "+io.formatDouble(minutesPassed));
				}
				if(this.isToggled() && !finished) {
					om.processOrders(false, false, true);
				}
				om.currentOrders.clear();
				refreshReady = true;
				isExecuting = false;
			} catch (Exception e) {
				io.sendError("Falha ao trabalhar no bazaar: " + e.toString() + " - " + e.getMessage());
				e.printStackTrace();
			}
			
		}).start();
		
	}
	
	private void filterItems() throws Exception {
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
				if(hourlyLiquidity*spread > 8000000 && margin > 1.2 && margin < 3 && hourlyLiquidity > 60 && bestSell > 50000) {
					currentItems.put(item.getDisplayName(), item);
				}
			}
		}
		sortItemsByProfit();
		getTopItems(40);
		itemValidation();
		sortItemsByCompetition();
		getTopItems(7);
	}
	
	private void itemValidation() throws Exception {
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(800);
		int repeats = 1;
		while(repeats <= 3) {
			for(int i = 0; i < currentItems.size(); i++) {
				try {
					
					BazaarItem item = currentItems.getByIndex(i);
					String name = item.getDisplayName();
					cm.clickSlot(cm.slotSearch, 0, 0, true);
					String write = name.substring(0, Math.min(15, name.length()));
					cm.writeSign(write);
					int slot = cm.getSlot(name, "", true);
					if(slot == -1) {
						item.validation.failedSearch = true;
						continue;
					}
					cm.clickSlot(slot, 0, 0, true);
					item.validation.buyOrdersCount += countNewOrders(cm.slotBuy, item.getBestSell(), true);
					item.validation.sellOrdersCount += countNewOrders(cm.slotSell, item.getBestBuy(), false);
					if(repeats == 3) {
						double initialSpread = item.getBestBuy() - item.getBestSell();
						double lowestSell = om.extractNumberFromTT(cm.slotSell, "each", "", "each");
						double highestBuy = om.extractNumberFromTT(cm.slotBuy, "each", "", "each");
						double newSpread = lowestSell - highestBuy;
						double spreadDiff = (Math.abs(newSpread-initialSpread)/initialSpread);
						if(spreadDiff > 0.2) 
							item.validation.safeSpread = false;
					}
					
					cm.clickSlot(cm.slotManageBack, 0, 0, true);
					
				} catch(Exception e) {
					io.sendError("Falha no loop de vaidação de items: " + e.toString());
				}
			}
			repeats++;
		}
		
		for (int i = currentItems.size() - 1; i >= 0; i--) {
			BazaarItem item = currentItems.getByIndex(i);
			if(item.validation.failedSearch || !item.validation.safeSpread)
				currentItems.remove(item.getDisplayName());
		}
	}
	
	private int countNewOrders(int slot, double oldOrder, boolean buy) throws Exception {
		ItemStack item = cm.getItemInSlot(slot);
		if(item != null) {
			List<String> tt  = item.getTooltip(mc.thePlayer, false);
			int firstOrderPos = -1;
			int count = 0;
			for(String t : tt) {
				t = mcu.cleanText(t);
				if(t.contains("each")) {
					if(firstOrderPos == -1)
						firstOrderPos = count;
					int end = t.indexOf("each");
					t = t.substring(0, end);
					double order = Double.parseDouble(mcu.getNumber(t));
					if(buy && order <= oldOrder) 
						return (count-firstOrderPos);
					else if(!buy && order >= oldOrder)
						return (count-firstOrderPos);
				}
				count++;
			}
		}
		return 7;
	}
	
	private void getTopItems(int n) {
		IndexedMap<String, BazaarItem> top = new IndexedMap<>();
		int limit = Math.min(n, currentItems.size());
		for (int i = 0; i < limit; i++) {
		    String key = currentItems.getKeyByIndex(i);
		    BazaarItem value = currentItems.getByIndex(i);
		    if(key.contains("Cinder")) {
		    	key = "Cinderbat";
		    	value.setDisplayName(key);
		    }
		    top.put(key, value);
		}
		currentItems = top;
	}
	
	private void sortItemsByProfit() {
	    currentItems.sort((a, b) -> {
	        double profitA = a.getHourlyLiquidity() * (a.getBestBuy() - a.getBestSell());
	        double profitB = b.getHourlyLiquidity() * (b.getBestBuy() - b.getBestSell());
	        return Double.compare(profitB, profitA);
	    });
	}
	
	private void sortItemsByCompetition() {
	    currentItems.sort((a, b) -> {
	        double competitionA = Math.pow(a.validation.buyOrdersCount, 2) + Math.pow(a.validation.sellOrdersCount, 2);
	        double competitionB = Math.pow(b.validation.buyOrdersCount, 2) + Math.pow(b.validation.sellOrdersCount, 2);
	        int result = Double.compare(competitionA, competitionB);
	        if(result!=0)
	        	return result;
	        double ppA = (a.getBestBuy()-a.getBestSell())*a.getHourlyLiquidity();
	        double ppB = (b.getBestBuy()-b.getBestSell())*b.getHourlyLiquidity();
	        return Double.compare(ppB, ppA);
	    });
	}
	
	private void printItems() {
		for(int i = 0; i < currentItems.size(); i++) {
			BazaarItem item = currentItems.getByIndex(i);
			io.sendChat(item.getDisplayName() + ": " + item.getBestSell() + " - " + item.getBestBuy());
		}
	}
	

}

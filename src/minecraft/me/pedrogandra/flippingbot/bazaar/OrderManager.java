package me.pedrogandra.flippingbot.bazaar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.module.AutoBazaar;
import me.pedrogandra.flippingbot.utils.ChestManager;
import me.pedrogandra.flippingbot.utils.IOManager;
import me.pedrogandra.flippingbot.utils.IndexedMap;
import me.pedrogandra.flippingbot.utils.MCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class OrderManager {

	public static OrderManager instance = new OrderManager();
	private final Minecraft mc = Minecraft.getMinecraft();
	public AutoBazaar bz;
	private ChestManager cm = new ChestManager();
	private MCUtils mcu = new MCUtils();
	private IOManager io = new IOManager();
	public Map<String, BazaarOrder> currentOrders = new HashMap<>();
	private static double initialPurse;
	
	public void updateOrderInfo() throws Exception {
		IndexedMap<String, BazaarItem> itemList = bz.getCurrentItems();
		int size = itemList.size();
		cm.clickSlot(cm.slotManage, 0, 0, true);
		for(int i = 0; i < size; i++) {
			BazaarItem item = itemList.getByIndex(i);
			String name = item.getDisplayName();
			BazaarOrder o = currentOrders.get(name);
			if(o==null)
				continue;
			int slotBuy = cm.getSlot(o.productName, "BUY", true);
			if(slotBuy == -1)
				o.currentBuyAmount = 0;
			else {
				double qtd = extractNumberFromTT(slotBuy, "Order amount", "", "");
				double filled = extractNumberFromTT(slotBuy, "Filled:", "", "/");
				if(filled != -1)
					qtd-=filled;
				o.currentBuyAmount = qtd*o.currentPurchasePrice;
			}
			int slotSell = cm.getSlot(o.productName, "SELL", true);
			if(slotSell == -1)
				o.currentSellAmount = 0;
			else {
				double qtd = extractNumberFromTT(slotSell, "Offer amount", "", "");
				double filled = extractNumberFromTT(slotSell, "Filled:", "", "/");
				if(filled != -1)
					qtd-=filled;
				o.currentSellAmount = qtd*o.currentPurchasePrice;
			}
			int qtdPlayer = cm.getAmountInInventory(name);
			o.currentInventoryAmount = qtdPlayer*o.currentPurchasePrice;
		}
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(800);
	}
	
	public void processOrders(boolean buyToggled, boolean removeAllBuys, boolean removeAllSells) throws Exception {
		IndexedMap<String, BazaarItem> itemList = bz.getCurrentItems();
		int size = itemList.size();
		for(int i = 0; i < size; i++) {
			
			try {
			
				double investment = initialPurse/size;
				BazaarItem item = itemList.getByIndex(i);
				String name = item.getDisplayName();
				BazaarOrder o = currentOrders.get(name);
				if(!buyToggled && !removeAllBuys && o.currentSellAmount == 0 && o.currentInventoryAmount == 0)
					continue;
				cm.clickSlot(cm.slotSearch, 0, 0, true);
				String write = name.substring(0, Math.min(15, name.length()));
				cm.writeSign(write);
				int slot = cm.getSlot(name, "", true);
				if(slot == -1)
					continue;
				cm.clickSlot(slot, 0, 0, true);
				double lowestSell = extractNumberFromTT(cm.slotSell, "each", "", "each");
				double highestBuy = extractNumberFromTT(cm.slotBuy, "each", "", "each");
				boolean updateBuy = false, updateSell = false;
				double volumeMax = (item.getHourlyLiquidity()*highestBuy)/2;
				
				if(o == null) {
					o = new BazaarOrder(name);
					if(createBuyOrder(o, investment, highestBuy, volumeMax)) {
						currentOrders.put(name, o);
						o.lastChecked = System.currentTimeMillis();
					}
					continue;
				}
				
				long now = System.currentTimeMillis();
				double sinceCheck = (double) (now - o.lastChecked)/1000;
				
				if (sinceCheck > 120 || removeAllBuys || removeAllSells || (o.currentBuyAmount > 0 && highestBuy != o.currentPurchasePrice) || (o.currentSellAmount > 0 && lowestSell != o.currentSalePrice)) {
					cm.clickSlot(cm.slotManageShort, 0, 0, true);
					o.lastChecked = System.currentTimeMillis();
					int slotBuy = cm.getSlot(o.productName, "BUY", true);
					int slotSell = cm.getSlot(o.productName, "SELL", true);
					double bought = 0, sold = 0;
					if(slotBuy != -1) {
						bought = collectBuys(slotBuy, o);
						if(removeAllBuys) {
							Thread.sleep(2500);
							removeBuyOrder(slotBuy, o);
						} else if(o.currentBuyAmount > 0 && highestBuy > o.currentPurchasePrice) {
							removeBuyOrder(slotBuy, o);
							updateBuy = true;
						}
					}	
					
					if(slotSell!=-1) {
						sold = collectSells(slotSell, o);
						if(removeAllSells) {
							Thread.sleep(2500);
							removeSellOrder(slotSell, o);
							int slotPlayer = cm.getSlot(name, "", false);
							cm.clickSlot(slotPlayer, 0, 0, false);
							cm.clickSlot(cm.slotSellInstantly, 0, 0, true);
						} else if(o.currentSellAmount > 0 && lowestSell < o.currentSalePrice) {
							if(updateBuy)
								Thread.sleep(3500);
							removeSellOrder(slotSell, o);
							updateSell = true;
						}
					}
					
					if(buyToggled && (updateBuy || slotBuy == -1)) {
						int slotManageBack = cm.getSlot("Go Back", "", true);
						cm.clickSlot(slotManageBack, 0, 0, true);
						createBuyOrder(o, investment, highestBuy, volumeMax);
					}
					
					int slotPlayer = cm.getSlot(name, "", false);
					
					if(updateSell || (slotSell == -1 && slotPlayer != -1)) {
						if(!removeAllSells)
							createSellOrder(slotPlayer, o);
					}
					
				}
				
				mc.thePlayer.sendChatMessage("/bz");
				Thread.sleep(800);
			
			} catch (Exception e) {
				io.sendError("Falha no loop de processamento de ordens: " + e.toString());
			}
		}
		
	}
	
	
	public void liquidateSales() throws Exception {
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(800);
		captureSales();
		int orders;
		do {
			orders = 0;
			for(Map.Entry<String, BazaarOrder> entry : currentOrders.entrySet()) {
				try {
					String name = entry.getKey();
					BazaarOrder o = entry.getValue();
					if(o.currentSellAmount <= 0)
						continue;
					orders++;
					cm.clickSlot(cm.slotSearch, 0, 0, true);
					String write = name.substring(0, Math.min(15, name.length()));
					cm.writeSign(write);
					int slot = cm.getSlot(name, "", true);
					if(slot == -1)
						continue;
					cm.clickSlot(slot, 0, 0, true);
					double lowestSell = extractNumberFromTT(cm.slotSell, "each", "", "each");
					long now = System.currentTimeMillis();
					double sinceCheck = (double) (now - o.lastChecked)/1000;
					if(sinceCheck > 120 || lowestSell != o.currentSalePrice) {
						cm.clickSlot(cm.slotManageShort, 0, 0, true);
						o.lastChecked = System.currentTimeMillis();
						int slotSell = cm.getSlot(o.productName, "SELL", true);
						if(slotSell!=-1) {
							collectSells(slotSell, o);
							if(o.currentSellAmount > 0 && lowestSell < o.currentSalePrice) {
								removeSellOrder(slotSell, o);
								int slotPlayer = cm.getSlot(name, "", false);
								createSellOrder(slotPlayer, o);
							}
						}
					}
					mc.thePlayer.sendChatMessage("/bz");
					Thread.sleep(800);
				} catch (Exception e) {
					orders++;
					io.sendError("Erro no loop de liquidação de vendas: " + e.toString());
				}
			}
		} while(orders > 0);
		currentOrders.clear();
	}
	
	public void captureSales() throws Exception {
		currentOrders.clear();
		Thread.sleep(800);
		cm.clickSlot(cm.slotManage, 0, 0, true);
		IInventory inv = cm.getChestInventory();
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			try {
				ItemStack stack = cm.getItemInSlot(i);
				if(stack==null)
					continue;
				String name = mcu.cleanText(stack.getDisplayName());
				if(name.contains("SELL")) {
					String itemName = name.replace("SELL", "").trim();
					BazaarOrder o = new BazaarOrder(itemName);
					o.currentSalePrice = extractNumberFromTT(i, "Price per unit:", "", "");
					double total = extractNumberFromTT(i, "Offer amount:", "", "");
					double filled = extractNumberFromTT(i, "Filled:", "", "/");
					if(filled != -1)
						total-=filled;
					o.currentSellAmount = total*o.currentSalePrice;
					o.lastChecked = System.currentTimeMillis();
					currentOrders.put(itemName, o);
				}
			}	catch(Exception e) {
				io.sendError("Falha ao capturar ordens de venda: " + e.toString());
			}
		}
		int slotManageBack = cm.getSlot("Go Back", "", true);
		cm.clickSlot(slotManageBack, 0, 0, true);
	}
	
	private boolean createSellOrder(int slotPlayer, BazaarOrder o) throws Exception {
		boolean success = true;
		success = (success && cm.clickSlot(slotPlayer, 0, 0, false));
		success = (success && cm.clickSlot(cm.slotSell, 0, 0, true));
		double price = extractNumberFromTT(cm.slotPrice, "Unit price", "", "");
		double amount = extractNumberFromTT(cm.slotPrice, "Selling:", "", "");
		success = (success && cm.clickSlot(cm.slotPrice, 0, 0, true));
		success = (success && cm.clickSlot(cm.slotSubmit, 0, 0, true));
		if(success) {
			o.currentSalePrice = price;
			o.currentInventoryAmount = 0;
			o.currentSellAmount = o.currentPurchasePrice*amount;
		}
		return success;
	}
	
	public void removeBuyOrder(int slot, BazaarOrder o) throws Exception {
		
		String name = mcu.cleanText(cm.getItemInSlot(slot).getDisplayName());
		while(slot != -1) {
			cm.clickSlot(slot, 0, 0, true);
			ItemStack item = cm.getItemInSlot(cm.slotCancelBuy);
			if(item != null) {
				String nameCancel = mcu.cleanText(item.getDisplayName());
				if(nameCancel!=null && nameCancel.contains("Cancel Order")) {
					while(!(cm.clickSlot(cm.slotCancelBuy, 0, 0, true)));
				}
			}
			slot = cm.getSlot(name, "", true);
		}
		if(o!=null)
			o.currentBuyAmount = 0;
	}
	
	private double collectBuys(int slotBuy, BazaarOrder o) throws Exception {
		double bought = 0;
    	boolean updatedValue = false;
        while(slotBuy != -1 && (bought = extractNumberFromTT(slotBuy, "items to claim", "", "")) > 0) {
        	if(!updatedValue) {
        		 o.currentBuyAmount -= bought*o.currentPurchasePrice;
        		 o.currentInventoryAmount += bought*o.currentPurchasePrice;
        		updatedValue = true;
        	}
        	cm.clickSlot(slotBuy, 0, 0, true);
        	slotBuy = cm.getSlot(o.productName, "BUY", true);
        }
        
        if(slotBuy==-1)
        	o.currentBuyAmount = 0;
        
        return bought;
	}
	
	public void removeSellOrder(int slot, BazaarOrder o) throws Exception {
		String name = mcu.cleanText(cm.getItemInSlot(slot).getDisplayName());
		while(slot != -1) {
			cm.clickSlot(slot, 0, 0, true);
			ItemStack item = cm.getItemInSlot(cm.slotCancelSell);
			if(item != null) {
				String nameCancel = mcu.cleanText(item.getDisplayName());
				if(nameCancel!=null && nameCancel.contains("Cancel Order")) {
					while(!(cm.clickSlot(cm.slotCancelSell, 0, 0, true)));
				}
			}
			slot = cm.getSlot(name, "", true);
		}
		if(o!=null)
			o.currentSellAmount = 0;
	}
	
	private double collectSells(int slotSell, BazaarOrder o) throws Exception {
		double sold = 0;
        boolean updatedValue = false;
        while(slotSell != -1 && (sold = extractNumberFromTT(slotSell, "coins to claim", "", "")) > 0) {
        	if(!updatedValue) {
        		o.currentSellAmount -= sold;
        		updatedValue = true;
        	}
    		cm.clickSlot(slotSell, 0, 0, true);
    		slotSell = cm.getSlot(o.productName, "SELL", true);
        }
        
        if(slotSell==-1)
        	o.currentSellAmount = 0;
        
        return sold;
	}
	
	private boolean createBuyOrder(BazaarOrder o, double investment, double highestBuy, double volumeMax) throws Exception {
		investment -= (o.currentSellAmount+o.currentInventoryAmount);
		//investment = Math.min(investment, volumeMax);
		investment = Math.min(investment, FlippingBot.currentPurse);
		int nItems = (int) Math.min(255, (investment/(highestBuy+1)));
		if(nItems <= 0)
			return false;
		boolean success = true;
		success = (success && cm.clickSlot(cm.slotBuy, 0, 0, true));
		cm.clickSlot(cm.slotAmount, 0, 0, true);
		cm.writeSign(nItems + "");
		double price = extractNumberFromTT(cm.slotPrice, "Unit price", "", "");
		o.currentPurchasePrice = price;
		o.currentBuyAmount = nItems*price;
		success = (success && cm.clickSlot(cm.slotPrice, 0, 0, true));
		success = (success && cm.clickSlot(cm.slotSubmit, 0, 0, true));
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(500);
		return success;
	}
	
	public double extractNumberFromTT(int slot, String search, String s1, String s2) throws Exception {
		ItemStack item = cm.getItemInSlot(slot);
		if(item != null) {
			List<String> tt  = item.getTooltip(mc.thePlayer, false);
			for(String t : tt) {
				t = mcu.cleanText(t);
				if(t.contains(search)) {
					int start = 0, end = t.length();
					if(s1!="")
						start = t.indexOf(s1);
					if(s2!="")
						end = t.indexOf(s2);
					t = t.substring(start, end);
					return Double.parseDouble(mcu.getNumber(t));
				}
			}
		}
		return -1;
	}
	
	public void printOrders() {
		Iterator<Map.Entry<String, BazaarOrder>> iterator = currentOrders.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, BazaarOrder> map = iterator.next();
			BazaarOrder o = map.getValue();
			IOManager.sendChat(o.productName + ": " + (o.currentBuyAmount/o.currentPurchasePrice) + " / " + o.currentPurchasePrice);
		}
	}
	
	public static void setInitialPurse(double p) {
		initialPurse = Math.min(p, 500000000);
	}
	
}

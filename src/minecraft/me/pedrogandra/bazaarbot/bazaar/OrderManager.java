package me.pedrogandra.bazaarbot.bazaar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.pedrogandra.bazaarbot.module.AutoBazaar;
import me.pedrogandra.bazaarbot.utils.ChestManager;
import me.pedrogandra.bazaarbot.utils.IOManager;
import me.pedrogandra.bazaarbot.utils.IndexedMap;
import me.pedrogandra.bazaarbot.utils.MCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class OrderManager {

	public static OrderManager instance = new OrderManager();
	private final Minecraft mc = Minecraft.getMinecraft();
	public AutoBazaar bz;
	private ChestManager cm = new ChestManager();
	private MCUtils mcu = new MCUtils();
	private IOManager io = new IOManager();
	public Map<String, BazaarOrder> currentOrders = new HashMap<>();
	public static double initialPurse;
	public static double currentPurse;
	
	public boolean updateOrderInfo() throws Exception {
		IndexedMap<String, BazaarItem> itemList = bz.getCurrentItems();
		int size = itemList.size();
		int nSells = 0;
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
				nSells++;
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
		Thread.sleep(500);
		return (nSells==0);
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
				
				if(o == null) {
					o = new BazaarOrder(name);
					if(createBuyOrder(o, investment, highestBuy)) {
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
							if(removeBuyOrder(slotBuy, o))
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
							if(removeSellOrder(slotSell, o))
								updateSell = true;
						}
					}
					
					if(buyToggled && (updateBuy || slotBuy == -1)) {
						cm.clickSlot(cm.slotManageBack, 0, 0, true);
						createBuyOrder(o, investment, highestBuy);
					}
					
					int slotPlayer = cm.getSlot(name, "", false);
					
					if(updateSell || (slotSell == -1 && slotPlayer != -1)) {
						if(!removeAllSells)
							createSellOrder(slotPlayer, o);
					}
					
				}
				
				mc.thePlayer.sendChatMessage("/bz");
				Thread.sleep(500);
			
			} catch (Exception e) {
				io.sendError("Falha no loop de processamento de ordens: " + e.toString());
			}
		}
		
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
	
	private boolean removeBuyOrder(int slot, BazaarOrder o) throws Exception {
		boolean success = true;
		success = (success && cm.clickSlot(slot, 0, 0, true));
		success = (success && cm.clickSlot(cm.slotCancelBuy, 0, 0, true));
		if(success)
			o.currentBuyAmount = 0;
		return success;
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
	
	private boolean removeSellOrder(int slot, BazaarOrder o) throws Exception {
		boolean success = true;
		success = (success && cm.clickSlot(slot, 0, 0, true));
		success = (success && cm.clickSlot(cm.slotCancelSell, 0, 0, true));
		if(success)
			o.currentSellAmount = 0;
		return success;
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
	
	private boolean createBuyOrder(BazaarOrder o, double investment, double highestBuy) throws Exception {
		investment -= (o.currentSellAmount+o.currentInventoryAmount);
		investment = Math.min(investment, currentPurse);
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
	
}

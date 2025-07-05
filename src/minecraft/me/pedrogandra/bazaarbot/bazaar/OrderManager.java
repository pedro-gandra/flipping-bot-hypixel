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
	public Map<String, BazaarOrder> currentOrders = new HashMap<>();
	public static double initialPurse;
	public static double currentPurse;
	
	public void checkOrders() throws InterruptedException {
		
		cm.clickSlot(cm.slotManage, 0, 0, true);
		Thread.sleep(500);
		if(!currentOrders.isEmpty()) {
			Iterator<Map.Entry<String, BazaarOrder>> iterator = currentOrders.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, BazaarOrder> map = iterator.next();
	            String name = map.getKey();
	            BazaarOrder o = map.getValue();
	            BazaarItem itemInfo = bz.getItemNamed(name);
	            
	            int slotBuy = cm.getSlot(name, "BUY", true);
	            int slotSell = cm.getSlot(name, "SELL", true);
	            
	            //REMOVE ORDENS PERDIDAS
	            if(slotBuy==-1 && slotSell == -1) {
	            	iterator.remove();
	            	continue;
	            }
	            
	            //COLETA OS ITEMS JA COMPRADOS
	        	int bought;
	        	boolean updatedValue = false;
	            while(slotBuy != -1 && (bought = (int) extractNumberFromTT(slotBuy, "items to claim")) > 0) {
	            	if(!updatedValue) {
	            		o.currentBuyAmount -= bought*o.currentPurchasePrice;
	            		updatedValue = true;
	            	}
	            	cm.clickSlot(slotBuy, 0, 0, true);
	            	Thread.sleep(500);
	            	slotBuy = cm.getSlot(name, "BUY", true);
	            }
	            
	            //CANCELA A ORDEM DE COMPRA SE NECESSÁRIO
	            if(slotBuy != -1) {
		            if(itemInfo == null) {
		            	removeBuyOrder(slotBuy, o);
		            	if(slotSell == -1) {
		            		iterator.remove();
		            		continue;
		            	}
		            } else if(itemInfo.getBestBuy() < o.currentPurchasePrice) {
		            	removeBuyOrder(slotBuy, o);
		            	o.updateBuy = true;
		            }
	            }
	            
	            //COLETA AS MOEDAS DOS ITEMS JA VENDIDOS
	            double sold;
	            updatedValue = false;
	            while(slotSell != -1 && (sold = extractNumberFromTT(slotSell, "coins to claim")) > 0) {
	            	if(!updatedValue) {
	            		o.currentSellAmount -= sold;
	            		updatedValue = true;
	            	}
	        		cm.clickSlot(slotSell, 0, 0, true);
	        		Thread.sleep(500);
	        		slotSell = cm.getSlot(name, "SELL", true);
	            }
	            
	            //COLOCA ITEMS COMPRADOS A VENDA JUNTO COM OS ATUAIS
	            int slotPlayer = cm.getSlot(name, "", false);
	            if(slotPlayer != -1) {
	            	if(slotSell != -1) {
	            		removeSellOrder(slotSell, o);
	            	}
	            	createSellOrder(slotPlayer, o);
	            }
	            
	            //ATUALIZA ORDEM DE VENDA
	            if(slotSell != -1 && (itemInfo == null || o.currentSalePrice > itemInfo.getBestSell())) {
	            	removeSellOrder(slotSell, o);
	            	slotPlayer = cm.getSlot(name, "", false);
	            	createSellOrder(slotPlayer, o);
	            }
	            
	        }
		}
    }
	
	public void createOrders() throws Exception {
		IndexedMap<String, BazaarItem> itemList = bz.getCurrentItems();
		int size = itemList.size();
		int cont = 0;
		cm.clickSlot(cm.slotManageBack, 0, 0, true);
		Thread.sleep(500);
		for(int i = 0; i < size; i++) {
			double investment = initialPurse/size;
			BazaarItem item = itemList.getByIndex(i);
			String name = item.getDisplayName();
			BazaarOrder o = currentOrders.get(name);
			if(o == null) {
				o = new BazaarOrder(name);
			} else if(o.updateBuy) {
				investment -= o.currentSellAmount;
				o.updateBuy = false;
			}
			investment = Math.min(investment, currentPurse);
			int nItems =  (int) Math.min(255, (investment/(item.getBestBuy() +1)));
			if(nItems <= 0)
				continue;
			cm.clickSlot(cm.slotSearch, 0, 0, true);
			Thread.sleep(500);
			String write = name.substring(0, Math.min(15, name.length()));
			cm.writeSign(write);
			Thread.sleep(500);
			int slot = cm.getSlot(name, "", true);
			cm.clickSlot(slot, 0, 0, true);
			Thread.sleep(500);
			cm.clickSlot(cm.slotBuy, 0, 0, true);
			Thread.sleep(500);
			cm.clickSlot(cm.slotAmount, 0, 0, true);
			Thread.sleep(500);
			cm.writeSign(nItems + "");
			Thread.sleep(500);
			double price = extractNumberFromTT(cm.slotPrice, "Unit price");
			o.currentPurchasePrice = price;
			o.currentBuyAmount = nItems*price;
			cm.clickSlot(cm.slotPrice, 0, 0, true);
			Thread.sleep(500);
			cm.clickSlot(cm.slotSubmit, 0, 0, true);
			Thread.sleep(500);
			mc.thePlayer.sendChatMessage("/bz");
			Thread.sleep(500);
		}
	}
	
	private double extractNumberFromTT(int slot, String search) {
		ItemStack item = cm.getItemInSlot(slot);
		List<String> tt  = item.getTooltip(mc.thePlayer, false);
		for(String t : tt) {
			if(t.contains(search)) {
				t = mcu.cleanText(t);
				return Double.parseDouble(mcu.getNumber(t));
			}
		}
		return -1;
	}
	
	private void createSellOrder(int slotPlayer, BazaarOrder o) throws InterruptedException {
		cm.clickSlot(slotPlayer, 0, 0, false);
		Thread.sleep(500);
		cm.clickSlot(cm.slotSell, 0, 0, true);
		Thread.sleep(500);
		double price = extractNumberFromTT(cm.slotPrice, "Unit price");
		o.currentSalePrice = price;
		o.currentSellAmount = o.currentPurchasePrice*extractNumberFromTT(cm.slotPrice, "Selling:");
		cm.clickSlot(cm.slotPrice, 0, 0, true);
		Thread.sleep(500);
		cm.clickSlot(cm.slotSubmit, 0, 0, true);
		Thread.sleep(500);
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(500);
		cm.clickSlot(cm.slotManage, 0, 0, true);
		Thread.sleep(500);
	}
	
	private void removeSellOrder(int slot, BazaarOrder o) throws InterruptedException {
		cm.clickSlot(slot, 0, 0, true);
		Thread.sleep(500);
		cm.clickSlot(cm.slotCancelSell, 0, 0, true);
		o.currentSellAmount = 0;
		Thread.sleep(500);
	}
	
	private void removeBuyOrder(int slot, BazaarOrder o) throws InterruptedException {	
		cm.clickSlot(slot, 0, 0, true);
		Thread.sleep(500);
		cm.clickSlot(cm.slotCancelBuy, 0, 0, true);
		o.currentBuyAmount = 0;
		Thread.sleep(500);
	}
	
}

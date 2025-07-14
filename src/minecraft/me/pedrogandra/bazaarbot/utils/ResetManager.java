package me.pedrogandra.bazaarbot.utils;

import me.pedrogandra.bazaarbot.bazaar.OrderManager;
import me.pedrogandra.bazaarbot.module.AutoBazaar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ResetManager {
	
	private final Minecraft mc = Minecraft.getMinecraft();
	private ChestManager cm = new ChestManager();
	private MCUtils mcu = new MCUtils();
	private IOManager io = new IOManager();
	private OrderManager om = OrderManager.instance;
	
	public void deleteAllOrders() throws Exception {
		int orders = 0;
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(800);
		cm.clickSlot(cm.slotManage, 0, 0, true);
		IInventory inv = cm.getChestInventory();
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			try {
				ItemStack stack = cm.getItemInSlot(i);
				if(stack==null)
					continue;
				String name = mcu.cleanText(stack.getDisplayName());
				if(name.contains("BUY")) {
					orders++;
					om.removeBuyOrder(i, null);
					Thread.sleep(3500);
				} else if(name.contains("SELL")) {
					orders++;
					om.removeSellOrder(i, null);
					Thread.sleep(3500);
				}
			} catch(Exception e) {
				io.sendError("Falha ao remover uma das ordens no reset: " + e.toString());
			}
		}
		
		if(orders > 0)
			deleteAllOrders();
		
		mc.thePlayer.sendChatMessage("/bz");
		Thread.sleep(800);
		if(cm.ttHas(cm.slotSellInventory, "Click to sell")) {
			cm.clickSlot(cm.slotSellInventory, 0, 0, true);
			cm.clickSlot(cm.slotConfirmSellInventory, 0, 0, true);
		}
	}
	
	public void inGameReset() {
		AutoBazaar.instance.onDisable();
		new Thread(() -> {
			try {
				Thread.sleep(8000);
				mc.thePlayer.sendChatMessage("/hub");
				Thread.sleep(20000);
				mc.thePlayer.sendChatMessage("/warp home");
				Thread.sleep(8000);
				deleteAllOrders();
				Thread.sleep(4000);
				AutoBazaar.instance.onEnable();
			} catch(Exception e) {
				io.sendError("Erro ao performar In Game Reset" + e.toString());
			}
		}).start();
	}
	
	public void reconnectReset() {
		AutoBazaar.instance.onDisable();
		new Thread(() -> {
			try {
				boolean tryReconnect = true;
				while(tryReconnect) {
					Thread.sleep(20000);
					if (!(mc.currentScreen instanceof GuiDisconnected)) {
						tryReconnect = false;
                        break;
                    }
					ServerData serverData = new ServerData("Reconnect", "hypixel.net", false);
                    GuiScreen parent = Minecraft.getMinecraft().currentScreen;
                    mc.displayGuiScreen(
                    	new GuiConnecting(parent, mc, serverData)
                    );
				}
				while (mc.thePlayer == null) {
				    Thread.sleep(200);
				}
				Thread.sleep(6000);
				mc.thePlayer.sendChatMessage("/skyblock");
				Thread.sleep(10000);
				mc.thePlayer.sendChatMessage("/warp home");
				Thread.sleep(8000);
				deleteAllOrders();
				Thread.sleep(4000);
				AutoBazaar.instance.onEnable();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	
}

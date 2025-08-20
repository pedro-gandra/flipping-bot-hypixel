package me.pedrogandra.flippingbot.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class ChestManager {
	
	public final int slotSellInstantly = 11;
	public final int slotCancelBuy = 11;
	public final int slotCancelSell = 13;
	public final int slotSubmit = 13;
	public final int slotPrice = 12;
	public final int slotAmount = 16;
	public final int slotBuy = 15;
	public final int slotSell = 16;
	public final int slotSearch = 45;
	public final int slotManage = 50;
	public final int slotManageShort = 32;
	public final int slotProductBack = 31;
	public final int slotSellInventory = 47;
	public final int slotConfirmSellInventory = 11;
	
	public final int slotItemBIN = 13;
	public final int slotBuyBIN = 31;
	public final int slotConfirmBIN = 11;
	public final int slotClaimItems = 21;
	public final int slotManageBIN = 13;
	public final int slotCollectBIN = 15;
	
	public final int slotBINDuration = 33;
	public final int slot24Hours = 13;
	public final int slotBINPrice = 31;
	public final int slotBINCreate = 29;
	
    private final Minecraft mc = Minecraft.getMinecraft();
    private final MCUtils mcu = new MCUtils();

    public GuiChest getOpenChest() {
        if (mc.currentScreen instanceof GuiChest) {
            return (GuiChest) mc.currentScreen;
        }
        return null;
    }

    public IInventory getChestInventory() {
        GuiChest chest = getOpenChest();
        if (chest != null) {
            return chest.lowerChestInventory;
        }
        return null;
    }
    
    public IInventory getPlayerInventory() {
        GuiChest chest = getOpenChest();
        if (chest != null) {
            return chest.upperChestInventory;
        }
        return null;
    }

    public ItemStack getItemInSlot(int slot) {
        IInventory inv = getChestInventory();
        return inv != null ? inv.getStackInSlot(slot) : null;
    }
    
    public int getAmountInInventory(String name) {
    	IInventory inv = getPlayerInventory();
    	if (inv == null) return 0;
    	int amount = 0;
    	for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item != null) {
	            if(item.getDisplayName().equalsIgnoreCase(name)) {
	            	amount += item.stackSize;
	            }
            }
    	}
    	return amount;
    }
    
    public boolean ttHas(int slot, String str) {
    	ItemStack item = getItemInSlot(slot);
    	if(item!=null) {
    		List<String> tt  = item.getTooltip(mc.thePlayer, false);
			for(String t : tt) {
				t = mcu.cleanText(t).toLowerCase();
				if(t.contains(str.toLowerCase()))
					return true;
			}
    	}
    	return false;
    }
    
    public int getSlot(String name, String type, boolean chest) {
    	IInventory inv;
    	if(chest)
    		inv = getChestInventory();
    	else
    		inv = getPlayerInventory();
        if (inv == null) return -1;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item != null) {
            	String itemName = MCUtils.cleanText(item.getDisplayName());
            	if(type.equals("")) {
	                if(itemName.equalsIgnoreCase(name))
	                    return i;
            	} else {
            		itemName.toLowerCase();
            		if(itemName.contains(name.toLowerCase()) && itemName.contains(type.toLowerCase()))
            			return i;
            	}
            }
        }
        return -1;
    }

    public boolean clickSlot(int slotId, int mouseButton, int mode, boolean chest) throws Exception {
    	if (!(mc.currentScreen instanceof GuiChest))
    		return false;
    	ItemStack itemInicial = getItemInSlot(slotId);
    	int trueSlot = slotId;
    	if(!chest) {
    		if(trueSlot <= 8) trueSlot+=36;
    		trueSlot += getChestInventory().getSizeInventory()-9;
    	}
        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, trueSlot, mouseButton, mode, mc.thePlayer);
        Thread.sleep(1000);
        if(!(mc.currentScreen instanceof GuiChest))
        	return true;
        ItemStack novoItem = getItemInSlot(slotId);
        if(novoItem == null)
        	return true;
        if(itemInicial.getDisplayName() == novoItem.getDisplayName())
        	return false;
        return true;
    }
    
    public void printTooltip(int slot) {
    	ItemStack item = getItemInSlot(slot);
    	if(item==null) return;
    	for(String t : item.getTooltip(mc.thePlayer, false)) {
    		t = t.replace("§", "&");
    		IOManager.sendChat("TT: " + t);
    	}
    }
    
    public boolean equalItems(ItemStack i1, ItemStack i2) {
    	if(i1 == null || i2 == null)
    		return false;
    	List<String> tt1 = i1.getTooltip(mc.thePlayer, false);
    	List<String> tt2 = i2.getTooltip(mc.thePlayer, false);
    	int count = 0;
    	for(int i = 0; i < tt1.size(); i++) {
    		if(tt1.get(i).equals(tt2.get(i))) {
    			count++;
    		}
    		if(count == 6 && i == 5)
    			return true;
    		if(i>=5)
    			return false;
    	}
    	return false;
    }
    
    public void writeSign(final String s) throws Exception {
    	if(!(mc.currentScreen instanceof GuiEditSign)) return;
        final GuiEditSign gui = (GuiEditSign) mc.currentScreen;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final int keyCode = getKeyCodeForChar(c);
            gui.keyTyped(c, keyCode);
            Thread.sleep(50);
        }
        MCUtils.clickButton("done");
        Thread.sleep(1000);
    }
    
    private int getKeyCodeForChar(char c) {
        if (c >= 'a' && c <= 'z') return Keyboard.KEY_A + (c - 'a');
        if (c == ' ') return Keyboard.KEY_SPACE;
        return Keyboard.KEY_NONE;
    }
}

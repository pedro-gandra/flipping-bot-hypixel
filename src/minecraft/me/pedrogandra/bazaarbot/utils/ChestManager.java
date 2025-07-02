package me.pedrogandra.bazaarbot.utils;

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
	
	public final int slotSubmit = 13;
	public final int slotPrice = 12;
	public final int slotAmount = 16;
	public final int slotBuy = 15;
	public final int slotSell = 16;
	public final int slotSearch = 45;
	public final int slotManage = 50;
	
    private final Minecraft mc = Minecraft.getMinecraft();
    private DelayManager dm = DelayManager.instance;

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

    public String getSlotName(int slot) {
        ItemStack item = getItemInSlot(slot);
        return item != null && item.hasDisplayName() ? item.getDisplayName() : null;
    }

    public ItemStack getItemInSlot(int slot) {
        IInventory inv = getChestInventory();
        return inv != null ? inv.getStackInSlot(slot) : null;
    }
    
    public int getSlotByItemName(String name) {
        IInventory inv = getChestInventory();
        if (inv == null) return -1;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            if (item != null) {
            	String itemName = MCUtils.cleanText(item.getDisplayName());
                if (itemName.equalsIgnoreCase(name)) {
                    return i;
                }
            }
        }

        return -1;
    }
    
    public void printItemInfo() {
    	IInventory inv = getChestInventory();
        if (inv == null) return;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack item = inv.getStackInSlot(i);
            int cont = 0;
            if (item != null) {
	            String name = item.getDisplayName();
	            IOManager.sendChat("Nome: " + name);
	            name = MCUtils.cleanText(name);
	            IOManager.sendChat("Nome: " + name);
            }
        }
    }

    public void clickSlot(int slotId, int mouseButton, int mode) {
        mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slotId, mouseButton, mode, mc.thePlayer);
    }
    
    public void writeSign(final String s) {
    	if(!(mc.currentScreen instanceof GuiEditSign)) return;
    	
        final GuiEditSign gui = (GuiEditSign) mc.currentScreen;

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            final int keyCode = getKeyCodeForChar(c);

            dm.schedule(() -> {
                try {
                    gui.keyTyped(c, keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 50 * i);
        }
    }
    
    private int getKeyCodeForChar(char c) {
        if (c >= 'a' && c <= 'z') return Keyboard.KEY_A + (c - 'a');
        if (c == ' ') return Keyboard.KEY_SPACE;
        return Keyboard.KEY_NONE;
    }
}

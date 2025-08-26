package me.pedrogandra.flippingbot.utils;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;

public class KeyboardManager {

    private static Set<Integer> currentlyPressed = new HashSet<Integer>();
    private static Set<Integer> justPressed = new HashSet<Integer>();

    public static void update() {
        justPressed.clear();
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if(!(screen instanceof GuiChat) && !(screen instanceof GuiEditSign)) {
	        for (int key = 0; key < Keyboard.KEYBOARD_SIZE; key++) {
	            boolean isDown = Keyboard.isKeyDown(key);
	            boolean wasDown = currentlyPressed.contains(key);
	            if (isDown && !wasDown) {
	                justPressed.add(key);
	                currentlyPressed.add(key);
	            } else if (!isDown && wasDown) {
	                currentlyPressed.remove(key);
	            }
	        }
        }
    }

    public static boolean isKeyJustPressed(int keyCode) {
        return justPressed.contains(keyCode);
    }

    public static boolean isKeyDown(int keyCode) {
        return currentlyPressed.contains(keyCode);
    }
}

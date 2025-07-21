package me.pedrogandra.flippingbot.utils;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class IOManager {

    private static final Minecraft mc = Minecraft.getMinecraft();
    
    public static void sendChat(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "[Bot] " + EnumChatFormatting.RESET + message));
        }
    }

    public static void sendError(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[Erro] " + EnumChatFormatting.RESET + message));
        }
    }

    public static void log(String message) {
        System.out.println("[IOManager] " + message);
    }

    public static void printCurrentScreen() {
        if (mc.currentScreen != null) {
            sendChat("Tela atual: " + mc.currentScreen.getClass().getSimpleName());
        } else {
            sendChat("Nenhuma tela aberta.");
        }
    }
    
    public static String formatDouble(double n) {
    	DecimalFormat df = new DecimalFormat("#,###.##");
		return df.format(n);
	}
}

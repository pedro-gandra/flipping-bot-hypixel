package me.pedrogandra.flippingbot.gui;

public class GuiUtil {
	
	public int color(String str) {
		if(str.equalsIgnoreCase("YELLOW"))
			return 0Xe7ea0b;
		if(str.equalsIgnoreCase("GRAY"))
			return 0xB5FFFFFF;
		if(str.contains("SPECIAL"))
			return 0xd20b0b;
		if(str.equalsIgnoreCase("MYTHIC"))
			return 0xfb26da;
		if(str.equalsIgnoreCase("LEGENDARY"))
			return 0xf9980e;
		if(str.equalsIgnoreCase("EPIC"))
			return 0x7e18f9;
		if(str.equalsIgnoreCase("RARE"))
			return 0X0b14e4;
		if(str.equalsIgnoreCase("UNCOMMON"))
			return 0x0cd629;
		return 0xffffff;
	}
	
}

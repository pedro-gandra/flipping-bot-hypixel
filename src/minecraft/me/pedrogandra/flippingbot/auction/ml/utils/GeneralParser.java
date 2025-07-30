package me.pedrogandra.flippingbot.auction.ml.utils;

public class GeneralParser {
	
	public int rarity(String str, int type) {
		
		if(type == 0) {
			if(str.contains("UNCOMMON"))
				return 1;
			if(str.contains("COMMON"))
				return 0;
			if(str.contains("RARE"))
				return 2;
			if(str.contains("EPIC"))
				return 3;
			if(str.contains("LEGENDARY"))
				return 4;
			if(str.contains("MYTHIC"))
				return 5;
			if(str.contains("VERY SPECIAL"))
				return 7;
			if(str.contains("SPECIAL"))
				return 6;
		} else if(type == 1) {
			if(str.contains("§f"))
				return 0;
			if(str.contains("§a"))
				return 1;
			if(str.contains("§9"))
				return 2;
			if(str.contains("§5"))
				return 3;
			if(str.contains("§6"))
				return 4;
			if(str.contains("§d"))
				return 5;
			if(str.contains("§c"))
				return 6;
		}
			
		
		return -1;
	}
	
}

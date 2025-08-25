package me.pedrogandra.flippingbot.auction.data.utils;

import java.util.List;

import me.pedrogandra.flippingbot.utils.MCUtils;

public class GeneralParser {
	
	private MCUtils mcu = new MCUtils();
	
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
	
	public int dungeonStars(String s) {
		int cont = 0;
		for(int i = 0; i < s.length(); i++) {
			if(s.charAt(i) == '✪')
				cont++;
		}
		return cont;
	}
	
	public int masterStars(String s) {
		if(s.contains("➊"))
			return 1;
		if(s.contains("➋"))
			return 2;
		if(s.contains("➌"))
			return 3;
		if(s.contains("�?"))
			return 4;
		if(s.contains("➎"))
			return 5;
		return 0;
	}
	
	public int hpbAmount(String s) {
		int gain = Integer.parseInt(mcu.getNumber(s.substring(0, s.indexOf(")"))));
		return gain/2;
	}
	
	public double averageGem(String s) {
		int n = 0;
		double cont = 0;
		for(int i = 0; i < s.length() ; i++) {
			if(s.charAt(i) == '[') {
				n+=rarity(s.substring(i-2, i), 1)+1;
				cont++;
			}
		}
		return n/cont;
	}
	
	public int romanToInt(String s) {
	    int total = 0, prev = 0;
	    for (char c : s.toUpperCase().toCharArray()) {
	        int curr;
	        switch (c) {
	            case 'I': curr = 1; break;
	            case 'V': curr = 5; break;
	            case 'X': curr = 10; break;
	            default: throw new IllegalArgumentException("Letra inválida: " + c);
	        }
	        total += curr > prev ? curr - 2 * prev : curr;
	        prev = curr;
	    }
	    if (total < 1 || total > 20)
	        throw new IllegalArgumentException("Número romano fora do intervalo (1–20)");
	    return total;
	}
	
	public int lastLineTT(List<String> tt) {
		int i = 0; 
		for(String t : tt) {
			if(t.contains("-----"))
				return i-1;
			i++;
		}
		return tt.size()-1;
	}
	
}

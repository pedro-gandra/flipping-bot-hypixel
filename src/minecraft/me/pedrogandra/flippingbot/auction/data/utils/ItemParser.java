package me.pedrogandra.flippingbot.auction.data.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.pedrogandra.flippingbot.auction.AuctionLog;
import me.pedrogandra.flippingbot.auction.data.categories.ArmorData;
import me.pedrogandra.flippingbot.auction.data.categories.ItemData;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;
import me.pedrogandra.flippingbot.utils.MCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ItemParser {

	private MCUtils mcu = new MCUtils();
	private Minecraft mc = Minecraft.getMinecraft();
	private GeneralParser gp = new GeneralParser();
	
	public PetData getAsPet(AuctionLog log) {
		ItemStack item = log.getItem();
		String itemName = mcu.cleanText(item.getDisplayName());
		int divisor = itemName.indexOf("]");
		String levelString = itemName.substring(0, itemName.indexOf("]"));
		String rest = itemName.substring(divisor+1);
		if(rest.indexOf("]") != -1)
			rest = itemName.substring(rest.indexOf("]")+1);
		
		List<String> tt = item.getTooltip(mc.thePlayer, false);
		int lastLine = gp.lastLineTT(tt);
		String name = rest.replaceAll("[^a-zA-Z ]", "").trim().toLowerCase();
		int rarity = gp.rarity(tt.get(lastLine), 0);
		int lvl = Integer.parseInt(mcu.getNumber(levelString));
		int petCandy = 0;
		String petItem = "none";
		int itemRarity = -1;
		String skin = "none";
		
		String skinInfo = tt.get(1);
		if(skinInfo.contains("Skin"))
			skin = skinInfo.substring(skinInfo.indexOf(",")+1).trim();
			
		
		
		for(String t: tt) {
			t = t.toLowerCase();
			if(t.contains("pet candy used")) {
				t = mcu.cleanText(t);
				int d = t.indexOf("/");
				petCandy = Integer.parseInt(mcu.getNumber(t.substring(0, d)));
			} else if(t.contains("held item:")) {
				int start = t.indexOf("held item:") + 10;
				String str = t.substring(start);
				itemRarity = gp.rarity(str, 1);
				petItem = mcu.cleanText(str).trim();
			}
		}
		
		return new PetData(name, rarity, log.getSoldAt(), log.getSellPrice(), lvl, petCandy, petItem, itemRarity, skin);
	}
	
	public ItemData getAsRegularItem(AuctionLog log) {
		ItemStack item = log.getItem();
		String name = mcu.cleanText(item.getDisplayName()).replaceAll("[^a-zA-Z ]", "");
		name = name.toLowerCase().trim();
		List<String> tt = item.getTooltip(mc.thePlayer, false);
		int rarity = gp.rarity(tt.get(gp.lastLineTT(tt)), 0);
		
		return new ItemData(name, rarity, log.getSoldAt(), log.getSellPrice());
		
	}
	
	public ArmorData getAsArmor(AuctionLog log) {
		String reforge = "none";
		int dungeonStars = -1;
		int masterStars = -1;
		int hpb = 0;
		boolean aop = false;
		double averageGem = 0;
		String dye = "none";
		String skin = "none";
		ArmorData piece;
		
		try {
		
			ItemStack item = log.getItem();
			String displayName = item.getDisplayName();
			String nameWReforge = mcu.cleanText(item.getDisplayName()).replaceAll("[^a-zA-Z ]", "").toLowerCase().replace("shiny", "").trim();
			
			List<String> tt = item.getTooltip(mc.thePlayer, false);
			String finalTT = tt.get(gp.lastLineTT(tt));
			boolean reforged = false;
			for(String t : tt) {
				if(t.contains("�9("))
					reforged = true;
				if(t.contains("Defense:") && t.contains(("�e(")))
					hpb = gp.hpbAmount(t.substring(t.indexOf("�e(")+3));
				if(t.contains("Health:") && t.contains("�c["))
					aop = true;
				if(t.contains("[") && mcu.cleanText(t).replaceAll("[^a-zA-Z]", "").length() == 0)
					averageGem = gp.averageGem(t);
				if(t.contains("Dyed") && mcu.cleanText(t).replace("Dyed", "").length() > 0)
					dye = mcu.cleanText(t).replaceAll("[^a-zA-Z ]", "").replace("Dyed", "").toLowerCase().trim();
				if(t.contains("Skin") && t.contains("�8"))
					skin = mcu.cleanText(t).replace("Skin", "").trim();
			}
			
			if(finalTT.contains("DUNGEON")) {
				dungeonStars = gp.dungeonStars(displayName);
				masterStars = gp.masterStars(displayName);
			}
			
			String name;
			if(reforged) {
				String[] split = nameWReforge.split(" ");
				ArrayList<String> splitList = new ArrayList();
				for(String s : split) {
					splitList.add(s);
				}
				reforge = splitList.get(0);
				splitList.remove(0);
				name = String.join(" ", splitList);
			} else
				name = nameWReforge;
				
			int rarity = gp.rarity(finalTT, 0);
			
			piece = new ArmorData(name, rarity, log.getSoldAt(), log.getSellPrice(), reforge, dungeonStars, masterStars, hpb, aop, averageGem, dye, skin);
			Map<String, Integer> encMap = piece.getEnchantments();
			String ttStr = tt.toString().toLowerCase();
			for(Entry<String, Integer> entry : encMap.entrySet()) {
				int index;
				String tempStr = new String(ttStr);
				String key = entry.getKey();
				do {
					index = tempStr.indexOf(key);
					if(index != -1) {
						String analyze = tempStr.substring(index-2);
						analyze = analyze.substring(0, analyze.indexOf(","));
						if(analyze.charAt(0) == '�' && (analyze.charAt(1) == 'l' || analyze.charAt(1) == '9')) {
							analyze = mcu.cleanText(analyze).replace(key, "").trim();
							int value = gp.romanToInt(analyze);
							encMap.replace(key, value);
							break;
						}
						tempStr = tempStr.substring(index+1);
					}
				} while(index != -1);
			}
		
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return piece;
	}
	
	public AuctionLog itemToLog(ItemStack item) {
		String id = "test";
		long soldAt = System.currentTimeMillis();
		long price = -1;
		List<String> tt = item.getTooltip(mc.thePlayer, false);
		boolean remove = false;
		int i = 0;
		for (String t : tt) {
		    if (t.contains("Buy it now:")) {
		        price = Long.parseLong(mcu.getNumber(mcu.cleanText(t)));
		    }
		}
		return new AuctionLog(id, soldAt, price, item);
	}
	
}

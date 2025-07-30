package me.pedrogandra.flippingbot.auction.ml.utils;

import java.util.List;

import me.pedrogandra.flippingbot.auction.AuctionLog;
import me.pedrogandra.flippingbot.auction.ml.categories.ItemData;
import me.pedrogandra.flippingbot.auction.ml.categories.PetData;
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
		String name = rest.replaceAll("[^a-zA-Z ]", "").trim().toLowerCase();
		int rarity = gp.rarity(tt.get(tt.size()-1), 0);
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
		int rarity = gp.rarity(tt.get(tt.size()-1), 0);
		
		return new ItemData(name, rarity, log.getSoldAt(), log.getSellPrice());
		
	}
	
}

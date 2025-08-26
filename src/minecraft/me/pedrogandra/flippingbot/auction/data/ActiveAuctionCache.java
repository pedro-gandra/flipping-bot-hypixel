package me.pedrogandra.flippingbot.auction.data;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.pedrogandra.flippingbot.api.HypixelApiClient;
import me.pedrogandra.flippingbot.api.util.AuctionDataCache;
import me.pedrogandra.flippingbot.auction.AuctionLog;
import me.pedrogandra.flippingbot.auction.data.categories.*;
import me.pedrogandra.flippingbot.auction.data.utils.ItemParser;
import me.pedrogandra.flippingbot.utils.ChestManager;
import me.pedrogandra.flippingbot.utils.IOManager;
import me.pedrogandra.flippingbot.utils.MCUtils;
import me.pedrogandra.flippingbot.utils.ResetManager;
import net.minecraft.client.Minecraft;

public class ActiveAuctionCache {
	
	private HypixelApiClient api = new HypixelApiClient();
	private IOManager io = new IOManager();
	private MCUtils mcu = new MCUtils();
	private Minecraft mc = Minecraft.getMinecraft();
	private AuctionDataCache auctionData = new AuctionDataCache();
	private HistoryManager hm = new HistoryManager();
	private ItemParser ip = new ItemParser();
	
	public static long lastUpdated = 0;
	public static List<PetData> listedPets = new ArrayList<>();
	public static List<ItemData> listedRegulars = new ArrayList<>();
	public static List<ArmorData> listedArmor = new ArrayList<>();
	
	public void updateCache() {
		boolean apiResult = true;
		listedPets.clear();
		try {
			for(int i = 0; i < 30; i++) {
				JsonObject res = api.getAuctionData(i);
				apiResult = res.get("success").getAsBoolean();
				if(apiResult) {
					lastUpdated = res.get("lastUpdated").getAsLong();
					JsonArray json = res.getAsJsonArray("auctions");
					auctionData.updateFromJson(json);
					for(AuctionLog l : auctionData.getItemList()) {
						if(l.getSellPrice() < 500_000) continue;
						String type = hm.classifyItem(l.getItem());
						if(type.equals("")) continue;
						if(type.equals("PET")) {
							listedPets.add(ip.getAsPet(l));
						} else if(type.equals("REGULAR")) {
							listedRegulars.add(ip.getAsRegularItem(l));
						} else if(type.equals("ARMOR")) {
							listedArmor.add(ip.getAsArmor(l));
						}
					}
				} else {
					io.sendChat("Erro lendo pagina " + i + " - " + res.get("cause").getAsString());
					i--;
					Thread.sleep(1000);
				}
				Thread.sleep(100);
			}
			io.sendChat("Cache de 30 paginas armazenado com sucesso");
		} catch(Exception e) {
			io.sendChat("Erro inesperado lendo cache do leilao");
			e.printStackTrace();
		}
	}
	
	public long cheapestEquivalent(AuctionLog l) {
		String type = hm.classifyItem(l.getItem());
		if(type.equals("PET"))
			return cheapestEquivalent(ip.getAsPet(l));
		else if(type.equals("REGULAR"))
			return cheapestEquivalent(ip.getAsRegularItem(l));
		else if(type.equals("ARMOR"))
			return cheapestEquivalent(ip.getAsArmor(l));
		return -1;
	}
	
	private long cheapestEquivalent(PetData p) {
		PetData cheapest = null;
		for(PetData pet : listedPets) {
			if(p.isEquivalent(pet) && p.getSoldAt() != pet.getSoldAt()) {
				if(cheapest == null || pet.getSellPrice() < cheapest.getSellPrice()) {
					cheapest = pet;
				}
			}
		}
		if(cheapest == null) return -1;
		return cheapest.getSellPrice();
	}
	
	private long cheapestEquivalent(ItemData i) {
		ItemData cheapest = null;
		for(ItemData item : listedRegulars) {
			if(i.equals(item) && i.getSoldAt() != item.getSoldAt()) {
				if(cheapest == null || item.getSellPrice() < cheapest.getSellPrice()) {
					cheapest = item;
				}
			}
		}
		if(cheapest == null) return -1;
		return cheapest.getSellPrice();
	}
	
	private long cheapestEquivalent(ArmorData i) {
		ArmorData cheapest = null;
		for(ArmorData item : listedArmor) {
			if(i.equals(item) && i.getSoldAt() != item.getSoldAt()) {
				if(cheapest == null || item.getSellPrice() < cheapest.getSellPrice()) {
					cheapest = item;
				}
			}
		}
		if(cheapest == null) return -1;
		return cheapest.getSellPrice();
	}
	
}

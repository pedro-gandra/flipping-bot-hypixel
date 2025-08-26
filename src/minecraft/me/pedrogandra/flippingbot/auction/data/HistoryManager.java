package me.pedrogandra.flippingbot.auction.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.api.HypixelApiClient;
import me.pedrogandra.flippingbot.api.util.AuctionDataCache;
import me.pedrogandra.flippingbot.auction.AuctionLog;
import me.pedrogandra.flippingbot.auction.data.categories.ArmorData;
import me.pedrogandra.flippingbot.auction.data.categories.ItemData;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;
import me.pedrogandra.flippingbot.auction.data.utils.CsvExporter;
import me.pedrogandra.flippingbot.auction.data.utils.GeneralParser;
import me.pedrogandra.flippingbot.auction.data.utils.ItemParser;
import me.pedrogandra.flippingbot.utils.MCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class HistoryManager {
	
	private Minecraft mc = Minecraft.getMinecraft();
	private HypixelApiClient api = new HypixelApiClient();
	private AuctionDataCache auctionData = new AuctionDataCache();
	private MCUtils mcu = new MCUtils();
	private ItemParser ip = new ItemParser();
	private GeneralParser gp = new GeneralParser();
	private CsvExporter csv = new CsvExporter();
	private LogCache log = new LogCache();
	
	private static List<PetData> petList = new ArrayList<>();
	private static List<ItemData> regularItemList = new ArrayList<>();
	private static List<ArmorData> armorList = new ArrayList<>();
	private static List<AuctionLog> currentLogPage = new ArrayList<>();
	private static long lastHistoryUpdate = 0;
	private static boolean running = false;
	private String DATA_FOLDER = FlippingBot.DATA_FOLDER;
	
	public static volatile boolean updatingCache = false;
	
	public void updateHistory() {
		new Thread(() -> {
			
			int cleanCount = 20;
			while(running) {
				
				try {
					
					clearLists();
					long timeUpdate = lastHistoryUpdate;
					while (timeUpdate == lastHistoryUpdate) {
						JsonObject res = api.getAuctionEnded();
						if(res == null || res.get("lastUpdated").getAsLong() == lastHistoryUpdate) {
							Thread.sleep(10000);
							continue;
						}
						timeUpdate = res.get("lastUpdated").getAsLong();
						JsonArray json = res.getAsJsonArray("auctions");
						auctionData.updateLog(json);
						currentLogPage = auctionData.getLogList();
						processItems();
						System.out.println("Items processados");
						if(cleanCount >= 20) {
							Thread.sleep(10000);
							updatingCache = true;
							log.cleanAll();
							updatingCache = false;
							System.out.println("Arquivos limpos");
							cleanCount = 0;
						} else
							cleanCount++;
					}
					this.lastHistoryUpdate = timeUpdate;
					Thread.sleep(10000);
				
				} catch(Exception e) {				
					e.printStackTrace();		
				}
				
			}
			
		}).start();
	}
	
	private void processItems() {
		for(AuctionLog log : currentLogPage) {
			String category = classifyItem(log.getItem());
			if(category.equals("PET")) {
				petList.add(ip.getAsPet(log));
			} else if(category.equals("REGULAR")) {
				regularItemList.add(ip.getAsRegularItem(log));
			} else if(category.equals("ARMOR")) {
				ArmorData item = ip.getAsArmor(log);
				if(item!=null)
					armorList.add(item);
			}
		}
		csv.petToCsv(petList, DATA_FOLDER+"pet.csv", true);
		csv.regularItemToCsv(regularItemList, DATA_FOLDER+"regular.csv", true);
		csv.armorToCsv(armorList, DATA_FOLDER+"armor.csv", true);
	}
	
	public String classifyItem(ItemStack item) {
		if(item.stackSize != 1) return "";
		String name = mcu.cleanText(item.getDisplayName());
		List<String> tt = item.getTooltip(mc.thePlayer, false);
		
		String desc = tt.get(gp.lastLineTT(tt));
		
		Pattern pattern = Pattern.compile("\\[Lvl \\d+\\]");
		Matcher matcher = pattern.matcher(name);
		if(matcher.find())
			return "PET";
		
		if(desc.contains("ACCESSORY") || desc.contains("PET ITEM") || desc.contains("COSMETIC"))
			return "REGULAR";
		
		if(desc.contains("CHESTPLATE") || desc.contains("HELMET") || desc.contains("LEGGINGS") || desc.contains("BOOTS"))
			return "ARMOR";
			
		return "";
	}
	
	private void clearLists() {
		petList.clear();
		regularItemList.clear();
		armorList.clear();
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		HistoryManager.running = running;
	}

}

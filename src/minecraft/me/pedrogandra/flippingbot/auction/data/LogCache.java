package me.pedrogandra.flippingbot.auction.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;
import me.pedrogandra.flippingbot.auction.data.utils.CsvExporter;

public class LogCache {

	private static CsvExporter csv = new CsvExporter();
	
	public static List<PetData> petList = new ArrayList<>();
	private static final String dataPath = FlippingBot.DATA_FOLDER;
	
	public static void readPet() {
		petList.clear();
		try (BufferedReader br = new BufferedReader(new FileReader(dataPath+"pet.csv"))) {
            String linha;
            boolean primeiraLinha = true;
            while ((linha = br.readLine()) != null) {
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }
                String[] partes = linha.split(",");
                String name = partes[0];
                int rarity = Integer.parseInt(partes[1]);
                long soldAt = Long.parseLong(partes[2]);
                long price = Long.parseLong(partes[3]);
                int lvl = Integer.parseInt(partes[4]);
                int candy = Integer.parseInt(partes[5]);
                String item = partes[6];
                int itemRarity = Integer.parseInt(partes[7]);
                String skin = partes[8];
                petList.add(new PetData(name, rarity, soldAt, price, lvl, candy, item, itemRarity, skin));
            }
            
            petList.sort((a, b) -> Long.compare(b.getSoldAt(), a.getSoldAt()));

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void cleanPetFile() {
		readPet();
		Map<PetData, Integer> count = new HashMap<>();
		List<PetData> cleanedList = new ArrayList<>();
		for(PetData pet : petList) {
			int qtd = count.getOrDefault(pet, 0);
			if(qtd < 10) {
				cleanedList.add(pet);
				count.put(pet, qtd+1);
			}
		}
		petList = cleanedList;
		if(!cleanedList.isEmpty())
			csv.petToCsv(cleanedList, dataPath+"pet.csv", false);
	}
	
	public static void updateAll() {
		readPet();
	}
	
	public static void cleanAll() {
		cleanPetFile();
	}
	
}

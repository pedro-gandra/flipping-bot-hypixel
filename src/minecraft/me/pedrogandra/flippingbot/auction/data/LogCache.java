package me.pedrogandra.flippingbot.auction.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.pedrogandra.flippingbot.FlippingBot;
import me.pedrogandra.flippingbot.auction.data.categories.ArmorData;
import me.pedrogandra.flippingbot.auction.data.categories.ItemData;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;
import me.pedrogandra.flippingbot.auction.data.utils.CsvExporter;

public class LogCache {

	private static CsvExporter csv = new CsvExporter();
	
	public static List<PetData> petList = new ArrayList<>();
	public static List<ItemData> regularList = new ArrayList<>();
	public static List<ArmorData> armorList = new ArrayList<>();
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
	
	public static void readRegular() {
		regularList.clear();
		try (BufferedReader br = new BufferedReader(new FileReader(dataPath+"regular.csv"))) {
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
                regularList.add(new ItemData(name, rarity, soldAt, price));
            }
            
            regularList.sort((a, b) -> Long.compare(b.getSoldAt(), a.getSoldAt()));

        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void cleanRegularFile() {
		readRegular();
		Map<ItemData, Integer> count = new HashMap<>();
		List<ItemData> cleanedList = new ArrayList<>();
		for(ItemData item : regularList) {
			int qtd = count.getOrDefault(item, 0);
			if(qtd < 10) {
				cleanedList.add(item);
				count.put(item, qtd+1);
			}
		}
		regularList = cleanedList;
		if(!cleanedList.isEmpty())
			csv.regularItemToCsv(cleanedList, dataPath+"regular.csv", false);
	}
	
	public static void readArmor() {
	    armorList.clear();
	    try (BufferedReader br = new BufferedReader(new FileReader(dataPath + "armor.csv"))) {
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
	            long sellPrice = Long.parseLong(partes[3]);
	            String reforge = partes[4];
	            int dungeonStars = Integer.parseInt(partes[5]);
	            int masterStars = Integer.parseInt(partes[6]);
	            int hpb = Integer.parseInt(partes[7]);
	            boolean aop = Boolean.parseBoolean(partes[8]);
	            double averageGem = Double.parseDouble(partes[9]);
	            String dye = partes[10];
	            String skin = partes[11];

	            ArmorData armor = new ArmorData(name, rarity, soldAt, sellPrice,
	                                            reforge, dungeonStars, masterStars, hpb,
	                                            aop, averageGem, dye, skin);

	            Map<String, Integer> enchants = new HashMap<>();
	            enchants.put("growth", Integer.parseInt(partes[12]));
	            enchants.put("hardened mana", Integer.parseInt(partes[13]));
	            enchants.put("hecatomb", Integer.parseInt(partes[14]));
	            enchants.put("last stand", Integer.parseInt(partes[15]));
	            enchants.put("legion", Integer.parseInt(partes[16]));
	            enchants.put("protection", Integer.parseInt(partes[17]));
	            enchants.put("rejuvenate", Integer.parseInt(partes[18]));
	            enchants.put("smarty pants", Integer.parseInt(partes[19]));
	            enchants.put("strong mana", Integer.parseInt(partes[20]));
	            enchants.put("sugar rush", Integer.parseInt(partes[21]));
	            enchants.put("wisdom", Integer.parseInt(partes[22]));

	            armor.setEnchantments(enchants);
	            armorList.add(armor);
	        }

	        armorList.sort((a, b) -> Long.compare(b.getSoldAt(), a.getSoldAt()));

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public static void cleanArmorFile() {
	    readArmor();
	    Map<ArmorData, Integer> count = new HashMap<>();
	    List<ArmorData> cleanedList = new ArrayList<>();
	    for (ArmorData armor : armorList) {
	        int qtd = count.getOrDefault(armor, 0);
	        if (qtd < 10) {
	            cleanedList.add(armor);
	            count.put(armor, qtd + 1);
	        }
	    }
	    armorList = cleanedList;
	    if (!cleanedList.isEmpty())
	        csv.armorToCsv(cleanedList, dataPath + "armor.csv", false);
	}

	
	public static void updateAll() {
		readPet();
		readRegular();
		readArmor();
	}
	
	public static void cleanAll() {
		cleanPetFile();
		cleanRegularFile();
		cleanArmorFile();
	}
	
}

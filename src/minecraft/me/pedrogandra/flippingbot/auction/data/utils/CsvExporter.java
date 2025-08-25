package me.pedrogandra.flippingbot.auction.data.utils;

import java.io.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import me.pedrogandra.flippingbot.auction.data.categories.ArmorData;
import me.pedrogandra.flippingbot.auction.data.categories.ItemData;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;

public class CsvExporter {

	public static void petToCsv(List<PetData> pets, String filePath, boolean append) {
	    boolean fileExists = new File(filePath).exists();

	    try (FileWriter writer = new FileWriter(filePath, append);
	         BufferedWriter bw = new BufferedWriter(writer)) {
	    	
	        if (!fileExists || !append) {
	            bw.write("name,rarity,soldAt,sellPrice,level,petCandy,item,itemRarity,skin");
	            bw.newLine();
	        }

	        for (PetData pet : pets) {
	            String line = String.format("%s,%d,%d,%d,%d,%d,%s,%d,%s",
	                    sanitize(pet.getName()),
	                    pet.getRarity(),
	                    pet.getSoldAt(),
	                    pet.getSellPrice(),
	                    pet.getLevel(),
	                    pet.getPetCandy(),
	                    sanitize(pet.getItem()),
	                    pet.getItemRarity(),
	                    sanitize(pet.getSkin()));
	            bw.write(line);
	            bw.newLine();
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
    
    public static void regularItemToCsv(List<ItemData> items, String filePath, boolean append) {
        boolean fileExists = new File(filePath).exists();

        try (FileWriter writer = new FileWriter(filePath, append);
   	         BufferedWriter bw = new BufferedWriter(writer)) {
   	    	
   	        if (!fileExists || !append) {
   	            bw.write("name,rarity,soldAt,sellPrice");
   	            bw.newLine();
   	        }

            for (ItemData item : items) {
                String line = String.format("%s,%d,%d,%d",
                        sanitize(item.getName()),
                        item.getRarity(),
                        item.getSoldAt(),
                        item.getSellPrice());
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void armorToCsv(List<ArmorData> armorList, String filePath, boolean append) {
        boolean fileExists = new File(filePath).exists();

        Set<String> allEnchantments = armorList.stream()
                .flatMap(a -> a.getEnchantments().keySet().stream())
                .collect(Collectors.toCollection(TreeSet::new));

        try (FileWriter writer = new FileWriter(filePath, append);
             BufferedWriter bw = new BufferedWriter(writer)) {

            if (!fileExists || !append) {
                bw.write("name,rarity,soldAt,sellPrice,reforge,dungeonStars,masterStars,hpb,aop,averageGem,dye,skin");
                for (String enchantment : allEnchantments) {
                    bw.write("," + enchantment);
                }
                bw.newLine();
            }

            for (ArmorData armor : armorList) {
                String line = String.format("%s,%d,%d,%d,%s,%d,%d,%d,%b,%s,%s,%s",
                        sanitize(armor.getName()),
                        armor.getRarity(),
                        armor.getSoldAt(),
                        armor.getSellPrice(),
                        sanitize(armor.getReforge()),
                        armor.getDungeonStars(),
                        armor.getMasterStars(),
                        armor.getHpb(),
                        armor.isAop(),
                        sanitizeNumber(armor.getAverageGem()),
                        sanitize(armor.getDye()),
                        sanitize(armor.getSkin())
                );

                bw.write(line);

                Map<String, Integer> enchMap = armor.getEnchantments();
                for (String enchantment : allEnchantments) {
                    int value = enchMap.getOrDefault(enchantment, 0);
                    bw.write("," + value);
                }

                bw.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String sanitizeNumber(double n) {
    	return String.format(Locale.US, "%.2f", n);
    }

    private static String sanitize(String s) {
        if (s == null || s.equals("") || s.equals(" ")) return "NA";
        return s.replace(",", "").replace("\n", "").trim();
    }
}

package me.pedrogandra.flippingbot.auction.ml.utils;

import java.io.*;
import java.util.List;

import me.pedrogandra.flippingbot.auction.ml.categories.ItemData;
import me.pedrogandra.flippingbot.auction.ml.categories.PetData;

public class CsvExporter {

    public static void petToCsv(List<PetData> pets, String filePath) {
        boolean fileExists = new File(filePath).exists();

        try (FileWriter writer = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(writer)) {

            if (!fileExists) {
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
    
    public static void regularItemToCsv(List<ItemData> items, String filePath) {
        boolean fileExists = new File(filePath).exists();

        try (FileWriter writer = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(writer)) {

            if (!fileExists) {
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

    private static String sanitize(String s) {
        if (s == null) return "NA";
        return s.replace(",", "").replace("\n", "").trim();
    }
}

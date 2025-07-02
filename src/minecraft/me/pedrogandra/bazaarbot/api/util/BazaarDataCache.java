package me.pedrogandra.bazaarbot.api.util;

import com.google.gson.JsonObject;

import me.pedrogandra.bazaarbot.bazaar.BazaarItem;

import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class BazaarDataCache {

    private ArrayList<BazaarItem> itemList = new ArrayList<>();
    private Map<String, String> productIdToDisplayName = new HashMap<>();

    public void updateFromJson(JsonObject productsJson) {
        itemList.clear();

        for (Map.Entry<String, JsonElement> entry : productsJson.entrySet()) {
            String productId = entry.getKey();
            JsonObject product = entry.getValue().getAsJsonObject();
            
            String displayName = getDisplayName(productId);
            if(displayName == null) continue;

            BazaarItem item = new BazaarItem();
            item.setProductId(productId);

            
            JsonObject quick = product.getAsJsonObject("quick_status");
            BazaarItem.QuickStatus status = new BazaarItem.QuickStatus();
            status.setProductId(quick.get("productId").getAsString());
            status.setBuyPrice(quick.get("buyPrice").getAsDouble());
            status.setBuyVolume(quick.get("buyVolume").getAsLong());
            status.setBuyMovingWeek(quick.get("buyMovingWeek").getAsLong());
            status.setBuyOrders(quick.get("buyOrders").getAsInt());
            status.setSellPrice(quick.get("sellPrice").getAsDouble());
            status.setSellVolume(quick.get("sellVolume").getAsLong());
            status.setSellMovingWeek(quick.get("sellMovingWeek").getAsLong());
            status.setSellOrders(quick.get("sellOrders").getAsInt());
            item.setQuickStatus(status);

           
            List<BazaarItem.OrderSummary> sellList = new ArrayList<BazaarItem.OrderSummary>();
            JsonArray sellArray = product.getAsJsonArray("sell_summary");
            for (JsonElement e : sellArray) {
                JsonObject o = e.getAsJsonObject();
                BazaarItem.OrderSummary order = new BazaarItem.OrderSummary();
                order.setAmount(o.get("amount").getAsInt());
                order.setPricePerUnit(o.get("pricePerUnit").getAsDouble());
                order.setOrders(o.get("orders").getAsInt());
                sellList.add(order);
            }
            item.setSellSummary(sellList);

            
            List<BazaarItem.OrderSummary> buyList = new ArrayList<BazaarItem.OrderSummary>();
            JsonArray buyArray = product.getAsJsonArray("buy_summary");
            for (JsonElement e : buyArray) {
                JsonObject o = e.getAsJsonObject();
                BazaarItem.OrderSummary order = new BazaarItem.OrderSummary();
                order.setAmount(o.get("amount").getAsInt());
                order.setPricePerUnit(o.get("pricePerUnit").getAsDouble());
                order.setOrders(o.get("orders").getAsInt());
                buyList.add(order);
            }
            item.setBuySummary(buyList);
            
            item.setDisplayName(getDisplayName(productId));
            
            itemList.add(item);
        }
    }

    public void loadDisplayNamesFromJson(JsonArray itemsArray) {
        productIdToDisplayName.clear();

        for (JsonElement element : itemsArray) {
            JsonObject obj = element.getAsJsonObject();
            String id = obj.get("id").getAsString();
            String name = obj.get("name").getAsString();

            productIdToDisplayName.put(id, name);
        }
    }

    public String getDisplayName(String productId) {
    	String display = productIdToDisplayName.get(productId);
    	StringBuilder str = new StringBuilder();
    	if(display != null)
    		return display;
    	if (productId.startsWith("SHARD_")) {
            display = productId.substring(6);
            String[] parts = display.split("_");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].isEmpty()) continue;
                str.append(capitalize(parts[i].toLowerCase()));
                if (i < parts.length - 1) {
                    str.append(" ");
                }
            }
            return str.toString();
        }
    	return null;
    }

    public ArrayList<BazaarItem> getAllItems() {
        return itemList;
    }
    
    private static String capitalize(String word) {
        if (word.length() == 0) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}

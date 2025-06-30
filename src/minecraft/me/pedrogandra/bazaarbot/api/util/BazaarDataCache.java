package me.pedrogandra.bazaarbot.api.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class BazaarDataCache {

	private Map<String, BazaarItem> itemMap = new HashMap<String, BazaarItem>();

    public void updateFromJson(JsonObject productsJson) {
        itemMap.clear();

        for (Map.Entry<String, JsonElement> entry : productsJson.entrySet()) {
            String productId = entry.getKey();
            JsonObject product = entry.getValue().getAsJsonObject();

            BazaarItem item = new BazaarItem();
            item.setProductId(productId);

            // Quick Status
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

            // Sell Summary
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

            // Buy Summary
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

            itemMap.put(productId, item);
        }
    }

    public BazaarItem getItem(String productId) {
        return itemMap.get(productId);
    }

    public Map<String, BazaarItem> getAllItems() {
        return itemMap;
    }
}

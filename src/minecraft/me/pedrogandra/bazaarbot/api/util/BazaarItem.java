package me.pedrogandra.bazaarbot.api.util;

import java.util.List;

public class BazaarItem {
    private String productId;
    private String displayName;
    private QuickStatus quickStatus;
    private List<OrderSummary> sellSummary;
    private List<OrderSummary> buySummary;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public QuickStatus getQuickStatus() { return quickStatus; }
    public void setQuickStatus(QuickStatus quickStatus) { this.quickStatus = quickStatus; }

    public List<OrderSummary> getSellSummary() { return sellSummary; }
    public void setSellSummary(List<OrderSummary> sellSummary) { this.sellSummary = sellSummary; }

    public List<OrderSummary> getBuySummary() { return buySummary; }
    public void setBuySummary(List<OrderSummary> buySummary) { this.buySummary = buySummary; }
    
    public double getBestBuy() { return buySummary.get(0).getPricePerUnit(); }
    public double getBestSell() { return sellSummary.get(0).getPricePerUnit(); }
    
    public double getHourlyLiquidity () {
    	long liquidity = Math.min(quickStatus.getBuyMovingWeek(), quickStatus.getSellMovingWeek());
		return (liquidity/7/24);
    }

    public static class QuickStatus {
        private String productId;
        private double buyPrice;
        private long buyVolume;
        private long buyMovingWeek;
        private int buyOrders;
        private double sellPrice;
        private long sellVolume;
        private long sellMovingWeek;
        private int sellOrders;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public double getBuyPrice() { return buyPrice; }
        public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }

        public long getBuyVolume() { return buyVolume; }
        public void setBuyVolume(long buyVolume) { this.buyVolume = buyVolume; }

        public long getBuyMovingWeek() { return buyMovingWeek; }
        public void setBuyMovingWeek(long buyMovingWeek) { this.buyMovingWeek = buyMovingWeek; }

        public int getBuyOrders() { return buyOrders; }
        public void setBuyOrders(int buyOrders) { this.buyOrders = buyOrders; }

        public double getSellPrice() { return sellPrice; }
        public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }

        public long getSellVolume() { return sellVolume; }
        public void setSellVolume(long sellVolume) { this.sellVolume = sellVolume; }

        public long getSellMovingWeek() { return sellMovingWeek; }
        public void setSellMovingWeek(long sellMovingWeek) { this.sellMovingWeek = sellMovingWeek; }

        public int getSellOrders() { return sellOrders; }
        public void setSellOrders(int sellOrders) { this.sellOrders = sellOrders; }
    }

    public static class OrderSummary {
        private int amount;
        private double pricePerUnit;
        private int orders;

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }

        public double getPricePerUnit() { return pricePerUnit; }
        public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

        public int getOrders() { return orders; }
        public void setOrders(int orders) { this.orders = orders; }
        
    }
}
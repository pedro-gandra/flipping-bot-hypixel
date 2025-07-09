package me.pedrogandra.bazaarbot.bazaar;

public class BazaarOrder {
	
	public String productName;
	public double currentBuyAmount;
	public double currentSellAmount;
	public double currentInventoryAmount;
	public double currentPurchasePrice;
	public double currentSalePrice;
	public boolean updateBuy;
	
	public BazaarOrder(String name) {
		this.productName = name;
		this.currentBuyAmount = this.currentSellAmount = this.currentInventoryAmount = 0;
		this.updateBuy = false;
	}
	
}

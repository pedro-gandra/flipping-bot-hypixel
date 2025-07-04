package me.pedrogandra.bazaarbot.bazaar;

public class BazaarOrder {
	
	public String productName;
	public double idealAmount;
	public double currentBuyAmount;
	public double currentSellAmount;
	public double currentPurchasePrice;
	public double currentSalePrice;
	
	public BazaarOrder(String name, double amount) {
		this.productName = name;
		this.idealAmount = amount;
		this.currentBuyAmount = this.currentSellAmount = 0;
	}
	
}

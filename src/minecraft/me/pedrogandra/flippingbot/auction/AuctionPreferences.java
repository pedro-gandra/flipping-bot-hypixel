package me.pedrogandra.flippingbot.auction;

public class AuctionPreferences {

	public AuctionPreferences(long minProfit, double minMargin) {
		super();
		this.minProfit = minProfit;
		this.minMargin = minMargin;
	}
	private long minProfit;
	private double minMargin;
	
	public long getMinProfit() {
		return minProfit;
	}
	public void setMinProfit(long minProfit) {
		this.minProfit = minProfit;
	}
	public double getMinMargin() {
		return minMargin;
	}
	public void setMinMargin(double minMargin) {
		this.minMargin = minMargin;
	}
	
	
	
}

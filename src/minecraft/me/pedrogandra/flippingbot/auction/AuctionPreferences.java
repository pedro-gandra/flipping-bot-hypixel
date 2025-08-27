package me.pedrogandra.flippingbot.auction;

public class AuctionPreferences {

	public AuctionPreferences(long minProfit, double minMargin, double multiplier) {
		super();
		this.minProfit = minProfit;
		this.minMargin = minMargin;
		this.multiplier = multiplier;
	}
	private long minProfit;
	private double minMargin;
	private double multiplier;
	
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
	public double getmultiplier() {
		return multiplier;
	}
	public void setmultiplier(double multiplier) {
		this.multiplier = multiplier;
	}
	
	
	
}

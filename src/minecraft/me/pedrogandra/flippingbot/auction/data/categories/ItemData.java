package me.pedrogandra.flippingbot.auction.data.categories;

public class ItemData {

	private String name;
	private int rarity;
	private long soldAt;
	private long sellPrice;
	
	public ItemData(String name, int rarity, long soldAt, long sellPrice) {
		this.name = name;
		this.rarity = rarity;
		this.soldAt = soldAt;
		this.sellPrice = sellPrice;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRarity() {
		return rarity;
	}
	public void setRarity(int rarity) {
		this.rarity = rarity;
	}
	public long getSoldAt() {
		return soldAt;
	}
	public void setSoldAt(long soldAt) {
		this.soldAt = soldAt;
	}
	public long getSellPrice() {
		return sellPrice;
	}
	public void setSellPrice(long sellPrice) {
		this.sellPrice = sellPrice;
	}
	
}

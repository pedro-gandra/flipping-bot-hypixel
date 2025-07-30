package me.pedrogandra.flippingbot.auction;

import net.minecraft.item.ItemStack;

public class AuctionLog {

	private String id;
	private long soldAt;
	private long sellPrice;
	private ItemStack item;
	
	public AuctionLog(String id, long soldAt, long sellPrice, ItemStack item) {
		super();
		this.id = id;
		this.soldAt = soldAt;
		this.sellPrice = sellPrice;
		this.item = item;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}
	
}

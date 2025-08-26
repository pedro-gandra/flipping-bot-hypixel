package me.pedrogandra.flippingbot.auction;

import net.minecraft.item.ItemStack;

public class AuctionFlip {

	public AuctionFlip(String id, long profit, long value) {
		super();
		this.id = id;
		this.profit = profit;
		this.value = value;
	}
	
	private String id;
	private long profit;
	private long value;
	private ItemStack item;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getProfit() {
		return profit;
	}
	public void setProfit(long profit) {
		this.profit = profit;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public ItemStack getItem() {
		return item;
	}
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
}

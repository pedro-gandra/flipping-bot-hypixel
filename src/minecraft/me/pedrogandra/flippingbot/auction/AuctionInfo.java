package me.pedrogandra.flippingbot.auction;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

public class AuctionInfo {
	
	private String name;
	private String rarity;
	private float price;
	private ItemStack item;
	
	public AuctionInfo(String n, String r, float price, ItemStack i) {
		this.name = n;
		this.rarity = r;
		this.price = price;
		this.item = i;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRarity() {
		return rarity;
	}

	public void setRarity(String rarity) {
		this.rarity = rarity;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}
	
}

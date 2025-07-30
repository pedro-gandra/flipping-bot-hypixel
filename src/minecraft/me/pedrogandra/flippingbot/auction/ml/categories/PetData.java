package me.pedrogandra.flippingbot.auction.ml.categories;

public class PetData extends ItemData {
	
	private int level;
	private int petCandy;
	private String item;
	private int itemRarity;
	private String skin;
	
	public PetData(String name, int rarity, long soldAt, long sellPrice, int level, int petCandy, String item, int itemRarity, String skin) {
		super(name, rarity, soldAt, sellPrice);
		this.level = level;
		this.petCandy = petCandy;
		this.item = item;
		this.itemRarity = itemRarity;
		this.skin = skin;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getPetCandy() {
		return petCandy;
	}

	public void setPetCandy(int petCandy) {
		this.petCandy = petCandy;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public int getItemRarity() {
		return itemRarity;
	}

	public void setItemRarity(int itemRarity) {
		this.itemRarity = itemRarity;
	}

	public String getSkin() {
		return skin;
	}

	public void setSkin(String skin) {
		this.skin = skin;
	}

	
}

package me.pedrogandra.flippingbot.auction.ml.categories;

import java.util.Objects;

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

	public boolean isEquivalent(PetData pet) {
		if (this.getName().equals(pet.getName()) && this.getRarity() == pet.getRarity() && this.skin.equals(pet.getSkin())) {
			int lvlDiff = Math.abs(this.level - pet.getLevel());
			if(lvlDiff == 0 || (this.level < 30 && pet.getLevel() < 30) || (this.level < 50 && lvlDiff <= 12) || (this.level < 70 && lvlDiff <= 8) || (this.level < 80 && lvlDiff <= 5) || (this.level < 90 && lvlDiff <= 3) || (this.level < 100 && lvlDiff <= 2)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (!(obj instanceof PetData)) return false;
	    PetData pet = (PetData) obj;

	    return this.getName().equals(pet.getName()) &&
	           this.getRarity() == pet.getRarity() &&
	           this.skin.equals(pet.getSkin()) &&
	           this.level == pet.level;
	}
	
	@Override
	public int hashCode() {
	    return Objects.hash(getName(), getRarity(), skin, level);
	}
	
}

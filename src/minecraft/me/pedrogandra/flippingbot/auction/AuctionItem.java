package me.pedrogandra.flippingbot.auction;

import java.util.ArrayList;

public class AuctionItem {
	
	private String name;
	private String rarity;
	private int level;
	private boolean excludeRecomb;
	private ArrayList<Integer> gearScore = new ArrayList();
	private ArrayList<String> specs = new ArrayList();
	private float price;
	
	public AuctionItem(String n, String r, boolean recomb, int lvl, int gs[], String spc[], float price) {
		this.name = n;
		this.rarity = r;
		this.level = lvl;
		this.excludeRecomb = recomb;
		if(gs!=null && gs.length == 2) {
			setGearScore(gs);
		}
		if(spc!=null) {
			setSpecs(spc);
		}
		this.price = price;
	}
	
	private void setSpecs(String spc[]) {
		for(String s : spc) {
			specs.add(s);
		}
	}
	
	public ArrayList<String> getSpecs() {
		return specs;
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public ArrayList<Integer> getGearScore() {
		return gearScore;
	}

	public void setGearScore(int gs[]) {
		this.gearScore.add(gs[0]);
		this.gearScore.add(gs[1]);
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public boolean isExcludeRecomb() {
		return excludeRecomb;
	}

	public void setExcludeRecomb(boolean excludeRecomb) {
		this.excludeRecomb = excludeRecomb;
	}
	
}

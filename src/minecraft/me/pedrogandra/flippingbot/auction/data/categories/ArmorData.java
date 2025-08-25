package me.pedrogandra.flippingbot.auction.data.categories;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;

public class ArmorData extends ItemData {
	
	private String reforge;
	private int dungeonStars;
	private int masterStars;
	private int hpb;
	private boolean aop;
	private double averageGem;
	private String dye;
	private String skin;
	private Map<String, Integer> enchantments;
		
	public ArmorData(String name, int rarity, long soldAt, long sellPrice, String reforge, int dungeonStars, int masterStars, int hpb, boolean aop, double averageGem, String dye, String skin) {
		super(name, rarity, soldAt, sellPrice);
		this.reforge = reforge;
		this.dungeonStars = dungeonStars;
		this.masterStars = masterStars;
		this.hpb = hpb;
		this.aop = aop;
		this.averageGem = averageGem;
		this.dye = dye;
		this.skin = skin;
		initMap();
	}
	
	private void initMap() {
		enchantments = new HashMap();
		enchantments.put("protection", 0);
		enchantments.put("growth", 0);
		enchantments.put("rejuvenate", 0);
		enchantments.put("strong mana", 0);
		enchantments.put("hardened mana", 0);
		enchantments.put("sugar rush", 0);
		enchantments.put("smarty pants", 0);
		enchantments.put("hecatomb", 0);
		enchantments.put("legion", 0);
		enchantments.put("last stand", 0);
		enchantments.put("wisdom", 0);
	}
	
	@Override
	public String toString() {
	    return "ArmorData{" +
	    		"name ='" + this.getName() + '\'' +
	            "reforge='" + reforge + '\'' +
	            ", dungeonStars=" + dungeonStars +
	            ", masterStars=" + masterStars +
	            ", hpb=" + hpb +
	            ", aop=" + aop +
	            ", averageGem=" + averageGem +
	            ", dye='" + dye + '\'' +
	            ", skin='" + skin + '\'' +
	            ", enchantments=" + enchantments +
	            '}';
	}

	public String getReforge() {
		return reforge;
	}

	public void setReforge(String reforge) {
		this.reforge = reforge;
	}

	public int getDungeonStars() {
		return dungeonStars;
	}

	public void setDungeonStars(int dungeonStars) {
		this.dungeonStars = dungeonStars;
	}

	public int getMasterStars() {
		return masterStars;
	}

	public void setMasterStars(int masterStars) {
		this.masterStars = masterStars;
	}

	public int getHpb() {
		return hpb;
	}

	public void setHpb(int hpb) {
		this.hpb = hpb;
	}

	public boolean isAop() {
		return aop;
	}

	public void setAop(boolean aop) {
		this.aop = aop;
	}

	public double getAverageGem() {
		return averageGem;
	}

	public void setAverageGem(double averageGem) {
		this.averageGem = averageGem;
	}

	public String getDye() {
		return dye;
	}

	public void setDye(String dye) {
		this.dye = dye;
	}

	public String getSkin() {
		return skin;
	}

	public void setSkin(String skin) {
		this.skin = skin;
	}

	public Map<String, Integer> getEnchantments() {
		return enchantments;
	}

	public void setEnchantments(Map<String, Integer> enchantments) {
		this.enchantments = enchantments;
	}
}

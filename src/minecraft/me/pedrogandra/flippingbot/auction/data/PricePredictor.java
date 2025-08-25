package me.pedrogandra.flippingbot.auction.data;

import java.util.ArrayList;
import java.util.List;

import me.pedrogandra.flippingbot.auction.data.categories.ArmorData;
import me.pedrogandra.flippingbot.auction.data.categories.ItemData;
import me.pedrogandra.flippingbot.auction.data.categories.PetData;

public class PricePredictor {
	
	public double pricePet(PetData target) {
		List<PetData> relevantPets = new ArrayList<>();
		long totalPrice = 0;
		int nPets = 0;
		for(PetData pet : LogCache.petList) {
			if(target.isEquivalent(pet)) {
				relevantPets.add(pet);
				nPets++;
			}
			if(nPets >= 10)
				break;
		}
		
		if(nPets < 7)
			return -1;
		
		relevantPets.sort((a, b) -> Long.compare(b.getSellPrice(), a.getSellPrice()));
		int start = 0, end = nPets;
		if(nPets == 10) {
			start += 3;
			end -= 3;
		} else {
			start += 2;
			end -= 2;
		}
		
		for(int i = start; i < end; i++) {
			totalPrice += relevantPets.get(i).getSellPrice();
		}
		
		return (double) totalPrice/(nPets-start*2);
	}
	
	public double priceRegular(ItemData target) {
		List<ItemData> relevantItems = new ArrayList<>();
		long totalPrice = 0;
		int nItems = 0;
		for(ItemData item : LogCache.regularList) {
			if(target.equals(item)) {
				relevantItems.add(item);
				nItems++;
			}
			if(nItems >= 10)
				break;
		}
		
		if(nItems < 7)
			return -1;
		
		relevantItems.sort((a, b) -> Long.compare(b.getSellPrice(), a.getSellPrice()));
		int start = 0, end = nItems;
		if(nItems == 10) {
			start += 3;
			end -= 3;
		} else {
			start += 2;
			end -= 2;
		}
		
		for(int i = start; i < end; i++) {
			totalPrice += relevantItems.get(i).getSellPrice();
		}
		
		return (double) totalPrice/(nItems-start*2);
	}
	
	public double priceArmor(ArmorData target) {
		List<ArmorData> relevantItems = new ArrayList<>();
		long totalPrice = 0;
		int nItems = 0;
		for(ArmorData item : LogCache.armorList) {
			if(target.equals(item)) {
				relevantItems.add(item);
				nItems++;
			}
			if(nItems >= 10)
				break;
		}
		
		if(nItems < 5)
			return -1;
		
		relevantItems.sort((a, b) -> Long.compare(b.getSellPrice(), a.getSellPrice()));
		int start = 0, end = nItems;
		if(nItems == 10) {
			start += 3;
			end -= 3;
		} else if(nItems >= 8) {
			start += 2;
			end -= 2;
		} else {
			start += 1;
			end -= 1;
		}
		
		for(int i = start; i < end; i++) {
			totalPrice += relevantItems.get(i).getSellPrice();
		}
		
		return (double) totalPrice/(nItems-start*2);
	}
	
}

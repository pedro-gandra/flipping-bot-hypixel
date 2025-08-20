package me.pedrogandra.flippingbot.auction.ml;

import java.util.ArrayList;
import java.util.List;

import me.pedrogandra.flippingbot.auction.ml.categories.PetData;

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
		
		if(nPets < 3)
			return -1;
		
		relevantPets.sort((a, b) -> Long.compare(b.getSellPrice(), a.getSellPrice()));
		int start = 0, end = nPets;
		if(nPets >= 8) {
			start += 2;
			end -= 2;
		} else if(nPets>=5) {
			start += 1;
			end -= 1;
		}
		
		for(int i = start; i < end; i++) {
			totalPrice += relevantPets.get(i).getSellPrice();
		}
		
		return (double) totalPrice/(nPets-start*2);
	}
	
}

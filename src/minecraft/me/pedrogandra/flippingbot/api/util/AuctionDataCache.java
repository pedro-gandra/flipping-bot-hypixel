package me.pedrogandra.flippingbot.api.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.nullicorn.nedit.NBTInputStream;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import me.pedrogandra.flippingbot.auction.AuctionInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class AuctionDataCache {

	private static ArrayList<AuctionInfo> itemList = new ArrayList<>();
	
	public void updateFromJson(JsonArray auctionArray) {
		itemList.clear();
		for (JsonElement element : auctionArray) {
	        if (!element.isJsonObject()) continue;
	        JsonObject obj = element.getAsJsonObject();
	        boolean bin = obj.get("bin").getAsBoolean();
	        if(!bin) continue;
	        String name = obj.has("item_name") ? obj.get("item_name").getAsString() : "";
	        String rarity = obj.has("tier") ? obj.get("tier").getAsString() : "";
	        float price = obj.has("starting_bid") ? obj.get("starting_bid").getAsFloat() : 0.0f;
	        String base64 = obj.get("item_bytes").getAsString();
	        ItemStack item = itemStackFromBase64(base64);
            AuctionInfo info = new AuctionInfo(name, rarity, price, item);
	        itemList.add(info);
	    }
	}
	
	private ItemStack itemStackFromBase64(String base64) {
        try {
        	InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        	NBTTagCompound decoded = CompressedStreamTools.readCompressed(inputStream).getTagList("i", 10).getCompoundTagAt(0);
            return ItemStack.loadItemStackFromNBT(decoded);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

	
	public static ArrayList<AuctionInfo> getItemList() {
		return itemList;
	}
	
}

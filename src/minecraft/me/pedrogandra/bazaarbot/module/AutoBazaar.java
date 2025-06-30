package me.pedrogandra.bazaarbot.module;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;

import me.pedrogandra.bazaarbot.BazaarBot;
import me.pedrogandra.bazaarbot.api.HypixelApiClient;
import me.pedrogandra.bazaarbot.api.util.*;
import me.pedrogandra.bazaarbot.gui.GuiIngameHook;
import me.pedrogandra.bazaarbot.utils.KeyboardManager;
import net.minecraft.client.Minecraft;

public class AutoBazaar extends Module {
	
	public static AutoBazaar instance = new AutoBazaar();
	private boolean refreshReady = false;
	private BazaarDataCache bazaarData = new BazaarDataCache();
	private HypixelApiClient api = new HypixelApiClient();
	private ArrayList<BazaarItem> currentItems;
	private int currentIndex = 0;
	
	public AutoBazaar() {
		super("AutoBazaar", Keyboard.KEY_F);
		instance = this;
	}
	
	public void onEnable() {
		refreshReady = true;
	}
	
	public void onDisable() {
		refreshReady = false;
		currentIndex = 0;
		currentItems = null;
	}
	
	public void onUpdate() {
		
		KeyboardManager.update();
		
		if (refreshReady) {
		    refreshReady = false;

		    new Thread(new Runnable() {
		        @Override
		        public void run() {
		            try {
		                JsonObject json = api.getBazaarData();
		                bazaarData.updateFromJson(json);
		                currentItems = new ArrayList<BazaarItem>(bazaarData.getAllItems().values());
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
		    }).start();
		}
		
		if(currentItems != null && !currentItems.isEmpty()) {
			if (KeyboardManager.isKeyJustPressed(Keyboard.KEY_RIGHT)) {
				 currentIndex = (currentIndex + 1) % currentItems.size();
			} else if(KeyboardManager.isKeyJustPressed(Keyboard.KEY_LEFT)) {
				if(currentIndex == 0)
					currentIndex = currentItems.size()-1;
				else
					currentIndex -= 1;
			}
		}
		
		
	}

	public ArrayList<BazaarItem> getCurrentItems() {
		return currentItems;
	}

	public void setCurrentItems(ArrayList<BazaarItem> currentItems) {
		this.currentItems = currentItems;
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}
	
	public BazaarItem getSpecificItem(int i) {
		if (currentItems == null || currentItems.isEmpty()) return null;
		return currentItems.get(i);
	}
	

}

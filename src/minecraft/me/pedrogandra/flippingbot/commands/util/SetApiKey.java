package me.pedrogandra.flippingbot.commands.util;

import java.util.ArrayList;

import me.pedrogandra.flippingbot.api.HypixelApiClient;
import me.pedrogandra.flippingbot.commands.Command;
import me.pedrogandra.flippingbot.commands.tests.TestString;
import me.pedrogandra.flippingbot.utils.IOManager;

public class SetApiKey extends Command {

	private static String key;
	
	public SetApiKey() {
		super("Set API Key", "Sets the API key to be used for this session", ".setkey <str>", ".setkey");
	}
	
	public void execute(String[] args) {
		String str = String.join(" ", args);
		if(str=="") return;
		if(str.equalsIgnoreCase("print")) {
			print();
		} else {
			key = str;
			HypixelApiClient.API_KEY = key;
			IOManager.sendChat("Key saved");
		}
	}
	
	public void print() {
		if(key==null) {
			IOManager.sendError("API key wasn't set");
		} else {
			IOManager.sendChat("Key: " + key);
		}
	}
}

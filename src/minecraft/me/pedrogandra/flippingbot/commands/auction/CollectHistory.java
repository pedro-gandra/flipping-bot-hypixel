package me.pedrogandra.flippingbot.commands.auction;

import java.util.Map.Entry;
import java.util.Set;

import me.pedrogandra.flippingbot.auction.ml.HistoryManager;
import me.pedrogandra.flippingbot.commands.Command;
import me.pedrogandra.flippingbot.module.AutoBIN;
import me.pedrogandra.flippingbot.utils.IOManager;
import net.minecraft.client.Minecraft;

public class CollectHistory extends Command {

	private Minecraft mc = Minecraft.getMinecraft();
	private IOManager io = new IOManager();
	private HistoryManager hm = new HistoryManager();
	
	public CollectHistory() {
		super("Collect Auction History", "Starts, stops and gets information from the HistoryManager", ".history <command>", ".history");
	}
	
	public void execute(String[] args) {
		if(args.length == 0) return;
		if(args[0].equalsIgnoreCase("start")) {
			hm.setRunning(true);
			hm.updateHistory();
			io.sendChat("Data is being collected");
		} else if(args[0].equalsIgnoreCase("stop")) {
			hm.setRunning(false);
			io.sendChat("Data will no longer be collected");
		}
	}
	
}

package me.pedrogandra.flippingbot.commands.auction;

import me.pedrogandra.flippingbot.auction.data.ActiveAuctionCache;
import me.pedrogandra.flippingbot.auction.data.HistoryManager;
import me.pedrogandra.flippingbot.commands.Command;
import me.pedrogandra.flippingbot.utils.IOManager;
import net.minecraft.client.Minecraft;

public class StoreActive extends Command {

	private Minecraft mc = Minecraft.getMinecraft();
	private IOManager io = new IOManager();
	private HistoryManager hm = new HistoryManager();
	private ActiveAuctionCache aac = new ActiveAuctionCache();
	
	public StoreActive() {
		super("Collect current active auctions", "Starts a thread that captures information from pages 0 to 29 of current active auctions", ".active <command>", ".active");
	}
	
	public void execute(String[] args) {
		if(args.length == 0) return;
		if(args[0].equalsIgnoreCase("start")) {
			new Thread(() -> {
				aac.updateCache();
			}).start();
		}
	}
	
}

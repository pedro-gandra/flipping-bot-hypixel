package me.pedrogandra.flippingbot.commands.auction;

import java.util.ArrayList;

import me.pedrogandra.flippingbot.auction.AuctionItem;
import me.pedrogandra.flippingbot.commands.Command;
import me.pedrogandra.flippingbot.gui.GuiAuctionForm;
import me.pedrogandra.flippingbot.module.AutoBIN;
import me.pedrogandra.flippingbot.utils.IOManager;
import net.minecraft.client.Minecraft;

public class ItemList extends Command {
	
	private Minecraft mc = Minecraft.getMinecraft();
	private IOManager io = new IOManager();
	
	public ItemList() {
		super("Item List", "Adds, edits or removes items on the auction item list", ".itemlist <command> <index>", ".itemlist");
	}
	
	public void execute(String[] args) {
		if(args.length == 0) return;
		if(args[0].equalsIgnoreCase("add")) {
			mc.displayGuiScreen(new GuiAuctionForm());
		} else if(args[0].equalsIgnoreCase("edit")) {
			int index = Integer.parseInt(args[1]);
			if(index < 0 || index >= AutoBIN.itemList.size()) {
				io.sendError("Not a valid id");
				return;
			}
			mc.displayGuiScreen(new GuiAuctionForm(index));
		} else if(args[0].equalsIgnoreCase("remove")) {
			int index = Integer.parseInt(args[1]);
			if(index < 0 || index >= AutoBIN.itemList.size()) {
				io.sendError("Not a valid id");
				return;
			}
			AutoBIN.itemList.remove(index);
			io.sendChat("Item removed");
			AutoBIN.instance.saveItemList();
		} else if(args[0].equalsIgnoreCase("removeall")) {
			AutoBIN.itemList.clear();
			io.sendChat("List cleared");
			AutoBIN.instance.saveItemList();
		}
	}
	
}
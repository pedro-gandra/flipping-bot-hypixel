package me.pedrogandra.flippingbot.commands;

import java.util.ArrayList;
import java.util.function.Consumer;

import me.pedrogandra.flippingbot.commands.auction.CollectHistory;
import me.pedrogandra.flippingbot.commands.tests.TestString;
import me.pedrogandra.flippingbot.commands.util.SetApiKey;
import me.pedrogandra.flippingbot.module.Module;
import me.pedrogandra.flippingbot.utils.IOManager;
import net.minecraft.client.gui.GuiChat;

public class CommandManager {
	
	private static ArrayList<Command> commands;
	
	public CommandManager() {
	    if (commands == null) {
	        commands = new ArrayList<Command>();
	        newCommand(new TestString());
	        newCommand(new SetApiKey());
	        newCommand(new CollectHistory());
	    }
	}
	
	public static void newCommand(Command c) {
		commands.add(c);
	}
	
	public static boolean handleCommand(String msg) {
        if (!msg.startsWith(".")) return false;

        String[] args = msg.split(" ");
        String cmd = args[0].toLowerCase();

        for(Command c : commands) {
        	String call = c.getCall();
        	if(cmd.equalsIgnoreCase(call)) {
        		String[] params = msg.substring(call.length()).trim().split("\\s+");
        		c.execute(params);
        		return true;
        	}
        }
        
        IOManager.sendError("Comando desconhecido");
        return true;
	}
	
}

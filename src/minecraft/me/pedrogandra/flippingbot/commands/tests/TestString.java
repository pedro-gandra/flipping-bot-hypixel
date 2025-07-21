package me.pedrogandra.flippingbot.commands.tests;

import java.util.ArrayList;
import java.util.List;

import me.pedrogandra.flippingbot.commands.Command;
import me.pedrogandra.flippingbot.utils.IOManager;

public class TestString extends Command {
	
	private static ArrayList<String> strs = new ArrayList<String>();
	
	public TestString() {
		super("Test String", "Captures and stores a string for future testing", ".teststring <str>", ".teststring");
	}
	
	public void execute(String[] args) {
		String str = String.join(" ", args);
		if(str=="") return;
		if(str.equalsIgnoreCase("print")) {
			printList();
		} else if(str.equalsIgnoreCase("clear")) {
			strs.clear();
			IOManager.sendChat("Lista reiniciada");
		} else {
			strs.add(str);
			IOManager.sendChat("String adicionada");
		}
	}

	public ArrayList<String> getStrs() {
		return strs;
	}

	public void setStrs(ArrayList<String> strs) {
		this.strs = strs;
	}
	
	public void printList() {
		if(strs.isEmpty()) {
			IOManager.sendError("Lista de teste vazia");
		} else {
			for(String s: strs) {
				IOManager.sendChat(s);
			}
		}
	}
	
}

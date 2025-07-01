package me.pedrogandra.bazaarbot.commands;

import java.util.ArrayList;

public abstract class Command {
	
	protected String name;
	protected String desc;
	protected String syntax;
	protected String call;
	
	public Command(String nm, String d, String s, String c) {
		name = nm;
		desc = d;
		syntax = s;
		call = c;
	}
	
	public void execute(String[] args) {}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getSyntax() {
		return syntax;
	}

	public void setSyntax(String syntax) {
		this.syntax = syntax;
	}

	public String getCall() {
		return call;
	}

	public void setCall(String call) {
		this.call = call;
	}
	
}

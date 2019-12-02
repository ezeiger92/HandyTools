package com.chromaclypse.handytools.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
import org.bukkit.command.CommandSender;

public class Spectate implements TabExecutor {
	private final SavedLocation locations;
	
	public Spectate(SavedLocation locations) {
		this.locations = locations;
	}

	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return null;
	}

}

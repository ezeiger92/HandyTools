package com.chromaclypse.handytools.command;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.chromaclypse.api.command.CommandBase;
import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.item.ItemBuilder;
import com.chromaclypse.handytools.ToolPlugin;
import com.chromaclypse.handytools.Util;

public class Utilities {
	private static final String RESET = "&r";
	private PlayerState storage;
	
	public Utilities(PlayerState storage) {
		this.storage = storage;
	}
	
	public TabExecutor getCommand() {
		return new CommandBase()
				.with().arg("name").calls(this::name)
				.with().arg("lore").calls(this::lore)
				.with().arg("type").calls(this::type)
				.with().arg("javaver").calls(this::javaver)
				.with().arg("mendmode")
					.option(CommandBase::onlinePlayers).arg("old|new").calls(this::mendmode)
				.getCommand();
	}
	
	public boolean name(Context context) {
		String name = context.SplatArgs(1);
		ItemBuilder builder = ItemBuilder.edit(context.GetHeld());
		
		if(!name.equals(RESET)) {
			builder.display(name);
		}
		else {
			builder.display(null);
		}
		
		return true;
	}
	
	public boolean lore(Context context) {
		String lore = context.SplatArgs(1);
		ItemBuilder builder = ItemBuilder.edit(context.GetHeld());
		
		if(!lore.equals(RESET)) {
			builder.wrapLore(lore);
		}
		else {
			builder.directLore(null);
		}
		
		return true;
	}
	
	public boolean type(Context context) {
		Material mat = context.GetMaterial(1);
		ItemBuilder builder = ItemBuilder.edit(context.GetHeld());
		
		builder.type(mat);
		
		return true;
	}
	
	public boolean javaver(Context context) {
		context.Sender().sendMessage("Java version: " + Runtime.class.getPackage().getImplementationVersion());
		return true;
	}
	
	public boolean mendmode(Context context) {
		String playerName = context.GetArg(1);
		String mendMode = context.GetArg(2);
		
		if(!"new".equalsIgnoreCase(mendMode)) {
			mendMode = "old";
		}
		
		List<Player> matches = Util.getPlayerMatch(playerName);
		PlayerState.State state = new PlayerState.State();
		state.mending_mode = mendMode;
		
		switch(matches.size()) {
		case 0:
			throw new IllegalArgumentException("Expected an online player name, but was given: \"" + context.GetArg(1) + "\"");
		case 1:
			Player p = matches.get(0);
			if("old".equalsIgnoreCase(mendMode)) {
				storage.players.remove(p.getUniqueId().toString());
			}
			else {
				storage.players.put(p.getUniqueId().toString(), state);
			}
			storage.save(ToolPlugin.instance);
			context.Sender().sendMessage("Stored mendMode="+mendMode+" for "+p.getName());
			break;
		default:
			throw new IllegalArgumentException("Multiple players matched! " +
					String.join(", ", matches.stream().map(Player::getName).collect(Collectors.toList())));
		}
		return true;
	}
}

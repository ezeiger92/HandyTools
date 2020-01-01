package com.chromaclypse.handytools.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.command.Context;
import com.chromaclypse.handytools.ToolPlugin;
import com.chromaclypse.handytools.command.SavedLocation.State;

public class Moderator {
	private final SavedLocation locations;
	
	public Moderator(SavedLocation locations) {
		this.locations = locations;
	}
	
	public boolean spectateOff(Context context) {
		Player sender = context.Player();
		String uuid = sender.getUniqueId().toString();
		State data = locations.player_locations.remove(uuid);
		
		if(data != null) {
			sender.teleport(data.loc);
			sender.setGameMode(GameMode.valueOf(data.mode.toUpperCase()));
			locations.save(ToolPlugin.instance);
			sender.sendMessage("Restored previous location");
		}
		else {
			sender.sendMessage("You weren't using spectate");
		}
		return true;
	}
	
	public boolean spectateOn(Context context) {
		Player sender = context.Player();
		storeLocation(sender);
		sender.setGameMode(GameMode.SPECTATOR);
		return true;
	}
	
	public boolean spectatePlayer(Context context) {
		Player sender = context.Player();
		String arg = context.GetArg(0).toLowerCase();
		
		List<Player> matches = new ArrayList<>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			String part = p.getName().toLowerCase();
			
			if(part.equals(arg)) {
				matches = Defaults.list(p);
				break;
			}
			if(part.startsWith(arg)) {
				matches.add(p);
			}
		}
		
		switch(matches.size()) {
		case 0:
			throw new IllegalArgumentException("Expected an online player name, \"on\", or \"off\", but was given: \"" + context.GetArg(1) + "\"");
		case 1:
		{
			storeLocation(sender);
			sender.setGameMode(GameMode.SPECTATOR);
			sender.teleport(matches.get(0));
			sender.setGameMode(GameMode.SPECTATOR);
			break;
		}
		default:
			throw new IllegalArgumentException("Multiple players matched! " +
						String.join(", ", matches.stream().map(Player::getName).collect(Collectors.toList())));
		}
		
		return true;
	}

	private void storeLocation(Player sender) {
		String uuid = sender.getUniqueId().toString();
		if(!locations.player_locations.containsKey(uuid)) {
			State data = new State();
			data.loc = sender.getLocation();
			data.mode = sender.getGameMode().toString();
			locations.player_locations.put(uuid, data);
			locations.save(ToolPlugin.instance);
			sender.sendMessage("Now spectating");
		}
	}
}

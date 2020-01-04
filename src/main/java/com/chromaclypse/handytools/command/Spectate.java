package com.chromaclypse.handytools.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.messages.Text;
import com.chromaclypse.handytools.ToolPlugin;
import com.chromaclypse.handytools.command.SavedLocation.State;

public class Spectate implements Listener {
	private final SavedLocation locations;
	
	public Spectate(SavedLocation locations) {
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
		
		if(!canSpectateWorld(sender, sender.getWorld())) {
			throw new IllegalArgumentException("You are not allowed to spectate players in that world (" + sender.getWorld().getName() + ")");
		}
		storeLocation(sender);
		sender.setGameMode(GameMode.SPECTATOR);
		return true;
	}
	
	private boolean canSpectateWorld(Player source, World target) {
		String permissionBase = "handytools.spectate.world.";
		return !locations.per_world_permissions
				|| source.hasPermission(permissionBase + "*")
				|| source.hasPermission(permissionBase + target.getName().toLowerCase());
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
			Player target = matches.get(0);
			
			if(!canSpectateWorld(sender, target.getWorld())) {
				throw new IllegalArgumentException("You are not allowed to spectate players in that world (" + target.getWorld().getName() + ")");
			}
			
			storeLocation(sender);
			sender.setGameMode(GameMode.SPECTATOR);
			sender.teleport(target);
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
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		if(event.getTo() == null) {
			return;
		}
		
		Player player = event.getPlayer();
		if(!locations.player_locations.containsKey(player.getUniqueId().toString())) {
			return;
		}
		
		World world = event.getTo().getWorld();
		if(canSpectateWorld(player, world)) {
			return;
		}
		
		player.sendMessage(Text.format().colorize("&cError: You are not allowed to spectate players in that world (" + world.getName() + ")"));
		event.setCancelled(true);
	}
}

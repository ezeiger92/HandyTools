package com.chromaclypse.handytools.command;

import java.util.Map;

import org.bukkit.Location;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path="locations.yml")
public class SavedLocation extends ConfigObject {
	public boolean per_world_permissions = false;
	public Map<String, State> player_locations = Defaults.emptyMap();
	public static class State {
		public Location loc;
		public String mode;
	}
}

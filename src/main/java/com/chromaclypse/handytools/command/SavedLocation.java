package com.chromaclypse.handytools.command;

import java.util.Map;

import org.bukkit.Location;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path="")
public class SavedLocation extends ConfigObject {
	public Map<String, Location> player_locations = Defaults.emptyMap();
}

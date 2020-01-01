package com.chromaclypse.handytools.command;

import java.util.Map;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path="playerstate.yml")
public class PlayerState extends ConfigObject {
	public Map<String, State> players = Defaults.emptyMap();
	
	public static class State {
		public String mending_mode = "old";
	}
	
}

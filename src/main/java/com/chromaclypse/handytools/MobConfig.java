package com.chromaclypse.handytools;

import java.util.Map;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;
import com.chromaclypse.api.config.Section;

@Section(path="mobs.yml")
public class MobConfig extends ConfigObject {

	public Map<String, String> potion_disk_map = Defaults.emptyMap();
}

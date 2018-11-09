package com.chromaclypse.handytools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.Defaults;
import com.chromaclypse.api.config.ConfigObject;

public class ToolConfig extends ConfigObject {
	public WoodTools wood_tools = new WoodTools();
	public static class WoodTools {
		public double pickaxe_cobble_chance = 0.5;
		public double shovel_sapling_chance = 0.18;
		public double hoe_seed_chance = 0.28;
		public ItemStack axe_item = null;
		public boolean sword_affects_players = false;
		public int sword_slow_duration = 20 * 18;
	}
	
	public StoneTools stone_tools = new StoneTools();
	public static class StoneTools {
		public int pickaxe_min_nuggets = 1;
		public int pickaxe_max_nuggets = 5;
	}
	
	public GoldTools gold_tools = new GoldTools();
	public static class GoldTools {
		public int pickaxe_depth = 1;
		public int pickaxe_width = 3;
		public int pickaxe_height = 3;

		public int pickaxe_tpc = 0;
		public int pickaxe_bpc = 9;
		
		public int spade_depth = 1;
		public int spade_width = 3;
		public int spade_height = 3;

		public int spade_tpc = 0;
		public int spade_bpc = 9;
		
		public boolean axe_decapitator = false;
		
		public HashMap<String, HeadData> heads = new HashMap<>();
		public static class HeadData {
			public double chance = 0.5;
			
			public ArrayList<HeadEntry> entries = new ArrayList<>();
			public static class HeadEntry {
				public ItemStack item = new ItemStack(Material.PLAYER_HEAD);
				public double weight = 1.0;
			}
			
			public String variant_func = "";
			public String variant_fallback = "";
			public HashMap<String, HeadData> variants = new HashMap<>();
		}
	}
	
	public LeatherArmor leather_armor = new LeatherArmor();
	public static class LeatherArmor {
		public boolean enabled = true;
		
		public int helmet_evasion_ticks = 5;
		public int chestplate_evasion_ticks = 10;
		public int legging_evasion_ticks = 7;
		public int boot_evasion_ticks = 4;
	}
	
	public ChainArmor chain_armor = new ChainArmor();
	public static class ChainArmor {
		public boolean enabled = true;
		
		public double helmet_miss_reduction = 0.1;
		public double chestplate_miss_reduction = 0.3;
		public double legging_miss_reduction = 0.2;
		public double boot_miss_reduction = 0.1;
	}
	
	public GoldArmor gold_armor = new GoldArmor();
	public static class GoldArmor {
		public boolean enabled = true;
		
		public int helmet_light_damage = 5;
		public int chestplate_light_damage = 10;
		public int legging_light_damage = 7;
		public int boot_light_damage = 4;
		
		public int health_cost = 20;
	}
	
	public CauldronConfig caudron_recipes = new CauldronConfig();
	public static class CauldronConfig {
		public List<String> world_blacklist = Defaults.emptyList();
	}
}

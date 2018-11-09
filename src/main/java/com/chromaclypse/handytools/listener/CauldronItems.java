package com.chromaclypse.handytools.listener;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.handytools.CauldronRecipe;
import com.chromaclypse.handytools.ToolConfig.CauldronConfig;

public class CauldronItems implements Listener {
	
	private final Map<Material, CauldronRecipe> recipes;
	private CauldronConfig config;
	
	private final void addRecipe(Material input, int minWater, int maxWater, Material output, int waterDelta) {
		recipes.put(input, new CauldronRecipe(input, minWater, maxWater, output, waterDelta));
	}
	
	public CauldronItems(CauldronConfig config) {
		recipes = new EnumMap<>(Material.class);
		this.config = config;

		addRecipe(Material.STICKY_PISTON, 1, 3, Material.PISTON, -1);
		addRecipe(Material.WET_SPONGE, 0, 2, Material.SPONGE, 3);
		
		String[] colors = {
				"ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY",
				"LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"
		};
		
		for(String color : colors) {
			addRecipe(Material.matchMaterial(color + "_WOOL"), 1, 3, Material.WHITE_WOOL, -1);
			addRecipe(Material.matchMaterial(color + "_BED"), 1, 3, Material.WHITE_BED, -1);
			addRecipe(Material.matchMaterial(color + "_CONCRETE_POWDER"), 3, 3, Material.matchMaterial(color + "_CONCRETE"), -3);
		}
		addRecipe(Material.WHITE_CONCRETE_POWDER, 3, 3, Material.WHITE_CONCRETE, -3);
	}
	
	@EventHandler
	public void onCauldron(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		
		if(event.hasItem() &&
				event.getAction() == Action.RIGHT_CLICK_BLOCK &&
				!player.isSneaking() &&
				event.getClickedBlock().getType() == Material.CAULDRON) {
			
			String worldName = event.getClickedBlock().getLocation().getWorld().getName();
			if(config.world_blacklist.contains(worldName)) {
				return;
			}
			
			ItemStack item = event.getItem();
			Levelled cauldron = (Levelled) event.getClickedBlock().getBlockData();
			
			int level = cauldron.getLevel();
			
			CauldronRecipe recipe = recipes.get(item.getType());
			
			if(recipe != null && recipe.isAcceptableLevel(level)) {
				recipe.transform(cauldron, item, player);
				event.getClickedBlock().setBlockData(cauldron);
				event.setUseItemInHand(Result.ALLOW);
			}
		}
	}
}

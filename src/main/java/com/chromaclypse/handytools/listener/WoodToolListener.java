package com.chromaclypse.handytools.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.chromaclypse.handytools.DummyBreakEvent;
import com.chromaclypse.handytools.ToolConfig.WoodTools;
import com.chromaclypse.handytools.ToolPlugin;
import com.chromaclypse.handytools.Util;

public class WoodToolListener implements Listener {
	private WoodTools config;
	
	public WoodToolListener(WoodTools config) {
		this.config = config;
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onDamage(EntityDamageByEntityEvent event) {
		if(!(event.getDamager() instanceof Player))
			return;
		
		ItemStack hand = ((Player)event.getDamager()).getInventory().getItemInMainHand();

		if(hand == null || hand.getType() != Material.WOODEN_SWORD)
			return;
		
		if(!(event.getEntity() instanceof LivingEntity))
			return;
		
		if(event.getEntity() instanceof Player && !config.sword_affects_players)
			return;
		
		((LivingEntity)event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 
				config.sword_slow_duration, 0));
	}
	
	// Highest is second-to-last in order of execution
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.getClass().equals(BlockBreakEvent.class))
			return;
		
		if(!event.isDropItems() || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		
		Material type = event.getBlock().getType();
		Material hand = event.getPlayer().getInventory().getItemInMainHand().getType();

		Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
		
		// Check wooden shovel vs grass
		if(type == Material.GRASS_BLOCK && hand == Material.WOODEN_SHOVEL) {
			if(Util.rng(config.shovel_sapling_chance)) {
				loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.OAK_SAPLING));
			}
		}
		// Check wooden pickaxe vs stone
		else if(type == Material.STONE && hand == Material.WOODEN_PICKAXE) {
			if(Util.rng(config.pickaxe_cobble_chance)) {
				loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.COBBLESTONE));
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event) {
		if(!event.hasBlock() || event.getHand() != EquipmentSlot.HAND || event.getItem() == null)
			return;
		
		Material type = event.getClickedBlock().getType();
		Material hand = event.getItem().getType();
		
		if(type == Material.GRASS && hand == Material.WOODEN_HOE) {
			if(new DummyBreakEvent(event.getClickedBlock(), event.getPlayer()).sendEvent()) {
				if(Util.rng(config.hoe_seed_chance)) {
					Location l = event.getClickedBlock().getLocation().add(0.5, 1.5, 0.5);
					l.getWorld().dropItemNaturally(l, new ItemStack(Material.WHEAT_SEEDS));
				}
			}
		}
		else if(hand == Material.WOODEN_AXE && event.getAction() == Action.LEFT_CLICK_BLOCK) {
			ItemStack item = event.getItem();
			
			GameMode oldMode = event.getPlayer().getGameMode();
			ItemMeta oldMeta = item.hasItemMeta() ? item.getItemMeta() : null;
			ItemMeta newMeta = config.axe_item.getItemMeta();
			
			if(type == Material.CRAFTING_TABLE) {
				if(oldMeta == null || !oldMeta.serialize().equals(newMeta.serialize())) {
					event.getPlayer().setGameMode(GameMode.ADVENTURE);
					item.setItemMeta(config.axe_item.getItemMeta());
					Bukkit.getScheduler().runTaskLater(ToolPlugin.instance, () -> {
						item.setItemMeta(oldMeta);
						
						event.getPlayer().updateInventory();
						event.getPlayer().setGameMode(oldMode);
					}, 1);
				}
			}
		}
	}
}

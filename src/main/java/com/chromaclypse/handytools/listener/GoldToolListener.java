package com.chromaclypse.handytools.listener;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.Log;
import com.chromaclypse.handytools.AOE;
import com.chromaclypse.handytools.Util;
import com.chromaclypse.handytools.ToolConfig.GoldTools;
import com.chromaclypse.handytools.ToolConfig.GoldTools.HeadData;
import com.chromaclypse.handytools.ToolConfig.GoldTools.HeadData.HeadEntry;

public class GoldToolListener implements Listener {
	private GoldTools config;
	
	public GoldToolListener(GoldTools config) {
		this.config = config;
	}
	
	@EventHandler
	public void onMend(PlayerItemMendEvent event) {
		Material tool = event.getItem().getType();
		
		if(tool == Material.GOLD_AXE
				|| tool == Material.GOLD_PICKAXE
				|| tool == Material.GOLD_SPADE
				|| tool == Material.GOLD_HOE
				|| tool == Material.GOLD_SWORD)
			event.setCancelled(true);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.getClass().equals(BlockBreakEvent.class))
			return;
		
		if(!event.isDropItems() || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		
		Material type = event.getBlock().getType();
		Material hand = event.getPlayer().getInventory().getItemInMainHand().getType();
		
		// When all blocks have their own ids (namely stone/granite/...) this won't be needed
		byte data = event.getBlock().getData();
		
		// Check golden pickaxe vs stone
		if(type == Material.STONE && hand == Material.GOLD_PICKAXE) {
			if(data == 0 || (data & 1) == 1)
				AOE.dig(event.getBlock(), event.getPlayer(),
						config.pickaxe_depth,
						config.pickaxe_width,
						config.pickaxe_height,
						config.pickaxe_tpc,
						config.pickaxe_bpc);
		}
		
		else if(hand == Material.GOLD_SPADE) {
			if(type == Material.GRASS ||
				type == Material.DIRT ||
				type == Material.SAND ||
				type == Material.GRAVEL ||
				type == Material.SOUL_SAND ||
				type == Material.MYCEL)
					AOE.dig(event.getBlock(), event.getPlayer(),
							config.spade_depth,
							config.spade_width,
							config.spade_height,
							config.spade_tpc,
							config.spade_bpc);
		}
		
		else if(hand == Material.GOLD_AXE) {
			if(type == Material.LOG ||
				type == Material.LOG_2)
				AOE.dig(event.getBlock(), event.getPlayer(), 9, 1, 1, 0, 9, Util.makeBasis(0, -90));
		}
	}
	
	private HashSet<UUID> itemDamageExempt = new HashSet<>();
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onItemDamage(PlayerItemDamageEvent event) {
		if(itemDamageExempt.remove(event.getPlayer().getUniqueId()))
			event.setDamage(0);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if(event.getCause() == DamageCause.ENTITY_ATTACK &&
				event.getDamager() instanceof Player &&
				event.getEntity() instanceof LivingEntity) {
			Player damager = (Player) event.getDamager();
			LivingEntity entity = (LivingEntity) event.getEntity();
			
			if(damager.getInventory().getItemInMainHand().getType() == Material.GOLD_SWORD && 
					entity.getHealth() > event.getFinalDamage())
				itemDamageExempt.add(damager.getUniqueId());
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onKill(EntityDeathEvent event) {
		Player killer = event.getEntity().getKiller();
		
		if(killer != null) {
			Material hand = killer.getInventory().getItemInMainHand().getType();
		
			if(hand == Material.GOLD_AXE && config.axe_decapitator) {
				ItemStack head = null;
				
				if(event.getEntity() instanceof Player) {
					
				}
				else {
					;//Log.info("Mob: " + event.getEntityType().name());
					HeadData data = config.heads.get(event.getEntityType().name());

					;//Log.info("data " + (data != null ? "found" : "not found"));
					
					if(data != null) {
						if(data.variant_func == null || data.variant_func.isEmpty())
							;//Log.info("No variant func");
							
						while(data.variant_func != null && !data.variant_func.isEmpty()) {
							;//Log.info("Variant func: " + data.variant_func);
							String key = null;
							try {
								key = String.valueOf(event.getEntity().getClass().getMethod(data.variant_func).invoke(event.getEntity()));

								;//Log.info("Identified varient: " + key);
							}
							catch(Exception e) {
								;//Log.info("Failed to identify varient");
							}
							
							if(key != null) {
								HeadData newData = data.variants.get(key);

								;//Log.info("variant " + (newData != null ? "found" : "not found"));
								
								if(newData == null && (data.variant_fallback != null || data.variant_fallback.length() > 0))
									newData = data.variants.get(data.variant_fallback);
								
								if(newData != null) {
									data = newData;
									continue;
								}
							}
							
							break;
						}
						
						if(Math.random() < data.chance) {
							double total = 0;
							for(HeadEntry entry : data.entries)
								total += entry.weight;
							
							double target = Math.random() * total;

							for(HeadEntry entry : data.entries) {
								target -= entry.weight;
								
								if(target <= 0) {
									head = entry.item;
									break;
								}
							}
						}
					}
				}
				
				if(head != null)
					event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), head);
			}
		}
	}
}

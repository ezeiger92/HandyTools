package com.chromaclypse.handytools.listener;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.handytools.Util;
import com.chromaclypse.handytools.ToolConfig.GoldArmor;

public class GoldArmorListener implements Listener {
	private GoldArmor config;
	
	public GoldArmorListener(GoldArmor config) {
		this.config = config;
	}

	private HashSet<UUID> helmetDamageExempt = new HashSet<>();
	private HashSet<UUID> chestplateDamageExempt = new HashSet<>();
	private HashSet<UUID> leggingDamageExempt = new HashSet<>();
	private HashSet<UUID> bootDamageExempt = new HashSet<>();
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onItemDamage(PlayerItemDamageEvent event) {
		UUID key = event.getPlayer().getUniqueId();
		Material itemType = event.getItem().getType();
		
		if(itemType == Material.GOLDEN_HELMET && helmetDamageExempt.remove(key)) {
			event.setDamage(0);
		}
		else if(itemType == Material.GOLDEN_CHESTPLATE && chestplateDamageExempt.remove(key)) {
			event.setDamage(0);
		}
		else if(itemType == Material.GOLDEN_LEGGINGS && leggingDamageExempt.remove(key)) {
			event.setDamage(0);
		}
		else if(itemType == Material.GOLDEN_BOOTS && bootDamageExempt.remove(key)) {
			event.setDamage(0);
		}
	}
	
	private EnumSet<Material> goldArmor = EnumSet.of(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS);

	private int durabilityOf(ItemStack stack) {
		return stack == null ? 0 : stack.getType().getMaxDurability() - Util.getDamage(stack);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageEvent event) {
		if(config.enabled && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			int lightDamage = 0;

			boolean helmet = false;
			boolean chestplate = false;
			boolean leggings = false;
			boolean boots = false;
			
			if(Util.helmetIs(player, Material.GOLDEN_HELMET)) {
				lightDamage += config.helmet_light_damage;
				helmet = true;
			}
			
			if(Util.chestplateIs(player, Material.GOLDEN_CHESTPLATE)) {
				lightDamage += config.chestplate_light_damage;
				chestplate = true;
			}
			
			if(Util.leggingIs(player, Material.GOLDEN_LEGGINGS)) {
				lightDamage += config.legging_light_damage;
				leggings = true;
			}
			
			if(Util.bootIs(player, Material.GOLDEN_BOOTS)) {
				lightDamage += config.boot_light_damage;
				boots = true;
			}
			
			if(lightDamage > 0 && event.getFinalDamage() >= player.getHealth()) {
				int cost = (int)(event.getFinalDamage() - player.getHealth() + 1) * config.health_cost;
				
				ItemStack[] armor = player.getInventory().getArmorContents();
				
				int totalDurability = 0;
				for(int i = 0; i < 4; ++i)
					if(armor[i] != null && !goldArmor.contains(armor[i].getType()))
						armor[i] = null;
					else
						totalDurability += durabilityOf(armor[i]);
				
				// Too much damage
				if(totalDurability < cost) {
					return;
				}

				int[] costs = {0, 0, 0, 0};
				int accum = 0;
				
				for(int i = 1; i < 4; ++i)
					accum += (costs[i] = (int)Math.round(cost * durabilityOf(armor[i]) / (double)totalDurability));
				
				costs[0] = cost - accum;
				
				for(int i = 0; i < 4; ++i)
					if(armor[i] != null) {
						Util.setDamage(armor[i], Math.min(armor[i].getType().getMaxDurability(), Util.getDamage(armor[i]) + costs[i]));
					}
				
				player.setHealth(1.0);
				event.setDamage(0);
			}
			
			if(event.getDamage() <= lightDamage) {
				if(helmet)
					helmetDamageExempt.add(player.getUniqueId());
				
				if(chestplate)
					chestplateDamageExempt.add(player.getUniqueId());
				
				if(leggings)
					leggingDamageExempt.add(player.getUniqueId());
				
				if(boots)
					bootDamageExempt.add(player.getUniqueId());
			}
		}
	}
}

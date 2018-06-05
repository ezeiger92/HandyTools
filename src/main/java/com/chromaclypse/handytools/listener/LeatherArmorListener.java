package com.chromaclypse.handytools.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import com.chromaclypse.handytools.ToolConfig.LeatherArmor;
import com.chromaclypse.handytools.Util;

public class LeatherArmorListener implements Listener {
	private LeatherArmor config;
	
	public LeatherArmorListener(LeatherArmor config) {
		this.config = config;
	}
	
	private HashMap<UUID, Long> invulnerabilities = new HashMap<>();
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageEvent event) {
		
		if(config.enabled && event.getEntity() instanceof Player) {
			Player target = (Player)event.getEntity();
			long time = System.currentTimeMillis();
			
			Long until = invulnerabilities.get(target.getUniqueId());
			
			if(until != null && until > time) {
				event.setCancelled(true);
			}
			
			else {
				int invulnerabilityTicks = 0;
				
				if(Util.helmetIs(target, Material.LEATHER_HELMET))
					invulnerabilityTicks += config.helmet_evasion_ticks;
				
				if(Util.chestplateIs(target, Material.LEATHER_CHESTPLATE))
					invulnerabilityTicks += config.chestplate_evasion_ticks;
				
				if(Util.leggingIs(target, Material.LEATHER_LEGGINGS))
					invulnerabilityTicks += config.legging_evasion_ticks;
				
				if(Util.bootIs(target, Material.LEATHER_BOOTS))
					invulnerabilityTicks += config.boot_evasion_ticks;

				if(invulnerabilityTicks > 0) {
					// + .5 seconds because that's vanilla
					invulnerabilityTicks += 10;
					
					// ticks to millis
					invulnerabilities.put(target.getUniqueId(), time + invulnerabilityTicks * 50);
				}
			}
		}
	}
}

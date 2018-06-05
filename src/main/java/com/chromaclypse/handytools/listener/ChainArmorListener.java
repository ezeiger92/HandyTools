package com.chromaclypse.handytools.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.chromaclypse.handytools.ToolPlugin;
import com.chromaclypse.handytools.Util;
import com.chromaclypse.handytools.ToolConfig.ChainArmor;

public class ChainArmorListener implements Listener {
	private static final String NAME = "HandyTools buff";
	private ChainArmor config;
	
	public ChainArmorListener(ChainArmor config) {
		this.config = config;
		
		for(Player p : Bukkit.getOnlinePlayers())
			cleanup(p);
	}
	
	HashMap<UUID, AttributeModifier> active = new HashMap<>();
	
	private static void cleanup(Player p) {
		AttributeInstance instance = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		
		for(AttributeModifier m : instance.getModifiers())
			if(m.getName().equals(NAME))
				instance.removeModifier(m);
	}
	
	@EventHandler
	public void login(PlayerJoinEvent event) {
		cleanup(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void swingAndAMiss(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		AttributeInstance instance = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		AttributeModifier current = active.remove(player.getUniqueId());
		
		if(current != null)
			instance.removeModifier(current);
		
		if(config.enabled && event.hasItem() &&
				event.getAction() == Action.LEFT_CLICK_AIR) {
			double value = 0;

			if(Util.helmetIs(player, Material.CHAINMAIL_HELMET)) {
				value += config.helmet_miss_reduction;
			}
			
			if(Util.chestplateIs(player, Material.CHAINMAIL_CHESTPLATE)) {
				value += config.chestplate_miss_reduction;
			}
			
			if(Util.leggingIs(player, Material.CHAINMAIL_LEGGINGS)) {
				value += config.legging_miss_reduction;
			}
			
			if(Util.bootIs(player, Material.CHAINMAIL_BOOTS)) {
				value += config.boot_miss_reduction;
			}
			
			if(value > 0) {
				AttributeModifier modifier = new AttributeModifier(NAME, value, Operation.MULTIPLY_SCALAR_1);
				instance.addModifier(modifier);
				active.put(player.getUniqueId(), modifier);
				
				Bukkit.getScheduler().runTaskLater(ToolPlugin.instance, () -> {
					
					AttributeModifier activeModifier = active.get(player.getUniqueId());
					
					if(modifier.equals(activeModifier)) {
						instance.removeModifier(modifier);
						active.remove(player.getUniqueId());
					}
				}, 40);
			}
		}
	}
}

package com.chromaclypse.handytools.listener;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class CustomItemListener implements Listener {
	
	@EventHandler
	public void onRes(EntityResurrectEvent event) {
		// Had no totem
		if(event.getEntity() instanceof Player && event.isCancelled()) {
			ItemStack[] inv = ((Player) event.getEntity()).getInventory().getStorageContents();
			int length = inv.length;
			
			for(int slot = 0; slot < length; ++slot) {
				ItemStack is = inv[slot];
				
				if(is != null && is.getType() == Material.TOTEM && is.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0) {
					is.setAmount(is.getAmount() - 1);
					event.setCancelled(false);
					break;
				}
			}
		}
	}
	
	private static EnumSet<Material> arrowTypes = EnumSet.of(Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW);
	
	private static boolean isArrow(ItemStack stack) {
		return stack != null && arrowTypes.contains(stack.getType());
	}
	
	private static ItemStack primaryArrow(PlayerInventory inv) {
		if(isArrow(inv.getItemInMainHand()))
			return inv.getItemInMainHand();
		
		else if(isArrow(inv.getItemInOffHand()))
			return inv.getItemInOffHand();
		
		else
			for(ItemStack item : inv.getStorageContents())
				if(isArrow(item))
					return item;
		
		return null;
	}
	
	@EventHandler
	public void shotBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player) {
			PlayerInventory inv = ((Player)event.getEntity()).getInventory();
			
			ItemStack arrow = primaryArrow(inv);
			
			if(arrow != null && arrow.getEnchantmentLevel(Enchantment.LURE) > 0) {
				EnderPearl pearl = (EnderPearl) event.getProjectile().getWorld().spawnEntity(event.getProjectile().getLocation(), EntityType.ENDER_PEARL);
				pearl.setShooter(event.getEntity());
				
				event.getProjectile().addPassenger(pearl);
			}
		}
	}
	
	@EventHandler
	public void arrowHit(ProjectileHitEvent event) {
		for(Entity passenger : event.getEntity().getPassengers()) {
			if(passenger instanceof Projectile) {
				Projectile projectile = (Projectile)passenger;
				projectile.leaveVehicle();
				projectile.teleport(event.getEntity(), TeleportCause.ENDER_PEARL);
				projectile.setVelocity(event.getEntity().getVelocity());
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			
			if(event.getFinalDamage() >= player.getHealth() && event.getFinalDamage() - player.getHealth() <= 6) {
				ItemStack[] inv = player.getInventory().getStorageContents();
				int length = inv.length;
				
				for(int slot = 0; slot < length; ++slot) {
					ItemStack item = inv[slot];
					
					if(item != null && item.getType() == Material.RABBIT_FOOT && item.getEnchantmentLevel(Enchantment.LUCK) > 0) {
						event.setDamage(0);
						player.setHealth(1);
						item.setAmount(item.getAmount() - 1);
						break;
					}
				}
			}
		}
	}
}

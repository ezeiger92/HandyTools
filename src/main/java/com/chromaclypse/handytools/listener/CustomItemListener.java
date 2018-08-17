package com.chromaclypse.handytools.listener;

import java.util.EnumSet;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import com.chromaclypse.handytools.ToolPlugin;

public class CustomItemListener implements Listener {
	
	@EventHandler
	public void onRes(EntityResurrectEvent event) {
		// Had no totem
		if(event.getEntity() instanceof Player && event.isCancelled()) {
			ItemStack[] inv = ((Player) event.getEntity()).getInventory().getStorageContents();
			int length = inv.length;
			
			for(int slot = 0; slot < length; ++slot) {
				ItemStack is = inv[slot];
				
				if(is != null && is.getType() == Material.TOTEM_OF_UNDYING && is.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0) {
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
	
	private static void useHand(Player player, EquipmentSlot hand) {
		ItemStack stack;
		
		if(player.getGameMode() == GameMode.CREATIVE) {
			return;
		}
		
		if(hand == EquipmentSlot.HAND) {
			stack = player.getInventory().getItemInMainHand();
		}
		else {
			stack = player.getInventory().getItemInOffHand();
		}
		
		stack.setAmount(stack.getAmount() - 1);
	}
	
	// throwable fire
	@EventHandler
	public void fireball(PlayerInteractEvent event) {
		if(event.getMaterial() != Material.FIRE_CHARGE ||
				event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}
		
		event.setUseItemInHand(Result.ALLOW);
		
		Player player = event.getPlayer();
		
		useHand(player, event.getHand());
		player.launchProjectile(SmallFireball.class);
		player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
	}
	
	// Eye in nether
	@EventHandler
	public void endTraveller(PlayerInteractEvent event) {
		if(event.getMaterial() != Material.ENDER_EYE) {
			return;
		}
		
		Action a = event.getAction();
		
		if(a != Action.RIGHT_CLICK_BLOCK && a != Action.RIGHT_CLICK_AIR) {
			return;
		}
		
		Location loc = event.getPlayer().getLocation();
		
		if(loc.getWorld().getEnvironment() != Environment.NETHER) {
			return;
		}
		
		useHand(event.getPlayer(), event.getHand());
		Block target = loc.getWorld().getHighestBlockAt(loc).getRelative(BlockFace.UP);
		
		target.setType(Material.END_PORTAL);
		event.getPlayer().teleport(target.getLocation());
		
		Bukkit.getScheduler().runTaskLater(ToolPlugin.instance, () -> {
			target.setType(Material.AIR);
		}, 1);
	}
	
	// fish effector
	@EventHandler
	public void onEat(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() != Material.TROPICAL_FISH) {
			return;
		}
		
		Player player = event.getPlayer();
		
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}
	
	@EventHandler
	public void antiDisc(EntityDeathEvent event) {
		if(event.getEntityType() != EntityType.CREEPER) {
			return;
		}
		
		for(ItemStack is : event.getDrops()) {
			Material type = is.getType();
			
			if(type.isRecord() && type != Material.MUSIC_DISC_CAT && type != Material.MUSIC_DISC_13) {
				if(type.name().hashCode() % 5 > 3) {
					is.setType(Material.MUSIC_DISC_13);
				}
				else {
					is.setType(Material.MUSIC_DISC_CAT);
				}
			}
		}
	}
	
	@EventHandler
	public void bottler(EntityDeathEvent event) {
		EntityDamageEvent source = event.getEntity().getLastDamageCause();
		
		if(source instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) source;
			
			if(ee.getDamager() instanceof Player && ee.getCause() == DamageCause.ENTITY_ATTACK) {
				Player p = (Player) ee.getDamager();
				
				ItemStack hand = p.getInventory().getItemInMainHand();
				
				if(hand.getType() != Material.GLASS_BOTTLE) {
					return;
				}
				
				if(event.getEntity() instanceof Ageable && !((Ageable)event.getEntity()).isAdult()) {
					return;
				}
				
				event.setDroppedExp(Math.min(event.getDroppedExp(), 0));
				
				if(hand.getAmount() == 1) {
					hand.setType(Material.EXPERIENCE_BOTTLE);
				}
				else {
					hand.setAmount(hand.getAmount() - 1);
					
					for(ItemStack is : p.getInventory().addItem(new ItemStack(Material.EXPERIENCE_BOTTLE)).values()) {
						Location loc = p.getLocation();
						
						loc.getWorld().dropItemNaturally(loc, is);
					}
				}
			}
		}
	}
}

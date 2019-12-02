package com.chromaclypse.handytools.listener;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class FarmlandListener implements Listener {
	private static final EnumSet<Material> crops;
	private static final EnumMap<Material, Material> seedMaterial;
	
	private static final ItemStack airStack = new ItemStack(Material.AIR);
	
	private static final void map(Material crop, Material seed) {
		seedMaterial.put(crop, seed);
		crops.add(crop);
	}
	
	private ItemStack listeningFor = airStack;
	private final Random random = new Random();
	
	static {
		seedMaterial = new EnumMap<>(Material.class);
		crops = EnumSet.noneOf(Material.class);

		map(Material.WHEAT, Material.WHEAT_SEEDS);
		map(Material.CARROTS, Material.CARROT);
		map(Material.POTATOES, Material.POTATO);
		map(Material.BEETROOTS, Material.BEETROOT_SEEDS);
		map(Material.NETHER_WART, Material.NETHER_WART);
	}
	
	private static final Material replant(Material material) {
		Material result = seedMaterial.get(material);
		
		return result == null ? material : result;
	}
	
	@EventHandler
	public void itemSpawn(ItemSpawnEvent event) {
		if(listeningFor.getType() != Material.AIR) {
			ItemStack spawning = event.getEntity().getItemStack();
			
			if(spawning.getType() == listeningFor.getType()) {
				int taking = Math.min(spawning.getAmount(), listeningFor.getAmount());

				spawning.setAmount(spawning.getAmount() - taking);
				listeningFor.setAmount(listeningFor.getAmount() - taking);
				
				if(spawning.getAmount() == 0) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void harvester(PlayerInteractEvent event) {
		Action action = event.getAction();
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		
		switch(action) {
			// Click crop
			case RIGHT_CLICK_BLOCK:
				if(event.getHand() == EquipmentSlot.OFF_HAND ||
						player.getGameMode() != GameMode.SURVIVAL ||
						player.isSneaking()) {
					break;
				}
				
				if(crops.contains(block.getType())) {
					Material seed = replant(block.getType());
					
					player.incrementStatistic(Statistic.USE_ITEM, seed);
					
					Ageable crop;
					
					if(block.getBlockData() instanceof Ageable) {
						crop = (Ageable) block.getBlockData();
						
						if(crop.getAge() != crop.getMaximumAge()) {
							break;
						}
					}
					else {
						break;
					}
					
					Location l = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);

					
					
					listeningFor = new ItemStack(seed, 1);
					BlockState state = block.getState();
					
					/*try {
						Object blockPosition = Reflect.NMS("BlockPosition").getConstructor(int.class, int.class, int.class)
								.newInstance(l.getBlockX(), l.getBlockY(), l.getBlockZ());
						Object worldObject = l.getWorld().getClass().getMethod("getHandle").invoke(l.getWorld());
						
						Reflect.NMS("World").getMethod("setAir", blockPosition.getClass(), boolean.class).invoke(worldObject, blockPosition, true);
					}
					catch(Exception e) {*/
						l.getWorld().spawnParticle(Particle.BLOCK_DUST, l, 50, 0.5, 0.5, 0.5, crop);
						
						Sound sound;
						if(state.getType() == Material.NETHER_WART) {
							sound = Sound.BLOCK_STONE_BREAK;
						}
						else {
							sound = Sound.BLOCK_GRASS_BREAK;
						}
						
						l.getWorld().playSound(l, sound, SoundCategory.BLOCKS, 1.0f, 0.80f);
						block.breakNaturally(player.getInventory().getItemInMainHand());
					//}
					
					// Force the crop back into existence (if setAir removed it) so we can set age below
					state.update(true, false);
					
					
					if(listeningFor.getAmount() > 0) {
						player.getInventory().removeItem(listeningFor);
						listeningFor = airStack;
					}
					
					event.setUseItemInHand(Result.ALLOW);
					
					crop.setAge(0);
					block.setBlockData(crop);
				}
				break;
				
				// Stomp crop
			case PHYSICAL:
				
				if(event.hasBlock()) {
					BlockData data = block.getBlockData();
					
					if(data instanceof Farmland) {
						if(player.isSneaking()) {
							if(random.nextFloat() * player.getFallDistance() < 1.0f) {
								event.setCancelled(true);
								break;
							}
						}
						
						if(drySoil(block, (Farmland) data)) {
							event.setCancelled(true);
						}
					}
				}
				break;
				
			default:
				break;
		}
	}
	
	private static boolean drySoil(Block block, Farmland soil) {
		if(soil.getMoisture() != soil.getMaximumMoisture()) {
			return false;
		}
		
		soil.setMoisture(0);
		block.setBlockData(soil);
		
		Block above = block.getRelative(BlockFace.UP);
		
		if(crops.contains(above.getType())) {
			Ageable crop;
			
			if(block.getBlockData() instanceof Ageable) {
				crop = (Ageable) block.getBlockData();
				
				int dec = Math.max(crop.getMaximumAge() / 4, 1);
				
				crop.setAge(Math.max(crop.getAge() - dec, 0));
				above.setBlockData(crop);
			}
		}
		return true;
	}
	
	private static boolean onWetSoil(Block block) {
		BlockData data = block.getBlockData();
		
		if(data instanceof Farmland) {
			Farmland soil = (Farmland) data;
			
			return drySoil(block, soil);
		}
		
		return false;
	}
	
	// entity stomp crop
	@EventHandler
	public void trampler(EntityChangeBlockEvent event) {
		if(onWetSoil(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	private static void hydrate(Location location, int radius) {
		World world = location.getWorld();
		int lx = location.getBlockX();
		int ly = location.getBlockY();
		int lz = location.getBlockZ();
		
		for(int x = lx - radius; x <= lx + radius; ++x) {
			for(int y = ly - radius; y <= ly + radius; ++y) {
				for(int z = lz - radius; z <= lz + radius; ++z) {
					Block block = world.getBlockAt(x, y, z);
					
					if(block.getBlockData() instanceof Farmland) {
						Farmland soil = (Farmland) block.getBlockData();
						
						soil.setMoisture(soil.getMaximumMoisture());
						block.setBlockData(soil);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void generic(ProjectileHitEvent event) {
		if(event.getEntity() instanceof ThrownPotion) {
			ThrownPotion potion = (ThrownPotion) event.getEntity();
			
			if(potion.getEffects().isEmpty()) {
				hydrate(potion.getLocation(), event instanceof LingeringPotionSplashEvent ? 2 : 1);
			}
		}
	}
}

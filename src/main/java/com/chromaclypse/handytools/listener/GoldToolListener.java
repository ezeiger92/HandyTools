package com.chromaclypse.handytools.listener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.persistence.PersistentDataType;

import com.chromaclypse.api.messages.Text;
import com.chromaclypse.handytools.AOE;
import com.chromaclypse.handytools.ToolPlugin;
import com.chromaclypse.handytools.Util;
import com.chromaclypse.handytools.command.PlayerState;
import com.chromaclypse.handytools.ToolConfig.GoldTools;
import com.chromaclypse.handytools.ToolConfig.GoldTools.HeadData;
import com.chromaclypse.handytools.ToolConfig.GoldTools.HeadData.HeadEntry;

public class GoldToolListener implements Listener {
	private GoldTools config;
	private PlayerState stateConfig;
	
	public GoldToolListener(GoldTools config, PlayerState stateConfig) {
		this.config = config;
		this.stateConfig = stateConfig;
	}
	
	private static final EnumSet<Material> goldTools = EnumSet.of(Material.GOLDEN_AXE,
			Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE, Material.GOLDEN_SWORD);
	
	@EventHandler
	public void onMend(PlayerItemMendEvent event) {
		ItemStack item = event.getItem();
		
		PlayerState.State state = stateConfig.players.get(event.getPlayer().getUniqueId().toString());
		
		if(state != null && "new".equals(state.mending_mode)) {
				event.setCancelled(true);
		}
		else if(goldTools.contains(item.getType())) {
			event.setCancelled(true);
		}
	}
	
	private boolean isMendable(ItemStack mend) {
		return mend != null && mend.hasItemMeta() && mend.getItemMeta().hasEnchant(Enchantment.MENDING);
	}
	
	private Random random = new Random();
	
	@EventHandler
	public void onNewMend(PlayerExpChangeEvent event) {
		PlayerState.State state = stateConfig.players.get(event.getPlayer().getUniqueId().toString());
		
		if(state != null && "new".equals(state.mending_mode)) {
			List<ItemStack> items = new ArrayList<>();
			
			for(ItemStack i : event.getPlayer().getInventory().getArmorContents()) {
				if(isMendable(i)) {
					items.add(i);
				}
			}
			
			ItemStack i = event.getPlayer().getInventory().getItemInMainHand();
			if(isMendable(i)) {
				items.add(i);
			}
			
			i = event.getPlayer().getInventory().getItemInOffHand();
			if(isMendable(i)) {
				items.add(i);
			}
			
			if(items.size() == 0) {
				return;
			}
			
			ItemStack item = items.get(random.nextInt(items.size()));
			
			int newAmt = newMend(item, event.getAmount());
			event.setAmount(newAmt);
		}
	}
	
	private final NamespacedKey PARTIAL_REPAIR = new NamespacedKey(ToolPlugin.instance, "partialRepair");
	
	private final String REPAIR_STR = Text.format().colorize("&7Repair Cost: &f");
	
	private final void updateLore(ItemMeta meta, String str) {
		List<String> lore;
		
		if(!meta.hasLore()) {
			if(str == null) {
				return;
			}
			lore = new ArrayList<String>();
		}
		else {
			lore = new ArrayList<String>(meta.getLore());
		}
		
		int index = -1;
		for(int i = 0; i < lore.size(); ++i) {
			if(lore.get(i).startsWith(REPAIR_STR)) {
				index = i;
				break;
			}
		}
		
		if(index >= 0) {
			if(str == null) {
				lore.remove(index);
			}
			else {
				lore.set(index, REPAIR_STR + Text.format().colorize(str));
			}
		}
		else if(str != null) {
			lore.add(REPAIR_STR + Text.format().colorize(str));
		}
		
		meta.setLore(lore);
	}
	
	private final int newMend(ItemStack item, int amount) {
		
		if(!item.hasItemMeta()) {
			return amount;
		}

		ItemMeta meta = item.getItemMeta();
		
		if(!(meta instanceof Repairable) || !meta.hasEnchant(Enchantment.MENDING)) {
			if(meta != null) {
				meta.getPersistentDataContainer().remove(PARTIAL_REPAIR);
				updateLore(meta, null);
			}
			
			return amount;
		}
		int cost = ((Repairable) meta).getRepairCost();
		
		if(cost > 39) {
			updateLore(meta, "&cUnrepairable");
		}
		
		int partial;
		{
			Integer p = meta.getPersistentDataContainer().get(PARTIAL_REPAIR, PersistentDataType.INTEGER);
			partial = p == null ? 0 : p;
		}
		
		int diff = cost - (partial + 99) / 100;
		
		// Reset cost
		if(diff > 0) {
			partial = cost * 100;
		}
		
		partial = Math.max(partial - amount, 0);
		amount -= Math.min(partial, amount);
		
		cost = (partial + 99) / 100;
		
		((Repairable)meta).setRepairCost(cost);
		
		if(partial > 0) {
			meta.getPersistentDataContainer().set(PARTIAL_REPAIR, PersistentDataType.INTEGER, partial);
			updateLore(meta, String.valueOf(partial));
		}
		else {
			meta.getPersistentDataContainer().remove(PARTIAL_REPAIR);
			updateLore(meta, null);
		}
		item.setItemMeta(meta);
		
		return amount;
	}
	
	@EventHandler
	public void onAnvil(PrepareAnvilEvent event) {
		PlayerState.State state = stateConfig.players.get(event.getViewers().get(0).getUniqueId().toString());
		
		if(state != null && "new".equals(state.mending_mode) && event.getResult() != null) {
			ItemStack item = event.getResult();
			newMend(item, 0);
			event.setResult(item);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.getClass().equals(BlockBreakEvent.class))
			return;
		
		if(!event.isDropItems() || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		
		Material type = event.getBlock().getType();
		Material hand = event.getPlayer().getInventory().getItemInMainHand().getType();
		
		// Check golden pickaxe vs stone
		if(Util.stoneTypes.contains(type) && hand == Material.GOLDEN_PICKAXE) {
			AOE.dig(event.getBlock(), event.getPlayer(),
					config.pickaxe_depth,
					config.pickaxe_width,
					config.pickaxe_height,
					config.pickaxe_tpc,
					config.pickaxe_bpc);
		}
		
		else if(hand == Material.GOLDEN_SHOVEL) {
			if(Util.dirtTypes.contains(type))
					AOE.dig(event.getBlock(), event.getPlayer(),
							config.spade_depth,
							config.spade_width,
							config.spade_height,
							config.spade_tpc,
							config.spade_bpc);
		}
		
		else if(hand == Material.GOLDEN_AXE) {
			if(Util.logTypes.contains(type))
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
			
			if(damager.getInventory().getItemInMainHand().getType() == Material.GOLDEN_SWORD && 
					entity.getHealth() > event.getFinalDamage())
				itemDamageExempt.add(damager.getUniqueId());
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onKill(EntityDeathEvent event) {
		Player killer = event.getEntity().getKiller();
		
		if(killer != null) {
			Material hand = killer.getInventory().getItemInMainHand().getType();
		
			if(hand == Material.GOLDEN_AXE && config.axe_decapitator) {
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

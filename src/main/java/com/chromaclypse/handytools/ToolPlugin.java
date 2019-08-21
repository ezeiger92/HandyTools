package com.chromaclypse.handytools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.Log;
import com.chromaclypse.api.item.ItemBuilder;
import com.chromaclypse.api.plugin.FuturePlugin;
import com.chromaclypse.handytools.listener.FarmlandListener;
import com.chromaclypse.handytools.listener.CauldronItems;
import com.chromaclypse.handytools.listener.ChainArmorListener;
import com.chromaclypse.handytools.listener.CustomItemListener;
import com.chromaclypse.handytools.listener.GoldArmorListener;
import com.chromaclypse.handytools.listener.GoldToolListener;
import com.chromaclypse.handytools.listener.LeatherArmorListener;
import com.chromaclypse.handytools.listener.WoodToolListener;

public class ToolPlugin extends JavaPlugin {
	ToolConfig config = new ToolConfig();
	MobConfig mobConfig = new MobConfig();
	NCPCompat compat = new NCPCompat();
	
	public static ToolPlugin instance;
	public ToolPlugin() {
		instance = this;
	}
	
	@Override
	public void onEnable() {
		new FuturePlugin(this, "NoCheatPlus") {
			@Override
			public void onFindPlugin(Plugin desiredPlugin) {
				compat.init();
			}
		};
		
		init();
		
		getCommand("handytools").setExecutor(this);

		{
			NamespacedKey key = new NamespacedKey(this, "lucky_rabbit_foot");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemBuilder(Material.RABBIT_FOOT)
					.forceEnchant(Enchantment.LUCK, 1)
					.display("&bLucky Rabbit's Foot")
					.wrapLore("&5Uncanny luck in near-death scenarios")
					.flag(ItemFlag.HIDE_ENCHANTS)
					.get());

			recipe.shape("GGG", "GRG", "GGG");
			recipe.setIngredient('R', Material.RABBIT_FOOT);
			recipe.setIngredient('G', Material.GOLD_INGOT);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "exploration_arrow");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemBuilder(Material.SPECTRAL_ARROW)
					.forceEnchant(Enchantment.LURE, 1)
					.display("&bArrow of Exploration")
					.wrapLore("&5An ender pearl AND an arrow!")
					.flag(ItemFlag.HIDE_ENCHANTS)
					.get());

			recipe.shape(" E ", "EAE", " E ");
			recipe.setIngredient('E', Material.ENDER_PEARL);
			recipe.setIngredient('A', Material.SPECTRAL_ARROW);
			getServer().addRecipe(recipe);
		}
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}
	
	private void init() {
		config.init(this);
		mobConfig.init(this);
		
		getServer().getPluginManager().registerEvents(new WoodToolListener(config.wood_tools), this);
		getServer().getPluginManager().registerEvents(new GoldToolListener(config.gold_tools), this);
		
		getServer().getPluginManager().registerEvents(new LeatherArmorListener(config.leather_armor), this);
		getServer().getPluginManager().registerEvents(new ChainArmorListener(config.chain_armor), this);
		getServer().getPluginManager().registerEvents(new GoldArmorListener(config.gold_armor), this);

		getServer().getPluginManager().registerEvents(new CustomItemListener(mobConfig), this);
		getServer().getPluginManager().registerEvents(new FarmlandListener(), this);
		
		getServer().getPluginManager().registerEvents(new CauldronItems(config.caudron_recipes), this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 0) {
			String arg1 = args[0];
			if("reload".equalsIgnoreCase(arg1)) {
				sender.sendMessage(ChatColor.GREEN + "HandyTools reloaded");
				onDisable();
				init();
			}
			else if("saveAxe".equalsIgnoreCase(arg1)) {
				config.wood_tools.axe_item = ((Player)sender).getInventory().getItemInMainHand();
				Log.info(config.wood_tools.axe_item.serialize().toString());
				config.save(this);
			}
		}
		
		return true;
	}
	
	public NCPCompat getNoCheat() {
		return compat;
	}
}

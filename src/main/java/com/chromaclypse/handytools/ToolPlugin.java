package com.chromaclypse.handytools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SkullType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.Log;
import com.chromaclypse.api.item.ItemBuilder;
import com.chromaclypse.api.plugin.FuturePlugin;
import com.chromaclypse.handytools.listener.ChainArmorListener;
import com.chromaclypse.handytools.listener.CustomItemListener;
import com.chromaclypse.handytools.listener.GoldArmorListener;
import com.chromaclypse.handytools.listener.GoldToolListener;
import com.chromaclypse.handytools.listener.LeatherArmorListener;
import com.chromaclypse.handytools.listener.WoodToolListener;

public class ToolPlugin extends JavaPlugin {
	ToolConfig config = new ToolConfig();
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
		
		config.init(this);
		
		getServer().getPluginManager().registerEvents(new WoodToolListener(config.wood_tools), this);
		getServer().getPluginManager().registerEvents(new GoldToolListener(config.gold_tools), this);
		
		getServer().getPluginManager().registerEvents(new LeatherArmorListener(config.leather_armor), this);
		getServer().getPluginManager().registerEvents(new ChainArmorListener(config.chain_armor), this);
		getServer().getPluginManager().registerEvents(new GoldArmorListener(config.gold_armor), this);
		
		getServer().getPluginManager().registerEvents(new CustomItemListener(), this);
		
		getCommand("handytools").setExecutor(this);

		{
			NamespacedKey key = new NamespacedKey(this, "lucky_rabbit_foot");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemBuilder(Material.RABBIT_FOOT)
					.forceEnchant(Enchantment.LUCK, 1)
					.display("&bLucky Rabbit's Foot")
					.wrapLore("&5Uncanny luck in near-death scenarios")
					.get());

			recipe.shape("GGG", "GRG", "GGG");
			recipe.setIngredient('R', Material.RABBIT_FOOT);
			recipe.setIngredient('G', Material.GOLD_INGOT);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "totem_of_resurrection");
			ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemBuilder(Material.TOTEM)
					.forceEnchant(Enchantment.ARROW_INFINITE, 1)
					.display("&bTotem of Resurrection")
					.wrapLore("&5And to the Reaper, we say:", " \"Not today!\"")
					.get());
			
			recipe.addIngredient(Material.DRAGON_EGG);
			recipe.addIngredient(Material.TOTEM);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "notch_apple");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.GOLDEN_APPLE, 1, (short)1));
			
			
			@SuppressWarnings("deprecation")
			MaterialData goldApple = new MaterialData(Material.GOLDEN_APPLE, (byte) 0);

			recipe.shape("GGG", "GNG", "GGG");
			recipe.setIngredient('N', Material.NETHER_STAR);
			recipe.setIngredient('G', goldApple);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "dragon_egg");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.DRAGON_EGG));

			recipe.shape("CHC", "OEO", "COC");
			recipe.setIngredient('C', Material.END_CRYSTAL);
			recipe.setIngredient('H', new ItemBuilder(SkullType.DRAGON).get().getData());
			recipe.setIngredient('E', Material.ELYTRA);
			recipe.setIngredient('O', Material.OBSIDIAN);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "totem_of_undying");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.TOTEM));

			recipe.shape("GGG", "GEG", "GGG");
			recipe.setIngredient('E', Material.EMERALD);
			recipe.setIngredient('G', Material.GOLD_BLOCK);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "elytra");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.ELYTRA));

			recipe.shape("SLS", "LNL", "L L");
			recipe.setIngredient('L', Material.LEATHER);
			recipe.setIngredient('S', Material.SHULKER_SHELL);
			recipe.setIngredient('N', Material.NETHER_STAR);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "exploration_arrow");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemBuilder(Material.SPECTRAL_ARROW)
					.forceEnchant(Enchantment.LURE, 1)
					.display("&bArrow of Exploration")
					.wrapLore("&5An ender pearl AND an arrow!")
					.get());

			recipe.shape(" E ", "EAE", " E ");
			recipe.setIngredient('E', Material.ENDER_PEARL);
			recipe.setIngredient('A', Material.SPECTRAL_ARROW);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "chain_helmet");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.CHAINMAIL_HELMET));

			recipe.shape("NNN", "N N");
			recipe.setIngredient('N', Material.IRON_NUGGET);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "chain_chestplate");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.CHAINMAIL_CHESTPLATE));

			recipe.shape("N N", "NNN", "NNN");
			recipe.setIngredient('N', Material.IRON_NUGGET);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "chain_leggings");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.CHAINMAIL_LEGGINGS));

			recipe.shape("NNN", "N N", "N N");
			recipe.setIngredient('N', Material.IRON_NUGGET);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "chain_boots");
			ShapedRecipe recipe = new ShapedRecipe(key, new ItemStack(Material.CHAINMAIL_BOOTS));

			recipe.shape("N N", "N N");
			recipe.setIngredient('N', Material.IRON_NUGGET);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "uncraft_iron_barding");
			ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.IRON_INGOT, 4));
			
			recipe.addIngredient(3, Material.EMERALD);
			recipe.addIngredient(Material.IRON_BARDING);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "uncraft_gold_barding");
			ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.GOLD_INGOT, 4));
			
			recipe.addIngredient(3, Material.EMERALD);
			recipe.addIngredient(Material.GOLD_BARDING);
			getServer().addRecipe(recipe);
		}
		{
			NamespacedKey key = new NamespacedKey(this, "uncraft_diamond_barding");
			ShapelessRecipe recipe = new ShapelessRecipe(key, new ItemStack(Material.DIAMOND, 4));
			
			recipe.addIngredient(3, Material.EMERALD);
			recipe.addIngredient(Material.DIAMOND_BARDING);
			getServer().addRecipe(recipe);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 0) {
			String arg1 = args[0];
			if("reload".equalsIgnoreCase(arg1)) {
				sender.sendMessage(ChatColor.GREEN + "HandyTools reloaded");
				config.init(this);
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

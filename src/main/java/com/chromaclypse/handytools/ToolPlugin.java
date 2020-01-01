package com.chromaclypse.handytools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.chromaclypse.api.Log;
import com.chromaclypse.api.command.CommandBase;
import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.item.ItemBuilder;
import com.chromaclypse.api.plugin.FuturePlugin;
import com.chromaclypse.handytools.listener.FarmlandListener;
import com.chromaclypse.handytools.command.General;
import com.chromaclypse.handytools.command.Moderator;
import com.chromaclypse.handytools.command.PlayerState;
import com.chromaclypse.handytools.command.Utilities;
import com.chromaclypse.handytools.command.SavedLocation;
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
	PlayerState stateConfig = new PlayerState();
	NCPCompat compat = new NCPCompat();
	private SavedLocation locations = new SavedLocation();
	
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
		
		getCommand("handytools").setExecutor(new CommandBase()
				.with().arg("reload").calls(this::reloadCommand)
				.with().arg("saveAxe").calls(this::saveAxeCommand)
				.with().arg("version").calls(CommandBase::pluginVersion)
				.getCommand());
		getCommand("echo").setExecutor(new CommandBase().calls(General::echo).getCommand());
		getCommand("util").setExecutor(new Utilities(stateConfig).getCommand());
		{
			Moderator modCommands = new Moderator(locations);
			TabExecutor mm = new CommandBase()
					.calls(modCommands::spectateOn)
					.with().arg("on").calls(modCommands::spectateOn)
					.with().arg("off").calls(modCommands::spectateOff)
					.with().option(CommandBase::onlinePlayers).calls(modCommands::spectatePlayer)
					.getCommand();
			getCommand("spectate").setExecutor(mm);
			getCommand("spectate").setTabCompleter(mm);
		}
		
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
		locations.init(this);
		stateConfig.init(this);
		
		getServer().getPluginManager().registerEvents(new WoodToolListener(config.wood_tools), this);
		getServer().getPluginManager().registerEvents(new GoldToolListener(config.gold_tools, stateConfig), this);
		
		getServer().getPluginManager().registerEvents(new LeatherArmorListener(config.leather_armor), this);
		getServer().getPluginManager().registerEvents(new ChainArmorListener(config.chain_armor), this);
		getServer().getPluginManager().registerEvents(new GoldArmorListener(config.gold_armor), this);

		getServer().getPluginManager().registerEvents(new CustomItemListener(mobConfig), this);
		getServer().getPluginManager().registerEvents(new FarmlandListener(), this);
		
		getServer().getPluginManager().registerEvents(new CauldronItems(config.caudron_recipes), this);
	}
	
	public boolean reloadCommand(Context context) {
		context.Sender().sendMessage(ChatColor.GREEN + "HandyTools reloaded");
		onDisable();
		init();
		return true;
	}
	
	public boolean saveAxeCommand(Context context) {
		config.wood_tools.axe_item = context.GetHeld();
		Log.info(config.wood_tools.axe_item.serialize().toString());
		config.save(this);
		return true;
	}
	
	public NCPCompat getNoCheat() {
		return compat;
	}
}

package com.chromaclypse.handytools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.chromaclypse.api.annotation.Mutable;

public class CauldronRecipe {
	private final Material input;
	private final Material output;
	private final int minWater;
	private final int maxWater;
	private final int waterDelta;
	
	public CauldronRecipe(Material input, int minWater, int maxWater, Material output, int waterDelta) {
		this.input = input;
		this.output = output;
		this.minWater = minWater;
		this.maxWater = maxWater;
		this.waterDelta = waterDelta;
	}
	
	public Material getInput() {
		return input;
	}
	
	public boolean isAcceptableLevel(int waterLevel) {
		return waterLevel >= minWater && waterLevel <= maxWater;
	}
	
	public void transform(@Mutable Levelled cauldron, @Mutable ItemStack stack, Player player) {
		int newLevel = Math.min(Math.max(0, cauldron.getLevel() + waterDelta), cauldron.getMaximumLevel());
		
		cauldron.setLevel(newLevel);
		
		if(stack.getAmount() == 1) {
			stack.setType(output);
			
			if(stack.hasItemMeta()) {
				stack.setItemMeta(Bukkit.getItemFactory().getItemMeta(output));
			}
		}
		else {
			stack.setAmount(stack.getAmount() - 1);
			
			for(ItemStack overflow : player.getInventory().addItem(new ItemStack(output)).values()) {
				player.getWorld().dropItemNaturally(player.getLocation(), overflow);
			}
		}
	}
}

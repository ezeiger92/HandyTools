package com.chromaclypse.handytools;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Util {
	public static boolean rng(double threshold) {
		return Math.random() < threshold;
	}
	
	public static Vector[] makeBasis(float yaw, float pitch) {
		Vector direction;
		Vector right;
		
		yaw = Math.round(yaw + 45 + 360) % 360;
		if(yaw < 90)
			right = new Vector(-1, 0, 0);
		else if(yaw < 180)
			right = new Vector(0, 0, -1);
		else if(yaw < 270)
			right = new Vector(1, 0, 0);
		else
			right = new Vector(0, 0, 1);
		
		if(pitch > 45)
			direction = new Vector(0, -1, 0);
		else if(pitch < -45)
			direction = new Vector(0, 1, 0);
		else
			direction = new Vector(0,1,0).crossProduct(right);
		
		Vector up = direction.clone().crossProduct(right);
		
		return new Vector[] {direction, right, up};
	}
	
	public static boolean helmetIs(Player player, Material helmet) {
		ItemStack armor = player.getInventory().getHelmet();
		
		return armor != null && armor.getType() == helmet;
	}
	
	public static boolean chestplateIs(Player player, Material chestplate) {
		ItemStack armor = player.getInventory().getChestplate();
		
		return armor != null && armor.getType() == chestplate;
	}
	
	public static boolean leggingIs(Player player, Material leggings) {
		ItemStack armor = player.getInventory().getLeggings();
		
		return armor != null && armor.getType() == leggings;
	}
	
	public static boolean bootIs(Player player, Material boots) {
		ItemStack armor = player.getInventory().getBoots();
		
		return armor != null && armor.getType() == boots;
	}
}

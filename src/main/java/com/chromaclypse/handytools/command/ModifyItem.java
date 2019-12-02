package com.chromaclypse.handytools.command;

import org.bukkit.Material;

import com.chromaclypse.api.command.Context;
import com.chromaclypse.api.item.ItemBuilder;

public class ModifyItem {
	private static final String RESET = "&r";
	
	public boolean name(Context context) {
		String name = context.SplatArgs(1);
		ItemBuilder builder = ItemBuilder.edit(context.GetHeld());
		
		if(!name.equals(RESET)) {
			builder.display(name);
		}
		else {
			builder.display(null);
		}
		
		return true;
	}
	
	public boolean lore(Context context) {
		String lore = context.SplatArgs(1);
		ItemBuilder builder = ItemBuilder.edit(context.GetHeld());
		
		if(!lore.equals(RESET)) {
			builder.wrapLore(lore);
		}
		else {
			builder.directLore(null);
		}
		
		return true;
	}
	
	public boolean type(Context context) {
		Material mat = context.GetMaterial(1);
		ItemBuilder builder = ItemBuilder.edit(context.GetHeld());
		
		builder.type(mat);
		
		return true;
	}
}

package com.chromaclypse.handytools;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class DummyBreakEvent extends BlockBreakEvent {
	public DummyBreakEvent(Block theBlock, Player player) {
		super(theBlock, player);
	}
	
	public boolean sendEvent() {
		ToolPlugin.instance.getNoCheat().exemptBlock(getPlayer().getUniqueId());
		Bukkit.getServer().getPluginManager().callEvent(this);
		ToolPlugin.instance.getNoCheat().unexemptBlock(getPlayer().getUniqueId());
		return !isCancelled();
	}
}

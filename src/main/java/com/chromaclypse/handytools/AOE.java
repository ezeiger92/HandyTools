package com.chromaclypse.handytools;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AOE {
	private static void dig(BlockState blockSource, Player player, Iterator<Vector> offsets, int ticksPerCall, int blocksPerCall) {
		if(!player.isOnline() || blocksPerCall < 1)
			return;
		
		ItemStack hand = player.getInventory().getItemInMainHand();
		
		for(int i = 0; i < blocksPerCall; ++i) {
			Block b;
			do {
				if(!offsets.hasNext())
					return;
				
				Vector offset = offsets.next();
				b = blockSource.getBlock().getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
			}
			while(!b.getDrops(hand).equals(blockSource.getBlock().getDrops(hand)));
			
			if(b.getLocation().equals(blockSource.getLocation()))
				continue;
			
			DummyBreakEvent dummy = new DummyBreakEvent(b, player);
			
			if(!dummy.sendEvent())
				return;
			
			if(dummy.isDropItems()) 
				b.breakNaturally();
			else
				b.setType(Material.AIR);
		}
		
		if(ticksPerCall > 0)
			Bukkit.getScheduler().scheduleSyncDelayedTask(ToolPlugin.instance, () -> {
				dig(blockSource, player, offsets, ticksPerCall, blocksPerCall);
			}, ticksPerCall);
		else
			dig(blockSource, player, offsets, ticksPerCall, blocksPerCall);
	}
	
	public static void dig(Block blockSource, Player player, int depth, int width, int height, int ticksPerCall, int blocksPerCall) {
		dig(blockSource, player, depth, width, height, ticksPerCall, blocksPerCall,
				Util.makeBasis(player.getLocation().getYaw(), player.getLocation().getPitch()));
	}
	
	public static void dig(Block blockSource, Player player, int depth, int width, int height, int ticksPerCall, int blocksPerCall, Vector[] basis) {
		int halfWidth = (width - 1) / 2;
		int halfHeight = (height - 1) / 2;

		Vector direction = basis[0];
		Vector right = basis[1];
		Vector up = basis[2];
		
		ArrayList<Vector> offsets = new ArrayList<>(depth * width * height);
		
		for(int d = 0; d < depth; ++d) {
			Vector depthVec = direction.clone().multiply(d);
			for(int h = -halfHeight; h <= halfHeight; ++h) {
				Vector heightVec = up.clone().multiply(h).add(depthVec);
				for(int w = -halfWidth; w <= halfWidth; ++w)
					offsets.add(right.clone().multiply(w).add(heightVec));
			}
		}
		dig(blockSource.getState(), player, offsets.iterator(), ticksPerCall, blocksPerCall);
	}
}

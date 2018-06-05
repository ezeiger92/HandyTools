package com.chromaclypse.handytools;

import java.util.UUID;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class NCPCompat {
	private boolean enabled = false;
	public void init() {
		enabled = true;
	}
	
	public void exemptBlock(UUID uuid) {
		if(!enabled)
			return;
		
		NCPExemptionManager.exemptPermanently(uuid, CheckType.BLOCKBREAK);
	}
	
	public void unexemptBlock(UUID uuid) {
		if(!enabled)
			return;
		
		NCPExemptionManager.unexempt(uuid, CheckType.BLOCKBREAK);
	}
}

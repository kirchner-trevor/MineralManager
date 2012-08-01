package me.hellfire212.MineralManager;

import java.util.Map.Entry;

public class UpdateThread implements Runnable {

	private final MineralManager plugin;
	
	public UpdateThread(MineralManager plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		for(Entry<Coordinate, BlockInfo> entry : plugin.blockMap.entrySet()) {
			BlockInfo info = entry.getValue();
			info.setCooldown((info.getRespawn() - System.currentTimeMillis()) / 1000);
			plugin.blockMap.put(entry.getKey(), info);
		}
	}
}

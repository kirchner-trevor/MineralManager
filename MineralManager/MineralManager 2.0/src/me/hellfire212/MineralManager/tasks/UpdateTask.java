package me.hellfire212.MineralManager.tasks;

import java.util.Map.Entry;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MineralManager;

public class UpdateTask implements Runnable {

	private final MineralManager plugin;
	
	public UpdateTask(MineralManager plugin) {
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

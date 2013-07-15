package me.hellfire212.MineralManager.tasks;

import java.util.ArrayList;
import java.util.Map.Entry;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MMConstants;
import me.hellfire212.MineralManager.MineralListener;
import me.hellfire212.MineralManager.MineralManager;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class EnableListenersTask implements Runnable {
	private MineralManager plugin;
	private ArrayList<Entry<Coordinate, BlockInfo>> blockEntryList;
	private int currentIndex = 0;
	private int waiting = 0;
	private final boolean debug;

	public EnableListenersTask(MineralManager mineralManager) {
		plugin = mineralManager;
		this.blockEntryList = new ArrayList<Entry<Coordinate, BlockInfo>>(plugin.getActiveBlocks().all());
		debug = plugin.getConfig().getBoolean("debug.task");
		if (debug) {
	        plugin.getLogger().info("Enable listeners: " + blockEntryList.size());
		}

	}

	@Override
	public void run() {
		if (currentIndex >= blockEntryList.size()) {
		    plugin.finishEnablingListeners();
			blockEntryList = null;
			plugin = null;
			return;
		}
		Server server = plugin.getServer();
		Entry<Coordinate, BlockInfo> entry = blockEntryList.get(currentIndex);
		
		Coordinate coordinate = entry.getKey();
		BlockInfo info = entry.getValue();
	
		// If we have Multiverse, we want to wait for the world to load.
		Plugin multiverse = server.getPluginManager().getPlugin(MMConstants.MULTIVERSE);
			
		if(multiverse != null && coordinate.getWorld() == null) {
			if (++waiting > 50) {
				plugin.getLogger().severe(String.format(
						"Was not able to get world '%s' before deadline", 
						coordinate.getWorldName()
				));
				waiting = 0;
				currentIndex++;
			}
			// 4 ticks is 200 milliseconds
			server.getScheduler().scheduleSyncDelayedTask(plugin, this, 4);
			return;
		}
		waiting = 0;
		if (debug) {
		    plugin.getLogger().info("Getting task for block at " + coordinate.toString() + ", expected block " + info.getBlockTypeId());
		}
		coordinate.getLocation().getBlock().setTypeIdAndData(info.getPlaceholderTypeId(), (byte) info.getPlaceholderData(), false);
		int tid = server.getScheduler().scheduleSyncDelayedTask(plugin, new RespawnTask(plugin, coordinate, info), info.getCooldown() * 20);
		MineralListener.taskMap.put(coordinate, tid);
		currentIndex++;
		long delay = (currentIndex > 10)? 1 : 0;
		server.getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
	}
}
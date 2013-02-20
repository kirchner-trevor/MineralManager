package me.hellfire212.MineralManager.tasks;

import java.util.ArrayList;
import java.util.Map.Entry;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MMConstants;
import me.hellfire212.MineralManager.MineralListener;
import me.hellfire212.MineralManager.MineralManager;
import me.hellfire212.MineralManager.BlockInfo.Type;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class EnableListenersTask implements Runnable {
	private MineralManager plugin;
	private ArrayList<Entry<Coordinate, BlockInfo>> blockEntryList;
	private int currentIndex = 0;
	private int waiting = 0;

	public EnableListenersTask(MineralManager mineralManager) {
		plugin = mineralManager;
		this.blockEntryList = new ArrayList<Entry<Coordinate, BlockInfo>>(plugin.blockMap.entrySet());
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
			
		coordinate.getLocation().getBlock().setTypeIdAndData(info.getTypeId(Type.PLACEHOLDER), (byte) info.getData(Type.PLACEHOLDER), false);
		int tid = server.getScheduler().scheduleSyncDelayedTask(plugin, new RespawnTask(plugin, coordinate, info), info.getCooldown() * 20);
		MineralListener.taskMap.put(coordinate, tid);
		currentIndex++;
		server.getScheduler().scheduleSyncDelayedTask(plugin, this);
	}
}
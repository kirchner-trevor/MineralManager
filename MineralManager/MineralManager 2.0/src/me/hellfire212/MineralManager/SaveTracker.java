package me.hellfire212.MineralManager;

import java.util.ArrayList;

import me.hellfire212.MineralManager.utils.Saveable;

/**
 * Track things that need to be saved.
 * @author james
 *
 */
public final class SaveTracker implements Runnable {
	private static ArrayList<Saveable> tracked = new ArrayList<Saveable>();
	
	private int position = 0;
	private int timeBudget;
	private MineralManager plugin;
	private final boolean debugMode;
	
	public SaveTracker(MineralManager plugin, int timeBudget) {
		this.plugin = plugin;
		this.timeBudget = timeBudget;
		this.debugMode = plugin.getConfig().getBoolean("debug.saver");
	}
	
	@Override
	public void run() {
		boolean saved = tracked.get(position).save(false);
		if (saved && debugMode) {
			plugin.getLogger().info("Saved " + tracked.get(position).getClass().toString());
		}
		position = (position + 1) % tracked.size();
		int ticks = Math.max(2, timeBudget / tracked.size());
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, ticks);
	}
	
	/* Static interface */
	public static void track(Saveable trackable) {
		if (!tracked.contains(trackable)) {
			tracked.add(trackable);
		}
	}
	
}

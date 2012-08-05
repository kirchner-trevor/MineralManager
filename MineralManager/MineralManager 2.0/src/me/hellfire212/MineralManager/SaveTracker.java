package me.hellfire212.MineralManager;

import java.util.ArrayList;

import me.hellfire212.MineralManager.utils.Saveable;

/**
 * Track things that need to be saved.
 * 
 * This is a self-scheduling repeating task that will split up all the
 * things to be saved across a time budget, thus spreading out the IO where
 * possible.
 *
 */
public final class SaveTracker implements Runnable {
	private static ArrayList<Saveable> tracked = new ArrayList<Saveable>();
	
	private int position = 0;
	private int timeBudget;
	private MineralManager plugin;
	private final boolean debugMode;
	
	/**
	 * Create a new SaveTracker.
	 * @param plugin MineralManager instance.
	 * @param timeBudget How long, in ticks, that the entire rotation ought to take.
	 */
	public SaveTracker(MineralManager plugin, int timeBudget) {
		this.plugin = plugin;
		this.timeBudget = timeBudget;
		this.debugMode = plugin.getConfig().getBoolean("debug.saver");
	}
	
	/** The meat of the saving, run every timeBudget / n ticks. */
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
	
	/** Shut down this tracker. */
	public void shutdown() {
		for (Saveable candidate : tracked) {
			candidate.save(false);
		}
		tracked.clear();
		plugin = null;
	}
	
	/* Static interface */
	/** 
	 * Track something that can be saved.
	 * @param trackable Some Saveable object
	 */
	public static void track(Saveable trackable) {
		if (!tracked.contains(trackable)) {
			tracked.add(trackable);
		}
	}
	
}

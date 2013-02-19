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
		doSave(tracked.get(position));
		position = (position + 1) % tracked.size();
		int ticks = Math.max(2, timeBudget / tracked.size());
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, ticks);
	}
	
	private boolean doSave(Saveable obj) {
		boolean saved = obj.save(false);
		if (saved && debugMode) {
			String info = obj.toString();
			if (!info.contains(obj.getClass().toString())) {
				info = obj.getClass().toString() + ": " + info;
			}
			plugin.getLogger().info("Saved " + info);
		}
		return saved;
	}
	
	/** Shut down this tracker. */
	public void shutdown() {
		if (debugMode && plugin != null) {
			plugin.getLogger().info("Beginning saver shutdown....");
		}
		for (Saveable candidate : tracked) {
			doSave(candidate);
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

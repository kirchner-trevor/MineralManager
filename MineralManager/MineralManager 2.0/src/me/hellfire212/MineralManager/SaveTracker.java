package me.hellfire212.MineralManager;

import java.util.ArrayList;

import me.hellfire212.MineralManager.utils.Saveable;

/**
 * Track things that need to be saved.
 * @author james
 *
 */
public final class SaveTracker implements Runnable{
	private static ArrayList<Saveable> tracked = new ArrayList<Saveable>();
	
	private int position = 0;
	
	public SaveTracker() {
		
	}
	
	@Override
	public void run() {
		tracked.get(position).save(false);
		position = (position + 1) % tracked.size();
	}
	
	/* Static interface */
	public static void track(Saveable trackable) {
		if (!tracked.contains(trackable)) {
			tracked.add(trackable);
		}
	}
	
}

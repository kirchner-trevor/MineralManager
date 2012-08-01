package me.hellfire212.mineralmanager;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MineralManager extends JavaPlugin {
	
	public static Map<Coordinate, Long> active = Collections.synchronizedMap(new HashMap<Coordinate, Long>());
	public static Set<Coordinate> locked = Collections.synchronizedSet(new HashSet<Coordinate>());
	public static Set<Coordinate> placed = Collections.synchronizedSet(new HashSet<Coordinate>());
	private String activePath = "plugins/MineralManager/bin/activeBlocks.bin";
	private String lockedPath = "plugins/MineralManager/bin/lockedBlocks.bin";
	private String placedPath = "plugins/MineralManager/bin/placedBlocks.bin";
	private FileHandler activeHandler = null;
	private FileHandler lockedHandler = null;
	private FileHandler placedHandler = null;

	@Override
	public void onLoad() {
		loadFiles();
	}
	
	@Override
	public void onEnable() {
		
	}
	
	@Override
	public void onDisable() {

	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		return false;
	}

	private void loadFiles() {
		long time = System.currentTimeMillis();
		try {
			activeHandler = new FileHandler(new File(activePath));
			lockedHandler = new FileHandler(new File(lockedPath));
			placedHandler = new FileHandler(new File(placedPath));
		} catch (IllegalArgumentException e) {}
		
		Thread activeThread = new Thread(new ActiveBlockLoader(activeHandler));
		Thread lockedThread = new Thread(new LockedBlockLoader(lockedHandler));
		Thread placedThread = new Thread(new PlacedBlockLoader(placedHandler));
		
		activeThread.start();
		lockedThread.start();
		placedThread.start();
		
		try {
			activeThread.join();
			lockedThread.join();
			placedThread.join();
		} catch (InterruptedException e1) {}
		System.out.println("File load took " + (System.currentTimeMillis() - time) + "ms.");
	}
	
	private class ActiveBlockLoader implements Runnable {
		
		private FileHandler handler = null;
		
		public ActiveBlockLoader(FileHandler fh) {
			handler = fh;
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			Map<?, ?> temp = handler.loadObject(MineralManager.active.getClass());
			MineralManager.active = (Map<Coordinate, Long>) temp;
		}
	}
	
	private class LockedBlockLoader implements Runnable {
		
		private FileHandler handler = null;
		
		public LockedBlockLoader(FileHandler fh) {
			handler = fh;
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			Set<?> temp = handler.loadObject(MineralManager.locked.getClass());
			MineralManager.locked = (Set<Coordinate>) temp;
		}
	}
	
	private class PlacedBlockLoader implements Runnable {
		
		private FileHandler handler = null;
		
		public PlacedBlockLoader(FileHandler fh) {
			handler = fh;
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			Set<?> temp = handler.loadObject(MineralManager.placed.getClass());
			MineralManager.placed = (Set<Coordinate>) temp;
		}
	}
	
}

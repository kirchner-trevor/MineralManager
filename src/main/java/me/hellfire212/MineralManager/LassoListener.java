package me.hellfire212.MineralManager;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class LassoListener implements Listener {
	private JavaPlugin plugin = null;
	private AtomicInteger counter = null;
	
	public LassoListener(JavaPlugin p) {
		plugin = p;
		counter = new AtomicInteger(0);
	}
	
	//Need to use this whenever we add a listener.
	public void add() {
		if(counter.getAndIncrement() == 0) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}
	
	//Need to use this whenever we finish listening.
	public void remove() {
		if(counter.decrementAndGet() == 0) {
			HandlerList.unregisterAll(this);
		}
	}
	
	/**
	 * Shut down this listener, cleaning up all resources we need to.
	 */
	public void shutdown() {
		plugin = null;
		HandlerList.unregisterAll(this);
	}

	@EventHandler
	public void onPlayerMove(final PlayerMoveEvent e) {
		Player player = e.getPlayer();
		ArrayList<Coordinate> boundaries = Commands.lassoCoordinateMap.get(player.getName());
		if(boundaries != null) {
			Block from = e.getFrom().getBlock();
			Block to = e.getTo().getBlock();
			if(!from.equals(to)) {
				Coordinate c = new Coordinate(to.getLocation());
				if (boundaries.size() == 0 || !c.equals(boundaries.get(boundaries.size()-1))) {
					boundaries.add(c);
				}
			}
		}
	}
}

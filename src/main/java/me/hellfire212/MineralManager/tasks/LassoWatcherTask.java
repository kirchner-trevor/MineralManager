package me.hellfire212.MineralManager.tasks;

import java.util.List;

import org.bukkit.entity.Player;

import me.hellfire212.MineralManager.Commands;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MineralManager;
import mondocommand.ChatMagic;

public class LassoWatcherTask implements Runnable {
	private static final long delay = 20L * 2L;
	private final String player;
	private int knownPoints = 0;

	public LassoWatcherTask(String player) {
		this.player = player;
	}

	@Override
	public void run() {
		List<Coordinate> coords = Commands.lassoCoordinateMap.get(this.player);
		if (coords == null) return;
		
		MineralManager plugin = MineralManager.getInstance();
		if (coords.size() != knownPoints) {
			Player player = plugin.getServer().getPlayer(this.player);
			if (player == null) return;
			ChatMagic.send(player, " {AQUA} -> {TEXT} Selected {VERB}%d{TEXT} points" , coords.size());
			knownPoints = coords.size();
		}
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, delay);
	}

}

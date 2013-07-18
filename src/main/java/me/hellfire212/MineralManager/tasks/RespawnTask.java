package me.hellfire212.MineralManager.tasks;

import me.hellfire212.MineralManager.*;
import org.bukkit.Location;

public class RespawnTask implements Runnable {

	private final Coordinate coordinate;
	private final BlockInfo info;
	private final MineralManager plugin;
	
	/**
	 * Sets the block at coordinate to the Block given by info.
	 * @param coordinate the coordinate of the block to change
	 * @param info the BlockInfo of the Block
	 */
	public RespawnTask(MineralManager plugin, Coordinate coordinate, BlockInfo info) {
		this.plugin = plugin;
		this.coordinate = coordinate;
		this.info = info;
	}
	
	@Override
	public void run() {
        Location location = coordinate.getLocation();
		location.getBlock().setTypeIdAndData(info.getBlockTypeId(), (byte) info.getBlockData(), false);
		plugin.getActiveBlocks().remove(coordinate);
        plugin.getWorldData(location.getWorld()).getPlacedBlocks().unset(coordinate);
		MineralListener.taskMap.remove(coordinate);
	}
}

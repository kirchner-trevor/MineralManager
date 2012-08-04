package me.hellfire212.MineralManager.tasks;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MineralManager;
import me.hellfire212.MineralManager.BlockInfo.Type;

public class PlaceholderTask implements Runnable {

	private final Coordinate coordinate;
	private final BlockInfo info;
	private final MineralManager plugin;

	/**
	 * Sets the block at coordinate to the Place holder given by info.
	 * @param coordinate the coordinate of the block to change
	 * @param info the BlockInfo of the place holder
	 */
	public PlaceholderTask(MineralManager plugin, Coordinate coordinate, BlockInfo info) {
		this.plugin = plugin;
		this.coordinate = coordinate;
		this.info = info;
	}
	
	@Override
	public void run() {
		coordinate.getLocation().getBlock().setTypeIdAndData(info.getTypeId(Type.PLACEHOLDER), (byte) info.getData(Type.PLACEHOLDER), false);
		plugin.blockMap.put(coordinate, info);
		if(plugin.blockMapFH != null) {
			plugin.blockMapFH.flagDirty();
		}
	}
}

package me.hellfire212.MineralManager.tasks;

import me.hellfire212.MineralManager.BlockInfo;
import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.MineralListener;
import me.hellfire212.MineralManager.MineralManager;
import me.hellfire212.MineralManager.BlockInfo.Type;

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
		coordinate.getLocation().getBlock().setTypeIdAndData(info.getTypeId(Type.BLOCK), (byte) info.getData(Type.BLOCK), false);
		plugin.blockMap.remove(coordinate);
		MineralListener.taskMap.remove(coordinate);
		if(plugin.blockMapFH != null) {
			plugin.blockMapFH.flagDirty();
		}
	}
}

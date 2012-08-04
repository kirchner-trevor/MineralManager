package me.hellfire212.MineralManager;

import me.hellfire212.MineralManager.BlockInfo.Type;

public class PlaceholderThread implements Runnable {

	private final Coordinate coordinate;
	private final BlockInfo info;
	private final MineralManager plugin;

	/**
	 * Sets the block at coordinate to the Place holder given by info.
	 * @param coordinate the coordinate of the block to change
	 * @param info the BlockInfo of the place holder
	 */
	public PlaceholderThread(MineralManager plugin, Coordinate coordinate, BlockInfo info) {
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

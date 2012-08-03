package me.hellfire212.MineralManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Convert data from older versions of MineralManager to the latest format(s).
 * @author James Crasta
 *
 */
public class Upgrader {

	/**
	 * Convert the old serialized-format of the placed blocks to the new bitmap format, separated by world.
	 * @param plugin The instance of the plugin.
	 * @param placedSetFile the location of the old placedSet file
	 */
	@SuppressWarnings("unchecked")
	public static void convertPlaced(MineralManager plugin, File placedSetFile) {
		Set<Coordinate> placedSet =  Collections.synchronizedSet(new HashSet<Coordinate>());
		FileHandler placedSetFH = new FileHandler(placedSetFile);
		try {
			placedSet = placedSetFH.loadObject(placedSet.getClass());
		} catch (FileNotFoundException e) {}
		
		// Get the world data for each item, and set the placed blocks for each.
		for (Coordinate coord: placedSet) {
			WorldData wdata = plugin.getWorldData(coord.getWorld());
			wdata.getPlacedBlocks().set(coord, true);
		}
	
	}

}

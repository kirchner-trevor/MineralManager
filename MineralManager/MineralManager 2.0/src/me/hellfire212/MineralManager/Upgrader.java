package me.hellfire212.MineralManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;


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
		plugin.getLogger().info("Beginning conversion of placed blocks to new format....");
		Set<Coordinate> placedSet =  Collections.synchronizedSet(new HashSet<Coordinate>());
		FileHandler placedSetFH = new FileHandler(placedSetFile);
		try {
			placedSet = placedSetFH.loadObject(placedSet.getClass());
		} catch (FileNotFoundException e) {
			return;
		}
		
		// Get the world data for each item, and set the placed blocks for each.
		for (Coordinate coord: placedSet) {
			WorldData wdata = plugin.getWorldData(coord.getWorld());
			wdata.getPlacedBlocks().set(coord, true);
		}
		plugin.getLogger().info(" -> Completed setting. Now saving...");
		for (WorldData wdata : plugin.allWorldDatas()) {
			wdata.getPlacedBlocks().flush();
		}
		plugin.getLogger().info(" -> Saved.");

		// Prevent doing the conversion again in the future.
		File backupFile = new File(placedSetFile.getAbsolutePath() + ".old");
		
		if (placedSetFile.renameTo(backupFile)) {
			plugin.getLogger().info("Renamed file to " + backupFile.getAbsolutePath());
		} else {
			plugin.getLogger().severe(String.format(
					"Could not rename file '%s' to '%s'", 
					placedSetFile.getAbsolutePath(), 
					backupFile.getAbsolutePath()
			));
		}
	
	}
	
	/** Temporary: we want to see what regions look like in yaml-land. */
	public static void convertRegions(MineralManager plugin, File regionSetFile, File regionYamlFile, RegionSet regionSet) {
		plugin.getLogger().info("Beginning conversion of regions to new format....");
		ArrayList<Region> regions = new ArrayList<Region>();
		for (Region region : regionSet) {
			regions.add(region);
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(regionYamlFile);
		config.set("regions", regions);
		try {
			config.save(regionYamlFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

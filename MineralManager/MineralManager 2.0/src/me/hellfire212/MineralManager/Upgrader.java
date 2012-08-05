package me.hellfire212.MineralManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;


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
		renameOld(plugin, placedSetFile, ".old");
	
	}
	
	/**
	 * Convert old binary FileHandler regions to the new yaml-based format.
	 * @param plugin The MineralManager plugin instance
	 * @param regionSetFile the File where this region set is stored.
	 */
	public static void convertRegions(MineralManager plugin, File regionSetFile) {
		plugin.getLogger().info("Beginning conversion of regions to new format....");

		RegionSet regionSet = new RegionSet();
		FileHandler regionSetFH = new FileHandler(regionSetFile);
		try {
			regionSet = regionSetFH.loadObject(regionSet.getClass());
		} catch (FileNotFoundException e) {}
		
		for (Region region : regionSet) {
			String configName = region.getConfiguration().getName();
			Configuration map_config = plugin.getConfigurationMap().get(configName);
			if (map_config != null) {
				region.setConfiguration(map_config);
			}
			plugin.getLogger().info("  -> " + region.getName());
			World world = Bukkit.getWorld(region.getWorldUUID());
			WorldData wd = plugin.getWorldData(world);
			wd.getRegionSet().add(region);
			wd.flagRegionSetDirty();
		}
		renameOld(plugin, regionSetFile, ".old");
		plugin.getLogger().info("Finished.");
	}
	
	/** 
	 * Make the File object for a backup file
	 * @param orig The original File object
	 * @param addon What to add to the filename, e.g. ".old"
	 * @return a new File object
	 */
	private static File makeBackupFile(File orig, String addon) {
		return new File(orig.getAbsolutePath() + addon);
	}
	
	/**
	 * Rename an old file, with logging output
	 * @param plugin An instance of MineralManager (to get logger)
	 * @param orig Original File object
	 * @param addon What to add to the filename, e.g. ".old"
	 */
	private static void renameOld(MineralManager plugin, File orig, String addon) {
		File backupFile = makeBackupFile(orig, ".old");

		if (orig.renameTo(backupFile)) {
			plugin.getLogger().info("Renamed file to " + backupFile.getAbsolutePath());
		} else {
			plugin.getLogger().severe(String.format(
					"Could not rename file '%s' to '%s'", 
					orig.getAbsolutePath(), 
					backupFile.getAbsolutePath()
			));
		}
	}

}

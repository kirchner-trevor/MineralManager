package me.hellfire212.MineralManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import me.hellfire212.MineralManager.datastructures.ActiveBlockMap;
import me.hellfire212.MineralManager.datastructures.BitmapChoice;
import me.hellfire212.MineralManager.utils.GenericUtil;

/**
 * Convert data from older versions of MineralManager to the latest format(s).
 * @author James Crasta
 *
 */
public final class Upgrader {

	/**
	 * Convert the old serialized-format of the placed blocks to the new bitmap format, separated by world.
	 * @param plugin The instance of the plugin.
	 * @param placedSetFile the location of the old placedSet file
	 */
	public static void convertPlaced(MineralManager plugin, File placedSetFile) {
		plugin.getLogger().info("Beginning conversion of placed blocks to new format....");
		convertCoordinateBitmap(plugin, placedSetFile, BitmapChoice.PLACED_BLOCKS);
	}
	
	/**
	 * Convert the old serialized-format of the locked blocks to the new bitmap format, separated by world.
	 * @param plugin The instance of the plugin.
	 * @param lockedSetFile the location of the old lockedSet file
	 */
	public static void convertLocked(MineralManager plugin, File lockedSetFile) {
		plugin.getLogger().info("Beginning conversion of locked blocks to new format....");
		convertCoordinateBitmap(plugin, lockedSetFile, BitmapChoice.LOCKED_BLOCKS);
	}

	private static void convertCoordinateBitmap(MineralManager plugin, File coordinateSetFile, BitmapChoice choice) {
		Set<Coordinate> coordinateSet = loadCoordinateSet(coordinateSetFile);
		
		// Get the world data for each item, and set the placed blocks for each.
		for (Coordinate coord: coordinateSet) {
			WorldData wdata = plugin.getWorldData(coord.getWorld());
			wdata.getBitmapData(choice).set(coord, true);
		}
		plugin.getLogger().info(" -> Completed setting. Now saving...");
		for (WorldData wdata : plugin.allWorldDatas()) {
			wdata.getBitmapData(choice).flush();
		}
		plugin.getLogger().info(" -> Saved.");

		// Prevent doing the conversion again in the future.
		renameOld(plugin, coordinateSetFile, ".old");
	}

	@SuppressWarnings("unchecked")
	private static Set<Coordinate> loadCoordinateSet(File coordinateSetFile) {
		Set<Coordinate> coordSet =  Collections.synchronizedSet(new HashSet<Coordinate>());
		FileHandler placedSetFH = new FileHandler(coordinateSetFile);
		try {
			coordSet = placedSetFH.loadObject(coordSet.getClass());
		} catch (FileNotFoundException e) {
			return null;
		}
		return coordSet;
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

    public static void convertBlockMap(MineralManager plugin, File blockMapFile) {
        plugin.getLogger().info("Converting MM 2.1 blockMap...");
        Map<Coordinate, BlockInfo> blockMap = new ConcurrentHashMap<Coordinate, BlockInfo>();
        FileHandler blockMapFH = new FileHandler(blockMapFile);
        try {
            blockMap =  GenericUtil.cast(blockMapFH.loadObject(blockMap.getClass()));
        } catch (FileNotFoundException e) {}
        ActiveBlockMap activeBlocks = plugin.getActiveBlocks();
        for (Map.Entry<Coordinate, BlockInfo> e: blockMap.entrySet()) {
            activeBlocks.add(e.getKey(), e.getValue());
        }
        renameOld(plugin, blockMapFile, ".old");
    }

}

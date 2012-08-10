package me.hellfire212.MineralManager;

import java.awt.geom.Point2D.Double;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.hellfire212.MineralManager.tasks.RespawnTask;
import me.hellfire212.MineralVein.MM13Loader;
import me.hellfire212.MineralVein.NoData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;


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
	
	public static void convertMM13(MineralManager plugin, File dir) {
		MM13Loader loader = new MM13Loader(dir, plugin.getLogger());
		
		// Find candidate for the world as a region
		World candidate = null;
		for (World w : Bukkit.getWorlds()) {
			if (w.getEnvironment() == Environment.NORMAL) {
				candidate = w;
				break;
			}
		}
		
		// Convert regions if possible
		convertMM13Regions(plugin, loader, candidate);
		convertMM13Locked(plugin, loader);
		convertMM13Placed(plugin, loader);
		convertMM13Active(plugin, loader);
	}

	private static void convertMM13Active(MineralManager plugin,
			MM13Loader loader) {
		try {
			int i = 0;
			for (me.hellfire212.MineralVein.SBlock sb : loader.getActiveBlocks()) {
				World w = Bukkit.getWorld(sb.getWorld());
				Material m = Material.matchMaterial(sb.getMaterial());
				Coordinate coord = new Coordinate(new Location(w, sb.getX(), sb.getY(), sb.getZ()));
				BlockInfo info = new BlockInfo(BlockInfo.Type.BLOCK, m.getId(), 0);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RespawnTask(plugin, coord, info), i++);
			}
		} catch (NoData e) {
			plugin.getLogger().warning(e.getMessage());
			e.printStackTrace();
		}
		
	}

	private static void convertMM13Placed(MineralManager plugin, MM13Loader loader) {
		try {
			for (Location loc: loader.getPlacedBlocks()) {
				plugin.getWorldData(loc.getWorld()).getPlacedBlocks().set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), true);
			}
		} catch (NoData e) {
			// TODO Auto-generated catch block
			plugin.getLogger().warning(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void convertMM13Regions(MineralManager plugin, MM13Loader loader, World candidate) {
		int level = 0;
		WorldData wd = plugin.getWorldData(candidate);
		try {
			for (me.hellfire212.MineralVein.Region r : loader.getRegions()) {
				int[] bits = r.getLocation();
				 // {x1, y1, z1, x2, y2, z2}
				double x1 = bits[0];
				double y1 = bits[1];
				double z1 = bits[2];
				double x2 = bits[3];
				double y2 = bits[4];
				double z2 = bits[5];
				ArrayList<Double> points = Tools.squareBoundaries(x1, z1, x2, z2);
				
				Region n = new Region(r.getName(), plugin.getDefaultConfiguration(), points , Math.min(y1, y2), Math.max(y1, y2), candidate, level);
				wd.getRegionSet().add(n);
				wd.flagRegionSetDirty();
				level += 1;
			}
		} catch (NoData e) {
			plugin.getLogger().warning(e.getMessage());
		}
	}
	
	private static void convertMM13Locked(MineralManager plugin, MM13Loader loader) {
		try {
			for (Location loc : loader.getLockedBlocks()) {
				plugin.lockedSet.add(new Coordinate(loc));
			}
		} catch (NoData e) {
			plugin.getLogger().warning(e.getMessage());
		}
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

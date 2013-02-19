package me.hellfire212.MineralManager;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.hellfire212.MineralManager.datastructures.BitmapChoice;
import me.hellfire212.MineralManager.tasks.RespawnTask;
import me.hellfire212.MineralManager.utils.ShapeUtils;
import me.hellfire212.MineralVein.MM13Loader;
import me.hellfire212.MineralVein.NoData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

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
		convertMM13Config(plugin, dir.getParentFile());
		
		// Convert regions if possible
		convertMM13Regions(plugin, loader, candidate);
		convertMM13Locked(plugin, loader);
		convertMM13Placed(plugin, loader);
		convertMM13Active(plugin, loader);
		
		renameOld(plugin, dir.getParentFile(), ".old");
	}

	private static void convertMM13Config(MineralManager plugin, File dir) {
		Map<String, Object> newSection = new HashMap<String, Object>();
		List<Object> managedBlocks = new ArrayList<Object>();
		File configFile = new File(dir, "config.yml");
		if (!configFile.exists()) return;
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		ConfigurationSection sec = config.getConfigurationSection("MineralVein");
		
		newSection.put("mineOriginalOnly", sec.getBoolean("MineralVein"));
		newSection.put("placeholder", sec.getString("Placeholder"));
		newSection.put("displayMessages", sec.get("DisplayMessages"));
		
		// Start doing managed blocks;
		List<Map<?, ?>> cdOrig = sec.getMapList("Cooldowns");
		List<Map<?, ?>> degOrig = sec.getMapList("Degrade");
		for (int i = 0; i < cdOrig.size(); i++) {
			
			Map.Entry<String, Number> cd = getFirstOnly(cdOrig.get(i));
			Map.Entry<String, Number> deg = getFirstOnly(degOrig.get(i));
			Map<String, Object> output = new HashMap<String, Object>();
			output.put("type", cd.getKey());
			output.put("cooldown", cd.getValue().intValue());
			output.put("degrade", deg.getValue().doubleValue());
			managedBlocks.add(output);
		}
		newSection.put("managedBlocks", managedBlocks);
		plugin.getConfig().set("CONFIGURATION.imported", newSection);
		plugin.saveConfig();
		plugin.reloadConfig();
		plugin.parseConfigurationValues();
	}

	@SuppressWarnings("unchecked")
	private static Map.Entry<String, Number> getFirstOnly(Map<?, ?> obj) {
		Map<String, Number> m = (Map<String, Number>) obj;
		for (Map.Entry<String, Number> e: m.entrySet()) {
			return e;
		}
		return null;
	}

	private static void convertMM13Active(MineralManager plugin, MM13Loader loader) {
		try {
			int i = 0;
			for (me.hellfire212.MineralVein.SBlock sb : loader.getActiveBlocks()) {
				World w = Bukkit.getWorld(sb.getWorld());
				Material m = Material.matchMaterial(sb.getMaterial());
				Coordinate coord = new Coordinate(new Location(w, sb.getX(), sb.getY(), sb.getZ()));
				BlockInfo info = new BlockInfo(BlockInfo.Type.BLOCK, m.getId(), 0);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RespawnTask(plugin, coord, info), (i++) * 3);
			}
			plugin.getLogger().info(String.format("-> Converted %d active blocks", i));
		} catch (NoData e) {
			plugin.getLogger().warning(e.getMessage());
		}
		
	}

	private static void convertMM13Placed(MineralManager plugin, MM13Loader loader) {
		try {
			int i = 0;
			for (Location loc: loader.getPlacedBlocks()) {
				i++;
				plugin.getWorldData(loc.getWorld()).getPlacedBlocks().set(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), true);
			}
			plugin.getLogger().info(String.format("-> Converted %d placed blocks", i));
		} catch (NoData e) {
			// TODO Auto-generated catch block
			plugin.getLogger().warning(e.getMessage());
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
				List<Point2D> points = Tools.squareBoundaries(x1, z1, x2, z2);
				java.awt.Shape shape = ShapeUtils.shapeFromBounds(points);
				
				Configuration conf = plugin.getConfigurationMap().get("imported");
				if (conf == null) conf = plugin.getDefaultConfiguration();
				
				Region n = new Region(r.getName(), conf, shape, Math.min(y1, y2), Math.max(y1, y2), candidate, level);
				boolean added = wd.getRegionSet().add(n);
				wd.flagRegionSetDirty();
				plugin.getLogger().info(String.format(" -> converted region %s at level %d, added=%s", r.getName(), level, (added? "yes" : "no")));
				level += 1;

			}
		} catch (NoData e) {
			plugin.getLogger().warning(e.getMessage());
		}
	}
	
	private static void convertMM13Locked(MineralManager plugin, MM13Loader loader) {
		try {
			for (Location loc : loader.getLockedBlocks()) {
				WorldData wdata = plugin.getWorldData(loc.getWorld());
				wdata.getLockedBlocks().set(new Coordinate(loc), true);
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

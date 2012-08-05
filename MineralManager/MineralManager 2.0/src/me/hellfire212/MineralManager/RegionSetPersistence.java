package me.hellfire212.MineralManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import me.hellfire212.MineralManager.utils.Saveable;

/**
 * This class is an interim solution to converting RegionSet to the YAML storage format.
 * 
 * Because changing the constructor or adding members to RegionSet itself will make
 * it binary incompatible with the old format, the current solution is to simply write
 * a companion class which manages its persistence.
 *
 */
public class RegionSetPersistence implements Saveable {
	private RegionSet regionSet;
	private File file;
	private boolean dirty = false;

	public RegionSetPersistence(RegionSet regionSet, File file) {
		this.regionSet = regionSet;
		this.file = file;
		if (file.exists()) load();
	}

	public void load() {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		List<?> regions = config.getList("regions");
		for (Object x: regions) {
			if (x instanceof Region) {
				regionSet.add((Region) x);
			}
		}
		
	}
	
	public void shutdown() {
		regionSet = null;
		file = null;
	}

	@Override
	public boolean save(boolean force) {
		if (!dirty  && !force) return false;
		
		ArrayList<Region> regions = new ArrayList<Region>();
		for (Region region : regionSet) {
			regions.add(region);
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		config.set("regions", regions);
		try {
			config.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		dirty = false;
		return true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void flagDirty() {
		this.dirty = true;
	}

}

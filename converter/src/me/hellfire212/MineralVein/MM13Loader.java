package me.hellfire212.MineralVein;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class MM13Loader {
	private File plugindir;
	private Logger log;

	public MM13Loader(File plugindir, Logger log) {
		this.log = log;
		this.plugindir = plugindir;
	}
	
	public Set<Location> getPlacedBlocks() throws NoData {
		log.info("Beginning placed blocks migration....");
		Map<SBlock, Integer> m = loadMap(new File(plugindir, "placedBlocks.bin"));
		if (m == null) {
			return null;
		}
		return asLocs(m);
	}
	
	public Set<Location> getLockedBlocks() throws NoData {
		Map<SBlock, Integer> m = loadMap(new File(plugindir, "lockedBlocks.bin"));
		if (m == null) {
			return null;
		}
		return asLocs(m);
	}
	
	public Set<SBlock> getActiveBlocks() throws NoData {
		Map<SBlock, Integer> m = loadMap(new File(plugindir, "activeBlocks.bin"));
		return m.keySet();
	}
	
	public Set<Region> getRegions() throws NoData {
		log.info("Beginning regions migration...");
		Object o = loadObject(new File(plugindir, "regions.bin"));
		if (o instanceof RegionSet) {
			Set<Region> regions = new HashSet<Region>();
			RegionSet rs = (RegionSet) o;
			for (String name : rs.getRegionNames()) {
				regions.add(rs.getRegion(name));
			}
			return regions;
		}
		return null;
	}

	private Set<Location> asLocs(Map<SBlock, Integer> m) {
		Set<Location> locs = new HashSet<Location>();
		for (SBlock s : m.keySet()) {
			locs.add(sbAsLoc(s));
		}
		return locs;
	}
	
	private Location sbAsLoc(SBlock s) {
		return new Location(Bukkit.getWorld(s.getWorld()), s.getX(), s.getY(), s.getZ());
	}


	public Object loadObject(File f) throws NoData {
	  if (!f.exists()) {
		  throw new NoData("File doesn't exist.");
	  }
	  try {
	    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
	    Object result = ois.readObject();
	    return result;
	  } catch (Exception e) {
	    e.printStackTrace();
	  }
	  return null;
	}
	
	public Map<SBlock, Integer> loadMap(File f) throws NoData {
		Object orig = loadObject(f);
		if (orig instanceof Map<?, ?>) {
			Map<?, ?> mo = (Map<?, ?>) orig;
			Map<SBlock, Integer> m = new HashMap<SBlock, Integer>();
			for (Map.Entry<?, ?> e : mo.entrySet()) {
				m.put((SBlock) e.getKey(), (Integer) e.getValue());
			}
			return m;
		} else {
			throw new NoData("Object is not a map, wtf");
		}
	}
}

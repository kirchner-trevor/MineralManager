package me.hellfire212.MineralManager;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public class Region implements Serializable, Comparable<Region>, ConfigurationSerializable {
	
	private static final long serialVersionUID = -2885326328430836535L;
	private String name = null;
	private ArrayList<Point2D.Double> boundaries = null;
	private Double floor = null;
	private Double ceil = null;
	private UUID world = null;
	private int level = 0;
	private boolean global = false;
	
	private Configuration configuration = new Configuration();
	
	/**
	 * Creates a polyprism with a given name and attributes.
	 * @param n the unique name of the region
	 * @param b an array of vertices that make up the bounding polygon
	 * @param f the lowest y coordinate of the region
	 * @param c the highest y coordinate of the region
	 * @param w the world in which the region resides
	 * @param l the level of the Region, higher levels are seen first
	 */
	public Region(String n, Configuration config, ArrayList<Point2D.Double> b, Double f, Double c, World w, int l) {
		name = n;
		configuration = config;
		boundaries = b;
		floor = f;
		ceil = c;
		world = w.getUID();
		level = l;
		// Region is global if boundaries are empty and floor/ceiling are both negative.
		// XXX Still contains old config global setting for conversion purposes
		global = config.isGlobal() || (b.size() == 0 && f < -0.9D && c < -0.9D);
	}

	/**
	 * Checks whether or not the given coordinate is within the region.
	 * @param coordinate the Coordinate to test
	 * @return true if the region contains the coordinate
	 */
	public boolean contains(Coordinate coordinate) {
		return (
			global
			|| (coordinate.getY() >= floor && coordinate.getY() <= ceil && coordinate.inPolygon(boundaries))
		);
	}
	
	/**
	 * Sets the regions configuration to the given configuration
	 * @param newConfiguration the new configuration
	 */
	public void setConfiguration(Configuration newConfiguration) {
		configuration = newConfiguration;
		// XXX legacy compatibility holdover
		if (newConfiguration.isGlobal()) global = true;
	}
	
	/**
	 * Returns the configuration of the Region.
	 * @return the configuration of the Region
	 */
	public Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Returns the level of the Region
	 * @return the level of the Region
	 */
	public int getLevel() {
		return level;
	}
	
	public UUID getWorldUUID() {
		return world;
	}
	
	/**
	 * Returns the name of the Region
	 * @return the name of the Region
	 */
	public String getName() {
		return name;
	}

	/** Explain what kind of region we are. */
	public String kind() {
		if (global) return "World";
		if (boundaries.size() == 7) {
			return "Cuboid";
		} else {
			return String.format("Polygon (%d points)", boundaries.size() - 2);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	
	@Override
	public int compareTo(Region r) {
		// Sort descending by level, higher levels go first.
		int levelTest = 0 - new Integer(level).compareTo(r.getLevel());
		// Fall back to the name test when levels are equal, for a stable sort.
		return (levelTest != 0) ? levelTest : name.compareTo(r.getName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Region other = (Region) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/** Used for serialization to bukkit configs.
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> values = new java.util.HashMap<String, Object>();
		values.put("name", name);
		values.put("configuration", configuration.getName());
		values.put("floor", floor);
		values.put("ceil", ceil);
		values.put("world", world.toString());
		values.put("level", level);
		if (global) values.put("global", global);
		ArrayList<java.lang.Double> condensedBoundaries = new ArrayList<Double>();
		for (Point2D.Double point : boundaries) {
			condensedBoundaries.add(point.getX());
			condensedBoundaries.add(point.getY());
		}
		values.put("boundaries", condensedBoundaries);
		return values;
	}
	
	/**
	 * De-serialize from a bukkit config.
	 * @param values provided map describing this Region.
	 * @return new Region instance.
	 */
	public static Region deserialize(Map<String, Object> values) {
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		java.lang.Double x = null;
		int i = 0;
		Object rawBoundaries = values.get("boundaries");
		if (rawBoundaries instanceof Collection<?>) {
			for (Object location: (Collection<?>) rawBoundaries) {
				if (location instanceof java.lang.Double) {
					if ((i++ % 2) == 0) {
						x = (java.lang.Double) location;
					} else {
						points.add(new Point2D.Double(x, (java.lang.Double) location));
					}
				} else {
					System.out.printf("Expected a Double, got a %s (printed %s)", location.getClass().toString(), location.toString());
				}
			}
		}
		
		// Deal with the configuration allowing default.
		MineralManager plugin = MineralManager.getInstance();
		String configName = (String) values.get("configuration");
		Configuration config;
		if (configName.equals("DEFAULT")) {
			config = plugin.getDefaultConfiguration();
		} else {
			config = plugin.getConfigurationMap().get(configName);
			if (config == null) config = plugin.getDefaultConfiguration();
		}
				
		return new Region(
				(String) values.get("name"),
				config,
				points,
				(Double) values.get("floor"),
				(Double) values.get("ceil"), 
				Bukkit.getWorld(UUID.fromString((String) values.get("world"))),
				((Number)values.get("level")).intValue()
		);
	}
}
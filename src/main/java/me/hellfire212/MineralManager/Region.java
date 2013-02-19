package me.hellfire212.MineralManager;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import me.hellfire212.MineralManager.utils.GenericUtil;
import me.hellfire212.MineralManager.utils.ShapeUtils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public class Region implements Serializable, Comparable<Region>, ConfigurationSerializable {
	
	private static final long serialVersionUID = -2885326328430836535L;
	private String name = null;
	private Double floor = null;
	private Double ceil = null;
	private UUID world = null;
	private int level = 0;
	private boolean global = false;
	private Shape shape;
	
	private Configuration configuration = new Configuration();
	
	/**
	 * Creates a polyprism with a given name and attributes.
	 * @param n the unique name of the region
	 * @param s a shape describing the x/z bounds of the region.
	 * @param f the lowest y coordinate of the region
	 * @param c the highest y coordinate of the region
	 * @param w the world in which the region resides
	 * @param l the level of the Region, higher levels are seen first
	 */
	public Region(String n, Configuration config, Shape s, Double f, Double c, World w, int l) {
		name = (n != null)? n : "";
		configuration = config;
		shape = s;
		floor = f;
		ceil = c;
		world = w.getUID();
		level = l;
		// Region is global if boundaries are empty and floor/ceiling are both negative.
		// XXX Still contains old config global setting for conversion purposes
		global = config.isGlobal() || (shape == null && f < -0.9D && c < -0.9D);
	}

	/**
	 * Checks whether or not the given coordinate is within the region.
	 * @param coordinate the Coordinate to test
	 * @return true if the region contains the coordinate
	 */
	public boolean contains(Coordinate coordinate) {
		return (
			global
			|| (coordinate.getY() >= floor && coordinate.getY() <= ceil && shape.contains(coordinate.getX(), coordinate.getZ()))
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
		return ShapeUtils.describeShape(shape);
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
		return name.hashCode();
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
		values.put("world", world.toString());
		values.put("level", level);
		if (global) {
			values.put("global", global);
		} else {
			values.put("shape", ShapeUtils.serializeShape(shape));
			values.put("floor", floor);
			values.put("ceil", ceil);
		}
		return values;
	}
	
	/**
	 * De-serialize from a bukkit config.
	 * @param values provided map describing this Region.
	 * @return new Region instance.
	 */
	public static Region deserialize(Map<String, Object> values) {
		boolean global = false;
		double ceil = -1D, floor = -1D;
		Configuration config;
		Shape shape = null;
		
		Object oglobal = values.get("global");
		if (oglobal != null && oglobal instanceof Boolean) {
			global = ((Boolean) oglobal).booleanValue();
		}

		if (!global) {
			ceil = (Double) values.get("ceil");
			floor = (Double) values.get("floor");
			ArrayList<Point2D> points = ShapeUtils.pointsFromCompactBounds(values.get("boundaries"));
			if (points == null) {
				Object oshape = values.get("shape");
				if (oshape instanceof Map<?, ?>) {
					Map<String, Object> shapeInfo = GenericUtil.cast(oshape);
					shape = ShapeUtils.deserializeShape(shapeInfo);
				}
			} else {
				shape = ShapeUtils.shapeFromBounds(GenericUtil.<ArrayList<Point2D>>cast(points));
			}
		}
		
		// Deal with the configuration allowing default.
		MineralManager plugin = MineralManager.getInstance();
		String configName = (String) values.get("configuration");
		if (configName.equals("DEFAULT")) {
			config = plugin.getDefaultConfiguration();
		} else {
			config = plugin.getConfigurationMap().get(configName);
			if (config == null) config = plugin.getDefaultConfiguration();
		}

		return new Region(
				(String) values.get("name"),
				config,
				shape,
				floor,
				ceil,
				Bukkit.getWorld(UUID.fromString((String) values.get("world"))),
				((Number)values.get("level")).intValue()
		);
	}
}
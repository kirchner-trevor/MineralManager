package me.hellfire212.MineralManager;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

import me.hellfire212.MineralManager.utils.DecoderRing;
import me.hellfire212.MineralManager.utils.GenericUtil;
import me.hellfire212.MineralManager.utils.ShapeUtils;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;


public final class Region implements Comparable<Region>, ConfigurationSerializable {
	private final String name;
	private final double floor;
	private final double ceil;
	private final int level;
	private boolean global;
	private final Shape shape;
	
	private Configuration configuration;
	
	/**
	 * Creates a region with a given name and attributes.
	 * @param name the unique name of the region
	 * @param config The Configuration we're using.
	 * @param shape a shape describing the x/z bounds of the region.
	 * @param floor the lowest y coordinate of the region
	 * @param ceil the highest y coordinate of the region
	 * @param world the world in which the region resides
	 * @param level the level of the Region, higher levels are seen first
	 */
	public Region(String name, Configuration config, Shape shape, double floor, 
	              double ceil, int level) {
	    Validate.notNull(name);
	    Validate.notNull(config);
		this.name = (name != null)? name : "";
		this.configuration = config;
		this.shape = shape;
		this.floor = floor;
		this.ceil = ceil;
		this.level = level;
		// Region is global if boundaries are empty and floor/ceiling are both negative.
		// XXX Still contains old config global setting for conversion purposes
		global = config.isGlobal() || (shape == null && floor < -0.9D && ceil < -0.9D);
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
	    Validate.notNull(newConfiguration);
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
		if (obj instanceof Region) {
    		Region other = (Region) obj;
    		return name.equals(other.getName());
		}
		return false;
	}

	/** Used for serialization to bukkit configs. */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> values = new java.util.HashMap<String, Object>();
		values.put("name", name);
		values.put("configuration", configuration.getName());
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
		boolean global = DecoderRing.decodeBool(values.get("global"), false);
		String name = DecoderRing.decodeString(values.get("name"), "");

		double ceil = -1D, floor = -1D;
		Configuration config;
		Shape shape = null;

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
				shape = ShapeUtils.shapeFromBounds(points);
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
				name,
				config,
				shape,
				floor,
				ceil,
				DecoderRing.decodeInt(values.get("level"), 0)
		);
	}
}
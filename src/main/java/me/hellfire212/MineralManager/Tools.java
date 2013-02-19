package me.hellfire212.MineralManager;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import me.hellfire212.MineralManager.utils.TimeFormat;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public final class Tools {
	
	//This class is used as a toolbox of sorts for static methods that don't really belong in any specific class.
	
	/**
	 * Attempts to parse an Object into an int. If the Object contains a single value,
	 * that value is returned if it is a valid Material id. If the Object contains two 
	 * values separated by a space, the first value is returned if it is a valid Material id,
	 * otherwise -1 is returned.
	 * @param obj
	 * @return
	 */
	public static int parseTypeId(Object obj) {
		if(obj instanceof String) {
			String[] info = ((String) obj).split(" ");
			Material tempMaterial = Material.getMaterial(info[0]);
			if(tempMaterial == null) {
				int tempInteger = tryParseInteger(info[0]);
				tempMaterial = tempInteger == -1 ? null : Material.getMaterial(tempInteger);
			}
			return tempMaterial == null ? -1 : tempMaterial.getId();
		} else if(obj instanceof Integer) {
			Material tempMaterial = Material.getMaterial((Integer) obj);
			return tempMaterial == null ? -1 : tempMaterial.getId();
		} else {
			return -1;
		}
	}
	
	/**
	 * Attempts to parse an Object into a byte. If the Object contains a single value, 0 is returned.
	 * If the Object contains two values separated by a space the second value is returned if it is 
	 * a valid byte, otherwise -1 is returned.
	 * @param obj
	 * @return
	 */
	public static byte parseTypeData(Object obj) {
		if(obj instanceof String) {
			String[] info = ((String) obj).split(" ");
			if(info.length < 2) {
				return 0;
			} else if(info.length > 2) {
				return -1;
			} else {
				return tryParseByte(info[1]);
			}
		} else {
			return 0;
		}
	}
	
	/**
	 * Attempts to parse an Object into an int. If the Object cannot be parsed into a
	 * valid int, then -1 is returned.
	 * @param obj
	 * @return
	 */
	public static int parseCooldown(Object obj) {
		if(obj instanceof Integer) {
			return (Integer) obj;
		} else if (obj instanceof CharSequence) {
			return TimeFormat.parse(((CharSequence) obj).toString());
		} else {
			return -1;
		}
	}
	
	/**
	 * Attempts to parse an Object into a double. If the Object cannot be parsed into a
	 * valid double, then -1 is returned.
	 * @param obj
	 * @return
	 */
	public static double parseDegrade(Object obj) {
		if(obj instanceof Double) {
			return (Double) obj;
		} else if(obj instanceof Integer) {
			return (Double) Double.parseDouble(obj.toString());
		} else {
			return -1;
		}
	}
	
	/**
	 * Attempts to parse a String into an int. If the String is not a valid int the method
	 * returns -1.
	 * @param string
	 * @return
	 */
	private static int tryParseInteger(String string) {
		Integer result = -1;
		try {
			result = Integer.parseInt(string);
		} catch (NumberFormatException e) {
			result = -1;
		}
		return result;
	}
	
	/**
	 * Attempts to parse a String into an byte. If the String is not a valid byte the method
	 * returns -1.
	 * @param string
	 * @return
	 */
	private static byte tryParseByte(String string) {
		Byte result = -1;
		try {
			result = Byte.parseByte(string);
		} catch (NumberFormatException e) {
			result = -1;
		}
		return result;
	}

	static ArrayList<Point2D> squareBoundaries(double x1, double z1, double x2, double z2) {
		ArrayList<Point2D> boundaries = new ArrayList<Point2D>(7);
		
		Point2D.Double zero = new Point2D.Double();
		Point2D.Double origin = new Point2D.Double(x1, z1);
	
		boundaries.add(zero);
		boundaries.add(origin);
		boundaries.add(new Point2D.Double(x1, z2));
		boundaries.add(new Point2D.Double(x2, z2));
		boundaries.add(new Point2D.Double(x2, z1));
		boundaries.add(origin);
		boundaries.add(zero);
		return boundaries;
	}

	public static String parseDispMessage(ConfigurationSection config, String key) {
		String message = config.getString(key);
		if (message == null) return null;
		if (message.equalsIgnoreCase("null") || message.equalsIgnoreCase("false")) {
			return null;
		}
		return message;
	}
}

package me.hellfire212.MineralManager;

import me.hellfire212.MineralManager.utils.TimeFormat;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public final class Tools {

    public static byte INVALID_PARSE = -7;
	//This class is used as a toolbox of sorts for static methods that don't really belong in any specific class.
	
	/**
	 * Attempts to parse an Object into an int. If the Object contains a single value,
	 * that value is returned if it is a valid Material id. If the Object contains two 
	 * values separated by a space, the first value is returned if it is a valid Material id,
	 * otherwise INVALID_PARSE is returned.
	 * @param obj
	 * @return
	 */
	public static int parseTypeId(Object obj) {
		if(obj instanceof String) {
			String[] info = ((String) obj).split(" ");
			Material tempMaterial = Material.getMaterial(info[0]);
			if(tempMaterial == null) {
				int tempInteger = tryParseInteger(info[0]);
				tempMaterial = tempInteger == INVALID_PARSE ? null : Material.getMaterial(tempInteger);
			}
			return tempMaterial == null ? INVALID_PARSE : tempMaterial.getId();
		} else if(obj instanceof Integer) {
			Material tempMaterial = Material.getMaterial((Integer) obj);
			return tempMaterial == null ? INVALID_PARSE : tempMaterial.getId();
		} else {
			return INVALID_PARSE;
		}
	}
	
	/**
	 * Attempts to parse an Object into a byte. If the Object contains a single value, -1 is returned.
	 * If the Object contains two values separated by a space the second value is returned if it is 
	 * a valid byte, otherwise INVALID_PARSE is returned.
	 * @param obj
	 * @return
	 */
	public static byte parseTypeData(Object obj) {
		if(obj instanceof String) {
			String[] info = ((String) obj).split(" ");
			if(info.length < 2) {
				return -1;
			} else if(info.length > 2) {
				return INVALID_PARSE;
			} else {
				return tryParseByte(info[1]);
			}
		} else {
			return -1; //Return wildcard as no data value is present if the object isn't a string
		}
	}
	
	/**
	 * Attempts to parse an Object into an int. If the Object cannot be parsed into a
	 * valid int, then INVALID_PARSE is returned.
	 * @param obj
	 * @return
	 */
	public static int parseCooldown(Object obj) {
		if(obj instanceof Integer) {
			return (Integer) obj;
		} else if (obj instanceof CharSequence) {
			return TimeFormat.parse(obj.toString());
		} else {
			return INVALID_PARSE;
		}
	}
	
	/**
	 * Attempts to parse an Object into a double. If the Object cannot be parsed into a
	 * valid double, then INVALID_PARSE is returned.
	 * @param obj
	 * @return
	 */
	public static double parseDegrade(Object obj) {
		if(obj instanceof Double) {
			return (Double) obj;
		} else if(obj instanceof Integer) {
			return Double.parseDouble(obj.toString());
		} else {
			return INVALID_PARSE;
		}
	}
	
	/**
	 * Attempts to parse a String into an int. If the String is not a valid int the method
	 * returns -1.
	 * @param string
	 * @return
	 */
	private static int tryParseInteger(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	/**
	 * Attempts to parse a String into an byte. If the String is not a valid byte the method
	 * returns -1.
	 * @param string
	 * @return
	 */
	private static byte tryParseByte(String string) {
		try {
			return Byte.parseByte(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static String parseDispMessage(ConfigurationSection config, String key) {
		String message = config.getString(key);
		if (message == null) return null;
		if (message.equalsIgnoreCase("null") || message.equalsIgnoreCase("false") || message.equalsIgnoreCase("none")) {
			return null;
		}
		return message;
	}
}

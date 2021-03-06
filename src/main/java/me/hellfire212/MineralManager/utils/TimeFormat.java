package me.hellfire212.MineralManager.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class TimeFormat {
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int HOURS_PER_DAY = 24;
	
	private static Map<Character, Integer> convLookup = buildConvLookup();

	/**
	 * Format a time in seconds to more "friendly" units.
	 * @param seconds Total number of seconds.
	 * @return A string like "5h40m" or "4m30s"
	 */
	public static String format(long seconds) {
		long minutes = seconds / SECONDS_PER_MINUTE;
		long hours = 0;
		if (minutes > MINUTES_PER_HOUR) {
			hours = minutes / MINUTES_PER_HOUR;
			minutes = minutes % MINUTES_PER_HOUR;
		}
		seconds = seconds % SECONDS_PER_MINUTE;
		return doFormat(hours, minutes, seconds);
	}

	/** Internal formatter for building time format. */
	private static String doFormat(long hours, long minutes, long seconds) {
		StringBuilder sb = new StringBuilder();
		if (hours != 0) {
			sb.append(hours);
			sb.append("h");
		}
		if (minutes != 0) {
			sb.append(minutes);
			sb.append("m");
		}
		if (seconds != 0 || sb.length() == 0) {
			sb.append(seconds);
			sb.append("s");
		}
		return sb.toString();
	}
	
	/** 
	 * Parse a string containing a time format to integer seconds.
	 * @param formatted time input string e.g. 4h20m
	 * @return Number of seconds
	 */
	public static int parse(String formatted) {
		String s = formatted.toLowerCase();
		int output = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c >= '0' && c <= '9') {
				sb.append(c);
			} else if (c == ' ' || c == '\t' || c == ',') {
				continue;
			} else if (convLookup.containsKey(c)) {
				int conversion = convLookup.get(c);
				int val = Integer.parseInt(sb.toString());
				output += val * conversion;
				sb.delete(0, sb.length());
			}
		}
		if (sb.length() != 0) {
			output += new Integer(sb.toString());
		}
		return output;
	}
	
	private static Map<Character, Integer> buildConvLookup() {
		Map<Character,Integer> convLookup = new HashMap<Character, Integer>();
		convLookup.put('s', 1);
		convLookup.put('m', SECONDS_PER_MINUTE);
		convLookup.put('h', SECONDS_PER_MINUTE * MINUTES_PER_HOUR);
		convLookup.put('d', SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY);
		return Collections.unmodifiableMap(convLookup);
	}
}

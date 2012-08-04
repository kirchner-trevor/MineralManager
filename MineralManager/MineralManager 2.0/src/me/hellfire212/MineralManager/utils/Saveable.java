package me.hellfire212.MineralManager.utils;

/**
 * Represents something which can be saved.
 * 
 */
public interface Saveable {
	/**
	 * Save this object.
	 * @param force if true, should always save.
	 * @return true if anything happened, false otherwise
	 */
	public boolean save(boolean force);
}

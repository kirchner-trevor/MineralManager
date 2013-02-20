package me.hellfire212.MineralManager;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Coordinate implements Serializable {
	private static final long serialVersionUID = 7400534354401651744L;
	private final double x;
	private final double y;
	private final double z;
	private final String worldName;

	public Coordinate(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		worldName = location.getWorld().getName();
	}
	
	public Coordinate(double x, double y, double z, String worldName) {
	    this.x = x;
	    this.y = y;
	    this.z = z;
	    this.worldName = worldName;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
	
	//This can return null if the world this coordinate exists in doesn't exists on this server anymore.
	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}
	
	// Useful for logging and other information, gets the world name.
	public String getWorldName() {
		return worldName;
	}

	//This will return null if getWorld returns null
	public Location getLocation() {
		return new Location(getWorld(), getX(), getY(), getZ());
	}
	
	@Override
	public String toString() {
		return getWorld().getName() + " : " + getX() + ", " + getY() + ", " + getX();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((worldName == null) ? 0 : worldName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Coordinate other = (Coordinate) obj;
		if (worldName == null) {
			if (other.worldName != null)
				return false;
		} else if (!worldName.equals(other.worldName))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		return true;
	}
}
package me.hellfire212.mineralmanager;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class Coordinate implements Serializable {

	private static final long serialVersionUID = 7400534354401651744L;
	private final double x;
	private final double y;
	private final double z;
	private final UUID world;
	
	public Coordinate(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		world = location.getWorld().getUID();
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	
	public boolean inPolygon(Point2D.Double[] poly) {
		boolean c = false;
		int nvert = poly.length;
		for(int i = 0, j = nvert - 1; i < nvert; j = i++) {
			if(((poly[i].getY() > y) != (poly[j].getY() > y)) && (x < (poly[j].getX() - poly[i].getX()) * (y - poly[i].getY()) / (poly[j].getY() - poly[i].getY()) + poly[i].getX())) {
				c = !c;
			}
		}
		return c;
	}
	
	@Override
	public String toString() {
		return 	world + ": " + x + ", " + y + ", " + z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((world == null) ? 0 : world.hashCode());
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
		if (world == null) {
			if (other.world != null)
				return false;
		} else if (!world.equals(other.world))
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

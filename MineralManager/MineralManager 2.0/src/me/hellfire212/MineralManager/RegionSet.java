package me.hellfire212.MineralManager;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

import me.hellfire212.MineralManager.utils.ChatMagic;

public class RegionSet implements Serializable, Iterable<Region> {

	private static final long serialVersionUID = 3833726660750427658L;
	private ConcurrentSkipListSet<Region> regionSet = null;
	
	/**
	 * Creates a new RegionSet that initializes the underlying ConcurrentSkipListSet.
	 */
	public RegionSet() {
		regionSet = new ConcurrentSkipListSet<Region>();
	}
	
	/**
	 * Adds a region to the set.
	 * @param r the region to be added
	 * @return true if the regionSet did not already contain the region
	 */
	public boolean add(Region r) {
		return regionSet.add(r);
	}
	
	/**
	 * Removes a region from the set.
	 * @param r the name of the region to be removed
	 * @return true if the regionSet contained the region
	 */
	public boolean remove(String name) {
		for(Region region : regionSet) {
			if(region.getName().equalsIgnoreCase(name)) {
				return regionSet.remove(region);
			}
		}
		return false;
	}
	
	/**
	 * Removes a region from the set.
	 * @param r the region to be removed
	 * @return true if the regionSet contained the region
	 */
	public boolean remove(Region region) {
		return regionSet.remove(region);
	}
	
	/**
	 * Returns a region specified by name if it exists.
	 * @param name the name of the region
	 * @return the region if it exists in this set
	 */
	public Region get(String name) {
		for(Region region : regionSet) {
			if(region.getName().equalsIgnoreCase(name)) {
				return region;
			}
		}
		return null;
	}
	
	/**
	 * Tests the Coordinate with each region and returns the first region to contain the Coordinate.
	 * @param c the Coordinate to be tested
	 * @return the Region that contains the Coordinate or null if no Region contains it.
	 */
	public Region contains(Coordinate c) {
		for(Region region : regionSet) {
			if(region.contains(c)) {
				return region;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Region region : regionSet) {
			// XXX getLevel() returns a Double but it's actually an integer value in the end.
			// TODO fix when the binary format is eradicated
			b.append(String.format(" %s: %s [level=%d]\n", region.getName(), region.kind(), (int) region.getLevel()));
		}
		return b.toString();
	}
	
	public String toColorizedString() {
		StringBuilder b = new StringBuilder();
		for(Region region : regionSet) {
			// XXX getLevel() returns a Double but it's actually an integer value in the end.
			// TODO fix when the binary format is eradicated
			b.append(ChatMagic.colorize(
					"    {AQUA}%s{TEXT}: {VERB}%s {GOLD}[{TEXT}level={VERB}%d{TEXT}, config={VERB}%s{GOLD}]\n", 
					region.getName(), region.kind(), (int) region.getLevel(), region.getConfiguration().getName()
			));
		}
		return b.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((regionSet == null) ? 0 : regionSet.hashCode());
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
		RegionSet other = (RegionSet) obj;
		if (regionSet == null) {
			if (other.regionSet != null)
				return false;
		} else if (!regionSet.equals(other.regionSet))
			return false;
		return true;
	}
	
	public int size() {
		return regionSet.size();
	}

	@Override
	public Iterator<Region> iterator() {
		return regionSet.iterator();
	}
}

package me.hellfire212.MineralManager.datastructures;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.hellfire212.MineralManager.Coordinate;
import me.hellfire212.MineralManager.utils.Saveable;
/**
 * A sparse bitmap for storing boolean values about MineCraft blocks. 
 * 
 * This bitmap stores in a single file a single true/false value for every block 
 * coordinate. The values all start out as false, and can be set or unset as needed.
 * 
 * The file is a sparse file, that is, 16x16x16 chunks are accounted for in a 524-byte record, 
 * each of which is prefixed by the location within the world this chunk comes from, this allowing
 * data locality and also the file to grow sensibly in a useful time-space tradeoff.
 */
public class BlockBitmap implements Saveable {
	/* Information about records */
	private static final int HEADER_SIZE = 12;
	private static final int BITMAP_SIZE = 512;
	private static final int RECORD_SIZE = HEADER_SIZE + BITMAP_SIZE;
	
	private RandomAccessFile bitmapFile;
	private final DirtyFlag dirtyFlag = new DirtyFlag();
	private long firstEmpty = 0;
	private Map<BlockCoordinate, BlockBitmapNode> nodes = new HashMap<BlockCoordinate, BlockBitmapNode>();

	/**
	 * Create a BlockBitmap, making the file if required.
	 * @param f a File object which points to the location of the DB file this will use.
	 */
	public BlockBitmap(File f) {
		try {
			this.bitmapFile = new RandomAccessFile(f, "rw");
			this.parse();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Public API */

	/**
	 * Get the boolean value at a given coordinate
	 * @param x X component
	 * @param y Y component (nominally 0-256)
	 * @param z Z component
	 * @return true if bit is set, false otherwise.
	 */
	public final boolean get(int x, int y, int z) {
		BlockCoordinate baseCoord = makeBaseCoord(x, y, z);
		BlockBitmapNode node = nodes.get(baseCoord);
		if (node == null) {
			return false;
		} else if (!node.isLoaded()) {
			node.load(bitmapFile);
		}
		return node.get(x & 0xF, y & 0xF, z & 0xF);
	}
	
	/**
	 * Set the value at a given coordinate
	 * @param x X component
	 * @param y Y component (nominally 0-256)
	 * @param z Z component
	 * @param value New value to set/clear.
	 */
	public final void set(int x, int y, int z, boolean value) {
		BlockCoordinate baseCoord = makeBaseCoord(x, y, z);
		BlockBitmapNode node = nodes.get(baseCoord);
		if (node == null) {
			node = addNode(baseCoord);
		} 
		if (!node.isLoaded()) {
			node.load(bitmapFile);
		}
		node.set(x & 0xF, y & 0xF, z & 0xF, value);
	}

	/* File IO */

	/**
	 * Close this BlockBitmap, flushing any un-saved changes to disk.
	 */
	public void close() throws IOException {
		flush();
		bitmapFile.close();
		bitmapFile = null;
	}
	
	
	/**
	 * Parse the input file, finding the nodes available (but not populating them)
	 * @throws IOException
	 */
	private void parse() throws IOException {
		bitmapFile.seek(0);
		try {
			int x = bitmapFile.readInt();
			int y = bitmapFile.readInt();
			int z = bitmapFile.readInt();
			BlockCoordinate coord = new BlockCoordinate(x, y, z);
			nodes.put(coord, new BlockBitmapNode(bitmapFile.getFilePointer(), dirtyFlag));
			bitmapFile.skipBytes(BITMAP_SIZE);
		} catch (EOFException e) {
			
		}
		firstEmpty = bitmapFile.getFilePointer();
	}

	/**
	 * Flush all dirty nodes to disk.
	 * @return How many nodes were flushed.
	 */
	public int flush() {
		if (!dirtyFlag.isDirty()) {
			return 0;
		}
		int saved = 0;
		// Make a list of sorted nodes, so we can seek through the file in order.
		ArrayList<BlockBitmapNode> sortedNodes = new ArrayList<BlockBitmapNode>(nodes.values());
		Collections.sort(sortedNodes);
		
		// Save all nodes
		for (BlockBitmapNode node: sortedNodes) {
			if (node.saveIfDirty(bitmapFile)) {
				saved++; 
			}
		}
		dirtyFlag.clear();
		return saved;
	}
	
	/* Saveable contract */
	public boolean save(boolean force) {
		return (flush() > 0);
	}

	/* Utility functions */
	
	/**
	 * Allocate space in the file for a new record.
	 * @param baseCoord The base coordinates of this record.
	 * @return The newly allocated record.
	 */
	private BlockBitmapNode addNode(BlockCoordinate baseCoord) {
		try {
			bitmapFile.seek(firstEmpty);
			baseCoord.writeAsIntsToFile(bitmapFile);
			bitmapFile.write(new byte[512]);
			firstEmpty += RECORD_SIZE;
			BlockBitmapNode node = new BlockBitmapNode(firstEmpty - BITMAP_SIZE, dirtyFlag);
			nodes.put(baseCoord, node);
			return node;
		} catch (IOException e) {
			return null;
		}	
	}

	/**
	 * Given a coordinate, return a base coordinate
	 */
	private final BlockCoordinate makeBaseCoord(int x, int y, int z) {
		return new BlockCoordinate(x >> 4, y >> 4, z >> 4);
	}

	
	/** Specialty access for holdover stuff, this will soon become deprecated */
	
	public final void set(Coordinate c, boolean value) {
		int x = (int) Math.rint(c.getX());
		int y = (int) Math.rint(c.getY());
		int z = (int) Math.rint(c.getZ());
		set(x, y, z, value);
	}

	/** Un-set a flag without a new allocation */
	public void unset(Coordinate c) {
		int x = (int) Math.rint(c.getX());
		int y = (int) Math.rint(c.getY());
		int z = (int) Math.rint(c.getZ());
		if (get(x, y, z)) {
			set(x, y, z, false);
		}
		
	}
	
	@Override
	public String toString() {
	    return String.format("<BlockBitmap: %d nodes>", firstEmpty / RECORD_SIZE);
	}
}

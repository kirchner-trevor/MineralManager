package me.hellfire212.MineralManager.datastructures;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A single node used by BlockBitmap.
 *
 */
public class BlockBitmapNode implements Comparable<BlockBitmapNode> {
	/** Each byte stores 8 values, so we use bitmasks to extract the value out. */
	private static final byte[] masks = {
		(byte) 128, (byte) 64, (byte) 32, (byte) 16, (byte) 8, (byte) 4, (byte) 2, (byte) 1
	};
	private final long fileOffset;

	private final DirtyFlag dirtyFlag;
	private boolean dirty = false;
	private boolean loaded = false;
	private boolean[] map;

	/**
	 * Create a new BlockBitmapNode.
	 * @param offset The offset in the database where our bitmap starts.
	 */
	public BlockBitmapNode(long offset, DirtyFlag flag) {
		this.dirtyFlag = flag;
		this.fileOffset = offset;
	}
	
	/**
	 * Load our map from the database file
	 * @param f the database file.
	 */
	public void load(RandomAccessFile f) {
		map = new boolean[4096];
		try {
			f.seek(fileOffset);
			for (int b = 0; b < 512; b++) {
				byte thisByte = f.readByte();
				int offset = b * 8;
				int mask = 128;
				for (int bit=0; bit < 8; bit++) {
					map[offset + bit] = ((thisByte & mask) == mask);
					mask >>= 1;
				}
			}
			loaded = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save this node's bytes, to the database file.
	 * @param f Our database file
	 * @return true if save happened, false otherwise.
	 */
	public boolean saveIfDirty(RandomAccessFile f) {
		if (dirty) {
			return save(f);
		} else {
			return false;
		}
	}
	
	/**
	 * Save this node's bytes
	 * @param f Our database file
	 * @return true if save succeeded, false on error.
	 */
	public boolean save(RandomAccessFile f) {
		try {
			f.seek(fileOffset);
			for (int i = 0; i < 4096; i += 8) {
				int val = 0;
				for (int offset = 0; offset < 8; offset++) {
					if(map[i+offset]) {
						val += masks[offset];
					}
				}
				f.writeByte(val);
			}
			dirty = false;
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Get a value from this node.
	 * @param i 4-bit x component
	 * @param j 4-bit y component
	 * @param k 4-bit z component
	 * @return
	 */
	public boolean get(int i, int j, int k) {
		int loc = (i << 8) | (j << 4) | k;
		return map[loc];
	}
	
	/**
	 * Set a value in the node.
	 * @param i 4-bit x component
	 * @param j 4-bit y component
	 * @param k 4-bit z component
	 * @param value
	 */
	public void set(int i, int j, int k, boolean value) {
		int loc = (i << 8) | (j << 4) | k;
		map[loc] = value;
		dirtyFlag.flagDirty();
		dirty = true;
	}

	/**
	 * 
	 * @return true if loaded.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	public long getFileOffset() {
		return fileOffset;
	}

	/**
	 * Used for sorting this node by file offsets.
	 */
	@Override
	public int compareTo(BlockBitmapNode other) {
		return new Long(fileOffset).compareTo(other.getFileOffset());
	}
}

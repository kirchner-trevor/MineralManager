package me.hellfire212.MineralManager.datastructures;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BlockBitmapNode implements Comparable<BlockBitmapNode> {
	private static final byte[] masks = {
		(byte) 128, (byte) 64, (byte) 32, (byte) 16, (byte) 8, (byte) 4, (byte) 2, (byte) 1
	};
	private final long fileOffset;

	private boolean dirty = false;
	private boolean loaded = false;
	private boolean[] map;

	public BlockBitmapNode(long offset) {
		this.fileOffset = offset;
	}
	
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
	public boolean saveIfDirty(RandomAccessFile f) {
		if (dirty) {
			return save(f);
		} else {
			return false;
		}
	}
	
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

	public boolean get(int i, int j, int k) {
		System.out.println(String.format("i=%d,j=%d,k=%d", i,j,k));
		int loc = (i << 8) | (j << 4) | k;
		return map[loc];
	}
	
	public void set(int i, int j, int k, boolean value) {
		System.out.println(String.format("i=%d,j=%d,k=%d", i,j,k));
		int loc = (i << 8) | (j << 4) | k;
		map[loc] = value;
		dirty = true;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public long getFileOffset() {
		return fileOffset;
	}

	@Override
	public int compareTo(BlockBitmapNode other) {
		return new Long(fileOffset).compareTo(other.getFileOffset());
	}
}

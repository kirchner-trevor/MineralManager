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

public class BlockBitmap {
	private static final int HEADER_SIZE = 12;
	private static final int BITMAP_SIZE = 512;
	private static final int RECORD_SIZE = HEADER_SIZE + BITMAP_SIZE;
	private RandomAccessFile bitmapFile;
	private long firstEmpty = 0;
	private Map<BlockCoordinate, BlockBitmapNode> nodes = new HashMap<BlockCoordinate, BlockBitmapNode>();

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

	public final boolean get(int x, int y, int z) {
		BlockCoordinate baseCoord = makeBaseCoord(x, y, z);
		System.out.println("finding for:" + baseCoord);
		BlockBitmapNode node = nodes.get(baseCoord);
		if (node == null) {
			System.out.println(" -> Node not found");
			return false;
		} else if (!node.isLoaded()) {
			System.out.println(" -> Node needs to be loaded");
			node.load(bitmapFile);
		}
		return node.get(x & 0xF, y & 0xF, z & 0xF);
	}
	
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
	
	public void close() throws IOException {
		flush();
		bitmapFile.close();
		bitmapFile = null;
	}
	
	/* File IO */
	
	private void parse() throws IOException {
		bitmapFile.seek(0);
		try {
			int x = bitmapFile.readInt();
			int y = bitmapFile.readInt();
			int z = bitmapFile.readInt();
			BlockCoordinate coord = new BlockCoordinate(x, y, z);
			System.out.println(coord.toString());
			nodes.put(coord, new BlockBitmapNode(bitmapFile.getFilePointer()));
			bitmapFile.skipBytes(BITMAP_SIZE);
		} catch (EOFException e) {
			
		}
		firstEmpty = bitmapFile.getFilePointer();
	}
	
	/* Utility functions */
	private BlockBitmapNode addNode(BlockCoordinate baseCoord) {
		try {
			bitmapFile.seek(firstEmpty);
			baseCoord.writeAsIntsToFile(bitmapFile);
			bitmapFile.write(new byte[512]);
			firstEmpty += RECORD_SIZE;
			System.out.printf("New node, offset=%d, firstEmpty=%d\n", firstEmpty-BITMAP_SIZE, firstEmpty);
			BlockBitmapNode node = new BlockBitmapNode(firstEmpty - BITMAP_SIZE);
			nodes.put(baseCoord, node);
			return node;
		} catch (IOException e) {
			return null;
		}	
	}

	private final BlockCoordinate makeBaseCoord(int x, int y, int z) {
		return new BlockCoordinate(x >> 4, y >> 4, z >> 4);
	}

	public int flush() {
		int saved = 0;
		ArrayList<BlockBitmapNode> sortedNodes = new ArrayList<BlockBitmapNode>(nodes.values());
		Collections.sort(sortedNodes);
		for (BlockBitmapNode node: sortedNodes) {
			if (node.saveIfDirty(bitmapFile)) {
				saved++; 
			}
		}
		return saved;
	}
}

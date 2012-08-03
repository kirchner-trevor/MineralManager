package me.hellfire212.MineralManager.datastructures;

import java.io.DataOutput;
import java.io.IOException;

import org.bukkit.util.BlockVector;

public class BlockCoordinate {
    private final int x;
    private final int y;
    private final int z;
    private final int myHashCode;
    
    public BlockCoordinate(BlockVector vec) {
        this(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
    }
    
    public BlockCoordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.myHashCode = x ^ y ^ z;
    }
    
    public BlockCoordinate replaceX(int newX) {
        return new BlockCoordinate(newX, y, z);
    }
    
    public BlockCoordinate replaceZ(int newZ) {
        return new BlockCoordinate(x, y, newZ);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
    
    @Override
    public int hashCode() {
    	return myHashCode;
    }
    
    @Override
    public String toString() {
    	return String.format("<BlockCoordinate(x=%d, y=%d, z=%d)>", x, y, z);
    }
    
    @Override
    public boolean equals(Object other) {
    	if (other == this) return true;
    	if (other instanceof BlockCoordinate) {
    		BlockCoordinate bcOther = (BlockCoordinate) other;
    		return (bcOther.getX() == x && bcOther.getY() == y && bcOther.getZ() == z);
    	} else {
    		return false;
    	}
    }
    
    public void writeAsIntsToFile(DataOutput output) throws IOException {
    	output.writeInt(x);
    	output.writeInt(y);
    	output.writeInt(z);
    }

}

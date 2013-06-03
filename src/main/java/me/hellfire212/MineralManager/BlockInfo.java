package me.hellfire212.MineralManager;

import java.io.Serializable;

public class BlockInfo implements Serializable {

	private static final long serialVersionUID = -1677234085298704597L;
	private final int blockTypeId;
	private final int placeholderTypeId;
	private final int blockData; //Must be cast to a byte when used in Block.setTypeIdandData
	private final int placeholderData; //Must be cast to a byte when used in Block.setTypeIdandData
	
	private long respawn = 0; //The time at which the block will re-spawn
	public enum Type { BLOCK, PLACEHOLDER }

	public BlockInfo(Type type, int typeId, int data) {
		switch(type) {
		case BLOCK :
			blockTypeId = typeId;
			blockData = data;
			placeholderTypeId = -1;
			placeholderData = -1;
			break;
		case PLACEHOLDER :
			blockTypeId = -1;
			blockData = -1;
			placeholderTypeId = typeId;
			placeholderData = data;
			break;
		default:
			blockTypeId = -1;
			blockData = -1;
			placeholderTypeId = -1;
			placeholderData = -1;
		}
	}
	
	public BlockInfo(int blockTypeId, int blockData, int placeholderTypeId, int placeholderData) {
		this.blockTypeId = blockTypeId;
		this.blockData = blockData;
		this.placeholderTypeId = placeholderTypeId;
		this.placeholderData = placeholderData;
	}
	
	public int getTypeId(Type type) {
		switch(type) {
		case BLOCK :
			return blockTypeId;
		case PLACEHOLDER :
			return placeholderTypeId;
		default :
			return -1;
		}
	}
	
	public int getData(Type type) {
		switch(type) {
		case BLOCK :
			return blockData;
		case PLACEHOLDER :
			return placeholderData;
		default :
			return -1;
		}
	}
	
	public long getCooldown() {
		return Math.max(0, (respawn - System.currentTimeMillis()) / 1000L);
	}

	public long getRespawn() {
		return respawn;
	}

	public void setRespawn(long respawn) {
		this.respawn = respawn;
	}

	public String toString(Type type) {
		switch(type) {
		case BLOCK :
			return blockTypeId + " " + blockData;
		case PLACEHOLDER :
			return placeholderTypeId + " " + placeholderData;
		default :
			return blockTypeId + " " + blockData + ", " + placeholderTypeId + " " + placeholderData;
		}
	}
	
	@Override
	public String toString() {
		return blockTypeId + " " + blockData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + blockData;
		result = prime * result + blockTypeId;
		return result;
	}

	//This only compares blockTypeID and blockData since we use this in a Hash Map concerned with block typeId's and data's.
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockInfo other = (BlockInfo) obj;
		if (blockData != other.blockData)
			return false;
		if (blockTypeId != other.blockTypeId)
			return false;
		return true;
	}
	
	public BlockInfo clone() {
	    return new BlockInfo(blockData, blockTypeId, placeholderData, placeholderTypeId);
	}
}
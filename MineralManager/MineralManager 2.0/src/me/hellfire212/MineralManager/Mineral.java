package me.hellfire212.MineralManager;

import java.io.Serializable;

public class Mineral implements Serializable {

	private static final long serialVersionUID = -2569665927266852145L;
	
	private int cooldown;
	private double degrade;
	private final BlockInfo blockInfo;
	
	public Mineral(BlockInfo block, int cool, double deg) {
		blockInfo = block;
		cooldown = cool;
		degrade = deg;
 	}
	
	public BlockInfo getBlockInfo() {
		return blockInfo;
	}
	
	public int getCooldown() {
		return cooldown;
	}
	
	public double getDegrade() {
		return degrade;
	}

	public void setCooldown(int cd) {
		cooldown = cd >= 0 ? cd : 0;
	}
	
	public void setDegrade(double deg) {
		degrade = deg >= 0.0 ? deg : 0.0;
	}
	
	@Override
	public String toString() {
		return "(" + blockInfo.toString() + "):(" + cooldown + " " + degrade + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((blockInfo == null) ? 0 : blockInfo.hashCode());
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
		Mineral other = (Mineral) obj;
		if (blockInfo == null) {
			if (other.blockInfo != null)
				return false;
		} else if (!blockInfo.equals(other.blockInfo))
			return false;
		return true;
	}
}
package me.hellfire212.MineralManager;

import org.apache.commons.lang.Validate;

public class MineralConfig {
    private final int cooldown;
    private final double degrade;
    private final BlockInfo blockInfo;

    public MineralConfig(BlockInfo block, int cool, double deg) {
        Validate.notNull(block);
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

    @Override
    public String toString() {
        return "(" + blockInfo.toString() + "):(" + cooldown + " " + degrade + ")";
    }

    @Override
    public int hashCode() {
        return blockInfo.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MineralConfig other = (MineralConfig) obj;
        if (blockInfo == null) {
            if (other.blockInfo != null)
                return false;
        } else if (!blockInfo.equals(other.blockInfo))
            return false;
        return true;
    }
}
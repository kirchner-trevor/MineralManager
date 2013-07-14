package me.hellfire212.MineralManager.utils;

import java.util.logging.Logger;

import me.hellfire212.MineralManager.MineralManager;

public final class LogTools {
    private LogTools() {}
    
    public static Logger getLogger() {
        return MineralManager.getInstance().getLogger();
    }
    
    public static void logInfo(String s, Object ... args) {
        getLogger().info(String.format(s, args));
    }

}

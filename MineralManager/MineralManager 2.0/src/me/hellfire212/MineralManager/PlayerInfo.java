package me.hellfire212.MineralManager;

import me.hellfire212.MineralManager.datastructures.ObjectMaker;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class PlayerInfo {
	private static final String PLAYERS_SECTION = "players";
	
	private boolean advanced = false;
	private String name;
	
	public PlayerInfo(String name) {
		this.name = name;
		load(MineralManager.getInstance().getDataConfig());
	}

	boolean getAdvanced() {
		return advanced ;
	}
	
	void setAdvanced(boolean advanced) {
		this.advanced = advanced;
		findMySection(MineralManager.getInstance().getDataConfig()).set("advanced", advanced);
	}
	

	private void load(FileConfiguration config) {
		ConfigurationSection sec = findMySection(config);
		advanced = sec.getBoolean("advanced");

	}

	private ConfigurationSection findMySection(FileConfiguration config) {
		ConfigurationSection players = config.getConfigurationSection(PLAYERS_SECTION);
		if (players == null) {
			players = config.createSection(PLAYERS_SECTION);
		}
		ConfigurationSection me = players.getConfigurationSection(name);
		if (me == null) {
			me = players.createSection(name);
		}
		return me;
	}
	
	public static ObjectMaker<PlayerInfo> getMaker() {
		return new ObjectMaker<PlayerInfo>() {
			public PlayerInfo build(Object key) {
				return new PlayerInfo((String) key);
			}
		};
	}
}

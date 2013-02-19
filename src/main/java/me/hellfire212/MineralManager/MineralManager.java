package me.hellfire212.MineralManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;

import me.hellfire212.MineralManager.datastructures.DefaultDict;
import me.hellfire212.MineralManager.tasks.EnableListenersTask;
import me.hellfire212.MineralManager.utils.MetricsLite;
import mondocommand.ChatMagic;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class MineralManager extends JavaPlugin {
	public static final ChatColor TEXT_COLOR = ChatColor.LIGHT_PURPLE;
	public static final ChatColor HEADER_COLOR = ChatColor.GOLD;
	public static final String PREFIX = ChatMagic.colorize("{AQUA}[MineralManager] {TEXT}");
	
	public static final String REGION_SET_FILENAME = "regionSet.bin";
	public static final String BLOCK_MAP_FILENAME = "blockMap.bin";
	private static final String PLACED_SET_FILENAME = "placedMap.bin";
	private static final String LOCKED_SET_FILENAME = "lockedMap.bin";
	private static final String DATA_YAML_FILENAME = "data.yml";
	
	private static MineralManager plugin = null;
		
	public ConcurrentHashMap<Coordinate, BlockInfo> blockMap;
	public FileHandler blockMapFH;
	
	public MineralListener mineralListener;
	public LassoListener lassoListener;
	
	private HashMap<String, Configuration> configurationMap;
	private ConcurrentHashMap<Player, Selection> selectionMap;
	private Set<String> knownWorlds = new HashSet<String>();
	
	private Configuration defaultConfiguration;
	private DefaultDict<String, WorldData> worldData = new DefaultDict<String, WorldData>(WorldData.getMaker());
	private DefaultDict<String, PlayerInfo> playerInfo = new DefaultDict<String, PlayerInfo>(PlayerInfo.getMaker());

	private SaveTracker saveTracker;
	private YamlConfiguration dataConfig;

	
	public MineralManager() {
		super();
		plugin = this;
		configurationMap = new HashMap<String, Configuration>();
		selectionMap = new ConcurrentHashMap<Player, Selection>();
	}

	
	/**
	 * Called when this plugin is enabled. 
	 */
	@Override
	public void onEnable() {
		// Initial setup of files and paths.
		if (!(new File(this.getDataFolder(), "config.yml").exists())) { 
			saveDefaultConfig();
		}
		parseConfigurationValues();

		File binFolder = new File(plugin.getDataFolder(), "bin");
		if (!binFolder.isDirectory()) {
			if (!binFolder.mkdir()) {
				getLogger().severe("Could not create folder for plugin data");
			}
		}

		WorldData.BASE_FOLDER = binFolder;
		
		loadFileHandlerDatabases(binFolder);
		performDataUpgrades(binFolder);
				
		saveTracker = new SaveTracker(this, MMConstants.SAVE_DEADLINE);
		getServer().getScheduler().scheduleSyncDelayedTask(this, saveTracker, MMConstants.SAVETRACKER_STARTUP_DELAY);		
		new EnableListenersTask(this).run();
		
		// Do plugin metrics
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// TODO
		}
		new Commands(this);
	}


	/**
	 * Called when this plugin is disabled. 
	 */
	@Override
	public void onDisable() {
		if(plugin.blockMapFH != null) {
			plugin.blockMapFH.saveObject(plugin.blockMap);
		}
		if (saveTracker != null) {
			saveTracker.shutdown();
			saveTracker = null;
		}

		for (WorldData wdata : worldData.values()) {
			wdata.shutdown();
		}
		worldData.clear();
		Commands.shutdown();
		lassoListener.shutdown();
		plugin = null;
	}
	
	//Change iterator to "for each"
	public void parseConfigurationValues() {
		boolean debug = getConfig().getBoolean("debug.config");
		if (debug) getLogger().info("Loading configuration sections...");
		ConfigurationSection currentConfig = getConfig().getConfigurationSection("DEFAULT");
		try {
			defaultConfiguration = new Configuration(currentConfig, new Configuration());
		} catch (NumberFormatException e) {
			getLogger().severe(e.getLocalizedMessage());
		} catch (ParseException e) {
			getLogger().severe(e.getLocalizedMessage());
		} catch (DataFormatException e) {
			getLogger().severe(e.getLocalizedMessage());
		}
		if(defaultConfiguration == null) {
			System.exit(-1);
		}

		ConfigurationSection regionConfig = getConfig().getConfigurationSection("CONFIGURATION");
		Set<String> configSections = regionConfig.getKeys(false);
		for (String name: configSections) {
			if (debug) getLogger().info(" -> Section " + name);
			currentConfig = regionConfig.getConfigurationSection(name);
			Configuration tempConfig = null;
			try {
				tempConfig = new Configuration(currentConfig, defaultConfiguration);
			} catch (NumberFormatException e) {
				getLogger().severe(e.getLocalizedMessage());
			} catch (ParseException e) {
				getLogger().severe(e.getLocalizedMessage());
			} catch (DataFormatException e) {
				getLogger().severe(e.getLocalizedMessage());
			}
			configurationMap.put(currentConfig.getName().toLowerCase(), tempConfig);
		}

		// Data.yml stuff
		YamlConfiguration dataConfig = getDataConfig();
		List<String> knownWorldsList = dataConfig.getStringList("knownWorlds");
		knownWorlds.addAll(knownWorldsList);
	}
        //commandhelp = new MMCommand[]{select, create, remove, list, lock, creative};
        /*
         * private MMCommand select = new MMCommand("select", true);
    private MMCommand select_world = new MMCommand("world");
    private MMCommand select_cube = new MMCommand("cube", new Argument(Integer.class, "xz-radius"), new Argument(Integer.class, "y-radius"));
    private MMCommand select_region = new MMCommand("region", new Argument(String.class, "start / end"));
    private MMCommand select_lasso = new MMCommand("lasso", new Argument(String.class, "start / end"));

    private MMCommand create = new MMCommand("create", new Argument(String.class, "region name"), new Argument(String.class, "configuration"), new Argument(Integer.class, "level"));
    
         */
		
				/*
				List<String> subList = argumentList.subList(1, argumentList.size());
				
				if ((validList = select_world.validate(subList)) != null) {
					selectionMap.put(player, Commands.selectWorld(plugin, player, validList));
					return true;
				}
				
				if((validList = select_cube.validate(subList)) != null) {
					selectionMap.put(player, Commands.selectCube(plugin, player, validList));
					return true;
				}
				
				if((validList = select_region.validate(subList)) != null) {
					Selection temp = Commands.selectRegion(plugin, player, validList);
					if(temp != null) {
						selectionMap.put(player, temp);
					}
					return true;
				}
				
				if((validList = select_lasso.validate(subList)) != null) {
					Selection temp = Commands.selectLasso(plugin, player, validList);
					if(temp != null) {
						selectionMap.put(player, temp);
					}
					return true;
				}
				
				String error = MMCommand.getError();
				player.sendMessage(error.isEmpty() ? selectList : MineralManager.PREFIX + "/mm " + select.getName() + " " + error);
				return false;
			}*/

			/*
			if((validList = create.validate(argumentList)) != null) {
				Commands.create(plugin, player, validList);
				return true;
			}*/
			
			/*
			if ((validList = advancedCmd.validate(argumentList)) != null) {
				PlayerInfo pi = getPlayerInfo(player.getName());
				pi.setAdvanced(!pi.getAdvanced());
				ChatMagic.send(player, 
						"%sYou are now in %s mode", 
						MineralManager.PREFIX,
						(pi.getAdvanced() ? "advanced": "basic")
				);
				return true;
			}
			
			String error = MMCommand.getError();
			player.sendMessage(error.isEmpty() ? commandList : MineralManager.PREFIX + "/mm " + error);
			return false;
			*/


	public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
	}

	public Selection getSelection(Player player) {
		return selectionMap.get(player);
	}

	public HashMap<String, Configuration> getConfigurationMap() {
		return configurationMap;
	}

	public WorldData getWorldData(World world) {
		return worldData.ensure(world.getName());
	}
	
	public Collection<WorldData> allWorldDatas() {
		List<WorldData> items = new ArrayList<WorldData>();
		for (String known: knownWorlds) {
			items.add(worldData.ensure(known));
		}
		return items;
	}
	
	public PlayerInfo getPlayerInfo(String playerName) {
		return playerInfo.ensure(playerName);
	}
	
	/**
	 * Get the set of all regions known about.
	 * @return a brand new set.
	 */
	public Set<Region> allRegions() {
		HashSet<Region> allRegions = new HashSet<Region>();
		for (WorldData wd : allWorldDatas()) {
			for (Region r: wd.getRegionSet()) {
				allRegions.add(r);
			}
		}
		return allRegions;
	}
	
	public YamlConfiguration getDataConfig() {
		if (dataConfig == null) {
			dataConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), DATA_YAML_FILENAME));
		}
		return dataConfig;
	}
	
	public boolean saveDataConfig() {
		try {
			getDataConfig().save(new File(getDataFolder(), DATA_YAML_FILENAME));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void addKnownWorld(String worldName) {
		if (knownWorlds.add(worldName)) {
			getDataConfig().set("knownWorlds", new ArrayList<String>(knownWorlds));
			saveDataConfig();
		}	
	}
	
	/* Things done at initialization */
	private void performDataUpgrades(File binFolder) {
		// Data conversion stuff
		File placedSetFile = new File(binFolder, PLACED_SET_FILENAME);
		if (placedSetFile.exists()) {
			Upgrader.convertPlaced(this, placedSetFile);
		}
		File lockedSetFile = new File(binFolder, LOCKED_SET_FILENAME);
		if (lockedSetFile.exists()) {
			Upgrader.convertLocked(this, lockedSetFile);
		}
		
		File regionSetFile = new File(binFolder, REGION_SET_FILENAME);
		if (regionSetFile.exists()) {
			Upgrader.convertRegions(this, regionSetFile);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadFileHandlerDatabases(File binFolder) {
		// Initial setup
		blockMap = new ConcurrentHashMap<Coordinate, BlockInfo>();
		blockMapFH = new FileHandler(new File(binFolder, BLOCK_MAP_FILENAME));

		// Do actual loading
		
		try {
			blockMap =  blockMapFH.loadObject(blockMap.getClass());
		} catch (FileNotFoundException e) {}
		
		SaveTracker.track(blockMapFH.getSaver(blockMap));		
	}
	
	/**
	 * Used to get the current plugin instance, for cases we don't have the state available.
	 */
	public static MineralManager getInstance() {
		return plugin;
	}
	
	
	/** Static block to set up Bukkit serialization */
	static {
		ConfigurationSerialization.registerClass(Region.class);
	}
}

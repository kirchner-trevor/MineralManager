package me.hellfire212.MineralManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;

import me.hellfire212.MineralManager.datastructures.DefaultDict;
import me.hellfire212.MineralManager.tasks.EnableListenersTask;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class MineralManager extends JavaPlugin {
	public static final ChatColor TEXT_COLOR = ChatColor.LIGHT_PURPLE;
	public static final ChatColor HEADER_COLOR = ChatColor.GOLD;
	public static final String PREFIX = ChatColor.AQUA + "[MineralManager] " + MineralManager.TEXT_COLOR;
	
	private static final String BIN_PATH = "plugins/MineralManager/bin/";
	private static final String REGION_SET_PATH = BIN_PATH + "regionSet.bin";
	private static final String BLOCK_MAP_PATH = BIN_PATH + "blockMap.bin";
	private static final String PLACED_SET_PATH = BIN_PATH + "placedMap.bin";
	private static final String LOCKED_SET_PATH = BIN_PATH + "lockedMap.bin";
	
	private MineralManager plugin = null;
	
	private MMCommand select = new MMCommand("select", true);
	private MMCommand cube = new MMCommand("cube", new Argument(Integer.class, "xz-radius"), new Argument(Integer.class, "y-radius"));
	private MMCommand region = new MMCommand("region", new Argument(String.class, "start / end"));
	private MMCommand lasso = new MMCommand("lasso", new Argument(String.class, "start / end"));

	private MMCommand create = new MMCommand("create", new Argument(String.class, "region name"), new Argument(String.class, "configuration"), new Argument(Integer.class, "level"));
	private MMCommand remove = new MMCommand("remove", new Argument(String.class, "region name"));
	private MMCommand list = new MMCommand("list");
	private MMCommand update = new MMCommand("update", new Argument(String.class, "region name"));
	private MMCommand lock = new MMCommand("lock");
	private MMCommand creative = new MMCommand("creative");
	
	public RegionSet regionSet;
	public FileHandler regionSetFH;
	
	public ConcurrentHashMap<Coordinate, BlockInfo> blockMap;
	public FileHandler blockMapFH;
	
	public Set<Coordinate> lockedSet;
	public FileHandler lockedSetFH;
	
	public MineralListener mineralListener;
	public LassoListener lassoListener;
	
	private HashMap<String, Configuration> configurationMap;
	private ConcurrentHashMap<Player, Selection> selectionMap;
	
	private Configuration defaultConfiguration;
	private DefaultDict<String, WorldData> worldData = new DefaultDict<String, WorldData>(WorldData.getMaker());

	private SaveTracker saveTracker;

	
	public MineralManager() {
		regionSet = new RegionSet();
		regionSetFH = new FileHandler(new File(REGION_SET_PATH));
		blockMap = new ConcurrentHashMap<Coordinate, BlockInfo>();
		blockMapFH = new FileHandler(new File(BLOCK_MAP_PATH));
		lockedSet = Collections.synchronizedSet(new HashSet<Coordinate>());
		lockedSetFH = new FileHandler(new File(LOCKED_SET_PATH));
		configurationMap = new HashMap<String, Configuration>();
		selectionMap = new ConcurrentHashMap<Player, Selection>();
		plugin = this;
	}

	
	/**
	 * Called when this plugin is enabled. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		if (!(new File(this.getDataFolder(), "config.yml").exists())) { 
			saveDefaultConfig();
		}
		parseConfigurationValues();

		// Data conversion stuff
		File placedSetFile = new File(PLACED_SET_PATH);
		if (placedSetFile.exists()) {
			Upgrader.convertPlaced(this, placedSetFile);
		}
		File binFolder = new File(plugin.getDataFolder(), "bin");
		if (!binFolder.isDirectory()) {
			if (!binFolder.mkdir()) {
				getLogger().severe("Could not create folder for plugin data");
			}
		}

		WorldData.BASE_FOLDER = binFolder;
		
		try {
			regionSet = regionSetFH.loadObject(regionSet.getClass());
		} catch (FileNotFoundException e) {}
		
		try {
			blockMap =  blockMapFH.loadObject(blockMap.getClass());
		} catch (FileNotFoundException e) {}
		
		try {
			lockedSet = lockedSetFH.loadObject(lockedSet.getClass());
		} catch (FileNotFoundException e) {}
		
		SaveTracker.track(blockMapFH.getSaver(blockMap));
		
		saveTracker = new SaveTracker(this, MMConstants.SAVE_DEADLINE);
		getServer().getScheduler().scheduleSyncDelayedTask(this, saveTracker, MMConstants.SAVETRACKER_STARTUP_DELAY);
		
		new EnableListenersTask(this).run();
		
		// Update configurations on all the regions to those loaded from file.
		for (Region region: regionSet) {
			String config_name = region.getConfiguration().getName();
			if (configurationMap.containsKey(config_name)) {
				region.setConfiguration(configurationMap.get(config_name));
			}
		}
		
	}
	
	/**
	 * Called when this plugin is disabled. 
	 */
	@Override
	public void onDisable() {
		if(plugin.blockMapFH != null) {
			plugin.blockMapFH.saveObject(plugin.blockMap);
		}
	}
	
	//Change iterator to "for each"
	private void parseConfigurationValues() {
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
		Set<String> regionSet = regionConfig.getKeys(false);
		for (String name: regionSet) {
			getLogger().info("Section " + name);
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
	}
	
	/**
	 * Executes the given command, returning its success.
	 * @param sender Source of the command
	 * @param command Command which was executed
	 * @param label Alias of the command which was used
	 * @param args Passed command arguments
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean result = false;
		if(sender instanceof Player) {
			handleCommand((Player) sender, label, args);
		} else {
			System.out.println("Non-player command!");
		}
		return result;
	}
	
	private boolean handleCommand(Player player, String command, String[] args) {
		
		//We don't want non-admins to be using these commands.
		if(!player.hasPermission(MineralListener.PERMISSION_ADMIN)) {
			return false;
		}
		
		List<String> argumentList = Arrays.asList(args);
		
		String commandList = MineralManager.PREFIX + MineralManager.HEADER_COLOR + "[Commands]\n" + MineralManager.TEXT_COLOR +
							 "/mm " + select.getUsage() + "\n" +
							 "/mm " + create.getUsage() + "\n" +
							 "/mm " + remove.getUsage() + "\n" +
							 "/mm " + list.getUsage()   + "\n" +
							 "/mm " + update.getUsage() + "\n" +
							 "/mm " + lock.getUsage() + "\n" + 
							 "/mm " + creative.getUsage() + "\n";
		
		List<Object> validList = null;
		
		if(command.equalsIgnoreCase("mm")) {
			
			if((validList = select.validate(argumentList)) != null) {
				String selectList = MineralManager.PREFIX + MineralManager.HEADER_COLOR + "[Selection Commands]\n" + MineralManager.TEXT_COLOR +
									"/mm select " + cube.getUsage() + "\n" + 
									"/mm select " + region.getUsage() + "\n" +
									"/mm select " + lasso.getUsage() + "\n";
				
				List<String> subList = argumentList.subList(1, argumentList.size());
				
				if((validList = cube.validate(subList)) != null) {
					selectionMap.put(player, Commands.selectCube(plugin, player, validList));
					return true;
				}
				
				if((validList = region.validate(subList)) != null) {
					Selection temp = Commands.selectRegion(plugin, player, validList);
					if(temp != null) {
						selectionMap.put(player, temp);
					}
					return true;
				}
				
				if((validList = lasso.validate(subList)) != null) {
					Selection temp = Commands.selectLasso(plugin, player, validList);
					if(temp != null) {
						selectionMap.put(player, temp);
					}
					return true;
				}
				
				String error = MMCommand.getError();
				player.sendMessage(error.isEmpty() ? selectList : MineralManager.PREFIX + "/mm " + select.getName() + " " + error);
				return false;
			}
				
			if((validList = create.validate(argumentList)) != null) {
				Commands.create(plugin, player, validList);
				return true;
			}
				
			if((validList = remove.validate(argumentList)) != null) {
				Commands.remove(plugin, player, validList);
				return true;
			}
				
			if((validList = list.validate(argumentList)) != null) {
				Commands.list(plugin, player, validList);
				return true;
			}
				
			if((validList = update.validate(argumentList)) != null) {
				Commands.update(plugin, player, validList);
				return true;
			}
			
			if((validList = lock.validate(argumentList)) != null) {
				Commands.lock(plugin, player, validList);
				return true;
			}
			
			if((validList = creative.validate(argumentList)) != null) {
				Commands.creative(plugin, player, validList);
				return true;
			}
			
			String error = MMCommand.getError();
			player.sendMessage(error.isEmpty() ? commandList : MineralManager.PREFIX + "/mm " + error);
			return false;
			
		} else if(command.equalsIgnoreCase("test")) {
			Coordinate testCoord = new Coordinate(player.getLocation());

			Region inRegion = regionSet.contains(testCoord);
			if(inRegion != null) {
				player.sendMessage(MineralManager.PREFIX + "You are in region " + inRegion);
			} else {
				player.sendMessage(MineralManager.PREFIX + "You are not in a region.");
			}
			return true;
		}
		return false;
	}

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
		return worldData.values();
	}
}

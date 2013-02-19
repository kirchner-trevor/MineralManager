package me.hellfire212.MineralManager;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.hellfire212.MineralManager.dialogue.CreateRegion;
import me.hellfire212.MineralManager.tasks.LassoWatcherTask;
import me.hellfire212.MineralManager.utils.ShapeUtils;
import mondocommand.CallInfo;
import mondocommand.ChatMagic;
import mondocommand.FormatConfig;
import mondocommand.MondoCommand;
import mondocommand.MondoFailure;
import mondocommand.SubHandler;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public final class Commands {
	private static final String START = "start";
	private static final String END = "end";
	private static ConcurrentHashMap<Player, Coordinate> regionStartMap = new ConcurrentHashMap<Player, Coordinate>();
	public static ConcurrentHashMap<String, ArrayList<Coordinate>> lassoCoordinateMap = new ConcurrentHashMap<String, ArrayList<Coordinate>>();
    private final MineralManager plugin;

	public Commands(MineralManager plugin) {
	    this.plugin = plugin;
	    setup();
	}

	private void setup() {
        FormatConfig config = new FormatConfig()
            .setReplyPrefix(MineralManager.PREFIX);

        MondoCommand base = new MondoCommand(config);
        plugin.getCommand("mm").setExecutor(base);

        base.addSub("create")
            .setDescription("Create a new region")
            .setHandler(new SubHandler() {
                public void handle(CallInfo call) {
                    new CreateRegion(plugin).begin(call.getPlayer());
                }
                
            });
        
        base.addSub("remove")
            .allowConsole()
            .setDescription("Remove MM region")
            .setMinArgs(1)
            .setUsage("region name")
            .setHandler(new SubHandler() {
                public void handle(CallInfo call) {
                    remove(call);
                }
            });
        
        base.addSub("list")
            .allowConsole()
            .setDescription("List MM regions")
            .setHandler(new SubHandler() {
                public void handle(CallInfo call) throws MondoFailure {
                    list(call);

                }            
            });
        
        base.addSub("lock")
            .setDescription("Lock block on your cursor")
            .setHandler(new SubHandler() {
                public void handle(CallInfo call) throws MondoFailure {
                    lock(call);
                }
            });
        
        base.addSub("creative")
            .setDescription("Creative Mode")
            .setHandler(new SubHandler() {
                public void handle(CallInfo call) throws MondoFailure {
                    creative(call, call.getPlayer());
                }            
            });
        
        plugin.getCommand("test").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                Player player = (Player) sender;
                Coordinate testCoord = new Coordinate(player.getLocation());
                WorldData wdata = plugin.getWorldData(player.getWorld());
                Region inRegion = wdata.getRegionSet().contains(testCoord);
                if(inRegion != null) {
                    player.sendMessage(MineralManager.PREFIX + "You are in region " + inRegion);
                } else {
                    player.sendMessage(MineralManager.PREFIX + "You are not in a region.");
                }
                return true;
            }
        });
        
        ChatMagic.registerAlias("{TEXT}", ChatColor.LIGHT_PURPLE);
        ChatMagic.registerAlias("{VERB}", ChatColor.GREEN);
	}
	//These methods all make the assumption that "args" contains the correct amount of arguments of the correct type.

	//****This method hasn't been cleaned up yet.
	public static void create(MineralManager plugin, Player player, List<Object> args) {
		String name = (String) args.get(0);
		String configName = ((String) args.get(1)).toLowerCase();
		int level = (Integer) args.get(2);
	
		Configuration configuration = plugin.getDefaultConfiguration();
		HashMap<String, Configuration> configurationMap = plugin.getConfigurationMap();
		if(configurationMap.containsKey(configName)) {
			configuration = configurationMap.get(configName);
		}
		
		Selection selection = plugin.getSelection(player);
		if(selection != null) {
			Region newRegion = actuallyCreateRegion(plugin, name, configuration, selection, player, level);
			boolean regionAdded = (newRegion != null);
			player.sendMessage(MineralManager.PREFIX + newRegion.getName() + (regionAdded ? " was" : " was not") + " added at level " + (int) newRegion.getLevel() + " with configuration " + newRegion.getConfiguration().getName() + ".");
		} else {
			player.sendMessage(MineralManager.PREFIX + "No region is currently selected.");
		}
	}
	
	// TODO refactor this to a utility module or something
	public static Region actuallyCreateRegion(MineralManager plugin, String name, Configuration configuration, Selection selection, Player player, int level) {
		Region newRegion = new Region(name, configuration, selection.getShape(), selection.getFloor(), selection.getCeil(), player.getWorld(), level);
		WorldData wdata = plugin.getWorldData(player.getWorld());
		boolean regionAdded = wdata.getRegionSet().add(newRegion);
		wdata.flagRegionSetDirty();
		return regionAdded? newRegion: null;
	}


	public static Selection selectWorld(MineralManager plugin, Player player, List<Object> validList) {
		player.sendMessage(MineralManager.PREFIX + "Selected whole world " + player.getWorld().getName());
		return new Selection(null, -1.0D, -1.0D);
	}
	
	//****This method hasn't been cleaned up yet.
	public static Selection selectLasso(MineralManager plugin, Player player, List<Object> args) {
		String toggle = (String) args.get(0);
		if(toggle.equalsIgnoreCase(START)) {
			beginLasso(player);
			player.sendMessage(MineralManager.PREFIX + "Recording positions as selection.");
		} else if(toggle.equalsIgnoreCase(END) && lassoCoordinateMap.containsKey(player.getName())) {
			return finishLasso(player, MineralManager.PREFIX);
		}
		return null;
	}

	public static void beginLasso(Player player) {
		//Tells the lassoListener that there is one more person listening for lasso selections.
		if(!lassoCoordinateMap.containsKey(player.getName())) {
			MineralManager.getInstance().lassoListener.add();
		}
		
		//Adds the player to the lassoCoordinateMap so points can be added.
		lassoCoordinateMap.put(player.getName(), new ArrayList<Coordinate>());
		new LassoWatcherTask(player.getName()).run();
	}
	
	public static Selection finishLasso(Player player, String prefix) {
		ArrayList<Point2D> boundaries = new ArrayList<Point2D>();
		ArrayList<Coordinate> temp = lassoCoordinateMap.get(player.getName());
		double floor = Integer.MAX_VALUE;
		double ceil = Integer.MIN_VALUE;
		for(Coordinate coordinate : temp) {
			double y = coordinate.getY();
			if (y < floor) {
				floor = y;
			}
			if (y > ceil) {
				ceil = y;
			}
			boundaries.add(new Point2D.Double(coordinate.getX(), coordinate.getZ()));
		}
		boundaries = ShapeUtils.reduceBoundaries(boundaries);
		
		Polygon poly = new Polygon();
		for (Point2D p: boundaries) {
			ShapeUtils.addPolyPoint(poly, p);
		}
		
		// Tells the lassoListener that there is one less person listening for lasso selections.
		MineralManager.getInstance().lassoListener.remove();
		
		//Removes the player from the lassoCoordinateMap since we completed our selection.
		Commands.lassoCoordinateMap.remove(player.getName());
		
		player.sendMessage(prefix + "Finished recording.");
		return new Selection(poly, floor, ceil);
	}

	//2 Arguments
	public static Selection selectCube(MineralManager plugin, Player player, List<Object> args) {
		int xzRadius = (Integer) args.get(0);
		int yRadius = (Integer) args.get(1);
		return selectCube(plugin, player, xzRadius, yRadius);
	}

	public static Selection selectCube(MineralManager plugin, Player player, int xzRadius, int yRadius) {

		int playerX = player.getLocation().getBlockX();
		int playerY = player.getLocation().getBlockY();
		int playerZ = player.getLocation().getBlockZ();
		int west = playerX - xzRadius;
		int south = playerZ - xzRadius;
		int east = playerX + xzRadius;
		int north = playerZ + xzRadius;
		
		Rectangle rect = new Rectangle(west, south, xzRadius *2, xzRadius * 2);

		player.sendMessage(MineralManager.PREFIX + "A cube spanning (" +  west + ", " + south + ") to (" + east + ", " + north + ") was selected.");
		return new Selection(rect, playerY - yRadius, playerY + yRadius);
	}
	
	//1 Argument
	public static Selection selectRegion(MineralManager plugin, Player player, List<Object> args) {
		String toggle = (String) args.get(0);
		if(toggle.equalsIgnoreCase(START)) {
			regionStartMap.put(player, new Coordinate(player.getLocation()));
			player.sendMessage(MineralManager.PREFIX + "Recording first position of selection.");
		} else if(toggle.equalsIgnoreCase(END) && regionStartMap.containsKey(player))  {
			Coordinate startCoordinate = regionStartMap.get(player);
			Coordinate endCoordinate = new Coordinate(player.getLocation());
			return actuallySelectRegion(plugin, player, startCoordinate.getLocation(), endCoordinate.getLocation(), MineralManager.PREFIX);
		}
		return null;
	}
	
	/** Not a command, but functionality used by the dialogue to do the actual selection. */
	public static Selection actuallySelectRegion(MineralManager plugin, Player player, Location startCoordinate, Location endCoordinate, String prefix) {
		int x1 = startCoordinate.getBlockX();
		int z1 = startCoordinate.getBlockZ();
		int x2 = endCoordinate.getBlockX();
		int z2 = endCoordinate.getBlockZ();
		
		double y1 = startCoordinate.getY();
		double y2 = endCoordinate.getY();
		
		Rectangle rect = new Rectangle(
				Math.min(x1, x2),
				Math.min(z1, z2),
				Math.abs(x1 - x2),
				Math.abs(z1 - z2)
		);
		
		player.sendMessage(prefix + "A cube spanning (" + x1 + ", " +  z1 + ") to (" + x2 + ", " + z2 + ") was selected.");
		return new Selection(rect, Math.min(y1, y2), Math.max(y1, y2));
	}

	//1 Argument
	public void remove(CallInfo call) {
		String status = "was not";
		String regionName = call.getArg(0);
		for (WorldData wdata : plugin.allWorldDatas()) {
			if(wdata.getRegionSet().remove(regionName)) {
				wdata.flagRegionSetDirty();
				status = "was";
			}
		}
		call.reply("%s %s removed", regionName, status);
	}
	
	//0 Arguments
	public void list(CallInfo call) {
		call.reply("{HEADER}[Region List]");
		Collection<WorldData> wds = plugin.allWorldDatas();
		boolean prefixWorld = (wds.size() > 1);
		for (WorldData wdata : wds) {
			RegionSet rs = wdata.getRegionSet();
			if (rs.size() == 0) continue;
			if (prefixWorld) call.reply("{TEXT}%s:", wdata.getWorldName());
			call.reply(rs.toColorizedString());
		}
	}

	//0 Arguments
	public void lock(CallInfo call) {
		Block targetBlock = call.getPlayer().getTargetBlock(null, 20); //The 20 is the maximum distance away a block can be to be "selected".
		if(targetBlock != null) {
			WorldData wdata = plugin.getWorldData(targetBlock.getWorld());
			String status;
			boolean new_flag;
			if(wdata.isLocked(targetBlock)) {
				status = "no longer";
				new_flag = false;
			} else {
				new_flag = true;
				status = "now";
			}
			wdata.getLockedBlocks().set(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ(), new_flag);
			call.reply("Target block is %s locked.", status);
		}
	}
	
	//0 Arguments
	public void creative(CallInfo call, Player player) {
		String status = "NORMAL";
		if(player.hasMetadata(MineralListener.METADATA_CREATIVE)) {
			player.removeMetadata(MineralListener.METADATA_CREATIVE, plugin);
		} else {
			player.setMetadata(MineralListener.METADATA_CREATIVE, new FixedMetadataValue(plugin, true));
			status = "CREATIVE";
		}
		call.reply("You are now in {HEADER}%s{TEXT} mode.", status);
	}
	
	public static void shutdown() {
		regionStartMap.clear();
		lassoCoordinateMap.clear();
	}

}

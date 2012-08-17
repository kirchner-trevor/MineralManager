package me.hellfire212.MineralManager;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import me.hellfire212.MineralManager.tasks.LassoWatcherTask;
import me.hellfire212.MineralManager.utils.ChatMagic;
import me.hellfire212.MineralManager.utils.ShapeUtils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public final class Commands {
	
	private static final String START = "start";
	private static final String END = "end";
	private static ConcurrentHashMap<Player, Coordinate> regionStartMap = new ConcurrentHashMap<Player, Coordinate>();
	public static ConcurrentHashMap<String, ArrayList<Coordinate>> lassoCoordinateMap = new ConcurrentHashMap<String, ArrayList<Coordinate>>();
	
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
	public static void remove(MineralManager plugin, Player player, List<Object> args) {
		String name = (String) args.get(0);
		String status = "was not";
		for (WorldData wdata : plugin.allWorldDatas()) {
			if(wdata.getRegionSet().remove(name)) {
				wdata.flagRegionSetDirty();
				status = "was";
			}
		}
		player.sendMessage(MineralManager.PREFIX + name + " " + status + " removed.");
	}
	
	//0 Arguments
	public static void list(MineralManager plugin, Player player, List<Object> args) {
		ChatMagic.send(player, "%s{HEADER}[Region List]", MineralManager.PREFIX);
		Collection<WorldData> wds = plugin.allWorldDatas();
		boolean prefixWorld = (wds.size() > 1);
		for (WorldData wdata : wds) {
			RegionSet rs = wdata.getRegionSet();
			if (rs.size() == 0) continue;
			if (prefixWorld) ChatMagic.send(player, "{TEXT}%s:", wdata.getWorldName());
			player.sendMessage(rs.toColorizedString());
		}
	}

	//0 Arguments
	public static void lock(MineralManager plugin, Player player, List<Object> args) {
		Block targetBlock = player.getTargetBlock(null, 20); //The 20 is the maximum distance away a block can be to be "selected".
		if(targetBlock != null) {
			Coordinate coordinate = new Coordinate(targetBlock.getLocation());
			String status = "no longer";
			if(!plugin.lockedSet.remove(coordinate)) {
				status = "now";
				plugin.lockedSet.add(coordinate);
			} 
			player.sendMessage(MineralManager.PREFIX + "Target block is " + status + " locked.");
			if(!plugin.lockedSetFH.saveObject(plugin.lockedSet)) {
				plugin.getServer().getLogger().severe("Failure occured when saving lockedSet during lock command!");
			}
		}
	}
	
	//0 Arguments
	public static void creative(MineralManager manager, Player player, List<Object> args) {
		String status = "NORMAL";
		if(player.hasMetadata(MineralListener.METADATA_CREATIVE)) {
			player.removeMetadata(MineralListener.METADATA_CREATIVE, manager);
		} else {
			player.setMetadata(MineralListener.METADATA_CREATIVE, new FixedMetadataValue(manager, true));
			status = "CREATIVE";
		}
		ChatMagic.send(player, "%s You are now in {HEADER}%s{TEXT} mode.", MineralManager.PREFIX, status);
	}
	
	public static void shutdown() {
		regionStartMap.clear();
		lassoCoordinateMap.clear();
	}

}

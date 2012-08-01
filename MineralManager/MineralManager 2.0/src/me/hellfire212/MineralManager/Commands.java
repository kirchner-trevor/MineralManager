package me.hellfire212.MineralManager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class Commands {
	
	private static final String START = "start";
	private static final String END = "end";
	private static ConcurrentHashMap<Player, Coordinate> regionStartMap = new ConcurrentHashMap<Player, Coordinate>();
	public static ConcurrentHashMap<Player, ArrayList<Coordinate>> lassoCoordinateMap = new ConcurrentHashMap<Player, ArrayList<Coordinate>>();
	
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
			Region newRegion = new Region(name, configuration, selection.getBoundaries(), selection.getFloor(), selection.getCeil(), player.getWorld(), level);
			boolean regionAdded = plugin.regionSet.add(newRegion);
			player.sendMessage(MineralManager.PREFIX + newRegion.getName() + (regionAdded ? " was" : " was not") + " added at level " + (int) newRegion.getLevel() + " with configuration " + newRegion.getConfiguration().getName() + ".");
			if(regionAdded) {
				if(!plugin.regionSetFH.saveObject(plugin.regionSet)) {
					plugin.getServer().getLogger().severe("Failure occured when saving regionSet during create command!");
				}
			}
		} else {
			player.sendMessage(MineralManager.PREFIX + "No region is currently selected.");
		}
	}
	
	//****This method hasn't been cleaned up yet.
	public static Selection selectLasso(MineralManager plugin, Player player, List<Object> args) {
		String toggle = (String) args.get(0);
		if(toggle.equalsIgnoreCase(START)) {
			//Tells the lassoListener that there is one more person listening for lasso selections.
			if(!Commands.lassoCoordinateMap.containsKey(player)) {
				plugin.lassoListener.add();
			}
			
			//Adds the player to the lassoCoordinateMap so points can be added.
			lassoCoordinateMap.put(player, new ArrayList<Coordinate>());
			
			player.sendMessage(MineralManager.PREFIX + "Recording positions as selection.");
		} else if(toggle.equalsIgnoreCase(END) && lassoCoordinateMap.containsKey(player)) {
			ArrayList<Point2D.Double> boundaries = new ArrayList<Point2D.Double>();
			ArrayList<Coordinate> temp = lassoCoordinateMap.get(player);
			double floor = Integer.MAX_VALUE;
			double ceil = Integer.MIN_VALUE;
			for(Coordinate coordinate : temp) {
				double y = coordinate.getY();
				if(y < floor) {
					floor = y;
				} else if(y > ceil) {
					ceil = y;
				}
				boundaries.add(new Point2D.Double(coordinate.getX(), coordinate.getZ()));
			}
			boundaries = reduceBoundaries(boundaries);
			if(!boundaries.isEmpty()) {
				boundaries.add(boundaries.get(0));
				boundaries.add(0, new Point2D.Double(0.0, 0.0));
				boundaries.add(new Point2D.Double(0.0, 0.0));
			}
			
			//Tells the lassoListener that there is one less person listening for lasso selections.
			plugin.lassoListener.remove();
			
			//Removes the player from the lassoCoordinateMap since we completed our selection.
			Commands.lassoCoordinateMap.remove(player);
			
			player.sendMessage(MineralManager.PREFIX + "Finished recording.");
			return new Selection(boundaries, floor, ceil);
		}
		return null;
	}
	
	//2 Arguments
	public static Selection selectCube(MineralManager plugin, Player player, List<Object> args) {
		int xzRadius = (Integer) args.get(0);
		int yRadius = (Integer) args.get(1);
		double playerX = player.getLocation().getX();
		double playerY = player.getLocation().getY();
		double playerZ = player.getLocation().getZ();
		double west = playerX - xzRadius;
		double south = playerZ - xzRadius;
		double east = playerX + xzRadius;
		double north = playerZ + xzRadius;
		
		ArrayList<Point2D.Double> boundaries = new ArrayList<Point2D.Double>(7);
		
		Point2D.Double zero = new Point2D.Double();
		Point2D.Double origin = new Point2D.Double(west, south);

		boundaries.add(zero);
		boundaries.add(origin);
		boundaries.add(new Point2D.Double(west, north));
		boundaries.add(new Point2D.Double(east, north));
		boundaries.add(new Point2D.Double(east, south));
		boundaries.add(origin);
		boundaries.add(zero);

		player.sendMessage(MineralManager.PREFIX + "A cube spanning (" + (int) west + ", " + (int) south + ") to (" + (int) east + ", " + (int) north + ") was selected.");
		return new Selection(boundaries, playerY - yRadius, playerY + yRadius);
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
			
			double x1 = startCoordinate.getX();
			double z1 = startCoordinate.getZ();
			double x2 = endCoordinate.getX();
			double z2 = endCoordinate.getZ();
			double y1 = startCoordinate.getY();
			double y2 = endCoordinate.getY();
			
			ArrayList<Point2D.Double> boundaries = new ArrayList<Point2D.Double>(7);
			
			Point2D.Double zero = new Point2D.Double();
			Point2D.Double origin = new Point2D.Double(x1, z1);

			boundaries.add(zero);
			boundaries.add(origin);
			boundaries.add(new Point2D.Double(x1, z2));
			boundaries.add(new Point2D.Double(x2, z2));
			boundaries.add(new Point2D.Double(x2, z1));
			boundaries.add(origin);
			boundaries.add(zero);
			
			player.sendMessage(MineralManager.PREFIX + "A cube spanning (" + (int) x1 + ", " + (int) z1 + ") to (" + (int) x2 + ", " + (int) z2 + ") was selected.");
			return new Selection(boundaries, Math.min(y1, y2), Math.max(y1, y2));
		}
		return null;
	}
	
	private static ArrayList<Point2D.Double> reduceBoundaries(ArrayList<Point2D.Double> boundaries) {
		int size = boundaries.size();
		int index = 0;
		while(index + 2 < size) {
			if(isBetween(boundaries.get(index), boundaries.get(index + 2), boundaries.get(index + 1))) {
				boundaries.remove(index + 1);
				size--;
			} else {
				index++;
			}
		}
		return boundaries;
	}
	
	private static boolean isBetween(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
		double epsilon = 0.05; //Threshold to determine whether a point is "on" the line.
		double cyMINUSay = c.y - a.y;
		double bxMINUSax = b.x - a.x;
		double cxMINUSax = c.x - a.x;
		double byMINUSay = b.y - a.y;
		double crossProduct = cyMINUSay * bxMINUSax - cxMINUSax * byMINUSay;
		if(Math.abs(crossProduct) > epsilon) {
			return false;
		}
		double dotProduct = cxMINUSax * bxMINUSax + cyMINUSay * byMINUSay;
		if(dotProduct < 0.0) {
			return false;
		}
		double squaredLengthBA = bxMINUSax * bxMINUSax + byMINUSay * byMINUSay;
		if(dotProduct > squaredLengthBA) {
			return false;
		}
		return true;
	}
	
	//1 Argument
	public static void remove(MineralManager plugin, Player player, List<Object> args) {
		String name = (String) args.get(0);
		String status = "was not";
		if(plugin.regionSet.remove(name)) {
			status = "was";
			if(!plugin.regionSetFH.saveObject(plugin.regionSet)) {
				plugin.getServer().getLogger().severe("Failure occured when saving regionSet during remove command!");
			}
		}
		player.sendMessage(MineralManager.PREFIX + name + " " + status + " removed.");
	}
	
	//0 Arguments
	public static void list(MineralManager plugin, Player player, List<Object> args) {
		player.sendMessage(MineralManager.PREFIX + MineralManager.HEADER_COLOR + "[Region List]" + MineralManager.TEXT_COLOR);
		player.sendMessage(plugin.regionSet.toString());
	}

	//****Issue when calling update, if blocks are added to the managedBlocks list the placeholder block doesn't work for the the first time they're mined.
	//1 Argument
	public static void update(MineralManager plugin, Player player, List<Object> args) {
		String name = (String) args.get(0);
		Region region = plugin.regionSet.get(name);
		String status = "Could not update";
		if(region != null) {
			Configuration configuration = plugin.getConfigurationMap().get(region.getConfiguration().getName());
			if(configuration != null) {
				plugin.regionSet.remove(region);
				region.setConfiguration(configuration);
				plugin.regionSet.add(region);
				status = "Successfully updated";
				if(!plugin.regionSetFH.saveObject(plugin.regionSet)) {
					plugin.getServer().getLogger().severe("Failure occured when saving regionSet during update command!");
				}
			}
		}
		player.sendMessage(MineralManager.PREFIX + status + " region \"" + name + "\".");
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
		player.sendMessage(MineralManager.PREFIX + "You are now in " + MineralManager.HEADER_COLOR + status + MineralManager.TEXT_COLOR + " mode.");
	}
}

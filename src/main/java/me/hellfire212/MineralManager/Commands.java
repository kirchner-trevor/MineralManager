package me.hellfire212.MineralManager;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import me.hellfire212.MineralManager.dialogue.CreateRegion;
import me.hellfire212.MineralManager.tasks.LassoWatcherTask;
import me.hellfire212.MineralManager.utils.ShapeUtils;
import mondocommand.CallInfo;
import mondocommand.ChatMagic;
import mondocommand.FormatConfig;
import mondocommand.MondoCommand;
import mondocommand.dynamic.Sub;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public final class Commands {
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
        // This is to set up the ordering, since iterating a class is not predictable.
        base.addSub("list");
        base.addSub("create");
        base.addSub("remove");
        base.addSub("lock");
        
        // Add all other subs automagically
        base.autoRegisterFrom(this);
        
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
	
	@Sub(description="Create a new region", allowConsole=false)
	public void create(CallInfo call) {
        new CreateRegion(plugin).begin(call.getPlayer(), call.getArgs());
	}

	@Sub(description="Remove MM region", minArgs=1, usage="region name")
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
	
	@Sub(description = "List MM Regions")
	public void list(CallInfo call) {
		call.reply("{HEADER}[Region List]");
		Collection<WorldData> wds = plugin.allWorldDatas();
		boolean prefixWorld = (wds.size() > 1);
		for (WorldData wdata : wds) {
			RegionSet rs = wdata.getRegionSet();
			if (rs.size() == 0) continue;
			if (prefixWorld) call.reply(false, "{TEXT}%s:", wdata.getWorldName());
			call.reply(false, rs.toColorizedString());
		}
	}

	@Sub(description = "Lock block on your cursor")
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
	
	@Sub(description = "Creative Mode", allowConsole = false)
	public void creative(CallInfo call) {
	    Player player = call.getPlayer();
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

    
    // TODO refactor this to a utility module or something
    public static Region actuallyCreateRegion(MineralManager plugin, String name, Configuration configuration, Selection selection, Player player, int level) {
        Region newRegion = new Region(name, configuration, selection.getShape(), selection.getFloor(), selection.getCeil(), level);
        WorldData wdata = plugin.getWorldData(player.getWorld());
        boolean regionAdded = wdata.getRegionSet().add(newRegion);
        wdata.flagRegionSetDirty();
        return regionAdded? newRegion: null;
    }


    public static Selection selectWorld(MineralManager plugin, Player player) {
        player.sendMessage(MineralManager.PREFIX + "Selected whole world " + player.getWorld().getName());
        return new Selection(null, -1.0D, -1.0D);
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
}

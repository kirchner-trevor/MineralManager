package me.hellfire212.MineralManager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import me.hellfire212.MineralManager.BlockInfo.Type;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class MineralListener implements Listener {
	
	public static final String METADATA_CREATIVE = "MineralManager.Creative";
	public static final String PERMISSION_USER = "MineralManager.User";
	public static final String PERMISSION_ADMIN = "MineralManager.Admin";
	private static final long UPDATE_PERIOD = 200;

	public static ConcurrentHashMap<Coordinate, Integer> taskMap = new ConcurrentHashMap<Coordinate, Integer>();
	
	private MineralManager plugin = null;

	public MineralListener(MineralManager p) {
		plugin = p;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new UpdateThread(plugin), 0, UPDATE_PERIOD);
	}
	
	/**
	 * Adds managed blocks to their respective Sets and sets up the cool down
	 * for blocks that are successfully mined.
	 * @param e the BlockBreakEvent
	 * @see EventHandler priority = MONITOR, ignoreCancelled = true
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent e) {
		Player player = e.getPlayer();
		Block block = e.getBlock();
		Coordinate coordinate = new Coordinate(block.getLocation());
		Region region = plugin.regionSet.contains(coordinate);
		
		if(player.hasMetadata(MineralListener.METADATA_CREATIVE)) {
			if(plugin.blockMap.containsKey(coordinate)) {
				cancelRespawnAtCoordinate(coordinate);
			}
			plugin.blockMap.remove(coordinate);
			plugin.placedSet.remove(coordinate);
			plugin.lockedSet.remove(coordinate);
			//Save sets as well possibly.
			return;
		}
		
		if(region != null) {

			Configuration configuration = region.getConfiguration();
			
			if(configuration.isVolatile() && plugin.blockMap.containsKey(coordinate)) {
				cancelRespawnAtCoordinate(coordinate);
				plugin.blockMap.remove(coordinate);
				plugin.placedSet.remove(coordinate);
				plugin.lockedSet.remove(coordinate);
				//Save sets as well possibly.
				return;
			}

			if(configuration.isActive()) {
				if(!(configuration.isUsePermissions() && player.hasPermission(PERMISSION_USER))) {

					BlockInfo placeholder = configuration.getPlaceholderBlock();
					HashMap<BlockInfo, Mineral> blockMap = configuration.getBlockMap();
					BlockInfo info = new BlockInfo(block.getTypeId(), block.getData(), placeholder.getTypeId(Type.PLACEHOLDER), placeholder.getData(Type.PLACEHOLDER));
					
					if(blockMap.containsKey(info)) {
						if(!(configuration.isMineOriginalOnly() && plugin.placedSet.contains(coordinate))) {

							if((configuration.isLocked() || plugin.lockedSet.contains(coordinate)) && player.getItemInHand().getEnchantments().toString().contains("SILK_TOUCH")) {
								block.breakNaturally();
							}
							
							Mineral mineral = blockMap.get(info);
							
							if(Math.random() > mineral.getDegrade()) {
								long cooldown = mineral.getCooldown();

								info.setRespawn(System.currentTimeMillis() + (cooldown * 1000));
								plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlaceholderThread(plugin, coordinate, info));
								int tid = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RespawnThread(plugin, coordinate, info), cooldown * 20);
								MineralListener.taskMap.put(coordinate, tid);
								
								String message = configuration.getOnBlockBreak();
								if(!message.equals("false")) {
									player.sendMessage(getCustomMessage(message, info, cooldown));
								}
							}
						} else {
							plugin.placedSet.remove(coordinate);
						}
					}
				}
			}
		}
	}

	/**
	 * This is used when a player destroys a block while in "creative" mode or when the block is in a volatile region and it's placeholder is destroyed.
	 * This method assumes that MineralManager.blockMap.containsKey(coordinate) is true, however this method will simply do nothing if it doesn't.
	 * MineralManager.blockMap is a parallel map to MineralListener.taskMap however blockMap contains BlockInfo's while taskMap contains task ID's.
	 * @param coordinate
	 */
	private void cancelRespawnAtCoordinate(Coordinate coordinate) {
		Integer tid;
		if((tid = MineralListener.taskMap.get(coordinate)) != null) {
			plugin.getServer().getScheduler().cancelTask(tid);
		}
		MineralListener.taskMap.remove(coordinate);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockDamageEvent(final BlockDamageEvent e) {
		Player player = e.getPlayer();
		//We don't want to spam players in creative mode with messages so we simply return immediately.
		if(player.hasMetadata(MineralListener.METADATA_CREATIVE)) {
			return;
		}

		Coordinate coordinate = new Coordinate(e.getBlock().getLocation());
		
		if(plugin.blockMap.containsKey(coordinate)) {
			Region region = plugin.regionSet.contains(coordinate);
			if(region != null) {
				Configuration configuration = region.getConfiguration();
				if(!(configuration.isUsePermissions() && !player.hasPermission(PERMISSION_USER))) {
					String message = configuration.getOnBlockProspect();
					if(!message.equals("false")) {
						BlockInfo info = plugin.blockMap.get(coordinate);
						Mineral mineral = configuration.getBlockMap().get(info);
						int cooldown = mineral != null ? mineral.getCooldown() : 0;
						player.sendMessage(getCustomMessage(message, info, cooldown));
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(final BlockPlaceEvent e) {
		//We don't do anything if the player is in creative mode since they're allowed to place "natural" blocks.
		if(e.getPlayer().hasMetadata(MineralListener.METADATA_CREATIVE)) {
			return;
		}
		
		plugin.placedSet.add(new Coordinate(e.getBlock().getLocation()));
		(new Thread(new Runnable() { public void run() { plugin.placedSetFH.saveObject(plugin.placedSet); } })).start();
	}
	
	private String getCustomMessage(String message, BlockInfo info, long cooldown) {
		String displayMessage = ChatColor.AQUA + "[" + plugin.getName() + "] " + ChatColor.WHITE + message;
		Material blockType = Material.getMaterial(info.getTypeId(Type.BLOCK));
		displayMessage = displayMessage.replaceAll("%b", ChatColor.GOLD + "" + blockType + ChatColor.WHITE);
		displayMessage = displayMessage.replaceAll("%c", ChatColor.GOLD + "" + cooldown + ChatColor.WHITE);
		displayMessage = displayMessage.replaceAll("%r", ChatColor.GOLD + "" + Math.round((info.getRespawn() - System.currentTimeMillis()) / 1000) + ChatColor.WHITE);
		return displayMessage;
	}
}

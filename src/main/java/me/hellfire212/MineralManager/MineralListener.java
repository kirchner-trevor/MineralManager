package me.hellfire212.MineralManager;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import me.hellfire212.MineralManager.BlockInfo.Type;
import me.hellfire212.MineralManager.tasks.PlaceholderTask;
import me.hellfire212.MineralManager.tasks.RespawnTask;
import me.hellfire212.MineralManager.utils.ChatMagic;
import me.hellfire212.MineralManager.utils.TimeFormat;

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

	public static ConcurrentHashMap<Coordinate, Integer> taskMap = new ConcurrentHashMap<Coordinate, Integer>();
	
	private MineralManager plugin = null;

	public MineralListener(MineralManager p) {
		plugin = p;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
		WorldData wdata = plugin.getWorldData(block.getWorld());
		Region region = wdata.getRegionSet().contains(coordinate);
		if(player.hasMetadata(METADATA_CREATIVE)) {
			if(plugin.blockMap.containsKey(coordinate)) {
				cancelRespawnAtCoordinate(coordinate);
				plugin.blockMap.remove(coordinate);
				plugin.blockMapFH.flagDirty();
			}
			wdata.getPlacedBlocks().unset(coordinate);
			wdata.getLockedBlocks().unset(coordinate);
			return;
		}
		
		if(region == null) return;
		
		Configuration configuration = region.getConfiguration();
		if(!configuration.isActive()) return;
		
		if(configuration.isVolatile() && plugin.blockMap.containsKey(coordinate)) {
			cancelRespawnAtCoordinate(coordinate);
			plugin.blockMap.remove(coordinate);
			wdata.getPlacedBlocks().unset(coordinate);
			wdata.getLockedBlocks().unset(coordinate);
			return;
		}

		
		if(configuration.isUsePermissions() && !player.hasPermission(PERMISSION_USER)) {
			return;
		}

		BlockInfo placeholder = configuration.getPlaceholderBlock();
		HashMap<BlockInfo, Mineral> blockMap = configuration.getBlockMap();
		BlockInfo info = new BlockInfo(block.getTypeId(), block.getData(), placeholder.getTypeId(Type.PLACEHOLDER), placeholder.getData(Type.PLACEHOLDER));
		
		if(!blockMap.containsKey(info)) return;
		
		if(!(configuration.isMineOriginalOnly() && wdata.wasPlaced(block))) {

			if((configuration.isLocked() || wdata.isLocked(block)) && player.getItemInHand().getEnchantments().toString().contains("SILK_TOUCH")) {
				block.breakNaturally();
			}
			
			Mineral mineral = blockMap.get(info);
			if(Math.random() > mineral.getDegrade()) {
				long cooldown = mineral.getCooldown();

				info.setRespawn(System.currentTimeMillis() + (cooldown * 1000));
				plugin.blockMap.put(coordinate, info);
				plugin.blockMapFH.flagDirty();
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new PlaceholderTask(plugin, coordinate, info));
				int tid = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new RespawnTask(plugin, coordinate, info), cooldown * 20);
				taskMap.put(coordinate, tid);
				
				String message = configuration.getOnBlockBreak();
				if(message != null) {
					player.sendMessage(getCustomMessage(message, info, cooldown));
				}
			}
		} else {
			wdata.getPlacedBlocks().unset(coordinate);
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
		if((tid = taskMap.get(coordinate)) != null) {
			plugin.getServer().getScheduler().cancelTask(tid);
		}
		taskMap.remove(coordinate);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockDamageEvent(final BlockDamageEvent e) {
		Player player = e.getPlayer();
		//We don't want to spam players in creative mode with messages so we simply return immediately.
		if(player.hasMetadata(METADATA_CREATIVE)) {
			return;
		}

		Coordinate coordinate = new Coordinate(e.getBlock().getLocation());
		
		if(plugin.blockMap.containsKey(coordinate)) {
			WorldData wdata = plugin.getWorldData(player.getWorld());
			Region region = wdata.getRegionSet().contains(coordinate);
			if(region != null) {
				Configuration configuration = region.getConfiguration();
				if(!configuration.isUsePermissions() || player.hasPermission(PERMISSION_USER)) {
					String message = configuration.getOnBlockProspect();
					if(message != null) {
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
		if(e.getPlayer().hasMetadata(METADATA_CREATIVE)) {
			return;
		}
		Block block = e.getBlock();
		WorldData wd = plugin.getWorldData(block.getWorld());
		wd.getPlacedBlocks().set(block.getX(), block.getY(), block.getZ(), true);
	}
	
	private String getCustomMessage(String message, BlockInfo info, long cooldown) {
		String displayMessage = ChatMagic.colorize("{AQUA}[%s] {WHITE}%s", plugin.getName(), message);
		Material blockType = Material.getMaterial(info.getTypeId(Type.BLOCK));
		displayMessage = displayMessage.replaceAll("%b", ChatMagic.colorize("{GOLD}%s{WHITE}", blockType));
		displayMessage = displayMessage.replaceAll("%c", ChatMagic.colorize("{GOLD}%d{WHITE}", TimeFormat.format(cooldown)));
		displayMessage = displayMessage.replaceAll("%r", ChatMagic.colorize("{GOLD}%d{WHITE}", TimeFormat.format(info.getCooldown())));
		return displayMessage;
	}
}
package me.hellfire212.MineralManager;

import java.io.File;
import java.io.IOException;

import org.bukkit.block.Block;

import me.hellfire212.MineralManager.datastructures.BlockBitmap;
import me.hellfire212.MineralManager.datastructures.ObjectMaker;
import me.hellfire212.MineralManager.utils.StringTools;

/**
 * Stores data local to a single world.
 *
 * This marshals data belonging to a given world, so that individual worlds 
 * can each have their own scope-local data.
 */
public final class WorldData {
	public static File BASE_FOLDER = new File("plugins/MineralManager/bin/");
	private final String worldName;
	private File worldFolder;
	private BlockBitmap placedBlocks;
	private RegionSet regionSet = new RegionSet();
	private RegionSetPersistence rsPersist;

	public WorldData(String worldName) {
		this.worldName = worldName;
		this.worldFolder = new File(BASE_FOLDER, StringTools.md5String(worldName));
		if (!worldFolder.exists()) worldFolder.mkdir();
		this.load();
	}
	
	private void load() {
		placedBlocks = new BlockBitmap(new File(worldFolder, MMConstants.PLACED_BLOCKS_FILENAME));
		SaveTracker.track(placedBlocks);
		
		rsPersist = new RegionSetPersistence(
				regionSet,
				new File(worldFolder, MMConstants.REGION_YAML_FILENAME)
		);
		SaveTracker.track(rsPersist);
	}
	
	public void shutdown() {
		try {
			placedBlocks.close();
		} catch (IOException e) {}
		placedBlocks = null;
	}
	
	/** Get the bitmap for placed blocks */
	public BlockBitmap getPlacedBlocks() {
		return placedBlocks;
	}
	
	/** Convenience function, check the placed array without having to get it first. */
	public boolean wasPlaced(int x, int y, int z) {
		return placedBlocks.get(x, y, z);
	}
	
	/** Convenience function for the use case of Block locations */
	public boolean wasPlaced(Block block) {
		return placedBlocks.get(block.getX(), block.getY(), block.getZ());
	}

	
	/** Get an ObjectMaker, for use with DefaultDict. */
	public static ObjectMaker<WorldData> getMaker() {
		return new ObjectMaker<WorldData>() {
			public WorldData build(Object key) {
				return new WorldData((String) key);
			}
		};
	}

	public String getWorldName() {
		return worldName;
	}
}

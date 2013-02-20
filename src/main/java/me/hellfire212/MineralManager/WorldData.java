package me.hellfire212.MineralManager;

import java.io.File;
import java.io.IOException;

import org.bukkit.block.Block;

import me.hellfire212.MineralManager.datastructures.BitmapChoice;
import me.hellfire212.MineralManager.datastructures.BlockBitmap;
import me.hellfire212.MineralManager.datastructures.ObjectMaker;
import me.hellfire212.MineralManager.tasks.SaveTracker;
import me.hellfire212.MineralManager.utils.StringTools;

/**
 * Stores data local to a single world.
 *
 * This marshals data belonging to a given world, so that individual worlds 
 * can each have their own scope-local data. It also ensures that a unique
 * folder name for the world is created.
 */
public final class WorldData {
	public static File BASE_FOLDER = new File("plugins/MineralManager/bin/");
	private final String worldName;
	private File worldFolder;
	private BlockBitmap placedBlocks;
	private BlockBitmap lockedBlocks;

	private RegionSet regionSet = new RegionSet();
	private RegionSetPersistence rsPersist;

	public WorldData(String worldName) {
		this.worldName = worldName;
		this.worldFolder = new File(BASE_FOLDER, StringTools.md5String(worldName));
		if (!worldFolder.exists()) worldFolder.mkdir();
		MineralManager.getInstance().addKnownWorld(worldName);
		this.load();
	}
	
	/** Load/initialize any enclosed data structures. */
	private void load() {
		placedBlocks = new BlockBitmap(new File(worldFolder, MMConstants.PLACED_BLOCKS_FILENAME));
		SaveTracker.track(placedBlocks);
		lockedBlocks = new BlockBitmap(new File(worldFolder, MMConstants.LOCKED_BLOCKS_FILENAME));
		//SaveTracker.track(lockedBlocks);
	
		rsPersist = new RegionSetPersistence(
				regionSet,
				new File(worldFolder, MMConstants.REGION_YAML_FILENAME)
		);
		SaveTracker.track(rsPersist);
	}
	
	/** 
	 * Shutdown this WorldData. 
	 * Do not use any data structures after this has been shut down.
	 */
	public void shutdown() {
		for (BitmapChoice b : BitmapChoice.values()) {
			try {
				getBitmapData(b).close();
			} catch (IOException e) {
				MineralManager.getInstance().getLogger().severe(String.format(
					"Could not save bit-map file %s in world '%s': (reason %s)",
					b.toString(), this.worldName, e.getMessage()
				));
			}
		}
		placedBlocks = null;
		lockedBlocks = null;
		regionSet = null;
		rsPersist.shutdown();
		rsPersist = null;
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
	
	public BlockBitmap getLockedBlocks() {
		return lockedBlocks;
	}
	
	public boolean isLocked(int x, int y, int z) {
		return lockedBlocks.get(x, y, z);
	}

	/** Convenience function for the use case of Block locations */
	public boolean isLocked(Block block) {
		return lockedBlocks.get(block.getX(), block.getY(), block.getZ());
	}

	/** Get the RegionSet for this world. */
	public RegionSet getRegionSet() {
		return regionSet;
	}
	
	/** 
	 * Flag that the RegionSet in question is dirty. 
	 * In the future we hope it can handle this itself. 
	 */
	public void flagRegionSetDirty() {
		rsPersist.flagDirty();
	}

	/** Get the associated world name. */
	public String getWorldName() {
		return worldName;
	}

	
	/** Get an ObjectMaker, for use with DefaultDict. */
	public static ObjectMaker<WorldData> getMaker() {
		return new ObjectMaker<WorldData>() {
			public WorldData build(Object key) {
				return new WorldData((String) key);
			}
		};
	}
	
	public BlockBitmap getBitmapData(BitmapChoice choice) {
		switch (choice) {
		case PLACED_BLOCKS:
			return placedBlocks;
		case LOCKED_BLOCKS:
			return lockedBlocks;
		}
		return null;
	}
}

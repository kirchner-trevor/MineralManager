package me.hellfire212.MineralManager.utils;

import java.io.File;
import java.util.Random;

import me.hellfire212.MineralManager.MineralManager;

/**
 * Manage the saving pattern of save-as-new-file-then-flip.
 * 
 * This works by managing the filenames of a temporary save file, 
 * as well as keeping a backup file as .old.
 * @author James Crasta
 *
 */
public class SaveFlipper {
	private static final Random rand = new Random();
	private final File originalFile;
	private final File oldBackupFile;
	private File saveTempFile = null;

	public SaveFlipper(File file) {
		this.originalFile = file;
		this.oldBackupFile = appendName(file, ".old");
	}
	
	/**
	 * Call this when we are finished saving.
	 * @return true if file flip succeeded, false otherwise.
	 */
	public boolean saveFinished() {
		if (!saveTempFile.exists()) {
			return false;
		}
		
		// Rename the original file to .old, if applicable.
		if (originalFile.exists()) {
			if (!originalFile.renameTo(oldBackupFile)) {
				if (oldBackupFile.delete()) {
					originalFile.renameTo(oldBackupFile);
				}
			}
		}
		// Rename the temp file to the original file.
		if (!saveTempFile.renameTo(originalFile)) {
			MineralManager.getInstance().getLogger().severe(String.format(
					"Could not rename file '%s' to %s'",
					saveTempFile.getAbsolutePath(),
					originalFile.getAbsolutePath()
			));
			return false;
		}
		return true;
	}
	
	/** Call me when save failed. */
	public void saveFailed() {
		MineralManager.getInstance().getLogger().severe(String.format(
				"Saving failed for '%s', temporary data may have been left at '%s'.",
				originalFile.getAbsolutePath(),
				saveTempFile.getAbsolutePath()
		));
	}
	
	/** Get the File which represents the temporary location we're going to save to. */
	public File getSaveTemp() {
		if (saveTempFile == null) {
			makeSaveTempFile();
		}
		return saveTempFile;
	}
	
	public File getOldBackupFile() {
		return oldBackupFile;
	}
	
	private void makeSaveTempFile() {
		File f = null;
		do {
			int flux = rand.nextInt(50000);
			f = appendName(originalFile, ".new" + flux);
		} while (f.exists());
		saveTempFile = f;
	}
	
	private File appendName(File orig, String addon) {
		return new File(originalFile.getParentFile(), originalFile.getName() + addon);
	}

}

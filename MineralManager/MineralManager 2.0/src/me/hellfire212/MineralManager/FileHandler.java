package me.hellfire212.MineralManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import me.hellfire212.MineralManager.utils.Saveable;

public class FileHandler {

	private static ConcurrentHashMap<File, FileHandler> fileMap = new ConcurrentHashMap<File, FileHandler>();
	
	private ReentrantReadWriteLock readWriteLock = null;
	private Lock read = null;
	private Lock write = null;
	private File file = null;
	
	/**
	 * Creates a new thread-safe FileHandler for reading and writing objects to a file. 
	 * There can be at most one FileHandler associated with any given file, if a FileHandler is created 
	 * that is to contain a File that another FileHandler already contains an IllegalArgumentException is thrown.
	 * This should generally only be used if it is known for certain no FileHandler is handling this file.
	 * @param f the file to be used for reading and writing.
	 * @see getFileHander
	 * @throws IllegalArgumentException
	 */
	public FileHandler(File f) throws IllegalArgumentException {
		if(fileMap.containsKey(f)) {
			throw new IllegalArgumentException("There is already a FileHandler associated with " + f.getName());
		} else {
			fileMap.put(f, this);
		}

		file = f;
		readWriteLock = new ReentrantReadWriteLock();
		read = readWriteLock.readLock();
		write = readWriteLock.writeLock();
	}
	
	/**
	 * Used to get an existing FileHandler associated with a file. This method is preferable to the constructor when
	 * it isn't known whether or not a FileHandler exists for this file.
	 * @param f the file that is associated with a FileHandler
	 * @return the FileHandler associated with the file or a new FileHandler if none exists.
	 */
	public static FileHandler getFileHandler(File f) {
		FileHandler temp = fileMap.get(f);
		if(temp == null) {
			return new FileHandler(f);
		} else {
			return temp;
		}
	}
	
	/**
	 * Loads an Object whose class is "type" from "file" and returns it if valid, else it returns null.
	 * @param file the file to be opened for reading.
	 * @param type the type of the object to be read.
	 * @return the object that was loaded from the file.
	 * @throws FileNotFoundException 
	 */
	public <T> T loadObject(Class<T> type) throws FileNotFoundException {
		if(!file.exists()) {
			throw new FileNotFoundException();
		}
		read.lock();
		try {
			InputStream is = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is));
			try {
				return type.cast(ois.readObject());
			} finally {
				ois.close();
			}
		} catch (FileNotFoundException e) {
			System.err.println("File load failed with error: " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.err.println("File read failed with error: " + e.getLocalizedMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Object class definition failed with error: " + e.getLocalizedMessage());
		} catch (ClassCastException e) {
			System.err.println("Object cast failed with error: " + e.getLocalizedMessage());
		} finally {
			read.unlock();
		}
		
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {}
		
		return null;
	}
	
	/**
	 * Saves an Object of the specified class to "file" and returns true on success.
	 * @param file the file to be opened for writing.
	 * @param object the object to be written to file.
	 * @return true if the save was successful.
	 */
	public <T> boolean saveObject(T object) {
		write.lock();
		try {
			OutputStream os = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(os));
			try {
				oos.writeObject(object);
			} finally {
				oos.close();
			}
			return true;
		} catch (FileNotFoundException e) {
			System.err.println("File save failed with error: " + e.getLocalizedMessage());
		} catch (IOException e) {
			System.err.println("File write failed with error: " + e.getLocalizedMessage());
		} finally {
			write.unlock();
		}
		return false;
	}

	@Override
	protected void finalize() {
		fileMap.remove(file);
	}
	n
	public <T> ObjectSaver<T> getSaver(T collection) {
		return new ObjectSaver<T>(collection);
	}
	
	class ObjectSaver<T> implements Saveable {
		private T collection;
		
		public ObjectSaver(T collection) {
			this.collection = collection;
		}
		@Override
		public boolean save(boolean force) {
			return saveObject(collection);
		}
		
	}
}
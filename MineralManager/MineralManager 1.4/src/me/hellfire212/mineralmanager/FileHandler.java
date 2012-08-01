package me.hellfire212.mineralmanager;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileHandler {

	private static Set<File> fileSet = Collections.synchronizedSet(new HashSet<File>());
	
	private final ReentrantReadWriteLock readWriteLock;
	private final Lock read;
	private final Lock write;
	private final File file;
	
	/**
	 * Creates a new thread-safe FileHandler for reading and writing objects to a file. 
	 * There can be at most one FileHandler associated with any given file, if a FileHandler is created 
	 * that is to contain a File that another FileHandler already contains an IllegalArgumentException is thrown.
	 * @param f - the file to be used for reading and writing.
	 * @throws IllegalArgumentException
	 */
	public FileHandler(File f) throws IllegalArgumentException {
		if(!fileSet.add(f)) {
			throw new IllegalArgumentException("There is already a FileHandler associated with " + f.getName());
		}

		file = f;
		readWriteLock = new ReentrantReadWriteLock();
		read = readWriteLock.readLock();
		write = readWriteLock.writeLock();
	}
	
	/**
	 * Loads an Object whose class is "type" from "file" and returns it if valid, else it returns null.
	 * @param file - the file to be opened for reading.
	 * @param type - the type of the object to be read.
	 * @return the object that was loaded from the file.
	 */
	public <T> T loadObject(Class<T> type) {
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
		return null;
	}
	
	/**
	 * Saves an Object of the specified class to "file" and returns true on success.
	 * @param file - the file to be opened for writing.
	 * @param object - the object to be written to file.
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
		fileSet.remove(file);
	}
}

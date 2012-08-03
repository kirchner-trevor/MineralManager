package MineralManagerTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import me.hellfire212.MineralManager.datastructures.BlockBitmap;

import org.junit.Before;
import org.junit.Test;

public class BlockBitmapTest {
	private BlockBitmap bitmap;
	private File binFile;

	@Before
	public void setUp() throws Exception {
		this.binFile = new java.io.File("tests/bitmap_test.bin");
		if (binFile.exists()) binFile.delete();
	}

	@Test
	public void testPersistence() {
		bitmap = new BlockBitmap(binFile);
		assertEquals(false, bitmap.get(512, 0, 769));
		bitmap.set(512, 0, 769, true);
		assertEquals(true, bitmap.get(512, 0, 769));
		bitmap.set(512, 0, 777, true);
		assertEquals(1, bitmap.flush());
		safeClose(bitmap);

		bitmap = new BlockBitmap(binFile);
		assertEquals(true, bitmap.get(512, 0, 769));
		assertEquals(true, bitmap.get(512, 0, 777));
		assertEquals(false, bitmap.get(512, 0, 778));
	
		assertEquals(524, binFile.length());
		safeClose(bitmap);
	}
	
	@Test
	public void testFileGrowth() {
		bitmap = new BlockBitmap(binFile);
		bitmap.set(500, 10, 9, true);
		assertEquals(1, bitmap.flush());
		safeClose(bitmap);
		assertEquals(524, binFile.length());
		
		bitmap = new BlockBitmap(binFile);
		bitmap.set(500, 10, 10, false);
		bitmap.set(1000, 20, 1000, true);
		assertEquals(2, bitmap.flush());

		assertEquals(true, bitmap.get(1000, 20, 1000));
		safeClose(bitmap);
		assertEquals(1048, binFile.length());
	}
	private void safeClose(BlockBitmap b) {
		try {
			b.close();
		} catch (IOException e) {
			Assert.fail("IOException in close");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

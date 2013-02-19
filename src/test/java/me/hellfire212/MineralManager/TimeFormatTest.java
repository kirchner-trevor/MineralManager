package MineralManagerTests;

import static org.junit.Assert.*;

import me.hellfire212.MineralManager.utils.TimeFormat;

import org.junit.Test;

public class TimeFormatTest {

	@Test
	public void testFormat() {
		assertEquals("0s", TimeFormat.format(0));
		assertEquals("45s", TimeFormat.format(45));
		assertEquals("1m30s", TimeFormat.format(90));
		assertEquals("30m", TimeFormat.format(1800));
		assertEquals("2h", TimeFormat.format(7200));
		assertEquals("2h30m", TimeFormat.format(9000));
		assertEquals("2h30m50s", TimeFormat.format(9050));
	}
	
	@Test
	public void testParseNormal() {
		assertEquals(1500, TimeFormat.parse("025m"));
		assertEquals(1800, TimeFormat.parse("30m"));
		assertEquals(1850, TimeFormat.parse("30m, 50s"));
		assertEquals(9000, TimeFormat.parse("2h 30m"));
		assertEquals(9050, TimeFormat.parse("2h30m50s"));
		assertEquals(7250, TimeFormat.parse("2h50s"));
	}
	
	@Test
	public void testParseUndecorated() {
		assertEquals(45, TimeFormat.parse("45"));
		assertEquals(1800, TimeFormat.parse("1800"));
	}
	
	@Test
	public void testParseInvalidBlank() {
		assertEquals(0, TimeFormat.parse(""));
	}
	
	@Test(expected = NumberFormatException.class)
	public void testParseInvalidChar() throws Exception {
		TimeFormat.parse("h");
	}
}

 package br.com.bluesoft.commons.lang;
 
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.junit.Test;
 
 public class NumberUtilTest {
 
 	@Test
 	public void testToInt() {
 		assertEquals(4, NumberUtil.toInt(BigDecimal.valueOf(4d)));
 		assertEquals(4, NumberUtil.toInt(BigDecimal.valueOf(4.123456789d)));
 		assertEquals(4, NumberUtil.toInt(4.123456789));
 		assertEquals(4, NumberUtil.toInt("4"));
 		assertEquals(0, NumberUtil.toInt("abcde"));
 		assertEquals(0, NumberUtil.toInt((Object) null));
 		assertEquals(0, NumberUtil.toInt((BigDecimal) null));
 		assertEquals(10, NumberUtil.toInt((Object) BigDecimal.valueOf(10)));
 		assertEquals(Integer.valueOf(99), NumberUtil.toInt("abcde", 99));
 		assertEquals(Integer.valueOf(10), NumberUtil.toInt("10", Integer.valueOf(0)));
 	}
 
 	@Test
 	public void testToLong() {
 		assertEquals(4L, NumberUtil.toLong(BigDecimal.valueOf(4d)));
 		assertEquals(4L, NumberUtil.toLong(BigDecimal.valueOf(4.123456789d)));
 		assertEquals(4L, NumberUtil.toLong(4.123456789));
 		assertEquals(4L, NumberUtil.toLong("4"));
 		assertEquals(Long.valueOf(4), NumberUtil.toLong("4", 0L));
 		assertEquals(0L, NumberUtil.toLong("abcde"));
 		assertEquals(10L, NumberUtil.toLong(BigDecimal.valueOf(10)));
 		assertEquals(0L, NumberUtil.toLong((BigDecimal) null));
 		assertEquals(10L, NumberUtil.toLong((Object) BigDecimal.valueOf(10)));
 		assertEquals(Long.valueOf(99L), NumberUtil.toLong("abcde", 99L));
 	}
 
 	@Test
 	public void testToBigDecimal() {
 		assertEquals(new BigDecimal("1.5"), NumberUtil.toBigDecimal(new BigDecimal("1.5")));
 		assertEquals(new BigDecimal("10"), NumberUtil.toBigDecimal(10));
 		assertEquals(new BigDecimal("10.55"), NumberUtil.toBigDecimal("10.55"));
 		assertEquals(new BigDecimal("10.543448"), NumberUtil.toBigDecimal("10.543448"));
 		assertEquals(new BigDecimal("10"), NumberUtil.toBigDecimal(10L));
 		assertEquals(new BigDecimal("10.65"), NumberUtil.toBigDecimal(10.65f));
 		assertEquals(BigDecimal.ZERO, NumberUtil.toBigDecimal((BigDecimal) null));
 		assertEquals(BigDecimal.ZERO, NumberUtil.toBigDecimal("afgd"));
 	}
 
 	@Test
 	public void testToDouble() {
 		assertEquals(10d, NumberUtil.toDouble(10), 0);
 		assertEquals(10.55, NumberUtil.toDouble("10.55"), 0);
 		assertEquals(10.543, NumberUtil.toDouble("10543", 3), 0);
 		assertEquals(3d, NumberUtil.toDouble("aaa", 3d), 0);
 		assertEquals(100d, NumberUtil.toDouble("100", 3d), 0);
 		assertEquals(10d, NumberUtil.toDouble(10L), 0);
 		assertEquals(10.65d, NumberUtil.toDouble(10.65f), 0);
 		assertEquals(0d, NumberUtil.toDouble((Long) null), 0);
 		assertEquals(10d, NumberUtil.toDouble((Object) BigDecimal.valueOf(10)), 0);
 		assertEquals(10d, NumberUtil.toDouble(BigDecimal.valueOf(10.10), 0), 0);
 		assertEquals(0d, NumberUtil.toDouble((Object) null, 0), 0);
 		assertEquals(10d, NumberUtil.toDouble(10d, 0), 0);
 		assertEquals(10d, NumberUtil.toDouble(BigDecimal.valueOf(10)), 0);
 	}
 
 	@Test
 	public void testToZeroIfNull() {
 		assertEquals(BigDecimal.valueOf(1.0), NumberUtil.toZeroIfNull(new BigDecimal("1.0")));
 		assertEquals(10.54d, NumberUtil.toZeroIfNull(10.54d), 0);
 		assertEquals(12.6f, NumberUtil.toZeroIfNull(12.6f), 0);
 		assertEquals(104, NumberUtil.toZeroIfNull(104), 0);
 		assertEquals(25L, NumberUtil.toZeroIfNull(25L), 0);
 
 		final BigDecimal bigDecimal = null;
 		assertEquals(BigDecimal.ZERO, NumberUtil.toZeroIfNull(bigDecimal));
 
 		final Long longNumber = null;
 		assertEquals(0L, NumberUtil.toZeroIfNull(longNumber));
 
 		final Integer integerNumber = null;
 		assertEquals(0, NumberUtil.toZeroIfNull(integerNumber));
 
 		final Double doubleNumber = null;
 		assertEquals(0d, NumberUtil.toZeroIfNull(doubleNumber), 0);
 
 		final Float floatNumber = null;
 		assertEquals(0f, NumberUtil.toZeroIfNull(floatNumber), 0);
 	}
 
 	@Test
 	public void testToIntegerArray() {
 		final Integer[] numeros = new Integer[] { 1, 13, 25 };
 		assertEquals(numeros[1], NumberUtil.toIntegerArray(new int[] { 1, 13, 25 })[1]);
 		assertEquals(numeros[0], NumberUtil.toIntegerArray(new String[] { "1", "13", "25" })[0]);
 		assertEquals(numeros[2], NumberUtil.toIntegerArray(new Double[] { 1.9, 13.5, 25.7 })[2]);
 		assertEquals(numeros[2], NumberUtil.toIntegerArray(new Double[] { 1.9, 13.5, 25.7 })[2]);
 		assertEquals(0, NumberUtil.toIntegerArray((Object[]) null).length);
 	}
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testToLongArray() {
 		final Long[] array = new Long[] { 1L, 13L, 25L };
 		assertEquals(array[1], NumberUtil.toLongArray(new Long[] { 1L, 13L, 25L })[1]);
 		assertEquals(array[0], NumberUtil.toLongArray(new String[] { "1", "13", "25" })[0]);
 		assertEquals(array[2], NumberUtil.toLongArray(new Double[] { 1.9, 13.5, 25.7 })[2]);
 		assertEquals(0, NumberUtil.toLongArray((Object[]) null).length);
 	}
 
 	@Test
 	public void testToIntegerSet() {
 		final Set<Integer> numeros = new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 2, 3, 4, 5 }));
 		assertEquals(numeros, NumberUtil.toIntegerSet(new Integer[] { 1, 2, 3, 4, 5 }));
 		assertEquals(numeros, NumberUtil.toIntegerSet(new String[] { "1", "2", "3", "4", "5" }));
 		assertEquals(numeros, NumberUtil.toIntegerSet(new Character[] { '1', '2', '3', '4', '5' }));
 		assertEquals(numeros, NumberUtil.toIntegerSet(new Float[] { 1f, 2f, 3f, 4f, 5f }));
 		assertEquals(0, NumberUtil.toIntegerSet((Object[]) null).size());
 	}
 
 	@Test
 	public void testToLongSet() {
 		final Set<Long> numeros = new HashSet<Long>(Arrays.asList(new Long[] { 1L, 2L, 3L, 4L, 5L }));
 		assertEquals(numeros, NumberUtil.toLongSet(new Integer[] { 1, 2, 3, 4, 5 }));
 		assertEquals(numeros, NumberUtil.toLongSet(new String[] { "1", "2", "3", "4", "5" }));
 		assertEquals(numeros, NumberUtil.toLongSet(new Character[] { '1', '2', '3', '4', '5' }));
 		assertEquals(numeros, NumberUtil.toLongSet(new Float[] { 1f, 2f, 3f, 4f, 5f }));
 		assertEquals(new HashSet<Long>(0), NumberUtil.toLongSet(null));
 		assertEquals(0, NumberUtil.toLongSet((Object[]) null).size());
 	}
 
 	@Test
 	public void testToIntArray() {
 		final int[] numeros = new int[] { 1, 13, 25 };
 		assertEquals(numeros[0], NumberUtil.toIntArray(new HashSet<Integer>(Arrays.asList(new Integer[] { 1, 13, 25 })))[0]);
 		assertEquals(numeros[1], NumberUtil.toIntArray(new String[] { "1", "13", "25" })[1]);
 		assertEquals(numeros[2], NumberUtil.toIntArray(new Double[] { 1.9, 13.5, 25.7 })[2]);
 		assertEquals(0, NumberUtil.toIntArray((Collection<?>) null).length);
 		assertEquals(0, NumberUtil.toIntArray((Object[]) null).length);
 	}
 
 	@Test
 	public void testToIntegerArrayRecebendoCollection() {
 
 		final Set<Integer> collection = new LinkedHashSet<Integer>();
 		collection.add(Integer.valueOf(1));
 		collection.add(Integer.valueOf(13));
 		collection.add(Integer.valueOf(25));
 
 		final Integer[] numeros = new Integer[] { 1, 13, 25 };
 
 		assertEquals(numeros[0], NumberUtil.toIntegerArray(collection)[0]);
 		assertEquals(numeros[1], NumberUtil.toIntegerArray(collection)[1]);
 		assertEquals(numeros[2], NumberUtil.toIntegerArray(collection)[2]);
 
 		assertEquals(0, NumberUtil.toIntegerArray((Collection<?>) null).length);
 		assertEquals(0, NumberUtil.toIntegerArray((Object[]) null).length);
 	}
 
 	@Test
 	public void testToStringArray() {
 		assertEquals(new String[] { "1.9", "13.5", "25.7" }[0], NumberUtil.toStringArray(new Double[] { 1.9, 13.5, 25.7 })[0]);
 		assertEquals(new String[] { "1", "2", "3", "4", "5" }[1], NumberUtil.toStringArray(new Integer[] { 1, 2, 3, 4, 5 })[1]);
 	}
 
 	@Test
 	public void testTiraZeroAEsquerda() {
 		assertEquals("125", NumberUtil.tiraZeroAEsquerda("0000125"));
 		assertEquals("259633", NumberUtil.tiraZeroAEsquerda("0259633"));
 		assertEquals("1", NumberUtil.tiraZeroAEsquerda("00000001"));
 	}
 
 	@Test
 	public void testArrendondar() {
 		assertEquals(168.70, NumberUtil.arredondar(168.6988165488, 2), 0);
 		assertEquals(10.1, NumberUtil.arredondar(10.099999, 1), 0);
 		assertEquals(10.4, NumberUtil.arredondar(10.45, 1, RoundingMode.HALF_DOWN), 0);
 		assertEquals(10.5, NumberUtil.arredondar(10.45, 1, RoundingMode.HALF_UP), 0);
 	}
 
 	@Test
 	public void testIsNaN() {
 		assertFalse(NumberUtil.isNaN("10"));
 		assertFalse(NumberUtil.isNaN("10.55"));
 		assertTrue(NumberUtil.isNaN("10,55"));
 		assertTrue(NumberUtil.isNaN("abcde"));
 		assertTrue(NumberUtil.isNaN("145a"));
 	}
 
 	@Test
 	public void testTruncate() {
 		assertEquals(10.55, NumberUtil.truncate(10.5555, 2), 0);
 		assertEquals(10.123456, NumberUtil.truncate(10.123456789, 6), 0);
 		assertEquals(10.1, NumberUtil.truncate(10.1, 10), 0);
 		assertEquals(10d, NumberUtil.truncate(10.9877, 0), 0);
 	}
 
 	@Test
 	public void testParseDoubleLocale() throws ParseException {
 		assertEquals(1000.18, NumberUtil.parseDoubleLocale("1.000,18"), 0);
 		assertEquals(1222333.44, NumberUtil.parseDoubleLocale("1.222.333,44"), 0);
 		assertEquals(10d, NumberUtil.parseDoubleLocale("10"), 0);
 	}
 
 	@Test
 	public void testToDoubleLocale() {
 		assertEquals(1000.18, NumberUtil.toDoubleLocale("1.000,18"), 0);
 		assertEquals(1222333.44, NumberUtil.toDoubleLocale("1.222.333,44"), 0);
 		assertEquals(10d, NumberUtil.toDoubleLocale("10", 0d), 0);
 		assertEquals(5d, NumberUtil.toDoubleLocale("abcde", 5d), 0);
 		assertEquals(0d, NumberUtil.toDoubleLocale("abcde"), 0);
 	}
 
 	@Test
 	public void testToLongLocale() {
 		assertEquals(1000L, NumberUtil.toLongLocale("1.000,18"));
 		assertEquals(1222333L, NumberUtil.toLongLocale("1.222.333,44"));
 		assertEquals(0L, NumberUtil.toLongLocale("abcde"));
 	}
 
 	@Test
 	public void testToIntLocale() {
 		assertEquals(1000, NumberUtil.toIntLocale("1.000,18"));
 		assertEquals(1222333, NumberUtil.toIntLocale("1.222.333,44"));
 		assertEquals(0, NumberUtil.toIntLocale("abcde"));
 	}
 
 	@Test(expected = ParseException.class)
 	public void testParseDoubleLocaleOfAnInvalidNumber() throws ParseException {
 		NumberUtil.parseDoubleLocale("abcde");
 	}
 
 	@Test
 	public void isZeroOrNull() {
 
 		assertTrue(NumberUtil.isZeroOrNull(null));
 		assertTrue(NumberUtil.isZeroOrNull((byte) 0));
 		assertTrue(NumberUtil.isZeroOrNull((short) 0));
 		assertTrue(NumberUtil.isZeroOrNull(0));
 		assertTrue(NumberUtil.isZeroOrNull(0L));
 		assertTrue(NumberUtil.isZeroOrNull(0F));
 		assertTrue(NumberUtil.isZeroOrNull(0D));
 
 		assertFalse(NumberUtil.isZeroOrNull((byte) 1));
 		assertFalse(NumberUtil.isZeroOrNull((short) 2));
 		assertFalse(NumberUtil.isZeroOrNull(3));
 		assertFalse(NumberUtil.isZeroOrNull(4L));
 		assertFalse(NumberUtil.isZeroOrNull(5F));
 		assertFalse(NumberUtil.isZeroOrNull(6D));
 
 	}
 
 	@Test
 	public void isNullOrNegative() {
 
 		assertTrue(NumberUtil.isNullOrNegative(null));
 		assertTrue(NumberUtil.isNullOrNegative((byte) -1));
 		assertTrue(NumberUtil.isNullOrNegative((short) -1));
 		assertTrue(NumberUtil.isNullOrNegative(-1));
 		assertTrue(NumberUtil.isNullOrNegative(-1L));
 		assertTrue(NumberUtil.isNullOrNegative(-1F));
 		assertTrue(NumberUtil.isNullOrNegative(-1D));
 
 		assertFalse(NumberUtil.isNullOrNegative((byte) 1));
 		assertFalse(NumberUtil.isNullOrNegative((short) 2));
 		assertFalse(NumberUtil.isNullOrNegative(3));
 		assertFalse(NumberUtil.isNullOrNegative(4L));
 		assertFalse(NumberUtil.isNullOrNegative(5F));
 		assertFalse(NumberUtil.isNullOrNegative(6D));
 
 	}
 
 	@Test
 	public void testDecimalFormat() {
 		assertEquals("10,99", NumberUtil.formatDecimal(10.999));
 		assertEquals("10,99", NumberUtil.formatDecimal(10.9999999999999));
 		assertEquals("10,9", NumberUtil.formatDecimal(10.9999999999999, 1));
 		assertEquals("10,999", NumberUtil.formatDecimal(10.9999999999999, 3));
 		assertEquals("10,9999", NumberUtil.formatDecimal(10.9999999999999, 4));
 		assertEquals("10,99999", NumberUtil.formatDecimal(10.9999999999999, 5));
 		assertEquals("10", NumberUtil.formatDecimal(10, 0, 2));
 		assertEquals("10,5", NumberUtil.formatDecimal(10.5, 0, 2));
 		assertEquals("10,54", NumberUtil.formatDecimal(10.5444444, 1, 2));
 	}
 
 	@Test
 	public void testPercentFormat() {
 		assertEquals("99%", NumberUtil.formatPercent(0.99));
 		assertEquals("50%", NumberUtil.formatPercent(0.5));
 		assertEquals("5%", NumberUtil.formatPercent(0.05));
 		assertEquals("99,5%", NumberUtil.formatPercent(0.9954));
 		assertEquals("99,6%", NumberUtil.formatPercent(0.9959));
 	}
 
 	@Test
 	public void testCurrencyFormat() {
 		assertEquals("R$ 0,99", NumberUtil.formatCurrency(0.99));
 		assertEquals("R$ 1,00", NumberUtil.formatCurrency(0.9955));
 		assertEquals("R$ 2,99", NumberUtil.formatCurrency(2.98999));
 	}
 
 	@Test
 	public void toPercent() {
 		assertEquals(0d, NumberUtil.toPercent(0), 0d);
 		assertEquals(0d, NumberUtil.toPercent(0d), 0d);
 		assertEquals(0.1d, NumberUtil.toPercent(10), 0d);
 		assertEquals(00.9999d, NumberUtil.toPercent(99.99), 0d);
 	}
 
 }

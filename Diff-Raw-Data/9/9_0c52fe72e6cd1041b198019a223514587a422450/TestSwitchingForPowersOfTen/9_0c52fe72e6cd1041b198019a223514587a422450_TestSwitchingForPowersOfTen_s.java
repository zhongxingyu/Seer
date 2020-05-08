 import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
 import junit.framework.Assert;
 import junit.framework.AssertionFailedError;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.text.ParseException;
 
 @SuppressWarnings("ConstantConditions")
 public class TestSwitchingForPowersOfTen extends AbstractBenchmark {
 	private static final boolean THROW_EXCEPTION_FROM_DEFAULT = false;
 	private static final int TESTS_PER_ITER = 20000;
 	private static final int NUM_ITER = 1000;
	private static final int MAX_POWER = 10;
 
 	private static final int[] RANDOM_INTS = new int[TESTS_PER_ITER];
 	private static final double[] RANDOM_DOUBLES = new double[TESTS_PER_ITER];
 	private static final int MAX_NUM_CASES = 32;
 	private static final long[] RESULT = new long[MAX_NUM_CASES + 1];
 	static {
 		for (int i = 0; i < MAX_NUM_CASES; i++)
 			RESULT[i] = 0L;
 	}
 
 	public TestSwitchingForPowersOfTen() {
 
 	}
 
 	@BeforeClass
 	public static void setup() {
 		fillRandomInts();
 		fillRandomDoubles();
 		// printCheckRandoms();
 		System.out.println("Starting tests.");
 	}
 
 	private static void fillRandomInts() {
 		for (int i = 0; i < TESTS_PER_ITER; i++) {
 			RANDOM_INTS[i] = RandomFactory.randomInt(MAX_POWER);
 		}
 	}
 
 	private static void fillRandomDoubles() {
 		for (int i = 0; i < TESTS_PER_ITER; i++) {
 			RANDOM_DOUBLES[i] = RandomFactory.randomDouble();
 		}
 	}
 
 	@SuppressWarnings("UnusedDeclaration")
 	private static void printCheckRandoms() {
 		System.out.print("First 20 random ints: ");
 		for (int i = 0; i < 20; i++) {
 			if (i == 19)
 				System.out.println(RANDOM_INTS[i]);
 			else
 				System.out.print(RANDOM_INTS[i] + ", ");
 		}
 
 		System.out.print("First 20 random doubles: ");
 		for (int i = 0; i < 20; i++) {
 			if (i == 19)
 				System.out.println(RANDOM_DOUBLES[i]);
 			else
 				System.out.print(RANDOM_DOUBLES[i] + ", ");
 		}
 	}
 
 	@AfterClass
 	public static void printResultToPreventOptimization() {
 		System.out.println("Tests finished.");
 		for (int i = 0; i <= MAX_NUM_CASES; i++) {
 			System.out.print(i + "=" + RESULT[i] + ", ");
 		}
 		System.out.println();
 
 		final long baseResult = RESULT[MAX_NUM_CASES];
 		for (int i = MAX_NUM_CASES; i >= MAX_POWER; i--) {
 			try {
 				Assert.assertEquals(baseResult, RESULT[i]);
 				// Assert.assertEquals(Math.abs(result - RESULT[i]) < 15000, true);
 			} catch (AssertionFailedError e) {
 				System.out.println("Bad result at index=" + i + " with result=" + RESULT[i] + " != baseResult=" + baseResult);
 				throw e;
 			}
 		}
 	}
 
 	@Test
 	public void test32CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 32;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case32MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case32MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			case 26:
 				return val * 1000000000000000000L * 100000000;
 			case 27:
 				return val * 1000000000000000000L * 1000000000;
 			case 28:
 				return val * 1000000000000000000L * 10000000000L;
 			case 29:
 				return val * 1000000000000000000L * 100000000000L;
 			case 30:
 				return val * 1000000000000000000L * 1000000000000L;
 			case 31:
 				return val * 1000000000000000000L * 10000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test31CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 31;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case31MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case31MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			case 26:
 				return val * 1000000000000000000L * 100000000;
 			case 27:
 				return val * 1000000000000000000L * 1000000000;
 			case 28:
 				return val * 1000000000000000000L * 10000000000L;
 			case 29:
 				return val * 1000000000000000000L * 100000000000L;
 			case 30:
 				return val * 1000000000000000000L * 1000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test30CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 30;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case30MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case30MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			case 26:
 				return val * 1000000000000000000L * 100000000;
 			case 27:
 				return val * 1000000000000000000L * 1000000000;
 			case 28:
 				return val * 1000000000000000000L * 10000000000L;
 			case 29:
 				return val * 1000000000000000000L * 100000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test29CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 29;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case29MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case29MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			case 26:
 				return val * 1000000000000000000L * 100000000;
 			case 27:
 				return val * 1000000000000000000L * 1000000000;
 			case 28:
 				return val * 1000000000000000000L * 10000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test28CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 28;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case28MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case28MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			case 26:
 				return val * 1000000000000000000L * 100000000;
 			case 27:
 				return val * 1000000000000000000L * 1000000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test27CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 27;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case27MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	private static double case27MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			case 26:
 				return val * 1000000000000000000L * 100000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test26CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 26;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case26MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case26MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			case 25:
 				return val * 1000000000000000000L * 10000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test25CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 25;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case25MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case25MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			case 24:
 				return val * 1000000000000000000L * 1000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test24CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 24;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case24MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case24MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			case 23:
 				return val * 1000000000000000000L * 100000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test23CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 23;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case23MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case23MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			case 22:
 				return val * 1000000000000000000L * 10000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test22CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 22;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case22MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case22MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			case 21:
 				return val * 1000000000000000000L * 1000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test21CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 21;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case21MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case21MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			case 20:
 				return val * 1000000000000000000L * 100;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test20CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 20;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case20MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case20MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			case 19:
 				return val * 1000000000000000000L * 10;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test19CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 19;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case19MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case19MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			case 18:
 				return val*1000000000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test18CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 18;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case18MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case18MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			case 17:
 				return val*100000000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test17CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 17;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case17MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case17MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			case 16:
 				return val*10000000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test16CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 16;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case16MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case16MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			case 15:
 				return val*1000000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test15CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 15;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case15MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case15MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			case 14:
 				return val*100000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test14CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 14;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case14MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case14MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			case 13:
 				return val*10000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test13CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 13;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case13MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case13MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			case 12:
 				return val*1000000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test12CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 12;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case12MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case12MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			case 11:
 				return val*100000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test11CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 11;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case11MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case11MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			case 10:
 				return val*10000000000L;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test10CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 10;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case10MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case10MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			case 9:
 				return val*1000000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test9CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 9;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case9MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case9MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			case 8:
 				return val*100000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test8CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 8;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case8MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case8MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			case 7:
 				return val*10000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test7CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 7;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case7MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case7MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			case 6:
 				return val*1000000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test6CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 6;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case6MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case6MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			case 5:
 				return val*100000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test5CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 5;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case5MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case5MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			case 4:
 				return val*10000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test4CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 4;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case4MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case4MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			case 3:
 				return val*1000;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test3CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 3;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case3MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case3MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			case 2:
 				return val*100;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test2CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 2;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case2MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case2MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			case 1:
 				return val*10;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test1CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 1;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case1MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case1MultiplyByPowerOfTen(double val, int power) throws ParseException {
 		switch (power)
 		{
 			case 0:
 				return val;
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 
 	@Test
 	public void test0CaseSwitchingMultiplyByPowersOfTen() {
 		final int numCases = 0;
 		if (MAX_POWER > numCases)
 			return;
 
 		try
 		{
 			for (int i = 0; i < NUM_ITER; i++) {
 				for (int j = 0; j < TESTS_PER_ITER; j++) {
 					final double d = RANDOM_DOUBLES[j];
 					final double multiplied = case0MultiplyByPowerOfTen(d, RANDOM_INTS[j]);
 					
 					RESULT[numCases] += Double.doubleToLongBits(multiplied);
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private static double case0MultiplyByPowerOfTen(@SuppressWarnings("UnusedParameters") double val, int power) throws ParseException {
 		switch (power)
 		{
 			default:
 				if (THROW_EXCEPTION_FROM_DEFAULT)
 					throw new ParseException("Unhandled power of ten: " + power, 0);
 				else
 					return val;
 		}
 	}
 }

 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.test;
 
 import us.exultant.ahs.log.*;
 import us.exultant.ahs.util.*;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 public abstract class TestCase implements Runnable {
 	/**
 	 * @param $log
 	 *                Fatal failures that cause the entire case to fail to complete
 	 *                are logged at ERROR level; failed assertions without a unit are
 	 *                logged at WARN level; units that pass as logged at INFO level,
 	 *                and confirmations of individually passed assertions (if enabled)
 	 *                are logged at DEBUG level.
 	 * @param $enableConfirmation
 	 */
 	public TestCase(Logger $log, boolean $enableConfirmation) {
 		this.$log = $log;
 		this.$confirm = $enableConfirmation;
 	}
 	
 	/**
 	 * By default, this will always attempt to call {@link System#exit(int)} at the
 	 * end of running tests, exiting with 0 if all tests pass, 4 if any units have
 	 * failed, and 5 if any unit failed catastrophically (i.e. the entire case was not
 	 * completed). This behavior and/or those values can be overridden by overriding
 	 * the {@link #succeeded()}, {@link #failed()}, and {@link #aborted()} methods,
 	 * respectively.
 	 */
 	public synchronized void run() {
 		List<Unit> $units = getUnits();	// list is assumed immutable on pain of death or idiocy
 		
 		$numUnits = $units.size();
 		$numUnitsRun = 0;
 		$numUnitsPassed = 0;
 		$numUnitsFailed = 0;
 		
 		for (int $i = 0; $i < $units.size(); $i++) {
 			Unit $unit = $units.get($i);
 			if ($unit == null) continue;
 			
 			try {
 				resetFailures();
 				
 				$log.info(this, "TEST UNIT "+$unit.getName()+" STARTING...");
 				$numUnitsRun++;
 				$unit.call();
 				if ($unit.expectExceptionType() != null) {
 					$numUnitsFailed++;
 					$log.error(this.getClass(), "EXPECTED EXCEPTION; TEST CASE ABORTED.");
 					aborted();
 				}
 				if ($unitFailures == 0) {
 					$numUnitsPassed++;
 					$log.info(this, "TEST UNIT "+$unit.getName()+" PASSED SUCCESSFULLY!\n");
 				} else {
 					$numUnitsFailed++;
 					$log.info(this, "TEST UNIT "+$unit.getName()+" FAILED (WITH "+$unitFailures+" FAILURES)!\n");
 				}
 			} catch (AssertionFatal $e) {
 				$numUnitsFailed++;
 				$log.error(this.getClass(), "FATAL EXCEPTION; TEST CASE ABORTED.", $e);
 				aborted();
 				break;
 			} catch (AssertionFailed $e) {
 				$numUnitsFailed++;
 				$log.error(this.getClass(), "TEST UNIT "+$unit.getName()+" ABORTED.", $e);
 			} catch (Throwable $e) {
 				if ($unit.expectExceptionType() != null) {
 					// some kind of exception was expected.
 					if ($unit.expectExceptionType().isAssignableFrom($e.getClass())) {
 						// and it was this kind that was expected, so this is good.
						$numUnitsPassed++;
 						assertInstanceOf($unit.expectExceptionType(), $e);	// generates a normal confirmation message
 						$log.info(this, "TEST UNIT "+$unit.getName()+" PASSED SUCCESSFULLY!\n");
 					} else {
 						// and it wasn't this kind.  this represents fatal failure.
						$numUnitsFailed++;
 						$log.error(this.getClass(), "FATAL EXCEPTION; TEST CASE ABORTED.", $e);
 						aborted();
 						break;
 					}
 				} else {
 					// no exception was expected.  any exception represents fatal failure.
					$numUnitsFailed++;
 					$log.error(this.getClass(), "FATAL EXCEPTION; TEST CASE ABORTED.", $e);
 					aborted();
 					break;
 				}
 			}
 		}
 		
 		if ($numUnitsFailed > 0)
 			failed();
 		else
 			succeeded();
 	}
 	
 	
 	/**
 	 * <p>
 	 * Called when the entire test case finished with all units passing successfully.
 	 * Default behavior is printing {@link #preExitMessage()} to stdout followed by
 	 * forceful termination of the program via {@link System#exit(int)} with an exit
 	 * code of 0.
 	 * </p>
 	 */
 	protected void succeeded() {
 		System.out.println(preExitMessage());
 		System.exit(0);
 	}
 	
 	/**
 	 * <p>
 	 * Called when the entire test case finished, but at least one unit did not pass
 	 * successfully. Default behavior is printing {@link #preExitMessage()} to stdout
 	 * followed by forceful termination of the program via {@link System#exit(int)}
 	 * with an exit code of 4.
 	 * </p>
 	 */
 	protected void failed() {
 		System.out.println(preExitMessage());
 		System.exit(4);
 	}
 	
 	/**
 	 * <p>
 	 * Called when the entire test case is aborted (i.e. a unit throws an unexpected
 	 * exception or AssertionFatal). Default behavior is printing
 	 * {@link #preExitMessage()} to stdout followed byis forceful termination of the
 	 * program via {@link System#exit(int)} with an exit code of 5.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note that the entire test case is <b>not</b> considered aborted when a single
 	 * unit of the case fails or or aborted, and as such this method will not be
 	 * called in that situation ({@link #failed()} will be).
 	 * </p>
 	 */
 	protected void aborted() {
 		System.out.println(preExitMessage());
 		System.exit(5);
 	}
 	
 	protected String preExitMessage() {
 		return "{\"#\":\"TESTCASE\",\n"+
 		"         \"numUnits\":"+$numUnits+",\n"+
 		"      \"numUnitsRun\":"+$numUnitsRun+",\n"+
 		"   \"numUnitsPassed\":"+$numUnitsPassed+",\n"+
 		"   \"numUnitsFailed\":"+$numUnitsFailed+"\n"+
 		"}";
 	}
 	
 	protected final Logger	$log;
 	private boolean		$confirm;
 	private int		$unitFailures;
 	private int		$numUnits;
 	private int		$numUnitsRun;
 	private int		$numUnitsPassed;
 	private int		$numUnitsFailed;
 	
 	public abstract List<Unit> getUnits();
 	
 	
 
 	/**
 	 * <p>
 	 * Each Unit in a TestCase contains a coherent set of assertions (or just one
 	 * assertion) preceeded by code to set up the test. The class name of an instance
 	 * of Unit is used when logging the successful passing of a Unit and so use of
 	 * anonymous subclasses of Unit is not advised.
 	 * </p>
 	 * 
 	 * <p>
 	 * Any object returned by the {@link #call()} method is ignored by TestCase, so
 	 * it's typically appropriate to act as if Unit actually implemented
 	 * <tt>Callable&lt;{@link Void}&gt;</tt>. (The return type of Object is allowed in
 	 * case the client cares to compose their units in odd ways, but doing so is not
 	 * recommended.)
 	 * </p>
 	 */
 	public abstract class Unit implements Callable<Object> {
 		/**
 		 * If this returns null, any exception thrown from the {@link #call()}
 		 * method results in failure of the Unit and aborting of all further Units
 		 * in the entire Case. Otherwise, if this method is overriden to return a
 		 * type, an exception <i>must</i> be thrown from the call method that is
 		 * instanceof that type, or the Unit fails and all further Units in the
 		 * entire Case are aborted.
 		 */
 		public <$T extends Throwable> Class<$T> expectExceptionType() { return null; }
 		// this method often seems to cause warnings about unchecked conversion in subclasses even when the return type is obviously legitimate, but i'm unsure of why.
 		
 		public void breakIfFailed() throws AssertionFailed {
 			if ($unitFailures > 0) throw new AssertionFailed("breaking: "+$unitFailures+" failures.");
 		}
 		public void breakCaseIfFailed() throws AssertionFatal {
 			if ($unitFailures > 0) throw new AssertionFatal("breaking case: "+$unitFailures+" failures.");
 		}
 		
 		public final String getName() {
 			String[] $arrg = Primitives.PATTERN_DOT.split(getClass().getCanonicalName());
 			return $arrg[$arrg.length-1];
 		}
 	}
 	
 	
 	
 	protected void resetFailures() {
 		$unitFailures = 0;
 	}
 	
 	// using autoboxing on primitives as much as these message functions do bothers me but it does save me a helluva lot of lines of code here and i am assuming you're not using any assertions inside of terribly tight loops (or if you are, you're eiter not using confirmation or not failing hundreds of thousands of times).
 	// it might be a clarity enhancement to do quotation marks around the actual and expected values depending on type, though, which i don't do right now.
 	static String messageFail(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion failed -- expected " + $expected + " != " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" failed -- expected " + $expected + " != " + $actual + " actual.";
 	}
 	static String messagePass(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion passed -- expected " + $expected + " == " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" passed -- expected " + $expected + " == " + $actual + " actual.";
 	}	// i'm not recycling code in the above two because i think someday i might do some alignment stuff, in which case the above become more complicated cases.
 	static String messageFailNot(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion failed -- unexpected " + $expected + " == " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" failed -- unexpected " + $expected + " == " + $actual + " actual.";
 	}
 	static String messagePassNot(String $label, Object $expected, Object $actual) {
 		if ($label == null)
 			return "assertion passed -- expected " + $expected + " != " + $actual + " actual.";
 		else
 			return "assertion \"" + $label + "\" passed -- expected " + $expected + " != " + $actual + " actual.";
 	}
 	static String messageFail(String $label, String $message) {
 		if ($label == null)
 			return "assertion failed -- " + $message;
 		else
 			return "assertion \"" + $label + "\" failed -- " +$message;
 	}
 	static String messagePass(String $label, String $message) {
 		if ($label == null)
 			return "assertion passed -- " + $message;
 		else
 			return "assertion \"" + $label + "\" passed -- " + $message;
 	}
 	// note that failure messages get wrapped in exceptions and then given to the logger (with a constant message of "assertion failed")
 	//  whereas success messages get passed to the logger as actual messages (with no exception attached).
 	//   this... might be a poor inconsistency, since i could see wanting to be able to report line numbers of successes outloud as well. 
 	
 	
 	
 	
 	
 	////////////////
 	//  BOOLEAN
 	////////////////
 	public boolean assertTrue(boolean $bool) {
 		return assertEquals(null, true, $bool);
 	}
 	public boolean assertFalse(boolean $bool) {
 		return assertEquals(null, false, $bool);
 	}
 	public boolean assertEquals(boolean $expected, boolean $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertTrue(String $label, boolean $bool) {
 		return assertEquals($label, true, $bool);
 	}
 	public boolean assertFalse(String $label, boolean $bool) {
 		return assertEquals($label, false, $bool);
 	}
 	public boolean assertEquals(String $label, boolean $expected, boolean $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFail($label, $expected, $actual)));
 			return false;
 		}
 		if ($confirm) $log.debug(this.getClass(), messagePass($label, $expected, $actual));
 		return true;
 	}
 	
 	
 	////////////////
 	//  Object
 	////////////////
 	public boolean assertSame(Object $expected, Object $actual) {
 		return assertSame(null, $expected, $actual);
 	}
 	public boolean assertSame(String $label, Object $expected, Object $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFail($label, $expected, $actual)));
 			return false;
 		}
 		if ($confirm) $log.debug(this.getClass(), messagePass($label, $expected, $actual));
 		return true;
 	}
 	public boolean assertNotSame(Object $expected, Object $actual) {
 		return assertNotSame(null, $expected, $actual);
 	}
 	public boolean assertNotSame(String $label, Object $expected, Object $actual) {
 		if ($expected == $actual) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFailNot($label, $expected, $actual)));
 			return false;
 		}
 		if ($confirm) $log.debug(this.getClass(), messagePassNot($label, $expected, $actual));
 		return true;
 	}
 	public boolean assertNull(Object $actual) {
 		return assertSame(null, null, $actual);
 	}
 	public boolean assertNull(String $label, Object $actual) {
 		return assertSame($label, null, $actual);
 	}
 	public boolean assertEquals(Object $expected, Object $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, Object $expected, Object $actual) {
 		if (!assertEqualsHelper($expected, $actual)) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFail($label, $expected, $actual)));
 			return false;
 		}
 		if ($confirm) $log.debug(this.getClass(), messagePass($label, $expected, $actual));
 		return true;
 	}
 	private boolean assertEqualsHelper(Object $expected, Object $actual) {
 		if ($expected == null) return ($actual == null);
 		return $expected.equals($actual);
 	}
 	public boolean assertInstanceOf(Class<?> $klass, Object $obj) {
 		return assertInstanceOf(null, $klass, $obj);
 	}
 	public boolean assertInstanceOf(String $label, Class<?> $klass, Object $obj) {
 		if ($obj == null) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFail($label, "null is never an instance of anything, and certainly not "+$klass+".")));
 			return false;
 		}
 		try {
 			$klass.cast($obj);
 			if ($confirm) $log.debug(this.getClass(), messagePass($label, "\""+$obj.getClass().getCanonicalName()+"\" is an instance of \""+$klass.getCanonicalName()+"\""));
 			return true;
 		} catch (ClassCastException $e) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFail($label, $e.getMessage()+".")));
 			return false;
 		}
 	}
 	
 	
 	////////////////
 	//  String
 	////////////////
 	// there's not actually a dang thing special about these, i just want the api itself to reassure developers that yes, strings can be asserted on and nothing weird happens.
 	public boolean assertEquals(String $expected, String $actual) {
 		return assertEquals(null, (Object)$expected, (Object)$actual);
 	}
 	public boolean assertEquals(String $label, String $expected, String $actual) {
 		return assertEquals($label, (Object)$expected, (Object)$actual);
 	}
 	
 	
 	////////////////
 	//  INT
 	////////////////
 	public boolean assertEquals(int $expected, int $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, int $expected, int $actual) {
 		if ($expected != $actual) {
 			$unitFailures++;
 			$log.warn(this.getClass(), new AssertionFailed(messageFail($label, $expected, $actual)));
 			return false;
 		}
 		if ($confirm) $log.debug(this.getClass(), messagePass($label, $expected, $actual));
 		return true;
 	}
 	
 	
 	////////////////
 	//  BYTE
 	////////////////
 	public boolean assertEquals(byte[] $expected, byte[] $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, byte[] $expected, byte[] $actual) {
 		return assertEquals($label, Strings.toHex($expected), Strings.toHex($actual));
 	}
 	
 	////////////////
 	//  CHAR
 	////////////////
 	public boolean assertEquals(char[] $expected, char[] $actual) {
 		return assertEquals(null, $expected, $actual);
 	}
 	public boolean assertEquals(String $label, char[] $expected, char[] $actual) {
 		return assertEquals($label, Arr.toString($expected), Arr.toString($actual));
 	}
 	
 	
 	
 	
 	
 	public static class AssertionFailed extends Error {
 		public AssertionFailed() { super(); }
 		public AssertionFailed(String $arg0) { super($arg0); }
 		public AssertionFailed(Throwable $arg0) { super($arg0); }
 		public AssertionFailed(String $arg0, Throwable $arg1) { super($arg0, $arg1); }
 	}
 	public static class AssertionFatal extends AssertionFailed {
 		public AssertionFatal() { super(); }
 		public AssertionFatal(String $arg0) { super($arg0); }
 		public AssertionFatal(Throwable $arg0) { super($arg0); }
 		public AssertionFatal(String $arg0, Throwable $arg1) { super($arg0, $arg1); }
 	}
 	
 	// Note!  You can not make methods like:
 	//	assertNotEquals(byte[] $a, byte[] $b) {
 	//		return !assertEquals($a, $b);
 	// because they'll still do the failure count and the log messages backwards inside.
 	
 	//future work:
 	//   i think it should be more or less possible to provide an interface to retrofit ahs TestCase to JUnit, which would be handy for folks that like the ability integrate JUnit with eclipse plugins or the like.
 }

 package hammer;
 
 import java.util.ArrayList;
 import java.io.IOException;
 
 /**
  * A base ef test class.
  * @author rynor_000
  *
  */
 public class HammerTest {
 	/**
 	 * The earfuck code to use for this test.
 	 */
 	protected String efCode;
 	
 	/**
 	 * The HammerFramework object to use to control the parser.
 	 */
 	private HammerFramework mHammer;
 	
 	/**
 	 * A list of IO tasks to be performed in this test.
 	 * These tasks will be performed in the order they appear in the list.
 	 * i.e. the order they are added in.
 	 */
 	private ArrayList<TestTask> mTasks;
 	
 	/**
 	 * The name of the test.
 	 */
 	private String mName;
 	
 	/**
 	 * Indicates if this test failed to prepare.
 	 * If true, run() will immediately return false.
 	 */
 	protected boolean setupFailed = false;
 	
 	/**
 	 * A message indicating why setup failed.
 	 */
 	protected String failureMessage = "";
 	
 	public HammerTest(String name, String code, HammerFramework hammer) {
 		efCode = code;
 		mHammer = hammer;
 		mTasks = new ArrayList<TestTask>();
 		mName = name;
 	}
 	
 	public HammerTest(String name, String code) {
 		this(name, code, new HammerFramework());
 	}
 	
 	public String getName() {
 		return mName;
 	}
 	
 	/**
 	 * Runs the test.
 	 * @return Whether the test passed or not (true/false)
 	 */
 	public boolean run() {
 		boolean testPassed = true;
 		
 		if (setupFailed) {
 			// We failed to set up the test, so return failure.
 			HammerLog.error(
 					"== Test: " + mName + " failed to prepare ==");
 			HammerLog.error(failureMessage + "\n");
 			return false;
 		}
 		
 		HammerLog.info("== Running test: " + mName + " ==");
 		
 		// Set the piece playing.
 		mHammer.setPiece(efCode);
 		mHammer.startPlaying();
 		
 		// For each IO task we have, execute it (either give input,
 		// or check output).
 		// If it fails (i.e. the output doesn't match expected) return
 		// failure.
 		for (TestTask task : mTasks) {
 			if (!task.execute(mHammer)) {
 				HammerLog.info("Test Failed!");
 				testPassed = false;
 				break;
 			}
 		}
 
         mHammer.tearDown();
 		
 		// If we got this far, the test must have passed
        if (testPassed) {
        	HammerLog.info("Test Passed!");
        }
 		HammerLog.debug("");
		return testPassed;
 	}
 	
 	/**
 	 * Adds a task to the queue.
 	 * @param task - the task to add.
 	 */
 	public void addTask(TestTask task) {
 		mTasks.add(task);
 	}
 	
 	/**
 	 * A class representing a single task for a test to execute.
 	 * @author rynor_000
 	 *
 	 */
 	public static interface TestTask {
 		public boolean execute(HammerFramework hammer);
 	}
 	
 	public static class OutputTask implements TestTask {
 		private int expected;
 		
 		public OutputTask(int expected) {
 			this.expected = expected;
 		}
 		
 		public boolean execute(HammerFramework hammer) {
 			try {
             	int output = hammer.waitAndGetOutput();
                 return hammer.hammerAssert(
                         "Expected: " + String.valueOf(expected) +
                         "  Got: " + String.valueOf(output), 
                         output == expected);
             }
             catch (IOException e) {
             	HammerLog.error(e.getMessage());
                 return false;
             }
 		}
 	}
 	
 	public static class InputTask implements TestTask {
 		private int value;
 		
 		public InputTask(int value) {
 			this.value = value;
 		}
 		
 		public boolean execute(HammerFramework hammer) {
 			try {
 				hammer.waitAndSendInput(value);
 			} catch (IOException e) {
 				HammerLog.error(e.getMessage());
 				return false;
 			}
 			return true;
 		}
 	}
 	
 	public static class RestartTask implements TestTask {
 		
 		public RestartTask() {
 		}
 		
 		public boolean execute(HammerFramework hammer) {
			hammer.tearDown();
 			hammer.resetParser();
 			hammer.startPlaying();
 			return true;
 		}
 	}
 }

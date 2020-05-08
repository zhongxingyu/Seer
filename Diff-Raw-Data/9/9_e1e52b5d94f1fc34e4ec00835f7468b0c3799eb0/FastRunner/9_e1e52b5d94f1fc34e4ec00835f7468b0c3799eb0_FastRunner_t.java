 package jumble.fast;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import jumble.mutation.Mutater;
 import jumble.mutation.MutatingClassLoader;
 import jumble.ui.InitialTestStatus;
 import jumble.ui.JumbleListener;
 import jumble.ui.NullListener;
 import jumble.util.IOThread;
 import jumble.util.JavaRunner;
 import junit.framework.TestResult;
 
 import com.reeltwo.util.Debug;
 
 /**
  * A runner for the <CODE>FastJumbler</CODE>. Runs the FastJumbler in a new
  * JVM and detects timeouts.
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class FastRunner {
 
   /** Filename for the cache */
   public static final File CACHE_FILE = new File(System.getProperty("user.home"), ".jumble-cache.dat");
 
   // Configuration properties
 
   /** Whether to mutate constants */
   private boolean mInlineConstants = true;
 
   /** Whether to mutate return values */
   private boolean mReturnVals = true;
 
   /** Whether to mutate increments */
   private boolean mIncrements = true;
 
   private boolean mOrdered = true;
 
   private boolean mVerbose = false;
 
   private boolean mLoadCache = true;
 
   private boolean mSaveCache = true;
 
   private boolean mUseCache = true;
 
   /** Maximum number of mutations per JVM */
   private int mMaxExternalMutations = -1;
 
   private Set mExcludeMethods = new HashSet();
 
   // State during run
 
   /** The class being tested */
   private String mClassName;
 
   private File mCacheFile;
 
   private File mTestSuiteFile;
 
   private JavaRunner mRunner = new JavaRunner("jumble.fast.FastJumbler");
 
   private Process mChildProcess = null;
 
   private IOThread mIot = null;
 
   private IOThread mEot = null;
 
   private int mMutationCount;
 
   private long mTotalRuntime;
 
   /** The variable storing the failed tests - can get pretty big */
   FailedTestMap mCache = null;
 
   /**
    * Gets whether verbose mode is set.
    * 
    * @return true if verbose mode is enabled.
    */
   public boolean isVerbose() {
     return mVerbose;
   }
 
   /**
    * Sets whether verbose mode is enabled.
    * 
    * @param newVerbose
    *          true if verbose mode should be enabled.
    */
   public void setVerbose(final boolean newVerbose) {
     mVerbose = newVerbose;
   }
 
   /**
    * Gets whether inline constants will be mutated.
    * 
    * @return true if inline constants will be mutated.
    */
   public boolean isInlineConstants() {
     return mInlineConstants;
   }
 
   /**
    * Sets whether inline constants will be mutated.
    * 
    * @param argInlineConstants
    *          true if inline constants should be mutated.
    */
   public void setInlineConstants(final boolean argInlineConstants) {
     mInlineConstants = argInlineConstants;
   }
 
   /**
    * Gets whether return values will be mutated.
    * 
    * @return true if return values will be mutated.
    */
   public boolean isReturnVals() {
     return mReturnVals;
   }
 
   /**
    * Sets whether return values will be mutated.
    * 
    * @param argReturnVals
    *          true return values should be mutated.
    */
   public void setReturnVals(final boolean argReturnVals) {
     mReturnVals = argReturnVals;
   }
 
   /**
    * Gets whether increments will be mutated.
    * 
    * @return true if increments will be mutated.
    */
   public boolean isIncrements() {
     return mIncrements;
   }
 
   /**
    * Sets whether increments will be mutated.
    * 
    * @param argIncrements
    *          true if increments will be mutated.
    */
   public void setIncrements(final boolean argIncrements) {
     mIncrements = argIncrements;
   }
 
   /**
    * Gets whether tests are ordered by their run time.
    * 
    * @return true if tests are ordered by their run time.
    */
   public boolean isOrdered() {
     return mOrdered;
   }
 
   /**
    * Sets whether tests are ordered by their run time.
    * 
    * @param argOrdered
    *          true if tests should be ordered by their run time.
    */
   public void setOrdered(final boolean argOrdered) {
     mOrdered = argOrdered;
   }
 
   /**
    * Gets the value of loadCache
    * 
    * @return the value of loadCache
    */
   public boolean isLoadCache() {
     return mLoadCache;
   }
 
   /**
    * Sets the value of loadCache
    * 
    * @param argLoadCache
    *          Value to assign to loadCache
    */
   public void setLoadCache(final boolean argLoadCache) {
     mLoadCache = argLoadCache;
   }
 
   /**
    * Gets the value of saveCache
    * 
    * @return the value of saveCache
    */
   public boolean isSaveCache() {
     return mSaveCache;
   }
 
   /**
    * Sets the value of saveCache
    * 
    * @param argSaveCache
    *          Value to assign to saveCache
    */
   public void setSaveCache(final boolean argSaveCache) {
     mSaveCache = argSaveCache;
   }
 
   /**
    * Gets the value of useCache
    * 
    * @return the value of useCache
    */
   public boolean isUseCache() {
     return mUseCache;
   }
 
   /**
    * Sets the value of useCache
    * 
    * @param argUseCache
    *          Value to assign to useCache
    */
   public void setUseCache(final boolean argUseCache) {
     mUseCache = argUseCache;
   }
 
   /**
    * Gets the set of excluded method names
    * 
    * @return the set of excluded method names
    */
   public Set getExcludeMethods() {
     return mExcludeMethods;
   }
 
   /**
    * A function which computes the timeout for given that the original test took
    * <CODE>runtime</CODE>
    * 
    * @param runtime
    *          the original runtime
    * @return the computed timeout
    */
   public static long computeTimeout(long runtime) {
     return runtime * 10 + 2000;
   }
 
   private void initCache() throws Exception {
     boolean loaded = false;
 
     // Load the cache if it exists and is needed
     if (mLoadCache) {
       try {
         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CACHE_FILE));
         mCache = (FailedTestMap) ois.readObject();
         loaded = true;
       } catch (IOException e) {
         loaded = false;
       }
     }
     if (!loaded) {
       mCache = new FailedTestMap();
     }
   }
 
   private void updateCache(MutationResult mutation) {
     if (mutation.isPassed()) {
       StringTokenizer tokens = new StringTokenizer(mutation.getTestDescription(), ":");
       String clazzName = tokens.nextToken();
       assert clazzName.equals(mutation.getClassName());
       String methodName = tokens.nextToken();
       int mutPoint = Integer.parseInt(tokens.nextToken());
       String testName = tokens.nextToken();
       mCache.addFailure(clazzName, methodName, mutPoint, testName);
     }
   }
 
   private boolean writeCache(File f) {
     try {
       if (f.exists()) {
         f.delete();
       }
       ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(f));
       o.writeObject(mCache);
       o.close();
       return true;
     } catch (IOException e) {
       e.printStackTrace();
       return false;
     }
   }
 
   public void addExcludeMethod(String methodName) {
     mExcludeMethods.add(methodName);
   }
 
   /** Constructs arguments to the FastJumbler */
   private String[] createArgs(int currentMutation) {
     ArrayList args = new ArrayList();
     // mutation point
     args.add("-s");
     args.add(String.valueOf(currentMutation));
     // class name
     args.add(mClassName);
     // test suite filename
     args.add(mTestSuiteFile.toString());
 
     if (mUseCache) {
       // Write a temp cache
       if (writeCache(mCacheFile)) {
         args.add(mCacheFile.toString());
       }
     }
 
     // exclude methods
     if (!mExcludeMethods.isEmpty()) {
       StringBuffer ex = new StringBuffer();
       Iterator it = mExcludeMethods.iterator();
       for (int i = 0; i < mExcludeMethods.size(); i++) {
         if (i == 0) {
           ex.append(it.next());
         } else {
           ex.append("," + it.next());
         }
       }
       args.add("-x");
       args.add(ex.toString());
     }
     // inline constants
     if (mInlineConstants) {
       args.add("-k");
     }
     // return values
     if (mReturnVals) {
       args.add("-r");
     }
     // increments
     if (mIncrements) {
       args.add("-i");
     }
     // verbose
     if (mVerbose) {
       args.add("-v");
     }

     if (getMaxExternalMutations() >= 0) {
       args.add("-l");
       args.add("" + getMaxExternalMutations());
     }
     return (String[]) args.toArray(new String[args.size()]);
   }
 
   private Mutater createMutater(int mutationpoint) {
     // Get the number of mutation points from the Jumbler
     final Mutater m = new Mutater(mutationpoint);
     m.setIgnoredMethods(mExcludeMethods);
     m.setMutateIncrements(mIncrements);
     m.setMutateInlineConstants(mInlineConstants);
     m.setMutateReturnValues(mReturnVals);
     return m;
   }
 
   private int countMutationPoints() {
     return createMutater(0).countMutationPoints(mClassName);
   }
 
   private boolean debugOutput(String out, String err) {
     if (err != null) {
       System.err.println("Child.err->" + err);
     }
     if (out != null) {
       System.err.println("Child.out->" + out);
     }
     return true; // So we can be enabled/disabled via assertion mechanism.
   }
 
   private void waitForStart(IOThread iot, IOThread eot) throws InterruptedException {
     // read the "START" to let us know the JVM has started
     // we don't want to time this.
     // FIXME this looks dangerous. What if the test can't even get to the point
     // of outputting START (e.g. class loading issues)
     while (true) {
       String out = iot.getNext();
       String err = eot.getAvailable();
       if (mVerbose) {
         debugOutput(out, err);
       }
       if ((out == null) && (err == null)) {
         Thread.sleep(10);
       } else if ("START".equals(out)) {
         break;
       } else {
         throw new RuntimeException("jumble.fast.FastJumbler returned " + ((out != null) ? out : err + " on stderr") + " instead of START");
       }
     }
   }
 
   private void startChildProcess(String[] args) throws IOException, InterruptedException {
     mRunner.setArguments(args);
     mChildProcess = mRunner.start();
     mIot = new IOThread(mChildProcess.getInputStream());
     mIot.setDaemon(true);
     mIot.start();
     mEot = new IOThread(mChildProcess.getErrorStream());
     mEot.setDaemon(true);
     mEot.start();
     waitForStart(mIot, mEot);
   }
 
   /** Reads a mutation result from the child process */
   private MutationResult readMutation(int currentMutation, long timeout) throws InterruptedException {
     long before = System.currentTimeMillis();
     long after = before;
     String modification = null;
     // Run until we have a result or time out
     while (true) {
       String out = mIot.getNext();
       if (mVerbose) {
         debugOutput(out, mEot.getAvailable());
       }
       if (out == null) {
         if (after - before > timeout) {
           mChildProcess.destroy();
           mChildProcess = null;
           return new MutationResult(MutationResult.TIMEOUT, mClassName, currentMutation, modification);
         } else {
           Thread.sleep(50);
           after = System.currentTimeMillis();
         }
       } else {
         if (out.startsWith(FastJumbler.INIT_PREFIX)) {
           modification = out.substring(FastJumbler.INIT_PREFIX.length());
         } else {
           int status = -1;
           MutationResult m = null;
           if (out.startsWith(FastJumbler.PASS_PREFIX)) {
             status = MutationResult.PASS;
             m = new MutationResult(status, mClassName, currentMutation, modification, out.substring(FastJumbler.PASS_PREFIX.length()));
           } else if (out.startsWith(FastJumbler.FAIL_PREFIX)) {
             status = MutationResult.FAIL;
             m = new MutationResult(status, mClassName, currentMutation, modification);
           }
           if (mUseCache) {
             updateCache(m);
           }
           return m;
         }
       }
     }
   }
 
   /**
    * Runs tests without mutating at all. If all OK, write out testsuitefile for
    * later use, otherwise return a JumbleResult
    */
   private JumbleResult runInitialTests(List testClassNames) {
     mMutationCount = countMutationPoints();
     if (mMutationCount == -1) {
       return new InterfaceResult(mClassName);
     }
 
     Class[] testClasses = new Class[testClassNames.size()];
     for (int i = 0; i < testClasses.length; i++) {
       try {
         testClasses[i] = Class.forName((String) testClassNames.get(i));
       } catch (ClassNotFoundException e) {
         // test class did not exist
         return new MissingTestsTestResult(mClassName, testClassNames, mMutationCount);
       }
     }
 
     try {
       // RED ALERT - heinous reflective execution of the initial tests in
       // another classloader
       assert Debug.println("Parent. Starting initial run without mutating");
       MutatingClassLoader jumbler = new MutatingClassLoader(mClassName, createMutater(-1));
       Class suiteClazz = jumbler.loadClass("jumble.fast.TimingTestSuite");
       Object suiteObj = suiteClazz.getDeclaredConstructor(new Class[] {List.class }).newInstance(new Object[] {testClassNames });
       Class trClazz = jumbler.loadClass(TestResult.class.getName());
       Class jtrClazz = jumbler.loadClass(JUnitTestResult.class.getName());
       Object trObj = jtrClazz.newInstance();
       suiteClazz.getMethod("run", new Class[] {trClazz }).invoke(suiteObj, new Object[] {trObj });
       boolean successful = ((Boolean) trClazz.getMethod("wasSuccessful", new Class[] {}).invoke(trObj, new Object[] {})).booleanValue();
       assert Debug.println("Parent. Finished");
 
       // Now, if the tests failed, can return straight away
       if (!successful) {
         if (mVerbose) {
           System.err.println(trObj);
         }
         return new BrokenTestsTestResult(mClassName, testClassNames, mMutationCount);
       }
 
       // Set the test runtime so we can calculate timeouts when running the
       // mutated tests
       mTotalRuntime = ((Long) suiteClazz.getMethod("getTotalRuntime", new Class[] {}).invoke(suiteObj, new Object[] {})).longValue();
 
       // Store the test suite information serialized in a temporary file so
       // FastJumbler can load it.
       Object order = suiteClazz.getMethod("getOrder", new Class[] {Boolean.TYPE }).invoke(suiteObj, new Object[] {Boolean.valueOf(mOrdered) });
       ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mTestSuiteFile));
       oos.writeObject(order);
       oos.close();
 
     } catch (RuntimeException e) {
       throw e;
     } catch (Exception e) {
       RuntimeException r = new IllegalStateException("Problem using reflection to set up run under another classloader");
       r.initCause(e);
       throw r;
     }
 
     return null;
   }
 
   /**
    * Performs a Jumble run on the specified class with the specified tests.
    * 
    * @param className
    *          the name of the class to Jumble
    * @param testClassNames
    *          the names of the associated test classes
    * @return the results of the Jumble run
    * @throws Exception
    *           if something goes wrong
    * @see JumbleResult
    * 
    */
   public JumbleResult runJumble(final String className, final List testClassNames) throws Exception {
     return runJumbleProxy(className, testClassNames, null);
   }
 
   /**
    * Performs a Jumble run on the specified class with the specified tests.
    * 
    * @param className
    *          the name of the class to Jumble
    * @param testClassNames
    *          the names of the associated test classes
    * @param listener
    *          the listener associated with this Jumble run.
    * @throws Exception
    *           if something goes wrong
    * @see JumbleResult
    * @see JumbleListener
    */
   public void runJumble(final String className, final List testClassNames, JumbleListener listener) throws Exception {
     runJumbleProxy(className, testClassNames, listener);
   }
 
   /**
    * Performs a Jumble run on the specified class with the specified tests.
    * 
    * @param className
    *          the name of the class to Jumble
    * @param testClassNames
    *          the names of the associated test classes
    * @param listener
    *          the listener associated with this Jumble run.
    * @return the results of the Jumble run
    * @throws Exception
    *           if something goes wrong
    * @see JumbleResult
    * @see JumbleListener
    */
   private JumbleResult runJumbleProxy(final String className, final List testClassNames, JumbleListener listener) throws Exception {
     if (listener == null) {
       listener = new NullListener();
     }
     mClassName = className;
     mCacheFile = File.createTempFile("cache", ".dat");
     mTestSuiteFile = File.createTempFile("testSuite", ".dat");
 
     if (mUseCache) {
       initCache();
     }
 
     listener.jumbleRunStarted(mClassName, testClassNames);
 
     JumbleResult initialResult = runInitialTests(testClassNames);
     if (initialResult != null) {
       final int status;
 
       if (initialResult.isInterface()) {
         status = InitialTestStatus.INTERFACE;
       } else if (initialResult.isMissingTestClass()) {
         status = InitialTestStatus.NO_TEST;
       } else if (!initialResult.initialTestsPassed()) {
         status = InitialTestStatus.FAILED;
       } else {
         // should never happen
         throw new RuntimeException();
       }
 
       listener.performedInitialTest(status, mMutationCount, -1);
       // Jumbling will not happen here
       listener.jumbleRunEnded();
       return initialResult;
     }
 
     // compute the timeout
     long timeout = computeTimeout(mTotalRuntime);
 
     listener.performedInitialTest(InitialTestStatus.OK, mMutationCount, timeout);
 
     mChildProcess = null;
     mIot = null;
     mEot = null;
 
     final MutationResult[] allMutations = new MutationResult[mMutationCount];
     int count = 0;
     final int max = getMaxExternalMutations();
     for (int currentMutation = 0; currentMutation < mMutationCount; currentMutation++) {
       if (mChildProcess == null) {
         startChildProcess(createArgs(currentMutation));
         count = 0;
       }
       allMutations[currentMutation] = readMutation(currentMutation, timeout);
       
      if (max >= 0 && ++count >= max) {
         mChildProcess = null;
       }
       listener.finishedMutation(allMutations[currentMutation]);
     }
 
     JumbleResult ret = new NormalJumbleResult(className, testClassNames, allMutations, timeout);
 
     // finally, delete the test suite file
     if (mTestSuiteFile.exists() && !mTestSuiteFile.delete()) {
       System.err.println("Error: could not delete temporary file");
     }
     // Also delete the temporary cache and save the cache if needed
     if (mUseCache) {
       if (mCacheFile.exists() && !mCacheFile.delete()) {
         System.err.println("Error: could not delete temporary cache file " + mCacheFile);
       }
       if (mSaveCache) {
         writeCache(CACHE_FILE);
       }
     }
     listener.jumbleRunEnded();
     mCache = null;
     return ret;
   }
 
   /**
    * Gets the maximum number of mutations performed by the external JVM.
    * 
    * @return the maximum number of external mutations. A negative value implies
    *         no maximum.
    */
   public int getMaxExternalMutations() {
     return mMaxExternalMutations;
   }
 
   /**
    * Sets the maximum number of mutations performed by the external JVM.
    * 
    * @param maxExternalMutations
    *          the maximum number of external mutations. A negative value implies
    *          no maximum.
    */
   public void setMaxExternalMutations(int maxExternalMutations) {
     mMaxExternalMutations = maxExternalMutations;
   }
 }

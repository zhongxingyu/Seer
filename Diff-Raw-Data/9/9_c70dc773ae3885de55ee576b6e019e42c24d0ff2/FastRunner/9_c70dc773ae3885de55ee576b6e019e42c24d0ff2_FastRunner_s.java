 package jumble.fast;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import jumble.Mutater;
 import jumble.Mutation;
 import jumble.util.IOThread;
 import jumble.util.JavaRunner;
 import jumble.util.Utils;
 import junit.framework.TestResult;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * A runner for the <CODE>FastJumbler</CODE>. Runs the FastJumbler in a new
  * JVM and detects timeouts. Consists mostly of static methods.
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  * 
  */
 public class FastRunner {
   /** Filename for the cache */
   public static final String CACHE_FILENAME = "jumble-cache.dat";
 
   /** Prevent instantiation. */
   private FastRunner() {
   }
 
   /**
    * Main method.
    * 
    * @param args
    *          command line arguments. format is
    * 
    * <PRE>
    * 
    * java jumble.fast.FastRunner [OPTIONS] [CLASS] [TESTS]
    * 
    * CLASS the fully-qualified name of the class to mutate.
    * 
    * TESTS a list of test names to run on this class
    * 
    * OPTIONS -r Mutate return values. -k Mutate inline constants. -i Mutate
    * increments. -x Exclude specified methods. -h Display this help message. -o
    * Name of the class responsible for producing output. -n Do not order tests
    * by runtime. -s Do not save cache. -l Do not load cache.
    * 
    * </PRE>
    */
   public static void main(String[] args) throws Exception {
     try {
       // Process arguments
       boolean help = Utils.getFlag('h', args);
       String excludes = Utils.getOption('x', args);
       boolean constants = Utils.getFlag('k', args);
       boolean returns = Utils.getFlag('r', args);
       boolean increments = Utils.getFlag('i', args);
       boolean finishedTests = false;
       String outputClass = Utils.getOption('o', args);
       boolean noOrder = Utils.getFlag('n', args);
       boolean load = !Utils.getFlag('l', args);
       boolean save = !Utils.getFlag('s', args);
       boolean use = !Utils.getFlag('u', args);
 
       String className;
       List testList;
       Set excludeMethods = new HashSet();
 
       StringTokenizer tokens = new StringTokenizer(excludes, ",");
 
       while (tokens.hasMoreTokens()) {
         excludeMethods.add(tokens.nextToken());
       }
 
       if (help) {
         printUsage();
         return;
       }
 
       className = Utils.getNextArgument(args).replace('/', '.');
       testList = new ArrayList();
 
       // We need at least one test
       try {
           testList.add(Utils.getNextArgument(args).replace('/', '.'));
       } catch (NoSuchElementException e) {
           finishedTests = true;
           // no test class given, guess its name
           final String testName;
           if (className.startsWith("Abstract")) {
              testName = "Dummy" + className.substring(8) + "Test";
           } else {
               testName = className + "Test";
           }
           testList.add(testName);
       }
 
       while (!finishedTests) {
         try {
           testList.add(Utils.getNextArgument(args));
         } catch (NoSuchElementException e) {
           finishedTests = true;
         }
       }
       Utils.checkForRemainingOptions(args);
       JumbleResult res = runJumble(className, testList, excludeMethods,
           constants, returns, increments, noOrder, load, save, use);
       JumbleResultPrinter printer = getPrinter(outputClass);
       printer.printResult(res);
 
     } catch (Exception e) {
       printUsage();
       System.out.println();
       e.printStackTrace(System.out);
     }
   }
 
   /**
    * Returns a result printer instance as specified by <CODE>className</CODE>.
    * The printer is constructed with <CODE>System.out</CODE> as the argument.
    * If this is not allowed, then the no arguments constructor is ibvoked.
    * 
    * @param className
    *          name of result printer class.
    * @return a <CODE>JumbleResultPrinter</CODE> instance.
    */
   private static JumbleResultPrinter getPrinter(String className) {
     try {
       final Class clazz = Class.forName(className);
       try {
         final Constructor c = clazz
             .getConstructor(new Class[] {PrintStream.class });
         return (JumbleResultPrinter) c.newInstance(new Object[] {System.out });
       } catch (IllegalAccessException e) {
         ; // too bad
       } catch (InvocationTargetException e) {
         ; // too bad
       } catch (InstantiationException e) {
         ; // too bad
       } catch (NoSuchMethodException e) {
         ; // too bad
       }
       try {
         final Constructor c = clazz.getConstructor(new Class[0]);
         return (JumbleResultPrinter) c.newInstance(new Object[0]);
       } catch (IllegalAccessException e) {
         System.err.println("Invalid output class. Exception: ");
         e.printStackTrace();
         return new SeanResultPrinter(System.out);
       } catch (InvocationTargetException e) {
         System.err.println("Invalid output class. Exception: ");
         e.printStackTrace();
         return new SeanResultPrinter(System.out);
       } catch (InstantiationException e) {
         System.err.println("Invalid output class. Exception: ");
         e.printStackTrace();
         return new SeanResultPrinter(System.out);
       } catch (NoSuchMethodException e) {
         System.err.println("Invalid output class. Exception: ");
         e.printStackTrace();
         return new SeanResultPrinter(System.out);
       }
     } catch (ClassNotFoundException e) {
       return new SeanResultPrinter(System.out);
     }
   }
 
   /**
    * Prints command line usage help for this class.
    */
   private static void printUsage() {
     System.out.println("Usage:");
     System.out.println("java jumble.fast.FastRunner [OPTIONS] [CLASS] [TESTS]");
     System.out.println();
 
     System.out.println("CLASS the fully-qualified name of the class to mutate.");
     System.out.println();
     System.out.println("TESTS a test suite file containing the tests.");
     System.out.println();
     System.out.println("OPTIONS");
     System.out.println("         -r Mutate return values.");
     System.out.println("         -k Mutate inline constants.");
     System.out.println("         -i Mutate increments.");
     System.out.println("         -x Exclude specified methods. ");
     System.out.println("         -n Do not order tests according to runtime");
     System.out.println("         -h Display this help message.");
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
 
   /**
    * Performs a Jumble run on the specified class with the specified tests.
    * 
    * @param className
    *          the name of the class to Jumble
    * @param testClassNames
    *          the names of the associated test classes
    * @param excludeMethods
    *          a <CODE>Set</CODE> of strings containing the names of the
    *          methods which are excluded from the jumble run.
    * @param inlineConstants
    *          flag indicating whether to mutate inline constants
    * @param returnVals
    *          flag indicating whether to mutate return values.
    * @param increments
    *          flag indicating whether to mutate increment instructions.
    * @return the results of the Jumble run
    * @throws Exception
    *           if something goes wrong
    * @see JumbleResult
    */
   public static JumbleResult runJumble(final String className,
       final List testClassNames, final Set excludeMethods,
       final boolean inlineConstants, final boolean returnVals,
       final boolean increments, boolean noOrder, boolean loadCache,
       boolean saveCache, boolean useCache) throws Exception {
 
     Class[] testClasses = new Class[testClassNames.size()];
     final TestResult initialResult;
     final TimingTestSuite timingSuite;
     final TestOrder order;
     // Unique name for this test suite
     final String fileName = "testSuite" + System.currentTimeMillis() + ".dat";
     // Unique name for the cache
     final String cacheFileName = "cache" + System.currentTimeMillis() + ".dat";
 
     /** The variable storing the failed tests - can get pretty big */
     FailedTestMap cache = null;
 
     if (useCache) {
       boolean loaded = false;
 
       // Load the cache if it exists and is needed
       if (loadCache) {
         try {
           ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
               CACHE_FILENAME));
           cache = (FailedTestMap) ois.readObject();
           loaded = true;
         } catch (IOException e) {
           loaded = false;
         }
       }
       if (!loaded) {
         cache = new FailedTestMap();
       }
     }
 
     for (int i = 0; i < testClasses.length; i++) {
       testClasses[i] = Class.forName((String) testClassNames.get(i));
     }
     initialResult = new TestResult();
     timingSuite = new TimingTestSuite(testClasses);
     timingSuite.run(initialResult);
     order = timingSuite.getOrder();
 
     if (noOrder) {
       order.dropOrder();
     }
 
     // Now, if the tests failed, can return straight away
     if (!initialResult.wasSuccessful()) {
       return new FailedTestResult(className, testClassNames, initialResult);
     }
 
     // Store the timing stuff in a temporary file
     ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
         fileName));
     oos.writeObject(order);
     oos.close();
 
     // compute the timeout
     long timeLeft = order.getTotalRuntime();
 
     // Get the number of mutation points from the Jumbler
     final Mutater m = new Mutater(0);
     m.setIgnoredMethods(excludeMethods);
     m.setMutateIncrements(increments);
     m.setMutateInlineConstants(inlineConstants);
     m.setMutateReturnValues(returnVals);
 
     final int mutationCount = m.countMutationPoints(className);
 
     final JavaRunner runner = new JavaRunner("jumble.fast.FastJumbler");
     Process childProcess = null;
     IOThread iot = null;
     
     final Mutation[] allMutations = new Mutation[mutationCount];
     for (int currentMutation = 0; currentMutation < mutationCount; currentMutation++) {
       // If no process is running, start a new one
       if (childProcess == null) {
         ArrayList args = new ArrayList();
         // class name
         args.add(className);
         // mutation point
         args.add(String.valueOf(currentMutation));
         // test suite filename
         args.add(fileName);
         if (useCache) {
           try {
             File f = new File(cacheFileName);
             if (f.exists()) {
               f.delete();
             }
             ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(
                 cacheFileName));
             o.writeObject(cache);
             o.close();
             args.add(cacheFileName);
           } catch (IOException e) {
             e.printStackTrace();
           }
         }
         // exclude methods
         if (!excludeMethods.isEmpty()) {
           StringBuffer ex = new StringBuffer();
           ex.append("-x ");
           Iterator it = excludeMethods.iterator();
           for (int i = 0; i < excludeMethods.size(); i++) {
             if (i == 0) {
               ex.append(it.next());
             } else {
               ex.append("," + it.next());
             }
           }
           args.add(ex.toString());
         }
         // inline constants
         if (inlineConstants) {
           args.add("-k");
         }
         // return values
         if (returnVals) {
           args.add("-r");
         }
         // increments
         if (increments) {
           args.add("-i");
         }
         // start process
         runner.setArguments((String[]) args.toArray(new String[args.size()]));
         childProcess = runner.start();
         iot = new IOThread(childProcess.getInputStream());
         iot.start();
         // read the "START" to let us know the JVM has started
         // we don't want to time this.
         while (true) {
           String str = iot.getNext();
           // System.out.println(str);
           if (str == null) {
             Thread.sleep(10);
           } else if (str.equals("START")) {
             break;
           } else {
             throw new RuntimeException("jumble.fast.FastJumbler returned "
                 + str + " instead of START");
           }
         }
       }
       long before = System.currentTimeMillis();
       long after = before;
       long timeout = computeTimeout(timeLeft);
       // Run until we time out
       while (true) {
         String out = iot.getNext();
         if (out == null) {
           if (after - before > timeout) {
             allMutations[currentMutation] = new Mutation("TIMEOUT", className,
                 currentMutation);
             childProcess.destroy();
             childProcess = null;
             break;
           } else {
             Thread.sleep(50);
             after = System.currentTimeMillis();
           }
         } else {
           try {
             // We have output so go to the next loop iteration
             allMutations[currentMutation] = new Mutation(out, className,
                 currentMutation);
             if (useCache && allMutations[currentMutation].isPassed()) {
               // Remove "PASS: " and tokenize
               StringTokenizer tokens = new StringTokenizer(out.substring(6),
                   ":");
               String clazzName = tokens.nextToken();
               assert clazzName.equals(className);
               String methodName = tokens.nextToken();
               int mutPoint = Integer.parseInt(tokens.nextToken());
               String testName = tokens.nextToken();
               cache.addFailure(className, methodName, mutPoint, testName);
             }
           } catch (RuntimeException e) {
             throw e;
           }
           break;
         }
       }
     }
 
     JumbleResult ret = new NormalJumbleResult(className, testClassNames,
         initialResult, allMutations, order);
     // finally, delete the test suite file
     if (!new File(fileName).delete()) {
       System.err.println("Error: could not delete temporary file");
     }
     // Also delete the temporary cache and save the cache if needed
     if (useCache) {
       if (!new File(cacheFileName).delete()) {
         System.err.println("Error: could not delete temporary cache file");
       }
       if (saveCache) {
         try {
           // System.out.println("saving...");
           ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
               CACHE_FILENAME));
           os.writeObject(cache);
           os.close();
         } catch (RuntimeException e) {
           e.printStackTrace();
         }
       }
     }
     return ret;
   }
 }
 
 class FailedTestResult extends JumbleResult {
   private String mClassName;
 
   private List mTestClassNames;
 
   private TestResult mInitialResult;
 
   public FailedTestResult(String className, List testClassNames,
       TestResult result) {
     mClassName = className;
     mTestClassNames = testClassNames;
     mInitialResult = result;
   }
 
   public String getClassName() {
     return mClassName;
   }
 
   public Mutation[] getAllMutations() {
     return null;
   }
 
   public String[] getTestClasses() {
     return (String[]) mTestClassNames
         .toArray(new String[mTestClassNames.size()]);
   }
 
   public long getTimeoutLength() {
     return 0;
   }
 
   public TestResult getInitialTestResult() {
     return mInitialResult;
   }
 
   public Mutation[] getCovered() {
     return null;
   }
 
   public Mutation[] getMissed() {
     return null;
   }
 
   public Mutation[] getTimeouts() {
     return null;
   }
 }
 
 class NormalJumbleResult extends JumbleResult {
   private String mClassName;
 
   private List mTestClassNames;
 
   private TestResult mInitialResult;
 
   private Mutation[] mAllMutations;
 
   private TestOrder mOrder;
 
   public NormalJumbleResult(String className, List testClassNames,
       TestResult initialResult, Mutation[] allMutations, TestOrder order) {
     mClassName = className;
     mTestClassNames = testClassNames;
     mInitialResult = initialResult;
     mAllMutations = allMutations;
     mOrder = order;
   }
 
   public String getClassName() {
     return mClassName;
   }
 
   public TestResult getInitialTestResult() {
     return mInitialResult;
   }
 
   public Mutation[] getAllMutations() {
     return mAllMutations;
   }
 
   public Mutation[] getCovered() {
     return filter(Mutation.PASS);
   }
 
   public Mutation[] getTimeouts() {
     return filter(Mutation.TIMEOUT);
   }
 
   public Mutation[] getMissed() {
     return filter(Mutation.FAIL);
   }
 
   public long getTimeoutLength() {
     return FastRunner.computeTimeout(mOrder.getTotalRuntime());
   }
 
   public String[] getTestClasses() {
     return (String[]) mTestClassNames
         .toArray(new String[mTestClassNames.size()]);
   }
 
   private Mutation[] filter(int mutationType) {
     Mutation[] all = getAllMutations();
     ArrayList ret = new ArrayList();
 
     for (int i = 0; i < all.length; i++) {
       if (all[i].getStatus() == mutationType) {
         ret.add(all[i]);
       }
     }
 
     return (Mutation[]) ret.toArray(new Mutation[ret.size()]);
   }
 
 }

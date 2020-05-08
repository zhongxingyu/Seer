 package jumble;
 
 import java.io.PrintStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 import jumble.fast.FastRunner;
 import jumble.fast.JumbleResult;
 import jumble.fast.JumbleResultPrinter;
 import jumble.fast.SeanResultPrinter;
 import jumble.util.Utils;
 
 /**
  * A CLI interface to the <CODE>FastRunner</CODE>.
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class Jumble {
 
   /** Prevent instantiation */
   private Jumble() {
   }
 
   /**
    * Main method.
    * 
    * @param args
    *          command line arguments. format is
    * 
    * <PRE>
    * 
    * java jumble.fast.Jumble [OPTIONS] [CLASS] [TESTS]
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
       FastRunner jumble = new FastRunner();
 
       boolean help = Utils.getFlag('h', args);
       String excludes = Utils.getOption('x', args);
       boolean finishedTests = false;
       String outputClass = Utils.getOption('o', args);
 
       jumble.setInlineConstants(Utils.getFlag('k', args));
       jumble.setReturnVals(Utils.getFlag('r', args));
       jumble.setIncrements(Utils.getFlag('i', args));
       jumble.setNoOrder(Utils.getFlag('n', args));
       jumble.setLoadCache(!Utils.getFlag('l', args));
       jumble.setSaveCache(!Utils.getFlag('s', args));
       jumble.setUseCache(!Utils.getFlag('u', args));
 
       String className;
       List testList;
 
       StringTokenizer tokens = new StringTokenizer(excludes, ",");
 
       while (tokens.hasMoreTokens()) {
         jumble.addExcludeMethod(tokens.nextToken());
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
           String testName;
           if (className.startsWith("Abstract")) {
             testName = "Dummy" + className.substring(8) + "Test";
           } else {
             final int ab = className.indexOf(".Abstract");
             if (ab != -1) {
              testName = className.substring(0, ab) + ".Dummy" + className.substring(ab + 9);
             } else {
              testName = className;
             }
           }
           final int dollar = testName.indexOf('$');
           if (dollar != -1) {
             testName = testName.substring(0, dollar);
           }
          testList.add(testName + "Test");
       }
 
       while (!finishedTests) {
         try {
           testList.add(Utils.getNextArgument(args));
         } catch (NoSuchElementException e) {
           finishedTests = true;
         }
       }
       Utils.checkForRemainingOptions(args);
       JumbleResult res = jumble.runJumble(className, testList);
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
     System.out.println("java jumble.fast.Jumble [OPTIONS] [CLASS] [TESTS]");
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
 
 }
 

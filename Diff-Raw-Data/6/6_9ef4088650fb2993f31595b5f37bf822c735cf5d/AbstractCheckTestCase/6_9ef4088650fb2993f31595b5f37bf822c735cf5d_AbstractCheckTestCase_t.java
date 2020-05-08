 package net.sf.clirr.core.internal.checks;
 
 import net.sf.clirr.core.Checker;
 import net.sf.clirr.core.ClassSelector;
 import net.sf.clirr.core.CheckerFactory;
 import net.sf.clirr.core.ClassFilter;
 import net.sf.clirr.core.internal.ClassChangeCheck;
 
 /**
  * Abstract Baseclass to test individual Checks.
  * @author lkuehne
  */
 public abstract class AbstractCheckTestCase extends AbstractCheckerTestCase
 {
     /**
     * Creates the Checker that is configured to run this test.
     *
     * @param tdl a
      */
     protected Checker createChecker()
     {
         return CheckerFactory.createChecker(createCheck());
     }
 
     /**
      * This base implementation returns a filter which selects all classes
      * in the base "testlib" package (but no sub-packages). Tests which wish
      * to select different classes from the test jars should override this
      * method.
      */
     protected ClassFilter createClassFilter()
     {
         // only check classes in the base "testlib" package of the jars
         ClassSelector classSelector = new ClassSelector(ClassSelector.MODE_IF);
         classSelector.addPackage("testlib");
         return classSelector;
     }
 
     /**
      * Creates a check and sets it up so ApiDifferences are reported to the test diff listener.
      *
      * @param tdl the test diff listener that records the recognized api changes.
      * @return the confiured check
      */
     protected abstract ClassChangeCheck createCheck();
 }

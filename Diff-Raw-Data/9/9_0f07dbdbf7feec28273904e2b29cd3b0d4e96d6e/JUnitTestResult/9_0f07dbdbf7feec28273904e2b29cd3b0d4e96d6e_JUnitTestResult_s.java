 package com.reeltwo.jumble.fast;
 
 import java.util.Enumeration;
 
 import junit.framework.TestFailure;
 import junit.framework.TestResult;
 
 
 /**
  * <code>JUnitTestResult</code> extends the standard
  * <code>TestResult</code> class with a decent <code>toString</code>
  * method.
  *
  * @author <a href="mailto:len@reeltwo.com">Len Trigg</a>
  * @version $Revision$
  */
 public class JUnitTestResult extends TestResult {
   private static final String LS = System.getProperty("line.separator");
 
   @Override
   public String toString() {
     final StringBuffer sb = new StringBuffer();
    for (final Enumeration<TestFailure> e = errors(); e.hasMoreElements(); ) {
      final TestFailure f = e.nextElement();
       sb.append("TEST FINISHED WITH ERROR: ").append(f.toString()).append(LS);
       sb.append(f.trace());
     }
    for (final Enumeration<TestFailure> e = failures(); e.hasMoreElements(); ) {
      final TestFailure f = e.nextElement();
       sb.append("TEST FINISHED WITH FAILURE: ").append(f.toString()).append(LS);
       sb.append(f.trace());
     }
     return sb.toString();
   }
 }

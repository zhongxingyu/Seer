 package models.data;
 
 import java.util.Calendar;
 import java.lang.Runnable;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Assert;
import org.junit.Ignore;
 
 import models.data.DataDaemon;
 
 /**
  * Tests the DataDaemon class.
  * @author Felix Van der Jeugt
  */
 public class DataDaemonTest {
 
     // How many tasks should run.
     public static final int TASKS = 20;
 
     // The daemon, shortcut variable.
     private DataDaemon daemon = DataDaemon.getInstance();
 
     /** Before anf after any test, the queue should be empty. */
     @Before @After public void testEmpty() {
         Assert.assertTrue(daemon.empty());
     }
 
     /** Tests if all added tasks are run. */
     @Test public void testPersistence() {
         final Counter counter = new Counter(TASKS);
         final Bool bool = new Bool();
         Runnable task = new Runnable() {
             public void run() { if(counter.down()) bool.set(true); }
         };
         try {
             for(int i = 0; i < TASKS; i++) {
                 daemon.runAt(task, Calendar.getInstance());
             }
             Thread.sleep(1000);
         } catch(InterruptedException e) {
             Assert.fail(e.getMessage());
         }
         Assert.assertTrue("Not all tasks run within a second.", bool.get());
     }
 
     private static class Counter {
         private int count;
         public Counter(int count) { this.count = count; }
         public synchronized boolean down() { return (--count == 0); }
     }
 
     private static class Bool {
         private boolean bool = false;
         public synchronized void set(boolean bool) { this.bool = bool; }
         public synchronized boolean get() { return bool; }
     }
 
 
     private static int PRECISION = Calendar.MILLISECOND;
 
     /** Test if the tasks are run at the set time. */
    @Ignore @Test public void testPrecision() {
         Appender appender = new Appender();
         String checkString = "";
         for(int i = 1; i < TASKS; i++) {
             Calendar date = Calendar.getInstance();
             date.add(PRECISION, 20 * i);
             daemon.runAt(new NumTask(appender), date);
             checkString = checkString + (date.get(PRECISION)/20) + " ";
         }
         try { Thread.sleep(1000); }
         catch(InterruptedException e) {}
         Assert.assertEquals(checkString, appender.get());
     }
 
     private static class Appender {
         private String str = "";
         public synchronized void append(int s) { str = str + (s/20) + " "; }
         public synchronized String get() { return str; }
     }
 
     private static class NumTask implements Runnable {
         private Appender a;
         public NumTask(Appender a) { this.a = a; }
         public void run() {
             a.append(Calendar.getInstance().get(PRECISION));
         }
     }
 
 }

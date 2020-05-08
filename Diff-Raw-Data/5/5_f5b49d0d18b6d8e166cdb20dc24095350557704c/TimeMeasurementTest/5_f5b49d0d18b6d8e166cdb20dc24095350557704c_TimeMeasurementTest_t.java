 package com.coremedia.contribution.timemeasurement;
 
 import etm.core.monitor.EtmPoint;
 import junit.framework.TestCase;
 import org.junit.Test;
 
 /**
  *
  */
 public class TimeMeasurementTest extends TestCase {
 
   @Test
   public void testWorkingJetmConnector() {
     System.setProperty("timemeasurement.enabled", "true");
     TimeMeasurement.reset();
 
     EtmPoint etmPoint = null;
     try {
       etmPoint = TimeMeasurement.start("testWorkingJetmConnector");
       Thread.sleep(5);
     } catch (InterruptedException e) {
       e.printStackTrace();
     } finally {
       TimeMeasurement.stop(etmPoint);
       assertTrue(TimeMeasurement.getMeasurementResults().contains("testWorkingJetmConnector"));
     }
   }
 
   @Test
   public void testStartWithNullParameter() {
     System.setProperty("timemeasurement.enabled", "true");
     TimeMeasurement.reset();
 
     EtmPoint etmPoint = null;
     try {
       etmPoint = TimeMeasurement.start(null);
       Thread.sleep(5);
     } catch (InterruptedException e) {
       e.printStackTrace();
     } finally {
       TimeMeasurement.stop(etmPoint);
       assertTrue(TimeMeasurement.getMeasurementResults().contains("default"));
     }
   }
 
   @Test
   public void testStopWithNullParameter() {
     System.setProperty("timemeasurement.enabled", "true");
     TimeMeasurement.reset();
 
 
     TimeMeasurement.stop(null);
   }
 
   @Test
   public void testIsActive() {
     System.setProperty("timemeasurement.enabled", "true");
     TimeMeasurement.reset();
 
     assertTrue(TimeMeasurement.getMBean().isActive());
   }
 
   @Test
   public void testStdOut() {
     System.setProperty("timemeasurement.enabled", "true");
     TimeMeasurement.reset();
 
     TimeMeasurement.toStdOut();
   }
 
   @Test
   public void testUseNested() {
     System.setProperty("timemeasurement.enabled", "true");
     System.setProperty("timemeasurement.useNested", "true");
 
     TimeMeasurement.reset();
 
     EtmPoint etmPoint = null;
     try {
       etmPoint = TimeMeasurement.start("testUseNested");
       Thread.sleep(5);
       nestedMethod();
     } catch (InterruptedException e) {
       e.printStackTrace();
     } finally {
       TimeMeasurement.stop(etmPoint);
       assertTrue(TimeMeasurement.getMeasurementResults().contains("testUseNested"));
       assertTrue(TimeMeasurement.getMeasurementResults().contains("nestedMethod"));
       TimeMeasurement.toLog();
     }
   }
 
   private void nestedMethod() {
     EtmPoint etmPoint = null;
     try {
       etmPoint = TimeMeasurement.start("nestedMethod");
       Thread.sleep(5);
     } catch (InterruptedException e) {
       e.printStackTrace();
     } finally {
       TimeMeasurement.stop(etmPoint);
     }
   }
 
   @Test
   public void testUseMillis() {
     System.setProperty("timemeasurement.enabled", "true");
     System.setProperty("timemeasurement.useMillis", "true");
     TimeMeasurement.reset();
 
     EtmPoint etmPoint = null;
     try {
       etmPoint = TimeMeasurement.start("testUseMillis");
       //although most systems, Thread.sleep(millis,nanos) does not work (Thread sleeps only for given milliseconds),
       //it's extremly unlikely that this thread would sleep exactly 5 milliseconds.
       //if measured in nanoseconds, it's sleeping ~5100
      Thread.sleep(5,999);
     } catch (InterruptedException e) {
       e.printStackTrace();
     } finally {
       TimeMeasurement.stop(etmPoint);
       assertTrue(TimeMeasurement.getMeasurementResults().contains("testUseMillis"));
      assertTrue(TimeMeasurement.getMeasurementResults().contains("6"));
       TimeMeasurement.toLog();
     }
   }
 
   @Test
   public void testDummyJetmConnector() {
     //unfortunately, the SystemPropery is ignored because TimeMeasurement is initialized in a static block.
     //if one test with "timemeasurement.enabled" set to "true" runs before this test, TimeMeasurement will be
     //initialized with a WorkingJetmConnector and vice versa.
     System.setProperty("timemeasurement.enabled", "false");
 
     //therefore, I have to manually set it to false, so the DummyJetmConnector is loaded
     TimeMeasurement.getMBean().setActive(false);
 
     EtmPoint etmPoint = null;
     try {
       etmPoint = TimeMeasurement.start("testDummyJetmConnector");
       Thread.sleep(5);
     } catch (InterruptedException e) {
       e.printStackTrace();
     } finally {
       TimeMeasurement.stop(etmPoint);
       assertTrue(!TimeMeasurement.getMBean().isActive());
       assertTrue(TimeMeasurement.getMeasurementResults().contains("disabled"));
     }
   }
 
 }

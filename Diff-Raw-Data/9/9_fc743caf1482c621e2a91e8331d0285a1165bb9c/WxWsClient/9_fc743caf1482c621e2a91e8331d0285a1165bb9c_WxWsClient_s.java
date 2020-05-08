 /**
  * WeatherServiceTestCase.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
  */
 
 package org.tom.weather.ws.client;
 
 import java.rmi.RemoteException;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.Date;
 import org.tom.weather.ws.client.generated.*;
 import org.apache.log4j.Logger;
 import org.tom.weather.SnapShot;
 
 public class WxWsClient {
   private static final Logger LOGGER = Logger.getLogger(WxWsClient.class);
   private static int invokeCount = 0;
   private static long totalInvokeTime = 0;
   private static org.tom.weather.ws.client.generated.WeatherWeatherBindingStub binding;
   static {
     try {
       binding = (org.tom.weather.ws.client.generated.WeatherWeatherBindingStub) new org.tom.weather.ws.client.generated.WeatherServiceLocator()
           .getWeatherWeatherPort();
     } catch (javax.xml.rpc.ServiceException jre) {
       if (jre.getLinkedCause() != null)
         LOGGER.error(jre);
       throw new junit.framework.AssertionFailedError(
           "JAX-RPC ServiceException caught: " + jre);
     }
 
     // Time out after a minute
     binding.setTimeout(60000);
   }
 
   public static void resetCache(String password, String location) throws RemoteException {
     binding.resetCache(password, location);
   }
 
   private WxWsClient() {
   }
 
   private static ArchiveStruct getArchiveStruct() {
     LOGGER.debug(Calendar.getInstance().getTime());
     ArchiveStruct a = new ArchiveStruct();
     a.setRainfall(1.01);
     a.setInside_temp(68.8);
     a.setHigh_rain_rate(2.05);
     a.setInside_humidity(45);
     a.setNumber_of_wind_samples(200);
     a.setAverage_wind_speed(12);
     a.setOutside_humidity(65);
     a.setOutside_temp(33.1);
     a.setHigh_wind_speed(23);
     a.setHigh_outside_temp(33.3);
     a.setDate(Calendar.getInstance());
     a.setDirection_of_high_wind_speed(270);
     a.setPressure(30.002);
     a.setLow_outside_temp(32.8);
     a.setPrevailing_wind_direction(270);
//MPJ    a.setExtra_temp1(0);
     return a;
   }
 
   
   private static InputSampleStruct getInputSampleStruct() {
     InputSampleStruct inputSampleStruct = new InputSampleStruct();
     inputSampleStruct.setTemp(68.0);
     inputSampleStruct.setWind_direction(180);
     inputSampleStruct.setSample_date(Calendar.getInstance());
     inputSampleStruct.setPressure(30.001);
     inputSampleStruct.setBar_status("steady");
     inputSampleStruct.setRain_rate(.01);
     inputSampleStruct.setWindspeed(12);
     inputSampleStruct.setHumidity(50);
     inputSampleStruct.setExtra_temp1(0);
     return inputSampleStruct;
   }
 
   public static void main(String argv[]) {
     while (true) {
       try {
 
         long d = getLatestArchiveRecordDate("01915");
         Timestamp ts = new Timestamp(d);
         LOGGER.debug(ts);
         LOGGER.debug(d);
         
 //        datetime = Process.dmpTimeStamp(// 26, 1, 2004, 15, 0);
         
         LOGGER.debug(d);
 //        InputSampleStruct struct = getInputSampleStruct();
 //        struct.setSample_date(Calendar.getInstance());
 //        postSample("wx", struct);
 //        ArchiveStruct a = getArchiveStruct();
 //        LOGGER.debug("before posting to Rails: " + a.getDate().getTime());
 //        postArchiveEntry("wx", a);
 //        a = binding.getLastArchive(LOCATION);
 //        LOGGER.debug("after posting to Rails: " + a.getDate().getTime());
         try {
           Thread.sleep(2000);
         } catch (InterruptedException e) {
           LOGGER.error(e);
         }
       } catch (RemoteException e) {
         LOGGER.error(e);
       }
     }
   }
 
 
   public static void postSample(String password, String location,
       InputSampleStruct inputSampleStruct) throws RemoteException {
     long start = new Date().getTime();
     binding.putCurrentConditions(password, location, inputSampleStruct);
     long end = new Date().getTime();
     totalInvokeTime += (end - start);
     invokeCount++;
     LOGGER.debug("call: " + (end - start) + " ms - avg: " + totalInvokeTime
         / invokeCount);
 //    try {
 //      Thread.sleep(5 * 1000l);
 //    } catch (InterruptedException e) {
 //      LOGGER.error(e);
 //    }
   }
   
   public static long getLatestArchiveRecordDate(String location) throws RemoteException {
     ArchiveStruct s = binding.getLastArchive(location);
     if (s != null) {
         return s.getDate().getTimeInMillis();
     } else {
         return 0;
     }
   }
 
   public static void postArchiveEntry(String password, String location,
       ArchiveStruct archiveStruct) throws RemoteException {
     long start = new Date().getTime();
     binding.putArchiveEntry(password, location, archiveStruct);
     long end = new Date().getTime();
     totalInvokeTime += (end - start);
     invokeCount++;
     LOGGER.debug("call: " + (end - start) + " ms - avg: " + totalInvokeTime
         / invokeCount);
 //    try {
 //      Thread.sleep(5 * 1000l);
 //    } catch (InterruptedException e) {
 //      LOGGER.error(e);
 //    }
   }
 
 }

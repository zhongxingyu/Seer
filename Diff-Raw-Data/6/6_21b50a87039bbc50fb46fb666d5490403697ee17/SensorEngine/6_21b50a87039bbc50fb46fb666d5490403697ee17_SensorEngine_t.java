 // 
 //  SensorEngine.java
 //  AndroidWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-03-08.
 //  Copyright 2011 University of California, Berkeley. All rights reserved.
 //  
 //  Uses portions by Mr. Charles Wang <charleswang007@gmail.com>
 // 
 
 package edu.berkeley.androidwave.waveservice.sensorengine;
 
 import edu.berkeley.androidwave.waveexception.SensorNotAvailableException;
 import edu.berkeley.androidwave.waverecipe.*;
 import edu.berkeley.androidwave.waverecipe.waverecipealgorithm.*;
 import edu.berkeley.androidwave.waveservice.sensorengine.sensors.*;
 
 import android.content.Context;
 import android.hardware.SensorEvent;    // <- should change on necessary changes to WaveSensorListener
 import android.util.Log;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * SensorEngine
  * 
  * SensorEngine is a singleton, because there is only one set of underlying
  * hardware sensors.
  */
 public class SensorEngine implements WaveSensorListener {
     
     private static final String TAG = SensorEngine.class.getSimpleName();
     
     protected static SensorEngine theInstance;
     
     class AuthorizationStats {
         public WaveRecipeLocalDeviceSupportInfo supportInfo;
         public WaveRecipeAlgorithm algorithmInstance;
         public HashMap<WaveSensor, WaveSensorDescription>sensorToDescriptionMap;  // an inversion of supportInfo.descriptionToSensorMap
         public HashMap<WaveSensorDescription, Long>lastSampleTimes;
 
         AuthorizationStats(WaveRecipeLocalDeviceSupportInfo supportInfo) {
             this.supportInfo = supportInfo;
             sensorToDescriptionMap = new HashMap<WaveSensor, WaveSensorDescription>();
             lastSampleTimes = new HashMap<WaveSensorDescription, Long>();
         }
     }
     
     class AlgorithmOutputForwarder implements WaveRecipeAlgorithmListener {
         long lastForwardTime;
         double maxOutputRate;
         double maxOutputPrecision;
         WaveRecipeAuthorization authorization;
         WaveRecipeOutputListener destination;
         
         AlgorithmOutputForwarder(double rate, double precision, WaveRecipeAuthorization auth, WaveRecipeOutputListener dest) {
             lastForwardTime = 0;
             maxOutputRate = rate;
             maxOutputPrecision = precision;
             authorization = auth;
             destination = dest;
         }
         
         public void handleRecipeData(Object data) {
             Log.d("AlgorithmOutputForwarder", "handleRecipeData("+data+")");
             // TODO: do we really need the shadow object here?
             try {
                 WaveRecipeOutputData outputData = new WaveRecipeOutputDataShadow(data);
                 // drop this data if it exceeds the max rate
                 // TODO: Consider SystemClock.elapsedRealTime() in place of System.currentTimeMillis()
                 long now = System.currentTimeMillis();
                 double thisRate = 1000.0 / (now - lastForwardTime);
                 if (thisRate < maxOutputRate) {
                     // rate is good, truncate precision
                     outputData.quantize(maxOutputPrecision);
                     destination.receiveDataForAuthorization(outputData, authorization);
                 } else {
                     Log.d(TAG, "Dropped excessive recipe output");
                 }
             } catch (Exception e) {
                 Log.d("AlgorithmOutputForwarder", "Exception encountered", e);
             }
         }
     }
     
     
     /**
      * non-static fields
      */
     protected Context mContext;
     protected Set<WaveSensor> availableLocalSensors;
     protected Map<WaveRecipeAuthorization, AuthorizationStats> scheduledAuthorizations;
     protected List<WaveSensor> runningSensors;
     
     /**
      * Private Constructor for Singleton
      */
     private SensorEngine(Context c) {
         mContext = c;
         
         availableLocalSensors = null;
         scheduledAuthorizations = new HashMap<WaveRecipeAuthorization, AuthorizationStats>();
         runningSensors = new ArrayList<WaveSensor>();
     }
     
     public static void init(Context c) { //throws Exception {
         if (theInstance != null) {
             Log.w(TAG, "SensorEngine.init() called more than once, dropping singleton instance "+theInstance);
             //throw new Exception("SensorEngine.init can only be called once");
         }
         theInstance = new SensorEngine(c);
     }
     
     /**
      * getInstance
      * 
      * Access the Singleton SensorEngine instance (without needing to supply
      * a context object)
      */
     public static SensorEngine getInstance() throws Exception {
         if (theInstance == null) {
             throw new Exception("SensorEngine.init not yet called.");
         }
         return theInstance;
     }
     
     protected String internalIdForSensor(WaveSensor s) {
         return s.getVersion();
     }
     
     protected WaveSensor sensorForInternalId(String internalId) {
         if (internalId == null) throw new NullPointerException("internalId parameter cannot be null");
         
         Set<WaveSensor> sensors = getAvailableLocalSensors();
         for (WaveSensor s : sensors) {
             if (s.getVersion().equals(internalId)) {
                 return s;
             }
         }
         return null;
     }
     
     /**
      * getAvailableLocalSensors
      */
     protected Set<WaveSensor> getAvailableLocalSensors() {
         if (availableLocalSensors == null) {
             // cache-miss, populate availableLocalSensors
             Log.d(TAG, "getAvailableLocalSensors: building availableLocalSensors");
             
             availableLocalSensors = new HashSet<WaveSensor>();
             
             // It is a nuissance to reflectively find the WaveSensor
             // subclasses, and Package.getAnnotations() is not implemented by
             // Android yet, so that is ruled out.
             // So, for the purpose of the base sensors, we name their classes
             // explicitly here.  While this is fragile, it is straightforward
             // with no additional overhead
             List<Class> waveSensorClasses = new ArrayList<Class>();
             waveSensorClasses.add(AndroidHardwareAccelerometer.class);
             waveSensorClasses.add(AndroidHardwareMagneticField.class);
             waveSensorClasses.add(AndroidLocationSensor.class);
             // Class<WaveSensor>[] waveSensorClasses = new Class<WaveSensor>[] {
             //     AndroidHardwareAccelerometer.class,
             //     AndroidHardwareMagneticField.class,
             //     AndroidLocationSensor.class
             // };
             
             for (Class aClass : waveSensorClasses) {
                 assert aClass.isAssignableFrom(WaveSensor.class) : aClass;
                 
                 Log.d(TAG, "\t"+aClass);
                 
                 try {
                     // Java is stupid here, we have to be careful that we
                     // actually get the method implementation of the subclass,
                     // and not of the WaveSensor class
                     Method m = aClass.getMethod("instancesAvailableInContext", Context.class);
                     
                     Set<WaveSensor> theseInstances = (Set<WaveSensor>)m.invoke(aClass, mContext);
                     for (WaveSensor ws : theseInstances) {
                         Log.d(TAG, "\t\t"+ws);
                     }
                     
                     availableLocalSensors.addAll(theseInstances);
                 } catch (NoSuchMethodException nsme) {
                     Log.w(TAG, ""+aClass, nsme);
                 } catch (IllegalAccessException iae) {
                     Log.w(TAG, ""+aClass, iae);
                 } catch (InvocationTargetException ite) {
                     Log.w(TAG, ""+aClass, ite);
                 }
             }
         }
         return availableLocalSensors;
     }
     
     /**
      * availableSensorsMatchingWaveSensorDescription
      * 
      * @see getAvailableLocalSensors
      */
     public Set<WaveSensor> availableSensorsMatchingWaveSensorDescription(WaveSensorDescription sensorDescription) {
         
         HashSet<WaveSensor> matchingSensors = new HashSet<WaveSensor>();
         
         for (WaveSensor candidateSensor : this.getAvailableLocalSensors()) {
             if (candidateSensor.matchesWaveSensorDescription(sensorDescription)) {
                 matchingSensors.add(candidateSensor);
             }
         }
         
         return matchingSensors;
     }
     
     /**
      * supportInfoForRecipe
      * 
      * provides information about the sensors which this device offers to
      * support a given recipe.  Currently if a device has multiple local
      * sensors of the same time, there is no particular order or ranking to
      * that portion of the matching.
      * 
      * TODO: Add support for conforming units, as currently they must match
      *       exactly
      * TODO: Add support for non-local sensors
      */
     public WaveRecipeLocalDeviceSupportInfo supportInfoForRecipe(WaveRecipe recipe) {
         WaveRecipeLocalDeviceSupportInfo supportInfo = new WaveRecipeLocalDeviceSupportInfo(recipe);
         
         boolean allSensorsSatisfied = true;
         Set<WaveSensor> availableSensors = getAvailableLocalSensors();
         // this is an inefficient inner loop, but the number of sensors is
         // expected to be small
         for (WaveSensorDescription wsd : recipe.getSensors()) {
             boolean thisSensorSatisfied = false;
             for (WaveSensor s : availableSensors) {
                 if (s.matchesWaveSensorDescription(wsd)) {
                     thisSensorSatisfied = true;
                     // store information for this sensor in the supportInfo
                     supportInfo.getDescriptionToSensorMap().put(wsd, s);
                     break;
                 }
             }
             allSensorsSatisfied &= thisSensorSatisfied;
         }
         
         supportInfo.setSupported(allSensorsSatisfied);
         
         return supportInfo;
     }
     
     /**
      * scheduleAuthorization
      * 
      * For the time being, we will not allow updating of the schedule, as we
      * are not yet adjusting recipes on the fly.
      * 
      * TODO: return void and throw Exceptions
      */
     public boolean scheduleAuthorization(WaveRecipeAuthorization authorization, WaveRecipeOutputListener listener)
             throws SensorNotAvailableException {
         
         if (scheduledAuthorizations.containsKey(authorization)) {
             return false;
         }
         
         // first check that the authorization is supported on the current hardware
         WaveRecipeLocalDeviceSupportInfo supportInfo = supportInfoForRecipe(authorization.getRecipe());
         if (!supportInfo.isSupported()) {
             throw new SensorNotAvailableException();
         }
         
         AuthorizationStats as = new AuthorizationStats(supportInfo);
         try {
             as.algorithmInstance = authorization.getRecipe().getAlgorithmInstance();
         } catch (Exception e) {
             Log.d(TAG, "Exception while getting algorithm instance for authorization", e);
             return false;
         }
         // link up the output of the algorithm instance
         double maxRate = 0.0;
         double maxPrecision = 0.0;
         try {
             maxRate = authorization.getOutputRate();
             maxPrecision = authorization.getOutputPrecision();
         } catch (Exception e) {
             Log.w(TAG, "Exception while getting authorization output rates", e);
             return false;
         }
         as.algorithmInstance.setWaveRecipeAlgorithmListener(new AlgorithmOutputForwarder(maxRate, maxPrecision, authorization, listener));
         
         // start the sensors needed for this authorization
         for (Map.Entry<WaveSensorDescription, WaveSensor> entry : supportInfo.getDescriptionToSensorMap().entrySet()) {
             WaveSensorDescription wsd = entry.getKey();
             WaveSensor ws = entry.getValue();
             
             as.sensorToDescriptionMap.put(ws, wsd);
             SensorAttributes sa = authorization.getSensorAttributesForSensor(wsd);
             double requestedRate = sa.rate;
             // now that we have discovered the requested rate, see if the
             // matching hardware sensor is already running
             try {
                 if (runningSensors.contains(ws)) {
                     if (ws.desiredRate < requestedRate) {
                         ws.alterRate(requestedRate);
                     }
                 } else {
                     // sensor is not running, start it at the requested rate
                     // this just sets a guess at the actual hardware rate
                     // anyway.
                     ws.start(this, requestedRate);
                 }
             } catch (Exception e) {
                 Log.w(TAG, "Exception while altering rate", e);
             }
         }
         
         // all sensors for this authorization should be started at this point
         scheduledAuthorizations.put(authorization, as);
         
         return true;
     }
     
     public boolean descheduleAuthorization(WaveRecipeAuthorization authorization) {
         if (!scheduledAuthorizations.containsKey(authorization)) {
             return false;
         }
         
         // we do not stop sensors here, we stop in onSensorChanged if there is
         // no place left to forward the data
         scheduledAuthorizations.remove(authorization);
         return true;
     }
     
     
     /**
      * --------------------- WaveSensorListener Methods ---------------------
      */
     public void onWaveSensorChanged(WaveSensor waveSensor, SensorEvent event) {
         // first update sensor stats for this sensor
         // SensorStats ss = runningSensors.get(event.sensor);
         long now = System.currentTimeMillis();
         // long last = ss.lastSampleTime;
         // ss.lastSampleTime = now;
         // ss.estimatedRate = 1000.0 / (now - last);
         
         // check which authorizations are relevant, then feed the throttled
         // data to the algorithm instances.
         // TODO: feed on separate threads
         // TODO: optimize the lookups
         boolean hasResponder = false;
         for (Map.Entry<WaveRecipeAuthorization, AuthorizationStats> entry : scheduledAuthorizations.entrySet()) {
             WaveRecipeAuthorization auth = entry.getKey();
             AuthorizationStats stats = entry.getValue();
             
             // look up the authorized rate for this sensor in this authorization
             // (should be null if the recipe for this sensor was unscheduled)
             WaveSensorDescription wsd = stats.sensorToDescriptionMap.get(waveSensor);
             if (wsd != null) {
                 hasResponder = true;
                 SensorAttributes sa = auth.getSensorAttributesForSensor(wsd);
                 // decide whether to dispatch data
                 // we have a simple dropping scheme for now to avoid delivering
                 // faster than the authorized rate
                 Long lastDeliveredTime = stats.lastSampleTimes.get(wsd);
                 boolean shouldSend = true;
                 if (lastDeliveredTime != null) {
                     double rateForThis = 1000.0 / (now - lastDeliveredTime.longValue());
                     if (rateForThis > sa.rate) {
                         shouldSend = false;
                     }
                 }
                 // now dispatch the data if necessary
                 if (shouldSend) {
                     // store this timestamp
                     stats.lastSampleTimes.put(wsd, new Long(now));
                     // package the sensor data
                     Map<String, Double> values = new HashMap<String, Double>();
                     // for now simple channel handling
                     // TODO: better channel handling
                     WaveSensorChannelDescription[] wscds = wsd.getChannels();
                     for (int i=0; i<4; i++) {
                         if (wscds.length > i && event.values.length > i) {
                             // clip the precision of the delivered value
                             double truncatedValue = event.values[i];
                             long factor = (long)(truncatedValue / sa.precision);
                             truncatedValue = ((double)factor) * sa.precision;
                             values.put(wscds[i].getName(), new Double(truncatedValue));
                         }
                     }
                     // call up the algorithmInstance of the authorization
                     stats.algorithmInstance.ingestSensorData(new WaveSensorData(now, values));
                 }
             }
         }
         
         if (!hasResponder) {
             // no authorizations require this sensor, so shut it down
             Log.d(TAG, "No listeners for "+event.sensor+", stopping.");
             try {
                // TODO: it seems that a queue of messages builds up, so we
                //       end up calling stop multiple times.  Makes the log
                //       ugly, but we catch the Exception that results.
                waveSensor.stop();
             } catch (Exception e) {
                 Log.w(TAG, "Exception while stopping sensor", e);
             }
         }
     }
 }

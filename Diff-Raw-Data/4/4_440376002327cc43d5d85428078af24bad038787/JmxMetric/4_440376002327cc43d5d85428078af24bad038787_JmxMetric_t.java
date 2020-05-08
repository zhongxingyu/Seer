 package com.od.jtimeseries.server.servermetrics.jmx;
 
 import com.od.jtimeseries.context.TimeSeriesContext;
 import com.od.jtimeseries.server.servermetrics.ServerMetric;
 import com.od.jtimeseries.server.servermetrics.jmx.measurement.JmxMeasurement;
 import com.od.jtimeseries.source.ValueRecorder;
 import com.od.jtimeseries.timeseries.function.aggregate.AggregateFunction;
 import com.od.jtimeseries.timeseries.function.aggregate.AggregateFunctions;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import com.od.jtimeseries.util.numeric.DoubleNumeric;
 import com.od.jtimeseries.util.numeric.Numeric;
 import com.od.jtimeseries.util.time.TimePeriod;
 import com.od.jtimeseries.util.identifiable.IdentifiableBase;
 import com.od.jtimeseries.scheduling.Triggerable;
 
 import javax.management.MBeanServerConnection;
 import javax.management.remote.JMXServiceURL;
 import java.net.MalformedURLException;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 03-Dec-2009
  * Time: 22:14:47
  * To change this template use File | Settings | File Templates.
  *
  * Connect to a JMX management service to capture values to a timeseries (e.g. heap memory)
  * This is used by the server itself to capture its own memory usage.
  * It may also be configured to capture performance stats from third party processes.
  * See the serverMetricsContext.xml where the jmx metrics are defined.
  */
 public class JmxMetric implements ServerMetric {
 
     private static LogMethods logMethods = LogUtils.getLogMethods(JmxMetric.class);
     private static JmxExecutorService jmxExecutorService = new DefaultJmxExecutorService(10, 60000);
     private static final AtomicInteger triggerableId = new AtomicInteger();
 
     private final TimePeriod timePeriod;
     private final String serviceUrl;
     private JMXServiceURL url;
     private List<JmxMeasurement> jmxMeasurements;
     private Map<JmxMeasurement, ValueRecorder> measurementsToValueRecorder = Collections.synchronizedMap(new HashMap<JmxMeasurement, ValueRecorder>());
 
     /**
      * A JmxMetric with a single series / measurement
      */
     public JmxMetric(TimePeriod timePeriod, String serviceUrl, String parentContextPath, String id, String description, JmxValue jmxValue ) {
         this(timePeriod, parentContextPath, id, description, serviceUrl, Arrays.asList(jmxValue), AggregateFunctions.LAST()); //last of 1 value is that value
     }
 
     /**
      * A JmxMetric with a single series, which reads several jmx values and aggregates them using a defined function (e.g. Sum)
      */
     public JmxMetric(TimePeriod timePeriod, String serviceUrl, String parentContextPath, String id, String description, List<JmxValue> listOfJmxValue, AggregateFunction aggregateFunction) {
         this(timePeriod, serviceUrl, Arrays.asList(new JmxMeasurement(parentContextPath, id, description, listOfJmxValue, aggregateFunction)));
     }
 
     public JmxMetric(TimePeriod timePeriod, String serviceUrl, JmxMeasurement jmxMeasurement) {
         this(timePeriod, serviceUrl, Arrays.asList(jmxMeasurement));
     }
 
     public JmxMetric(TimePeriod timePeriod, String serviceUrl, List<JmxMeasurement> jmxMeasurements) {
         this.timePeriod = timePeriod;
         this.serviceUrl = serviceUrl;
         this.jmxMeasurements = jmxMeasurements;
     }
 
    public String toString() {
        return "JmxMetric " + serviceUrl + " timeperiod: " + timePeriod + " measurements: " + jmxMeasurements.size();
    }

     protected static JmxExecutorService getJmxExecutorService() {
         return jmxExecutorService;
     }
 
     public static void setJmxExecutorService(JmxExecutorService jmxExecutorService) {
         JmxMetric.jmxExecutorService = jmxExecutorService;
     }
 
     public void initializeMetrics(TimeSeriesContext rootContext) {
         try {
             url = new JMXServiceURL(serviceUrl);
         } catch (MalformedURLException e) {
             logMethods.logError("Failed to set up JMX Metric - bad URL " + serviceUrl, e);
         }
 
         createValueRecorders(rootContext);
 
         //adding the triggerable to root context should cause it to start getting triggered
         rootContext.addChild(new TriggerableJmxConnectTask());
     }
 
     private void createValueRecorders(TimeSeriesContext rootContext) {
         for (JmxMeasurement m : jmxMeasurements) {
             TimeSeriesContext c = rootContext.createContextForPath(m.getParentContextPath());
             ValueRecorder r = c.newValueRecorder(m.getId(), m.getDescription());
             measurementsToValueRecorder.put(m, r);
         }
     }
 
     private class TriggerableJmxConnectTask extends IdentifiableBase implements Triggerable {
 
         public TriggerableJmxConnectTask() {
             super("TriggerableJmxConnectTask" + triggerableId.getAndIncrement(), "Trigger for jmx metric at serviceUrl " + serviceUrl);
         }
 
         public TimePeriod getTimePeriod() {
             return timePeriod;
         }
 
         public void trigger(long timestamp) {
             for (JmxMeasurement m : jmxMeasurements) {
                 processMeasurement(m);
             }
         }
 
         private void processMeasurement(JmxMeasurement m) {
             try {
                 CalculateJmxMeasurementTask task = new CalculateJmxMeasurementTask(m);
                 getJmxExecutorService().executeTask(task);
                 Numeric result = task.getResult();
 
                 if ( ! result.isNaN() ) {
                     if ( m.getDivisor() != 1) {
                         result = DoubleNumeric.valueOf(result.doubleValue() / m.getDivisor());
                     }
 
                     ValueRecorder v = measurementsToValueRecorder.get(m);
                     v.newValue(result);
                 }
             } catch (Throwable t) {
                 logMethods.logError("Error processing JmxMeasurement " + m, t);
             }
         }
     }
 
     private class CalculateJmxMeasurementTask implements JmxExecutorTask {
 
         private Numeric result = Numeric.NaN;
         private JmxMeasurement m;
 
         public CalculateJmxMeasurementTask(JmxMeasurement m) {
             this.m = m;
         }
 
         public void executeTask(MBeanServerConnection jmxConnection) throws Exception {
             AggregateFunction aggregateFunction = m.getAggregateFunction();
             synchronized(aggregateFunction) {
                 retreiveAndAddValues(jmxConnection, aggregateFunction);
                 result = aggregateFunction.calculateAggregateValue();
                 aggregateFunction.clear();
             }
         }
 
         public JMXServiceURL getServiceURL() {
             return url;
         }
 
         private void retreiveAndAddValues(MBeanServerConnection jmxConnection, AggregateFunction aggregateFunction) throws Exception {
             for ( JmxValue n : m.getListOfJmxValue()) {
                 n.readValues(jmxConnection, aggregateFunction);
             }
         }
 
         public Numeric getResult() {
             return result;
         }
     }
 
 }

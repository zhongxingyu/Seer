 package mx.itesm.mexadl.metrics;
 
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import mx.itesm.mexadl.metrics.util.Util;
 
 /**
  * The MetricsChecker class is responsible of verifying expected and real
  * values, for the quality metrics associated to Java classes.
  * 
  * @author jccastrejon
  * 
  */
 public class MetricsChecker {
 
     /**
      * Class logger.
      */
     private Logger logger = Logger.getLogger(MetricsChecker.class.getName());
 
     /**
      * Check quality metrics for a Java class.
      * 
      * @param metricsSet
      * @param expectedMetricsData
      * @param realMetrics
      * @throws Exception
      */
    public void check(final String type, final String metricsSet,
            final Map<String, Map<String, Object>> expectedMetricsData,
             final Map<String, Map<String, Map<String, Integer>>> realMetrics) throws Exception {
         String tool;
         Integer realValue;
         String metricCode;
         Integer expectedValue;
         ComparissonOperator operator;
         Map<String, Integer> resultsType;
         Map<String, Object> currentMetrics;
         Map<String, Map<String, Integer>> results;
 
         currentMetrics = expectedMetricsData.get(metricsSet);
         if (currentMetrics != null) {
             logger.log(Level.INFO, "---- Beginning " + metricsSet + " check ----");
             for (String metric : currentMetrics.keySet()) {
                 operator = ComparissonOperator.valueOf(Util.getConfigurationProperty(MaintainabilityMetrics.class,
                         metric + ".operator"));
                 tool = Util.getConfigurationProperty(MaintainabilityMetrics.class, metric + ".tool");
                 metricCode = Util.getConfigurationProperty(MaintainabilityMetrics.class, metric + ".code");
                 if (metricCode != null) {
                     results = (Map<String, Map<String, Integer>>) realMetrics.get(tool);
 
                     if (results != null) {
                         resultsType = results.get(type);
                         if (resultsType != null) {
                             realValue = resultsType.get(metricCode);
                             expectedValue = Integer.parseInt(currentMetrics.get(metric).toString());
 
                            if (operator.isValid(realValue, expectedValue)) {
                                 logger.log(Level.INFO, "Valid value for " + metric + ". (expected: " + expectedValue
                                         + ", real: " + realValue + ")");
                             } else {
                                 logger.log(Level.INFO, "Invalid value for " + metric + ". (expected: " + expectedValue
                                         + ", real: " + realValue + ")");
                             }
                         } else {
                             logger.log(Level.WARNING, "No real value found for metric " + metric + ", by tool: " + tool
                                     + ", for type: " + type);
                         }
                     } else {
                         logger.log(Level.WARNING, "No real value found for metric " + metric + ", by tool: " + tool);
                     }
                 } else {
                     logger.log(Level.WARNING, "No code found for metric: " + metric);
                 }
             }
             logger.log(Level.INFO, "---- Ending " + metricsSet + " check -------");
         }
     }
 }

 package mx.itesm.mexadl.metrics;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import mx.itesm.mexadl.metrics.util.Util;
 
 import org.objectweb.asm.ClassReader;
 
 /**
  * The VerificationProcessor class is responsible of collection, analysis and
  * verification of quality metrics associated to java classes.
  * 
  * @author jccastrejon
  * 
  */
 public class VerificationProcessor {
 
     /**
      * 
      */
     private static Logger logger = Logger.getLogger(VerificationProcessor.class.getName());
 
     /**
      * Sets of metrics that should be analyzed.
      */
     private static final String[] METRICS_SETS = Util.getConfigurationProperty(MaintainabilityMetrics.class,
             "metricsSets").split(",");
 
     /**
      * Process the quality metrics of the classes found in the
      * <em>classesDir</em> parameter, according to the metrics reports found in
      * the <em>reportsDir</em> parameter.
      * 
      * @param classesDir
      * @param reportsDir
      * @throws Exception
      */
     public static void processMetrics(final File classesDir, final File reportsDir) throws Exception {
         String currentType;
         List<String> classes;
         InputStream inputStream;
         MetricsVisitor metricsVisitor;
         MetricsChecker metricsChecker;
         Map<String, Map<String, Object>> expectedMetrics;
 
         classes = VerificationProcessor.getClassesInDirectory(classesDir);
         if (classes != null) {
             metricsChecker = new MetricsChecker();
 
             for (String clazz : classes) {
                 metricsVisitor = new MetricsVisitor();
                 inputStream = new FileInputStream(clazz);
                 new ClassReader(inputStream).accept(metricsVisitor, ClassReader.SKIP_DEBUG);
                 expectedMetrics = metricsVisitor.getMetrics();
                 currentType = metricsVisitor.getType();
 
                 // Analyze only user classes associated to the
                 // MaintainabilityMetrics annotation
                 if ((!expectedMetrics.isEmpty()) && (!currentType.startsWith("mx.itesm.mexadl"))) {
                     VerificationProcessor.logger.log(Level.INFO, "** Beginning analysis for: " + currentType + " **");
                     for (String metricsSet : VerificationProcessor.METRICS_SETS) {
                        metricsChecker.check(currentType, metricsSet, expectedMetrics, VerificationProcessor
                                .collectMetrics(reportsDir));
                     }
                     VerificationProcessor.logger.log(Level.INFO, "** Ending analysis for: " + currentType + " *****");
                 }
             }
         }
     }
 
     /**
      * Get the Java classes in the given directory, including those in
      * sub-directories.
      * 
      * @param directory
      * @return
      */
     private static List<String> getClassesInDirectory(final File directory) {
         File currentFile;
         File[] directoryFiles;
         List<String> innerFiles;
         List<String> returnValue;
 
         returnValue = null;
         directoryFiles = directory.listFiles(new FileFilter() {
             @Override
             public boolean accept(final File pathname) {
                 return (pathname.isDirectory() || pathname.toString().endsWith(".class"));
             }
         });
 
         if ((directoryFiles != null) && (directoryFiles.length > 0)) {
             returnValue = new ArrayList<String>();
             for (int i = 0; i < directoryFiles.length; i++) {
                 currentFile = directoryFiles[i];
 
                 if (currentFile.isDirectory()) {
                     innerFiles = VerificationProcessor.getClassesInDirectory(currentFile);
                     returnValue.addAll(innerFiles);
                 } else {
                     returnValue.add(currentFile.getAbsolutePath());
                 }
             }
         }
 
         return returnValue;
     }
 
     /**
      * Collect quality metrics reports from registered metrics tools.
      * 
      * @param directory
      * @return
      */
     private static Map<String, Map<String, Map<String, Integer>>> collectMetrics(final File directory) {
         String[] tools;
         MetricsTool metricsTool;
         Map<String, Map<String, Integer>> metrics;
         Map<String, Map<String, Map<String, Integer>>> returnValue;
 
         try {
             tools = Util.getConfigurationProperty(MaintainabilityMetrics.class, "tools").split(",");
             returnValue = new HashMap<String, Map<String, Map<String, Integer>>>(tools.length);
             for (String tool : tools) {
                 metricsTool = (MetricsTool) Class.forName(tool.trim()).newInstance();
                 metrics = metricsTool.getMetrics(directory);
                 returnValue.put(tool.trim(), metrics);
             }
         } catch (Exception e) {
             e.printStackTrace();
             returnValue = null;
         }
 
         return returnValue;
     }
 }

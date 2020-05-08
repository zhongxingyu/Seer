 package jp.sf.fess.solr.plugin.util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.sf.fess.solr.plugin.analysis.monitor.MonitoringTask;
 import jp.sf.fess.solr.plugin.analysis.monitor.MonitoringTask.Callback;
 import jp.sf.fess.solr.plugin.analysis.monitor.MonitoringTask.Target;
 
 import org.apache.log4j.lf5.util.StreamUtils;
 import org.apache.lucene.analysis.util.AbstractAnalysisFactory;
 import org.apache.lucene.analysis.util.ResourceLoader;
 import org.apache.lucene.analysis.util.ResourceLoaderAware;
 import org.apache.lucene.util.IOUtils;
 import org.apache.solr.cloud.ZkSolrResourceLoader;
 import org.apache.solr.core.SolrResourceLoader;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MonitoringUtil {
     protected final static Logger log = LoggerFactory
             .getLogger(MonitoringUtil.class);
 
     private static final boolean VERBOSE = true; // debug
 
     private static final String MONITORING_PERIOD = "monitoringPeriod";
 
     private static final String MONITORING_FILE = "monitoringFile";
 
     private static final String BASE_CLASS = "baseClass";
 
     private static final String CLASS = "class";
 
     private MonitoringUtil() {
     }
 
     public static Map<String, String> createMonitorArgs(
             final Map<String, String> baseArgs) {
         final Map<String, String> monitorArgs = new HashMap<String, String>();
         monitorArgs.put(MONITORING_FILE, baseArgs.remove(MONITORING_FILE));
         monitorArgs.put(MONITORING_PERIOD, baseArgs.remove(MONITORING_PERIOD));
         return monitorArgs;
     }
 
     public static String initBaseArgs(final Map<String, String> baseArgs,
             final String luceneVersion) {
         final String baseClass = baseArgs.remove(BASE_CLASS);
         baseArgs.put(CLASS, baseClass);
         baseArgs.put(AbstractAnalysisFactory.LUCENE_MATCH_VERSION_PARAM,
                 luceneVersion);
         return baseClass;
     }
 
     public static <T> T createFactory(final String className,
             final Map<String, String> baseArgs, final ResourceLoader loader) {
         if (VERBOSE) {
             System.out.println("Create " + className + " with " + baseArgs);
         }
 
         try {
             @SuppressWarnings("unchecked")
             final Class<? extends T> clazz = (Class<? extends T>) Class
                     .forName(className);
             final Constructor<? extends T> constructor = clazz
                     .getConstructor(Map.class);
             final T factory = constructor
                     .newInstance(new HashMap<String, String>(baseArgs));
 
             if (loader != null && factory instanceof ResourceLoaderAware) {
                 ((ResourceLoaderAware) factory).inform(loader);
             }
             return factory;
         } catch (final Exception e) {
             throw new IllegalArgumentException(
                     "Invalid parameters to create TokenizerFactory.", e);
         }
     }
 
     public static MonitoringTask createMonitoringTask(
             final Map<String, String> monitorArgs, final ResourceLoader loader,
             final Callback callback) throws IOException {
         final Target monitoringTarget;
         final String monitoringFilePath = monitorArgs.get(MONITORING_FILE);
         final File file = new File(monitoringFilePath);
         if (file.exists()) {
             monitoringTarget = new Target() {
                 @Override
                 public long lastModified() {
                     return file.lastModified();
                 }
             };
         } else if (loader instanceof ZkSolrResourceLoader) {
             monitoringTarget = new ZkMonitoringTarget(loader,
                     monitoringFilePath);
         } else {
             final File targetFile = new File(
                     ((SolrResourceLoader) loader).getConfigDir(), // TODO
                     monitoringFilePath);
             monitoringTarget = new Target() {
                 @Override
                 public long lastModified() {
                     return targetFile.lastModified();
                 }
             };
         }
         final String monitoringPeriodStr = monitorArgs.get(MONITORING_PERIOD);
         final long monitoringPeriod = monitoringPeriodStr == null ? MonitoringTask.DEFAULT_PERIOD
                 : Long.parseLong(monitoringPeriodStr);
         if (VERBOSE) {
             System.out.println("Create MonitoringFileTask(" + monitoringPeriod
                     + "ms) to monitor " + monitoringFilePath);
         }
 
         return new MonitoringTask(monitoringTarget, monitoringPeriod, callback);
     }
 
     static boolean diff(final File file1, final File file2) {
         if (file1 == file2) {
             return false;
         } else if (file1 == null || file2 == null) {
             return true;
         }
 
         InputStream is1 = null;
         InputStream is2 = null;
         try {
             is1 = new BufferedInputStream(new FileInputStream(file1));
             is2 = new BufferedInputStream(new FileInputStream(file2));
             while (true) {
                 final int value1 = is1.read();
                 final int value2 = is2.read();
                 if (value1 != value2) {
                     // update
                     return true;
                 } else if (value1 == -1) {
                     // same file
                     return false;
                 }
             }
         } catch (final IOException e) {
             log.warn("Failed to compare " + file1.getAbsolutePath() + " and "
                     + file2.getAbsolutePath(), e);
             return false;
         } finally {
             IOUtils.closeWhileHandlingException(is1, is2);
         }
     }
 
     public static class ZkMonitoringTarget implements Target {
        private String pathname;
 
        private ResourceLoader loader;
 
         private File file;
 
         public ZkMonitoringTarget(final ResourceLoader loader,
                 final String pathname) throws IOException {
             file = File.createTempFile("zk_mon_", ".tmp");
             updateFile(file);
         }
 
         @Override
         public long lastModified() {
             File newFile = null;
             try {
                 newFile = File.createTempFile("zk_mon_", ".tmp");
                 updateFile(newFile);
                 if (diff(file, newFile)) {
                     file.delete();
                     file = newFile;
                 } else {
                     newFile.delete();
                 }
             } catch (final IOException e) {
                 log.warn("Failed to create " + newFile, e);
                 if (newFile != null) {
                     newFile.delete();
                 }
             }
             return file.lastModified();
         }
 
         private void updateFile(final File file) throws IOException {
             InputStream is = null;
             OutputStream os = null;
             try {
                 is = loader.openResource(pathname);
                 os = new BufferedOutputStream(new FileOutputStream(file));
                 StreamUtils.copy(is, os);
                 os.flush();
             } finally {
                 IOUtils.closeWhileHandlingException(is, os);
             }
         }
     }
 }

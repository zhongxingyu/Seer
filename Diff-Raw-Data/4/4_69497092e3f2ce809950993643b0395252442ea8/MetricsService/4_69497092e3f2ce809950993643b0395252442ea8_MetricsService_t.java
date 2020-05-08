 package com.spindle.elasticsearch.metrics;
 
 import com.codahale.metrics.Gauge;
 import com.codahale.metrics.MetricFilter;
 import com.codahale.metrics.MetricRegistry;
 import com.codahale.metrics.log4j.InstrumentedAppender;
 import com.codahale.metrics.graphite.*;
 import com.codahale.metrics.jvm.*;
 import org.apache.log4j.Appender;
 import org.apache.log4j.LogManager;
 import org.elasticsearch.ElasticSearchException;
 import org.elasticsearch.ElasticSearchIllegalArgumentException;
 import org.elasticsearch.common.component.AbstractLifecycleComponent;
 import org.elasticsearch.common.inject.Inject;
 import org.elasticsearch.common.settings.Settings;
 import org.elasticsearch.common.settings.SettingsFilter;
 import org.elasticsearch.common.unit.TimeValue;
 import org.elasticsearch.node.Node;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.net.InetSocketAddress;
 
 public class MetricsService extends AbstractLifecycleComponent<MetricsService> {
 
     private Graphite graphite;
     private GraphiteReporter graphiteReporter;
    private InstrumentedAppender metricsAppender;
     private MetricRegistry metricsRegistry;
     private final Node node;
 
     @Inject
     public MetricsService(Settings settings, SettingsFilter settingsFilter, Node node) {
         super(settings);
 
         this.node = node;
         settingsFilter.addFilter(new MetricsSettingsFilter());
     }
 
     private synchronized void createGraphiteReporter() {
         if (graphiteReporter == null) {
             String graphiteHostname = componentSettings.get("graphite.hostname");
             int graphitePort = componentSettings.getAsInt("graphite.port", 2003);
             TimeValue graphiteReportInterval =
                     componentSettings.getAsTime("graphite.report_interval",
                             TimeValue.timeValueMinutes(1));
             String graphitePrefix = componentSettings.get("graphite.prefix");
 
             if (graphiteHostname == null)
                 throw new ElasticSearchIllegalArgumentException("Graphite hostname not specified");
 
             graphite = new Graphite(new InetSocketAddress(graphiteHostname, graphitePort));
             graphiteReporter = GraphiteReporter.forRegistry(metricsRegistry)
                                                .prefixedWith(graphitePrefix)
                                                .filter(MetricFilter.ALL)
                                                .build(graphite);
             graphiteReporter.start(graphiteReportInterval.millis(), TimeUnit.MILLISECONDS);
             logger.info("Starting Graphite reporter: hostname [{}], port [{}], prefix [{}]",
                     graphiteHostname, graphitePort, graphitePrefix);
         }
     }
 
     private synchronized void createLoggingMetrics() {
         metricsAppender = new InstrumentedAppender(metricsRegistry);
        metricsAppender.activateOptions();
         LogManager.getRootLogger().addAppender(metricsAppender);
     }
 
     private Gauge<Long> createDocumentCountGauge(final String indexName) {
         return new Gauge<Long>() {
                     @Override
                     public Long getValue() {
                         try {
                             return node.client().admin().indices().prepareStats(indexName).clear().setDocs(true).execute().get().getPrimaries().docs().count();
                         } catch (InterruptedException e) {
                             logger.warn("Could not collect index stats", e);
                             return 0L;
                         } catch (ExecutionException e) {
                             logger.warn("Could not collect index stats", e);
                             return 0L;
                         }
                     }
                 };
     }
 
     private synchronized void createLocalMetrics() {
         for (final String indexName : componentSettings.getAsArray("stats.indices")) {
             logger.debug("Enabling index metrics for [{}]", indexName);
             metricsRegistry.register(MetricRegistry.name(this.getClass(), "document count"), createDocumentCountGauge(indexName));
         }
     }
 
     private synchronized void destroyGraphiteReporter() {
         if (graphiteReporter != null) {
             graphiteReporter.stop();
             graphiteReporter = null;
         }
     }
 
     private synchronized void destroyLoggingMetrics() {
         if (metricsAppender != null) {
             LogManager.getRootLogger().removeAppender(metricsAppender);
             metricsAppender = null;
         }
     }
 
     private synchronized void destroyLocalMetrics() {
         if (metricsRegistry != null) {
             //metricsRegistry.shutdown();
             metricsRegistry = null;
         }
     }
 
     private void createMetrics() {
         metricsRegistry = new MetricRegistry();
 
         metricsRegistry.register("jvm.fd.ratio", new FileDescriptorRatioGauge());
         metricsRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
         metricsRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
         metricsRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
 
         createGraphiteReporter();
         createLoggingMetrics();
         createLocalMetrics();
     }
 
     private void destroyMetrics() {
         destroyGraphiteReporter();
         destroyLoggingMetrics();
         destroyLocalMetrics();
 
         if (metricsRegistry != null) {
             //metricsRegistry.shutdown();
             metricsRegistry = null;
         }
     }
 
     @Override
     protected void doStart() throws ElasticSearchException {
         createMetrics();
     }
 
     @Override
     protected void doStop() throws ElasticSearchException {
     }
 
     @Override
     protected void doClose() throws ElasticSearchException {
         destroyMetrics();
     }
 
 }

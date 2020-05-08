 package org.atlasapi.remotesite.bbc.ion;
 
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutorCompletionService;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.PeriodFormat;
 
 import com.google.common.base.Supplier;
 import com.metabroadcast.common.scheduling.ScheduledTask;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class BbcIonScheduleUpdater extends ScheduledTask {
 
     private static final int THREADS = 5;
 
     private final Supplier<Iterable<String>> urlSupplier;
     private final RemoteSiteClient<IonSchedule> scheduleClient;
     private final BbcIonBroadcastHandler handler;
     private final AdapterLog log;
 
<<<<<<< HEAD
=======

>>>>>>> mbst-694-model-segments
     public BbcIonScheduleUpdater(Supplier<Iterable<String>> urlSupplier, RemoteSiteClient<IonSchedule> client, BbcIonBroadcastHandler handler, AdapterLog log) {
         this.urlSupplier = urlSupplier;
         this.scheduleClient = client;
         this.handler = handler;
         this.log = log;
     }
 
     @Override
     public void runTask() {
         DateTime start = new DateTime(DateTimeZones.UTC);
         log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update initiated"));
 
         ExecutorService executor = Executors.newFixedThreadPool(THREADS);
         CompletionService<Integer> completer = new ExecutorCompletionService<Integer>(executor);
 
         int submitted = 0;
 
         for (String url : urlSupplier.get()) {
             completer.submit(new BbcIonScheduleUpdateTask(url, scheduleClient, handler, log));
             submitted++;
         }
 
         reportStatus(String.format("Submitted %s update tasks", submitted));
 
         int processed = 0, failed = 0, broadcasts = 0;
 
         for (int i = 0; i < submitted; i++) {
             try {
                 if (!shouldContinue()) {
                     break;
                 }
                 Future<Integer> result = completer.poll(5, TimeUnit.SECONDS);
                 if (result != null) {
                     try {
                         broadcasts += result.get();
                     } catch (Exception e) {
                         failed++;
                         log.record(AdapterLogEntry.warnEntry().withCause(e).withSource(getClass()).withDescription("Schedule update failed"));
                     }
                 }
                 reportStatus(String.format("Processed %s / %s. %s failures. %s broadcasts processed", ++processed, submitted, failed, broadcasts));
             } catch (InterruptedException e) {
                 log.record(AdapterLogEntry.warnEntry().withCause(e).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update interrupted waiting for results"));
             }
         }
 
         executor.shutdown();
 
         String runTime = new Period(start, new DateTime(DateTimeZones.UTC)).toString(PeriodFormat.getDefault());
         log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withDescription("BBC Ion Schedule Date Range Update finished in " + runTime));
     }
 }

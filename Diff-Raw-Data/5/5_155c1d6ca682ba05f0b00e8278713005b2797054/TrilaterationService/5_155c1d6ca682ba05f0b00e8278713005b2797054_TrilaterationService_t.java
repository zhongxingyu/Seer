 package be.pursuit.witrack.trilaterate;
 
 import be.pursuit.witrack.mongo.Database;
 import be.pursuit.witrack.mongo.Position;
 import be.pursuit.witrack.mongo.Scan;
 import be.pursuit.witrack.mongo.Scanner;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.util.*;
 import java.util.concurrent.ForkJoinPool;
 
 /**
  * @author Jo Voordeckers - jo.voordeckers@pursuit.be
  */
 @Singleton
 public class TrilaterationService {
 
     private static final Logger LOG = LoggerFactory.getLogger(TrilaterationService.class);
 
     private static final int ONE_MINUTES = 1 * 1000 * 60;
 
     private static final int BUCKET_WINDOW = 30 * 1000; // 30s window
 
     private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
 
     private static final Comparator<Scan> SCAN_COMP_BY_DEV_AND_TIME = new Comparator<Scan>() {
         @Override
         public int compare(final Scan s1, final Scan s2) {
 
             int devComp = s1.getDeviceId().compareTo(s2.getDeviceId());
 
             if (devComp == 0) {
                 return s1.getLastSeen().compareTo(s2.getLastSeen());
             }
 
             return devComp;
 
         }
     };
 
     @Inject
     private Database db;
 
     @Inject
     private TrilaterationCalulator calulator;
 
     private ForkJoinPool pool;
 
     @PostConstruct
     public void init() {
         pool = new ForkJoinPool(CPU_CORES);
     }
 
     public void trilaterate() {
 
         try {
 
             Date oneMinutesAgo = new Date(System.currentTimeMillis() - ONE_MINUTES);
 
             Iterable<Scan> scanIterable = db.scans().find().as(Scan.class);
 
             List<Scan> scans = Lists.newArrayList(scanIterable);
 
             processScans(scans);
 
             db.scans().remove("{ lastSeen: { $lt: #} }", oneMinutesAgo);
 
         } catch (Exception e) {
             LOG.error("Failed to trilaterate", e);
         }
     }
 
     private void processScans(final List<Scan> scans) {
 
         LOG.trace("Processing {} scans for trilateration", scans.size());
 
         Collections.sort(scans, SCAN_COMP_BY_DEV_AND_TIME);
 
         List<Bucket> buckets = bucketize(scans);
 
         for (Bucket bucket : buckets) {
 
             Position position = calulator.calculate(bucket);
             db.positions().insert(position);
         }
 
     }
 
     private List<Bucket> bucketize(final List<Scan> scans) {
 
         Map<String, Scanner> scanners = fetchScanners();
 
         List<Bucket> bucketList = Lists.newArrayList();
 
         Bucket bucket = new Bucket();
 
         for (Scan scan : scans) {
 
             if (bucket.getDeviceId() == null) {
                bucket.setTime(scan.getLastSeen());
                 bucket.setDeviceId(scan.getDeviceId());
 
             } else if (!bucket.getDeviceId().equals(scan.getDeviceId())
                     || !inTimeRange(scan.getLastSeen(), bucket.getTime())) {
 
                 bucketList.add(bucket);
                 bucket = new Bucket();
                 bucket.setDeviceId(scan.getDeviceId());
                 bucket.setTime(scan.getLastSeen());
             }
 
             Bucket.Measurement measurement = new Bucket.Measurement();
             measurement.setScanner(scanners.get(scan.getScannerId()));
             measurement.setPower(scan.getPower());
             bucket.getMeasurements().add(measurement);
         }
 
         return bucketList;
     }
 
     private boolean inTimeRange(Date scan, Date bucketTime) {
         return bucketTime.getTime() + BUCKET_WINDOW > scan.getTime();
     }
 
     private Map<String, Scanner> fetchScanners() {
         Iterable<Scanner> scannerIterable = db.scanners().find().as(Scanner.class);
         return Maps.uniqueIndex(scannerIterable, new Function<Scanner, String>() {
             @Override
             public String apply(final be.pursuit.witrack.mongo.Scanner scanner) {
                 return scanner.getScannerId();
             }
         });
     }
 
     @PreDestroy
     public void destroy() {
         pool.shutdown();
     }
 
 }

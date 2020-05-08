 package com.convert.rice;
 
 import static java.util.Arrays.asList;
 
 import java.io.IOException;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HConstants;
 import org.apache.hadoop.hbase.client.HTablePool;
 
 import com.convert.rice.hbase.HbaseHourTimeSeries;
 import com.convert.rice.hbase.HbaseMinuteTimeSeries;
 import com.convert.rice.server.http.RiceHttpServer;
 import com.convert.rice.server.protobuf.RiceProtoBufRpcServer;
 import com.google.common.base.Supplier;
 
 public class Main {
 
     public static void main(String[] args) throws InterruptedException, IOException {
         OptionParser parser = new OptionParser() {
 
             {
                 accepts("zkquorum").withRequiredArg().describedAs("ZooKeeper Quorum").defaultsTo("localhost");
                 accepts("httpPort").withRequiredArg().ofType(Integer.class).defaultsTo(8080)
                         .describedAs("HTTP port to start on");
                 accepts("protoPort").withRequiredArg().ofType(Integer.class).defaultsTo(10000)
                         .describedAs("Protobuf binary port to start on");
                accepts("aggreg").withRequiredArg().describedAs("Aggregation Interval").defaultsTo("minute");
                 accepts("downSample").withRequiredArg().describedAs("The down sample interval can be h/m/d")
                         .defaultsTo("h");
                 acceptsAll(asList("h", "?"), "show help");
             }
         };
 
         OptionSet options = parser.parse(args);
         Configuration conf = new Configuration();
         conf.set(HConstants.ZOOKEEPER_QUORUM, (String) options.valueOf("zkquorum"));
         HTablePool pool = new HTablePool(conf, Integer.MAX_VALUE);
         Supplier<TimeSeries> supplier = null;
         if (options.has("h")) {
             parser.printHelpOn(System.out);
         }
         if (options.valueOf("downSample").equals("m")) {
             supplier = new MinuteTimeSeriesSupplier(pool);
         } else if (options.valueOf("downSample").equals("d")) {
             throw new NotImplementedException();
         } else if (options.valueOf("downSample").equals("h")) {
             supplier = new HourTimeSeriesSupplier(pool);
         }
        if (options.hasArgument("httpPort")) {
             new RiceHttpServer((Integer) options.valueOf("httpPort"),
                     supplier).start();
         }
        if (options.hasArgument("protoPort")) {
             new RiceProtoBufRpcServer((Integer) options.valueOf("protoPort"),
                     supplier).start();
         }
     }
 
     private static class HourTimeSeriesSupplier implements Supplier<TimeSeries> {
 
         private HTablePool pool;
 
         public HourTimeSeriesSupplier(HTablePool pool) {
             this.pool = pool;
         }
 
         @Override
         public TimeSeries get() {
             return new HbaseHourTimeSeries(pool);
         }
     }
 
     private static class MinuteTimeSeriesSupplier implements Supplier<TimeSeries> {
 
         private HTablePool pool;
 
         public MinuteTimeSeriesSupplier(HTablePool pool) {
             this.pool = pool;
         }
 
         @Override
         public TimeSeries get() {
             return new HbaseMinuteTimeSeries(pool);
         }
     }
 }

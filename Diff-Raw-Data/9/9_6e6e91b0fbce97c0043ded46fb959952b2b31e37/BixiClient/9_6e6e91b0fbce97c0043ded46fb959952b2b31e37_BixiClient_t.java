 package org.apache.hadoop.hbase.client.coprocessor;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.coprocessor.BixiProtocol;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.RegexStringComparator;
 import org.apache.hadoop.hbase.filter.RowFilter;
 import org.apache.hadoop.hbase.filter.CompareFilter;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import bixi.hbase.query.BixiConstant;
 
 public class BixiClient {
   public static final Log log = LogFactory.getLog(BixiClient.class);
 
   HTable table, stat_table, cluster_table;;
   Configuration conf;
  private static final byte[] TABLE_NAME = Bytes.toBytes(BixiConstant.SCHEMA1_TABLE_NAME);
   private static final byte[] STATION_TABLE_NAME = Bytes.toBytes(BixiConstant.SCHEMA2_BIKE_TABLE_NAME);
   private static final byte[] STATION_CLUSTER_TABLE_NAME = Bytes.toBytes(BixiConstant.SCHEMA2_CLUSTER_TABLE_NAME);
 
   public BixiClient(Configuration conf) throws IOException {
     this.conf = conf;
     this.table = new HTable(conf, TABLE_NAME);
     this.stat_table = new HTable(conf, STATION_TABLE_NAME);
     this.cluster_table = new HTable(conf, STATION_CLUSTER_TABLE_NAME);
     log.debug("in constructor of BixiClient");
   }
 
   /**
 * @param stationIds
 * @param dateWithHour
 * : most simple format; format is: dd_mm_yyyy__hh
 * @return //01_10_2010__01
 * @throws Throwable
 * @throws IOException
 */
   public <R> Map<String, Integer> getAvailBikes(final List<String> stationIds,
       String dateWithHour) throws IOException, Throwable {
     final Scan scan = new Scan();
     log.debug("in getAvailBikes: " + dateWithHour);
     if (dateWithHour != null) {
       scan.setStartRow((dateWithHour + "_00").getBytes());
       scan.setStopRow((dateWithHour + "_59").getBytes());
     }
     class BixiCallBack implements Batch.Callback<Map<String, Integer>> {
       Map<String, Integer> res = new HashMap<String, Integer>();
 
       @Override
       public void update(byte[] region, byte[] row, Map<String, Integer> result) {
         log.debug("in update, result is: " + result.toString());
         System.out.println("in update as a sop" + result.toString());
         res = result;
       }
     }
     BixiCallBack callBack = new BixiCallBack();
     table.coprocessorExec(BixiProtocol.class, scan.getStartRow(), scan
         .getStopRow(), new Batch.Call<BixiProtocol, Map<String, Integer>>() {
       public Map<String, Integer> call(BixiProtocol instance)
           throws IOException {
         return instance.giveAvailableBikes(0, stationIds, scan);
       };
     }, callBack);
 
     return callBack.res;
   }
 
   public Map<String, Integer> getAvgUsageForPeriod(final List<String> stationIds,
       String startDate, String endDate) throws IOException, Throwable {
     final Scan scan = new Scan();
     if(endDate == null)
     	endDate = startDate;
     if (startDate != null) {
       scan.setStartRow((startDate + "_00").getBytes());
       scan.setStopRow((endDate + "_59").getBytes());
     }
     class BixiCallBack implements Batch.Callback<Map<String, Integer>> {
       Map<String, Integer> res = new HashMap<String, Integer>();
       int count = 0;
 
       @Override
       public void update(byte[] region, byte[] row, Map<String, Integer> result) {
         count++;
         log.debug("in update, result is: " + result.toString());
         System.out.println("in update as a sop" + result.toString());
         for (Map.Entry<String, Integer> e : result.entrySet()) {
           if (res.containsKey(e.getKey())) { // add the val
             int t = e.getValue();
             t += res.get(e.getKey());
             res.put(e.getKey(), t);
           } else {
             res.put(e.getKey(), e.getValue());
           }
         }
       }
 
       private Map<String, Integer> getResult() {
         for (Map.Entry<String, Integer> e : res.entrySet()) {
           int i = e.getValue() / count;
           res.put(e.getKey(), i);
         }
         return res;
       }
     }
 
     BixiCallBack callBack = new BixiCallBack();
     table.coprocessorExec(BixiProtocol.class, scan.getStartRow(), scan
         .getStopRow(), new Batch.Call<BixiProtocol, Map<String, Integer>>() {
       public Map<String, Integer> call(BixiProtocol instance)
           throws IOException {
         return instance.giveAverageUsage(stationIds, scan);
       };
     }, callBack);
 
     return callBack.getResult();
 
   }
 
   // get number of free bikes at a given time. for a given pair of lat/lon and a
   // radius
 
   /**
 * @param lat
 * @param lon
 * @param radius
 * @param dateWithHour
 * @return
 * @throws IOException
 * @throws Throwable
 */
   public Map<String, Double> getAvailableBikesFromAPoint(final double lat,
       final double lon, final double radius, String dateWithHour)
       throws IOException, Throwable {
     final Get get = new Get((dateWithHour + "_00").getBytes());
     log.debug("in getAvgUsageForAHr: " + dateWithHour);
     class BixiAvailCallBack implements Batch.Callback<Map<String, Double>> {
       Map<String, Double> res = new HashMap<String, Double>();
 
       @Override
       public void update(byte[] region, byte[] row, Map<String, Double> result) {
         res = result;
       }
 
       private Map<String, Double> getResult() {
         return res;
       }
     }
 
     BixiAvailCallBack callBack = new BixiAvailCallBack();
     table.coprocessorExec(BixiProtocol.class, get.getRow(), get.getRow(),
         new Batch.Call<BixiProtocol, Map<String, Double>>() {
           public Map<String, Double> call(BixiProtocol instance)
               throws IOException {
             return instance.getAvailableBikesFromAPoint(lat, lon, radius, get);
           };
         }, callBack);
 
     return callBack.getResult();
 
   }
   
   /* Schema 2 implementation */
   
   public Map<String, Integer> getAvgUsageForPeriod_Schema2(final List<String> stationIds,
 	      String startDateWithHour, String endDateWithHour) throws IOException, Throwable {
 	    final Scan scan = new Scan();
 	    log.debug("in getAvgUsageForPeriod: " + startDateWithHour);
 	    if(endDateWithHour == null){
 	    	endDateWithHour = startDateWithHour;
 	    }
 	    if (startDateWithHour != null) {
 	      scan.setStartRow((startDateWithHour).getBytes());
 	      scan.setStopRow((endDateWithHour + "_ZZ").getBytes());
 	      if(stationIds!=null && stationIds.size()>0){
 	    	  String regex = "(";
 	    	  boolean start = true;
 	    	  for(String sId : stationIds){
 	    		  if(!start)
 	    			  regex += "|";
 	    		  start = false;
 	    		  regex += sId;
 	    	  }
 	    	  regex += ")$";
 	    	  Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(regex));
 	    	  scan.setFilter(filter);
 	      }
 	    }
 	    class BixiCallBack implements Batch.Callback<Map<String, Integer>> {
 	      Map<String, Integer> res = new HashMap<String, Integer>();
 	      int count = 0;
 
 	      @Override
 	      public void update(byte[] region, byte[] row, Map<String, Integer> result) {
 	        count++;
 	        log.debug("in update, result is: " + result.toString());
 	        System.out.println("in update as a sop" + result.toString());
 	        for (Map.Entry<String, Integer> e : result.entrySet()) {
 	          if (res.containsKey(e.getKey())) { // add the val
 	            int t = e.getValue();
 	            t += res.get(e.getKey());
 	            res.put(e.getKey(), t);
 	          } else {
 	            res.put(e.getKey(), e.getValue());
 	          }
 	        }
 	      }
 
 	      private Map<String, Integer> getResult() {
 	        for (Map.Entry<String, Integer> e : res.entrySet()) {
 	          int i = e.getValue() / count;
 	          res.put(e.getKey(), i);
 	        }
 	        return res;
 	      }
 	    }
 
 	    BixiCallBack callBack = new BixiCallBack();
 	    stat_table.coprocessorExec(BixiProtocol.class, scan.getStartRow(), scan
 	        .getStopRow(), new Batch.Call<BixiProtocol, Map<String, Integer>>() {
 	      public Map<String, Integer> call(BixiProtocol instance)
 	          throws IOException {
 	        return instance.getAverageUsage_Schema2(stationIds, scan);
 	      };
 	    }, callBack);
 
 	    return callBack.getResult();
 
 	  }
 
 	  // get number of free bikes at a given time. for a given pair of lat/lon and a
 	  // radius
 
 	  /**
 	* @param lat
 	* @param lon
 	* @param radius
 	* @param dateWithHour
 	* @return
 	* @throws IOException
 	* @throws Throwable
 	*/
 	  public Map<String, Double> getAvailableBikesFromAPoint_Schema2(final double lat,
 	      final double lon, String dateWithHour)
 	      throws IOException, Throwable {
 		  
 		  List<String> stationIds = this.getStationsNearPoint(lat, lon);
 		  
 		  final Scan scan = new Scan();
 		  if (dateWithHour != null) {
 		      scan.setStartRow((dateWithHour).getBytes());
 		      scan.setStopRow((dateWithHour + "_ZZ").getBytes());
 		      if(stationIds!=null && stationIds.size()>0){
 		    	  String regex = "(";
 		    	  boolean start = true;
 		    	  for(String sId : stationIds){
 		    		  if(!start)
 		    			  regex += "|";
 		    		  start = false;
 		    		  regex += sId;
 		    	  }
 		    	  regex += ")$";
 		    	  Filter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(regex));
 		    	  scan.setFilter(filter);
 		      }
 		    }
 	    class BixiAvailCallBack implements Batch.Callback<Map<String, Double>> {
 	      Map<String, Double> res = new HashMap<String, Double>();
 
 	      @Override
 	      public void update(byte[] region, byte[] row, Map<String, Double> result) {
 	        res.putAll(result);
 	      }
 
 	      private Map<String, Double> getResult() {
 	        return res;
 	      }
 	    }
 
 	    BixiAvailCallBack callBack = new BixiAvailCallBack();
 	    stat_table.coprocessorExec(BixiProtocol.class, scan.getStartRow(), scan.getStopRow(),
 	        new Batch.Call<BixiProtocol, Map<String, Double>>() {
 	          public Map<String, Double> call(BixiProtocol instance)
 	              throws IOException {
 	            return instance.getAvailableBikesFromAPoint_Schema2(lat, lon, scan);
 	          };
 	        }, callBack);
 
 	    return callBack.getResult();
 
 	  }
 	  
 	  public List<String> getStationsNearPoint(final double lat, final double lon) throws IOException, Throwable{
 		  
 		  class BixiAvailCallBack implements Batch.Callback<List<String>> {
 		      List<String> res = new ArrayList<String>();
 
 		      @Override
 		      public void update(byte[] region, byte[] row, List<String> result) {
 		        res.addAll(result);
 		      }
 
 		      private List<String> getResult() {
 		        return res;
 		      }
 		    }
 
 		    BixiAvailCallBack callBack = new BixiAvailCallBack();
 		    cluster_table.coprocessorExec(BixiProtocol.class, null, null,
 		        new Batch.Call<BixiProtocol, List<String>>() {
 		          public List<String> call(BixiProtocol instance)
 		              throws IOException {
 		            return instance.getStationsNearPoint_Schema2(lat, lon);
 		          };
 		        }, callBack);
 
 		    return callBack.getResult();
 	  }
   
 }
 

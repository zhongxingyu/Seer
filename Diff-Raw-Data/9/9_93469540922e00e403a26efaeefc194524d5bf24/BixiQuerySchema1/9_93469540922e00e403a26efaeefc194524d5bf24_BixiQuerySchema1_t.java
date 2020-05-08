 package bixi.hbase.query;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.client.coprocessor.BixiClient;
 import org.apache.hadoop.hbase.util.Bytes;
 
 
 public class BixiQuerySchema1 extends BixiQueryAbstraction{
 	
	String table_name = BixiConstant.SCHEMA1_TABLE_NAME;
 	String family_name = BixiConstant.SCHEMA1_FAMILY_NAME;
 	
 /******************************************************
  * ********************************Coprocessor*********
  ******************************************************/
 	@Override
 	public void queryAvgUsageByTimeSlot4Stations(String start,
 			String end, String stations) {
 
 		List<String> stationIds = new ArrayList<String>();
 
 		if (!("All").equals(stations)) {
 			String[] idStr = stations.split(BixiConstant.ID_DELIMITER);
 			for (String id : idStr) {
 				stationIds.add(id);
 			}
 		}
 		try{
 		    BixiClient client = new BixiClient(conf);
 		    Map<String, Integer> avgusage = client
 		        .getAvgUsageForPeriod(stationIds, start, end);
 		    System.out.println("Average Usage: " + avgusage);	    	
 	    }catch(Exception e){
 	    	e.printStackTrace();
 	    }catch(Throwable e){
 	    	e.printStackTrace();
 	    } 		
 			
 	}
 
 	@Override
 	public void queryAvailableByTimeStamp4Point(String timestamp,
 			double latitude, double longitude,double radius) {
 	    System.out.println("callAvailBikesFromAPoint");
 	    try{
 		    BixiClient client = new BixiClient(conf);
 		    Map<String, Double> availBikesFromAPoint = client
 		        .getAvailableBikesFromAPoint(latitude, longitude, radius, timestamp);
 		    System.out.println("availBikes is: " + availBikesFromAPoint);	    	
 	    }catch(Exception e){
 	    	e.printStackTrace();
 	    }catch(Throwable e){
 	    	e.printStackTrace();
 	    }
 	}
 
 	/******************************************************
 	 * ********************************Scan and Get*********
 	 ******************************************************/
 	
 	@Override
 	public void queryAvgUsageByTimeSlot4StationsWithScan(String sDateWithHour,
 			String eDateWithHour, String stations){
 		List<String> stationIds = new ArrayList<String>();
 
 		if (!("All").equals(stations)) {
 			String[] idStr = stations.split(BixiConstant.ID_DELIMITER);
 			for (String id : idStr) {
 				stationIds.add(id);
 			}
 		}
 		Scan scan = new Scan();
 		scan.setCaching(this.cacheSize);
 		if (sDateWithHour != null && eDateWithHour != null) {
 			scan.setStartRow((sDateWithHour + "_00").getBytes());
 			scan.setStopRow((eDateWithHour + "_59").getBytes());
 		}
 
 		for (String qualifier : stationIds) {
 			scan.addColumn(BixiConstant.FAMILY, qualifier.getBytes());
 		}
 		
 		Map<String, Integer> result = new HashMap<String, Integer>();
 
 		long starttime = System.currentTimeMillis();
 		
 		int counter = 0;
 		ResultScanner scanner = null;
 		try {
 			HTable table = new HTable(conf, table_name.getBytes());
 			scanner = table.getScanner(scan);
 
 			for (Result r : scanner) {
 				// System.out.println("Row number:"+counter);
 				for (KeyValue kv : r.raw()) {
 					int emptyDocks = getEmptyDocks(kv);
 					String id = Bytes.toString(kv.getQualifier());
 					Integer prevVal = result.get(id);
 					emptyDocks = emptyDocks
 							+ (prevVal != null ? prevVal.intValue() : 0);
 
 					result.put(id, emptyDocks);
 					counter++;
 				}
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}finally {
 			if (scanner != null)
 				scanner.close();
 		}
 
 		for (Map.Entry<String, Integer> e : result.entrySet()) {
 			// System.out.println("counter and value is" + counter
 			// +","+e.getKey()+": " + e.getValue());
 			int i = e.getValue() / counter;
 			result.put(e.getKey(), i);
 		}
 		System.out.println("schema1 counter: " + counter + "; time = "
 				+ (System.currentTimeMillis() - starttime));
 		System.out.println("schema1 Avg map is: " + result);
 		
 	}	
 
 	@Override
 	public void queryAvailableByTimeStamp4PointWithScan(String timestamp,
 			double latitude, double longitude,double radius) {
 		//TODO change the time stamp
 		
 		double lat = latitude;// Double.parseDouble(latitude);
 		double lon = longitude; // Double.parseDouble(longitude);
 		double rad = radius; //Double.parseDouble(radius);
 		String dateWithHr = timestamp;
 		try{
 			long starttime = System.currentTimeMillis();
 			Get g = new Get(Bytes.toBytes(dateWithHr + "_00"));
 			System.out.println(dateWithHr + "_00");
 			HTable table = new HTable(conf, "BixiData".getBytes());
 			Result r = table.get(g);
 			Map<String, Double> result = new HashMap<String, Double>();
 			// this r contains the entire row for the hr+00 min. Now compute the
 			// distance and do the stuff.
 			String valStr = null, latStr = null, lonStr = null;
 			for (KeyValue kv : r.raw()) {
 				valStr = Bytes.toString(kv.getValue());
 				// log.debug("cell value is: "+s);
 				String[] sArr = valStr.split(BixiConstant.ID_DELIMITER); // array of
 				// key=value
 				// pairs
 				latStr = sArr[3];
 				lonStr = sArr[4];
 				latStr = latStr.substring(latStr.indexOf("=") + 1);
 				lonStr = lonStr.substring(lonStr.indexOf("=") + 1);
 				// log.debug("lon/lat values are: "+lonStr +"; "+latStr);
 				double distance = giveDistance(Double.parseDouble(latStr),
 						Double.parseDouble(lonStr), lat, lon)
 						- rad;
 
 				if (distance < 0) {// with in the distance: add it
 					result.put(sArr[0], distance);
 				}
 			}		
 
 			System.out.println("schema1 execution time: "+ (System.currentTimeMillis() - starttime) + "  schema1 availBikes is with Scan: " + result.size());
 					//+ ":" + result);					
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private int getEmptyDocks(KeyValue kv) {
 		String[] str = Bytes.toString(kv.getValue()).split(
 				BixiConstant.ID_DELIMITER);
 		if (str.length != 11)
 			return 0;
 		String availBikes = str[10];
 		// System.out.println("emptyDocks::" + availBikes);
 		try {
 			return Integer.parseInt(availBikes.substring(availBikes
 					.indexOf("=") + 1));
 		} catch (Exception e) {
 			System.err.println("Non numeric value as avail bikes!");
 		}
 		return 0;
 	}
 
 	final static double RADIUS = 6371;
 
 	private double giveDistance(double lat1, double lon1, double lat2,
 			double lon2) {
 		double dLon = Math.toRadians(lon1 - lon2);
 		double dLat = Math.toRadians(lat1 - lat2);
 		double a = Math.pow(Math.sin(dLat / 2), 2)
 				+ Math.cos(Math.toRadians(lat1))
 				* Math.cos(Math.toRadians(lat2))
 				* Math.pow(Math.sin(dLon / 2), 2);
 		double res = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double distance = RADIUS * res;
 		return distance;
 	}
 	
 
 	
 	
 
 }

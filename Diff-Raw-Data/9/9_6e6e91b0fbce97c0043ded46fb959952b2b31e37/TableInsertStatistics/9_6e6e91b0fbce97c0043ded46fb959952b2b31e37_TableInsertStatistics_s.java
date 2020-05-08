 package bixi.hbase.upload;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 
 import bixi.dataset.collection.BixiReader;
 import bixi.dataset.collection.XStation;
 import bixi.hbase.query.BixiConstant;
 
 public class TableInsertStatistics {
 
 	static Configuration conf = HBaseConfiguration.create();
 	HTable table;
 	static byte[] tableName = BixiConstant.SCHEMA2_BIKE_TABLE_NAME.getBytes();//"Station_Statistics".getBytes();
 	static byte[] idsFamily = BixiConstant.SCHEMA2_BIKE_FAMILY_NAME.getBytes();//"statistics".getBytes();
 
 	/**
 	 * @throws IOException
 	 */
 	public TableInsertStatistics() throws IOException {
 		table = new HTable(conf, tableName);
 		table.setAutoFlush(true);
 
 	}
 
 	public static void main(String[] args) throws IOException {
 		TableInsertStatistics inserter = new TableInsertStatistics();
 		//String fileDir = "/home/dan/Downloads/BixiData/BixiData";
 		String fileDir = "/home/dan/Downloads/BixiData/BixiData/";
 		//inserter.batchInsertRow(fileDir);
		inserter.batchInsertRow4schema3(fileDir);
 	}
 
 	public void insertRow(String fileDir) {
 
 		File dir = new File(fileDir);
 		if (!dir.isDirectory()) {
 			System.out.println(" dir is: " + dir.getAbsolutePath());
 			System.exit(1);
 		}
 		BixiReader reader = new BixiReader();
 		String[] fileNames = dir.list();
 
 		for (String fileName : fileNames) { // instantiate the file and dump it
 			if (fileName.indexOf("xml") < 0)
 				continue;
 			System.out.println("processing file: " + dir.getAbsoluteFile()
 					+ "/" + fileName);
 			File f = new File(dir.getAbsoluteFile() + "/" + fileName);
 			String[] timestamps = this.parseTimeStamp(f.getName());
 			String rowkey = timestamps[0] + "-";
 
 			try {
 				reader.parseXML(dir.getAbsolutePath() + "/" + fileName);
 				for (int i = 0; i < reader.stationList.size(); i++) {
 					XStation station = reader.stationList.get(i);
 					String value = station.getNbBikes() + ";"
 							+ station.getNbEmptyDocks();
 					Put put = new Put((rowkey + station.getId()).getBytes());
 					put.add(idsFamily, timestamps[1].getBytes(),
 							value.getBytes());
 					table.put(put);
 				}
 				reader.cleanStationList();
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public void batchInsertRow(String fileDir){
 		  
 		   File dir = new File(fileDir);
 		    if (!dir.isDirectory()){
 		      System.out.println(" dir is: "+dir.getAbsolutePath());
 		      System.exit(1);
 		    }		    
 		    String[] fileNames = dir.list();
 		    Arrays.sort(fileNames);
 		    // put filename into a hash, timstamp, and filename...
 		    // for each timestamp,read all the file in the value ,and parse the value of metrics 
 		    HashMap<String,List<String>> fileHash = new HashMap<String,List<String>>();
 		    
 		    for(String fileName: fileNames){		    	
 		    	if(fileName.indexOf(".xml")<0) continue;
 		    	File f = new File(dir.getAbsoluteFile() + "/" + fileName);
 		        if(f.length() < 1024*5) { // < 5k
 		            System.err.println("File is corrupt!" + f.getAbsolutePath());
 		            continue;// erroreneous file
 		         }		    	
 		    	//String fileName_sub = fileName.substring(0, fileName.lastIndexOf("_"));
 		    	//String toHours = fileName_sub.substring(0, fileName_sub.lastIndexOf("_"));
 		        String[] timestampes = this.parseTimeStamp(fileName);
 		    	if (fileHash.containsKey(timestampes[0])){
 		    		fileHash.get(timestampes[0]).add(fileName);	
 		    	}else{
 		    		List<String> file_list = new LinkedList<String>();
 		    		file_list.add(fileName);
 		    		fileHash.put(timestampes[0], file_list);
 		    	}		    	
 		    }
 		    
 		    //TreeMap<String,List<String>> sorted = new TreeMap<String,List<String>>(fileHash);
 	    
 		    Iterator<String> keys = fileHash.keySet().iterator();
 		    
 		    int counter = 0;
 		    int row_counter = 0;
 		    while(keys.hasNext()){
 		    	String prefix = keys.next();
 		    	counter++;
 		    	System.out.print(counter+" detail to hour:  "+prefix);
 		    	List<HashMap<String,String>> oneHourMetrics = this.parseOneHourForAll(fileHash.get(prefix), fileDir);
 				HashMap<String,HashMap<String,String>> station_list = getMetricsForOneStation(oneHourMetrics);
 				
 				Iterator<String> station_iterator = station_list.keySet().iterator();
 				while(station_iterator.hasNext()){
 					String station_id = station_iterator.next();
 					System.out.println(counter+"||"+(row_counter++)+" station: "+station_id+"=> ");
 					
 					HashMap<String,String> minutes_map = station_list.get(station_id);
 					Iterator<String> minutes = minutes_map.keySet().iterator();
 					try{
 						Put put = new Put((prefix +"-"+ station_id).getBytes());
 						while(minutes.hasNext()){
 							String oneMinute = minutes.next();
 							String value = minutes_map.get(oneMinute);
 							System.out.print("("+oneMinute+":"+value+");");
 							put.add(idsFamily, oneMinute.getBytes(),value.getBytes());						
 						}
 						table.put(put);						
 					}catch(Exception e){
 						e.printStackTrace();
 					}
 			
 					System.out.println();
 					
 				}
 
 		    }
 		    System.out.println("the row number "+counter);
     
 	}
 /*
  * rowkey is changed to stationid-timestamp	
  */
 	public void batchInsertRow4schema3(String fileDir){
 		  
 		   File dir = new File(fileDir);
 		    if (!dir.isDirectory()){
 		      System.out.println(" dir is: "+dir.getAbsolutePath());
 		      System.exit(1);
 		    }		    
 		    String[] fileNames = dir.list();
 		    Arrays.sort(fileNames);
 		    // put filename into a hash, timstamp, and filename...
 		    // for each timestamp,read all the file in the value ,and parse the value of metrics 
 		    HashMap<String,List<String>> fileHash = new HashMap<String,List<String>>();
 		    
 		    for(String fileName: fileNames){		    	
 		    	if(fileName.indexOf(".xml")<0) continue;
 		    	File f = new File(dir.getAbsoluteFile() + "/" + fileName);
 		        if(f.length() < 1024*5) { // < 5k
 		            System.err.println("File is corrupt!" + f.getAbsolutePath());
 		            continue;// erroreneous file
 		         }		    	
 
 		        String[] timestampes = this.parseTimeStamp(fileName);
 		    	if (fileHash.containsKey(timestampes[0])){
 		    		fileHash.get(timestampes[0]).add(fileName);	
 		    	}else{
 		    		List<String> file_list = new LinkedList<String>();
 		    		file_list.add(fileName);
 		    		fileHash.put(timestampes[0], file_list);
 		    	}		    	
 		    }
 		    
 		    //TreeMap<String,List<String>> sorted = new TreeMap<String,List<String>>(fileHash);
 	    
 		    Iterator<String> keys = fileHash.keySet().iterator();
 		    
 		    int counter = 0;
 		    int row_counter = 0;
 		    while(keys.hasNext()){
 		    	String prefix = keys.next();
 		    	counter++;
 		    	//System.out.print(counter+" detail to hour:  "+prefix);
 		    	List<HashMap<String,String>> oneHourMetrics = this.parseOneHourForAll(fileHash.get(prefix), fileDir);
 				HashMap<String,HashMap<String,String>> station_list = this.getMetricsForOneStation(oneHourMetrics);
 				
 				Iterator<String> station_iterator = station_list.keySet().iterator();
 				while(station_iterator.hasNext()){
 					String station_id = station_iterator.next();
 					//System.out.println(counter+"||"+(row_counter++)+" station: "+station_id+"=> ");
 					
 					HashMap<String,String> minutes_map = station_list.get(station_id);
 					Iterator<String> minutes = minutes_map.keySet().iterator();
 					try{
 						Put put = new Put((station_id+"-"+prefix).getBytes()); // rowkey : station_id+timestamp
 						while(minutes.hasNext()){
 							String oneMinute = minutes.next();
 							String value = minutes_map.get(oneMinute);
 						//	System.out.print("("+oneMinute+":"+value+");");
 							put.add(idsFamily, oneMinute.getBytes(),value.getBytes());	// column is oneMinute					
 						}
 						table.put(put);						
 					}catch(Exception e){
 						e.printStackTrace();
 					}
 			
 					System.out.println();
 					
 				}
 
 				
 		    }
 		    System.out.println("the row number "+counter);
  
 	}	
 	
 	
 	/**
 	 * @snapShots: each element indicates one file, each file stores into hashmap with the key value as <stationid-minutes,values>
 	 * @return each element indicates one station, each stations store into hashmap with the key value as <stationid,<minutes,values>>
 	 */
 	private HashMap<String,HashMap<String,String>> getMetricsForOneStation(List<HashMap<String,String>> oneHourMetrics){
 		HashMap<String,HashMap<String,String>> stations = null;
 		try{
 			if(oneHourMetrics != null){
 				stations = new HashMap<String,HashMap<String,String>>();
 				
 				for(int i=0;i<oneHourMetrics.size();i++){					
 					HashMap<String,String> oneMinutes = oneHourMetrics.get(i);
 					Iterator<String> ids = oneMinutes.keySet().iterator();										
 					
 					while(ids.hasNext()){
 						String station_minute = ids.next();
 						String value = oneMinutes.get(station_minute);
 						
 						StringTokenizer tokens = new StringTokenizer(station_minute,"-");
 						String station_id = tokens.nextToken();
 						String minute = tokens.nextToken();
 						
 						if(stations.containsKey(station_id)){
 							stations.get(station_id).put(minute, value);
 						}else{
 							HashMap<String,String> time_series = new HashMap<String,String>();
 							time_series.put(minute, value);
 							stations.put(station_id, time_series);							
 						}
 
 					}					
 												
 				}	
 
 			}
 			
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		return stations;
 		
 		
 	}
 	
 	
 	
 	/*	    
 	 * List of map<stationId-minute, metrics>
 	 * There are 407 members because there are 407 stations 
 	 * 
 	 */
 	
 	private List<HashMap<String,String>> parseOneHourForAll(List<String> file_list,String directory){
 		List<HashMap<String,String>> oneHourFiles = new LinkedList<HashMap<String,String>>();
 		try{			
 		    for(String fileName: file_list){		    	
 		    	if(fileName.indexOf(".xml")<0) continue;
 		    	File f = new File(directory + "/" + fileName);		    	
 		        if(f.length() < 1024*5) { // < 5k
 		            System.err.println("File is corrupt!" + f.getAbsolutePath());
 		            continue;// erroreneous file
 		         }			
 		        // parse one file and get all key value for all stations in one timestamp
 		        HashMap<String,String> oneFile = parseOneFile(directory+"/"+fileName);
 		        oneHourFiles.add(oneFile);
 		    }
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return oneHourFiles;
 	}
 
 	/*
 	 * filename with full path
 	 * hashmap: (stationId-Minutes,metrics value) e.g. ("1-56","4,27")
 	 * There are 407 members, because 1 minute has a pair of metrics value, and each file has 407 stations
 	 */
 
 	private HashMap<String, String> parseOneFile(String filename) {
 		
 		File f = new File(filename);
 		if (f.length() < 1024 * 5) { // < 5k
 			System.err.println("File is corrupt!" + f.getAbsolutePath());
 			return null;
 		}		
 		HashMap<String,String> station_statistics = new HashMap<String,String>();
 		String[] timestamps = this.parseTimeStamp(f.getName());		
 
 		try {
 			BixiReader reader = new BixiReader();
 			reader.parseXML(filename);
 			for (int i = 0; i < reader.stationList.size(); i++) {
 				XStation station = reader.stationList.get(i);
 				String value = station.getNbBikes() + ";"
 						+ station.getNbEmptyDocks();
 				String key = station.getId()+"-"+timestamps[1];
 				station_statistics.put(key, value);
 			}
 			reader.cleanStationList();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return station_statistics;
 
 	}
 	
 	
 	
 	private String[] parseTimeStamp(String filename) {
 		String[] sub_keys = new String[2];
 		if (filename != null) {
 			filename = filename.substring(0, filename.lastIndexOf('_'));
 			filename = filename.replace("__", ":");			
 
 			StringTokenizer tokens = new StringTokenizer(filename, ":");
 			String date_str = tokens.nextToken();
 			String hours_str = tokens.nextToken();
 
 			StringTokenizer dates = new StringTokenizer(date_str, "_");
 			StringTokenizer hours = new StringTokenizer(hours_str, "_");
 
 			String day = dates.nextToken();
 			String month = dates.nextToken();
 			String year = dates.nextToken();
 
 			String hour = hours.nextToken();
 			String minute = hours.nextToken();
 
 			sub_keys[0] = (year + month + day + hour);
 			sub_keys[1] = minute;
 		}
 
 		return sub_keys;
 	}
 }
 
 
 /*
  * 		    
 		    TreeMap<String,List<String>> sorted = new TreeMap<String,List<String>>(fileHash);
 		    Iterator<String> keys = sorted.keySet().iterator();
 		    int counter = 0;
 		    while(keys.hasNext()){
 		    	String prefix = keys.next();
 		    	System.out.print((counter++)+"  == "+prefix+" : ( ");
 				Iterator<String> stamps = sorted.get(prefix).iterator();
 				int index = 0;
 				//while(stamps.hasNext()){
 				//	System.out.print(stamps.next()+";");
 				//	index++;
 				//}		    	
 		    	System.out.println(")=="+index);
 		    }
 		    
 		    
 		    
 				for(int i=0;i<oneHourMetrics.size();i++){					
 					HashMap<String,String> oneTime = oneHourMetrics.get(i);
 					
 					Iterator<String> interval = oneStation.keySet().iterator();					
 					System.out.print((row_counter++)+" Row: "+prefix+" =>");
 					
 					while(interval.hasNext()){
 						String key = interval.next();
 						String value = oneStation.get(key);
 						System.out.print("("+key+":"+value+");");
 					}					
 							
 					System.out.println();
 				}		    
  */

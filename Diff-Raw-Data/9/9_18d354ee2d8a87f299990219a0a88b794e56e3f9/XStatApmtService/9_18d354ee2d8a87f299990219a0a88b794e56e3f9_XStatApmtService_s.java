 package savi.hcat.rest.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.client.coprocessor.Batch;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.FilterList;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import savi.hcat.analytics.coprocessor.HCATProtocol;
 import savi.hcat.analytics.coprocessor.RCopResult;
 import savi.hcat.analytics.coprocessor.RStatResult;
 import savi.hcat.common.util.XConstants;
 import savi.hcat.common.util.XTimestamp;
 
 
 import com.util.XTableSchema;
 
 
 
 public class XStatApmtService extends XBaseStatService implements XStatisticsInterface{
 		
 	private static Log LOG = LogFactory.getLog(XStatApmtService.class);
 	 
 	public XStatApmtService(){
 		this.tableSchema = new XTableSchema("schema/appointment.schema"); // it is used for proving table description to query in hbase
 		if(this.tableSchema == null){
 			LOG.error("the table schema fails to be loaded ");
 		}else{
 			LOG.info("family name: "+this.tableSchema.getFamilyName()+"; version"+this.tableSchema.getMaxVersions());
 		}
 		try {
 			this.setHBase();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 	}
 	/**
	 * get the summary of Appointment
 	 */
 	@Override
 	public JSONArray getSummary(JSONObject request){		
 		LOG.info("in getSummary");
 		long s_time = System.currentTimeMillis();
 		// get parameters of the query
 		boolean decomposed = this.decompose(request);
 		if(!decomposed)
 			LOG.error("decompose Error!");
 
 		//prepare the callback function
 		SummaryCallBack callback = new SummaryCallBack(this);
 		
 		Hashtable<String,String> timeslots = XTimestamp.splitTime(this.start_time,this.end_time,this.split_num);
 		for(Entry<String,String> pair: timeslots.entrySet()){
 			String[] rowRange = getScanRowRange(pair.getKey(),pair.getValue());// getRowRange
 			// send separate queries for each city
 			for(final String region: regions){			
 				try {				
 					// create the scan 
 					FilterList fList = getScanFilterList(rowRange,region);// getFilter list
 					final Scan scan = hbase.generateScan(null, fList,
 							new String[] { this.tableSchema.getFamilyName() },
 							null,this.tableSchema.getMaxVersions());				
 					
 					LOG.info("scan: "+scan.toString());
 					//send the caller 
 					hbase.getHTable().coprocessorExec(HCATProtocol.class,
 							scan.getStartRow(), scan.getStopRow(),
 							new Batch.Call<HCATProtocol, RStatResult>() {
 
 						public RStatResult call(HCATProtocol instance)
 								throws IOException {
 							
 							return instance.getSummary(scan,region,condition,unit,start_time,end_time);
 						};
 					}, callback);
 
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block			
 					e1.printStackTrace();
 				} catch (Throwable e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}			
 		} // end of timeslot 
 
 		/// aggregate the result
 		long cop_end = System.currentTimeMillis();		
 		long exe_time = cop_end - s_time;
 		// TODO callback.cities;
 		LOG.info("the returned value: "+callback.regions.toString());
 		for(String key: callback.regions.keySet()){
 			RStatResult result = callback.regions.get(key);
 			JSONObject regionJSON = new JSONObject();
 			TreeMap<String,Integer> treemap = new TreeMap<String,Integer>(result.getHashUnit());
 			try {
 				regionJSON.put("region", key);
 				JSONArray values = new JSONArray();
 				for(String unit: treemap.keySet() ){	
 					JSONObject unitJSON= new JSONObject();
 					unitJSON.put(unit, treemap.get(unit));
 					values.put(unitJSON);
 				}										
 				regionJSON.put("values", values);
 			
 				// get all coprocessors of this region request
 				List<RCopResult> coprocessors = callback.coprocesses.get(key);
 				// add the statistics of request				
 				JSONArray copStatJSON = this.buildCopStat(coprocessors);
 				regionJSON.put(XConstants.REQUEST_STAT_RESPONSE_TIME, exe_time);
 				regionJSON.put(XConstants.REQUEST_COPS_STAT, copStatJSON);	
 				
 				
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 			this.response.put(regionJSON);
 		}
 		
 		return this.response;
 	}
 
 
 	/**
 	 * To compute the average value
 	 * @author dan
 	 *
 	 */
 
 	class SummaryCallBack implements Batch.Callback<RStatResult> {
 		//TODO this should be improved
 		public HashMap<String, RStatResult> regions = new HashMap<String,RStatResult>();
 		public Hashtable<String, List<RCopResult>> coprocesses = new Hashtable<String,List<RCopResult>>();
 		int count = 0; // the number of coprocessor
 		XBaseStatService service = null;
 
 		public SummaryCallBack(XBaseStatService s) {
 			this.service = s;
 				
 		}
 
 		@Override
 		public void update(byte[] region, byte[] row, RStatResult result) {	
 			LOG.info("SummaryCallBack: update");			
 			String regionPatient = result.getRegion();
 			Hashtable<String,Integer> hashUnit = result.getHashUnit();
 			if(this.regions.containsKey(regionPatient)){
 				Hashtable<String,Integer> temp = this.regions.get(regionPatient).getHashUnit();
 				for(String key:temp.keySet()){
 					temp.put(key, temp.get(key)+hashUnit.get(key));
 				}
 				List<RCopResult> cops = coprocesses.get(regionPatient);
 				cops.add(result.getCopStat());	
 				
 			}else{
 				this.regions.put(regionPatient, result);
 				List<RCopResult> cops = new ArrayList<RCopResult>();
 				cops.add(result.getCopStat());
 				this.coprocesses.put(regionPatient, cops);
 			}			
 			
 		}
 	}
 
 	/**
 	 * Not decided yet
 	 */
 	@Override
 	public JSONArray getAverage(JSONObject request) {
 		return this.response;
 	}
 	
 }

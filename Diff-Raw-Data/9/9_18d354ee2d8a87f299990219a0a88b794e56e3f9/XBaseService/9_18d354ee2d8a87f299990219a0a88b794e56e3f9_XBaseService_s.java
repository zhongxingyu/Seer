 package savi.hcat.rest.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hbase.client.HTable;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import savi.hcat.analytics.coprocessor.RCopResult;
 import savi.hcat.analytics.coprocessor.RStatResult;
 import savi.hcat.common.util.XConstants;
 
 import com.hbase.service.HBaseUtil;
 import com.util.XTableSchema;
 
 public class XBaseService {
 
 	private static Log LOG = LogFactory.getLog(XBaseStatService.class);
 	// output
 	protected JSONArray response = new JSONArray(); 
 	
 	protected HBaseUtil hbase = null;	
 	protected XTableSchema tableSchema = null;	
 	private String HBASE_CONF_PATH = "conf/myhbase.xml";
 	
 	// common parameters included in POST body
 	protected String start_time = null; // 2013-08-28 12:22:22
 	protected String end_time = null; // 2013-08-28 12:22:22
 	protected ArrayList<String> regions = new ArrayList<String>();
 	protected int split_num = 1;
 	
 	
 	/**
 	 * should be called second to connect with HBase
 	 * @param hbaseConfPath
 	 * @return
 	 * @throws IOException
 	 */
 	protected void setHBase() throws IOException {
 		LOG.info("in setHBase()");
 		try{
 			if(hbase == null){
 				hbase = new HBaseUtil(HBASE_CONF_PATH);
 				HTable tableHandler = hbase.getTableHandler(this.tableSchema.getTableName());
 				LOG.info("table name: "+this.tableSchema.getTableName());
 				String scanCache = hbase.getHBaseConfig().get("hbase.block.cache.size");	
 								
 				if(scanCache != null){				
 					hbase.setScanConfig(Integer.valueOf(scanCache), true);				
 				}else{					
 					System.out.println("Default scan cache: "+tableHandler.getScannerCaching());
 				}				
 			}else{
 				LOG.info("HBase is not null");
 			}
 						
 		}catch(Exception e){
 			if(hbase != null)
 				hbase.closeTableHandler();
 			e.printStackTrace();
 		}			
 	}
 	
 
 	protected boolean decomposeCommonParams(JSONObject reqObj){
 		System.out.println("[DEBUG]: in doComposeCommonParams");
 		try{
 			if(reqObj != null){
 				if(reqObj.has(XConstants.POST_KEY_START_TIME)){				
 					this.start_time = (String)reqObj.get(XConstants.POST_KEY_START_TIME);
 				}
 				if(reqObj.has(XConstants.POST_KEY_END_TIME)){
 					this.end_time = (String)reqObj.get(XConstants.POST_KEY_END_TIME);
 				}
 				if(reqObj.has(XConstants.POST_KEY_REGIONS)){
 					String cityObj = (String)reqObj.get(XConstants.POST_KEY_REGIONS);
 					String[] items = cityObj.split(",");
 					for(String item: items){
 						this.regions.add(item);	
 					}					
 				}
 				if(reqObj.has(XConstants.POST_KEY_SPLIT_NUM)){
 					this.split_num = Integer.valueOf(reqObj.getInt(XConstants.POST_KEY_SPLIT_NUM));
 				}				
 				
 				
 				return true;
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	private JSONObject buildOneCopStat(RCopResult one_cop){
 		JSONObject reqStatJSON = new JSONObject();
 		try{
 			reqStatJSON.put(XConstants.REQUEST_STAT_COP_EXE_TIME, one_cop.getEnd()-one_cop.getStart());
 			reqStatJSON.put(XConstants.REQUEST_STAT_CELLS, one_cop.getCells());
 			reqStatJSON.put(XConstants.REQUEST_STAT_ROWS, one_cop.getRows());
 			reqStatJSON.put(XConstants.REQUEST_CELL_SIZE, one_cop.getKvLength());
 			reqStatJSON.put(XConstants.REQUEST_STAT_START_ROW, one_cop.getStartRow());
			reqStatJSON.put(XConstants.REQUEST_STAT_END_ROW, one_cop.getStartRow());
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		return reqStatJSON;		
 	}
 	
 	public JSONArray buildCopStat(List<RCopResult> cops){
 		JSONArray cop_array = new JSONArray();
 		for(RCopResult one_cop: cops){
 			JSONObject oneJSON = buildOneCopStat(one_cop);
 			cop_array.put(oneJSON);
 		}
 		return cop_array;
 	}
 	
 	
 	public JSONArray getResponse() {
 		if(response==null)
 			response = new JSONArray();
 		return response;
 	}
 	
 	public String getStart_time() {
 		return start_time;
 	}
 	public String getEnd_time() {
 		return end_time;
 	}
 	
 }

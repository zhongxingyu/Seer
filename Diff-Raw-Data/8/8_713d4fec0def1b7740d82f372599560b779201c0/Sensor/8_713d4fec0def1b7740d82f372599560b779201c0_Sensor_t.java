 package com.yuktix.data;
 
 import java.util.HashMap;
 import java.util.List;
 
 import com.microsoft.windowsazure.services.table.client.CloudTableClient;
 import com.microsoft.windowsazure.services.table.client.DynamicTableEntity;
 import com.microsoft.windowsazure.services.table.client.EntityProperty;
 import com.microsoft.windowsazure.services.table.client.TableEntity;
 import com.microsoft.windowsazure.services.table.client.TableOperation;
 import com.microsoft.windowsazure.services.table.client.TableQuery;
 import com.yuktix.cloud.azure.Table;
 import com.yuktix.dto.provision.SensorParam;
 import com.yuktix.dto.query.ScrollingParam;
 import com.yuktix.dto.response.ResultSet;
 import com.yuktix.rest.exception.ArgumentException;
 import com.yuktix.rest.exception.RestException;
 import com.yuktix.util.Log;
 import com.yuktix.util.StringUtil;
 
 public class Sensor {
 	
 	public static void add(SensorParam param) {
 		
 		try {
 
 			StringUtil.null_check("projectId",param.getProjectId()) ;
 			StringUtil.null_check("deviceId",param.getDeviceId()) ;
 			
 			TableEntity entity1 ;
 			
 			HashMap<String, EntityProperty> data =  new HashMap<String, EntityProperty>();
 			HashMap<String,String> metaData = param.getMetaData() ;
 			String projectId = param.getProjectId() ;
 			List<String> groupKeys = param.getGroupKeys() ;
 			
 			if(groupKeys != null && (groupKeys.size() > 0)) {
 				if(metaData == null || (metaData.size() == 0)) {
 					throw new ArgumentException("group keys should be part of meta data");
 				}
 				
 				// group keys should be part of meta data
 				for(String key : param.getGroupKeys()) {
 					if(!metaData.containsKey(key)) {
 						throw new ArgumentException("group keys is not part of meta data");
 					}
 				}
 				
 				String groups = StringUtil.COMMA_JOINER.join(param.getGroupKeys());
 				data.put("groups", new EntityProperty(groups));
 			}
 			
 			// project specific partition
 			// E(size) = 10^4 for large deployments
 			String partitionKey = "sensordb;sensor;" + projectId;
 			data.put("deviceId", new EntityProperty(param.getDeviceId()));
 			data.put("projectId", new EntityProperty(param.getProjectId()));
 			
 			// meta data for a sensor is supplied at provisioning
 			// this information is project specific and is not 
 			// supplied by the sensor circuit.
 			// Any information supplied by sensor circuit should
 			// come in as a device variable.
 			if(metaData != null && (metaData.size() > 0)) {
 				for(String key : metaData.keySet()) {
 					data.put(key, new EntityProperty(metaData.get(key)));
 				}
 			}
 			
 			entity1 = new DynamicTableEntity(data);
 			entity1.setPartitionKey(partitionKey);
 			entity1.setRowKey(param.getSerialNumber());
 			TableOperation operation1 = TableOperation.insert(entity1);
 			 
 			CloudTableClient client = Table.getInstance();
 			client.execute("test", operation1);
 			
 		} catch(RestException rex) {
 			throw rex ;
 		} catch (Exception ex) {
 			Log.error(ex);
			throw new RestException("error adding new sensor");
 		}
 	}
 	
 	public static HashMap<String,Object> getOnId(String projectId,String guid) {
 		
 		try{
 			
 			String partitionKey = "sensordb;sensor;" + projectId;
 			HashMap<String,Object> map = Common.getEntity("test",partitionKey,guid);
 			return map ;
 			
 		} catch(RestException rex) {
 			throw rex ;
 		} catch(Exception ex) {
 			Log.error(ex);
			throw new RestException("error retrieving sensor");
 		}
 	}
 	
 	public static ResultSet list(String projectId,ScrollingParam param) {
 		
 		try {
 			
 			String partitionKey = "sensordb;sensor;" + projectId ;
 			String where_condition = String.format("(PartitionKey eq '%s')",partitionKey);
 			
 			TableQuery<DynamicTableEntity> myQuery = TableQuery
 					.from("test", DynamicTableEntity.class)
 					.where(where_condition).take(10);
 			
 			ResultSet result = Common.getSegmentedResultSet(myQuery,param);
 			return result ;
 			
 		} catch(RestException rex) {
 			throw rex ;
 		} catch(Exception ex) {
 			Log.error(ex);
			throw new RestException("error retrieving sensors");
 		}
 	}
 	
 }

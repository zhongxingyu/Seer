 package com.versionone.common.sdk;
 
 import com.versionone.Oid;
 import com.versionone.apiclient.Asset;
 import com.versionone.apiclient.IAssetType;
 import com.versionone.apiclient.IAttributeDefinition;
 import com.versionone.apiclient.IMetaModel;
 import com.versionone.apiclient.IServices;
 import com.versionone.apiclient.Query;
 import com.versionone.apiclient.QueryResult;
 import com.versionone.apiclient.V1Exception;
 
 public class TaskStatusCodes implements IStatusCodes {
 
 	class StatusCode {
 		private String _id;
 		private String _name;
 		
 		StatusCode(Oid oid, String name) {
 			_id = oid.getToken();
 			_name = name;
 		}
 		
 		String getId() {
 			return _id;
 		}
 		
 		String getName() {
 			return _name;
 		}
 	}
 
 	private StatusCode[] _status = null;
 
 	/**
 	 * Create
 	 * @param metaModel - metamodel to use for obtaining data
 	 * @param services  - services to use for obtaining data
 	 * @throws V1Exception - if we cannot read data
 	 */
 	TaskStatusCodes(IMetaModel metaModel, IServices services) throws V1Exception {
 		
 		IAssetType statusType = metaModel.getAssetType("TaskStatus");
 		IAttributeDefinition name = statusType.getAttributeDefinition("Name");
 
 		Query query = new Query(statusType);
 		query.getSelection().add(name);
 		QueryResult queryResults = services.retrieve(query);
 		Asset[] result = queryResults.getAssets();
 		
 		_status = new StatusCode[result.length+1];
 		_status[0] = new StatusCode(Oid.Null, "");
 		for(int i = 0; i < result.length; ++i) {
 			_status[i+1] = new StatusCode(result[i].getOid(), result[i].getAttribute(name).getValue().toString());
 		}
 	}
 
	@Override
 	public String getDisplayValue(int index) {
 		if( (index >= 0) && (index < _status.length) ){
 			return _status[index]._name;
 		}
 		throw new IndexOutOfBoundsException();
 	}
 
	@Override
 	public String[] getDisplayValues() {
 		String[] rc = new String[_status.length];
 		for(int i=0; i < rc.length; ++i) {
 			rc[i] = _status[i]._name;
 		}
 		return rc;
 	}
 
	@Override
 	public int getOidIndex(String oid) {
 		for(int i = 0; i < _status.length; ++i) {
 			if(oid.equals(_status[i]._id))
 				return i;
 		}
 		return 0;
 	}
 
	@Override
 	public String getID(int index) {
 		if( (index >= 0) && (index < _status.length) ){
 			return _status[index]._id;
 		}
 		throw new IndexOutOfBoundsException();
 	}
 
	@Override
 	public String getDisplayFromOid(String oid) {
 		String rc = null;
 		for(int i=0; i < _status.length; ++i) {
 			if(oid.equals(_status[i]._id)) {
 				rc = _status[i]._name; 
 			}
 		}
 		if(null == rc)
 			rc = "*** Invalid OID " + oid + "***";
 		return rc;
 	}
 }

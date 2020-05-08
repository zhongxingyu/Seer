 package com.htb.cnk.data;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 
 import android.util.Log;
 
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Server;
 
 public class TableSetting {
 	public class TableSettingItem {
 		protected int mStatus;
 		protected String mName;
 		protected int mId;
 
 		public TableSettingItem(int status, String name, int id) {
 			mStatus = status;
 			mName = name;
 			mId = id;
 		}
 
 		public void setStatus(int status) {
 			mStatus = status;
 		}
 
 		public int getStatus() {
 			return mStatus;
 		}
 
 		public int getId() {
 			return mId;
 		}
 		
 		public String getName(){
 			return mName;
 		}
 	}
 
 	static List<TableSettingItem> mTableSettings = new ArrayList<TableSettingItem>();
 
 	public TableSetting() {
 
 	}
 
 	public void add(TableSettingItem item) {
 		mTableSettings.add(item);
 	}
 
 	public int size() {
 		return mTableSettings.size();
 	}
 
 	public int getStatus(int index) {
 		return mTableSettings.get(index).getStatus();
 	}
 	
 	public int getStatusTableId(int index){
 		int i;
 		for(i = 0;i< mTableSettings.size()-1;i++){
 			if(index == mTableSettings.get(i).getId()){
 				break;
 			}
 		}
 		return mTableSettings.get(i).getStatus();
 	}
 	
 	public int getId(int index) {
 		return mTableSettings.get(index).getId();
 	}
 
 	public String getName(int index){
 		return mTableSettings.get(index).getName();
 	}
 	
 	public void setStatus(int index, int n) {
 		mTableSettings.get(index).setStatus(n);
 	}
 
 	public int getTableStatusFromServer() {
 		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS, "");
 		if(tableStatusPkg == null){
 			return -1;
 		}
 		try {
 			JSONArray tableList = new JSONArray(tableStatusPkg);
 			int length = tableList.length();
 			TableSettingItem asItem;
 			TableSetting setting = new TableSetting();
 			for (int i = 0; i < length; i++) {// 遍历JSONArray
 				JSONObject item = tableList.getJSONObject(i);
 				int id = item.getInt("id");
 				String name = item.getString("name");
 				int status = item.getInt("status");
 				asItem = new TableSettingItem(status, name, id);
 				setting.add(asItem);
 			}
 			return 0;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return -1;
 	}
	
	public int getItemTableStatus(int tableId) {
		String tableStatusPkg = Http.get(Server.GET_ITEM_TABLE_STATUS, "TSI=" + tableId);
		Log.d("res", tableStatusPkg);
		if(tableStatusPkg == null){
			return -1;
		}
		return Integer.parseInt(tableStatusPkg);
	}
	
 	public int updatusStatus(int tableId, int status) {
 		String tableStatusPkg = Http.get(Server.UPDATE_TABLE_STATUS, "TID="
 				+ tableId + "&TST=" + status);
 		if (tableStatusPkg == null) {
 			return -1;
 		}
 		return 0;
 	}
	
 
	
 	public int remove(int index) {
 		mTableSettings.remove(index);
 		return 0;
 	}
 
 	public int clear() {
 		mTableSettings.clear();
 		return 0;
 	}
 	
 	public int cleanTalble(int tableId){
 		JSONObject order = new JSONObject();
 		Date date = new Date();
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = df.format(date);
 		try {
 			order.put("timestamp", time);
 			order.put("TID", tableId);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		Log.d("JSON", order.toString());
 		String tableStatusPkg = Http.post(Server.CLEAN_TABLE,order.toString() );
 		if (tableStatusPkg == null) {
 			return -1;
 		}
 		return 0;
 	}
 
 
 
 	public void backUpTableStatus(int[] mTableStatus) {
 		for(int i=0 ;i<mTableSettings.size();i++){
 			mTableStatus[i] = getStatus(i);
 		}
 		
 	}
 
 
 }

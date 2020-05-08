 package com.zehjot.smartday.data_access;
 
 import java.io.File;
 import java.util.Calendar;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.zehjot.smartday.Config;
 import com.zehjot.smartday.MainActivity;
 import com.zehjot.smartday.R;
 import com.zehjot.smartday.data_access.DownloadTask.onDataDownloadedListener;
 import com.zehjot.smartday.data_access.LoadFileTask.onDataLoadedListener;
 import com.zehjot.smartday.data_access.UserData.OnUserDataAvailableListener;
 import com.zehjot.smartday.helper.Utilities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class DataSet implements OnUserDataAvailableListener, onDataDownloadedListener, onDataLoadedListener{
 	private static DataSet instance = null;
 	private static UserData userData = null;
 	private static Activity activity = null;
 	private static SharedPreferences sharedPreferences = null;
 	private static SharedPreferences.Editor editor = null;
 	private static JSONObject user = null;
 	private static long queryStart;
 	private static JSONObject selectedApps = null;
 	private static JSONObject ignoreApps = null;
 	private static JSONObject tmpJSONResult = null;
 	private static JSONObject tmpJSONResultToday = null;
 	private static long todayCacheMin = 5*60*1000;
 	private static JSONObject colorsOfApps = null;
 	
 	public static class RequestedFunction{
 		public static final String getAllApps= "getAllApps";
 		public static final String getEventsAtDate= "getEventsAtDate";
 		public static final String initDataSet= "initDataSet";
 		public static final String updatedFilter= "updatedFilter";
 	}
 	
 	protected DataSet(){
 		//For Singleton
 	}
 	
 	public void delete(){	
 		instance = null;
 		userData = null;
 		activity = null;
 		sharedPreferences = null;
 		editor = null;
 		user = null;
 		selectedApps = null;
 		ignoreApps = null;
 		tmpJSONResult = null;
 	}
 	
 	public static DataSet getInstance(Context context){
 		if(instance == null){
 			init(context);
 		}
 		return instance;
 	}
 	public static void updateActivity(Activity act){
 		if(act == null)
 			return;
 		activity = act;
 		userData.updateActivity(act);
 		updateDate();
 	}
 	
 	public static JSONObject getUser() {
 		return user;
 	}
 	
 	private static void init(Context context){
 		instance = new DataSet();
 		activity = (Activity) context;
 		createUserData();
 		sharedPreferences = activity.getPreferences(MainActivity.MODE_PRIVATE);
 		editor = sharedPreferences.edit();
 		selectedApps = new JSONObject();
 		//File file = new File(activity.getString(R.string.file_ignored_apps));
 		if( activity.getFileStreamPath(activity.getString(R.string.file_ignored_apps)).exists()){
 			try {
 				ignoreApps = new JSONObject(Utilities.readFile(activity.getString(R.string.file_ignored_apps), activity));
 			} catch (JSONException e) {
 				e.printStackTrace();
 				ignoreApps = new JSONObject();
 			}
 		}else
 			ignoreApps = new JSONObject();
 		
 		if( activity.getFileStreamPath(activity.getString(R.string.file_app_colors)).exists()){
 			try {
 				colorsOfApps = new JSONObject(Utilities.readFile(activity.getString(R.string.file_app_colors), activity));
 			} catch (JSONException e) {
 				colorsOfApps = new JSONObject();
 				e.printStackTrace();
 			}
 		}else
 			colorsOfApps = new JSONObject();
 		tmpJSONResult = new JSONObject();
 		tmpJSONResultToday = new JSONObject();
 	}
 
 	private static void initDate(){
 		final Calendar c = Calendar.getInstance();
 		int day = c.get(Calendar.DAY_OF_MONTH);
 		int month = c.get(Calendar.MONTH);
 		int year = c.get(Calendar.YEAR);
 		editor.putString(activity.getString(R.string.key_date_default), day+". "+activity.getResources().getStringArray(R.array.months)[month]+" "+year);
 		editor.putLong(activity.getString(R.string.key_date_default_timestamp), Utilities.getTimestamp(year, month, day, 0, 0, 0)).commit();
 		//editor.putString(activity.getString(R.string.key_date_selected_apps),"not Initialized"); //Sets String for selectDate to "not Initialized"
 		editor.commit();
 		editor.putString(activity.getString(R.string.key_date), day+". "+activity.getResources().getStringArray(R.array.months)[month]+" "+year);
 		editor.putInt(activity.getString(R.string.key_date_day), day);
 		editor.putInt(activity.getString(R.string.key_date_month), month);
 		editor.putInt(activity.getString(R.string.key_date_year), year);
 		editor.commit();
 		instance.getApps(null);
 	}
 	
 	private static void updateDate(){ 
 		final Calendar c = Calendar.getInstance();
 		int day = c.get(Calendar.DAY_OF_MONTH);
 		int month = c.get(Calendar.MONTH);
 		int year = c.get(Calendar.YEAR);
 		editor.putString(activity.getString(R.string.key_date_default), day+". "+activity.getResources().getStringArray(R.array.months)[month]+" "+year);
 		editor.putLong(activity.getString(R.string.key_date_default_timestamp), Utilities.getTimestamp(year, month, day, 0, 0, 0)).commit();		
 	}
 	
 	private static void createUserData(){
 		if(userData==null)
 			userData = new UserData();
 		userData.getUserLoginData(activity);
 	}
 	
 	public void createNewUser(){
 		userData.getUserLoginData(activity, true);
 	}
 	
 	public interface onDataAvailableListener{
 		public void onDataAvailable(JSONObject jObj, String requestedFunction);
 	}
 	
 	public void getApps(onDataAvailableListener listener){
 		getAppsAtDate(getSelectedDateAsTimestamp(),listener);
 	}
 	
 	private void getAppsAtDate(long timestamp, onDataAvailableListener listener){
 		long start=timestamp;
 		long end=getNextDayAsTimestamp();
 		
 		JSONObject data = new JSONObject();
 		try {
 			data.put("model", "SPECIFIC");
 			data.put("start", start);
 			data.put("end", end);/*
 			data.put("source", "MOBILE");
 			data.put("type", "APPSTART");
 			data.put("key", "app");*/
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		if(listener != null)
 			getData(listener, RequestedFunction.getEventsAtDate, data);
 		else
 			getData(listener, RequestedFunction.initDataSet, data);
 	}
 	
 	public JSONObject getSelectedApps(){
 		return selectedApps;
 	}
 	
 	public JSONObject getIgnoreApps() {
 		return ignoreApps;
 	}
 	
 	public void setIgnoreApps(JSONObject ignoreApps) {
 		DataSet.ignoreApps = ignoreApps;
 		Utilities.writeFile(activity.getString(R.string.file_ignored_apps), ignoreApps.toString(), activity);
 		((onDataAvailableListener)activity).onDataAvailable(null, RequestedFunction.updatedFilter);
 	}
 /*
 	public boolean[] getSelectedApps(ArrayList<String> apps){
 		boolean[] boolSelectedApps = new boolean[apps.size()];
 		for(int i=0 ; i<apps.size();i++){
 			boolSelectedApps[i] = selectedApps.optBoolean(apps.get(i));
 		}
 		return boolSelectedApps;
 	}
 */
 	public void setSelectedApps(JSONObject selectedApps){
 		DataSet.selectedApps = selectedApps;
 		((onDataAvailableListener)activity).onDataAvailable(null, RequestedFunction.updatedFilter);
 	}
 
 	public String getSelectedDateAsString(){
 		String date = getSharedString(R.string.key_date);
 		String default_date = sharedPreferences.getString(activity.getString(R.string.key_date_default), activity.getString(R.string.key_date_default));
 		if(date.equals(default_date))
 			return activity.getString(R.string.today);
 		return date;
 	}
 
 	public long getTodayAsTimestamp(){
 		return sharedPreferences.getLong(activity.getString(R.string.key_date_default_timestamp), -1);
 	}
 
 	public void setSelectedDate(int year,  int month, int day){
 		//TODO notify
 		editor.putString(activity.getString(R.string.key_date), day+". "+activity.getResources().getStringArray(R.array.months)[month]+" "+year);
 		editor.putInt(activity.getString(R.string.key_date_day), day);
 		editor.putInt(activity.getString(R.string.key_date_month), month);
 		editor.putInt(activity.getString(R.string.key_date_year), year);
 		editor.commit();
 		getApps((onDataAvailableListener) activity);
 	}
 	/*
 	public int[] getSelectedDateAsArray(){
 		int year = getSharedInt(R.string.key_date_year);
 		int month = getSharedInt(R.string.key_date_month);
 		int day = getSharedInt(R.string.key_date_day);
 		
 		return new int[] {year, month, day};
 	}
 	*/
 	public long getSelectedDateAsTimestamp(){
 		int year = getSharedInt(R.string.key_date_year);
 		int month = getSharedInt(R.string.key_date_month);
 		int day = getSharedInt(R.string.key_date_day);		
 		return Utilities.getTimestamp(year, month, day, 0, 0, 0);
 	}
 	public long getNextDayAsTimestamp(){	
 		return getSelectedDateAsTimestamp()+24*60*60;
 	}
 	
 	/** ColorsOfApps:
 	 * {
 	 * 	"colors":
 	 * 		[
 	 * 			{
 	 * 				"app":String,
 	 * 				"color": int
 	 * 			}
 	 * 			...
 	 * 		]
 	 * }
 	 */
 	public JSONObject getColorsOfApps() {
 		return colorsOfApps;
 	}
 	public void setColorsOfApps(JSONObject colorsOfApps) {
 		DataSet.colorsOfApps = colorsOfApps;
 		((onDataAvailableListener)activity).onDataAvailable(null, RequestedFunction.updatedFilter);
 		Utilities.writeFile(activity.getString(R.string.file_app_colors), colorsOfApps.toString(), activity);
 	}
 	public void storeColorsOfApps(JSONObject jObj){
 		DataSet.colorsOfApps = jObj;
 		Utilities.writeFile(activity.getString(R.string.file_app_colors), colorsOfApps.toString(), activity);		
 	}
 	@Override
 	public void onUserDataAvailable(JSONObject jObj) {
 		if(user==null){//first call of DataSet
 			user = jObj;
 			initDate();
 		}else{
 			user = jObj;
 			tmpJSONResult = null;
 			tmpJSONResultToday = null;
 		}
 	}
 
 	public void onDataDownloaded(int serverResponse, JSONObject jObj, String requestedFunction, onDataAvailableListener requester, String fileName){
 		/**
 		 * Gets called after server has send data
 		 * if result jObj is null it will ask for new Login data
 		 * else calls the requester and if a filename is given the data will be stored
 		 */
 		Log.d("Downloadtime", "Download time: "+(Utilities.getSystemTime()-queryStart)+"ms");
 	
 		if(jObj == null){
 			downloadTaskErrorHandler(jObj, serverResponse,requestedFunction);
 		}else{
 			if(requestedFunction.equals(RequestedFunction.getAllApps)){
 				if(requester!=null)
 					requester.onDataAvailable(constructAllAppNamesJSONObject(jObj), requestedFunction);
 				else{
 					//JSONObject result = constructColorJSONObj(jObj);
 					//setColorsOfApps(result);
 				}
 				return;
 			}else if(requestedFunction.equals(RequestedFunction.getEventsAtDate)){
 				JSONObject result = constructBasicJSONObj(jObj);
 				if(getSelectedDateAsTimestamp()<getTodayAsTimestamp()){
 					tmpJSONResult = result;					
 					if(fileName != null)
 						new StoreFileTask(activity).execute(result.toString(),fileName);
				}else{
 					tmpJSONResultToday = result;
 				}
 				if(requester!=null)
 					result = filterIgnoredApps(result);
 				requester.onDataAvailable(result, requestedFunction);
 				return;				
 			}else if(requestedFunction.equals(RequestedFunction.initDataSet)){
 				JSONObject result = constructBasicJSONObj(jObj);
 				if(getSelectedDateAsTimestamp()==getTodayAsTimestamp())
 					tmpJSONResultToday = result;
 				else
 					tmpJSONResult = result;
 				((onDataAvailableListener) activity).onDataAvailable(null, requestedFunction);
 				return;
 			}
 		}
 	}
 	public void onDataLoaded(JSONObject jObj, String requestedFunction, onDataAvailableListener requester, String fileName){
 		/**
 		 * gets called after internal data is loaded
 		 * deletes file if jObj==null
 		 * else calls the requester
 		 */
 
 		if(jObj == null && fileName!= null){
 			File file = activity.getFileStreamPath(fileName);
 			if(file.exists())
 				file.delete();
 		}else{
 			if(fileName!=null){
 				Log.d("InternalLoadtime", "Load time: "+(Utilities.getSystemTime()-queryStart)+"ms");
 				tmpJSONResult = jObj;
 			}
 			if(requester!=null)
 				jObj = filterIgnoredApps(jObj);
 			requester.onDataAvailable(jObj, requestedFunction);
 		}
 	}
 	/*
 	public void getContext(onDataAvailableListener requester){
 		getContext(getSelectedDateAsTimestamp(), getNextDayAsTimestamp(), requester);
 	}
 	*//*
 	public void getContext(long start, long end, onDataAvailableListener requester){
 		JSONObject data = new JSONObject();
 		try{
 		data.put("model", "SPECIFIC");
 		data.put("start", start);
 		data.put("end", end);
 		//data.put("type", "POSITION");
 		}catch(JSONException e){
 			
 		}
 		getData(requester, "events", data);
 	}
 	*/
 	private String getSharedString(int id){
 		return sharedPreferences.getString(activity.getString(id), activity.getString(R.string.error_no_string));
 	}
 	private int getSharedInt(int id){
 		return sharedPreferences.getInt(activity.getString(id), -1);
 	}
 	public void getAllApps(onDataAvailableListener requester){
 		JSONObject data = new JSONObject();
 		try{
 			data.put("type", "APPSTART");
 			data.put("key", "app");
 			data.put("start", Utilities.getTimestamp(2012, 0, 1, 0, 0, 0));
 			data.put("end",getTodayAsTimestamp());
 		}catch(JSONException e){
 			
 		}
 		getData(requester, RequestedFunction.getAllApps, data);
 	}
 	private JSONObject constructBasicJSONObj(JSONObject jObj){
 		JSONArray jArrayInput=null;
 		JSONArray lastKnownPos = null;
 		JSONArray jArrayOutput= new JSONArray();
 		JSONObject result=new JSONObject();
 		try{
 			result.put("dateTimestamp", getSelectedDateAsTimestamp());
 			result.put("downloadTimestamp", Utilities.getSystemTime());
 			jArrayInput = jObj.getJSONArray("events");					
 			for(int i=0; i<jArrayInput.length();i++){
 				JSONObject jObjInput = jArrayInput.getJSONObject(i);
 				if(jObjInput.getString("type").equals("APPSTART")){
 					boolean found = false;
 					
 					String appName = "NoAppNameFound";
 					JSONArray entities = jObjInput.getJSONArray("entities");
 					for(int x = 0; x<entities.length(); x++){
 						if(entities.getJSONObject(x).getString("key").equals("app")){
 							appName = entities.getJSONObject(x).getString("value");
 							break;
 						}
 					}
 					
 					String appSession = jObjInput.getString("session");
 					long time = jObjInput.getLong("timestamp");
 					for(int j = 0; j<jArrayOutput.length();j++){
 						JSONObject jObjOutput = jArrayOutput.getJSONObject(j);
 						if(jObjOutput.getString("app").equals(appName)){
 							JSONArray usages = jObjOutput.getJSONArray("usage");
 							for(int k = 0; k<usages.length();k++){
 								JSONObject usage = usages.getJSONObject(k);
 								if(usage.getString("session").equals(appSession)){
 									usage.put("end", time);
 									found = true;
 									break;
 								}
 							}
 							if(!found){
 								JSONObject usage = new JSONObject();
 								//usage.put("end", Utilities.getSystemTime()/1000);
 								usage.put("start", time);
 								usage.put("session",appSession);
 								usages.put(usage);
 								found = true;
 								break;
 							}
 						}
 					}
 					if(!found){
 						JSONObject app = new JSONObject();
 							JSONArray usages = new JSONArray();
 								JSONObject usage = new JSONObject();
 								usage.put("start", time);
 								usage.put("session",appSession);
 							usages.put(usage);
 						app.put("usage",usages);
 						app.put("app", appName);
 						jArrayOutput.put(app);
 					}
 				}else if(jObjInput.getString("type").equals("POSITION")){
 					long time = jObjInput.getLong("timestamp");
 					JSONArray location = jObjInput.getJSONArray("entities");
 					lastKnownPos = location;
 					for(int j = 0; j<jArrayOutput.length();j++){
 						JSONArray appUsages = jArrayOutput.getJSONObject(j).getJSONArray("usage");
 						for(int k = 0; k<appUsages.length();k++){
 							JSONObject usage = appUsages.getJSONObject(k);
 							if(usage.getLong("start")<=time && !usage.has("location")){
 								usage.put("location",location);
 							}
 						}
 					}
 				}
 			}
 			for(int i = 0; i<jArrayOutput.length(); i++){
 				for(int j = 0; j < jArrayOutput.getJSONObject(i).getJSONArray("usage").length(); j++){
 					if(!jArrayOutput.getJSONObject(i).getJSONArray("usage").getJSONObject(j).has("location")){
 						jArrayOutput.getJSONObject(i).getJSONArray("usage").getJSONObject(j).put("location", lastKnownPos);
 					}
 				}
 			}
 			result.put("result", jArrayOutput);
 		}catch(JSONException e){
 			
 		}
 		return result;
 	}
 	/*
 	private JSONObject constructAppNameJSONObj(JSONObject jObj){
 		JSONObject result= new JSONObject();
 		JSONArray output= new JSONArray();
 		try{
 			for(int i = 0; i< jObj.getJSONArray("result").length(); i++){
 				JSONObject app = jObj.getJSONArray("result").getJSONObject(i);
 				output.put(new JSONObject().put("app",app.getString("app")));			
 			}
 			result.put("result", output);
 		}catch(JSONException e){
 			return result;			
 		}
 		return result;
 	}
 	*/
 	private JSONObject constructAllAppNamesJSONObject(JSONObject jObj){
 		JSONObject result= new JSONObject();
 		JSONArray output= new JSONArray();
 		try{
 			result.put("downloadTimestamp", Utilities.getSystemTime());
 			for(int i = 0; i< jObj.getJSONArray("values").length(); i++){
 				JSONObject app = jObj.getJSONArray("values").getJSONObject(i);
 				output.put(new JSONObject().put("app",app.getString("value")));			
 			}
 			result.put("result", output);
 		}catch(JSONException e){
 			return result;			
 		}
 		return result;
 	}
 	private void getData(onDataAvailableListener requester, String requestedFunction, JSONObject jObj){
 		queryStart = Utilities.getSystemTime();
 		if(requestedFunction.equals(RequestedFunction.initDataSet)){
 			String fileName = Utilities.getFileName(requestedFunction, user, jObj,activity);
 			String url = Utilities.getURL(Config.Request.events, jObj.toString(), user, activity);
 			new DownloadTask(requester,activity).execute(url,requestedFunction,fileName);			
 		}else if(requestedFunction.equals(RequestedFunction.getAllApps)){
 			String url = Utilities.getURL(Config.Request.values,jObj.toString(),user, activity);
 			if(Config.getDebug())
 				queryStart = Utilities.getSystemTime();
 			new DownloadTask(requester,activity).execute(url,RequestedFunction.getAllApps,null);
 		}
 		else if(requestedFunction.equals(RequestedFunction.getEventsAtDate)){
 			/**
 			 * Checks if requested data is offline available and loads it.
 			 * If it's not available the server is requested.
 			 */			
 			//Check cached data
 			if(tmpJSONResultToday!=null 
 					&& getSelectedDateAsTimestamp() == getTodayAsTimestamp() 
 					&& (Utilities.getSystemTime()-tmpJSONResultToday.optLong("downloadTimestamp"))<todayCacheMin ){
 				onDataLoaded(tmpJSONResultToday, requestedFunction, requester, null);
 				return;
 			}else if(tmpJSONResult!=null 
 					&& tmpJSONResult.optLong(("dateTimestamp"),-1)==getSelectedDateAsTimestamp()){
 				onDataLoaded(tmpJSONResult, requestedFunction, requester, null);
 				return;				
 			}
 			
 			//Check stored Data fileExists not needed for Basic file		
 			boolean fileExists = false;
 			String fileName = Utilities.getFileName(requestedFunction, user, jObj,activity);
 			if(fileName != null)
 				fileExists = activity.getFileStreamPath(fileName).exists();
 			if(fileExists){
 				new LoadFileTask(requester, activity).execute(fileName, requestedFunction);
 			}else{
 				String url = Utilities.getURL(Config.Request.events, jObj.toString(), user, activity);
 				new DownloadTask(requester,activity).execute(url,requestedFunction,fileName);
 			}
 		}
 	}
 	private JSONObject filterIgnoredApps(JSONObject jObj){
 		JSONObject result = new JSONObject();
 		JSONArray output = new JSONArray();
 		try{
 		for(int i=0; i<jObj.getJSONArray("result").length();i++){
 			JSONObject app = jObj.getJSONArray("result").getJSONObject(i);
 			if(!ignoreApps.optBoolean(app.getString("app")))
 				output.put(app);
 		}
 		return result.put("result",output);
 		}catch(JSONException e){
 			return result;
 		}
 	}
 	private void downloadTaskErrorHandler(JSONObject jObj, int serverResponse, String requestedFunction){
 		ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 		if(networkInfo == null || !networkInfo.isConnected()){
 			Utilities.showDialog(activity.getString(R.string.info_no_data_connection),activity);
 			if(requestedFunction.equals(RequestedFunction.initDataSet))
 					((onDataAvailableListener) activity).onDataAvailable(null, requestedFunction);
 			return;
 			//
 		}
 		if(jObj == null){
 			String errorMessage;
 			switch (serverResponse) {
 			case -1:
 				if(user==null)
 					errorMessage = activity.getString(R.string.error_no_user_jObj);
 				else
 					errorMessage = activity.getString(R.string.error);
 				break;
 			case -2:
 				//task was canceled by user
 				return;
 			case 403:
 				errorMessage = activity.getString(R.string.error_authentication_fail)+serverResponse;
 				break;
 			case 404:
 				errorMessage = activity.getString(R.string.error)+serverResponse;
 				break;
 			default:
 				errorMessage = activity.getString(R.string.error);
 				break;
 			}
 			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 			builder.setMessage(errorMessage)
 				.setCancelable(false)
 				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			           @Override
 			           public void onClick(DialogInterface dialog, int id) {
 			        	  //Create or Recreate user data
 			        	  createUserData();
 			           }
 			       });
 			AlertDialog dialog = builder.create();
 			if(((MainActivity) activity).isRunning())
 				dialog.show();			
 		}else{
 			try {
 				Utilities.showDialog("Error occurred: "+jObj.getString("reason"),activity);
 			} catch (JSONException e) {
 				Utilities.showDialog("Error occured while trying to read server error, no reason stated.",activity);
 			}
 		}
 	}
 }

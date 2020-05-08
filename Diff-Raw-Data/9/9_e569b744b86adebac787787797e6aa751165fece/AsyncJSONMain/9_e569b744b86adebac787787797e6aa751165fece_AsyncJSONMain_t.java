 package com.appointmentmanager.helper;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.util.Log;
 
 import com.appointmentmanager.MainActivity;
 
 public class AsyncJSONMain extends AsyncTask<Void, Void, HashMap<String, Object>>{
 
 	private String hollisticURL, serviceURL, employeeURL;
 	private ProgressDialog progressDialog;
 	private Activity activity;
 	private Context context;
 
 	public AsyncJSONMain(String hollisticURL, String serviceURL, 
 			String employeeURL, Activity activity) {
 		//		Log.w("progress", "Async onstart URL");
 		this.hollisticURL = hollisticURL;
 		this.serviceURL = serviceURL;
 		this.employeeURL = employeeURL;
 		this.activity = activity;
 		context = activity.getApplicationContext();
 	}
 
 	protected void onPreExecute() {
 		progressDialog= ProgressDialog.show(activity, "Loading Service Information",
				"Retrieving data...", true);
 		//do initialization of required objects objects here                
 	}
 
 	protected HashMap<String, Object> doInBackground(Void... params) {
 
 		HashMap<String, Object> data = new HashMap<String, Object> ();
 
 		//hollistic
 		JSONArray dataArray = null;
 		JSONParser jParser = new JSONParser();
 		JSONObject json = jParser.getJSONFromUrl(hollisticURL);
 		try {
 			data.put("OPENING_TIME", json.getString("opening time"));
 			data.put("CLOSING_TIME", json.getString("closing time"));
 			data.put("MINIMUM_APPOINTMENT_TIME", json.getString("minimum appointment time"));
 		} catch (JSONException e) {
 			Log.v("progress", "JSON EXCEPTION");
 			e.printStackTrace();
 		}
 
 		//Services
 		dataArray = null;
 		jParser = new JSONParser();
 		json = jParser.getJSONFromUrl(serviceURL);
 		try {
 			dataArray = json.getJSONArray("services");
 
 			JSONObject c;
 
 			String[] serviceIDList = new String[dataArray.length()];
 			String[] serviceNameList = new String[dataArray.length()];
 			String[] serviceTimeList = new String[dataArray.length()];
 			String[] servicePriceList = new String[dataArray.length()];
 
 			for (int i=0; i<dataArray.length();i++ ) {
 				c = dataArray.getJSONObject(i);
 				serviceIDList[i] = c.getString("id");
 				serviceNameList[i] = c.getString("service");
 				serviceTimeList[i] = c.getString("service_time");
 				servicePriceList[i] = c.getString("price");
 			}
 
 			data.put("SERVICE_AMOUNT", dataArray.length());
 			data.put("SERVICE_ID_LIST", serviceIDList);
 			data.put("SERVICE_NAME_LIST", serviceNameList);
 			data.put("SERVICE_DURATION_LIST", serviceTimeList);
 			data.put("SERVICE_PRICE_LIST", servicePriceList);
 			
 			data.put("SERVICE_ID_LIST_NEW", combine(serviceIDList));
 			data.put("SERVICE_NAME_LIST_NEW", combine(serviceNameList));
 			data.put("SERVICE_DURATION_LIST_NEW", combine(serviceTimeList));
 			data.put("SERVICE_PRICE_LIST_NEW", combine(servicePriceList));
 
 		} catch (JSONException e) {
 			Log.v("progress", "JSON EXCEPTION");
 			e.printStackTrace();
 		}
 
 		//Employees
 		dataArray = null;
 		jParser = new JSONParser();
 		json = jParser.getJSONFromUrl(employeeURL);
 		try {
 			dataArray = json.getJSONArray("employees");
 
 			JSONObject c;
 
 			String[] idList = new String[dataArray.length()];
 			String[] nameList = new String[dataArray.length()];
 			String[] portraitList = new String[dataArray.length()];
 			String[] hobbyList = new String[dataArray.length()];
 			String[] yearList = new String[dataArray.length()];
 			String[] avaliableList = new String[dataArray.length()];
 
 			for (int i=0; i<dataArray.length();i++ ) {
 				c = dataArray.getJSONObject(i);
 				idList[i] = c.getString("id");
 				portraitList[i] = c.getString("portrait");
 				nameList[i] = c.getString("name");
 				hobbyList[i] = c.getString("hobbies");
 				yearList[i] = c.getString("years");
 				avaliableList[i] = c.getString("avaliable");
 			}
 
 			data.put("STAFF_AMOUNT", dataArray.length());
 			data.put("STAFF_ID_LIST", idList);
 			data.put("STAFF_NAME_LIST", nameList);
 			data.put("STAFF_PORTRAIT_LIST", portraitList);
 			data.put("STAFF_HOBBY_LIST", hobbyList);
 			data.put("STAFF_YEAR_LIST", yearList);
 			data.put("STAFF_AVALIABLE_LIST", avaliableList);
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		//save portraits to SD
 		String[] portraitList = (String[]) data.get("STAFF_PORTRAIT_LIST");
 		Bitmap bmp = null;
 		OutputStream outStream = null;
 		try {
 			String extStorageDirectory = Environment.getExternalStorageDirectory().toString()+"/aptmgr";
 			File dir = new File(extStorageDirectory);
 			if(!dir.exists()) dir.mkdirs();
 			
 			File testLoc;
 			
 			for (int i=0; i<portraitList.length; i++) { 
 				testLoc = new File(extStorageDirectory+"/employeePortrait"+(i+1)+".JPEG");
 //				if (testLoc.exists()) Log.e("progress", "employee"+i+" exists: "+testLoc.length());
 				
 				//integrate version number for updates
 				if (!testLoc.exists()) {
 					InputStream in = new java.net.URL(portraitList[i]).openStream();
 					bmp = BitmapFactory.decodeStream(in);
 					
 					File file = new File(extStorageDirectory, "employeePortrait"+(i+1)+".JPEG");
 					outStream = new FileOutputStream(file);
 					bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
 					outStream.flush();
 					outStream.close();
 				
 				}
 				
 			}
 		} catch (MalformedURLException e) {
 			Log.v("progress", "Something wrong with the bitmap URL");
 		} catch (IOException e) {
 			Log.v("progress", "Something wrong with the bitmap I/O");
 		}
 
 		return data;
 	}
 	@Override
 	protected void onPostExecute(HashMap<String, Object> result) {
 		progressDialog.cancel();
 
 		MainActivity.setMemoryMap(result);
 		
 //		String[] tempArr;
 //		for (Entry<String, Object> s:result.entrySet()) {
 //			if ( ((Entry<String, Object>) s).getValue() instanceof String[] ) {
 //				tempArr = (String[]) s.getValue();
 //				for (int i=0; i< tempArr.length; i++) {
 ////					SaveInfo("main", "", "");
 //					Log.i("progress", ((Entry<String, Object>) s).getKey() + (i+1) + ": " + tempArr[i]);
 //				}
 //			} else {
 //				SaveInfo("main", ((Entry<String, Object>) s).getKey(), ""+((Entry<String, Object>) s).getValue());
 //				Log.i("progress", ((Entry<String, Object>) s).getKey() + ": " + ((Entry<String, Object>) s).getValue());
 //			}
 //		}
 	}
 	
 	private String combine(String[] array) {
 		String ret = "";
 		
 		//middle dot for splits parsing
 		for (String s: array)
 			ret += (s + "");
 		
 		Log.e("progress", "COMBINED:  " + ret);
 		
 		return ret;
 	}
 	
 	private void SaveInfo(String area, String saveTag, String value){
 		SharedPreferences sharedPreferences = context.getSharedPreferences(area, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putString(saveTag, value);
 		editor.commit();
 	}
 
 	private String GetInfo(String tag, String which)
 	{	
 		SharedPreferences sharedPreferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
 		String savedStr = sharedPreferences.getString(which, "");
 		return savedStr;
 	}	  	
 
 }

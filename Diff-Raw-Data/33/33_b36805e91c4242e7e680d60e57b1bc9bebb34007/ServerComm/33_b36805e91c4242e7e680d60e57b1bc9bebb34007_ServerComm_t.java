 package com.dirtyunicorns.updater;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import android.util.Log;
 
 public class ServerComm extends Thread{
 	
 	private String Model;
 	private InputStream is;
 	private String result = "";
 	private String resultAll = "";
 	private String[] results;
 	private List<String> JSONVals = new ArrayList<String>();
 	private List<String> JSONValsAll = new ArrayList<String>();
 	private List<String> Dates = new ArrayList<String>();
 	
 	public ServerComm(String model) {
 		Model = model;
 	}
 	
 	
 	
 	public String getLatest() {
 		final CountDownLatch latch = new CountDownLatch(1);
 		Thread t = new Thread() {
         	
 			public void run() {
         		
         		try {
         			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         			nameValuePairs.add(new BasicNameValuePair("codename",Model));
         			
         			try {
         				HttpClient httpClient = new DefaultHttpClient();
        				HttpPost httpPost = new HttpPost("http://dirtyunicorns.com/dusite/vers.php");
         				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
         				HttpResponse response = httpClient.execute(httpPost);
         				HttpEntity entity = response.getEntity();
         				is = entity.getContent();
         			} catch (Exception e) {
         				Log.e("DU UPDATE", "Error in http connection " + e.toString());
         			}
         			
         			
         		
         			//convert response to string
         			try{
         			        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
         			        StringBuilder sb = new StringBuilder();
         			        String line = null;
         			        while ((line = reader.readLine()) != null) {
         			                sb.append(line + "\n");
         			        }
         			        is.close();
         			        if (!sb.toString().contains("codename"))
         			        {
         			        	result = "Device not found in the database";
         			        }
         			        else {
         			        	result=sb.toString();
         			        }
         			}catch(Exception e){
         			        Log.e("log_tag", "Error converting result "+e.toString());
         			}
         			
         			
         			if (result != "Device not found in the database") {
         				
         			
 	        			//parse json data
 	        			try{
 	        			        JSONArray jArray = new JSONArray(result);
 	        			        for(int i=0;i<jArray.length();i++){
 	        			                JSONObject json_data = jArray.getJSONObject(i);
 	        			                
 	        			                JSONVals.add(json_data.getString("codename")+
 	        			                        ","+json_data.getDouble("vers")+
 	        			                        ","+json_data.getInt("buildnum")+
 	        			                        ","+json_data.getString("link") +
 	        			                        ","+json_data.get("uploaded"));
 	        			                Dates.add(json_data.get("uploaded").toString());
 	        			                
 	        			        }
 	        			}catch(JSONException e){
 	        			        Log.e("log_tag", "Error parsing data "+e.toString());
 	        			}
 	                   String latest = Collections.max(Dates);
 	                   
 	                   for (String vals : JSONVals) {
 	                	   if (vals.contains(latest)) {
 	                		   result = vals;
 	                	   }
 	                   }
 	                   latch.countDown();
         			}
         			else
         			{
         				latch.countDown();
         			}
 	            } catch (Exception e) {
 	            	e.printStackTrace();
 	            }
         	}
         };
         t.start();
         try {
 			latch.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
         System.out.println(result);
 		return result;
 	}
 	
 	
 	public String[] getAll() {
 		final CountDownLatch latch = new CountDownLatch(1);
 		Thread t = new Thread() {
         	
 			public void run() {
         		
         		try {
         			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
         			nameValuePairs.add(new BasicNameValuePair("codename",Model));
         			
         			try {
         				HttpClient httpClient = new DefaultHttpClient();
         				HttpPost httpPost = new HttpPost("http://dirtrom.com/du/vers.php");
         				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
         				HttpResponse response = httpClient.execute(httpPost);
         				HttpEntity entity = response.getEntity();
         				is = entity.getContent();
         			} catch (Exception e) {
         				Log.e("DU UPDATE", "Error in http connection " + e.toString());
         			}
         			
         			
         		
         			//convert response to string
         			try{
         			        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
         			        StringBuilder sb2 = new StringBuilder();
         			        String line = null;
         			        while ((line = reader.readLine()) != null) {
         			                sb2.append(line + "\n");
         			        }
         			        is.close();
         			        System.out.println(sb2.toString());
         			        resultAll=sb2.toString();
         			}catch(Exception e){
         			        Log.e("log_tag", "Error converting result "+e.toString());
         			}
         			 
         			//parse json data
         			try{
         			        JSONArray jArray = new JSONArray(resultAll);
         			        for(int i=0;i<jArray.length();i++){
         			                JSONObject json_data = jArray.getJSONObject(i);
         			                
         			                JSONValsAll.add(json_data.getString("codename")+
         			                        ","+json_data.getDouble("vers")+
         			                        ","+json_data.getInt("buildnum")+
         			                        ","+json_data.getString("link") +
         			                        ","+json_data.get("uploaded"));
         			                
         			        }
         			}catch(JSONException e){
         			        Log.e("log_tag", "Error parsing data "+e.toString());
         			}
         		   if (JSONValsAll.size() > 10)
         		   {
         			   results = new String[10];
         		   }
         		   else
         		   {
         			   results = new String[JSONValsAll.size()];
         		   }
                    int i = 0;
                    for (String vals : JSONValsAll) {
                 	   results[i] = vals;
                 	   i++;
                    }
                    latch.countDown();
 	            } catch (Exception e) {
 	            	
 	            }
         	}
         };
         t.start();
         try {
 			latch.await();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return results;
 	}
 
 }

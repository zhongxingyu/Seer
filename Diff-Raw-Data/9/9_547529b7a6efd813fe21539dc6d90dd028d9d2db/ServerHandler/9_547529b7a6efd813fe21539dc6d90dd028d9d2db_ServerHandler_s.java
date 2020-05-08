 package com.cs4750.finalproject;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 public class ServerHandler {
 	
 	private static final String server = "http://people.virginia.edu/~blw9u/cs4750/server.php";
 	String result;
 	ArrayList<String> dataResult;
 	ArrayList<NameValuePair> nameValuePairs;
 	HttpClient httpClient;
 
 	public ServerHandler(){
 		httpClient = new DefaultHttpClient();
 		nameValuePairs = new ArrayList<NameValuePair>();
 	}
 
 	public String signIn(String username, String pw){
 		nameValuePairs.add(new BasicNameValuePair("command","signIn"));
 		nameValuePairs.add(new BasicNameValuePair("username",username));
 		nameValuePairs.add(new BasicNameValuePair("password",pw));
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 				//dataResult.add(line);
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return result;
 	}
 	public String addUser(String name, String lastName, String age, String email, String phone, String screenName, String pw, boolean... user){
 		nameValuePairs.add(new BasicNameValuePair("command","addUser"));
 		nameValuePairs.add(new BasicNameValuePair("name",name));
		nameValuePairs.add(new BasicNameValuePair("lastname",name));
 		nameValuePairs.add(new BasicNameValuePair("age",age));
 		nameValuePairs.add(new BasicNameValuePair("email",email));
 		nameValuePairs.add(new BasicNameValuePair("phone",phone));
 		nameValuePairs.add(new BasicNameValuePair("username",screenName));
 		nameValuePairs.add(new BasicNameValuePair("password",pw));
 		nameValuePairs.add(new BasicNameValuePair("userType1",String.valueOf(user[0])));
 		nameValuePairs.add(new BasicNameValuePair("userType2",String.valueOf(user[1])));
 		nameValuePairs.add(new BasicNameValuePair("userType3",String.valueOf(user[2])));
 		nameValuePairs.add(new BasicNameValuePair("userType4",String.valueOf(user[3])));
 		nameValuePairs.add(new BasicNameValuePair("userType5",String.valueOf(user[4])));
 		nameValuePairs.add(new BasicNameValuePair("userType6",String.valueOf(user[5])));
 		nameValuePairs.add(new BasicNameValuePair("userType7",String.valueOf(user[6])));
 			
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			dataResult = new ArrayList<String>();
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 				dataResult.add(line);
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return result;
 	}
 	public String createPostRequest(String data){
 		nameValuePairs.add(new BasicNameValuePair("command",data));
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 				dataResult.add(line);
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return result;
 	}
 	
 	public ArrayList<String> getUserMachines(String userId){
 		nameValuePairs.add(new BasicNameValuePair("command","getUserMachines"));
 		nameValuePairs.add(new BasicNameValuePair("userId", userId));
 		dataResult = new ArrayList<String>();
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 				dataResult.add(line);
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return dataResult;
 	}
 	
 	public ArrayList<String> getUserClasses(String userId){
 		nameValuePairs.add(new BasicNameValuePair("command","getUserClasses"));
 		nameValuePairs.add(new BasicNameValuePair("userId", userId));
 		dataResult = new ArrayList<String>();
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 				dataResult.add(line);
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return dataResult;
 	}
 	public String getUserId(String data){
 		
 		nameValuePairs.add(new BasicNameValuePair("command","getUserId"));
 		nameValuePairs.add(new BasicNameValuePair("user_name",data));
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return result;
 	}
 	
 	
 	public void setMachineUser(String userId, String machineId){
 		
 		nameValuePairs.add(new BasicNameValuePair("command","setMachineUser"));
 		nameValuePairs.add(new BasicNameValuePair("userId",userId));
 		nameValuePairs.add(new BasicNameValuePair("machineId",machineId));
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 	}
 	
 	public String getMachineUser( String machineId){
 		
 		nameValuePairs.add(new BasicNameValuePair("command","getMachineUser"));
 		nameValuePairs.add(new BasicNameValuePair("machineId",machineId));
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return result;
 	}
 	public ArrayList<String> createPostRequestArrayList(String data){
 		dataResult = new ArrayList<String>();
 		nameValuePairs.add(new BasicNameValuePair("command",data));
 		String line;
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse  = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 				dataResult.add(line);
 			}
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		nameValuePairs.clear();
 		return dataResult;
 	}
 	
 	public String signUp(String userId, String classId ){
 		nameValuePairs.add(new BasicNameValuePair("command","signUp"));
 		nameValuePairs.add(new BasicNameValuePair("userId",userId));
 		nameValuePairs.add(new BasicNameValuePair("classId",classId));
 		String line;
 	
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public String checkIn(String userId, String classId ){
 		nameValuePairs.add(new BasicNameValuePair("command","checkIn"));
 		nameValuePairs.add(new BasicNameValuePair("user_id",userId));
 		nameValuePairs.add(new BasicNameValuePair("class_id",classId));
 		String line;
 	
 		try {
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	//for machines
 	public String changeAvailibility(String avail, String id, String type){
 		nameValuePairs.add(new BasicNameValuePair("command","changeAvailibility"));
 		nameValuePairs.add(new BasicNameValuePair("avail",avail));
 		nameValuePairs.add(new BasicNameValuePair("id",id));
 		nameValuePairs.add(new BasicNameValuePair("type", type));
 		String line;
 		try{
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 		e.printStackTrace();
 	}
 	nameValuePairs.clear();
 	return result;
 		
 	}
 	
 	public String getClass(String classId){
 		nameValuePairs.add(new BasicNameValuePair("command","getClass"));
 		nameValuePairs.add(new BasicNameValuePair("classId",classId));
 		String line;
 		try{
 			HttpPost httpPost = new HttpPost(server);
 			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 			HttpResponse httpResponse = httpClient.execute(httpPost);
 			HttpEntity entity = httpResponse.getEntity();
 			InputStream is = entity.getContent();
 			
 			//get response string...
 			BufferedReader bf = new BufferedReader(new InputStreamReader(is));
 			StringBuilder sb = new StringBuilder();
 			while((line = bf.readLine()) != null){        
 				sb.append(line + "\n");
 			}
 
 			is.close();
 			result = sb.toString();
 		} catch (Exception e) {
 		e.printStackTrace();
 	}
 	nameValuePairs.clear();
 	return result;
 		
 	}
 	
 	
 	//nameValuePairs.add(new BasicNameValuePair("command","getRooms"));
 	
 /*	try{
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost("http://people.virginia.edu/~blw9u/cs4750/server.php");
 		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		HttpResponse response= httpClient.execute(httpPost);
 		HttpEntity entity = response.getEntity();
 		InputStream is = entity.getContent();
 		
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 		String line = "";
 		while((line = reader.readLine()) != null){
 			sb.append(line + "\n");
 		}
 		is.close();
 		result = sb.toString();
 } catch(Exception e){
 	e.printStackTrace();
 }*/
 }

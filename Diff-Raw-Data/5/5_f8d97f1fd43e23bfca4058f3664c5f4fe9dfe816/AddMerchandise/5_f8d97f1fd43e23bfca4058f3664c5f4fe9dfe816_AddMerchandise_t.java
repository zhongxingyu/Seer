 package com.sample.onpremise;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 
 public class AddMerchandise {
 
 		public static void main(String [] args){
 			Properties props = new Properties();
 
             try {
 
 	            props.load(new FileInputStream("AddMerchandise.properties"));
 
 	            String username = props.getProperty("username");
 	            String passsword = props.getProperty("password");
 	            
 	            if (username == null || username.trim().equals("") || 
	            	password == null || password.trim().equals("")){
 	            	throw new IllegalArgumentException("Please provide a username and password in the properties file");
 	            }
 
 	            String clientId = props.getProperty("client.id");
 	            String clientSecret = props.getProperty("client.secret");
 	            if (clientId == null || clientId.trim().equals("") ||
 	            	clientSecret == null || clientSecret.trim().equals("")){
 	            	throw new IllegalArgumentException("Please provide a valid client id and client secret in the properties file");
 	            }
 
 	            String loginURL = props.getProperty("login.url");
 	            
 	            String name = props.getProperty("merchandise.name");
 	            String price = props.getProperty("merchandise.price");
 	            String desc = props.getProperty("merchandise.description");
 	            String inventory = props.getProperty("merchandise.inventory");
 	            
	            ForceLogin login = login2ForceDotCom(username, password, 
 	            				    				 clientId, clientSecret, 
 	            									 loginURL);
 	            if (login != null){	
 	            	String merchId = addMerchandise(login, name, price, desc, inventory);
 	            	if (merchId != null){
 	            		System.out.println("Merchandise record created successfully. Id="+merchId);
 	            	}
 	            }
             }
             catch(IOException e){
             	e.printStackTrace();
             }
 		}
 		
 /*		private static ForceLogin login2ForceDotCom(String uName, 
 												String pwd,
 												String id,
 												String secret,											
 												String url){
 			String postURL = (url==null || url.equals(""))?"https://login.salesforce.com":url;
 			
 			postURL+="/services/oauth2/token?grant_type=password&client_id="+id+"&client_secret="+secret+"&username="+uName+"&password="+pwd;
 			
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			HttpPost post = new HttpPost(postURL);
 			
 			try {
             	HttpResponse response = httpclient.execute(post);
                 final int statusCode = response.getStatusLine().getStatusCode();
                 if (statusCode != HttpStatus.SC_OK) {
                 	System.out.println("Error authenticating to Force.com:"+statusCode);
                 	System.out.println("Error is:"+EntityUtils.toString(response.getEntity()));
                 	return null;
                 }
                 
                 String result = EntityUtils.toString(response.getEntity()); 
     			JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
     			
     			ForceLogin login = new AddMerchandise().new ForceLogin();
     			login.accessToken = object.getString("access_token");
     			login.instanceUrl= object.getString("instance_url");
     			
     			return login;
             }	
     	    catch (Exception e)
     	    {
     	    	e.printStackTrace();
     	    }	
 			return null;
 		} */
 		
 /*		public static String addMerchandise(ForceLogin login,
 											String name,
 											String price,
 											String desc,
 											String inventory){
 			JSONObject mechandise = new JSONObject();
 			
 			try {
 				if (name != null && !name.trim().equals(""))
 					mechandise.put("Name", name);
 				if (price != null && !price.trim().equals(""))
 					mechandise.put("Price__c", price);
 				if (desc != null && !desc.trim().equals(""))
 					mechandise.put("Description__c", desc);
 				if (inventory != null && !inventory.trim().equals(""))
 					mechandise.put("Total_Inventory__c", inventory);
 
 				String restResourceURI = login.instanceUrl + "/services/data/v23.0/sobjects/Merchandise__c/";
 
 				HttpPost post = new HttpPost(restResourceURI);
 				StringEntity se = new StringEntity(mechandise.toString());
 				post.setEntity(se);
 				post.setHeader("Authorization", "OAuth " + login.accessToken);
 				post.setHeader("Content-type", "application/json");
 					
 				DefaultHttpClient client = new DefaultHttpClient();
 		    	HttpResponse resp = client.execute(post);
 		    	String result = EntityUtils.toString(resp.getEntity()); 
 		    			    	
 		    	
 		    	if (resp.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED)
 		    	{
 		    		JSONObject ret = ((JSONArray) new JSONTokener(result).nextValue()).getJSONObject(0);
 		    		System.out.println("Could not create new Merchandise record:"+resp.getStatusLine().getStatusCode());
                 	System.out.println("Error code:"+ret.getString("errorCode"));
                 	System.out.println("Error message:"+ret.getString("message"));
 		    		return null;
 		    	}
 		    	else{
 		    	   	return ((JSONObject) new JSONTokener(result).nextValue()).getString("id");
 		    	}   	
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
  		
 			return null;
 		} */
 		
 		public class ForceLogin {
 			private String instanceUrl;
 			private String accessToken;
 			
 			public String getInstanceUrl()
 			{
 				return instanceUrl;
 			}
 			
 			public void setInstanceUrl(String i)
 			{
 				instanceUrl = i;
 			}
 
 			public String getAccessToken()
 			{
 				return accessToken;
 			}
 			
 			public void setAccessToken(String a)
 			{
 				accessToken = a;
 			}
 		}
 }

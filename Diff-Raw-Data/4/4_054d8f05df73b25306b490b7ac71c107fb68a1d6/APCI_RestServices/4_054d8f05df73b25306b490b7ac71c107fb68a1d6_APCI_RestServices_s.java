 package com.allplayers.android;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.json.JSONObject;
 
 public class APCI_RestServices
 {
 	public static String user_id = "";
 	public static String session_cookie = ""; //first session cookie
 	public static String chocolatechip_cookie = ""; //second cookie
 	
 	public APCI_RestServices()
 	{
 		//Create a trust manager that does not validate certificate chains
 		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
 		{
 			public java.security.cert.X509Certificate[] getAcceptedIssuers()
 			{
 				return null;
 			}
 			
 			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
 			{
 			}
 			
 			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
 			{
 			}
 		}};
 		
 		//Install the all-trusting trust manager
 		try
 		{
 			SSLContext sc = SSLContext.getInstance("SSL");
 			sc.init(null, trustAllCerts, new java.security.SecureRandom());
 			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		}
 		catch (Exception ex)
 		{
 		}
 		//Now you can access an https URL without having the certificate in the truststore
 	}
 	
 	public static boolean isLoggedIn()
 	{
 		if(user_id.equals(""))
 		{
 			return false;
 		}
 		
 		//Check an authorized call
 		try
 		{
 			URL url = new URL("https://www.allplayers.com/?q=api/v1/rest/users/" + user_id + ".json");
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			connection.setDoInput(true);
 			connection.addRequestProperty("Cookie", chocolatechip_cookie + ";" + session_cookie);
 			//connection.addRequestProperty("Cookie", session_cookie);
 			InputStream inStream = connection.getInputStream();
 			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
 			
 			String line = "";
 
 			String result = "";
 
 			while((line = input.readLine()) != null)
 			{
 				result += line;
 			}
 			
 			JSONObject jsonResult = new JSONObject(result);
 			String retrievedUUID = jsonResult.getString("uuid");
 			
 			if(retrievedUUID.equals(user_id))
 			{
 				return true;
 			}
 			else //This case should not occur
 			{
 				return false;
 			}
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return false;
 		}
 	}
 	
 	public static String deleteMessage(int threadId, String type)
 	{
 		//String[][] contents = new String[1][2];
 		//Type: thread or message (default = thread)
 		
 		return makeAuthenticatedDelete("https://www.allplayers.com/?q=api/v1/rest/messages/" + threadId + ".json");
 	}
 	
 	//Change read/unread status
 	public static String putMessage(int threadId, int status, String type)
 	{
 		String[][] contents = new String[1][2];
 		//Status: 1=unread, 0=read
 		contents[0][0] = "status";
 		contents[0][1] = "" + status;
 		//Type: thread or message (default = thread)
 		
 		return makeAuthenticatedPut("https://www.allplayers.com/?q=api/v1/rest/messages/" + threadId + ".json", contents);
 	}
 	
 	public static String validateLogin(String username, String password)
 	{
 		String[][] contents = new String[2][2];
 		contents[0][0] = "username";
 		contents[0][1] = username;
 		contents[1][0] = "password";
 		contents[1][1] = password;
 		
 		return makeAuthenticatedPost("https://www.allplayers.com/?q=api/v1/rest/users/login.json", contents);
 	}
 	
 	public static String postMessage(int threadId, String body)
 	{
 		String[][] contents = new String[2][2];
 		contents[0][0] = "thread_id";
 		contents[0][1] = "" + threadId;
 		contents[1][0] = "body";
 		contents[1][1] = body;
 		
 		return makeAuthenticatedPost("https://www.allplayers.com/?q=api/v1/rest/messages.json", contents);
 	}
 	
 	public static String searchGroups(String search, int zipcode, int distance)
 	{
 		return makeUnauthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups.json&search=\"" + search + "\"" +
 				"&distance[postal_code]="+ zipcode + "&distance[search_distance]=" + distance + "&distance[search_units]=mile");
 	}
 	
 	public static String getUserGroups()
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/" + user_id + "/groups.json");
 	}
 	
 	public static String getUserFriends()
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/" + user_id + "/friends.json");
 	}
 	
 	public static String getUserGroupmates()
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/" + user_id + "/groupmates.json");
 	}
 	
 	public static String getUserEvents()
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/users/" + user_id + "/events.json");
 	}
 	
 	public static String getGroupInformationByGroupId(String group_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/" + group_uuid + ".json");
 	}
 	
 	public static String getGroupAlbumsByGroupId(String group_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/" + group_uuid + "/albums.json");
 	}
 	
 	public static String getGroupEventsByGroupId(String group_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/" + group_uuid + "/events.json");
 	}
 	
 	public static String getGroupMembersByGroupId(String group_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/" + group_uuid + "/members.json");
 	}
 	
 	public static String getGroupPhotosByGroupId(String group_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/groups/photos.json");
 	}
 	
 	public static String getAlbumByAlbumId(String album_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/albums/" + album_uuid + ".json");
 	}
 	
 	public static String getAlbumPhotosByAlbumId(String album_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/albums/" + album_uuid + "/photos.json");
 	}
 	
 	public static String getPhotoByPhotoId(String photo_uuid)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/photos/" + photo_uuid + ".json");
 	}
 	
 	public static String getUserInbox()
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/messages.json&box=inbox");
 	}
 	
 	public static String getUserSentBox()
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/messages.json&box=sent");
 	}
 	
 	public static String getUserMessagesByThreadId(String thread_id)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/messages/" + thread_id + ".json");
 	}
 	
 	public static String getEventByEventId(String event_id)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/events/" + event_id + ".json");
 	}
 	
 	public static String getUserResourceByResourceId(String resource_id)
 	{
 		return makeAuthenticatedGet("https://www.allplayers.com/?q=api/v1/rest/resources/" + resource_id + ".json");
 	}
 	
 	private static String makeAuthenticatedGet(String urlString)
 	{
 		if(!isLoggedIn())
 		{
 			return "You are not logged in";
 		}
 		
 		//Make and return from authenticated get call
 		try
 		{
 			URL url = new URL(urlString);
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			connection.setDoInput(true);
 			connection.setRequestProperty("Cookie", chocolatechip_cookie + ";" + session_cookie);
 			InputStream inStream = connection.getInputStream();
 			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
 			
 			String line = "";
 			String result = "";
 			while((line = input.readLine()) != null)
 			{
 				result += line;
 			}
 			
 			return result;
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 	
 	private static String makeAuthenticatedDelete(String urlString)
 	{
 		if(!isLoggedIn())
 		{
 			return "You are not logged in";
 		}
 		
 		//Make and return from authenticated delete call
 		try
 		{
 			URL url = new URL(urlString);
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			
 			connection.setDoOutput(true);
 			connection.setRequestMethod("DELETE");
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			connection.connect();
 			
 			return "done";
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 	
 	private static String makeAuthenticatedPut(String urlString, String[][] contents)
 	{
 		if(!isLoggedIn())
 		{
 			return "You are not logged in";
 		}
 		
 		//Make and return from authenticated put call
 		try
 		{
 			URL url = new URL(urlString);
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			
 			connection.setDoOutput(true);
 			connection.setDoInput(true);
 			connection.setRequestMethod("PUT");
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
 			
 			DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
 			
 			//Send PUT output.
 			String content = "";
 			if(contents.length > 0)
 			{
 				for(int i = 0; i < contents.length; i++)
 				{
 					if(i > 0)
 					{
 						content += "&";
 					}
 					
 					content += contents[i][0] + "=" + URLEncoder.encode(contents[i][1], "UTF-8");
 				}
 			}
 			
 			printout.writeBytes(content);
 			printout.flush();
 			printout.close();
 			
 			return "done";
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 	
 	private static String makeUnauthenticatedGet(String urlString)
 	{
 		//Make and return from unauthenticated get call
 		try
 		{
 			URL url = new URL(urlString);
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			InputStream inStream = connection.getInputStream();
 			BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
 			
 			String line = "";
 			String result = "";
 			while((line = input.readLine()) != null)
 			{
 				result += line;
 			}
 			
 			return result;
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 	
 	private static String makeAuthenticatedPost(String urlString, String[][] contents)
 	{
 		//Make and return from authenticated post call
 		try
 		{
 			URL url = new URL(urlString);
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 			connection.setUseCaches(false);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			
 			//If not logging in, set the cookies in the header
 			if(!urlString.equals("https://www.allplayers.com/?q=api/v1/rest/users/login.json"))
 			{
 				connection.setRequestProperty("Cookie", chocolatechip_cookie + ";" + session_cookie);
 			}
 			
 			DataOutputStream printout = new DataOutputStream(connection.getOutputStream());
 			
 			//Send POST output.
 			String content = "";
 			if(contents.length > 0)
 			{
 				for(int i = 0; i < contents.length; i++)
 				{
 					if(i > 0)
 					{
 						content += "&";
 					}
 					
 					content += contents[i][0] + "=" + URLEncoder.encode(contents[i][1], "UTF-8");
 				}
 			}
 			
 			printout.writeBytes(content);
 			printout.flush();
 			printout.close();
 			
 			//Get response data.
 			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			String str;
 			
 			String result = "";
 			while((str = input.readLine()) != null)
 			{
 				result += str;
 			}
 			
 			input.close();
 			
 			//If logging in, store the cookies for future use
 			if(urlString.equals("https://www.allplayers.com/?q=api/v1/rest/users/login.json"))
 			{
 				setCookies(connection);
 			}
 			
 			return result;
 		}
 		catch(Exception ex)
 		{
 			System.out.println(ex);
 			return ex.toString();
 		}
 	}
 
 	private static void setCookies(HttpURLConnection connection)
 	{
 		//Get all cookies from the server
 		for(int i = 0; ; i++)
 		{
 			String headerName = connection.getHeaderFieldKey(i);
 			String headerValue = connection.getHeaderField(i);
 			
 			if (headerName == null && headerValue == null)
 			{
 				//No more headers
 				break;
 			}
 			
 			if("Set-Cookie".equalsIgnoreCase(headerName))
 			{
 				//parse cookie
 				String[] fields = headerValue.split(";\\s*");
 				
 				//Only cookieValue is used for now
 				String cookieValue = fields[0];
 				//String expires = null;
 				//String path = null;
 				//String domain = null;
 				//boolean secure = false;
 				
 				//Parse each field
 				/*for(int j=1; j<fields.length; j++)
 				{
 					if("secure".equalsIgnoreCase(fields[j]))
 					{
 						//secure = true;
 					}
 					else if(fields[j].indexOf('=') > 0)
 					{
 						String[] f = fields[j].split("=");
 						if("expires".equalsIgnoreCase(f[0]))
 						{
 							//expires = f[1];
 						}
 						else if("domain".equalsIgnoreCase(f[0]))
 						{
 							//domain = f[1];
 						}
 						else if("path".equalsIgnoreCase(f[0]))
 						{
 							//path = f[1];
 						}
 					}
 				}*/
 				
 				if(cookieValue.startsWith("SESS"))
 				{
 				session_cookie = cookieValue;
 				}
 				else if(cookieValue.startsWith("CHOCOLATECHIP"))
 				{
 				chocolatechip_cookie = cookieValue;
 				}
 			}
 		}
 	}
 }

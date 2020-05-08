 package de.hska.shareyourspot.android.restclient;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.simpleframework.xml.Serializer;
 import org.simpleframework.xml.core.Persister;
 
 import de.hska.shareyourspot.android.activites.GoogleMaps;
 import de.hska.shareyourspot.android.activites.MainActivity;
 import de.hska.shareyourspot.android.domain.Comment;
 import de.hska.shareyourspot.android.domain.Comments;
 import de.hska.shareyourspot.android.domain.Parties;
 import de.hska.shareyourspot.android.domain.Party;
 import de.hska.shareyourspot.android.domain.Post;
 import de.hska.shareyourspot.android.domain.Posts;
 import de.hska.shareyourspot.android.domain.User;
 import de.hska.shareyourspot.android.domain.Users;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap.CompressFormat;
 import android.os.StrictMode;
 import android.provider.Settings;
 
 abstract class HttpHandler {
 
 	public final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
 
 	public HttpHandler() {
 		if (android.os.Build.VERSION.SDK_INT > 9) {
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 					.permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 		}
 	}
 
 	public static enum DomainType {
 		User, Party, Post, Comment, Users, Parties, Posts, Comments
 	};
 
 	protected Object get(String uri, DomainType type) {
 		Object obj = null;
 		try {
 			URL url = new URL(uri);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			conn.setRequestProperty("Accept", "application/xml");
 			if (conn.getResponseCode() != 200) {
 				throw new RuntimeException("Failed : HTTP error code : "
 						+ conn.getResponseCode());
 			}
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					(conn.getInputStream())));
			String output;
			System.out.println("Output from Server .... \n");
			String xmlString = "";
			while ((output = br.readLine()) != null) {
				if (output != null) {
					xmlString += output;
					System.out.println(output);
				}
			}
			obj = deserialize(xmlString, type);
 
 			conn.disconnect();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return obj;
 	}
 
 	private int getResponseCode(String uri) {
 		int responseCode = -1;
 		try {
 			URL url = new URL(uri);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			responseCode = conn.getResponseCode();
 			conn.disconnect();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return responseCode;
 	}
 
 	protected int post(String uri, String input) {
 		int statusCode = 0;
 		try {
 			input = xmlHeader + input;
 			URL url = new URL(uri);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setDoOutput(true);
 			conn.setRequestMethod("POST");
 			conn.setRequestProperty("Content-Type", "application/xml");
 			OutputStream os = conn.getOutputStream();
 			if (input != null)
 				os.write(input.getBytes());
 			os.flush();
 			statusCode = conn.getResponseCode();
 			try {
 				if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED
 						&& conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
 						&& conn.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND) {
 
 					// TODO ERRORHANDLING
 					return HttpURLConnection.HTTP_CLIENT_TIMEOUT;
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					(conn.getInputStream())));
 			String output;
 			System.out.println("Output from Server .... \n");
			String xmlString = "";
 			while ((output = br.readLine()) != null) {
				if (output != null) {
					xmlString += output;
				}
 			}
			System.out.println(xmlString);
 			statusCode = conn.getResponseCode();
 			conn.disconnect();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return statusCode;
 	}
 
 	protected Object post(String uri, String input, DomainType type) {
 		int statusCode = 0;
 
 		Object object = null;
 
 		try {
 			input = xmlHeader + input;
 			URL url = new URL(uri);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setDoOutput(true);
 			conn.setRequestMethod("POST");
 			conn.setRequestProperty("Content-Type", "application/xml");
 			OutputStream os = conn.getOutputStream();
 			if (input != null)
 				os.write(input.getBytes());
 			os.flush();
 			statusCode = conn.getResponseCode();
 			try {
 				if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED
 						&& conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
 						&& conn.getResponseCode() != HttpURLConnection.HTTP_OK
 						&& conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
 					throw new RuntimeException("Failed : HTTP error code : "
 							+ conn.getResponseCode());
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					(conn.getInputStream())));
 			String output;
 			System.out.println("Output from Server .... \n");
 			String xmlString = "";
 			while ((output = br.readLine()) != null) {
 				if (output != null) {
 					xmlString += output;
 					System.out.println(output);
 				}
 
 			}
 
 			object = deserialize(xmlString, type);
 			statusCode = conn.getResponseCode();
 			System.out.println(statusCode);
 			conn.disconnect();
 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return object;
 	}
 
 	protected int delete(String uri) {
 		int statusCode = 0;
 		try {
 			URL url = new URL(uri);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("DELETE");
 			statusCode = conn.getResponseCode();
 			conn.disconnect();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return statusCode;
 	}
 
 	protected String serialize(Object obj) {
 		Serializer serializer = new Persister();
 		String output = null;
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		try {
 			serializer.write(obj, os);
 			output = new String(os.toByteArray());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return output;
 	}
 
 	private Object deserialize(String xmlStr, DomainType type) {
 		Serializer serializer = new Persister();
 		try {
 			switch (type) {
 			case User:
 				return serializer.read(User.class, xmlStr);
 			case Users:
 				return serializer.read(Users.class, xmlStr);
 			case Post:
 				return serializer.read(Post.class, xmlStr);
 			case Posts:
 				return serializer.read(Posts.class, xmlStr);
 			case Party:
 				return serializer.read(Party.class, xmlStr);
 			case Parties:
 				return serializer.read(Parties.class, xmlStr);
 			case Comment:
 				return serializer.read(Comment.class, xmlStr);
 			case Comments:
 				return serializer.read(Comments.class, xmlStr);
 			default:
 				return null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Problem with SimpleXML");
 		}
 		return null;
 	}
     public boolean executeMultipartPost(String url, String filename, byte[] data) throws Exception {
     	    // TODO Auto-generated method stub
     	    try {
     	        HttpClient httpClient = new DefaultHttpClient();
     	        HttpPost postRequest = 
     	            new HttpPost(url);
     	        ByteArrayBody bab = new ByteArrayBody(data, filename);
     	        MultipartEntity reqEntity = new MultipartEntity(
     	                HttpMultipartMode.BROWSER_COMPATIBLE);
     	        reqEntity.addPart("image", bab);
     	        postRequest.setEntity(reqEntity);
     	        HttpResponse response = httpClient.execute(postRequest);
     	        System.out.println("Status is "+response.getStatusLine());
     	        BufferedReader reader = new BufferedReader(new InputStreamReader(
     	                response.getEntity().getContent(), "UTF-8"));
     	        String sResponse;
     	        StringBuilder s = new StringBuilder();
     	        while ((sResponse = reader.readLine()) != null) {
     	            s = s.append(sResponse);
     	        }
     	        System.out.println("Response: " + s);
     	    } catch (Exception e) {
     	        // handle exception here
     	        e.printStackTrace();
     	        return false;
     	    }
     	    return true;
     	}            
     }
 

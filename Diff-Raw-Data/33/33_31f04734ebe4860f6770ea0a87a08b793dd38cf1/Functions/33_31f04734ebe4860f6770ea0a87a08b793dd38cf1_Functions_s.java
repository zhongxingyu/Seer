 package main;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
import java.util.LinkedList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 public class Functions {
 	private static final String[] ALPHA_ARRAY = { "a", "b", "c", "d", "e", "f",
 			"g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
 			"t", "u", "v", "w", "x", "y", "z" };
 
 	public static String Get(ArrayList<NameValuePair> headerList, String URL) {
 		HttpClient hc = HttpClientBuilder.create().build();
 		HttpGet httpGet = new HttpGet(URL);
 
 		headerList.add(new BasicNameValuePair("Client", "true"));
 
 		for (NameValuePair nvp : headerList) {
 			httpGet.setHeader(nvp.getName(), nvp.getValue());
 		}
 
 		try {
 			HttpResponse response = hc.execute(httpGet);
 			String res = EntityUtils.toString(response.getEntity());
 			return res;
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static String Post(ArrayList<NameValuePair> postParameters,
 			String URL) {
 		HttpClient hc = HttpClientBuilder.create().build();
 		HttpPost httpPost = new HttpPost(URL);
 
 		postParameters.add(new BasicNameValuePair("Client", "true"));
 
 		HttpResponse response = null;
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
 			response = hc.execute(httpPost);
 			String res = EntityUtils.toString(response.getEntity());
 			return res;
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	// GETs, but returns the Response instead
 	public static HttpResponse GetResponse(ArrayList<NameValuePair> headerList,
 			String URL) {
 		HttpClient hc = HttpClientBuilder.create().build();
 		HttpGet httpGet = new HttpGet(URL);
 
 		headerList.add(new BasicNameValuePair("Client", "true"));
 
 		for (NameValuePair nvp : headerList) {
 			httpGet.setHeader(nvp.getName(), nvp.getValue());
 		}
 
 		try {
 			HttpResponse response = hc.execute(httpGet);
 			return response;
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static String getTime(DateFormat df) {
 		Date now = Calendar.getInstance().getTime();
 		return df.format(now);
 	}
 
 
 	public static String encrypt(String tempMessage, int[] keyList) {
 		System.out.println("Encrypting: " + tempMessage);
 		String[] strArray = split(tempMessage);
 
 		String cipherText = "";
 
 		for (int i = 0; i < strArray.length; i++) {
 			int index = getLoc(strArray[i]) + keyList[i];
 			System.out.println(index);
 			while (index > 25) {
 				index -= 26;
 			}
 			cipherText += ALPHA_ARRAY[index];
 		}
 
 		return cipherText;
 	}
 
 	
 	public static String decrypt(String tempMessage, int[] keyList) {
 		System.out.println("Decrypting: " + tempMessage);
 		String[] strArray = split(tempMessage);
 		
 		String cipherText = "";
 
 		for (int i = 0; i < strArray.length; i++) {
 			int index = getLoc(strArray[i]) - keyList[i];
 			index = index % 26;
 
 			while (index < 0) {
 				index += 26;
 			}
 			cipherText += ALPHA_ARRAY[index];
 		}
 
 		return cipherText;
 	}
 	
 	
 	
 
 	// Returns the location of the given letter (0-25)
 	public static int getLoc(String s) {
 		int index = 0;
 		for (String str : ALPHA_ARRAY) {
 			if (str.equalsIgnoreCase(s)) {
 				return index;
 			}
 			index++;
 		}
 		return -1;
 		// Invalid character (Used in clearIllegal())
 	}
 	
 	
 	//This is to be used as a replacement to String.split("") which has a whitespace at [0]
 	public static String[] split(String s){
 		char[] c = s.toCharArray();
 		String[] str = new String[s.length()];
 		
 		for (int i = 0; i<s.length(); i ++){
 			str[i] = String.valueOf(c[i]);
 		}
 		return str;
 	}
 
 	//Fixes illegal characters (only lets alphabetical letters through)
 	public static String clearIllegal(String s) {
 		String str = "";
 		for (char c: s.toCharArray()){
 			if (getLoc(String.valueOf(c)) != -1){
 				str+=(c);
 			}
 		}
 		return str;
 	}
 
 }

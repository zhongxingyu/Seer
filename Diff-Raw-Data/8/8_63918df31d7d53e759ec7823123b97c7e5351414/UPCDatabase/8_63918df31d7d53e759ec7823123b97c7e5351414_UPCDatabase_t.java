 package Shopaholix.database;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 
 public class UPCDatabase {
 	
 
 	public static Item lookupByUPC(String upc) {
 		try {
		java.net.URL url = new java.net.URL("http://www.searchupc.com/handlers/upcsearch.ashx?request_type=1&access_token=567CFFB1-26F8-4BD6-8C29-935D9324B425&upc="+upc);
 		 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
 	     InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 	     String item = readStream(in);
 	     urlConnection.disconnect();
 	     return new Item(upc, item, new ItemRatings());
 		}
 		catch (Exception e) {
 			return null;
 		}
 	}
 	
 	private static String readStream(InputStream in) {
  		InputStreamReader is = new InputStreamReader(in);
  		BufferedReader br = new BufferedReader(is);
  		String data = "";
  		try {
 			data = br.readLine();
 			data = br.readLine();
 		} catch (IOException e) {
 			return null;
 		}
 		return toItem(data);
 	}
 	
 	private static String toItem(String data) {
		String pattern = "\"(.+)\",\"(.+)\",\"(.+)\",\"(.+)\",\"(.+)\",\"(.+)\"";
		if (data == null) {
			return null;
		}
 		if (data.matches(pattern)) {
 			return data.replace(pattern, "$1");
 		}
 		else {
 			return null;
 		}
 	}
 }

 package eu.trentorise.smartcampus.protocolcarrier.common;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 public class Utils {
 
 	public static byte[] responseContentToByteArray(InputStream is)
 			throws IOException {
 		if (is == null) {
 			return null;
 		}
 
 		byte[] buffer = new byte[1024];
 		BufferedInputStream bis = new BufferedInputStream(is);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
		while (bis.read(buffer) != -1) {
			baos.write(buffer);
 		}
 
 		byte[] result = baos.toByteArray();
 		return result;
 	}
 
 	public static String responseContentToString(InputStream is) {
 		if (is == null) {
 			return null;
 		}
 
 		String line = "";
 		StringBuilder total = new StringBuilder();
 
 		// Wrap a BufferedReader around the InputStream
 		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
 
 		// Read response until the end
 		try {
 			while ((line = rd.readLine()) != null) {
 				total.append(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		// Return full string
 		return total.toString();
 	}
 
 	public static boolean isOnline(Context context) {
 		ConnectivityManager conMgr = (ConnectivityManager) context
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo i = conMgr.getActiveNetworkInfo();
 		if (i == null) {
 			return false;
 		}
 		if (!i.isConnected()) {
 			return false;
 		}
 		if (!i.isAvailable()) {
 			return false;
 		}
 		return true;
 	}
 
 }

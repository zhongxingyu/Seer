 package com.wap.battle.client;
 
 import android.os.Environment;
 import android.util.Log;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URL;
import java.util.UUID;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  * Cake is becoming godly... Oops.
  *
  * @see http://book.cakephp.org/2.0/en/core-libraries/global-constants-and-functions.html
  */
 public abstract class Cake {
 	public static final String BASE = "http://ec2-54-251-197-27.ap-southeast-1.compute.amazonaws.com"; // Excludes the trailing slash.
 	public static final String DS = "/";
 	public static final String LOG_TAG = Cake.class.getName();
 	
 	/**
 	 * Save remote (binary) file to external storage.
 	 * 
 	 * @param fullPath Absolute server path to the file.
 	 * @param rawName The file name without the extension.
 	 * @return
 	 */
 	public static String getFile(String fullPath, String rawName) {
 		String localDir = Environment.getExternalStorageDirectory().getPath() + Cake.DS + "MonsterBattleClient" + Cake.DS;
 		String[] parts = fullPath.split("\\.(?=[^\\.]+$)");
		String localFile = localDir + UUID.randomUUID().toString() + "." + parts[1];
 		try {
 			File folder = new File(localDir);
 			if (!folder.exists()) {
 				folder.mkdir();
 				Log.i(LOG_TAG, "Made directory " + folder.getAbsolutePath());
 			}
 			File file = new File(localFile);
 			if (!file.exists()) {
 				URL url = new URL(fullPath);
 				InputStream in = new BufferedInputStream(url.openStream());
 				OutputStream out = new FileOutputStream(localFile);
 				byte data[] = new byte[1024];
 				int count;
 				while ((count = in.read(data)) != -1) {
 					out.write(data, 0, count);
 				}
 				out.flush();
 				out.close();
 				in.close();
 				Log.i(LOG_TAG, "Wrote file " + localFile);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return localFile;
 	}
 	
 	/**
 	 * Read remote (text) file.
 	 * 
 	 * @param fullPath
 	 * @return
 	 */
 	public static String getJSON(String fullPath) {
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet req = new HttpGet(fullPath);
 		try {
 	    	HttpResponse res = client.execute(req);
 	    	StatusLine line = res.getStatusLine();
 	    	int code = line.getStatusCode();
 	    	if (code == HttpStatus.SC_OK) {
 	    		HttpEntity entity = res.getEntity();
 	    		InputStream content = entity.getContent();
 	    		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 	    		String tmp;
 	    		while ((tmp = reader.readLine()) != null) {
 	    			builder.append(tmp);
 	    		}
 	    	} else {
 	    		Log.e(LOG_TAG, "Received " + String.valueOf(code) + " for " + fullPath); 
 	    	}
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }
 		return builder.toString();
 	}
 }

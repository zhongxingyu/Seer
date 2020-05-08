 package org.canthack.tris.pipurr.client;
 
 import java.io.IOException;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.os.AsyncTask;
 
 public class GetUrlTask extends AsyncTask<String, Integer, Void> {
 
 	protected Void doInBackground(String... urls) {
 		
 		HttpClient httpClient = CustomHTTPClient.getHttpClient();
 		try {
 			HttpGet request = new HttpGet(urls[0]);
 			HttpParams params = new BasicHttpParams();
 			HttpConnectionParams.setSoTimeout(params, 60000);   // 1 minute
 			request.setParams(params);
 
			/*HttpResponse response =*/ httpClient.execute(request);	
 			
 		} 
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 
 }

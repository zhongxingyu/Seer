 package org.redbus;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.apache.http.protocol.HTTP;
 import android.os.AsyncTask;
 
 public class BusDataHelper {
 	
 	public static void GetBusTimesAsync(long stopCode)
 	{
 		StringBuilder url = new StringBuilder("http://www.mybustracker.co.uk/getBusStopDepartures.php?" +
 											  "refreshCount=0&" +
 											  "clientType=b&" +
 											  "busStopDay=0&" +
 											  "busStopService=0&" +
 											  "numberOfPassage=2&" +
 											  "busStopTime&" +
 											  "busStopDestination=0&");
 		url.append("busStopCode=");
 		url.append(stopCode);
 		try {
 			new AsyncHttpRequestTask().execute(new BusDataRequest(new URL(url.toString()), BusDataRequest.REQ_BUSTIMES));
 		} catch (MalformedURLException ex) {
			// FIXME: log it as programming error
 		}
 	}
 	
 	private static void GetBusTimesResponse(BusDataRequest request)
 	{
 		int i  = 1;
 		i = i + 20;
 		
 		// FIXME
 	}
 
 	private static class AsyncHttpRequestTask extends AsyncTask<BusDataRequest, Integer, BusDataRequest> {
 		
 		protected BusDataRequest doInBackground(BusDataRequest... params) {
 			BusDataRequest bdr = params[0];
 			
 			InputStreamReader reader = null;
 			try {
 				// make the request and check the response code
 				HttpURLConnection connection = (HttpURLConnection) bdr.url.openConnection();
 				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
 					bdr.responseCode = connection.getResponseCode();
 					return bdr;
 				}
 				
 				// figure out the content encoding
 				String charset = connection.getContentEncoding();
 				if (charset == null)
 					charset = HTTP.DEFAULT_CONTENT_CHARSET;
 				
 				// read the request data
 				reader = new InputStreamReader(connection.getInputStream(), charset);
 				StringBuilder result = new StringBuilder();
 				char[] buf= new char[1024];
 				while(true) {
 					int len = reader.read(buf);
 					if (len < 0)
 						break;
 					result.append(buf, 0, len);
 				}
 				bdr.content = result.toString();
 			} catch (Exception ex) {
 				bdr.exception = ex;
 			} finally {
 				if (reader != null)
 					try {
 						reader.close();
 					} catch (IOException e) {
 					}
 			}
 			
 			return bdr;
 		}
 
 		protected void onPostExecute(BusDataRequest request) {
 			switch(request.requestType) {
 			case BusDataRequest.REQ_BUSTIMES:
 				BusDataHelper.GetBusTimesResponse(request);			
 			}
 		}
 	}
 	
 	private static class BusDataRequest {
 		
 		public static final int REQ_BUSTIMES = 0;
 		
 		public BusDataRequest(URL url, int requestType)
 		{
 			this.url = url;
 			this.requestType = requestType;
 		}
 		
 		public URL url;
 		public int responseCode = -1;
 		public String content = null;
 		public Exception exception = null;
 		public int requestType;
 	}
 }

 package f.me.ramc.gps;
 
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Looper;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import f.me.ramc.AdditionalKeyStoresSSLSocketFactory;
 import f.me.ramc.LogActivity;
 import f.me.ramc.R;
 
 public class LocationSender {
 	private static final String TAG = LocationSender.class.getSimpleName();
 	
 	private boolean isFree;
 	private String lastSuccessfulRequest;
 	private Handler handler;
 	
 	private ThreadSafeClientConnManager manager;
 	private HttpParams params;
 	private Context context;
 	private KeyStore keystore;
 	private SSLSocketFactory sslSocketFactory;
 	private SchemeRegistry registry;
 	
 	private int mFailsCounter = 0;
     private static final int MAX_FAILS = 5;
 	
 	public LocationSender(Context context) {
 		this.context = context;
 		isFree = true;
 		handler = new Handler(Looper.getMainLooper());
 		lastSuccessfulRequest = context.getString(R.string.connection_not_established);
 		
 		// Create a KeyStore containing our trusted CAs
 		try {
 			keystore = KeyStore.getInstance("PKCS12");
 			keystore.load(context.getResources().openRawResource(R.raw.keystore), "".toCharArray());
 			
 			sslSocketFactory = new AdditionalKeyStoresSSLSocketFactory(keystore);
 			// use this for test servers
 			//sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
 			params = new BasicHttpParams();
 	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 	        HttpProtocolParams.setUseExpectContinue(params, true);
 			
 	        // Set the timeout in milliseconds until a connection is established.
 	        // The default value is zero, that means the timeout is not used.
 	        HttpConnectionParams.setConnectionTimeout(params, LocationUtils.CONNECTION_TIMEOUT_IN_MILLISECONDS);
 	        // Set the default socket timeout (SO_TIMEOUT) 
 	        // in milliseconds which is the timeout for waiting for data.
 	        HttpConnectionParams.setSoTimeout(params, LocationUtils.SO_TIMEOUT_IN_MILLISECONDS);
 	        
 	        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
 			String userAgent = "ramc-par" + "/" + pInfo.versionName +
 								" (rv:" + String.valueOf(pInfo.versionCode) + "; " + "Android)";
 	        HttpProtocolParams.setUserAgent(params, userAgent);
 	        
 	        registry = new SchemeRegistry();
 	        int httpPort = Integer.parseInt(context.getResources().getString(R.string.ramc_http_port));
 	        int sslPort = Integer.parseInt(context.getResources().getString(R.string.ramc_ssl_port));
 	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), httpPort));
 	        registry.register(new Scheme("https", sslSocketFactory, sslPort));
 	        
 			manager = new ThreadSafeClientConnManager(params, registry);
 	        
 		} catch (Exception e) {
 			Log.d(TAG, e.getLocalizedMessage());
 		}
 		
 	}
 	
 	public void setFree(boolean isFree) {
 		this.isFree = isFree;
 	}
 	
 	public String getLastSuccessfulRequest() {
 		return lastSuccessfulRequest;
 	}
 	
 	public void send(final Location location) {
 		handler.post(new Runnable() {
 			public void run() {
 				new SendToRAMC().execute(location);
 			}
 		});
 	}
 	
 	private class SendToRAMC extends AsyncTask<Location, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(Location... locs) {
 			boolean fails = true;
 			String responseString = "";
 			Location loc = locs[0];
			
 			try {
 				DefaultHttpClient httpclient = new DefaultHttpClient(manager, params);
 				
 				String partnerId = context.getResources().getString(R.string.partner_id);
 		    	String url = context.getResources().getString(R.string.ramc_url) + partnerId;
 				HttpPut put = new HttpPut(url);
 
 				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 		        nameValuePairs.add(new BasicNameValuePair(
 		        		"lon",String.format(Locale.US, "%.7f", loc.getLongitude())));
 		        nameValuePairs.add(new BasicNameValuePair(
 		        		"lat",String.format(Locale.US, "%.7f", loc.getLatitude())));
 		        nameValuePairs.add(new BasicNameValuePair(
 		        		"isFree",String.valueOf(isFree)));
 	        	put.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        	
 	            HttpResponse httpresponse = httpclient.execute(put);
 	            responseString = httpresponse.getLastHeader("Date").getValue() + "\n" + httpresponse.getStatusLine().toString();
 	            // 200 OK (HTTP/1.0 - RFC 1945)
 	            if (httpresponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 	            	fails = false;
 	            	lastSuccessfulRequest = responseString;
 	            } else {
 	            	fails = true;
 	            }
 	            
 	            httpresponse.getEntity().getContent().close();
 			} catch (Exception e) {
 				responseString = e.getLocalizedMessage();
 				Log.d(TAG, responseString);
 			} finally {
 				broadcastLocationUpdateState(responseString);
 			}
 			return fails;
 		}
     	
 		@Override
 		protected void onPostExecute(Boolean fails) {
 			if (fails) {
 				mFailsCounter++;
 				if (mFailsCounter > MAX_FAILS) {
 					notificateUserAboutProblems();
 					// clear counter
                     mFailsCounter = 0;
 				}
 			} else {
 				// request succeeded clear counter
                 mFailsCounter = 0;
 			}
 		}
     }
 	
 	private void broadcastLocationUpdateState(String responseString) {
 		Intent intent = new Intent();
 		intent.setAction(GPSService.BROADCAST_LAST_LOCATION_UPDATE_INTENT);
 		intent.putExtra(GPSService.LOCATION_UPDATE_STATE, responseString);
 		context.sendBroadcast(intent);
 	}
 	
 	private void notificateUserAboutProblems() {
 		Intent notificationIntent = new Intent(context, LogActivity.class);
 		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
 				.setContentIntent(pendingIntent)
 				.setContentText(context.getString(R.string.network_error))
 				.setContentTitle(context.getString(R.string.app_name))
 				.setSmallIcon(R.drawable.ic_notify)
 				.setWhen(System.currentTimeMillis())
 				.setAutoCancel(true)
 				.setDefaults(Notification.DEFAULT_SOUND);
 		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
         nm.notify(0, builder.build());
 	}
 }

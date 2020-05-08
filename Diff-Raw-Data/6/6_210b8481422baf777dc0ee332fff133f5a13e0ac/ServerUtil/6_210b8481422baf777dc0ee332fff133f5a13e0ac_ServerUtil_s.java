 package org.financetool.financetooltracker;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.net.URL;
 import java.security.KeyStore;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.Certificate;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.android.gms.auth.GoogleAuthException;
 import com.google.android.gms.auth.GoogleAuthUtil;
 import com.google.android.gms.auth.UserRecoverableNotifiedException;
 
 import android.content.Context;
 import android.location.Location;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class ServerUtil {
 	public static String TAG = MainActivity.TAG;
 		
 	// The StartCom certificate isn't supported on older android versions
 	private static final String[] CUSTOM_CA_ASSET_PATHS 
 		= new String[] {"startcom.ca.crt", "startcom.sub.class1.server.ca.crt"}; 
 	
 	private PreferenceUtil prefs; 
 	private String apiBaseURL;
 	private Context context;
 	private JSONUtil jsonUtil;
 	private SSLSocketFactory sslSocketFactory;
 	
 	public ServerUtil(Context context) {
 		prefs = new PreferenceUtil(context);
 		apiBaseURL = context.getString(R.string.api_base_url);
 		jsonUtil= new JSONUtil();		
 		this.context = context;
 	}
 	
 	public boolean uploadLocations(Collection<Location> locs) {
 		try {
 			if (locs.size() > 0) {
 				JSONObject arg = new JSONObject();			
 					arg.put("locations", jsonUtil.getLocationsAsJSON(locs));			
 				JSONObject json = callAPI("locations/bulk_create", arg);
 				if (json != null) {
 					return json.getLong("num_created_locations") == locs.size();					
 				}
 			}
 		} catch (JSONException e) {	
 			Log.e(TAG, e.toString(), e);
 		}
 		return false;
 	}
 	
 	private JSONObject callAPI(String cmd, JSONObject arg) {
 		return callAPI(cmd, arg, true);
 	}
 	
 	private SSLSocketFactory getSSLSocketFactory() {		
 		if (sslSocketFactory == null) {
 			sslSocketFactory = sslSocketFactoryWithCustomCA(CUSTOM_CA_ASSET_PATHS);
 		}
 		return sslSocketFactory;
 	}
 	
 	private Certificate getCertForAssetPath(String assetPath) 
 			throws CertificateException, IOException {
 		CertificateFactory cf = CertificateFactory.getInstance("X.509");
 		InputStream caInput = new BufferedInputStream(
 				context.getAssets().open(assetPath));
 		Certificate ca = cf.generateCertificate(caInput);
 		caInput.close();
 		return ca;
 	}
 	
 	// From: http://developer.android.com/training/articles/security-ssl.html#UnknownCa
 	// Useful link: 
 	private SSLSocketFactory sslSocketFactoryWithCustomCA(String[] certPaths) {				
 		SSLSocketFactory socketFactory = null;		
 		try {						
 			// Create a KeyStore containing our trusted CAs
 			String keyStoreType = KeyStore.getDefaultType();
 			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
 			keyStore.load(null, null);
 			for (int i = 0; i < certPaths.length; i++) {
 				keyStore.setCertificateEntry("ca", getCertForAssetPath(certPaths[i]));
 			}
 
 			// Create a TrustManager that trusts the CAs in our KeyStore
 			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
 			TrustManagerFactory tmfCustom = TrustManagerFactory.getInstance(tmfAlgorithm);
 			tmfCustom.init(keyStore);												
 
 			// Create an SSLContext that uses our TrustManager
 			SSLContext sslContext = SSLContext.getInstance("TLS");
 			sslContext.init(null, tmfCustom.getTrustManagers(), null);
 
 			socketFactory = sslContext.getSocketFactory();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	    return socketFactory;		
 	}
 	
 	private JSONObject callAPI(String cmd, JSONObject arg, boolean retryIfUnauthorized) {
 		if (!isNetworkAvailable()) {
 			return null;
 		}
 		if (!prefs.isAuthAccountConnected()) {
 			return null;
 		}
 		
 		String authToken = prefs.getAuthToken();				
 		if (authToken == null) {
 			if (requestNewAuthToken() != null && retryIfUnauthorized) {
 				return callAPI(cmd, arg, false);
 			} else {
 				return null;
 			}
 		}
 										
 		try {													
 			arg.put("google_token", authToken);
 			
 			HttpsURLConnection http = (HttpsURLConnection) 
 					((new URL(apiBaseURL + cmd).openConnection()));
 			http.setDoOutput(true);
 			http.setRequestProperty("Content-Type", "application/json");
 			http.setRequestMethod("POST");
 			http.setInstanceFollowRedirects(false);
			http.setSSLSocketFactory(getSSLSocketFactory());
 			http.connect();				
 			
 			BufferedOutputStream out 
 				= new BufferedOutputStream(http.getOutputStream());
 			out.write(arg.toString().getBytes("UTF8"));
 			out.close();
 			
 			int code = http.getResponseCode();
 			if (code == 200) {				
 				String response = readFullyToString(http.getInputStream());
 				return new JSONObject(response);
 			} else if (code == 401) {
 				if (requestNewAuthToken() != null && retryIfUnauthorized) {
 					return callAPI(cmd, arg, false);
 				}
 			} else {
 				String msg = "Bad server response, code: " + code + 
 						"cmd: " + cmd + 
 						", arg: " + arg.toString();
 				Log.e(TAG, msg);
 			}			
 		} catch (JSONException e) {
 			Log.e(TAG, e.toString(), e);
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, e.toString(), e);
 		} catch (IOException e) {
 			Log.e(TAG, e.toString(), e);
 		}
 		
 		return null;
 	}	
 	
 	// From: http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
 	public static String readFullyToString(InputStream in) throws IOException
 	{
 	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 	    StringBuilder out = new StringBuilder();
 	    String line;
 	    while ((line = reader.readLine()) != null) {
 	        out.append(line);
 	    }
 	    return out.toString();
 	}
 	
 	public boolean isNetworkAvailable() {
 		NetworkInfo netInfo = 
 			((ConnectivityManager) 
 				context.getSystemService(Context.CONNECTIVITY_SERVICE))
 	     	.getActiveNetworkInfo();
 	    return netInfo != null && netInfo.isConnected();
 	}
 	
 	private String requestNewAuthToken() {
 		String token = prefs.getAuthToken();		
 		GoogleAuthUtil.invalidateToken(context, token);
 		token = null;
 		try {
 			token = GoogleAuthUtil.getTokenWithNotification(context, 
 					prefs.getAuthAccount().name, 
 					context.getString(R.string.auth_scope), null);
 		} catch (UserRecoverableNotifiedException e) {
 		    // Notification has already been pushed, continue wo/ token.
 		} catch (IOException e) {
 			 Log.i(TAG, "Transient auth error: " + e.getMessage(), e);
 		} catch (GoogleAuthException e) {
 			Log.e(TAG, "Unrecoverable auth exception: " + e.getMessage(), e);
 		}
 		prefs.setAuthToken(token);
 		return token;
 	}
 }

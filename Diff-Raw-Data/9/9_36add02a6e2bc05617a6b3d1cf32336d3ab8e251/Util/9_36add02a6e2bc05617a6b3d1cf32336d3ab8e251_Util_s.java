 package de.laxu.apps.sharedurllist;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.NotificationCompat.Builder;
 
 public abstract class Util  {
 
 	public static String loadFromURL(Activity activity, String url)
 			throws LoadException {
 				String text="";
 				ConnectivityManager cm = (ConnectivityManager) activity
 						.getSystemService(Context.CONNECTIVITY_SERVICE);
 				NetworkInfo ni = cm.getActiveNetworkInfo();
 				if (ni != null && ni.isConnected()) {
 					if(url == null || url.equals("")){
 						throw new LoadException("no URL given.");
 					}
 					try {
 						HttpClient client = new DefaultHttpClient();
 						HttpResponse response = client.execute(new HttpGet(url));
 						StatusLine statusLine = response.getStatusLine();
 						int statusCode = statusLine.getStatusCode();
 			
 						if (statusCode == 200) {
 							BufferedReader reader = new BufferedReader(
 									new InputStreamReader(response.getEntity()
 											.getContent()));
 							String line;
 							while ((line = reader.readLine()) != null) {
 								text += line;
 							}
 						} else {
 							throw new LoadException("HTTPError " + Integer.toString(statusCode));
 						}
 					} catch (MalformedURLException e) {
 						throw new LoadException("invalid URL");
 					} catch (IOException e) {
						throw new LoadException("network Error");
 					}
 				} else {
 					throw new LoadException("no network");
 				}
 				return text;
 			}
 	public static boolean hasSettingsErrors(MainActivity mainActivity){
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
 		String serverURL = sharedPrefs.getString("pref_serverurl", "");
 		String username = sharedPrefs.getString("pref_username", "");
 		String devicename = sharedPrefs.getString("pref_devicename", "");
 		
 		return (
 			(!serverURL.startsWith("https://") && !serverURL.startsWith("http://"))
 			|| username.equals("")
 			|| devicename.equals("")
 		);
 	}
 	public static String getSettingsErrors(MainActivity mainActivity) {
 		ArrayList<String> errorItems = new ArrayList<String>();
 		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
 		String errorString = "";
 		String serverURL = sharedPrefs.getString("pref_serverurl", "");
 		String username = sharedPrefs.getString("pref_username", "");
 		String devicename = sharedPrefs.getString("pref_devicename", "");
 		
 		if(serverURL.equals("")){
 			errorItems.add("Server-URL");
 		}
 		if(username.equals("")){
 			errorItems.add("Username");
 		}
 		if(devicename.equals("")){
 			errorItems.add("device name");
 		}
		if(errorItems.size() >0)
 			errorString += "No " + buildCommaList(errorItems) +" set.";
		
		if(!serverURL.startsWith("https://") && !serverURL.startsWith("http://")){
 			if(!errorString.equals(""))
 				errorString += "\n";
 			errorString += "Server-URL does not start with \"https://\" or \"http://\".";
 		}
 		return errorString;
 	}
 	private static String buildCommaList(ArrayList<String> errorItems){
 		String commaString="";
 		for(int i=0;i<errorItems.size(); i++){
 			if(i==0){ // first
 				commaString += errorItems.get(i);
 			}else if(i==errorItems.size()-1){ // last
 				commaString += " and " + errorItems.get(i);
 			}else{
 				commaString += ", "+errorItems.get(i);
 			}
 		}
 		return commaString;
 	}
 	private static final int NOTIFICATION_ID = 1;
 	public static void createNotification(Activity activity, String title, String text, int icon, boolean progress){
 		NotificationManager notificationManager = ((NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE));
 		notificationManager.cancelAll();
 		Intent newIntent = new Intent(activity.getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		PendingIntent pIntent = PendingIntent.getActivity(activity, 0, newIntent, 0);
 		Builder builder = new NotificationCompat.Builder(activity);
 		builder
 		.setSmallIcon(icon)
 		.setContentTitle(title)
 		.setContentText(text)
 		.setAutoCancel(true)
 		.setTicker(title+": "+text)
 		.setContentIntent(pIntent)
 		;
 		if(progress){
 			builder.setProgress(1, 0, true);
 		}
 		Notification notification = builder.build();
 		notificationManager.notify(NOTIFICATION_ID, notification);
 	}
 
 	public static void removeNotifications(Activity activity){
 		((NotificationManager)activity.getSystemService(Context.NOTIFICATION_SERVICE))
 		.cancel(NOTIFICATION_ID);
 	}
 	public Util() {
 		super();
 	}
 
 }

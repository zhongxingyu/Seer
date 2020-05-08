 package net.fhtagn.zoobeditor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import net.fhtagn.zoobeditor.browser.DeleteActivity;
 import net.fhtagn.zoobeditor.browser.RateActivity;
 import net.fhtagn.zoobeditor.browser.UploadActivity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
import android.content.ComponentName;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.RatingBar;
 
 public class Common {	
 	static final String TAG = "Common";
 	
 	public static final String LEVELS_DIR_NAME = "zoob_levels";
 	public static final String MY_LEVELS_SUBDIR = "mylevels";
 	public static final String ONLINE_LEVELS_SUBDIR = "community";
 	
 	public static final int dp2pixels (Context ctx, float dp) { 
 		final float scale = ctx.getResources().getDisplayMetrics().density;
 		return (int)(scale*dp);
 	}
 
 	
 	public static Intent playSerie (long id) {
 		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", ContentUris.withAppendedId(Series.CONTENT_URI, id));
		i.setClassName("net.fhtagn.zoobgame", "net.fhtagn.zoobgame.Zoob");
 		return i;
 	}
 	
 	public static long extractId (Uri serieUri) {
 		return Long.parseLong(serieUri.getLastPathSegment());
 	}
 	
 	public static Intent playSerie (long serieId, int level) {
 		Uri.Builder builder = new Uri.Builder();
 		builder.scheme("content");
 		builder.authority("net.fhtagn.zoobgame");
 		builder.path("/series/"+serieId);
 		builder.appendQueryParameter("startlevel", ""+level);
 		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", builder.build());
		i.setClassName("net.fhtagn.zoobgame", "net.fhtagn.zoobgame.Zoob");
 		return i;
 	}
 	
 	public static Intent playLevel (String json) {
 		Intent i = new Intent("net.fhtagn.zoobgame.PLAY", Uri.parse("content://net.fhtagn.zoobgame.SerieContentProvider/level"));
 		i.putExtra("json", json);
		i.setClassName("net.fhtagn.zoobgame", "net.fhtagn.zoobgame.Zoob");
 		return i;
 	}
 	
 	public static String removeSpecialCharacters(String s) {
 	  return s.replaceAll("\\W", "");
 	}
 
 	public static String convertStreamToString(InputStream is) {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder sb = new StringBuilder();
 
 		String line = null;
 		try {
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				is.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return sb.toString();
 	}
 	
 	private static final SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	public static Date dateFromDB(String date) {
 		try {
 	    return iso8601Format.parse(date);
     } catch (ParseException e) {
 	    e.printStackTrace();
 	    return new Date();
     }
 	}
 	
 	/*
 	*@return boolean return true if the application can access the internet
 	*/
 	public static boolean hasInternet(Context ctx){
 		NetworkInfo info=((ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
 		if(info==null || !info.isConnected()){
 			return false;
 		}
 		if(info.isRoaming()){
 			//here is the roaming option you can change it if you want to disable internet while roaming, just return false
 			//FIXME: add a preference for that
 			return true;
 		}
 		return true;
 	}
 	
 	//Run a GET query at the specified url and returns the content. returns null if status code != 200 or on error
 	public static String urlQuery (HttpClient httpClient, String url) {
 		HttpGet httpGet = new HttpGet(url);
 		try {
 			HttpResponse response = httpClient.execute(httpGet);
 			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				HttpEntity entity = response.getEntity();
 				if (entity == null) {
 					Log.e(TAG, "urlQuery() : Entity = null");
 					return null; 
 				}
 				InputStream instream = entity.getContent();
 				return Common.convertStreamToString(instream);
 			} else {
 				Log.e(TAG, "Error during urlQuery : " + response.getStatusLine().getStatusCode());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	
 	//These two functions can be used to create and handle the common options
 	//menu that should be available on all menu
 	public static void createCommonOptionsMenu (Activity activity, Menu menu) {
 		MenuInflater inflater = activity.getMenuInflater();
 		inflater.inflate(R.menu.base_menu, menu);
 	}
 	
 	public static boolean commonOnOptionsItemSelected (Activity activity, MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.prefs:
 				Intent i = new Intent(activity, Preferences.class);
 				activity.startActivity(i);
 				return true;
 			case R.id.help:
 				//FIXME: show help
 				return true;
 		}
 		return false;
 	}
 	
 	public static Dialog createConfirmDeleteDialog (Activity activity, int msgID, DialogInterface.OnClickListener listener) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		builder.setTitle(R.string.delete_dlg_title)
 					 .setMessage(msgID)
 					  .setCancelable(true)
 					  .setPositiveButton(R.string.ok, listener)
 					  .setNegativeButton(R.string.cancel,
 					    new DialogInterface.OnClickListener() {
 						    public void onClick(DialogInterface dialog, int id) {
 							    dialog.dismiss();
 						    }
 						  });
 		return builder.create();
 	}
 	
 	public static Dialog createRateDialog (final Activity activity, final long serieID) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 		final RatingBar ratingBar = new RatingBar(activity);
 		ratingBar.setNumStars(5);
 		builder.setTitle(R.string.dlg_rate_title)
 					 .setCancelable(true)
 					 .setView(ratingBar)
 					 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 						 public void onClick (DialogInterface dialog, int id) {
 							 dialog.dismiss();
 							 Intent i = new Intent(activity.getApplicationContext(), RateActivity.class);
 							 i.putExtra("community_id", serieID);
 							 i.putExtra("rating", ratingBar.getRating());
 							 activity.startActivityForResult(i, EditorConstants.REQUEST_RATE);
 						 }
 					 });
 		return builder.create();
 	}
 	
 	public static final class Level {
 		public static final void play (Activity sourceActivity, long serieID, int level, int requestCode) {
 			Intent i = Common.playSerie(serieID, level);
 	    sourceActivity.startActivityForResult(i, requestCode);
 		}
 	}
 	
 	//Regroup common actions on series
 	public static final class Serie {
 		public static final void upload (Activity sourceActivity, long serieID) {
 			Intent i = new Intent(sourceActivity.getApplicationContext(), UploadActivity.class);
 			i.putExtra("id", serieID);
 			sourceActivity.startActivityForResult(i, EditorConstants.REQUEST_UPLOAD);
 		}
 		
 		public static final void play (Activity sourceActivity, long serieID) {
 			Intent i = Common.playSerie(serieID);
 	    sourceActivity.startActivity(i);
 		}
 		
 		public static final void deleteSerie (Activity sourceActivity, long serieID) {
 			Intent i = new Intent(sourceActivity.getApplicationContext(), DeleteActivity.class);
 			i.putExtra("id", serieID);
 			sourceActivity.startActivityForResult(i, EditorConstants.REQUEST_DELETE);
 		}
 		
 		public static final void rateSerie (Activity sourceActivity, long serieID, int rating) {
 			
 			Intent i = new Intent(sourceActivity.getApplicationContext(), RateActivity.class);
 			i.putExtra("community_id", serieID);
 			i.putExtra("rating", rating);
 			sourceActivity.startActivityForResult(i, EditorConstants.REQUEST_RATE);
 		}
 	}
 }

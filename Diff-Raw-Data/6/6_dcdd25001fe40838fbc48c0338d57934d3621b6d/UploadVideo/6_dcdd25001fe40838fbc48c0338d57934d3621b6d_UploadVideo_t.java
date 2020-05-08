 package com.androidmontreal.weddingvideoguestbook;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 public class UploadVideo extends IntentService {
 	public String TAG = PrivateConstants.TAG;
 
 	public UploadVideo() {
 		super("UploadVideo");
 	}
 
 	@Override
 	protected void onHandleIntent(Intent uploadIntent) {
 		int notificationId = (int) System.currentTimeMillis();
 		String uploadStatusMessage = getString(R.string.preparing_upload);
 
 		if (uploadIntent.getExtras() == null) {
 			return;
 		}
 
 		// Get intent extras here
 		String fileName = uploadIntent.getExtras().getString(
 				PrivateConstants.EXTRA_FILENAME);
 		String dataFile = uploadIntent.getExtras().getString(
 				PrivateConstants.EXTRA_FILEPATH);
 
 		// Check for well-formed extras
 		if (fileName == null || dataFile == null) {
 			Log.e(TAG, "Invalid call to UploadVideo intent service.");
 			return;
 		}
		
		// Make sure the file exists
		if(!new File(dataFile).exists()){
			Log.e(TAG, "Invalid call to UploadVideo intent service, file does not exist.");
			return;
		}
 
 		// tryUploadAgain
 		Intent tryUploadAgain = new Intent(this, UploadVideo.class);
 		tryUploadAgain.putExtra(PrivateConstants.EXTRA_FILENAME, fileName);
 		tryUploadAgain.putExtra(PrivateConstants.EXTRA_FILEPATH, dataFile);
 
 		PendingIntent pIntent = PendingIntent.getService(this, 323813,
 				tryUploadAgain, Intent.FLAG_ACTIVITY_NO_HISTORY);
 
 		// NOTIFICATION
 		RemoteViews notificationView = new RemoteViews(getPackageName(),
 				R.layout.notification);
 		notificationView.setTextViewText(R.id.notification_text, getResources()
 				.getString(R.string.preparing_upload));
 		notificationView.setTextViewText(R.id.notification_title,
 				getResources().getString(R.string.video_upload));
 		Notification noti = new NotificationCompat.Builder(this)
 				.setTicker(uploadStatusMessage).setContent(notificationView)
 				.setSmallIcon(R.drawable.ic_lingsync).setContentIntent(pIntent)
 				.build();
 		noti.flags = Notification.FLAG_AUTO_CANCEL;
 		notifyUser(uploadStatusMessage, noti, notificationId, false);
 
 		/* Actually uploads the video */
 //		HttpClient httpClient = new SecureHttpClient(getApplicationContext());
 		HttpClient httpClient = new DefaultHttpClient();
 
 		HttpContext localContext = new BasicHttpContext();
 		String token = PrivateConstants.SERVER_TOKEN;
 		String url = PrivateConstants.SERVER_URL;
 		HttpPost httpPost = new HttpPost(url);
 
 		MultipartEntity entity = new MultipartEntity(
 				HttpMultipartMode.BROWSER_COMPATIBLE, null,
 				Charset.forName("UTF-8"));
 
 		try {
 			entity.addPart(
 					"token",
 					new StringBody(token, "text/plain", Charset
 							.forName("UTF-8")));
 			entity.addPart("videoFileName", new StringBody(fileName,
 					"text/plain", Charset.forName("UTF-8")));
 		} catch (UnsupportedEncodingException e) {
 			Log.d(TAG,
 					"Failed to add entity parts due to string encodinuserFriendlyMessageg UTF-8");
 			e.printStackTrace();
 		}
 
 		entity.addPart("videoFile", new FileBody(new File(dataFile),
 				"video/3gp"));
 		httpPost.setEntity(entity);
 		String userFriendlyErrorMessage = "";
 		uploadStatusMessage = getString(R.string.contacting_server);
 		notifyUser(uploadStatusMessage, noti, notificationId, false);
 		try {
 			HttpResponse response = httpClient.execute(httpPost, localContext);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					response.getEntity().getContent(), "UTF-8"));
 			uploadStatusMessage = getString(R.string.server_contacted);
 			notifyUser(uploadStatusMessage, noti, notificationId, false);
 			String JSONResponse = "";
 			String newLine;
 			do {
 				newLine = reader.readLine();
 				if (newLine != null) {
 					JSONResponse += newLine;
 				}
 			} while (newLine != null);
 
 			Log.v(TAG, "JSONResponse: " + JSONResponse);
 			try {
 				JSONObject serverResponse = new JSONObject(JSONResponse);
 				try {
 					if (serverResponse.get("url") != null) {
 						uploadStatusMessage = (String) serverResponse
 								.get("url");
 						notifyUser(uploadStatusMessage, noti, notificationId,
 								false);
 						// Add new metadata including url to audio files
 						// Find video file in MediaStore
 						ContentResolver cr = getContentResolver();
 
 						String[] projection = {
 								BaseColumns._ID, MediaStore.Video.Media.DATA,
 								MediaStore.Video.VideoColumns.TITLE,
 								MediaStore.Video.VideoColumns.TAGS,
 								MediaStore.Video.Media._ID};
 						Cursor cursor;
 						try {
 							cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
 									MediaStore.Video.VideoColumns.TITLE
 											+ " LIKE ?",
 									new String[] { fileName }, null);
 
 							cursor.moveToFirst();
 						} catch (Exception e) {
 							return;
 						}
 						ContentValues values = new ContentValues(1);
 						
 						values.put(MediaStore.Video.VideoColumns.TAGS,
 								uploadStatusMessage);
 					    
 						Uri videoFileUri = Uri
 								.withAppendedPath(
 										MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
 										""
 												+ cursor.getInt(cursor
 														.getColumnIndex(MediaStore.Video.Media._ID)));
 						
 						cr.update(videoFileUri, values, null, null);
 						cursor.close();
 						Intent i = new Intent(PrivateConstants.VIDEO_UPLOADED);
 						sendBroadcast(i);
 					}
 				} catch (JSONException e) {
 					Log.d(TAG, "No successs message");
 					try {
 						if (serverResponse.get("error") != null) {
 							userFriendlyErrorMessage = (String) serverResponse
 									.get("error");
 						}
 					} catch (JSONException e2) {
 						Log.d(TAG, "No error message.");
 						e2.printStackTrace();
 						userFriendlyErrorMessage = getString(R.string.error_while_uploading)
 								+ " (20)";
 					}
 				}
 			} catch (JSONException e) {
 				Log.d(TAG, "Server errored in reply.");
 				e.printStackTrace();
 				userFriendlyErrorMessage = getString(R.string.error_while_uploading)
 						+ " (21)";
 			}
 		} catch (ClientProtocolException e) {
 			userFriendlyErrorMessage = getString(R.string.error_while_uploading)
 					+ " (22)";
 			Log.e(TAG, "ClientProtocolException.");
 			e.printStackTrace();
 		} catch (IOException e) {
 			userFriendlyErrorMessage = getString(R.string.error_while_uploading)
 					+ " (23)";
 			Log.e(TAG, "IOException.");
 			e.printStackTrace();
 		}
 		/*
 		 * Displays enotificationManagerrror if exists upon upload, otherwise
 		 * cancels the notification
 		 */
 		if (!"".equals(userFriendlyErrorMessage)) {
 			notifyUser(fileName + " " + userFriendlyErrorMessage, noti,
 					notificationId, true);
 		} else {
 			/* Success: remove the notification */
 			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
 					.cancel(notificationId);
 		}
 	}
 
 	public void notifyUser(String message, Notification notification, int id,
 			boolean showTryAgain) {
 		notification.tickerText = message;
 		notification.contentView.setTextViewText(R.id.notification_text,
 				message);
 		if (showTryAgain) {
 			notification.contentView.setTextViewText(R.id.notification_title,
 					getString(R.string.try_again));
 		}
 		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
 				id, notification);
 	}
 }

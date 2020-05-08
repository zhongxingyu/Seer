 /*
  * Copyright (C) 2010 Adam Nybäck
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package se.anyro.tagtider;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import se.anyro.tagtider.utils.Http;
 import se.anyro.tagtider.utils.StringUtils;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.telephony.gsm.SmsManager;
 import android.text.SpannableString;
 import android.text.style.StrikethroughSpan;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.SimpleAdapter.ViewBinder;
 
 @SuppressWarnings("deprecation")
 public class TransferActivity extends ListActivity {
 
 	private static String sLastTransferId;
 	private String mTrain, mStationId, mStationName, mTransferId;
 	private String mRegistrationId;
 	
 	private Bundle mNewExtras;
 	private static SimpleAdapter sChangesAdapter;
 	private static List<Map<String, Object>> sChanges = new ArrayList<Map<String, Object>>();
 
 	private static final String SMS_SENT = "se.anyro.tagtider.SMS_SENT";
     private SmsStatusReceiver mSmsStatusReceiver = new SmsStatusReceiver();
     private IntentFilter mSmsSentFilter = new IntentFilter(SMS_SENT);
 	private ProgressDialog mProgressDialog;
 	private View mProgressBar;
 	private AlertDialog mDialog;
 	private String mPhoneNumber;
     
 	public static final String C2DM_REGISTERED = "se.anyro.tagtider.C2DM_REGISTERED";
 	private C2dmStatusReceiver mC2dmStatusReceiver = new C2dmStatusReceiver();
 	private IntentFilter mC2dmRegisteredFilter = new IntentFilter(C2DM_REGISTERED);
 	
 	// Display mapping from keys to view id:s
     private static final String[] FROM = {"detected", "comment", "other"};
     private static final int[] TO = {R.id.time, R.id.comment, R.id.other};    
 	private TextView mEmptyView;
 	
 	private static final String TYPE_AC2DM = "ac2dm";
 	private static final String TYPE_SMS = "sms";
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		setContentView(R.layout.transfer);
 		
 		// Use c2dm if we have Android 2.2
         int sdkVersion = Integer.parseInt(Build.VERSION.SDK); // Cupcake style
         if (sdkVersion >= Build.VERSION_CODES.FROYO + 10) { // TODO: Change when we want to use C2DM
         	findViewById(R.id.sms).setVisibility(View.GONE);
         } else {
     		//TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
     		mPhoneNumber = null; // tm.getLine1Number(); TODO: Change when server supports this
     		if (mPhoneNumber != null && mPhoneNumber.length() == 0)
     			mPhoneNumber = null;
         	findViewById(R.id.c2dm).setVisibility(View.GONE);
         }
 		
         mProgressBar = findViewById(R.id.progress);
         
 		setupTransferData(getIntent().getExtras());
 		
 		setupDialogs();
 		
 		if (!mTransferId.equals(sLastTransferId)) {
 			// Fetch additional data about changes made to this transfer
 			new FetchChangesTask().execute(mTransferId);
 			sLastTransferId = mTransferId;
 		} else if (sChanges.size() > 0) {
 			addChangesAdapter();
 		}
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		Bundle extras = intent.getExtras();
 		if (extras.getString("id") != null) {
 			mTransferId = extras.getString("id");
 		}
 		new FetchChangesTask().execute(mTransferId);
 	}
 	
 	private void setupTransferData(final Bundle extras) {
 		TextView trainView = (TextView) findViewById(R.id.train);
 		
 		ViewGroup originGroup = (ViewGroup) findViewById(R.id.origin_group);
 		TextView originView = (TextView) findViewById(R.id.origin);
 		TextView arrivalView = (TextView) findViewById(R.id.arrival);
 	
 		TextView stationTrackView = (TextView) findViewById(R.id.station_track);
 
 		ViewGroup destinationGroup = (ViewGroup) findViewById(R.id.destination_group);
 		TextView destinationView = (TextView) findViewById(R.id.destination);
 		TextView departureView = (TextView) findViewById(R.id.departure);
 
 		TextView commentView = (TextView) findViewById(R.id.comment);
 		mEmptyView = (TextView) findViewById(android.R.id.empty);
 		
 		trainView.setText("Tåg " + extras.getString("train") + " (" + extras.getString("type") + ")");
 
 		String origin = extras.getString("origin");
 		if (origin != null && origin.length() > 0) {
 			originView.setText("Från " + origin);
 			originGroup.setVisibility(View.VISIBLE);
 		} else {
 			originGroup.setVisibility(View.GONE);
 		}
 
 		String track = extras.getString("track");
 		if (track == null || track.equalsIgnoreCase("x") || track.equalsIgnoreCase("null"))
 			track = "";
 		
 		String arrival = extras.getString("arrival");
 		if (arrival != null && !arrival.startsWith("0000")) { 
 			arrivalView.setText("Ankommer " + StringUtils.extractTime(arrival));
 			String newArrival = extras.getString("newArrival"); 
 			if (newArrival != null) {
 				newArrival = StringUtils.extractTime(newArrival);
 				SpannableString strike = new SpannableString(arrivalView.getText() + " " + newArrival);
 			    strike.setSpan(new StrikethroughSpan(), strike.length() - 11, strike.length() - 6, 0); 
 				arrivalView.setText(strike, TextView.BufferType.SPANNABLE);
 			}
 			if (track.length() == 0) {
 				SpannableString strike = new SpannableString(arrivalView.getText());
 			    strike.setSpan(new StrikethroughSpan(), strike.length() - 5, strike.length(), 0); 
 			    arrivalView.setText(strike, TextView.BufferType.SPANNABLE);
 			}
 		}
 
 		if (extras.getString("stationName") != null) {
 			mStationName = extras.getString("stationName");
 		}
 		
 		if (track.length() > 0 && mStationName != null)
 			stationTrackView.setText(mStationName + ", spår " +  track);
 		else if (mStationName != null)
 			stationTrackView.setText(mStationName);
 		else if (track.length() > 0)
 			stationTrackView.setText("Spår " + track);
 		else
 			stationTrackView.setText("");
 
 		String destination = extras.getString("destination");
 		if (destination != null && destination.length() > 0) {
 			destinationView.setText("Till " + destination);
 			destinationGroup.setVisibility(View.VISIBLE);
 		} else {
 			destinationGroup.setVisibility(View.GONE);
 		}
 	
 		String departure = extras.getString("departure");
 		if (departure != null && !departure.startsWith("0000")) {
 			departureView.setText("Avgår " + StringUtils.extractTime(departure));
 			String newDeparture = extras.getString("newDeparture"); 
 			if (newDeparture != null) {
 				newDeparture = StringUtils.extractTime(newDeparture);
 				SpannableString strike = new SpannableString(departureView.getText() + " " + newDeparture);
 			    strike.setSpan(new StrikethroughSpan(), strike.length() - 11, strike.length() - 6, 0); 
 				departureView.setText(strike, TextView.BufferType.SPANNABLE);
 			}
 			if (track.length() == 0) {
 				SpannableString strike = new SpannableString(departureView.getText());
 			    strike.setSpan(new StrikethroughSpan(), strike.length() - 5, strike.length(), 0); 
 				departureView.setText(strike, TextView.BufferType.SPANNABLE);
 			}
 		}
 		
 		String comment = extras.getString("comment");
		if ((comment == null || comment.length() == 0) && track.length() == 0)
			comment = "Inställt";
 		if (comment != null && comment.length() > 0) {
 			commentView.setText(comment);
 			commentView.setVisibility(View.VISIBLE);
 		} else {
 			commentView.setVisibility(View.GONE);
 		}
 		
 		mTrain = extras.getString("train");
 		mStationId = extras.getString("stationId");
 		
 		mTransferId = extras.getString("id");
 	}
 	
 	private void setupDialogs() {
 		final AlertDialog smsDialog = createSmsDialog();		
 		Button sendSmsButton = (Button) findViewById(R.id.sms);
 		sendSmsButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				smsDialog.show();
 			}
 		});
 		
 		final AlertDialog c2dmDialog = createC2dmDialog();
 		Button sendC2dmButton = (Button) findViewById(R.id.c2dm);
 		sendC2dmButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				c2dmDialog.show();
 			}
 		});
 		
 		mProgressDialog = new ProgressDialog(this);
 		mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();
 	}
 
 	private AlertDialog createSmsDialog() {		
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.sms_dialog_title);
 		if (mPhoneNumber == null) {
 			builder.setMessage(R.string.sms_dialog_message);
 		} else {
 			builder.setMessage(String.format(getString(R.string.sms_dialog_message_free), mPhoneNumber));
 		}
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if (mPhoneNumber == null) {
 					sendSms("0730121096", mTrain + " " + mStationId);
 				} else {
 					new StartSubscriptionTask().execute(mPhoneNumber, TYPE_SMS);
 				}					
 			}
 		});
 		builder.setNegativeButton("Avbryt", null);
 		final AlertDialog smsDialog = builder.create();
 		return smsDialog;
 	}	
 	
 	private AlertDialog createC2dmDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.c2dm_dialog_title);
 		builder.setMessage(R.string.c2dm_dialog_message);
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if (mRegistrationId == null) {
 					registerC2dm();
 				} else {
 					new StartSubscriptionTask().execute(mRegistrationId, TYPE_AC2DM);
 				}
 			}
 		});
 		builder.setNegativeButton("Avbryt", null);
 		final AlertDialog smsDialog = builder.create();
 		return smsDialog;
 	}	
 	
 	private void addChangesAdapter() {
         sChangesAdapter = new SimpleAdapter(this, sChanges, R.layout.change_row, FROM, TO);
         sChangesAdapter.setViewBinder(new ViewBinder() {
         	public boolean setViewValue(View view, Object data, String textRepresentation) {
         		// Hide views with empty text
         		if (textRepresentation.length() == 0) {
         			view.setVisibility(View.GONE);
         			return true;
         		}
         		view.setVisibility(View.VISIBLE);
 
         		return false;
         	}
         });
         setListAdapter(sChangesAdapter);
 	}
 	
     /**
      * Class for fetching transfer changes asynchronously
      */
     private class FetchChangesTask extends AsyncTask<String, String, String> {
 
     	@Override
     	protected void onPreExecute() {
     		mEmptyView.setText("Hämtar ändringar...");
     		mProgressBar.setVisibility(View.VISIBLE);
     	}
     	
 		@Override
 		protected String doInBackground(String... params) {
 			
 			String transferId = params[0];
 			
 			HttpGet httpGet = new HttpGet("http://api.tagtider.net/v1/transfers/" + transferId + ".json");
 			httpGet.setHeader("User-Agent", Http.getUserAgent());
 			
 			try {
 				HttpResponse response = Http.getClient().execute(httpGet);
 				int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode != 200)
 					return "Tillfälligt fel " + statusCode + " :-(";
 
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				
 				String json = StringUtils.readTextFile(content);
 				try {
 					// Parse json response into objects
 					JSONObject root = new JSONObject(json);
 					
 					JSONObject transfer = root.getJSONObject("transfer");
 					
 					@SuppressWarnings("unchecked")
 					Iterator transferKeys = transfer.keys();
 					mNewExtras = new Bundle();
 					while (transferKeys.hasNext()) {
 						Object key = transferKeys.next();
 						Object value = transfer.get((String) key);
 						if (value == JSONObject.NULL)
 							value = null;
 						if (value instanceof String)
 							mNewExtras.putString((String) key, (String) value);
 					}
 					
 					if (!transfer.has("changes"))
 						return "Inga ändringar ännu";
 					JSONArray changes = transfer.getJSONObject("changes").getJSONArray("change");
 
 					int maxChanges = changes.length();
 					if (maxChanges > 3) {
 						maxChanges = 3;
 					}
 					
 					sChanges.clear();
 					
 					for (int i = 0; i < maxChanges; ++i) {
 						JSONObject change = changes.getJSONObject(i);
 						@SuppressWarnings("unchecked")
 						Iterator keys = change.keys();
 						Map<String, Object> changeMap = new HashMap<String, Object>();
 						while (keys.hasNext()) {
 							Object key = keys.next();
 							Object value = change.get((String) key);
 							if (value == JSONObject.NULL)
 								value = null;
 							changeMap.put((String) key, value);
 						}
 						
 						String track = change.getString("track");
 						if (track.length() > 0 &&  !track.equalsIgnoreCase("null"))
 							track = "Spår " + track;
 						else
 							track = "";
 						
 						String arrival = change.getString("arrival");
 						if (arrival.length() > 0 && arrival.charAt(0) == '2')
 							arrival = "Ankommer " + StringUtils.extractTime(arrival);
 						else
 							arrival = "";
 						
 						String departure = change.getString("departure");
 						if (departure.length() > 0 && departure.charAt(0) == '2')
 							departure = "Avgår " + StringUtils.extractTime(departure);
 						else
 							departure = "";
 						
 						String[] other = new String[]{track, arrival, departure};
 						changeMap.put("other", StringUtils.join(other, ", "));
 						sChanges.add(changeMap);
 					}
 					return null;
 				} catch (JSONException e) {
 					return "Tillfälligt fel";
 				}
 
 			} catch (ClientProtocolException e) {
 				return "Kommunikationsfel";
 			} catch (IOException e) {
 				return "Ingen kontakt";
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			
     		mProgressBar.setVisibility(View.GONE);
 			
     		if (result == null) {
 				addChangesAdapter();
 				if (mNewExtras != null) {
 					setupTransferData(mNewExtras);
 				}
 			} else {
 				mEmptyView.setText(result);
 			}
 		}
     }
     
     /**
      * Class for sending message to server to start subscription
      */
     private class StartSubscriptionTask extends AsyncTask<String, String, String> {
 
     	@Override
     	protected void onPreExecute() {
     		mProgressDialog.setMessage("Registrerar bevakning...");
     		mProgressDialog.show();
     	}
     	
 		@Override
 		protected String doInBackground(String... params) {
 			
 			String id = params[0];
 			String type = params[1];
 			
 			HttpPost httpPost = new HttpPost("http://api.tagtider.net/v1/subscriptions.json?device_token=" + id);
 			final List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("train", mTrain));
 			nameValuePairs.add(new BasicNameValuePair("station", mStationId));
 //			nameValuePairs.add(new BasicNameValuePair("date", value));
 			nameValuePairs.add(new BasicNameValuePair("type", type));
 	        HttpEntity postEntity = null;
 	        try {
 	            postEntity = new UrlEncodedFormEntity(nameValuePairs);
 	        } catch (final UnsupportedEncodingException e) {
 	            // this should never happen.
 	            throw new AssertionError(e);
 	        }
 	        httpPost.setEntity(postEntity);
 	        httpPost.setHeader(postEntity.getContentType());
 			httpPost.setHeader("User-Agent", Http.getUserAgent());
 			
 			try {
 				HttpResponse response = Http.getClient().execute(httpPost);
 				int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode != 200)
 					return "Tillfälligt fel " + statusCode + " :-(";
 
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				
 				String json = StringUtils.readTextFile(content);
 				try {
 					// Parse json response into objects
 					JSONObject root = new JSONObject(json);
 					
 /*					JSONObject transfer = root.getJSONObject("transfer");
 					if (!transfer.has("changes"))
 						return "Inga ändringar ännu";
 					JSONArray changes = transfer.getJSONObject("changes").getJSONArray("change");
 
 					int maxChanges = changes.length();
 					if (maxChanges > 3) {
 						maxChanges = 3;
 					}*/
 					
 					return null;
 				} catch (JSONException e) {
 					return "Tillfälligt fel";
 				}
 
 			} catch (ClientProtocolException e) {
 				return "Kommunikationsfel";
 			} catch (IOException e) {
 				return "Ingen kontakt";
 			}
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			mProgressDialog.hide();
 			if (result != null) {
 				showMessage("Problem", result);
 			}
 		}
     }
     
 	private void registerC2dm() {
 		mProgressDialog.setMessage("Initierar bevakning...");
 		mProgressDialog.show();
 		
         registerReceiver(mC2dmStatusReceiver, mC2dmRegisteredFilter);
 
 		Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
 		registrationIntent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));
 		registrationIntent.putExtra("sender", "tagtider@gmail.com");
 		startService(registrationIntent);
 	}
 	
     private class C2dmStatusReceiver extends BroadcastReceiver {
     	
         @Override
         public void onReceive(Context context, Intent intent) {
         	
         	if (intent.getAction().equals(C2DM_REGISTERED)) {
         		String error = intent.getStringExtra("error");
         		if (error != null) {
             		mProgressDialog.hide();
         			showMessage("Problem", error);
         		} else {
         			mRegistrationId = intent.getStringExtra("registrationId");
 					new StartSubscriptionTask().execute(mRegistrationId, TYPE_AC2DM);
         		}
             	unregisterReceiver(mC2dmStatusReceiver);
         	}
         }
     }
 
 	private void sendSms(String phoneNumber, String message) {
 		 
         registerReceiver(mSmsStatusReceiver, mSmsSentFilter);
 
         PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
  
         ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
         sentIntents.add(sentIntent); 
         
         SmsManager sms = SmsManager.getDefault();
         ArrayList<String> smstext = sms.divideMessage(message);
         
 		mProgressDialog.setMessage("Skickar SMS...");
 		mProgressDialog.show();
 
         // Using multipart as a work-around for a bug in HTC Tattoo
         sms.sendMultipartTextMessage(phoneNumber, null, smstext, sentIntents, null);
     }
 	
     private class SmsStatusReceiver extends BroadcastReceiver {
     	
         @Override
         public void onReceive(Context context, Intent intent) {
         	
         	if (intent.getAction().equals(SMS_SENT)) {
         		onSmsSent();
             	unregisterReceiver(mSmsStatusReceiver);
         	}
         }
 
 		private void onSmsSent() {
 
 			mProgressDialog.hide();
 
 			String error = null;
 			switch (getResultCode()) {
 			case Activity.RESULT_OK:
 				break;
 			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
 				error = "Tekniskt fel";
 				break;
 			case SmsManager.RESULT_ERROR_NO_SERVICE:
 				error = "Ingen service";
 				break;
 			case SmsManager.RESULT_ERROR_NULL_PDU:
 				error = "Ingen PDU";
 				break;
 			case SmsManager.RESULT_ERROR_RADIO_OFF:
 				error = "SMS är avstängt på telefonen";
 				break;
 			default:
 				error = "Okänt fel: " + getResultCode();
 			}
 			
 			if (error != null) {
 				showMessage("Problem", error);
 			}
 		}
     }
 	
 	private void showMessage(String title, String message) {
 		mDialog.setTitle(title);
 		mDialog.setMessage(message);
 		mDialog.show();
 	}
 }

 package com.example.getfromservervianfc;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Parcelable;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Switch;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	TextView textViewStatus;
 	// A vltoz amibe az ID-t olvassuk
 	String read;
 
 	// flag, hogy ne fusson kt hlzatos szl egyszerre
 	private static boolean running = false;
 	private static Object syncObject = new Object();
 	// Handler, ami majd mdostani fogja a UI-t
 	Handler handler;
 
 	// NFC-hez kell
 	private Switch enableWrite;
 	private Button enableRead;
 	private static String questionID = "1";
 	
 	private TextView ques;
 	private Button answ1;
 	private Button answ2;
 	private Button answ3;
 	private Button answ4;
 	private ImageView im;
 	
 	private static EditText StringToWrite;
 	IntentFilter[] mWriteTagFilters;
 	NfcAdapter mNfcAdapter;
 	private PendingIntent mNfcPendingIntent;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// remove these 2 lines if the image download doesnt run on the main thread
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		textViewStatus = (TextView) findViewById(R.id.textView1);
 	//	textViewStatus.setText("Oncreate");
 		
 		ques = (TextView) findViewById(R.id.textView2);
 		answ1 = (Button ) findViewById(R.id.button1);
 		answ2 = (Button ) findViewById(R.id.button2);
 		answ3 = (Button ) findViewById(R.id.button3);
 		answ4 = (Button ) findViewById(R.id.button4);
 		im = (ImageView) findViewById(R.id.imageView1);
 		
 		answ1.setVisibility(View.INVISIBLE);
 		answ2.setVisibility(View.INVISIBLE);
 		answ3.setVisibility(View.INVISIBLE);
 		answ4.setVisibility(View.INVISIBLE);
 		im.setVisibility(View.INVISIBLE);
 		
 		
 		
 		// Handler, ami majd mdostani fogja a UI-t
 		handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {
 				if (msg.what == 0) {
 					Bundle b = msg.getData();
 					// Kirja a kapott szveget
 					String tmp=b.getString("text");
 					//textViewStatus.setText(tmp);
 					
 					NdefMessage[] msgs = null;	
 					Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 					if (rawMsgs != null) {
 						msgs = new NdefMessage[rawMsgs.length];
 						for (int i = 0; i < rawMsgs.length; i++) {
 							msgs[i] = (NdefMessage) rawMsgs[i];
 						}
 					}
 					
 					if (msgs != null) {
 						for (NdefMessage tmpMsg : msgs) {
 							for (NdefRecord tmpRecord : tmpMsg.getRecords()) {
 								textViewStatus.setText(new String(tmpRecord.getPayload()));
 								//questionID=textViewStatus.getText().toString();
 							}
 						}
 					}
 					
 					try {
 						
 						JSONObject c = new JSONObject(tmp);
 						
 						String date = c.getString("Date");
 						String question = c.getString("Question");
 						JSONObject answers = c.getJSONObject("Answers");
 						String a1 = answers.getString("Answer1");
 						String a2 = answers.getString("Answer2");
 						String a3 = answers.getString("Answer3");
 						String a4 = answers.getString("Answer4");
 						String imageURL = c.getString("Image");
 						
 						ques.setText(question);
 						answ1.setText(a1);
 						answ2.setText(a2);
 						answ3.setText(a3);
 						answ4.setText(a4);
 						
 						answ1.setVisibility(View.VISIBLE);
 						answ2.setVisibility(View.VISIBLE);
 						answ3.setVisibility(View.VISIBLE);
 						answ4.setVisibility(View.VISIBLE);
 						im.setVisibility(View.VISIBLE);
 						
 						
 						// TODO on new thread, remove the strict mode setting
 						try {
 							  Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(imageURL).getContent());
 							  im.setImageBitmap(bitmap); 
 							} catch (MalformedURLException e) {
 							  e.printStackTrace();
 							} catch (IOException e) {
 							  e.printStackTrace();
 							}
 						
 						
 						
 						//textViewStatus.setText(a3);
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						ques.setText("Invalid question ID!");
 						answ1.setVisibility(View.INVISIBLE);
 						answ2.setVisibility(View.INVISIBLE);
 						answ3.setVisibility(View.INVISIBLE);
 						answ4.setVisibility(View.INVISIBLE);
 						im.setVisibility(View.INVISIBLE);
 						e.printStackTrace();
 					} 
 					
 
 					
 				} else if (msg.what == 100) {
 					textViewStatus.setText("NEM TUD KAPCSOLDNI A SZERVERHEZ");
 				}
 				super.handleMessage(msg);
 			}
 		};
 
 		// NFC rsz
 
 		
 
 		enableWrite = (Switch) findViewById(R.id.switch1);
 		StringToWrite = (EditText) findViewById(R.id.editText1);
 		enableWrite.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 				if (arg1) {
 					enableWrite();
 				} else {
 					disableWrite();
 				}
 
 			}
 		});
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
 		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
 				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 		
 		answ2.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 
				 HttpClient httpclient = new DefaultHttpClient();
 				 
 				 try {
 				 HttpPost httppost = new HttpPost("http://nfconlab.azurewebsites.net/Home/Questions");
 				    
 				 JSONObject json = new JSONObject();
				 json.put("Answer", "2");
 				 
 				 StringEntity se = new StringEntity(json.toString());
 				 se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
 				 httppost.setEntity(se);
 				 HttpResponse response = httpclient.execute(httppost);
 				 String s = EntityUtils.toString(response.getEntity());
 				 Log.d("httpresponse", s);
 					} catch (ClientProtocolException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}				
 			        
 			}
 				
 
 		});
 
 	}
 
 	class readFromServerAsync extends AsyncTask<URL, Integer, Long>{
 
 		@Override
 		protected Long doInBackground(URL... params) {
 			HttpClient httpclient = new DefaultHttpClient();
 			String URL = "http://nfconlab.azurewebsites.net/Home/Questions/";
 			//Meghvja a kiolvasott String paramterrel az URL-t
 			HttpGet httpGet = new HttpGet(URL + questionID);
 			//HttpGet httpGet = new HttpGet(URL);
 			HttpResponse response;
 			HttpEntity entity;
 			InputStream instream;
 			try {
 				response = httpclient.execute(httpGet);
 				entity = response.getEntity();
 				instream = entity.getContent();
 				processResponseAsync(instream, read);
 				
 			} catch (ClientProtocolException e) {
 				// Hiba van, kld egy zenetet, ami a UI-ra kirja,
 				// hogy hiba
 				e.printStackTrace();
 			} catch (IOException e) {
 				// Hiba van, kld egy zenetet, ami a UI-ra kirja,
 				// hogy hiba
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 	}
 	// Vlaszt feldolgoz fggvny
 	private void processResponseAsync(final InputStream aIS, final String s) {
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(aIS));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		String tmp = new String();
 		try {
 			// kiolvassa sorrol sorra
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 			// Belltja az zenetet a handler szmra
 			tmp=sb.toString();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (aIS != null) {
 				try {
 					aIS.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			textViewStatus.setText(tmp);
 		}
 
 	}
 	
 	// Olvass a szervertl
 	private void readFromServer(final String s) {
 		// Kln szlban, hogy ne blokkoldjon
 		new Thread() {
 			public void run() {
 				synchronized (syncObject) {
 					if (!running) {
 						running = true;
 						Log.d("nfcdebug", s);
 						HttpClient httpclient = new DefaultHttpClient();
 						String URL = "http://nfconlab.azurewebsites.net/Home/Questions/";
 						URL = URL + questionID;
 						//HttpGet httpGet = new HttpGet(URL + s);
 						HttpGet httpGet = new HttpGet(URL);
 						HttpResponse response;
 						HttpEntity entity;
 						InputStream instream;
 						try {
 							response = httpclient.execute(httpGet);
 							entity = response.getEntity();
 							instream = entity.getContent();
 							processResponse(instream, s);
 
 						} catch (ClientProtocolException e) {
 							// Hiba van, kld egy zenetet, ami a UI-ra kirja,
 							// hogy hiba
 							Message msg = handler.obtainMessage();
 							msg.what = 100;
 							// elkldi az zenetet, majd a handler mdostja a
 							// UI-t
 							handler.sendMessage(msg);
 							e.printStackTrace();
 						} catch (IOException e) {
 							// Hiba van, kld egy zenetet, ami a UI-ra kirja,
 							// hogy hiba
 							Message msg = handler.obtainMessage();
 							msg.what = 100;
 							// elkldi az zenetet, majd a handler mdostja a
 							// UI-t
 							handler.sendMessage(msg);
 							e.printStackTrace();
 						}
 						running = false;
 					}
 				}
 			}
 		}.start();
 	}
 
 	// Vlaszt feldolgoz fggvny
 	private void processResponse(final InputStream aIS, final String s) {
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(aIS));
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		try {
 			// kiolvassa sorrol sorra
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 			// Belltja az zenetet a handler szmra
 			Message msg = handler.obtainMessage();
 			msg.what = 0;
 			Bundle b = new Bundle();
 			b.putString("text", new String(sb.toString()));
 			msg.setData(b);
 			// elkldi az zenetet, majd a handler mdostja a UI-t
 			handler.sendMessage(msg);
 			String ssss = new String(sb.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (aIS != null) {
 				try {
 					aIS.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	protected void enableWrite() {
 		IntentFilter tagDetected = new IntentFilter(
 				NfcAdapter.ACTION_TAG_DISCOVERED);
 		mWriteTagFilters = new IntentFilter[] { tagDetected };
 		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
 				mWriteTagFilters, null);
 	}
 
 	protected void disableWrite() {
 		mNfcAdapter.disableForegroundDispatch(this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		//textViewStatus.setText("OnResume");
 		Intent intent = getIntent();
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			Parcelable[] rawMsgs = intent
 					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 			if (rawMsgs != null) {
 				NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
 				for (int i = 0; i < rawMsgs.length; i++) {
 					msgs[i] = (NdefMessage) rawMsgs[i];
 				}
 				String message;
 				for (NdefMessage tmpMsg : msgs) {
 					for (NdefRecord tmpRecord : tmpMsg.getRecords()) {
 						//tv.append("\n" + new String(tmpRecord.getPayload()));
 						new String(tmpRecord.getPayload());
 						message = new String(tmpRecord.getPayload());
 						String tmp = message.substring(1);
 						System.out.println(tmp);
 						readFromServer(tmp);
 						//AsyncTask<URL, Integer, Long> rfsa = new readFromServerAsync().execute();
 					}
 				}
 			}
 
 		}
 		
 		NdefMessage[] msgs = null;	
 		Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		if (rawMsgs != null) {
 			msgs = new NdefMessage[rawMsgs.length];
 			for (int i = 0; i < rawMsgs.length; i++) {
 				msgs[i] = (NdefMessage) rawMsgs[i];
 			}
 		}
 		
 		if (msgs != null) {
 			for (NdefMessage tmpMsg : msgs) {
 				for (NdefRecord tmpRecord : tmpMsg.getRecords()) {
 					textViewStatus.append("\n"+new String(tmpRecord.getPayload()));
 					//questionID=textViewStatus.getText().toString();
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		// Tag writing mode
 		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
 			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
 			NdefRecord record1 = createTextRecord(StringToWrite.getText()
 					.toString());
 
 			NdefMessage msg = new NdefMessage(new NdefRecord[] { record1 });
 
 			if (writeTag(msg, detectedTag)) {
 				Toast.makeText(this, "Successful write operation! "+"qID: "+StringToWrite.getText().toString(),
 						Toast.LENGTH_LONG).show();
 				Log.d("lol",StringToWrite.getText().toString());
 				
 				questionID=StringToWrite.getText().toString();
 			} else {
 				Toast.makeText(this, "Failed to write!", Toast.LENGTH_LONG)
 						.show();
 			}
 		}
 	}
 
 	public NdefRecord createTextRecord(String payload) {
 		byte[] textBytes = payload.getBytes();
 		byte[] data = new byte[1 + textBytes.length];
 		data[0] = (byte) 0;
 		System.arraycopy(textBytes, 0, data, 1, textBytes.length);
 		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
 				NdefRecord.RTD_TEXT, new byte[0], data);
 		return record;
 	}
 
 	public static boolean writeTag(NdefMessage message, Tag tag) {
 		int size = message.toByteArray().length;
 		try {
 			Ndef ndef = Ndef.get(tag);
 			if (ndef != null) {
 				ndef.connect();
 				if (!ndef.isWritable()) {
 					return false;
 				}
 				if (ndef.getMaxSize() < size) {
 					return false;
 				}
 				ndef.writeNdefMessage(message);
 				return true;
 			} else {
 				NdefFormatable format = NdefFormatable.get(tag);
 				if (format != null) {
 					try {
 						format.connect();
 						format.format(message);
 						return true;
 					} catch (IOException e) {
 						return false;
 					}
 				} else {
 					return false;
 				}
 			}
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 }

 package com.blogspot.comolog.speechtosocket;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.util.Base64;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import de.sciss.net.*;
 
 public class SpeechToSocketActivity extends Activity {
 	private static final int REQUEST_CODE = 0;
 	private static final int OUTGOING_PORT = 57110;
 	private static final int INCOMMING_PORT = 57111;
 	
 	private static final String PREF_KEY = "preferenceTest";
 	private static final String KEY_TEXT = "text";
 	private static final String DEFAULT_IP_ADDRESS = "192.168.0.2";
 	final Object mSync = new Object();
 	private int mMsgId = 0;
 	private OSCServer mOsc;
 	private InetSocketAddress mAddress;
 
 	// final OSCBundle bndl1, bndl2;
 	// final Integer nodeID;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Button button;
 		button = (Button) findViewById(R.id.buttonStartFreeForm);
 		button.setOnClickListener(mOnClickListener);
 		button = (Button) findViewById(R.id.buttonStartWebSearch);
 		button.setOnClickListener(mOnClickListener);
 		button = (Button) findViewById(R.id.buttonConnect);
 		button.setOnClickListener(mOnClickListener);
 		
 		SharedPreferences pref = getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
 		EditText ip = (EditText)findViewById(R.id.editTextIpAddress);
 		ip.setText(pref.getString(KEY_TEXT, DEFAULT_IP_ADDRESS));
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
 			String resultsString = "";
 			List<String> results = data
 					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 			for (int i = 0; i < results.size(); i++) {
 				String res = results.get(i);
 				resultsString += res + "\n";
 				byte[] b64str = null;
 				try {
 					b64str = Base64.encode(res.getBytes("UTF-8"), Base64.DEFAULT);
 				} catch (UnsupportedEncodingException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
				if (mOsc != null && mAddress != null && b64str != null) {
 					try {
 						mOsc.send(new OSCMessage("/notify", new Object[] {
 								new Integer(mMsgId), new Integer(i), b64str}), mAddress);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						//e.printStackTrace();
 					}
 				}
 			}
 			TextView edit = (TextView) findViewById(R.id.editTextResults);
 			edit.clearComposingText();
 			edit.setText(resultsString);
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		Log.v(SpeechToSocketActivity.class.toString(), "onKeyDown : keyCode:" + keyCode);
 //		if (keyCode == 85) {
 //			return true;
 //		} else if (keyCode == 86) {
 //			return true;
 //		} else if (keyCode == 87) {
 //			return true;
 //		}
 //		
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
 		Log.v(SpeechToSocketActivity.class.toString(), "onKeyLongPress : keyCode:" + keyCode);
 
 		return super.onKeyLongPress(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
 		Log.v(SpeechToSocketActivity.class.toString(), "onKeyMultiple : keyCode:" + keyCode);
 
 		return super.onKeyMultiple(keyCode, repeatCount, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		Log.v(SpeechToSocketActivity.class.toString(), "onKeyUp : keyCode:" + keyCode);
 		return super.onKeyUp(keyCode, event);
 	}
 	private void startSR(boolean isFree) {
 		try {
 			// FpCeg
 			Intent intent;
 			intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 			if (isFree)
 				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 			else
 				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 						RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
 
 			// intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
 			// R.string.check_pronounce_text);
 			startActivityForResult(intent, REQUEST_CODE);
 		} catch (ActivityNotFoundException e) {
 			Toast.makeText(SpeechToSocketActivity.this, e.toString(),
 					Toast.LENGTH_SHORT).show();
 		}		
 	}
 	
 	private OnClickListener mOnClickListener = new OnClickListener() {
 
 		public void onClick(View v) {
 			if (v.getId() == R.id.buttonConnect) {
 				if (mOsc != null) {
 					try {
 						mOsc.stop();
 					} catch (IOException e) {
 					}
 					mOsc.dispose();
 				}
 				EditText ip = (EditText) findViewById(R.id.editTextIpAddress);
 				if (ip == null)
 					return;
 				String s = ip.getText().toString();
 				if (s.isEmpty())
 					return;
 				SharedPreferences pref = getSharedPreferences(PREF_KEY, Activity.MODE_PRIVATE);
 				SharedPreferences.Editor editor = pref.edit();
 				editor.putString(KEY_TEXT, s);
 				editor.commit();
 
 				String ipAddr = SpeechToSocketActivity.this.getIpAddress();
 				byte[] b64str = null;
 				try {
 					b64str = Base64.encode(ipAddr.getBytes("UTF-8"), Base64.DEFAULT);
 				} catch (UnsupportedEncodingException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 
 				try {
 					mOsc = OSCServer.newUsing(OSCServer.UDP, INCOMMING_PORT);
 					mAddress = new InetSocketAddress(s, OUTGOING_PORT);
 					//mOsc.setTarget();
 					mOsc.start();
 					mOsc.addOSCListener(mOscListener);
 					mOsc.send(new OSCMessage("/start", new Object[] {b64str}), mAddress);
 					String str = getString(R.string.connected) + ": " + s + ":" + OUTGOING_PORT;
 					Toast.makeText(SpeechToSocketActivity.this, str,
 							Toast.LENGTH_SHORT).show();
 					
 				} catch (IOException e) {
 				}
				
 				return;
 			}
 			
 			int id = v.getId(); 
 
 			if (id == R.id.buttonStartFreeForm)
 				startSR(true);
 			else if (id == R.id.buttonStartWebSearch)
 				startSR(false);
 		}
 	};
 
 	private OSCListener mOscListener = new OSCListener() {
 
 		public void messageReceived(OSCMessage arg0, SocketAddress arg1,
 				long arg2) {
 			Log.v(SpeechToSocketActivity.class.toString(), "messageReceived:" + arg0.toString());
 			if (arg0.getName().equals("/n_end")) {
 				synchronized (mSync) {
 					mSync.notifyAll();
 				}
 			} else if (arg0.getName().equals("/kick_free")) {
 				startSR(true);
 			} else if (arg0.getName().equals("/kick_web")) {
 				startSR(false);
 			}
 		}
 	};
 	
     private String getIpAddress() {
         Enumeration<NetworkInterface> netIFs;
         try {
             netIFs = NetworkInterface.getNetworkInterfaces();
             while( netIFs.hasMoreElements() ) {
                 NetworkInterface netIF = netIFs.nextElement();
                 Enumeration<InetAddress> ipAddrs = netIF.getInetAddresses();
                 while( ipAddrs.hasMoreElements() ) {
                     InetAddress ip = ipAddrs.nextElement();
                     if( ! ip.isLoopbackAddress() ) {
                         return ip.getHostAddress().toString();
                     }
                 }
             }
         } catch (SocketException e) {
             e.printStackTrace();
         }
         return null;
     }
 }

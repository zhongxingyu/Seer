 package com.blogspot.comolog.speechtosocket;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class SpeechToSocketActivity extends Activity {
 	private static final String TAG = "SpeechToSocketActivity";
 	
 	private static final int REQUEST_CODE = 0;
     private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
 
     private static final String TAG_SEARCH = "SEAR";
     private static final String TAG_DIRECTION_UP = "DIRU";
     private static final String TAG_DIRECTION_DOWN = "DIRD";
     private static final String TAG_DIRECTION_LEFT = "DIRL";
     private static final String TAG_DIRECTION_RIGHT = "DIRR";
     private static final String TAG_TOUCH = "TOUC";
     private static final String TAG_TAP = "TAP_";
     
 	private static final float TORELANCE = 200.0f;
 
 	private BluetoothChat mChat;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mChat = new BluetoothChat();
 		mChat.create(this, mChatListener);
 		
 		Button button;
 		button = (Button) findViewById(R.id.buttonStartFreeForm);
 		button.setOnClickListener(mOnClickListener);
 		button = (Button) findViewById(R.id.buttonStartWebSearch);
 		button.setOnClickListener(mOnClickListener);
 		
 		View view = findViewById(R.id.viewTouch);
 		view.setOnTouchListener(mTouchListener);
 		
 		ListView listView = (ListView) findViewById(R.id.listResults);
 		listView.setOnItemClickListener(mItemClickListener);
 	}
 
 	@Override
 	protected void onDestroy() {		
 		mChat.destroy();
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		mChat.resume();
 		super.onResume();
 	}
 
 	@Override
 	protected void onStart() {
 		mChat.start();
 		super.onStart();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		switch (requestCode) {
 		case REQUEST_CODE:
 			if (resultCode == RESULT_OK) {
				String resultsString = "";
 				List<String> results = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 				ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
						R.layout.list_item);
 				ListView listView = (ListView) findViewById(R.id.listResults);
 				listView.setAdapter(aa);
 				aa.clear();
 				for (int i = 0; i < results.size(); i++) {
 					String res = results.get(i);
 					aa.add(res);
//					resultsString += res + "\n";
 				}
//				edit.setText(resultsString);
 			}
 			break;
         case REQUEST_CONNECT_DEVICE_SECURE:
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
             	mChat.connectDevice(data);
             }
             break;
         case BluetoothChat.REQUEST_ENABLE_BT:
             // When the request to enable Bluetooth returns
             if (resultCode == Activity.RESULT_OK) {
                 // Bluetooth is now enabled, so set up a chat session
             	mChat.setupChat();
             } else {
                 // User did not enable Bluetooth or an error occurred
                 Log.d(TAG, "BT not enabled");
                 Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                 this.finish();
             }
             break;
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
 		int action = event.getAction();
 		int keyCode = event.getKeyCode();
 		Log.v(TAG, "dispatchKeyEvent : keyCode:" + keyCode);
 		
 		if (true) {//action == KeyEvent.ACTION_DOWN) {
 			switch (keyCode) {
 			case KeyEvent.KEYCODE_CALL:
 				Log.v(TAG, "KeyEvent.KEYCODE_CALL");
 				return true;
 			case KeyEvent.KEYCODE_ENDCALL:
 				Log.v(TAG, "KeyEvent.KEYCODE_ENDCALL");
 				return true;
 			case KeyEvent.KEYCODE_VOLUME_UP:
 				Log.v(TAG, "KeyEvent.KEYCODE_VOLUME_UP");
 				return true;
 			case KeyEvent.KEYCODE_VOLUME_DOWN:
 				Log.v(TAG, "KeyEvent.KEYCODE_VOLUME_DOWN");
 				return true;
 			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
 				if (action == KeyEvent.ACTION_DOWN)
 					startSR(true);
 				return true;
 			case KeyEvent.KEYCODE_MEDIA_NEXT:
 				break;
 			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:				
 //			{
 //				KeyEvent newev = new KeyEvent(
 //						event.getDownTime(),
 //						event.getEventTime(),
 //						event.getAction(),
 //						KeyEvent.KEYCODE_BACK,
 //						event.getRepeatCount(),
 //						event.getMetaState(),
 //						event.getDeviceId(),
 //						event.getScanCode(),
 //						event.getFlags(),
 //						event.getSource());
 //				return super.dispatchKeyEvent(newev);
 //			}
 				break;
 			}
 		}
 		
 		return super.dispatchKeyEvent(event);
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		Log.v(TAG, "onKeyDown : keyCode:" + keyCode);
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
 		Log.v(TAG, "onKeyLongPress : keyCode:" + keyCode);
 
 		return super.onKeyLongPress(keyCode, event);
 	}
 
 	@Override
 	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
 		Log.v(TAG, "onKeyMultiple : keyCode:" + keyCode);
 
 		return super.onKeyMultiple(keyCode, repeatCount, event);
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		Log.v(TAG, "onKeyUp : keyCode:" + keyCode);
 		return super.onKeyUp(keyCode, event);
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.secure_connect_scan:
             // Launch the DeviceListActivity to see devices and do scan
         	mChat.startDeviceListActivity(REQUEST_CONNECT_DEVICE_SECURE);
             return true;
         case R.id.discoverable:
             // Ensure this device is discoverable by others
         	mChat.ensureDiscoverable();
             return true;        
         }
         return false;
     }
 	
 	private void startSR(boolean isFree) {
 
 		try {
 			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 			if (isFree)
 				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 						RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 			else
 				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 						RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
 			startActivityForResult(intent, REQUEST_CODE);
 		} catch (ActivityNotFoundException e) {
 			Toast.makeText(SpeechToSocketActivity.this, e.toString(),
 					Toast.LENGTH_SHORT).show();
 		}		
 	}
 	
 	private OnClickListener mOnClickListener = new OnClickListener() {
 
 		public void onClick(View v) {
 
 			int id = v.getId(); 
 
 			if (id == R.id.buttonStartFreeForm)
 				startSR(true);
 			else if (id == R.id.buttonStartWebSearch)
 				startSR(false);
 		}
 	};
 	
     private BluetoothChat.Listener mChatListener = new BluetoothChat.Listener() {
 
 		public void recieveMessage(String str) {
 			if (str.charAt(0) == 'f')
 				startSR(true);
 			else if (str.charAt(0) == 'w')
 				startSR(false);
 			Log.v(TAG, str);
 		}
     };
     
     private OnTouchListener mTouchListener = new OnTouchListener() {
 
 		public boolean onTouch(View v, MotionEvent event) {
 			return mGestureDetector.onTouchEvent(event);
 		}
     	
     };
     
     private GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
 
 		public boolean onDown(MotionEvent e) {
 			Log.v(TAG, "onDown");
 			return true;
 		}
 
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 				float velocityY) {
 			{
 				Log.v(TAG, "velocityX:" + velocityX);
 				Log.v(TAG, "velocityY:" + velocityY);
 			}
 			
 			float avx = Math.abs(velocityX);
 			float avy = Math.abs(velocityY);
 			if (avx - avy > TORELANCE) {
 				if (velocityX > TORELANCE) {
 					// right
 					mChat.sendMessage(TAG_DIRECTION_RIGHT + "\n");
 					return true;
 				} else if (velocityX < -TORELANCE) {
 					// left
 					mChat.sendMessage(TAG_DIRECTION_LEFT + "\n");
 					return true;
 				}
 			} else if (avy - avx > TORELANCE) {
 				if (velocityY > TORELANCE) {
 					// down
 					mChat.sendMessage(TAG_DIRECTION_DOWN + "\n");
 					return true;
 				} else if (velocityY < -TORELANCE) {
 					// up
 					mChat.sendMessage(TAG_DIRECTION_UP + "\n");
 					return true;
 				}				
 			}
 			
 			return false;
 		}
 
 		public void onLongPress(MotionEvent e) {
 			Log.v(TAG, "onLongPress");
 		}
 
 		public boolean onScroll(MotionEvent e1, MotionEvent e2,
 				float distanceX, float distanceY) {
 			Log.v(TAG, "distanceX:" + distanceX + " distanceY:" + distanceY);
 			mChat.sendMessage(TAG_TOUCH + "," + (int)distanceX + "," + (int)distanceY + "\n");
 
 			return true;
 		}
 
 		public void onShowPress(MotionEvent e) {
 			Log.v(TAG, "onShowPress");
 		}
 
 		public boolean onSingleTapUp(MotionEvent e) {
 			return true;
 		}
 		
 		public boolean onSingleTapConfirmed(MotionEvent e) {
 			Log.v(TAG, "onSingleTapConfirmed");
 			mChat.sendMessage(TAG_TAP + "\n");
 			return true;
 		}
 
     	
     });
     
     private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
 
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			ListView listView = (ListView)arg0;
 			String item = (String) listView.getItemAtPosition(arg2);
 //			mChat.sendMessage(TAG_SEARCH + "," + i + "," + res + "\n");	
 			mChat.sendMessage(TAG_SEARCH + "," + 0 + "," + item + "\n");	
 			Log.v(TAG, "onItemClick : " + item);
 		}
     	
     };
     
 }

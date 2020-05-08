 package edu.dartmouth.cs.audiorecorder.analytics;
 
 import edu.dartmouth.cs.audiorecorder.AudioRecorderService;
 import edu.dartmouth.cs.audiorecorder.R;
 import edu.dartmouth.cs.audiorecorder.SensorPreferenceActivity;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * This is the analytic portion of StressSense.
  * 
  * Displays: Status of the current processed audio History of the previous
  * processed audio statuses
  */
 public class StressActivity extends Activity {
 
 	// Layout Components
 	private TextView mTvGenericText;
 	private ArrayAdapter<String> mAdapter;
 
 	// Used for status writing
 	private static Handler sMessageHandler;
 
 	// BroadcastReceiver for getting On/Off signals from the service
 	private AudioRecorderStatusRecevier mAudioRecorderStatusReceiver;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_analytic);
 
 		mAudioRecorderStatusReceiver = new AudioRecorderStatusRecevier();
 
 		mTvGenericText = (TextView) findViewById(R.id.tvStatus);
 
 		ListView myListView = (ListView) findViewById(R.id.list);
 		mAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1,
 				AudioRecorderService.changeHistory);
 		myListView.setAdapter(mAdapter);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		registerReceiver(mAudioRecorderStatusReceiver, new IntentFilter(
 				AudioRecorderService.AUDIORECORDER_ON));
 		registerReceiver(mAudioRecorderStatusReceiver, new IntentFilter(
 				AudioRecorderService.AUDIORECORDER_OFF));
 		sMessageHandler = mHandler;
 		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
 				SensorPreferenceActivity.IS_ON, false)) {
 			mTvGenericText.setCompoundDrawablesWithIntrinsicBounds(
 					R.drawable.mic_on, 0, 0, 0);
			mTvGenericText.setText(": " + AudioRecorderService.changeHistory.get(0).split(": ")[1]);
 		}
 		mAdapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		unregisterReceiver(mAudioRecorderStatusReceiver);
 		sMessageHandler = null;
 		mTvGenericText.setCompoundDrawablesWithIntrinsicBounds(
 				R.drawable.mic_off, 0, 0, 0);
 		mTvGenericText.setText("");
 	}
 
 	/*--------------------------------BROADCASTRECEIVERS--------------------------------*/
 
 	/**
 	 * Listens for the On/Off signals given by AudioRecorderService and displays
 	 * accordingly
 	 */
 	class AudioRecorderStatusRecevier extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (intent.getAction()
 					.equals(AudioRecorderService.AUDIORECORDER_ON)) {
 				mTvGenericText.setCompoundDrawablesWithIntrinsicBounds(
 						R.drawable.mic_on, 0, 0, 0);
				mTvGenericText.setText(": " + AudioRecorderService.changeHistory.get(0).split(": ")[1]);
 			}
 			else if (intent.getAction().equals(
 					AudioRecorderService.AUDIORECORDER_OFF)) {
 				mTvGenericText.setCompoundDrawablesWithIntrinsicBounds(
 						R.drawable.mic_off, 0, 0, 0);
 				mTvGenericText.setText("");
 			}
 		}
 	}
 
 	/*-------------------------------HANDLER FUNCTIONALITY-------------------------------*/
 
 	/**
 	 * Receives data from setActivityText() in RehearsalAudioRecorder and
 	 * displays it to the user
 	 * 
 	 * If nothing was added to the list in the previous minute, then the current
 	 * status is added, with the list storing a maximum of 10 statuses
 	 */
 
 	Handler mHandler = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			mTvGenericText.setText(": " + msg.getData().getString(
 					AudioRecorderService.AUDIORECORDER_NEWTEXT_CONTENT));
 
 			mAdapter.notifyDataSetChanged();
 		}
 	};
 
 	public static Handler getHandler() {
 		return sMessageHandler;
 	}
 }

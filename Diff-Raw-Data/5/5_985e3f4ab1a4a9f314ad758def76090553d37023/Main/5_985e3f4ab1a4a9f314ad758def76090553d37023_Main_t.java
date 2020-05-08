 package pro.schmid.android.solde;
 
 import java.text.SimpleDateFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Main extends Activity {
 
 	private final int WAITING_TIME = 15000;
 
 	private SmsManager smsManager;
 
 	private SimpleDateFormat sdf;
 
 	private IntentFilter mIntentFilter;
 
 	private Progress progress;
 
 	private Main me;
 
 	private ContentMatcher smsMatcher;
 	private ContentMatcher dataMatcher;
 	private ContentMatcher voiceMatcher;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		me = this;
 
 		mIntentFilter = new IntentFilter();
 		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
 		mIntentFilter.setPriority(200);
 
 		sdf = new SimpleDateFormat();
 		sdf.applyPattern(getResources().getString(R.string.date_format));
 
 		smsManager = SmsManager.getDefault();
 
 		setupView();
 	}
 
 	@Override
 	protected void onResume() {
 		registerReceiver(mIntentReceiver, mIntentFilter);
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		unregisterReceiver(mIntentReceiver);
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 
 		case R.id.menu_recommend:
 			share();
 			break;
 
 		case R.id.menu_rate:
 			rate();
 			break;
 
 		case R.id.menu_about:
 			about();
 			break;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 	private void share() {
 		final Intent intent = new Intent(Intent.ACTION_SEND);
 
 		intent.setType("text/plain");
 		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.recommand_title));
 		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.recommand_text));
 
 		startActivity(Intent.createChooser(intent, getString(R.string.recommand_share_title)));
 	}
 
 	private void rate() {
 		try {
 			Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=pro.schmid.android.solde"));
 			startActivity(goToMarket);
 		} catch (Exception e) {
 			Toast.makeText(this, R.string.no_market, Toast.LENGTH_LONG);
 		}
 	}
 
 	private void about() {
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle(R.string.about_title);
 		b.setMessage(R.string.about_text);
 		b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		b.create().show();
 	}
 
 	private void setupView() {
 		final Button button = (Button) findViewById(R.id.requestButton);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//				requestInfo();
 
 				ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
 				pb.setVisibility(View.VISIBLE);
 
 				// Move the button
 				Animation anim = AnimationUtils.loadAnimation(getApplication(), R.anim.move_button_go);
 				anim.setAnimationListener(new AnimationListener() {
 
 					@Override
 					public void onAnimationStart(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationRepeat(Animation animation) {
 					}
 
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						button.setEnabled(false);
 					}
 				});
 				button.startAnimation(anim);
 
 				progress = new Progress((ProgressBar) findViewById(R.id.progressBar), WAITING_TIME);
 				progress.execute(new Void[0]);
 
 				debug();
 			}
 		});
 	}
 
 	private void requestInfo() {
 		smsManager.sendTextMessage(getResources().getString(R.string.sms_number), null, "solde", null, null);
 	}
 
 	private void debug() {
 		parseData(Debug.DEBUG_SMS);
 		parseData(Debug.DEBUG_DATA);
 		parseData(Debug.DEBUG_VOICE);
 	}
 
 	private void moveButtonBack() {
 		final Button button = (Button) findViewById(R.id.requestButton);
 		button.setEnabled(true);
 		Animation anim = AnimationUtils.loadAnimation(getApplication(), R.anim.move_button_back);
 		anim.setAnimationListener(new AnimationListener() {
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
 				pb.setProgress(0);
 				pb.setVisibility(View.VISIBLE);
 			}
 		});
 		button.startAnimation(anim);
 	}
 
 	private void parseData(String data) {
 
 		progress.cancel(true);
 		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
 
 		smsMatcher = new SmsMatcher(data);
 		dataMatcher = new DataMatcher(data);
 		voiceMatcher = new VoiceMatcher(data);
 
 		if (smsMatcher.isValid()) {
 			TextView tv = (TextView) findViewById(R.id.sms_monthly_credit_value);
 			tv.setText(String.valueOf(smsMatcher.getTotalCredits()));
 
 			tv = (TextView) findViewById(R.id.sms_remaining_credit_value);
 			tv.setText(String.valueOf(smsMatcher.getRemainingCredits()));
 
 			tv = (TextView) findViewById(R.id.sms_remaining_credit_per_day_value);
 			tv.setText(String.valueOf(smsMatcher.getRemainingPerDay()));
 
 			tv = (TextView) findViewById(R.id.sms_valid_until_value);
 			tv.setText(sdf.format(smsMatcher.getValidUntil().getTime()));
 
 			tv = (TextView) findViewById(R.id.sms_state_date_value);
 			tv.setText(sdf.format(smsMatcher.getStateDate().getTime()));
 
 			LinearLayout ll = (LinearLayout) findViewById(R.id.sms_layout_ref);
 			ll.setVisibility(View.VISIBLE);
 			pb.setVisibility(View.GONE);
 			return;
 		}
 
 		if (dataMatcher.isValid()) {
 			TextView tv = (TextView) findViewById(R.id.data_monthly_credit_value);
 			String merged = String.format(getResources().getString(R.string.data_unit), dataMatcher.getTotalCredits());
 			tv.setText(merged);
 
 			tv = (TextView) findViewById(R.id.data_remaining_credit_value);
 			merged = String.format(getResources().getString(R.string.data_unit), dataMatcher.getRemainingCredits());
 			tv.setText(merged);
 
 			tv = (TextView) findViewById(R.id.data_remaining_credit_per_day_value);
 			merged = String.format(getResources().getString(R.string.data_unit), dataMatcher.getRemainingPerDay());
 			tv.setText(merged);
 
 			tv = (TextView) findViewById(R.id.data_valid_until_value);
 			tv.setText(sdf.format(dataMatcher.getValidUntil().getTime()));
 
 			tv = (TextView) findViewById(R.id.data_state_date_value);
 			tv.setText(sdf.format(dataMatcher.getStateDate().getTime()));
 
 			LinearLayout ll2 = (LinearLayout) findViewById(R.id.data_layout_ref);
 			ll2.setVisibility(View.VISIBLE);
 			pb.setVisibility(View.GONE);
 			return;
 		}
 
 		if (voiceMatcher.isValid()) {
 			TextView tv = (TextView) findViewById(R.id.voice_monthly_credit_value);
 			String merged = String.format(getResources().getString(R.string.voice_unit), voiceMatcher.getTotalCredits());
 			tv.setText(merged);
 
 			tv = (TextView) findViewById(R.id.voice_remaining_credit_value);
 			merged = String.format(getResources().getString(R.string.voice_unit), voiceMatcher.getRemainingCredits());
 			tv.setText(merged);
 
 			tv = (TextView) findViewById(R.id.voice_remaining_credit_per_day_value);
 			merged = String.format(getResources().getString(R.string.voice_unit), voiceMatcher.getRemainingPerDay());
 			tv.setText(merged);
 
 			tv = (TextView) findViewById(R.id.voice_valid_until_value);
 			tv.setText(sdf.format(voiceMatcher.getValidUntil().getTime()));
 
 			tv = (TextView) findViewById(R.id.voice_state_date_value);
 			tv.setText(sdf.format(voiceMatcher.getStateDate().getTime()));
 
 			LinearLayout ll3 = (LinearLayout) findViewById(R.id.voice_layout_ref);
 			ll3.setVisibility(View.VISIBLE);
 			pb.setVisibility(View.GONE);
 			return;
 		}
 
 		// Nobody catched the message
 		AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setTitle(R.string.error_wrongsms_title);
 		String text = String.format(getString(R.string.error_wrongsms_text), data);
 		b.setMessage(text);
 		b.setPositiveButton(R.string.too_bad, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				moveButtonBack();
 			}
 		});
 		b.create().show();
 
 	}
 
 	private class Progress extends AsyncTask<Void, Void, Void> {
 
 		private final ProgressBar bar;
 		private final int waitingTime;
 		private final int interval = 500;
 		private int currentTime = 0;
 
 		public Progress(ProgressBar bar, int waitingTime) {
 			this.bar = bar;
 			this.waitingTime = waitingTime;
 			bar.setMax(waitingTime);
 		}
 
 		@Override
 		protected void onProgressUpdate(Void... values) {
 			bar.setProgress(currentTime);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 
 			while (currentTime < waitingTime) {
 				if (isCancelled()) {
 					return null;
 				}
 
 				try {
 					Thread.sleep(interval);
 				} catch (InterruptedException e) {
 				}
 				currentTime += interval;
 				publishProgress(new Void[0]);
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {

			if (result == null || isFinishing()) {
				return;
			}

 			AlertDialog.Builder builder = new AlertDialog.Builder(me);
 			builder.setTitle(R.string.error_nosms_title);
 			builder.setMessage(R.string.error_nosms_text);
 			builder.setPositiveButton(R.string.too_bad, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.dismiss();
 					moveButtonBack();
 				}
 			});
 			builder.create().show();
 		}
 
 	}
 
 	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			Log.d("MESSAGE", "Recu");
 
 			dispatch(context, intent);
 		}
 
 		private void dispatch(Context context, Intent intent) {
 			Bundle bundle = intent.getExtras();
 
 			if (bundle == null) {
 				return;
 			}
 
 			Object[] pdus = (Object[]) bundle.get("pdus");
 
 			if (pdus.length != 1) {
 				return;
 			}
 
 			SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdus[0]);
 
 			String number = context.getResources().getString(R.string.sms_number);
 
 			if (!number.equals(msg.getOriginatingAddress())) {
 				return;
 			}
 
 			String txt = msg.getDisplayMessageBody();
 			Intent i = new Intent();
 			i.putExtra("content", txt);
 
 			Toast.makeText(context, txt, Toast.LENGTH_LONG);
 
 			parseData(txt);
 
 			this.abortBroadcast();
 		}
 	};
 }

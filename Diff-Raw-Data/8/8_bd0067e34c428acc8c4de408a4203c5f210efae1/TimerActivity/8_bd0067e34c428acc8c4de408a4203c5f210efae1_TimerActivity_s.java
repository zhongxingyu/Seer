 package ch.almana.android.stillmeter.view.activity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import ch.almana.android.stillmeter.helper.Formater;
 import ch.almana.android.stillmeter.helper.Settings;
 import ch.almana.android.stillmeter.model.BreastModel.Position;
 import ch.almana.android.stillmeter.model.SessionModel;
 import ch.almana.android.stillmeter.provider.db.DB.Session;
 import ch.almana.android.stillmeter.provider.db.DB.StillTime;
 import ch.almana.android.stilltimer.R;
 
 public class TimerActivity extends Activity {
 
 	private Button buLeft;
 	private Button buRight;
 	private TextView tvLeft;
 	private TextView tvRight;
 	private TextView tvTotal;
 
 	private static SessionModel sessionModel;
 	private static WakeLock wakeLock = null;
 
 	private Handler handler;
 
 	private final Runnable updateView = new Runnable() {
 		@Override
 		public void run() {
 			updateView();
 		}
 	};
 	private TextView tvLastSession;
 	private TextView tvLastSessionDuration;
 	private TextView tvLastBreast;
 	private TextView tvLastBreastTime;
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		sessionModel.saveInstanceState(outState);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		sessionModel = new SessionModel(savedInstanceState);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.timer);
 
 		if (sessionModel == null) {
 			sessionModel = new SessionModel(this);
 		}
 		handler = new Handler();
 
 		buLeft = (Button) findViewById(R.id.buLeft);
 		buRight = (Button) findViewById(R.id.buRight);
 		tvLeft = (TextView) findViewById(R.id.tvLeft);
 		tvRight = (TextView) findViewById(R.id.tvRight);
 		tvTotal = (TextView) findViewById(R.id.tvTotal);
 		tvLastSession = (TextView) findViewById(R.id.tvLastSession);
 		tvLastSessionDuration = (TextView) findViewById(R.id.tvLastSessionDuration);
 		tvLastBreast = (TextView) findViewById(R.id.tvLastBreast);
 		tvLastBreastTime = (TextView) findViewById(R.id.tvLastBreastTime);
 
 		buLeft.setHeight(buLeft.getWidth());
 		buRight.setHeight(buRight.getWidth());
 
 		buLeft.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				buttonClick(Position.left);
 			}
 		});
 
 		buRight.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				buttonClick(Position.right);
 			}
 		});
 
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (sessionModel.isTooOld()) {
 			sessionModel = new SessionModel(this);
 		}
 		Settings settings = Settings.getInstance(this);
 		buLeft.setTextColor(settings.getLeftColor());
 		buRight.setTextColor(settings.getRightColor());
 		updateView();
 		runTimerHandler();
 	}
 
 	@Override
 	protected void onPause() {
 		handler.removeCallbacks(updateView);
 		super.onPause();
 	}
 
 	protected void updateView() {
 		buRight.setText(R.string.right);
 		buLeft.setText(R.string.left);
 		switch (sessionModel.getPossition()) {
 		case left:
 			buLeft.setText(R.string.buLeftOn);
 			break;
 		case right:
 			buRight.setText(R.string.buRightOn);
 			break;
 		}
 		long l = sessionModel.getLeftTime();
 		long r = sessionModel.getRightTime();
 		long t = sessionModel.getTotalTime();
 		updateTextView(tvLeft, l);
 		updateTextView(tvRight, r);
 		updateTextView(tvTotal, t);
 		long sessionStartTime = sessionModel.getStartTime();
 		Cursor sessionCursor = null;
 		try {
			sessionCursor = getContentResolver().query(Session.CONTENT_URI, Session.PROJECTION_DEFAULT, null, null, Session.SORTORDER_DEFAULT);
 			boolean notFound = true;
 			while (notFound && sessionCursor.moveToNext()) {
 				if (sessionCursor.getLong(Session.INDEX_TIME_START) < sessionStartTime
 						&& sessionCursor.getLong(Session.INDEX_TIME_END) > -1) {
 					tvLastSession.setText(Formater.sessionTime(sessionCursor));
 					tvLastSessionDuration.setText(Formater.timeElapsed(sessionCursor.getLong(Session.INDEX_TOTAL_TIME)));
 
 					Cursor timerCursor = null;
 					try {
 						timerCursor = getContentResolver().query(StillTime.CONTENT_URI, StillTime.PROJECTION_DEFAULT, null, null, StillTime.SORTORDER_DEFAULT);
 						if (timerCursor.moveToFirst()) {
 							tvLastBreast.setText(Formater.translatedBreast(this, timerCursor.getString(StillTime.INDEX_BREAST)));
 							long bt = timerCursor.getLong(StillTime.INDEX_TIME_END) - timerCursor.getLong(StillTime.INDEX_TIME_START);
 							tvLastBreastTime.setText(Formater.timeElapsed(bt));
 							notFound = false;
 						}
 					} finally {
 						if (timerCursor != null) {
 							timerCursor.close();
 						}
 					}
 				}
 			}
 		} finally {
 			if (sessionCursor != null) {
 				sessionCursor.close();
 			}
 		}
 		runTimerHandler();
 	}
 
 	protected void updateTextView(TextView tv, long time) {
 		if (time < 500) {
 			return;
 		}
 		tv.setText(Formater.timeElapsed(time));
 	}
 
 	protected void buttonClick(Position pos) {
 		if (sessionModel.isTooOld()) {
 			sessionModel = new SessionModel(this);
 		}
 		if (pos != sessionModel.getPossition()) {
 			sessionModel.startFeeding(this, pos);
 		} else {
 			sessionModel.endFeeding(this, pos);
 		}
 		if (sessionModel.getPossition() == Position.none) {
 			releaseWakelock();
 		} else {
 			acquireWakelock();
 		}
 		updateView();
 	}
 
 	private void runTimerHandler() {
 		handler.postDelayed(updateView, 1000);
 	}
 
 	private void acquireWakelock() {
 		int wakelockType = Settings.getInstance(this).getWakelockType();
 		if (wakelockType != Settings.NO_WAKELOCK) {
 			PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
 			wakeLock = pm.newWakeLock(wakelockType, "stilltimer");
 			wakeLock.acquire();
 		} else {
 			releaseWakelock();
 		}
 	}
 
 	private void releaseWakelock() {
 		if (wakeLock != null) {
 			wakeLock.release();
 			wakeLock = null;
 		}
 	}
 }

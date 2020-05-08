 package dk.noitso.vaerloesefh.views;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import dk.noitso.vaerloesefh.R;
 
 public class StopwatchFragment extends Fragment implements OnClickListener {
 	public static final String ARG_SECTION_NUMBER = "section_number";
	private Context context;
 	
 	private TextView timerMsTextView, timerTextView; // Temporary TextView
 	private Button lapButton, resetButton, startButton, stopButton; // Temporary Button
 	private Handler mHandler = new Handler();
 	private long startTime;
 	private long elapsedTime;
 	private final int REFRESH_RATE = 10;
 	private String hours, minutes, seconds, milliseconds;
 	private long secs, mins, hrs, msecs;
 	private boolean stopped = false;
 	private View v;
 	
 	public StopwatchFragment() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {		
 		
 		v = inflater.inflate(R.layout.stopwatch_layout, container, false);
				
		Typeface font = Typeface.createFromAsset(context.getAssets(), "altehaasgroteskbold.ttf");
 		timerTextView = (TextView) v.findViewById(R.id.timer);
 		timerTextView.setTypeface(font);
 		timerMsTextView = (TextView) v.findViewById(R.id.timerMs);
 		timerMsTextView.setTypeface(font);
		font = Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf");
 		startButton = (Button) v.findViewById(R.id.startButton);
 		startButton.setTypeface(font);
 		startButton.setOnClickListener(this);
 		resetButton = (Button) v.findViewById(R.id.resetButton);
 		resetButton.setTypeface(font);
 		resetButton.setOnClickListener(this);
 		stopButton = (Button) v.findViewById(R.id.stopButton);
 		stopButton.setTypeface(font);
 		stopButton.setOnClickListener(this);
 		lapButton = (Button) v.findViewById(R.id.lapButton);
 		lapButton.setTypeface(font);
 		lapButton.setOnClickListener(this);
 		return v;
 	}
 
 	private void showStopButton() {
 		startButton.setVisibility(View.GONE);
 		resetButton.setVisibility(View.GONE);
 		stopButton.setVisibility(View.VISIBLE);
 		lapButton.setVisibility(View.VISIBLE);
 	}
 
 	private void hideStopButton() {
 		startButton.setVisibility(View.VISIBLE);
 		resetButton.setVisibility(View.VISIBLE);
 		stopButton.setVisibility(View.GONE);
 		lapButton.setVisibility(View.GONE);
 	}
 
 	private void updateTimer(float time) {
 		secs = (long) (time / 1000);
 		mins = (long) ((time / 1000) / 60);
 		hrs = (long) (((time / 1000) / 60) / 60);
 
 		/*
 		 * Convert the seconds to String and format to ensure it has a leading
 		 * zero when required
 		 */
 		secs = secs % 60;
 		seconds = String.valueOf(secs);
 		if (secs == 0) {
 			seconds = "00";
 		}
 		if (secs < 10 && secs > 0) {
 			seconds = "0" + seconds;
 		}
 
 		/* Convert the minutes to String and format the String */
 
 		mins = mins % 60;
 		minutes = String.valueOf(mins);
 		if (mins == 0) {
 			minutes = "00";
 		}
 		if (mins < 10 && mins > 0) {
 			minutes = "0" + minutes;
 		}
 
 		/* Convert the hours to String and format the String */
 
 		hours = String.valueOf(hrs);
 		if (hrs == 0) {
 			hours = "00";
 		}
 		if (hrs < 10 && hrs > 0) {
 			hours = "0" + hours;
 		}
 
 		/*
 		 * Although we are not using milliseconds on the timer in this example I
 		 * included the code in the event that you wanted to include it on your
 		 * own
 		 */
 		milliseconds = String.valueOf((long) time);
 		if (milliseconds.length() == 2) {
 			milliseconds = "0" + milliseconds;
 		}
 		if (milliseconds.length() <= 1) {
 			milliseconds = "00";
 		}
 		milliseconds = milliseconds.substring(milliseconds.length() - 3,
 				milliseconds.length() - 2);
 
 		/* Setting the timer text to the elapsed time */
 		((TextView) v.findViewById(R.id.timer)).setText(hours + ":" + minutes
 				+ ":" + seconds);
 		((TextView) v.findViewById(R.id.timerMs)).setText("." + milliseconds);
 	}
 
 	private Runnable startTimer = new Runnable() {
 		public void run() {
 			elapsedTime = System.currentTimeMillis() - startTime;
 			updateTimer(elapsedTime);
 			mHandler.postDelayed(this, REFRESH_RATE);
 		}
 	};
 
 	public void onClick(View v) {
 		switch(v.getId()) {
 		case R.id.startButton:
 			showStopButton();
 			if (stopped) {
 				startTime = System.currentTimeMillis() - elapsedTime;
 			} else {
 				startTime = System.currentTimeMillis();
 			}
 			mHandler.removeCallbacks(startTimer);
 			mHandler.postDelayed(startTimer, 0);
 			break;
 		case R.id.stopButton:
 			hideStopButton();
 			mHandler.removeCallbacks(startTimer);
 			stopped = true;
 			break;
 		case R.id.resetButton:
 			stopped = false;
 			timerTextView.setText("00:00:00");
 			timerMsTextView.setText(".0");
 			break;
 		case R.id.lapButton:
 			// Set time.
 			break;
 		default:
 			break;
 		}
 	}
 }

 package jp.torifuku.lifemesure;
 
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import jp.torifuku.lifemeasure.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.appwidget.AppWidgetManager;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.RemoteViews;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class MainActivity extends Activity {
 	/** UI */
 	private DatePicker mDatePicker = null;
 	private TimePicker mTimePicker = null;
 	private TextView mResultTextView = null;
 	private Spinner mPatternSpinner = null;
 	private Button mShareButton = null;
 	private TextView mEtoTextView = null;
 	
 	/** */
 	private SharedPreferences mPref = null;
 	private BroadcastReceiver mReceiver = null;
 	private Utility mUtility = null;
 	private boolean mShowResultStarted = false;
 	
 	/** DATA */
 	static final String KEY_YEAR = "year";
 	static final String KEY_MONTH = "month";
 	static final String KEY_DAY_OF_MONTH = "day_of_month";
 	static final String KEY_HOUR = "hour";
 	static final String KEY_MINUTE = "minute";
 	private static final String KEY_SAVED = "saved";
 	private static final String KEY_PATTERN = "pattern";
 	private int mYear;
 	private int mMonth;
 	private int mDayOfMonth;
 	private int mHour;
 	private int mMinute;
 	private int mPattern;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
        this.setContentView();
         
         mUtility = new Utility();
     }
     
    private void setContentView() {
     	super.setContentView(R.layout.activity_main);
     	
     	/** 設定値を取得 */
    	mPref = super.getSharedPreferences(super.getPackageName(), Context.MODE_PRIVATE);
    	mYear = mPref.getInt(KEY_YEAR, 1970);
    	mMonth = mPref.getInt(KEY_MONTH, 0);
    	mDayOfMonth = mPref.getInt(KEY_DAY_OF_MONTH, 1);
    	mHour = mPref.getInt(KEY_HOUR, 0);
    	mMinute = mPref.getInt(KEY_MINUTE, 0);
    	mShowResultStarted = mPref.getBoolean(KEY_SAVED, false);
    	mPattern = mPref.getInt(KEY_PATTERN, 0);
     	
     	/** DatePicker */
     	mDatePicker = (DatePicker) super.findViewById(R.id.datePicker);
     	mDatePicker.init(mYear, mMonth, mDayOfMonth, new DatePicker.OnDateChangedListener() {
 			@Override
 			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
 				MainActivity.this.mYear = year;
 				MainActivity.this.mMonth = monthOfYear;
 				MainActivity.this.mDayOfMonth = dayOfMonth;
 				MainActivity.this.showResult();
 				MainActivity.this.showEto();
 				MainActivity.this.dataChanged();
 			}});
     	
     	/** TimePicker */
     	mTimePicker = (TimePicker) super.findViewById(R.id.timePicker);
     	mTimePicker.setIs24HourView(true);
     	mTimePicker.setCurrentHour(mHour);
     	mTimePicker.setCurrentMinute(mMinute);
     	mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
 			@Override
 			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
 				MainActivity.this.mHour = hourOfDay;
 				MainActivity.this.mMinute = minute;
 				MainActivity.this.showResult();
 				MainActivity.this.dataChanged();
 			}});
     	
     	/** Spinner */
     	Locale locale = Locale.getDefault();
     	if (locale.equals(Locale.JAPAN) || locale.equals(Locale.JAPANESE)) {
     		mPatternSpinner = (Spinner) super.findViewById(R.id.japan_phrase_spinner);
     		mPatternSpinner.setVisibility(View.VISIBLE);
     		mPatternSpinner.setSelection(mPattern);
 			mPatternSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 				@Override
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					if (mPattern != arg2) {
 						mPattern = arg2;
 						MainActivity.this.showResult();
 					}
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 				}});
     	}
     	
     	/** TextView */
     	mResultTextView = (TextView) super.findViewById(R.id.result_textView);
     	mEtoTextView = (TextView) super.findViewById(R.id.eto_textView);
     	if (!locale.equals(Locale.JAPAN) && !locale.equals(Locale.JAPANESE)) {
     		mEtoTextView.setVisibility(View.GONE);
     	}
     	
     	/** Button */
     	Button updateButton = (Button) super.findViewById(R.id.update_button);
     	updateButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				MainActivity.this.showResult();
 				MainActivity.this.startAutoCalc();
 			}});
     	
     	mShareButton = (Button) super.findViewById(R.id.share_button);
     	if (!mShowResultStarted) {
     		mShareButton.setEnabled(false);
     	} else {
     		mShareButton.setEnabled(true);
     	}
     	mShareButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				MainActivity.this.share();
 			}});
     	
     	/** データはかわっていないけれど、初期化のため、ここで呼び出しておく */
     	dataChanged();
     }
     
     @Override
     protected void onResume() {
     	super.onResume();
     	if (mShowResultStarted) {
     		showResult();
     	}
     }
     
     @Override
 	public void onConfigurationChanged(Configuration newConfig) {
     	super.onConfigurationChanged(newConfig);
     	
    	this.setContentView();
     }
     
     @Override
     protected void onPause() {
     	super.onPause();
     	mPref.edit()
     	.putInt(KEY_YEAR, mYear)
     	.putInt(KEY_MONTH, mMonth)
     	.putInt(KEY_DAY_OF_MONTH, mDayOfMonth)
     	.putInt(KEY_HOUR, mHour)
     	.putInt(KEY_MINUTE, mMinute)
     	.putBoolean(KEY_SAVED, mShowResultStarted)
     	.putInt(KEY_PATTERN, mPattern)
     	.commit();
     	
     	/** Widget更新 */
     	AppWidgetManager awm = AppWidgetManager.getInstance(this);
     	RemoteViews views = mUtility.createRemoteViews(this);
     	awm.updateAppWidget(new ComponentName(this, LifeMeasureAppWidgetProvider.class), views);
     	//Intent widgetUpdate = new Intent();
     	//widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
     	//super.sendBroadcast(widgetUpdate);
     }
     
     @Override
     protected void onDestroy() {
     	if (mReceiver != null) {
     		super.unregisterReceiver(mReceiver);
     	}
     	super.onDestroy();
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
     	if (outState != null) {
     		outState.putBoolean("show result stated?", mShowResultStarted);
     	}
     	super.onSaveInstanceState(outState);
     }
     
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     	if (savedInstanceState != null) {
     		mShowResultStarted = savedInstanceState.getBoolean("show result stated?");
     	}
     }
     
     
     private void startAutoCalc() {
     	if (mReceiver != null) {
     		return;
     	}
     	
     	mReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				MainActivity.this.showResult();
 			}
     	};
     	IntentFilter filter = new IntentFilter();
     	filter.addAction(Intent.ACTION_TIME_TICK);
 		super.registerReceiver(mReceiver, filter);
     }
     
     private void showResult() {
     	mShowResultStarted = true;
     	
 		String result = createMessage();
 		mResultTextView.setText(result);
 		
 		mShareButton.setEnabled(true);
 		startAutoCalc();
 		
 		showEto();
     }
     
     private void showEto() {
     	String[] etoArray = { "申", "酉", "戌", "亥", "子", "丑", "寅", "卯", "辰", "巳", "午", "未" };
     	int rem = mYear % 12;
     	this.mEtoTextView.setText(etoArray[rem]);
     }
     
     private void share() {
     	String msg = createMessage();
     	
     	Intent intent = new Intent(android.content.Intent.ACTION_SEND);
 	    intent.setType("text/plain");
 	    intent.putExtra(Intent.EXTRA_TEXT, msg);
 	    
 	    super.startActivity(Intent.createChooser(intent, "Please select..."));
     }
     
     private void dataChanged() {
     	GregorianCalendar baseDate = new GregorianCalendar(
 				mYear,
 				mMonth,
 				mDayOfMonth,
 				mHour,
 				mMinute);
     	GregorianCalendar now = new GregorianCalendar();
     	int comp = now.compareTo(baseDate);
     	boolean enabled = true;
     	if (comp <= -1) {
     		enabled = false;
     	}
     	if (mPatternSpinner != null) {
     		mPatternSpinner.setEnabled(enabled);
     	}
     }
     
     private String createMessage() {
     	GregorianCalendar baseDate = new GregorianCalendar(
 				mYear,
 				mMonth,
 				mDayOfMonth,
 				mHour,
 				mMinute);
     	
     	return mUtility.createMsg(baseDate, this.getApplicationContext(), mPattern);
     	
 		/*
 		sb.append(MainActivity.this.getString(R.string.result_header));
 		sb.append("\n");
 		sb.append("\n");
 		sb.append(aliveDay);
 		sb.append("[days]");
 		sb.append("\n");
 		sb.append(aliveHour);
 		sb.append("[hours]");
 		sb.append("\n");
 		sb.append(aliveMinute);
 		sb.append("[minutes]");
 		sb.append("\n");
 		sb.append(aliveSecond);
 		sb.append("[seconds]");
 		sb.append("\n");
 		sb.append("恥の多い生涯");
 		sb.append("(");
 		sb.append(aliveDay);
 		sb.append("日と");
 		sb.append(aliveHour);
 		sb.append("時間");
 		sb.append(aliveMinute);
 		sb.append("分");
 		sb.append(aliveSecond);
 		sb.append("秒");
 		sb.append(")");
 		sb.append("を送って来ました。");
 		
 		return sb.toString();*/
     }
 }

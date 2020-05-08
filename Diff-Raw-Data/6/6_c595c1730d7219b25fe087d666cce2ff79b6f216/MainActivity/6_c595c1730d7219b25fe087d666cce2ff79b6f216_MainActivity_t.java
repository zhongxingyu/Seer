 package ca.keithzg.paulmiller.is.offline;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 //import android.util.Log;
 
 @SuppressLint("ValidFragment")
 public class MainActivity extends FragmentActivity {
 
 	static String paulTimeLeft;
 	static long days, hours, minutes, seconds;
 	//static TextView tv;
 
 	long milliseconds;
 
 	private ViewPager mViewPager;
 	private MyFragmentPagerAdapter mMyFragmentPagerAdapter;
 
 	public void onCreate(Bundle savedInstanceState) {
 
 		Date internetTimeForPaul = new Date("05/01/2013 00:00:00");
 		milliseconds = internetTimeForPaul.getTime();
 
 		MyCount counter = new MyCount(milliseconds,1000);
 		counter.start();
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		mViewPager = (ViewPager) findViewById(R.id.viewpager);
 		mMyFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
 		mViewPager.setAdapter(mMyFragmentPagerAdapter);
 		mViewPager.setCurrentItem(0);
 	}
 
 	private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {
 
 		private static final int NUMBER_OF_PAGES = 2;
 
 		public MyFragmentPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		public Fragment getItem(int page) {
 			switch (page) {
 				case 0: return new Fragment1();
 				default: return new Fragment2();
 			}
 		}
 
 		@Override
 		public int getCount() {
 			return NUMBER_OF_PAGES;
 		}
 
 		public class Fragment1 extends Fragment {
 
 
 			public TextView mDaysView, mHoursView, mMinsView, mSecsView;
 			public Fragment1 newInstance(int index) {
 				Fragment1 f = new Fragment1();
 
 				// Supply index input as an argument.
 				Bundle args = new Bundle();
 				args.putInt("index", index);
 				f.setArguments(args);
 
 				return f;
 			}
 
 			public int getShownIndex() {
 				return getArguments().getInt("index", 0);
 			}
 
 			public class MyCount1 extends CountDownTimer {
 
 				public MyCount1(long millisInFuture, long countDownInterval) {
 					super(millisInFuture, countDownInterval);
 				}
 
 				@Override
 				public void onFinish() {
 				}
 
 				@Override
 				public void onTick(long millisUntilFinished) {
 					mHoursView.setText(String.valueOf(hours));
 					mDaysView.setText(String.valueOf(days));
 					mMinsView.setText(String.valueOf(minutes));
 					mSecsView.setText(String.valueOf(seconds));
 				}
 			}
 
 			@Override
 			public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			                         Bundle savedInstanceState) {
 				TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
 
 				java.util.Date date = new java.util.Date();
 				Calendar nowCal = Calendar.getInstance();nowCal.setTime(date);
 				Date internetTimeForPaul = new Date("05/01/2013 00:00:00");
 				long ontickseconds = internetTimeForPaul.getTime();
 
 				View v = inflater.inflate(R.layout.frag_time_left_2, null);
 				mHoursView = (TextView) v.findViewById(R.id.numHours);
 				mMinsView = (TextView) v.findViewById(R.id.numMins);
 				mSecsView = (TextView) v.findViewById(R.id.numSeconds);
 				mDaysView = (TextView) v.findViewById(R.id.numDays);
 
 				MyCount1 counter1 = new MyCount1(ontickseconds,1000);
 				counter1.start();
 
 				return v;
 			}
 
 		}
 
 		public class Fragment2 extends Fragment {
 			public Fragment2 newInstance(int index) {
 				Fragment2 f = new Fragment2();
 
 				// Supply index input as an argument.
 				Bundle args = new Bundle();
 				args.putInt("index", index);
 				f.setArguments(args);
 
 				return f;
 			}
 
 			public int getShownIndex() {
 				return getArguments().getInt("index", 0);
 			}
 
 			@Override
 			public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			                         Bundle savedInstanceState) {
 				if (container == null) {
 					// No container, no content.
 					return null;
 				}
 				View v = inflater.inflate(R.layout.frag_info, null);
 
 				return v;
 			}
 		}
 		public class Fragment3 extends Fragment {
 			public Fragment3 newInstance(int index) {
 				Fragment3 f = new Fragment3();
 
 				// Supply index input as an argument.
 				Bundle args = new Bundle();
 				args.putInt("index", index);
 				f.setArguments(args);
 
 				return f;
 			}
 
 			public int getShownIndex() {
 				return getArguments().getInt("index", 0);
 			}
 
 			public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			                         Bundle savedInstanceState) {
 				if (container == null) {
 					// No container, no content.
 					return null;
 				}
 
 				ScrollView scroller = new ScrollView(getActivity());
 				ImageView rock = new ImageView(getActivity());
 				//int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
 				//      4, getActivity().getResources().getDisplayMetrics());
 				//rock.setPadding(padding, padding, padding, padding);
 				//Resources res = getResources();
 				//Drawable rockBackground = res.getDrawable(R.drawable.pauloff_480p);
 
 				scroller.addView(rock);
 
 				rock.setBackgroundResource(R.drawable.rockdrawable);
 				return scroller;
 			}
 
 		}
 
 	}
 
 	/** Using Calendar - THE CORRECT WAY from http://tripoverit.blogspot.ca/2007/07/java-calculate-difference-between-two.html **/
 	public static long daysBetween(Calendar startDate, Calendar endDate) {
 		Calendar date = (Calendar) startDate.clone();
 		long daysBetween = 0;
 		while (date.before(endDate)) {
 			date.add(Calendar.DAY_OF_MONTH, 1);
 			daysBetween++;
 		}
 		// However, that returns one more day than we want.
 		if (daysBetween > 0){
 			daysBetween = daysBetween - 1;
 		}
 		return daysBetween;
 	}
 
 	// countdowntimer is an abstract class, so extend it and fill in methods
 	public class MyCount extends CountDownTimer {
 
 		public MyCount(long millisInFuture, long countDownInterval) {
 			super(millisInFuture, countDownInterval);
 		}
 
 		@Override
 		public void onFinish() {
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {
 
 			TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
 			DateFormat dateFormat = new SimpleDateFormat();
 			java.util.Date date = new java.util.Date();
 			Calendar nowCal = Calendar.getInstance();nowCal.setTime(date);
 
 			Date internetTimeForPaul = new Date("05/01/2013 00:00:00");
 			Calendar paulCal = Calendar.getInstance();paulCal.setTime(internetTimeForPaul);
 
 			long days = daysBetween(nowCal, paulCal);
 
 			dateFormat = new SimpleDateFormat("HH");
 			date = new java.util.Date();
 			int hours = Math.abs(Integer.parseInt(dateFormat.format(date)) - 24);
 			if (hours == 24) {
 				hours = 0;
 				days++;
 			}
			if (hours != 0 ) {hours = hours - 1;}
 
 			dateFormat = new SimpleDateFormat("mm");
 			date = new java.util.Date();
 			int minutes = Math.abs(Integer.parseInt(dateFormat.format(date)) - 60);
 			if (minutes == 60) {
 				minutes = 0;
 			}
 
 			dateFormat = new SimpleDateFormat("ss");
 			date = new java.util.Date();
 			int seconds = Math.abs(Integer.parseInt(dateFormat.format(date)) - 60);
 			if (seconds == 60) {
 				seconds = 0;
 			}
 			if (minutes != 0 ) {minutes = minutes - 1;}
 
 			paulTimeLeft = String.format(" %d days\n %d hours\n %d minutes\n %d seconds\n",
 					MainActivity.this.days = days,
 					MainActivity.this.hours = hours,
 					MainActivity.this.minutes = minutes,
 					MainActivity.this.seconds = seconds
 			);
 
 			//tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 58);
 			//SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyy MM dd \nhh mm ss");
 			//String testString = dateformatYYYYMMDD.format(date);
 			//tv.setText(paulTimeLeft + "\n" + testString);
 			//tv.setText(paulTimeLeft);
 			//Log.w("paulTimer: ", paulTimeLeft);
 		}
 	}
 
 	/**
 	 * Opens the Github page for the project
 	 * @param v
 	 */
 	public void openGithub(View v) {
 		startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/keithzg/paulmillerisoffline")));
 	}
 
 
 }

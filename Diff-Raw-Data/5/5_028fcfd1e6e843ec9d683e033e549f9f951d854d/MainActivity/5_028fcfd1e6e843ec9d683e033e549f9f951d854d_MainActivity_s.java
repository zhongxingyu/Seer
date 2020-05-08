 package ca.keithzg.paulmiller.is.offline;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.net.ParseException;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.app.Activity;
 import android.util.TypedValue;
 import android.widget.TextView;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 
 @SuppressLint("ValidFragment")
 public class MainActivity extends FragmentActivity {
 	
 	static String paulTimeLeft;
  	//static TextView tv;
 	
  	long milliseconds;
 
     private static final int NUMBER_OF_PAGES = 2;
 
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
     }
 
     private static class MyFragmentPagerAdapter extends FragmentPagerAdapter {
     	
     	
 
          public MyFragmentPagerAdapter(FragmentManager fm) {
               super(fm);
          }  
 
 
 
          public Fragment getItem(int page) {
              switch (page) {
                  case 0: return new Fragment1();
                  case 1: return new Fragment2();
                  case 2: return new Fragment3();
                 
              }
              return null;
          } 
 
          @Override 
          public int getCount() {
 
               return NUMBER_OF_PAGES;
          }
          
          public class Fragment1 extends Fragment { 
         	 	
         	    
         	 public TextView tv;
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
   	        	tv.setText(paulTimeLeft);
   	          }
      	    }
      	     
      	    @Override
      	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
      	            Bundle savedInstanceState) {
      	    	TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
      	        
      	        java.util.Date date = new java.util.Date();
      	        Calendar nowCal = Calendar.getInstance();nowCal.setTime(date);
 
      	        ScrollView scroller = new ScrollView(getActivity());
      	        
      	        int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
      	                4, getActivity().getResources().getDisplayMetrics());
      	        tv =  new TextView(getActivity());
      	        tv.setPadding(padding, padding, padding, padding);
      	        
      	        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 58);
      	        tv.setText("Initial text, to be replaced before you even see it.");
     	        MyCount1 counter1 = new MyCount1(300000,1000);
      	    	counter1.start();
      	        scroller.addView(tv);
      	        return scroller;
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
         	            // We have different layouts, and in one of them this
         	            // fragment's containing frame doesn't exist.  The fragment
         	            // may still be created from its saved state, but there is
         	            // no reason to try to create its view hierarchy because it
         	            // won't be displayed.  Note this is not needed -- we could
         	            // just run the code below, where we would create and return
         	            // the view hierarchy; it would just never be used.
         	            return null;
         	        }
 
         	        ScrollView scroller = new ScrollView(getActivity());
         	        TextView credits = new TextView(getActivity());
         	        int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
         	                4, getActivity().getResources().getDisplayMetrics());
         	        credits.setPadding(padding, padding, padding, padding);
         	        scroller.addView(credits);
         	        credits.setTextSize(TypedValue.COMPLEX_UNIT_SP, 58);
         	        credits.setText("Paul Miller is a journalist with The Verge\n \nKeith Z-G (poorly) coded this app \n ");
         	        return scroller;
         	    }
         	}
          public class Fragment3 extends Fragment { 
         	    	    
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
             hours = hours - 1;
 
             dateFormat = new SimpleDateFormat("mm");
             date = new java.util.Date();
             int minutes = Math.abs(Integer.parseInt(dateFormat.format(date)) - 60);
             if (minutes == 60) {
            	 minutes = 0;
            	 hours++;
             }
 
             dateFormat = new SimpleDateFormat("ss");
             date = new java.util.Date();
             int seconds = Math.abs(Integer.parseInt(dateFormat.format(date)) - 60);  
             if (seconds == 60) {
             	 seconds = 0;
             	 minutes++;
             }
             if (minutes != 0 ) {minutes = minutes - 1;}
 
             paulTimeLeft = String.format(" %d days\n %d hours\n %d minutes\n %d seconds\n",
                 days,
                 hours,
                 minutes,
                 seconds
                 );  
 
             //tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 58);
             //SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyy MM dd \nhh mm ss");
             //String testString = dateformatYYYYMMDD.format(date);
             //tv.setText(paulTimeLeft + "\n" + testString);
             //tv.setText(paulTimeLeft);
             Log.w("paulTimer: ", paulTimeLeft);
         }
     }
 
     
 }

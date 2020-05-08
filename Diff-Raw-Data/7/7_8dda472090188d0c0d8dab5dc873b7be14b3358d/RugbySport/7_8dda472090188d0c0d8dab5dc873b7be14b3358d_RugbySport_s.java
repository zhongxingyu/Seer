 package edu.umich.yourcast;
 
 import android.os.CountDownTimer;
 import android.util.Log;
 import android.widget.TextView;
 
 public class RugbySport extends Sport {
 
 	public RugbySport() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public int getPictureID() {
 		// TODO Auto-generated method stub
 		return R.drawable.rugby_field; 
 	}
 	
 	
 	@Override
 	public RugbyTimer getClock(String t) {
 		// TODO Auto-generated method stub
 		long second = 1000; 
		Log.d("MYMY", t); 
		long time = Long.getLong(t) * 1000; 
		
		// return new RugbyTimer(time, second); 
		return null; 
 	}
 	
 	public class RugbyTimer extends CountDownTimer {
 		private TextView t = null; 
 		private int total_time = 0; 
 		
 		public RugbyTimer(long total, long interval) {
 			super(total, interval); 
 		}
 		
 	     public void onTick(long millisUntilFinished) {
 	         total_time += 1; 
 	         t.setText(String.valueOf(total_time) + "'"); 
 	     }
 
 	     public void onFinish() {
 	         total_time += 1; 
 	         t.setText(String.valueOf(total_time) + "'"); 
 	     }
 	     
 	     public void setTextView(TextView t) {
 	    	this.t = t;  
 	     }
 	}
 
 	@Override
 	public SportEventTree getSportEventTree(String home_team, String away_team) {
 		// TODO Auto-generated method stub
 		return new RugbyEventTree(home_team, away_team); 
 	}
 
 	@Override
 	public int getRotatedPictureID() {
 		// TODO Auto-generated method stub
 		return R.drawable.rugby_field_rotated; 
 	}
 }

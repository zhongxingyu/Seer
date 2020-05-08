 package com.BattleBuilder;
 
 
 import com.BattleBuilder.adapter.ModelAdapter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class Intro extends Activity{
 	ProgressBar mProgress = null;
 	TextView mProgressInfo = null;
 	
 	public void onCreate(Bundle savedInstanceState){
 	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.intro);
 	    
	    mProgress = (ProgressBar)findViewById(R.id.load_progress);
 	    mProgress.setIndeterminate(true);
	    mProgressInfo = (TextView)findViewById(R.id.load_text);
 	    
         ModelAdapter.load(this);
 	}
 	
 	public void onProgressUpdate(int progress, String workingOn){
		mProgress.setIndeterminate(false);
 		mProgress.setProgress(progress);
 		mProgressInfo.setText(getString(R.string.loading) + workingOn);
 	}
 	
 	public void onLoadFinish(){
         Intent i = new Intent(this, MainScreen.class);
         startActivity(i);
 	}
 }

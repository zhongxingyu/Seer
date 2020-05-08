 package com.teamluper.luper;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.ColorFilter;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Path.Direction;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.view.View;
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.media.MediaRecorder;
 import android.media.MediaPlayer;
 import java.util.*;
 import java.io.IOException;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.teamluper.luper.AudioRecorderTest.PlayButton;
 import com.teamluper.luper.AudioRecorderTest.RecordButton;
 import com.teamluper.luper.CanvasTest.GraphicsView;
 
 
 public class ExampleProject extends SherlockActivity {
 	ActionBar AB;
 	//ProjectFragment PF;
 	
 	ColorChipView CCV;
 	ArrayList<Clip> clips = new ArrayList<Clip>();
 	TrackView TV;
 	
 	@Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         AB = getSupportActionBar();
         
         
         /* TESTS FUNCTIONALITY OF THE COLOR CHIP RENDERING*/
         
 //        CCV = new ColorChipView(this,clip1);
 //        setContentView(CCV);
         
         
         
         /* TESTS FUNCTIONALLITY OF THE TRACK VIEW RENDERING */
        Clip clip1 = new Clip(); clip1.begin = 50; clip1.end = 100; clip1.duration = 50;
         clips.add(clip1);
         TV = new TrackView(this, clips);
         setContentView(TV);
 		
         
         /*
         FragmentManager fragmentManager = getFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         PF = new ProjectFragment();
         fragmentTransaction.add(PF, "example_tag");
         fragmentTransaction.commit();
         */
         
        
    
     }
 }

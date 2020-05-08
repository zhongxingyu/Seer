 package com.siramix.phrasecraze;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.app.Activity;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class PhrasePackPurchase extends Activity {
 
   private static final String TAG = "CardPackPurchase";
 
   private List<String> mPackList;
   private List<ImageView> mPackViewList;
   private List<View> mPackLineList;
   
   /**
    * Create the packages screen from an XML layout and
    * 
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
       if (PhraseCrazeApplication.DEBUG) {
         Log.d(TAG, "onCreate()");
     }
     
     // Force volume controls to affect Media volume
     setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
     // Setup the view
     this.setContentView(R.layout.packpurchase);
     
     // Get our current context
     PhraseCrazeApplication application = (PhraseCrazeApplication) this
         .getApplication();
     GameManager game = application.getGameManager();
 
     // Populate and display list of cards
     LinearLayout packSets = (LinearLayout) findViewById(R.id.PackPurchase_PackSets);
     LinearLayout layout = new LinearLayout(this.getBaseContext());
     layout.setOrientation(LinearLayout.VERTICAL);
 
     // Iterate through all completed cards and set layout accordingly
     mPackViewList = new LinkedList<ImageView>();
     mPackLineList = new LinkedList<View>();
     mPackList = new LinkedList<String>();
     //TODO get this list from the database or marketplace if possible 
     mPackList.add("Test1");
     mPackList.add("Test2");
     mPackList.add("Test3");
     
     String packname;
     int count = 0;
 
     for (Iterator<String> it = mPackList.iterator(); it.hasNext();) {
       packname = it.next();
      
       LinearLayout line = (LinearLayout) LinearLayout.inflate(
           this.getBaseContext(), R.layout.packpurchaserow, layout);
       RelativeLayout packRow = (RelativeLayout) line.getChildAt(count);
 
       // Make every line alternating color
       if (count % 2 == 0) {
         View background = (View) packRow.getChildAt(0);
         background.setBackgroundResource(R.color.genericBG_trim);
       }
       
       // Set Pack Title
       TextView packTitle = (TextView) packRow.getChildAt(1);
       //TODO this will need to pull the string from our pack list
      packTitle.setText(it.next());
 
       // Set Row end icon
       ImageView packIcon = (ImageView) packRow.getChildAt(2);
       packIcon.setImageResource(R.drawable.logoram);
       mPackViewList.add(packIcon);
       mPackLineList.add(packRow);
       count++;
     }
     packSets.addView(layout);
   }
 }

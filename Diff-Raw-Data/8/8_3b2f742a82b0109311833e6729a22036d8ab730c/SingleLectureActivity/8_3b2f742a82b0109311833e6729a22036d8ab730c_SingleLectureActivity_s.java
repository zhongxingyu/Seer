 package com.gdg.andconlab.ui;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.gdg.andconlab.DBUtils;
 import com.gdg.andconlab.DatabaseHelper;
 import com.gdg.andconlab.R;
 import com.gdg.andconlab.R.id;
 import com.gdg.andconlab.R.layout;
 import com.gdg.andconlab.cache.ImageManager;
 import com.gdg.andconlab.cache.ImageManager.ImageType;
 import com.gdg.andconlab.models.Lecture;
 import com.gdg.andconlab.models.Speaker;
 import com.gdg.andconlab.utils.BrowserUtils;
 import com.gdg.andconlab.utils.SLog;
 import com.pagesuite.flowtext.FlowTextView;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 /**
  * Activity that displays a single lecture with all its details.
  * This activity expects to get the lecture ID in the calling intent
  *
  * @author Ran Nachmany & Amir Lazarovich
  */
 public class SingleLectureActivity extends SherlockActivity implements OnClickListener {
     //////////////////////////////////////////
     // Constants
     //////////////////////////////////////////
     private static final String TAG = "SingleLectureActivity";
     public static final String EXTRA_LECTURE_ID = "lecture_id";
 
     private static final int ILLEGAL_ID = -1;
 
     //////////////////////////////////////////
     // Members
     //////////////////////////////////////////
     private Lecture lecture;
 
     //////////////////////////////////////////
     // Activity Flow
     //////////////////////////////////////////
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         getSupportActionBar().setHomeButtonEnabled(true);
         setContentView(R.layout.single_lecture);
 
         long id = getIntent().getLongExtra(EXTRA_LECTURE_ID, ILLEGAL_ID);
         if (ILLEGAL_ID == id) {
             throw new IllegalStateException("You should set lecture ID in the calling intent");
         } else {
             //fetch the lecture from db
             SQLiteDatabase db = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION).getReadableDatabase();
             lecture = DBUtils.getLectureById(db, id);
 
             //set lecture details
             getSupportActionBar().setTitle(lecture.getName());
            ((TextView) findViewById(R.id.lecture_description)).setText(lecture.getDescription());
             String slidesUrl = lecture.getSlidesUrl();
             String videoUrl = lecture.getVideoUrl();
             boolean showButtons = false;
             if (!TextUtils.isEmpty(slidesUrl)) {
                 View btnSlides = findViewById(R.id.lecture_btn_slides);
                 btnSlides.setVisibility(View.VISIBLE);
                 btnSlides.setOnClickListener(this);
                 showButtons = true;
             }
 
             if (!TextUtils.isEmpty(videoUrl)) {
                 View btnVideo = findViewById(R.id.lecture_btn_video);
                 btnVideo.setVisibility(View.VISIBLE);
                 btnVideo.setOnClickListener(this);
                 showButtons = true;
             }
 
             if (showButtons) {
                 findViewById(R.id.lecture_btns_wrapper).setVisibility(View.VISIBLE);
             }
 
             //get all speakers
             ArrayList<Speaker> speakers = DBUtils.getSpeakersByLectureId(db, id);
 
             //loop and add speakers.
             LinearLayout ll = (LinearLayout) findViewById(R.id.lecture_container);
             int speakerTxtSize = getResources().getDimensionPixelSize(R.dimen.sub_title_size);
             final LayoutInflater layoutInflater = getLayoutInflater();
             for (Speaker speaker : speakers) {
                 FlowTextView ftv = (FlowTextView) layoutInflater.inflate(layout.speaker_item, ll, false);
                 ImageView img = (ImageView) ftv.findViewById(R.id.speaker_item_img);
 
                 ImageManager.loadImage(this, ImageType.SPEAKER, speaker.getImageUrl(), img);
                 Spanned span = Html.fromHtml("<b>" + speaker.getFullName() + "</b><br/>" + speaker.getBio());
                 ftv.setText(span);
                 ftv.setTextSize(speakerTxtSize);
                 ftv.invalidate();
                 ll.addView(ftv);
             }
 
             db.close();
         }
     }
 
     //////////////////////////////////////////
     // Overrides & Implementations
     //////////////////////////////////////////
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case id.lecture_btn_slides:
                 openWebsite(lecture.getSlidesUrl());
                 break;
 
             case id.lecture_btn_video:
                 openWebsite(lecture.getVideoUrl());
                 break;
         }
     }
 
 
     //////////////////////////////////////////
     // Private
     //////////////////////////////////////////
 
     /**
      * Request to open the browser on given <code>url</code>
      *
      * @param url
      */
     private void openWebsite(String url) {
         try {
             BrowserUtils.openBrowserForUrl(this, url);
         } catch (URISyntaxException e) {
             SLog.e(TAG, e, "Couldn't open website: [%s]", url);
         }
     }
 }

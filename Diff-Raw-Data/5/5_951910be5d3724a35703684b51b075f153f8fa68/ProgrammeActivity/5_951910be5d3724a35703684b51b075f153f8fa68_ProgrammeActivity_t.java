 package fr.ybo.ybotv.android.activity;
 
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.google.analytics.tracking.android.EasyTracker;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.lasylist.ImageLoader;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.util.AdMobUtil;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ProgrammeActivity extends SherlockActivity {
 
     private final static Map<String, Integer> mapOfRatings = new HashMap<String, Integer>(){{
         put("1/4", R.drawable.rating_1star);
         put("2/4", R.drawable.rating_2star);
         put("3/4", R.drawable.rating_3star);
         put("4/4", R.drawable.rating_4star);
     }};
 
     private final static Map<String, Integer> mapOfCsaRatings = new HashMap<String, Integer>(){{
         put("-18", R.drawable.moins18);
         put("-16", R.drawable.moins16);
         put("-12", R.drawable.moins12);
         put("-10", R.drawable.moins10);
     }};
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.programme_activity);
         Programme programme = getIntent().getParcelableExtra("programme");
        getSupportActionBar().setTitle(programme.getTitle());
         ImageLoader imageLoader=new ImageLoader(getApplicationContext());
 
         ImageView icon = (ImageView) findViewById(R.id.programme_activity_icon);
         ImageView rating = (ImageView) findViewById(R.id.programme_activity_rating);
         ImageView csaRating = (ImageView) findViewById(R.id.programme_activity_csa_rating);
         TextView description = (TextView) findViewById(R.id.programme_activity_description);
 
         if (programme.getIcon() != null && programme.getIcon().length() > 0) {
             imageLoader.DisplayImage(programme.getIcon(), icon);
             icon.setVisibility(View.VISIBLE);
         } else {
             icon.setVisibility(View.GONE);
         }
         if (programme.getStarRating() != null
                 && mapOfRatings.containsKey(programme.getStarRating())) {
             rating.setImageResource(mapOfRatings.get(programme.getStarRating()));
             rating.setVisibility(View.VISIBLE);
         } else {
             rating.setVisibility(View.GONE);
         }
         Log.d(YboTvApplication.TAG, "CsaRating : " + programme.getCsaRating());
         if (programme.getCsaRating() != null
                 && mapOfCsaRatings.containsKey(programme.getCsaRating())) {
             csaRating.setImageResource(mapOfCsaRatings.get(programme.getCsaRating()));
             csaRating.setVisibility(View.VISIBLE);
         } else {
             csaRating.setVisibility(View.GONE);
         }
         if (programme.getDesc() != null) {
             description.setText(programme.getDesc());
             description.setVisibility(View.VISIBLE);
         } else {
             description.setVisibility(View.GONE);
         }
 
         description.setMovementMethod(new ScrollingMovementMethod());
 
         AdMobUtil.manageAds(this);
     }
 
     @Override
     public void onStart() {
         super.onStart();
         EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         EasyTracker.getInstance().activityStop(this);
     }
 
 }
 

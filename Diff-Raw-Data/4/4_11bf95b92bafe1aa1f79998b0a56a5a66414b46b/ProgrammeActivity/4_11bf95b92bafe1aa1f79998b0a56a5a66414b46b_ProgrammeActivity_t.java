 package fr.ybo.ybotv.android.activity;
 
 import android.content.Context;
 import android.os.Bundle;
import android.text.Html;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.google.analytics.tracking.android.EasyTracker;
 import fr.ybo.ybotv.android.R;
 import fr.ybo.ybotv.android.YboTvApplication;
 import fr.ybo.ybotv.android.adapter.ProgrammeViewFlowAdapter;
 import fr.ybo.ybotv.android.lasylist.ImageLoader;
 import fr.ybo.ybotv.android.modele.Programme;
 import fr.ybo.ybotv.android.util.AdMobUtil;
 import fr.ybo.ybotv.android.util.GetView;
 import org.taptwo.android.widget.TitleFlowIndicator;
 import org.taptwo.android.widget.ViewFlow;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ProgrammeActivity extends SherlockActivity implements GetView {
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Programme programme = getIntent().getParcelableExtra("programme");
         getSupportActionBar().setTitle(programme.getTitle());
 
         if (((YboTvApplication)getApplication()).isTablet()) {
             createViewForTablet(programme);
         } else {
             createViewForPhone(programme);
         }
 
         AdMobUtil.manageAds(this);
     }
 
     private void createViewForPhone(Programme programme) {
         setContentView(R.layout.flow);
         ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
         ProgrammeViewFlowAdapter adapter = new ProgrammeViewFlowAdapter(this, programme);
         viewFlow.setAdapter(adapter);
         TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
         indicator.setTitleProvider(adapter);
         viewFlow.setFlowIndicator(indicator);
     }
 
     private void createViewForTablet(Programme programme) {
         setContentView(R.layout.programme_for_tablet);
         contructResumeView(this, this, programme);
         contructDetailView(this, this, programme);
     }
 
 
 
     public static void contructDetailView(Context context, GetView getView, Programme programme) {
         TextView duree = (TextView) getView.findViewById(R.id.programme_detail_duree);
         TextView date = (TextView) getView.findViewById(R.id.programme_detail_date);
         TextView credits = (TextView) getView.findViewById(R.id.programme_detail_credits);
 
         duree.setText(context.getString(R.string.duree, programme.getDuree()));
 
         if (programme.getDate() == null) {
             date.setVisibility(View.GONE);
         } else {
             date.setVisibility(View.VISIBLE);
             date.setText(context.getString(R.string.date, programme.getDate()));
         }
 
         StringBuilder builderCredits = new StringBuilder();
 
         for (String director : programme.getDirectors()) {
             builderCredits.append(context.getString(R.string.director, director));
             builderCredits.append('\n');
         }
         for (String actor : programme.getActors()) {
             builderCredits.append(context.getString(R.string.actor, actor));
             builderCredits.append('\n');
         }
         for (String writer : programme.getWriters()) {
             builderCredits.append(context.getString(R.string.writer, writer));
             builderCredits.append('\n');
         }
         for (String presenter : programme.getPresenters()) {
             builderCredits.append(context.getString(R.string.presenter, presenter));
             builderCredits.append('\n');
         }
 
         credits.setText(builderCredits.toString());
         credits.setMovementMethod(new ScrollingMovementMethod());
     }
     private static String formatterMots(String motsAFormatter) {
         StringBuilder motsFormattes = new StringBuilder();
         motsFormattes.append(motsAFormatter.substring(0, 1).toUpperCase());
         motsFormattes.append(motsAFormatter.substring(1));
         return motsFormattes.toString();
     }
 
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
 
     public static void contructResumeView(Context context, GetView getView, Programme programme) {
         ImageLoader imageLoader=new ImageLoader(context.getApplicationContext());
 
         ImageView icon = (ImageView) getView.findViewById(R.id.programme_resume_icon);
         ImageView rating = (ImageView) getView.findViewById(R.id.programme_resume_rating);
         ImageView csaRating = (ImageView) getView.findViewById(R.id.programme_resume_csa_rating);
 
         TextView categories = (TextView) getView.findViewById(R.id.programme_resume_categories);
         TextView description = (TextView) getView.findViewById(R.id.programme_resume_description);
 
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
 
         if (programme.getCategories().isEmpty()) {
             categories.setVisibility(View.GONE);
         } else {
             categories.setVisibility(View.VISIBLE);
             String categorie = null;
             for (String oneCategorie : programme.getCategories()) {
                 categorie = oneCategorie;
             }
             categories.setText(formatterMots(categorie));
         }
 
         if (programme.getDesc() != null) {
            description.setText(Html.fromHtml(programme.getDesc()));
             description.setVisibility(View.VISIBLE);
         } else {
             description.setVisibility(View.GONE);
         }
 
         description.setMovementMethod(new ScrollingMovementMethod());
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
 

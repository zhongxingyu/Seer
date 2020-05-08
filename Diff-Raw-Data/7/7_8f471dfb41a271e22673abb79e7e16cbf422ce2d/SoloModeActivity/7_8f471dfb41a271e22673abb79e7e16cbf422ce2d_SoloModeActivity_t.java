 package com.BrotherOfLewis.SearchPartyPocket;
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ShareActionProvider;
 import android.widget.TextView;
 
 import com.BrotherOfLewis.SearchPartyPocket.DataAccessObjects.QueryDAO;
 import com.BrotherOfLewis.SearchPartyPocket.Helpers.QueryHelper;
 import com.BrotherOfLewis.SearchPartyPocket.Helpers.QueryQuestionListener;
 import com.BrotherOfLewis.SearchPartyPocket.Helpers.SoundEffectHelper;
 import com.BrotherOfLewis.SearchPartyPocket.Models.QueryQuestion;
 import com.BrotherOfLewis.SearchPartyPocket.Models.Suggestion;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class SoloModeActivity extends Activity {
     private TextView TxtQuestion;
     private List<Button> BtnSuggestions;
     private ImageButton ImgBtnNext;
     private ShareActionProvider mShareActionProvider;
     private TextView TxtCurrentStreak;
     private TextView TxtBestStreak;
     private TextView TxtCorrectPercentage;
 
     private int currentStreak = 0;
     private int correctCount = 0;
     private int incorrectCount = 0;
    private boolean alreadyAnswered = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_solo_mode);
         // Show the Up button in the action bar.
         setupActionBar();
 
         TxtQuestion = (TextView) findViewById(R.id.txtQuestion);
         BtnSuggestions = new ArrayList<Button>();
         BtnSuggestions.add((Button) findViewById(R.id.btnSuggestion1));
         BtnSuggestions.add((Button) findViewById(R.id.btnSuggestion2));
         BtnSuggestions.add((Button) findViewById(R.id.btnSuggestion3));
         ImgBtnNext = (ImageButton) findViewById(R.id.imgBtnNext);
         TxtCurrentStreak = (TextView) findViewById(R.id.txtCurrentStreak);
         TxtBestStreak = (TextView) findViewById(R.id.txtBestStreak);
         TxtCorrectPercentage = (TextView) findViewById(R.id.txtCorrectPercentage);
 
         // click event for next button
         ImgBtnNext.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 LoadQueryQuestionAsync();
             }
         });
 
         LoadQueryQuestionAsync();
     }
 
     private void LoadQueryQuestionAsync() {
         // reset query and suggestions
        alreadyAnswered = false;
         TxtQuestion.setText("");
         TxtCurrentStreak.setText(String.valueOf(currentStreak));
         refreshBestStreak();
 
         for (Button btn : BtnSuggestions)
         {
             btn.setText("");
             btn.setBackgroundColor(Color.TRANSPARENT);
         }
         Collections.shuffle(BtnSuggestions);
 
         // get query and suggestions
         final QueryHelper qHelper = new QueryHelper();
         qHelper.setQueryQuestionListener(new QueryQuestionListener() {
             @Override
             public void OnQueryQuestionReturn(QueryQuestion qq) {
                 // not enough choice, then skip the current question
                 if (qq.getSuggestions().length < BtnSuggestions.size())
                 {
                     qHelper.getQueryQuestionAsync(QueryDAO.DEFAULT_PACK);
                     return;
                 }
 
                 TxtQuestion.setText(qq.getQuery());
 
                 Suggestion[] suggestions = qq.getSuggestions();
                 for (int i = 0; i < BtnSuggestions.size(); i++) {
                     final Button tmpBtn = BtnSuggestions.get(i);
                     tmpBtn.setOnClickListener(null);
 
                     if (i < suggestions.length)
                     {
                         tmpBtn.setText(suggestions[i].getTheSuggestion());
                         final int finalI = i;
                         tmpBtn.setOnClickListener(new View.OnClickListener() {
                             public void onClick(View v) {
                                if (alreadyAnswered) return;

                                alreadyAnswered = true;
                                 ImgBtnNext.setEnabled(true);
                                 ImgBtnNext.setBackgroundResource(R.drawable.next_search);
 
                                 if (finalI == 0) {
                                     SoundEffectHelper.getInstance().playSound(SoundEffectHelper.SOUND_SUCCESS);
                                     tmpBtn.setBackgroundColor(Color.GREEN);
                                     correctCount++;
                                     currentStreak++;
                                     if (currentStreak > QueryHelper.getBestStreak())
                                     {
                                         QueryHelper.setBestStreak(currentStreak);
                                         refreshBestStreak();
                                     }
                                 }
                                 else {
                                     SoundEffectHelper.getInstance().playSound(SoundEffectHelper.SOUND_FAIL);
                                     BtnSuggestions.get(0).setBackgroundColor(Color.GREEN);
                                     tmpBtn.setBackgroundColor(Color.RED);
                                     incorrectCount++;
                                     currentStreak = 0;
                                 }
                                 refreshCorrectPercentage();
                             }
                         });
                     }
                 }
             }
         });
         qHelper.getQueryQuestionAsync(QueryDAO.DEFAULT_PACK);
         ImgBtnNext.setEnabled(false);
         ImgBtnNext.setBackgroundResource(R.drawable.next_search_off);
     }
 
     private void refreshCorrectPercentage() {
         // load correct percentage
         int totalCount = (correctCount + incorrectCount);
         if (totalCount > 0)
         {
             int correctPercentage = (int)((correctCount / (double) totalCount) * 100);
             TxtCorrectPercentage.setText(String.valueOf(correctPercentage) + "%");
         }
     }
 
     private void refreshBestStreak() {
         final int bestStreak = QueryHelper.getBestStreak();
         TxtBestStreak.setText(String.valueOf(bestStreak));
     }
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.solo_mode, menu);
 
         return true;
     }
     
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
             case R.id.action_settings:
                 final Intent settingsIntent = new Intent(this, SettingsActivity.class);
                 startActivity(settingsIntent);
                 break;
             case R.id.action_share:
                 Intent share = new Intent(Intent.ACTION_SEND);
                 share.setType("text/plain");
                 share.putExtra(Intent.EXTRA_TEXT, "Here is something to share");
                 startActivity(Intent.createChooser(share, "Share"));
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }

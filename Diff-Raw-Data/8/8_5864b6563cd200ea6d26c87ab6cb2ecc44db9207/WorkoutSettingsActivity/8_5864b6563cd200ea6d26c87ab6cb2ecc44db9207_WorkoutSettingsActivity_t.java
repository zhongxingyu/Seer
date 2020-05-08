 package com.kinnack.nthings.activity;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.Date;
 
 import org.json.JSONException;
 
 import com.kinnack.nthings.ProgressChart;
 import com.kinnack.nthings.R;
 import com.kinnack.nthings.StopWatch;
 import com.kinnack.nthings.controller.PushupWorkoutController;
 import com.kinnack.nthings.controller.SitupWorkoutController;
 import com.kinnack.nthings.controller.WorkoutController;
 import com.kinnack.nthings.helper.CounterActivityManager;
 import com.kinnack.nthings.model.DayAndWeek;
 import com.kinnack.nthings.model.History;
 import com.kinnack.nthings.model.LevelSelectionViewAdapter;
 import com.kinnack.nthings.model.Logg;
 import com.kinnack.nthings.model.Test;
 import com.kinnack.nthings.model.WorkoutSelectionViewAdapter;
 import com.kinnack.nthings.model.Workout.Type;
 import com.kinnack.nthings.model.level.Level;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnClickListener;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class WorkoutSettingsActivity extends Activity {
     public static final String TAG = "dgmt:WorkoutSettings";
     private static final int COUNTER_INTENT = 100;
     private static final int TEST_INTENT = 150;
     private static final int REST_INTENT = 200;
     private static final int FINAL_TEST_INTENT=175;
     private static final int RESET_INTENT = 300;
     private static final String PUBLIC_FOLDER_PATH=Environment.getExternalStorageDirectory()+"/nhundredthings/";
     private static final String PUBLIC_FILE_PATH=PUBLIC_FOLDER_PATH+"/prefs_config.xml";
     private static final String PRIVATE_FILE_PATH = "/data/data/"+StopWatch.class.getPackage().getName()+"/shared_prefs/prefs_config.xml";
     public static String WORKOUT_TYPE = "workout-type";
     public static final String PREFS = "prefs_config";
     
     private WorkoutController workoutController;
     private Editor prefEditor;
     private CounterActivityManager counterActivityManager;
     
     @Override
     protected void onCreate(Bundle savedInstanceState_) {
         super.onCreate(savedInstanceState_);
         setContentView(R.layout.activity_settings);
         Bundle extras = getIntent().getExtras();
         Type type = Type.valueOf(extras.getString(WORKOUT_TYPE));
         TextView exerciseLabel = (TextView)findViewById(R.id.ExerciseLabel);
         switch (type) {
         case PUSHUP:
             workoutController = new PushupWorkoutController();
             exerciseLabel.setText("Push Ups");
             break;
         case SITUP:
             workoutController = new SitupWorkoutController();
             exerciseLabel.setText("Sit Ups");
             break;
         }
         SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
         prefEditor = prefs.edit();
         setDayWeekSelectorOnItemClick();
         setLevelSelectorOnItemSelect();
     }
     
     
     
     /**
      * 
      */
     private void setDayWeekSelectorOnItemClick() {
         ((Spinner)findViewById(R.id.dayWeekSelector)).setOnItemSelectedListener(new OnItemSelectedListener() {
             private int previousPosition = -1;
             @Override
             public void onItemSelected(AdapterView<?> parent_, View view_, int position_, long id_) {
                 Log.d("dgmt!dayWeekSelectorItemSelect","day and week changed to position "+position_);
                 DayAndWeek dayAndWeek = WorkoutSelectionViewAdapter.getDayAndWeekByPosition(position_);
                 boolean dayOrWeekChanged = !dayAndWeek.equals(workoutController.getDayAndWeek());
                 if (dayAndWeek.wasFound() && !workoutController.isTest() && dayOrWeekChanged ) {
                     Log.d("dgmt!dayWeekSelectorItemSelect","day and week has changed");
                     workoutController.setDayAndWeek(dayAndWeek);
                     dayWeekOrLevelChanged();
                 }
                 previousPosition = position_;
                 
                 
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0_) {
                 // TODO Auto-generated method stub
                 
             }
         });
         
     }
     
     private void setLevelSelectorOnItemSelect(){
         ((Spinner)findViewById(R.id.levelSelector)).setOnItemSelectedListener(new OnItemSelectedListener() {
 
             
            
             @Override
             public void onItemSelected(AdapterView<?> parent_, View view_, int position_, long id_) {
                 Log.d("dgmt!levelSelectorItemSelect","Level changed to position "+position_);
                 Level level = LevelSelectionViewAdapter.getLevelByPosition(position_);
                
                 boolean levelChanged = !level.equals(workoutController.getCurrentLevel());
                 Log.d("dgmt!levelSelectorItemSelect","Level changed?"+levelChanged+". current="+workoutController.getCurrentLevel()+"] selected="+level);
                 if(position_ != 3 && workoutController.setCurrentLevel(level)) {
                     Log.d("dgmt!levelSelectorItemSelect", "Level has actually changed");
                     findViewById(R.id.dayWeekSelector).setEnabled(true);
                 }
                 if (levelChanged) {dayWeekOrLevelChanged();}
                 
                 
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0_) {
                 // TODO Auto-generated method stub
                 
             }
         });
     }
     
     
     @Override
     protected void onResume() {
         super.onResume();
         counterActivityManager = new CounterActivityManager(PreferenceManager.getDefaultSharedPreferences(this), this);
         workoutController.setCounterActivityManager(counterActivityManager);
         workoutController.loadHistory(getSharedPreferences(PREFS, Context.MODE_PRIVATE));
         ((TextView)findViewById(R.id.OverallTotalCount)).setText(workoutController.getTotalCount()+" TOTAL");
         
         listDayWeekOptions();
         loadLevelOptions();
         configureMainView();
     }
     
     /**
      * 
      */
     private void configureMainView() {
         ((Button)findViewById(R.id.ActivityButton)).setEnabled(true);
         ((Button)findViewById(R.id.FinalButton)).setEnabled(false);
         findViewById(R.id.levelSelector).setEnabled(true);
         findViewById(R.id.dayWeekSelector).setEnabled(true);
         
         String value = "";
         if (workoutController.isTest()) {
             findViewById(R.id.dayWeekSelector).setEnabled(false);
             value = "TEST";
         } else if (workoutController.isFinal()) {
             findViewById(R.id.dayWeekSelector).setEnabled(false);
             findViewById(R.id.levelSelector).setEnabled(false);
             value = "FINAL";
             ((Button)findViewById(R.id.ActivityButton)).setEnabled(false);
         } else {
             value = workoutController.getLevelForDisplay();
         }
         
         dayWeekOrLevelChanged();
         
         
         if (workoutController.isFinalUnlocked()) ((Button)findViewById(R.id.FinalButton)).setEnabled(true);
     }
     
     protected void dayWeekOrLevelChanged() {
         String count = (String) getResources().getText(R.string.count_for_test_msg);
         if (!workoutController.shouldDisplayDayAsTest()) { count = workoutController.totalCountLeft()+""; }
         ((TextView)findViewById(R.id.count_for_settings)).setText("Drop and Give Me "+count+"!");
     }
     
     public void listDayWeekOptions() {
         
         Spinner dayWeekSelector = (Spinner)findViewById(R.id.dayWeekSelector);
         if (dayWeekSelector.getAdapter() !=  null) { return; }
         WorkoutSelectionViewAdapter listAdapter = new WorkoutSelectionViewAdapter(this, workoutController.isFinal());
         dayWeekSelector.setAdapter(listAdapter);
         Log.d("dgmt:listDayWeekOptions","Getting position for dayWeek with week="+workoutController.getWeek()+" and day="+workoutController.getDay());
         dayWeekSelector.setSelection(listAdapter.getPositionForWeekDay(workoutController.getWeek(), workoutController.getDay()));
         
     }
     
     public void resetSpinners() {
         ((Spinner)findViewById(R.id.dayWeekSelector)).setAdapter(null);
         ((Spinner)findViewById(R.id.levelSelector)).setAdapter(null);
         dayWeekOrLevelChanged();
     }
     
     public void loadLevelOptions() {
         Spinner levelSelector = (Spinner)findViewById(R.id.levelSelector);
         if (levelSelector.getAdapter() != null){return;}
         boolean showTest = workoutController.shouldDisplayDayAsTest();
         LevelSelectionViewAdapter viewAdapter = new LevelSelectionViewAdapter(this, showTest);
         levelSelector.setAdapter(viewAdapter);
         levelSelector.setSelection(showTest ? 3 :viewAdapter.getPositionForLevel(workoutController.getCurrentLevel()));
     }
     
   
     
    
     // ------------ ACTIONS ----------------
     
     public void doActivity(View target_) {
         if (workoutController.isTest()) {startTestActivity(); return;}
         if (workoutController.isFinal()) { startFinalTestActivity(); return;}
         workoutController.beginExercise(target_);
         startCounterActivity();
     }
     
     // ------------ APPLICATION FLOW ----------
     public void doFinalTest(View target_) {
         startFinalTestActivity();
     }
     
     public void showProgress(View target_) {
         showProgress(workoutController.getHistory());
     }
     
    
 
     /**
      * 
      */
     private void startCounterActivity() {
         Intent counterIntent = new Intent(this, workoutController.getCounterActivity());
         counterIntent.putExtra(CounterActivity.INIT_COUNT_KEY, workoutController.nextSet());
         counterIntent.putExtra(CounterActivity.SHOW_DONE, workoutController.isMaxSet());
    
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, COUNTER_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startTestActivity() {
         Intent counterIntent = new Intent(this, workoutController.getCounterActivity());
         counterIntent.putExtra(CounterActivity.INIT_COUNT_KEY, 0);
         counterIntent.putExtra(CounterActivity.SHOW_DONE, true);
         counterIntent.putExtra(CounterActivity.IS_TEST, true);
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, TEST_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startFinalTestActivity() {
         Intent counterIntent = new Intent(this, workoutController.getCounterActivity());
         counterIntent.putExtra(CounterActivity.INIT_COUNT_KEY, 100);
         counterIntent.putExtra(CounterActivity.SHOW_DONE, true);
         counterIntent.putExtra(CounterActivity.IS_TEST, true);
         counterIntent.putExtra(CounterActivity.USE_SUBCOUNT, true);
         Log.d(TAG, "Intent about to start");
         startActivityForResult(counterIntent, FINAL_TEST_INTENT);
         Log.d(TAG, "Intent started and returned");
     }
     
     private void startRestActivity() {
         Intent restIntent = new Intent(this, RestActivity.class);
         Log.d(TAG, "About to launch intnet for "+RestActivity.class.getName());
         restIntent.putExtra(RestActivity.REST_LENGTH, workoutController.nextSet());
         restIntent.putExtra(RestActivity.SETS_DONE, workoutController.completedSets());
         restIntent.putExtra(RestActivity.SETS_TO_GO, workoutController.incompleteSets());
         restIntent.putExtra(RestActivity.COUNT_TO_GO, workoutController.totalCountLeft());
         startActivityForResult(restIntent, REST_INTENT);
     }
     
     
     @Override
     protected void onActivityResult(int requestCode_, int resultCode_, Intent data_) {
        Resources resources = getResources();
        switch (requestCode_) {
         case COUNTER_INTENT:
             // this was because the back button was pressed during a counter. FIXME do something better
             if (data_ == null) { 
                 deleteAnyUnwantedLogs();
                 return; 
             }
             Bundle extras = data_.getExtras();
             int count = extras.getInt(CounterActivity.MAX_COUNT);
             long avgTime = extras.getLong(CounterActivity.AVG_TIME);
             Logg currentLog =workoutController.getCurrentLog();
             currentLog.addCountAndTime(count, avgTime);
            
             if (!workoutController.hasNext()) { 
                 workoutController.advanceDate();
                 saveHistory(); 
                 resetSpinners();
                 configureMainView();
                 showProgress(workoutController.getHistory());
                 
                 shareResults(currentLog);
                finish();
                 return; 
             }
             startRestActivity();
             
             break;
         case REST_INTENT:
             startCounterActivity();
             break;
         case TEST_INTENT:
             if (data_ == null) { return; }
             int testCount = data_.getExtras().getInt(CounterActivity.MAX_COUNT);
             
             Level level = workoutController.getLevelForTestResult(testCount);
             if (level == null) { return; }
             workoutController.addTestResult(testCount).resetDay().setCurrentLevel(level);
             saveHistory();
             configureMainView();
             Toast.makeText(this, level.toString(), Toast.LENGTH_SHORT).show();
             break;
         case FINAL_TEST_INTENT:
             if(data_ == null) { return; }
             testCount = data_.getExtras().getInt(CounterActivity.MAX_COUNT);
             long totalTime = data_.getExtras().getLong(CounterActivity.TOTAL_TIME);
             workoutController.addTestResult(testCount);
             if (testCount >= 100) {
                 shareComplete(testCount, totalTime);
                 String msg = String.format(resources.getString(R.string.final_complete_msg), 
                                             workoutController.getFinalTestCount(), 
                                             resources.getString(workoutController.getLabelResource()));
                 showUserDialog(R.string.final_complete_title, msg);
                 workoutController.markFinalComplete();
             } else {
                 shareDNFFinal(testCount, totalTime);
                 showUserDialog(R.string.final_DNF_title, resources.getString(R.string.final_DNF_msg));
             }
             workoutController.resetToWorkoutForFinal();
             saveHistory();
             break;
         case RESET_INTENT:
             if (data_ == null) {return;}
             if (data_.getExtras().getBoolean(ResetActivity.RESET,false)) {
                 workoutController = null;
             }
             
             break;
         default:
             Log.w(TAG, "Got an unknown activity result. request["+requestCode_+"], result["+resultCode_+"]");
             break;
         }
         
         
         
     }
 
 
 
     private void deleteAnyUnwantedLogs() {
         workoutController.removeCurrentLog();
     }
 
 
   
     private void showUserDialog(int title_, String msg_) {
         
         new AlertDialog.Builder(this)
         .setTitle(title_)
         .setMessage(msg_)
         .setIcon(R.drawable.dialog)
         .setPositiveButton(R.string.final_complete_OK, new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog_, int which_) {
                 dialog_.dismiss();
             }
         }).show();
     }
     
     /**
      * @param currentLog
      */
     private void shareResults(Logg currentLog) {
         long roundedFrequency = Math.round(60000*currentLog.getAveragePushupFrequency());
         String typeLabel = getResources().getString(workoutController.getLabelResource());
         launchSharingChooser("My Latest DGMT! Results",
                 getResources().getString(R.string.share_results_msg, currentLog.getTotalCount(), typeLabel ,roundedFrequency));
                 
 
     }
     
     private void shareComplete(int totalCount_, long totalTime_) {
         long roundedFrequency = Math.round(60000.0*totalCount_/totalTime_);
         String typeLabel = getResources().getString(workoutController.getLabelResource());
         launchSharingChooser("Mission Accomplished!",
                 getResources().getString(R.string.share_complete_msg, 
                                             workoutController.getFinalTestCount(),
                                             typeLabel,
                                             totalCount_, 
                                             roundedFrequency));
     }
     
     private void shareDNFFinal(int totalCount_, long totalTime_) {
         long roundedFrequency = Math.round(60000.0*totalCount_/totalTime_);
         String typeLabel = getResources().getString(workoutController.getLabelResource());
         launchSharingChooser("Almost There!",
                 getResources().getString(R.string.share_dnf_final_msg, 
                         totalCount_,
                         typeLabel,
                         roundedFrequency));
     }
     
     
     private void launchSharingChooser(String subject_, String text_) {
         Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
         shareIntent.setType("text/plain");
         shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject_);
         shareIntent.putExtra(Intent.EXTRA_TEXT, text_);
         startActivity(Intent.createChooser(shareIntent, "Share Results"));
     }
     
     
     
     // TODO move this out of here!
     private void saveHistory() {
         
         try {
             workoutController.saveHistory(prefEditor);
            
             File externalFolder = new File(PUBLIC_FOLDER_PATH);
             if (!externalFolder.exists()) { externalFolder.mkdir(); }
             prefEditor.commit();
             copyFile(new File(PRIVATE_FILE_PATH),new File(PUBLIC_FILE_PATH));
         } catch (JSONException e) {
             Log.e(TAG,"Couldn't convert history to JSON! ",e);
             Toast.makeText(this, "Error saving history", Toast.LENGTH_SHORT);
         }
     }
     
 
     /**
      * @return TODO
      * 
      */
     private boolean copyFile(File originalFile_, File copyFile_) {
         try {
             Log.i("DGMT!Home.copyFile","Copying "+originalFile_+"(exists?"+originalFile_.exists()+") to "+copyFile_+" (exists?"+copyFile_.exists()+")");
             
             if (!copyFile_.exists()) {
                 copyFile_.getParentFile().mkdirs();
                 copyFile_.createNewFile();
             }
             
             FileOutputStream out = new FileOutputStream(copyFile_);
             FileInputStream in = new FileInputStream(originalFile_);
             
             
             FileChannel inChannel = in.getChannel();
             FileChannel outChannel = out.getChannel();
 
             long transferedBytes = outChannel.transferFrom(inChannel, 0, inChannel.size());
 
             inChannel.close();
             outChannel.close();
             in.close();
             out.close();
 
             return transferedBytes > 0;
         } catch (FileNotFoundException e1) {
            Log.i(TAG,"Could nto find file shared_prefs/prefs_config.xml",e1);
         } catch (IOException e) {
             Log.w(TAG, "ERROR trying to write preferences to disk",e);
         } 
         return false;
     }
     
     private void showProgress(History history_) {
         ProgressChart chart = new ProgressChart();
         Intent progressIntent = chart.progressChart(history_, this);
         startActivity(progressIntent);
     }
     
 }

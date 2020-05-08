 package com.kinnack.nthings.controller;
 
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 import android.view.View;
 
 import com.kinnack.nthings.activity.CounterActivity;
 import com.kinnack.nthings.helper.CounterActivityManager;
 import com.kinnack.nthings.model.DayAndWeek;
 import com.kinnack.nthings.model.ExerciseSet;
 import com.kinnack.nthings.model.History;
 import com.kinnack.nthings.model.Logg;
 import com.kinnack.nthings.model.Workout;
 import com.kinnack.nthings.model.Workout.Type;
 import com.kinnack.nthings.model.level.Level;
 
 public abstract class WorkoutController {
 
     private CounterActivityManager counterActivityManager;
 
     protected abstract String getTag();
     protected abstract String getKeyForHistory();
     protected abstract Type getWorkoutType();
     protected abstract void getThisWeekAndDaySet();
     public abstract int getFinalTestCount();
     public abstract int getLabelResource();
     public abstract Level getLevelForTestResult(int testCount_);
     
     protected History history;
     protected ExerciseSet set;
 
     public WorkoutController() {
         super();
     }
 
     public void beginExercise(View target_) {
         Logg currentLog = new Logg(history, history.getWeek(),history.getDay());
         history.getLogs().add(currentLog);
 
         getThisWeekAndDaySet();
     }
     
     
 
     /**
      * @param prefs
      * @return 
      */
     public void loadHistory(SharedPreferences prefs) {
         if (history != null) { return; }
         try {
             history = new History(prefs.getString(getKeyForHistory(), null));
             Log.d(getTag(),"Loaded history as "+prefs.getString(getKeyForHistory(), "[Not found]"));
         } catch (JSONException e) {
             Log.e(getTag(), "Couldn't unmarshal history", e);
         } catch (NullPointerException npe) {
             Log.i(getTag(), "No history to load");
         }
         if (history == null) {
             history = new History();
             history.setType(getWorkoutType());
         }
         if (set == null && !isTest() && !isFinal()) {getThisWeekAndDaySet();}
     }
 
     public WorkoutController addTestResult(int testCount_) {
         history.getTestResults().add(testCount_);
         return this;
     }
 
     public WorkoutController resetDay() {
         history.setDay(1);
         return this;
     }
 
     public DayAndWeek advanceDate() {
         int day = history.getDay();
         int week = history.getWeek();
         if (day == 3) {
             day = (week==5 ? 0 : week%2);
             history.setDay(day);
             history.setWeek(history.getWeek()+1);
         } else {
             history.setDay(day+1);
         }
         
         return new DayAndWeek(day, week);
         
     }
     
     public void saveHistory(Editor prefEditor_) throws JSONException{
         setLastWorkout(new Date());
         prefEditor_.putString(getKeyForHistory(), history.toJSON().toString());
     }
     
     public Class<? extends CounterActivity> getCounterActivity() {
         return counterActivityManager.getActivity(getWorkoutType());
     }
 
     public int getWeek() {
         return history == null ? 1 : history.getWeek();
     }
 
     public int getDay() {
         return history == null ? 0: history.getDay();
     }
 
     public boolean isTest() {
         return history == null || history.isTest();
     }
 
     public boolean isFinal() {
         return history != null && history.isFinal();
     }
 
     public String getLevelForDisplay() {
         return history.getCurrentLevel().getLabel();
     }
 
     public boolean isFinalUnlocked() {
         return history.isFinalUnlocked();
     }
 
     public boolean shouldDisplayDayAsTest() {
         return history != null && (history.isTest() || history.isFinal());
     }
 
     public Level getCurrentLevel() {
         return history.getCurrentLevel();
     }
 
     public Logg getCurrentLog() {
         return history.getCurrentLog();
     }
 
 
     public void markFinalComplete() {
         history.setFinished(true);
     }
 
     public void resetToWorkoutForFinal() {
         history.setWeek(6);
         resetDay();
     }
     
     public int getTotalCount() {
         return history.getTotalCount();
     }
 
     public int nextSet() {
         return set.next();
     }
 
     public boolean hasNext() {
         return set.hasNext();
     }
 
     public boolean isMaxSet() {
         return set.isMax();
     }
 
     public int completedSets() {
         return set.getSetsDone();
     }
 
     public int incompleteSets() {
         return set.getSetsToGo();
     }
     
     public int totalCountLeft() {
         return set.getCountLeft();
     }
 
     public void setLastWorkout(Date lastWorkoutDate_) {
         history.setLastWorkout(lastWorkoutDate_);
     }
 
  
 
     public void setDayAndWeek(DayAndWeek dayAndWeek_) {
         Log.d("WorkoutController.setDayAndWeek",String.format("Setting day=%1$d and week=%2$d",dayAndWeek_.day,dayAndWeek_.week));
        
         history.setDay(dayAndWeek_.day);
         history.setWeek(dayAndWeek_.week);
         getThisWeekAndDaySet();
     }
 
     public DayAndWeek getDayAndWeek() {
         return new DayAndWeek(getDay(), getWeek());
     }
     
     public Logg addCountAndTimeLog(int count, long time_) {
         Logg log = history.getCurrentLog();
         log.addCountAndTime(count, time_);
         return log;
     }
 
     public void removeCurrentLog() {
         history.removeCurrentLog();
     }
 
     public boolean setCurrentLevel(Level level_) {
         history.setCurrentLevel(level_);
        getThisWeekAndDaySet();
         if (history.getDay() == 0) {
             history.setDay(1);
            return true;
         }
        return false;
     }
 
     /**
      * @return the counterActivityManager
      */
     protected CounterActivityManager getCounterActivityManager() {
         return counterActivityManager;
     }
 
     /**
      * @param counterActivityManager_ the counterActivityManager to set
      */
     public void setCounterActivityManager(CounterActivityManager counterActivityManager_) {
         counterActivityManager = counterActivityManager_;
     }
     /**
      * @return the history
      */
     public History getHistory() {
         return history;
     }
     /**
      * @param history_ the history to set
      */
     protected void setHistory(History history_) {
         history = history_;
     }
 
 }

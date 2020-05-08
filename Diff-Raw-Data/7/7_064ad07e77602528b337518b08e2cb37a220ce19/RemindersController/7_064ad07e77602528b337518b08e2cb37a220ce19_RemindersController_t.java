 package com.babycycle.babyfeeding.ui.controller;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import com.babycycle.babyfeeding.R;
 import com.babycycle.babyfeeding.model.PersistenceFacade;
 import com.babycycle.babyfeeding.model.Reminder;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dima
  * Date: 4/28/13
  * Time: 6:08 PM
  * To change this template use File | Settings | File Templates.
  */
 @Singleton
 public class RemindersController {
 
     private final long timeDelayForNextRemindersShowingInMillis = 1000*60*60;
 
     private Reminder chosenReminder;
 
     @Inject
     public RemindersController(Provider<Context> contextProvider) {
         setActivity(contextProvider.get());
     }
 
     WeakReference<Context> activity;
 
     private List<Reminder> reminders;
 
     private long lastRemindersShownTime = 0;
 
     @Inject
     PersistenceFacade persistenceFacade;
 
     public void setActivity(Context activity) {
         this.activity = new WeakReference<Context>(activity);
     }
 
     public void showReminders() {
         long currentTime = Calendar.getInstance(Locale.US).getTimeInMillis();
         if(tooEarlyToShowReminder()) return;
         lastRemindersShownTime = currentTime;
         reminders = persistenceFacade.getActiveReminders(activity.get());
         showNextReminder();
 
     }
 
     private boolean tooEarlyToShowReminder() {
         long currentTime = Calendar.getInstance(Locale.US).getTimeInMillis();
         if((currentTime -lastRemindersShownTime) < timeDelayForNextRemindersShowingInMillis) {
             return true;
         }
         lastRemindersShownTime = currentTime;
         return false;
     }
 
     private void showNextReminder() {
         chosenReminder = chooseNextReminderToShow();
         if(chosenReminder == null) {
             return;
         }
         showReminderOnScreen(chosenReminder);
     }
 
     private Reminder chooseNextReminderToShow() {
         if(reminders.size() == 0) return null;
         Reminder chosenReminder = reminders.get(0);
         reminders.remove(chosenReminder);
         return chosenReminder;
     }
 
     private void showReminderOnScreen(Reminder chosenReminder) {
         AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
 
         builder.setPositiveButton(R.string.reminder_confirm, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 setCurrentReminderConfirmed();
                 showNextReminder();
             }
         });
         builder.setNegativeButton(R.string.reminder_remind_later, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 showNextReminder();
             }
         });
         builder.setTitle(chosenReminder.getRemindMessage());
         builder.create().show();
     }
 
 
     private void setCurrentReminderConfirmed() {
         chosenReminder.setWasConfirmed(true);
         persistenceFacade.saveReminder(chosenReminder, activity.get());
     }
 }

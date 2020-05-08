 package ch.ffhs.esa.lifeguard.ui;
 
 import ch.ffhs.esa.lifeguard.ActivityMessage;
 import ch.ffhs.esa.lifeguard.Lifeguard;
 import ch.ffhs.esa.lifeguard.R;
 import ch.ffhs.esa.lifeguard.alarm.ServiceMessage;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmStateId;
 import android.app.Activity;
 import android.content.Intent;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 
 public class InitialView
     implements ViewStateStrategy
 {
     Activity activity;
 
     private CompoundButton.OnCheckedChangeListener tickToggleListener
         = new CompoundButton.OnCheckedChangeListener()
         {
             @Override
             public void onCheckedChanged (CompoundButton buttonView, boolean isChecked)
             {
                 buttonView.setEnabled (false);
                 triggerStartTicking ();
             }
         };
 
     @Override
     public void handleUi (Activity activity, Intent intent)
     {
         this.activity = activity;
 
         ProgressBar bar = (ProgressBar) activity.findViewById(R.id.progressBarDelay);
         bar.setProgress(bar.getMax ());
 
         Button sosButton = (Button) activity.findViewById (R.id.SOSButton);
         sosButton.setEnabled (false);
 
         CompoundButton tickButton
             = (CompoundButton) activity.findViewById (R.id.toggleButtonAlarmSwitch);
         tickButton.setOnCheckedChangeListener (null);
 
         sosButton.setText(R.string.main_label_sos);
         sosButton.setOnTouchListener (SOSButtonListener.create (
                 new Runnable () { public void run () { triggerManualAlarm (); }}));
         sosButton.setEnabled (true);
 
         tickButton.setChecked (false);
         tickButton.setOnCheckedChangeListener (tickToggleListener);
         tickButton.setEnabled (true);
 
         long maxTick
             = Long.parseLong(
                     activity.getSharedPreferences (
                         Lifeguard.APPLICATION_SETTINGS, Lifeguard.MODE_PRIVATE)
                     .getString (
                         activity.getString (R.string.alarmDelayConfigurationKey),
                         "600"));
 
         TextView delayText = (TextView) activity.findViewById (R.id.textViewDelay);
         delayText.setText (ClockTickFormatter.format (0, maxTick));
 
         // a message queue or something similar would be nice, something to handle
        // thos messages with (the messages below the big fat sos button).
         boolean wasCancelled = intent.getExtras ()
             .getBoolean (ServiceMessage.Key.WAS_CANCELLED);
         if (wasCancelled) {
             TextView buttonText = (TextView) activity.findViewById (
                     R.id.textViewSOSButton);
             buttonText.setText (activity.getString (R.string.operation_cancelled));
         }
     }
 
     @Override
     public void onClose ()
     {}
 
     private void triggerStartTicking ()
     {
         Intent intent = new Intent (ActivityMessage.STATE_CHANGE_REQUEST);
         intent.putExtra (ActivityMessage.Key.ALARM_STATE_ID,
                 AlarmStateId.TICKING.toString ());
 
         activity.sendBroadcast (intent);
     }
 
     private void triggerManualAlarm ()
     {
         Intent intent = new Intent (ActivityMessage.STATE_CHANGE_REQUEST);
         intent.putExtra (ActivityMessage.Key.ALARM_STATE_ID,
                 AlarmStateId.ALARMING.toString ());
 
         activity.sendBroadcast (intent);
     }
 }

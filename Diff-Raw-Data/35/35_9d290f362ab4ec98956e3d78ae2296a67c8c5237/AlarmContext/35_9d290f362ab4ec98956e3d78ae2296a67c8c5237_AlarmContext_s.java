 package ch.ffhs.esa.lifeguard.alarm.context;
 
 import android.content.Context;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmState;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmStateListener;
 
 /**
  * 
  * @author David Daniel <david.daniel@students.ffhs.ch>
  *
  */
 public interface AlarmContext
 {
     public Context getBaseContext ();
 
     public void setNext (AlarmState state);
 
     public AlarmState getState ();
 
     public void cancel ();
 
     public void addListener (AlarmStateListener listener);
 }

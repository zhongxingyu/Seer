 package ch.ffhs.esa.lifeguard.alarm.context;
 
import java.util.ArrayList;
 
 import android.content.Context;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmState;
 import ch.ffhs.esa.lifeguard.alarm.state.AlarmStateListener;
 import ch.ffhs.esa.lifeguard.alarm.state.InitialState;
 
 /**
  * 
  * @author David Daniel <david.daniel@students.ffhs.ch>
  *
  */
 public class ServiceAlarmContext
     implements AlarmContext
 {
     
 	/*//////////////////////////////////////////////////////////////////////////
 	 * PROPERTIES
 	 */
 	
	private ArrayList<AlarmStateListener> listeners
        = new ArrayList<AlarmStateListener> ();
 
     private AlarmState current = null;
 
    private Context baseContext;
 
     
     /*//////////////////////////////////////////////////////////////////////////
 	 * PUBLIC INTERFACE
 	 */
 
    public ServiceAlarmContext (Context baseContext)
     {
        this.baseContext = baseContext;
         current = new InitialState ();
     }
 
     @Override
     public Context getBaseContext ()
     {
        return baseContext;
     }
 
     @Override
     public void setNext (AlarmState state)
     {
         current = state;
         notifyListeners ();
         current.process (this);
     }
 
     @Override
     public AlarmState getState ()
     {
     	return current;
     }
 
     @Override
     public void addListener (AlarmStateListener listener)
     {
     	listeners.add (listener);
     }
 
     @Override
    public void removeListener (AlarmStateListener listener)
    {
        listeners.remove (listener);
    }

    @Override
     public void cancel ()
     {
         current.cancel ();
         current = new InitialState ();
     }
     
     /*//////////////////////////////////////////////////////////////////////////
 	 * PROTECTED OPERATIONS
 	 */
     
     protected void notifyListeners ()
     {
         for (AlarmStateListener l : listeners) {
             l.onStateChanged (this);
         }
     }

 }

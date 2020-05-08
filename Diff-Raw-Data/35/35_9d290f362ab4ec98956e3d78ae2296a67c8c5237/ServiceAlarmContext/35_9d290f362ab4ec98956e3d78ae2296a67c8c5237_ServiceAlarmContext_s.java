 package ch.ffhs.esa.lifeguard.alarm.context;
 
import java.util.TreeSet;
 
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
 	
	private TreeSet<AlarmStateListener> listeners
        = new TreeSet<AlarmStateListener> ();
 
     private AlarmState current = null;
 
    private Context serviceContext;
 
     
     /*//////////////////////////////////////////////////////////////////////////
 	 * PUBLIC INTERFACE
 	 */
 
    public ServiceAlarmContext (Context serviceContext)
     {
        this.serviceContext = serviceContext;
         current = new InitialState ();
     }
 
     @Override
     public Context getBaseContext ()
     {
        return serviceContext;
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

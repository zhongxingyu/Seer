 package ch.ffhs.esa.lifeguard.ui;
 
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 
 public class SOSButtonListener
     implements OnTouchListener
 {
     Handler handler = new Handler ();
 
     /** The action to run */
     Runnable realHandler;
 
     /** The button on that we listen */
     Button button;
 
     /**
      * Creates a new {@link SOSButtonListener}.
      * @param handler the handler to run on button activation
      */
     public SOSButtonListener (Runnable handler)
     {
         realHandler = handler;
     }
 
     @Override
     public boolean onTouch (View v, MotionEvent event)
     {
         // TODO: Visual button feedback (pressing button) should be possible
         switch (event.getAction ()) {
         case MotionEvent.ACTION_DOWN:
             handler.postDelayed (realHandler, 5000);
             break;
        case MotionEvent.ACTION_MOVE:
         case MotionEvent.ACTION_UP:
             handler.removeCallbacks (realHandler);
             break;
         }
 
         return true;
     }
 
     /**
      * Creates a new {@link SOSButtonListener} with the given handler.
      * @param handler the handler to run on button activation
      * @return
      */
     public static SOSButtonListener create (Runnable handler)
     {
         return new SOSButtonListener (handler);
     }
 }

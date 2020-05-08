 package org.idzaaus.twotaps;
 
 import java.util.ArrayList;
 
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class KeyboardButton {
   enum Type {
     BACKSPACE,
     SHIFT,
     SYSTEM_MENU,
     REGULAR
   }
   
   enum System_command {
     CAPS_LOCK,
     SETTINGS,
     NUMERIC
   }
   
   public interface OnHoldListener {
     void onKeyboardButtonHold(KeyboardButton target);
   } 
     
   public int number; //! General number of the button
   public int x, y;   //! Axis number of the button
   public Type type;   
   public int regularNumber; //! Only for type == REGULAR; number of the button over all regular buttons  
   public String singlePressLetter = null; 
   public System_command system_command = null;
   public String numericModeLetter = null;
   private boolean largeText = false;
   public TextView textView = null;
   public ImageButton imageButton = null;
   //private Service service = null;
   private float fontSize;
 
 
   
   public void setFontSize(float s) { 
     fontSize = s;
     setLargeText(largeText);
   }
   
 /*  public void setTextView(TextView v) { 
     textView = v;
     largeText = false;
   }*/
   
   private ArrayList<OnHoldListener> onHoldListeners = new ArrayList<OnHoldListener>();
   
   
   private final int repeatSpeed = 50;
   private final int repeatDelay = 500;
 
 
   public KeyboardButton(Service service, ImageButton imageButton, TextView textView) {
     this.textView = textView;
     this.imageButton = imageButton;
    //this.service = service;
 
     imageButton.setOnTouchListener(new View.OnTouchListener() {
       private Handler handler;
 
       @Override public boolean onTouch(View v, MotionEvent event) {
         switch(event.getAction()) {
         case MotionEvent.ACTION_DOWN:
           if (handler != null) return true;
           handler = new Handler();
           handler.postDelayed(action, repeatDelay);
           break;
         case MotionEvent.ACTION_UP:
           if (handler == null) return true;
           handler.removeCallbacks(action);
           handler = null;
           break;
         }
         return false;
       }
 
       Runnable action = new Runnable() {
         @Override public void run() {
           fireHoldEvent();
           handler.postDelayed(this, repeatSpeed);
         }
       };
 
     });
 
 
   }
   
   public void addOnHoldListener(OnHoldListener listener) {
     onHoldListeners.add(listener);
   }
   
   private void fireHoldEvent() {
     for(OnHoldListener listener : onHoldListeners) {
       listener.onKeyboardButtonHold(this);
     }
   }
   
   public void setLargeText(boolean enabled) {
 //    if (largeText != enabled) {
     largeText = enabled;
     textView.setTextSize(largeText? fontSize * 2 : fontSize);
      // textView.setTextAppearance(service, largeText? R.style.Button_candidate: R.style.Button_regular);
   //  }
   }
   
 
 }

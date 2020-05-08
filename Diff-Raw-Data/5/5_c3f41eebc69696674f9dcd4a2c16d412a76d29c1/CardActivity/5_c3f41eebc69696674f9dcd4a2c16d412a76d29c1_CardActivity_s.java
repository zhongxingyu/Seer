 // Planning Poker++ for Android, copyright (c) Richard Everett 2011
 // Full source available on GitHub at http://richev.github.com/PlanningPokerPlusPlus
 
 package com.richev.planningpokerplusplus;
 
 import com.richev.planningpokerplusplus.R;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.TypedValue;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 /**
  * The activity for showing a card that the user has selected
  * 
  * @author Rich
  * 
  */
 public class CardActivity extends MenuedActivity implements OnClickListener
 {
     private String _cardValue;
     private String[] _cardValues;
     
     private static final int TEXT_SIZE_ONE_CHAR = 340;
     private static final int TEXT_SIZE_TWO_CHARS = 260;
     private static final int TEXT_SIZE_THREE_CHARS = 180;
 
     private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
     private static final int SWIPE_THRESHOLD_VELOCITY = 200;
     private GestureDetector _gestureDetector;
     private View.OnTouchListener _gestureListener;
     
     private Preferences _prefs;
 
     /**
      * Based on code from http://stackoverflow.com/questions/937313/android-basic-gesture-detection/938657#938657
      *
      */
     private class MyGestureDetector extends SimpleOnGestureListener
     {
         @Override
         public boolean onSingleTapUp(MotionEvent e)
         {
             if (_prefs.getHideCardOnTap())
             {
                 toggleCardBack();
             }
             return false;
         }
         
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
         {
             if (!getCardBackShown()) // if the back of the card is shown, then flinging makes no sense
             {
                 if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                 {
                     return false;
                 }
                 
                 // right to left swipe
                 if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                 {
                     incrementCardValue();
                 }
                 else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
                 {
                     decrementCardValue();
                 }
                 
                 refreshCard();
             }
             return false;
         }
     }
 
     public void onClick(View v)
     {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.card);
 
         _prefs = new Preferences(PreferenceManager.getDefaultSharedPreferences(this));
         
         _cardValues = Utils.getCardValues(getResources(), _prefs);        
         _cardValue = (String)getIntent().getExtras().get("cardValue"); // set in MainActivity
 
         _gestureDetector = new GestureDetector(new MyGestureDetector());
         _gestureListener = new View.OnTouchListener()
         {
             public boolean onTouch(View v, MotionEvent event)
             {
                 return _gestureDetector.onTouchEvent(event);
             }
         };
 
         addListeners(R.id.cardContainer);
 
         refreshCard();
     }
 
     @Override
     public void onResume()
     {
         super.onResume();
 
         // The settings dialog may have been invoked...
         
         if (!_prefs.getHideCardOnTap() && getCardBackShown())
         {
             // The back of the card is shown, but this setting is no longer on
             toggleCardBack();
         }
 
         // This card value may no longer be allowed.
         _cardValues = Utils.getCardValues(getResources(), _prefs);
 
         Boolean cardValueAllowed = false;
 
         for (int i = 0; i < _cardValues.length; i++)
         {
             if (_cardValues[i].equals(_cardValue))
             {
                 cardValueAllowed = true;
                 break;
             }
         }
 
         if (!cardValueAllowed)
         {
             // Close this activity, which will return us to the main activity
             this.finish();
         }
     }
     
     private boolean getCardBackShown()
     {
         return findViewById(R.id.backLayout).getVisibility() == View.VISIBLE;
     }
     
     private void toggleCardBack()
     {
         if (getCardBackShown())
         {
             refreshCard();
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
         else
         {
             // Show the card back
             findViewById(R.id.cardLayout).setVisibility(View.INVISIBLE);
             findViewById(R.id.coffeeLayout).setVisibility(View.INVISIBLE);
             findViewById(R.id.backLayout).setVisibility(View.VISIBLE);
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         }
     }
     
     private void addListeners(int viewId)
     {
         View view = findViewById(viewId);
         view.setOnClickListener(CardActivity.this); 
         view.setOnTouchListener(_gestureListener);
     }
 
     private void incrementCardValue()
     {
         for (int i = 0; i < _cardValues.length; i++)
         {
             if (_cardValue.equals(_cardValues[i]))
             {
                 int nextCard = i + 1;
                 
                 if (nextCard <= _cardValues.length - 1)
                 {
                     _cardValue = _cardValues[nextCard];
                 }
                 else
                 {
                     vibrate(); // to hint to the user that there is no lower card
                 }
                 break;
             }
         }
     }
     
     private void decrementCardValue()
     {
         for (int i = 0; i < _cardValues.length; i++)
         {
             if (_cardValue.equals(_cardValues[i]))
             {
                 int prevCard = i - 1;
                 
                 if (prevCard >= 0)
                 {
                     _cardValue = _cardValues[prevCard];
                 }
                 else
                 {
                     vibrate(); // to hint to the user that there is no higher card
                 }
                 break;
             }
         }
     }
     
     private void vibrate()
     {
         Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
 
         vibrator.vibrate(50);
     }
 
     private void refreshCard()
     {
         findViewById(R.id.backLayout).setVisibility(View.INVISIBLE);
 
         if (_cardValue.equals("coffee"))
         {
             findViewById(R.id.cardLayout).setVisibility(View.INVISIBLE);
             findViewById(R.id.coffeeLayout).setVisibility(View.VISIBLE);
         }
         else
         {
             findViewById(R.id.cardLayout).setVisibility(View.VISIBLE);
             findViewById(R.id.coffeeLayout).setVisibility(View.INVISIBLE);
 
             TextView[] cardValueViews =
             {
                 (TextView)findViewById(R.id.cardValueTopLeft),
                 (TextView)findViewById(R.id.cardValueTopRight),
                 (TextView)findViewById(R.id.cardValueCenter),
                 (TextView)findViewById(R.id.cardValueBottomLeft),
                 (TextView)findViewById(R.id.cardValueBottomRight)
             };
     
             for (int i = 0; i < cardValueViews.length; i++)
             {
                 cardValueViews[i].setText(_cardValue);
             }
     
             float centerCardValueTextSize;
     
             switch (_cardValue.length())
             {
                 case 1:
                     centerCardValueTextSize = TEXT_SIZE_ONE_CHAR;
                     break;
                 case 2:
                     centerCardValueTextSize = TEXT_SIZE_TWO_CHARS;
                     break;
                 default:
                     centerCardValueTextSize = TEXT_SIZE_THREE_CHARS;
             }
     
             ((TextView)findViewById(R.id.cardValueCenter)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, centerCardValueTextSize);
         }
     }
 }

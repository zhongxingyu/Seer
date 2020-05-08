 package edu.ycp.cs481.ycpgames;
 
 import edu.ycp.cs481.ycpgames.util.SystemUiHider;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class TicTacToeActivity extends Activity {
     /**
      * Whether or not the system UI should be auto-hidden after
      * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
      */
     private static final boolean AUTO_HIDE = true;
 
     /**
      * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
      * user interaction before hiding the system UI.
      */
     private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
 
     /**
      * If set, will toggle the system UI visibility upon interaction. Otherwise,
      * will show the system UI visibility upon interaction.
      */
     private static final boolean TOGGLE_ON_CLICK = true;
 
     /**
      * The flags to pass to {@link SystemUiHider#getInstance}.
      */
     private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
 
     /**
      * The instance of the {@link SystemUiHider} for this activity.
      */
 
     private static final int X = 1;
     private static final int O = 2;
     private SystemUiHider mSystemUiHider;
     private TicTacToeGame game;
     private ImageButton[] buttons = new ImageButton[23];
     private int[][] tempGrid;
     private int currentTurn;
     protected ImageButton topLeftButton;
     protected ImageButton topCenterButton;
     protected ImageButton topRightButton;
     protected ImageButton centerLeftButton;
     protected ImageButton centerButton;
     protected ImageButton centerRightButton;
     protected ImageButton bottomLeftButton;
     protected ImageButton bottomCenterButton;
     protected ImageButton bottomRightButton;
     private int gameOver = 0;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         setContentView(R.layout.activity_tictactoe);
         game =  new TicTacToeGame();
         final View controlsView = findViewById(R.id.fullscreen_content_controls);
         final View contentView = findViewById(R.id.fullscreen_content);
         tempGrid = new int[3][3];
         for (int i = 0; i < 3; i++){
             for (int j = 0; j < 3; j++){
                 tempGrid[i][j] = 0;
             }
         }
 
         topLeftButton = (ImageButton) findViewById(R.id.topLeftButton);
         topCenterButton = (ImageButton) findViewById(R.id.topCenterButton);
         topRightButton = (ImageButton) findViewById(R.id.topRightButton);
         centerLeftButton = (ImageButton) findViewById(R.id.centerLeftButton);
         centerButton = (ImageButton) findViewById(R.id.centerButton);
         centerRightButton = (ImageButton) findViewById(R.id.centerRightButton);
         bottomLeftButton = (ImageButton) findViewById(R.id.bottomLeftButton);
         bottomCenterButton = (ImageButton) findViewById(R.id.bottomCenterButton);
         bottomRightButton = (ImageButton) findViewById(R.id.bottomRightButton);
 
         buttons[00] = bottomLeftButton;
         buttons[01] = bottomCenterButton;
         buttons[02] = bottomRightButton;
         buttons[10] = centerLeftButton;
         buttons[11] = centerButton;
         buttons[12] = centerRightButton;
         buttons[20] = topLeftButton;
         buttons[21] = topCenterButton;
         buttons[22] = topRightButton;
 
         // Set up an instance of SystemUiHider to control the system UI for
         // this activity.
         mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
         mSystemUiHider.setup();
         mSystemUiHider
                 .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                     // Cached values.
                     int mControlsHeight;
                     int mShortAnimTime;
 
                     @Override
                     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                     public void onVisibilityChange(boolean visible) {
                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                             // If the ViewPropertyAnimator API is available
                             // (Honeycomb MR2 and later), use it to animate the
                             // in-layout UI controls at the bottom of the
                             // screen.
                             if (mControlsHeight == 0) {
                                 mControlsHeight = controlsView.getHeight();
                             }
                             if (mShortAnimTime == 0) {
                                 mShortAnimTime = getResources().getInteger(
                                         android.R.integer.config_shortAnimTime);
                             }
                             controlsView.animate()
                                     .translationY(visible ? 0 : mControlsHeight)
                                     .setDuration(mShortAnimTime);
                         } else {
                             // If the ViewPropertyAnimator APIs aren't
                             // available, simply show or hide the in-layout UI
                             // controls.
                             controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                         }
 
                         if (visible) {
                             // Schedule a hide().
                             delayedHide(AUTO_HIDE_DELAY_MILLIS);
                         }
                     }
                 });
 
         // Set up the user interaction to manually show or hide the system UI.
         contentView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (TOGGLE_ON_CLICK) {
                     mSystemUiHider.toggle();
                 } else {
                     mSystemUiHider.show();
                 }
             }
         });
 
         topLeftButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(2,0);
 
                     }mUpdateView();
                 }
         });
 
         topCenterButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(2,1);
                     mUpdateView();
                 }
 
             }
         });
 
         topRightButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(2,2);
                     mUpdateView();
                 }
 
             }
         });
 
         centerLeftButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(1,0);
                     mUpdateView();
                 }
 
             }
         });
 
         centerButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(1,1);
                     mUpdateView();
                 }
 
             }
         });
 
         centerRightButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(1,2);
                     mUpdateView();
                 }
 
             }
         });
 
         bottomLeftButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(0,0);
                     mUpdateView();
                 }
 
             }
         });
 
         bottomCenterButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(0,1);
                     mUpdateView();
                 }
             }
         });
 
         bottomRightButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (game.whosTurn() == 1){
                     gameOver = game.move(0,2);
                     mUpdateView();
                 }
             }
         });
 
         // Upon interacting with UI controls, delay any scheduled hide()
         // operations to prevent the jarring behavior of controls going away
         // while interacting with the UI.
         //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
         // Trigger the initial hide() shortly after the activity has been
         // created, to briefly hint to the user that UI controls
         // are available.
         delayedHide(100);
     }
 
 
     /**
      * Touch listener to use for in-layout UI controls to delay hiding the
      * system UI. This is to prevent the jarring behavior of controls going away
      * while interacting with activity UI.
      */
     View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
         @Override
         public boolean onTouch(View view, MotionEvent motionEvent) {
             if (AUTO_HIDE) {
                 delayedHide(AUTO_HIDE_DELAY_MILLIS);
             }
             return false;
         }
     };
 
     Handler mHideHandler = new Handler();
     Runnable mHideRunnable = new Runnable() {
         @Override
         public void run() {
             mSystemUiHider.hide();
             mUpdateView();
         }
     };
 
     private void mUpdateView(){
         for (int i = 0; i< game.board.getGridHeight();i++){
             for(int j = 0; j< game.board.getGridWidth(); j++){
                 tempGrid[i][j] = game.board.getPieceAt(i,j);
                 switch(tempGrid[i][j]){
                     case X:
                         buttons[Integer.parseInt(Integer.toString(i) + Integer.toString(j))].setBackgroundResource(R.drawable.xbutton);
                         break;
                     case O:
                         buttons[Integer.parseInt(Integer.toString(i) + Integer.toString(j))].setBackgroundResource(R.drawable.obutton);
                         break;
                     default:
                         buttons[Integer.parseInt(Integer.toString(i) + Integer.toString(j))].setBackgroundResource(R.drawable.transbutton);
                         break;
                 }
             }
         }
 
         if(gameOver != 0){
             switch(gameOver){
                 case -1:
                     Toast.makeText(TicTacToeActivity.this, "It's a Draw!",Toast.LENGTH_SHORT).show();
                     break;
                 case 1:
                     Toast.makeText(TicTacToeActivity.this, "You Win!",Toast.LENGTH_SHORT).show();
                     break;
                 case 2:
                     Toast.makeText(TicTacToeActivity.this, "Computer wins!",Toast.LENGTH_SHORT).show();
                     break;
             }
         }
     }
 
     /**
      * Schedules a call to hide() in [delay] milliseconds, canceling any
      * previously scheduled calls.
      */
     private void delayedHide(int delayMillis) {
         mHideHandler.removeCallbacks(mHideRunnable);
         mHideHandler.postDelayed(mHideRunnable, delayMillis);
     }
 }

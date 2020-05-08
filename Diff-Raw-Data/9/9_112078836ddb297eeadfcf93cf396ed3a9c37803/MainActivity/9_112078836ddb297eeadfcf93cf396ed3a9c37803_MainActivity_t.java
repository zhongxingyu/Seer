 package com.bigtheta.ragedice;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GestureDetectorCompat;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity
         implements GameLogFragment.GameLogListener,
                    DiceDisplayFragment.DiceDisplayListener,
                    KSDescriptionFragment.KSDescriptionListener,
                    HistogramRollsFragment.HistogramRollsListener,
                    HistogramPlayerTimeFragment.HistogramPlayerTimeListener,
                    TabsFragment.TabsFragmentListener,
                    GestureDetector.OnGestureListener,
                    GestureDetector.OnDoubleTapListener {
 
     private GestureDetectorCompat m_gestureDetector;
     private static SQLiteDatabase m_database;
     private MySQLiteHelper m_dbHelper;
     private static Game m_game;
     private FragmentManager fm;
 
     public static SQLiteDatabase getDatabase() {
         return m_database;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         m_gestureDetector = new GestureDetectorCompat(this, this);
         m_gestureDetector.setOnDoubleTapListener(this);
         m_dbHelper = new MySQLiteHelper(this);
 
        initializeGame();
        addPlayer();
        addPlayer();
        addPlayer();

         m_database = m_dbHelper.getWritableDatabase();
 
         fm = getSupportFragmentManager();
         setContentView(R.layout.activity_main);
     }
 
     private void initializeGame() {
        Log.e("> initializeGame()", "");
         this.deleteDatabase("rage_dice.db");
        m_database = m_dbHelper.getWritableDatabase();
         m_game = new Game();
         addPlayer();
         new DieDescription(m_game, 1, 6, "alea_transface_colbg_",
                            R.color.yellow_die, R.id.yellow_die, DieDescription.NUMERIC);
         new DieDescription(m_game, 1, 6, "alea_transface_colbg_",
                            R.color.red_die, R.id.red_die, DieDescription.NUMERIC);
         new DieDescription(m_game, 1, 6, "ship_die_",
                            R.color.background, R.id.ship_die, DieDescription.SHIP);
     }
 
     static void addPlayer() {
         int playerNum = Player.getNumPlayers() + 1;
         new Player(m_game, playerNum, "Player " + Integer.toString(playerNum));
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         m_database = m_dbHelper.getWritableDatabase();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         m_dbHelper.close();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == R.id.menu_undo_dice_roll) {
             undoDiceRoll(null);
         }
         return true;
     }
 
     public void resetDiceRolls(View view) {
         DiceRoll.clear(m_game.getId());
         //displayInfo();
         refreshDisplay();
     }
 
     public void undoDiceRoll(View view) {
         DiceRoll dr = DiceRoll.getLastDiceRoll(m_game.getId());
         if (dr == null) {
             Context context = getApplicationContext();
             CharSequence text = getResources().getText(R.string.toast_msg_cannot_undo);
             int duration = Toast.LENGTH_SHORT;
             Toast toast = Toast.makeText(context, text, duration);
             toast.setGravity(Gravity.TOP, 0, 200);
             toast.show();
             return;
         }
         dr.delete();
         refreshDisplay();
     }
 
     public void rollDice(View view) {
         Player nextPlayer = Player.getNextPlayer(m_game.getId());
         Log.e("player id at 116:", Long.toString(nextPlayer.getId()));
         DiceRoll dr = new DiceRoll(nextPlayer);
         refreshDisplay();
     }
 
     public void nextFragment() {
         TabsFragment tf = getTabsFragment();
         if (tf == null) {
             throw new IllegalStateException("Tabs ui doesn't exist.");
         }else {
             tf.nextTab();
             refreshDisplay();
         }
     }
 
     public void prevFragment() {
         TabsFragment tf = getTabsFragment();
         if (tf == null) {
             throw new IllegalStateException("Tabs ui doesn't exist.");
         }else {
             tf.prevTab();
             refreshDisplay();
         }
     }
 
     public TabsFragment getTabsFragment() {
         TabsFragment tf = (TabsFragment) fm.findFragmentById(R.id.tabs_fragment_ui);
         return tf;
     }
 
     public void refreshDisplay() {
         DiceRoll dr = DiceRoll.getLastDiceRoll(m_game.getId());
         Player nextPlayer = Player.getLastPlayer(m_game.getId());
         DiceDisplayFragment ddf = (DiceDisplayFragment)
                 fm.findFragmentById(R.id.dice_fragment_ui);
         GameLogFragment glf = (GameLogFragment) fm.findFragmentByTag("glf");
 
         if (ddf != null && ddf.isVisible()) {
             ddf.displayDiceRoll(dr);
         }
         TabsFragment tf = getTabsFragment();
         if (tf == null) {
             throw new IllegalStateException("Tabs ui doesn't exist.");
         }else {
             tf.refreshDisplay();
         }
     }
 
     @Override
     public void onDiceSelected(int position) {
     }
 
     @Override
     public void onGameLogSelected(int position) {
     }
 
     @Override
     public void onKSDescriptionSelected(int position) {
     }
 
     @Override
     public void onHistogramRollsSelected(int position) {
     }
 
     @Override
     public void onHistogramPlayerTimeSelected(int position) {
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         refreshDisplay();
     }
 
     public static Game getGame() {
         return m_game;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event){
         this.m_gestureDetector.onTouchEvent(event);
         // Be sure to call the superclass implementation
         return super.onTouchEvent(event);
     }
 
     @Override
     public boolean onDown(MotionEvent event) {
         return true;
     }
 
     @Override
     public boolean onFling(MotionEvent start, MotionEvent finish,
                            float velocityX, float velocityY) {
         float deltaX = Math.abs(finish.getRawX() - start.getRawX());
         float deltaY = Math.abs(finish.getRawY() - start.getRawY());
         if (deltaX > deltaY) {
             if (finish.getRawX() < start.getRawX()) {
                 nextFragment();
             } else {
                 prevFragment();
             }
         }
 
         return true;
     }
 
     @Override
     public void onLongPress(MotionEvent event) {
     }
 
     @Override
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                             float distanceY) {
         return true;
     }
 
     @Override
     public void onShowPress(MotionEvent event) {
     }
 
     @Override
     public boolean onSingleTapUp(MotionEvent event) {
         return true;
     }
 
     @Override
     public boolean onDoubleTap(MotionEvent event) {
         return true;
     }
 
     @Override
     public boolean onDoubleTapEvent(MotionEvent event) {
         return true;
     }
 
     @Override
     public boolean onSingleTapConfirmed(MotionEvent event) {
         return true;
     }
 }
 

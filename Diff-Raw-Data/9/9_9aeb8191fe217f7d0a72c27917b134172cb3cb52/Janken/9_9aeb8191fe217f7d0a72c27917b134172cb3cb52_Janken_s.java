 package me.kukkii.janken;
 
 import me.kukkii.janken.bot.BotManager;
 import me.kukkii.janken.bot.AbstractBot;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.Toast;
 import android.widget.TextView;
 import android.graphics.drawable.Drawable;
 import android.content.res.Resources;
 import android.database.sqlite.SQLiteDatabase;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.util.Log;
 import com.google.ads.*;
 
 public class Janken extends Activity{
 
   private static final String tag = "janken";
 
   private AbstractBot bot;
   private Hand userHand;
   private Hand botHand;
   private Result result;
   private Judge judge = new Judge();
   private SQLiteDatabase mydb;
 
   private int numberOfWin = 0;
   private int numberOfDraw = 0;
   private int numberOfLose = 0;
 
   private static final int timeJan = 2500;
   private static final int timeKen = 1000;
   private static final int timePon = 1000;
   private static final int timeAfterPon = 50;
   private static final int timeTilNewGame = 2500;
   private static final int timeTilPon = timeJan + timeKen + timePon;
 
   private MySQLiteOpenHelper hlpr;
 
   private boolean resumed;
 
   public void onCreate(Bundle savedInstanceState){
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     AdView adView = (AdView)this.findViewById(R.id.adView);
     adView.loadAd(new AdRequest());
   }
 
   public void onStart(){
     super.onStart();
 
     hlpr = new MySQLiteOpenHelper(getApplicationContext());
     mydb = hlpr.getWritableDatabase();
 
     SQLiteDatabase radb = hlpr.getReadableDatabase();
     Cursor cursor = radb.query("logtable", new String[] {"result"}, null, null, null, null, null, null);
     while(cursor.moveToNext()){
       int n = cursor.getInt(0);
       if(n == Result.WIN.value()){
         numberOfWin += 1; 
       }
       if(n == Result.LOSE.value()){
         numberOfLose += 1; 
       }
       if(n == Result.DRAW.value()){
         numberOfDraw += 1; 
       }
     }
   }
 
   protected void onStop() {
     super.onStop();
     hlpr.close();
     Log.i(tag, "hlpr was closed");
   }
 
   public void onResume(){
     super.onResume();
     resumed = true;
     newGame();
   }
 
   public void onPause(){
     super.onPause();
     resumed = false;
   }
 
   public void afterPon(){
 //    botHand = bot.hand2();
     if(resumed == false){
       return;
     }
     result = judge.judge(userHand, botHand);
     ContentValues values = new ContentValues();
     values.put("result", result.value());
     mydb.insert("logtable", null, values);
     String text = bot.getName() + ":" + userHand.toString() + ":" + botHand.toString() + ":" + result.toString() + "\n" + history();
     showResultOnUiThread(text);
     sleep(timeTilNewGame);
     newGame();
   }
 
   public void showResultOnUiThread(String text) {
     final String text0 = text;
     final TextView view = (TextView) findViewById(R.id.text);
     runOnUiThread(new Runnable() {
       public void run() {
         view.setText(text0);
 //        setContentView(view);
       }
     });
   }
 
   public void newGame(){
     bot =(AbstractBot) BotManager.getManager().next();
     Resources res = getResources();
     final int drawableId = bot.getImage();
     final ImageView view = (ImageView) findViewById(R.id.view_BOT);
     runOnUiThread(new Runnable() {
       public void run() {
         view.setImageResource(drawableId); 
       }
     });
 
     Runnable tt = new Runnable() {
       public void run() {
         game();
       }
     };
     Thread t = new Thread(tt);
     t.start();
    
      Runnable botRun = new Runnable() {
       public void run() {
         sleep(timeTilPon);
         botHand(bot.hand2());
       }
     };
     Thread botThread = new Thread(botRun);
     botThread.start();
   }
 
   public void game() {
     sleep(timeJan);
     jan();
     sleep(timeKen);
     ken();
     sleep(timePon);
     pon();
     sleep(timeAfterPon);
     afterPon();
   }
 
   public void sleep(int msec) {
     try{
       Thread.sleep(msec);
     }
     catch (InterruptedException e){
     }
   }
 
   public void jan(){
     showResultOnUiThread("Jan");
   }
 
   public void ken(){
     showResultOnUiThread("Ken");
   }
     
   public void pon(){
     showResultOnUiThread("Pon");
   }
 
   public void hand(View view){
     userHand = null;
     int id = view.getId();
     if(id==R.id.button_ROCK){
       userHand = Hand.ROCK;
     }
     if(id==R.id.button_SCISSOR){
       userHand = Hand.SCISSOR;
     }
     if(id==R.id.button_PAPER){
       userHand = Hand.PAPER;
     }
     Toast.makeText(getApplicationContext(), userHand.toString(), Toast.LENGTH_SHORT).show();
   }
 
   public void botHand(Hand hand){
     botHand = hand;
     int botHandImage = 0;
     if(botHand == hand.ROCK){
       botHandImage = R.drawable.hand_rock;
     }
     if(botHand == hand.SCISSOR){
       botHandImage = R.drawable.hand_scissor;
     }
     if(botHand == hand.PAPER){
       botHandImage = R.drawable.hand_paper;
     }
     Resources res = getResources();
     final int drawableId = botHandImage;
     final ImageView view = (ImageView) findViewById(R.id.view_BOT);
     runOnUiThread(new Runnable() {
       public void run() {
         view.setImageResource(drawableId); 
       }
     });
   }
 
   public String history(){
     if(result == Result.WIN){
       numberOfWin += 1;
     }
     if(result == Result.LOSE){
       numberOfLose += 1;
     }
     if(result == Result.DRAW){
       numberOfDraw += 1;
     }
     String history = numberOfWin + " win, " + numberOfLose + " lose, " + numberOfDraw + " draw";
     return history;
   }
 }

 package com.bigtheta.ragedice;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.bigtheta.ragedice.R.drawable;
 
 public class MainActivity extends Activity {
     private static SQLiteDatabase m_database;
     private MySQLiteHelper m_dbHelper;
     private Game m_game;
 
     public static SQLiteDatabase getDatabase() {
         return m_database;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         m_dbHelper = new MySQLiteHelper(this);
         // REMOVEME
         this.deleteDatabase("rage_dice.db");
 
         m_database = m_dbHelper.getWritableDatabase();
         m_game = new Game();
 
         new Player(m_game, 1, "player one");
         new Player(m_game, 2, "player two");
         new Player(m_game, 3, "player awesome (three)");
         new Player(m_game, 4, "player better than awesome (four)");
 
         new DieDescription(m_game, 1, 6, "alea_transface_colbg_",
                           0xFFFFFF00, R.string.yellow_die, true);
         new DieDescription(m_game, 1, 6, "alea_transface_colbg_",
                            0xFFFF0000, R.id.red_die, true);
 
         View mainView = (View)findViewById(R.id.activity_main_view);
         mainView.setBackgroundColor(0xFF818181);
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
 
     protected void displayDiceRoll(DiceRoll dr) {
         TextView tv = (TextView)findViewById(R.id.player_number);
         Player currentPlayer = Player.retrieve(dr.getPlayerId());
         tv.setText(currentPlayer.getPlayerName());
 
         Class<drawable> res = R.drawable.class;
         for (DieResult result : DieResult.getDieResults(dr)) {
             DieDescription dd = DieDescription.retrieve(result.getDieDescriptionId());
             ImageView iv = (ImageView)findViewById(dd.getImageViewResource());
             String description = dd.getBaseIdentifierName()
                                + Integer.toString(result.getDieResult());
             try {
                 Field field = res.getField(description);
                 iv.setImageResource(field.getInt(null));
                iv.setBackgroundColor(dd.getBackgroundColor());
             } catch (Exception err){
                 Log.e("MainActivity::displayDiceRoll", err.getCause().getMessage());
             }
         }
         displayInfo();
     }
 
     protected void displayInfo() {
         TextView tv = (TextView)findViewById(R.id.debug_info);
         String info = "";
         info += "numDiceRolls: " + Integer.toString(DiceRoll.getNumDiceRolls());
         HashMap<Integer, Double> pmf = DieDescription.getPMF(m_game.getId());
         for (Integer observation : pmf.keySet()) {
             info += "\nObservation: " + Integer.toString(observation)
                   + " Probability: " + pmf.get(observation);
         }
         HashMap<Integer, Integer> dist = DiceRoll.getObservedRolls(m_game.getId());
         for (Integer observation : dist.keySet()) {
             info += "\nObservation: " + Integer.toString(observation)
                   + " Count: " + dist.get(observation);
         }
         double stat = DiceRoll.calculateKSTestStatistic(m_game.getId());
         info += "\nKS statistic: " + Double.toString(stat);
         // Probability that these are different distributions.
         info += "\nKS probability: " + Double.toString(DiceRoll.calculateKSProbability(m_game.getId()));
 
         tv.setText(info);
     }
 
     public void resetDiceRolls(View view) {
         DiceRoll.clear();
         displayInfo();
     }
 
     public void undoDiceRoll(View view) {
         DiceRoll dr = DiceRoll.getLastDiceRoll();
         dr.delete();
         dr = DiceRoll.getLastDiceRoll();
         displayDiceRoll(dr);
     }
 
     public void rollDice(View view) {
         Player nextPlayer = Player.getNextPlayer(m_game);
         DiceRoll dr = new DiceRoll(nextPlayer);
        dr = new DiceRoll(nextPlayer);
         displayDiceRoll(dr);
     }
 }
 

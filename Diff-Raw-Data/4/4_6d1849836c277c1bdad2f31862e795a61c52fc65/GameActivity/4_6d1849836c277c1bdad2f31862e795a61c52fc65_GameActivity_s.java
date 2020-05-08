 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameActivity extends Activity {
 
     private static final String PREFS_NAME = "Prefs";
     public static final int MODE_SINGLEPLAYER = 0;
     public static final int MODE_MULTIPLAYER_SHARED = 1;
     public static final int MODE_MULTIPLAYER_JOIN = 2;
     public static final int MODE_MULTIPLAYER_HOST = 3;
     private static final String TAG = GameActivity.class.getSimpleName();
     private GameView gameView;
     private TextView status;
     private int mode;
     private int boardSize;
     private int inarow;
     private boolean canMove;
 
     @Override
     public void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.game);
         this.mode = getIntent().getExtras().getInt("mode");
         SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
         boardSize = settings.getInt("boardSize", 3);
         inarow = settings.getInt("inarow", boardSize);
         status = (TextView) findViewById(R.id.status);
         gameView = (GameView) findViewById(R.id.game_view);
         gameView.setFocusable(true);
         gameView.setFocusableInTouchMode(true);
         gameView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) {
                     return true;
                 } else if (action == MotionEvent.ACTION_UP) {
                     int x = (int) event.getX();
                     int y = (int) event.getY();
                     int sxy = gameView.getSxy();
                     x = (x - 0) / sxy;
                     y = (y - 0) / sxy;
                     Toast.makeText(getApplicationContext(), String.valueOf(x + " " + y), Toast.LENGTH_SHORT).show();
                     if (canMove && x >= 0 && x < gameView.getBoardSize() && y >= 0 & y < gameView.getBoardSize()) {
                         int cell = x + gameView.getBoardSize() * y;
                         GamePlayer state = cell == gameView.getSelectedCell() ? gameView.getSelectedValue() : gameView.getBoard().get(x, y);
                         state = state == GamePlayer.EMPTY ? gameView.getBoard().getCurrentPlayer() : GamePlayer.EMPTY;
                         gameView.setSelectedCell(cell);
                         gameView.setSelectedValue(state);
                         if (gameView.getBoard().get(x, y) == GamePlayer.EMPTY) {
                             setCell(x, y, state);
                             if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                                 if (gameView.getBoard().getCurrentPlayer() == GamePlayer.PLAYER1) {
                                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                                     status.setText("Player 2's turn");
                                 } else {
                                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                                     status.setText("Player 1's turn");
                                 }
                             }
                         }
                     }
                     return true;
                 }
                 return false;
             }
         });
         switch (mode) {
             case MODE_SINGLEPLAYER: {
                 gameView.makeBoard(boardSize, inarow);
                 break;
             }
             case MODE_MULTIPLAYER_SHARED: {
                 gameView.makeBoard(boardSize, inarow);
                 canMove = true;
                 break;
             }
         }
     }
 
     public void setCell(int x, int y, GamePlayer player) {
         if (gameView.getBoard().put(x, y, player) == GameState.VALID_MOVE) {
             GameState s = gameView.getBoard().getState();
             switch (s) {
                 case WIN: {
                     gameView.setEnabled(false);
                     status.setText(String.format(getResources().getString(R.string.win),
                             player.toString()));
                     break;
                 }
                 case DRAW: {
                     gameView.setEnabled(false);
                     status.setText(R.string.draw);
                     break;
                 }
             }
         }
         gameView.invalidate();
     }
 }
 

 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.graphics.PorterDuff;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class GameActivity extends Activity {
 
     public static final int MODE_SINGLEPLAYER = 0;
     public static final int MODE_MULTIPLAYER_SHARED = 1;
     public static final int MODE_MULTIPLAYER_JOIN = 2;
     public static final int MODE_MULTIPLAYER_HOST = 3;
 
     private static final int DEFAULT_BOARD_SIZE = 3;
 
     private static final String TAG = GameActivity.class.getSimpleName();
     private static final String PREFS_NAME = "Prefs";
     private static final int PORT = 8080;
     private static final String INIT_REQUEST = "init";
     private static final String INIT_RESPONSE_OK = "init ok";
     private static final String NEW_GAME = "new game";
 
     private GameView gameView;
     private LinearLayout gameViewLayout;
     private LinearLayout gameViewHolder;
     private TextView textStatus;
     private TextView tv_lengthToWin;
     private ImageView status;
     private Button replay;
     private GameAI AI;
 
     private int mode;
     private int boardSize;
     private int inRow;
 
     private String localIp;
     private String remoteIp;
     private ServerSocket serverSocket;
     private Socket clientSocket;
     private PrintWriter out;
 
     @Override
     protected void onCreate(Bundle bundle) {
         super.onCreate(bundle);
         setContentView(R.layout.game);
         SharedPreferences settings = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
         this.mode = getIntent().getExtras().getInt("mode");
         this.remoteIp = getIntent().getExtras().get("remoteIp") != null ?
                 getIntent().getExtras().get("remoteIp").toString() : null;
         this.localIp = findIpAddress();
         this.boardSize = settings.getInt("boardSize", DEFAULT_BOARD_SIZE);
         this.inRow = settings.getInt("inRow", boardSize);
         this.textStatus = (TextView) findViewById(R.id.status);
         this.tv_lengthToWin = (TextView) findViewById(R.id.tv_lengthToWin);
         this.replay = new Button(this);
         tv_lengthToWin.setText(String.format(getString(R.string.required_length_to_win, inRow)));
         this.status = (ImageView) findViewById(R.id.imageview_status);
         this.gameViewHolder = (LinearLayout) findViewById(R.id.game_view_holder);
         this.gameViewLayout = (LinearLayout) findViewById(R.id.gameview_layout);
         switch (mode) {
             case MODE_SINGLEPLAYER: {
                 createGameView(boardSize, inRow);
                 int difficulty = getIntent().getExtras().getInt("difficulty");
                 AI = new GameAI(gameView.getBoard(), difficulty);
                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                 updateStatus();
                 break;
             }
             case MODE_MULTIPLAYER_SHARED: {
                 createGameView(boardSize, inRow);
                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                 updateStatus();
                 break;
             }
             case MODE_MULTIPLAYER_HOST: {
                 createGameView(boardSize, inRow);
                 gameView.setEnabled(false);
                 serverThread.start();
                 break;
             }
             case MODE_MULTIPLAYER_JOIN: {
                 clientThread.start();
                 break;
             }
         }
     }
 
     /**
      * Updates the status text to the current player
      */
     private void updateStatus() {
         GamePlayer player = gameView.getBoard().getCurrentPlayer();
         if (player == GamePlayer.PLAYER1) {
             status.setImageDrawable(getResources().getDrawable(R.drawable.xturn));
         } else if (player == GamePlayer.PLAYER2) {
             status.setImageDrawable(getResources().getDrawable(R.drawable.oturn));
         }
     }
 
     /**
      * Creates the GameView and attaches an onTouchListener which handles the coordinates a player clicks.
      * @param boardSize
      * @param inRow
      */
     private void createGameView(int boardSize, int inRow) {
         this.gameView = new GameView(this, boardSize, inRow);
         this.gameView.setFocusable(true);
         this.gameView.setFocusableInTouchMode(true);
         this.gameView.setOnTouchListener(new View.OnTouchListener() {
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 int action = event.getAction();
                 if (action == MotionEvent.ACTION_DOWN) {
                     return true;
                 } else if (action == MotionEvent.ACTION_UP) {
                     int sxy = gameView.getSxy();
                     int x = (int) event.getX() / sxy;
                     int y = (int) event.getY() / sxy;
                     if (gameView.isEnabled() && x >= 0 && x < gameView.getBoardSize() && y >= 0 & y < gameView.getBoardSize()) {
                         if (gameView.getBoard().get(x, y) == GamePlayer.EMPTY) {
                             GamePlayer player = gameView.getBoard().getCurrentPlayer();
                             putPlayer(x, y, player);
 
                         }
                     }
                     return true;
                 }
                 return false;
             }
         });
         gameViewHolder.addView(gameView);
     }
 
     /**
      * Does a move on x and y if it is valid
      * @param x
      * @param y
      * @param player
      */
     private void putPlayer(int x, int y, GamePlayer player) {
         if (gameView.getBoard().put(x, y, player) == GameState.INVALID_MOVE) {
             return;
         }
         switch (mode) {
             case MODE_SINGLEPLAYER: {
                 if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                     if (gameView.getBoard().getCurrentPlayer() == GamePlayer.PLAYER1) {
                         gameView.setEnabled(false);
                         gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                         Position AImove = AI.getMove();
                         putPlayer(AImove.getX(), AImove.getY(), gameView.getBoard().getCurrentPlayer());
                         return;
                     } else {
                         gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                         gameView.setEnabled(true);
                     }
                 }
                 break;
             }
             case MODE_MULTIPLAYER_SHARED: {
                 if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                     if (gameView.getBoard().getCurrentPlayer() == GamePlayer.PLAYER1) {
                         gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                     } else {
                         gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                     }
                 }
                 break;
             }
             case MODE_MULTIPLAYER_HOST: {
                 out.printf("%d %d\n", x, y);
                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                 gameView.setEnabled(false);
                 break;
             }
             case MODE_MULTIPLAYER_JOIN: {
                 out.printf("%d %d\n", x, y);
                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                 gameView.setEnabled(false);
                 break;
             }
         }
         updateStatus();
         if (mode != MODE_SINGLEPLAYER) {
             updateState(player);
         } else {
             updateState(gameView.getBoard().getCurrentPlayer());
         }
         gameView.invalidate();
     }
 
     /**
      * Updates the state of the game if it is a WIN or DRAW, and then shows a play again button.
      * @param player
      */
     private void updateState(GamePlayer player) {
         GameState s = gameView.getBoard().getState();
         switch (s) {
             case WIN: {
                 gameView.setEnabled(false);
                 setupPlayAgain();
                 if (player == GamePlayer.PLAYER1) {
                     status.setImageDrawable(getResources().getDrawable(R.drawable.xwins));
                     break;
                 } else if (player == GamePlayer.PLAYER2) {
                     status.setImageDrawable(getResources().getDrawable(R.drawable.owins));
                     break;
                 }
             }
             case DRAW: {
                 gameView.setEnabled(false);
                 setupPlayAgain();
                 status.setImageDrawable(getResources().getDrawable(R.drawable.draw));
                 break;
             }
         }
     }
 
     /**
      * Creates a Button for play again after a WIN or DRAW.
      */
     private void setupPlayAgain() {
         replay.setLayoutParams(new ViewGroup.LayoutParams(
                 ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
         replay.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_playagain));
         replay.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 playAgain();
                 switch (mode) {
                     case MODE_MULTIPLAYER_HOST: {
                         out.println(NEW_GAME);
                        gameView.setEnabled(true);
                         break;
                     }
                     case MODE_MULTIPLAYER_JOIN: {
                         gameView.setEnabled(false);
                         out.println(NEW_GAME);
                         break;
                     }
                     case MODE_SINGLEPLAYER: {
                         AI = new GameAI(gameView.getBoard(), GameAI.EASY);
                     }
                 }
             }
         });
         gameViewLayout.addView(replay);
     }
 
     /**
      * Deletes the current gameView and attaches a new one. Also removes the play again button.
      */
     private void playAgain() {
         gameViewHolder.removeView(gameView);
         createGameView(boardSize, inRow);
         gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
         updateStatus();
         gameViewLayout.removeView(replay);
     }
 
     /**
      * Get the device WIFI IP-address
      * @return IP-address of the device
      */
     private String findIpAddress() {
         final WifiInfo wifiInfo = ((WifiManager) getSystemService(WIFI_SERVICE)).getConnectionInfo();
         return Formatter.formatIpAddress(wifiInfo.getIpAddress());
     }
 
     /**
      * A Handler that handles the initial request for network play
      */
     private final Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             final String s = msg.getData().getString("message");
             if (INIT_REQUEST.equals(s)) {
                 Log.d(TAG, "Sent init request");
                 out.printf("%s %d %d\n", INIT_REQUEST, boardSize, inRow);
             } else {
                 textStatus.setText(s);
             }
         }
     };
 
     /**
      * Sends a message through the handler.
      * @param s
      */
     private void sendMessage(String s) {
         final Message msg = handler.obtainMessage();
         final Bundle bundle = new Bundle();
         bundle.putString("message", s);
         msg.setData(bundle);
         handler.sendMessage(msg);
     }
 
     private void sendMessage(int resId) {
         sendMessage(getResources().getString(resId));
     }
 
     /**
      * Parses the size of the game Board from string
      * @param line
      * @return Board size
      */
     private int[] parseSize(String line) {
         String[] words = line.split(" ");
         return new int[]{Integer.parseInt(words[1]), Integer.parseInt(words[2])};
     }
 
     /**
      * Parses the x and y coordinate from the given string
      * @param line
      * @return Integer[] containing x and y data of a move.
      */
     private int[] parseMove(String line) {
         String[] words = line.split(" ");
         return new int[]{Integer.parseInt(words[0]), Integer.parseInt(words[1])};
     }
 
     /**
      * Client thread
      */
     private final Thread clientThread = new Thread() {
         @Override
         public void run() {
             try {
                 clientSocket = new Socket(remoteIp, PORT);
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
             }
             if (clientSocket == null) {
                 sendMessage(R.string.could_not_connect);
                 return;
             }
             BufferedReader in = null;
             try {
                 out = new PrintWriter(clientSocket.getOutputStream(), true);
                 in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 String line;
                 while ((line = in.readLine()) != null) {
                     Log.d(TAG, "Client thread received: " + line);
                     if (line.startsWith(INIT_REQUEST)) {
                         final int[] boardParams = parseSize(line);
                         out.println(INIT_RESPONSE_OK);
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 boardSize = boardParams[0];
                                 inRow = boardParams[1];
                                 tv_lengthToWin.setText(String.format(getString(R.string.required_length_to_win, inRow)));
                                 createGameView(boardSize, inRow);
                                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                                 updateStatus();
                                 gameView.setEnabled(false);
                             }
                         });
                     } else if (NEW_GAME.equals(line)) {
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 playAgain();
                                 gameView.setEnabled(false);
                             }
                         });
                     } else {
                         final int[] xy = parseMove(line);
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 gameView.getBoard().put(xy[0], xy[1], GamePlayer.PLAYER1);
                                 gameView.setEnabled(true);
                                 updateState(gameView.getBoard().getCurrentPlayer());
                                 gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER2);
                                 if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                                     updateStatus();
                                 }
                             }
                         });
                         gameView.postInvalidate();
                     }
                 }
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
                 sendMessage(R.string.connection_failed);
             } finally {
                 closeInAndOut(in, out);
             }
         }
     };
 
     /**
      * Server thread
      */
     private final Thread serverThread = new Thread() {
         @Override
         public void run() {
             try {
                 serverSocket = new ServerSocket(PORT);
                 sendMessage(String.format(getResources().getString(R.string.waiting_for_player), localIp));
             } catch (IOException e) {
                 Log.e(TAG, "IOException", e);
             }
             if (serverSocket == null) {
                 sendMessage(R.string.server_socket_failed);
                 Log.e(TAG, "Server socket is null");
                 return;
             }
             while (true) {
                 Socket clientSocket = null;
                 try {
                     clientSocket = serverSocket.accept();
                     Log.d(TAG, "Received client connection");
                     sendMessage(R.string.connected);
                 } catch (IOException e) {
                     Log.e(TAG, "IOException", e);
                 }
                 if (clientSocket == null) {
                     sendMessage(R.string.client_socket_failed);
                     Log.e(TAG, "Client socket is null");
                     return;
                 }
                 BufferedReader in = null;
                 try {
                     out = new PrintWriter(clientSocket.getOutputStream(), true);
                     sendMessage(INIT_REQUEST);
                     in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     String line;
                     while ((line = in.readLine()) != null) {
                         Log.d(TAG, "Server thread received: " + line);
                         if (INIT_RESPONSE_OK.equals(line)) {
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                                     updateStatus();
                                     gameView.setEnabled(true);
                                 }
                             });
                         } else if (NEW_GAME.equals(line)) {
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     playAgain();
                                    gameView.setEnabled(true);
                                 }
                             });
                         } else {
                             final int[] xy = parseMove(line);
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     gameView.getBoard().put(xy[0], xy[1], GamePlayer.PLAYER2);
                                     gameView.setEnabled(true);
                                     updateState(gameView.getBoard().getCurrentPlayer());
                                     gameView.getBoard().setCurrentPlayer(GamePlayer.PLAYER1);
                                     if (gameView.getBoard().getState() == GameState.NEUTRAL) {
                                         updateStatus();
                                     }
                                 }
                             });
                         }
                         gameView.postInvalidate();
                     }
                 } catch (IOException e) {
                     Log.w(TAG, "IOException", e);
                     sendMessage(R.string.connection_failed);
                 } finally {
                     closeInAndOut(in, out);
                 }
             }
         }
     };
 
     /**
      * Closes Reader in and Writer out
      * @param in
      * @param out
      */
     private void closeInAndOut(Reader in, Writer out) {
         if (in != null) {
             try {
                 in.close();
             } catch (IOException e) {
                 Log.w(TAG, "IOException", e);
             }
         }
         if (out != null) {
             try {
                 out.close();
             } catch (IOException e) {
                 Log.w(TAG, "IOException", e);
             }
         }
     }
 
     /**
      * Closes the client socket and server socket onStop()
      */
     @Override
     protected void onStop() {
         super.onStop();
         if (clientSocket != null && !clientSocket.isClosed()) {
             try {
                 clientSocket.close();
             } catch (IOException e) {
                 Log.w(TAG, "Could not close clientSocket", e);
             }
         }
         if (serverSocket != null && !serverSocket.isClosed()) {
             try {
                 serverSocket.close();
             } catch (IOException e) {
                 Log.w(TAG, "Could not close serverSocket", e);
             }
         }
     }
 }

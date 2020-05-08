 package com.hex.network;
 
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.google.gson.Gson;
 import com.hex.core.Move;
 
 public class Client extends Thread {
     private String name;
     protected NetCommunication talk;
     protected int id = 1;
 
     public Gson gson = new Gson();
 
     public static final int VERSION = 1;
 
     private final LinkedBlockingQueue<Move> moves = new LinkedBlockingQueue<Move>();
     public NetworkCallbacks callbacks;
 
     public Client(NetCommunication talk) {
         System.out.println("Creating New TestClient: ");
         this.talk = talk;
     }
 
     public void messageDispach(String message) {
         final ServerResponse sr = gson.fromJson(message, ServerResponse.class);
         switch(sr.action) {
         case MOVE:
             moves.add(sr.move);
             break;
         case APROVE:
             break;
         case NEW_GAME:
             if(this.callbacks != null) {
                 this.callbacks.newGame(sr.data);
             }
             break;
         case OUT_OF_SYNC_ERROR:
             if(this.callbacks != null) {
                 this.callbacks.error(Errors.TURNMISMATCHEXCEPTION);
             }
             break;
         case UNDO:
             this.callbacks.undo(sr.number);
             break;
         case REQUEST_NEW_GAME:
             if(this.callbacks != null) {
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                        String gameData = callbacks.newGameRequest();
                         if(gameData != null) {
                             sendNewGame(gameData);
                             callbacks.newGame(gameData);
                         }
                     }
                 }).start();
             }
             break;
         case REQUEST_UNDO:
             if(this.callbacks != null) {
                 new Thread(new Runnable() {
                     @Override
                     public void run() {
                         if(callbacks.undoRequest(sr.number)) {
                             sendUndo(sr.number);
                             callbacks.undo(sr.number);
                         }
                     }
                 }).start();
 
             }
             break;
         case SENDCHAT:
             if(this.callbacks != null) {
                 this.callbacks.chat(sr.data);
             }
             break;
         case STARTING:
 
             if(sr.number != this.VERSION) {
                 sendError(Errors.VERSION);
                 this.callbacks.error(Errors.VERSION);
             }
             this.moves.clear();
             break;
         case ERROR:
             this.callbacks.error(Errors.values()[sr.number]);
             break;
         default:
             break;
 
         }
     }
 
     private void sendError(Errors e) {
         ServerResponse sr = new ServerResponse(name, this.id, null, Action.ERROR, null, e.ordinal());
         String json = gson.toJson(sr);
         talk.sendMessage(json);
 
     }
 
     private void sendUndo(int number) {
         ServerResponse sr = new ServerResponse(name, this.id, null, Action.UNDO, null, number);
         String json = gson.toJson(sr);
         talk.sendMessage(json);
     }
 
     private void sendNewGame(String gameData) {
         ServerResponse sr = new ServerResponse(name, this.id, null, Action.NEW_GAME, gameData);
         String json = gson.toJson(sr);
         this.talk.sendMessage(json);
 
     }
 
     public void kill() {
         talk.kill();
 
     }
 
     public boolean giveUp() {
         // TODO Auto-generated method stub
         return false;
     }
 
     public Move getPlayerTurn() {
         try {
             return this.moves.take();
 
         }
         catch(InterruptedException e) {
             e.printStackTrace();
             throw new RuntimeException("failed to get move");
         }
 
     }
 
     public void sendMove(Move move) {
         ServerResponse sr = new ServerResponse(name, this.id, move, Action.MOVE, null);
         String json = gson.toJson(sr);
         this.talk.sendMessage(json);
 
     }
 
     /**
      * @return the game
      */
 
     public int getTeam() {
         return this.id;
     }
 
     public void requestNewGame() {
         ServerResponse sr = new ServerResponse(name, this.id, null, Action.REQUEST_NEW_GAME, null);
         String json = gson.toJson(sr);
 
         this.talk.sendMessage(json);
 
     }
 
     public void simulateMove(Move move) {
         this.moves.add(move);
 
     }
 
     public void starting() {
         ServerResponse sr = new ServerResponse(name, this.id, null, Action.STARTING, null, this.VERSION);
         String json = gson.toJson(sr);
         talk.sendMessage(json);
 
     }
 
     public void requestUndo(int undoTo) {
         ServerResponse sr = new ServerResponse(name, this.id, null, Action.REQUEST_UNDO, null, undoTo);
         String json = gson.toJson(sr);
         talk.sendMessage(json);
 
     }
 
 }

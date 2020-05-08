 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ogo.spec.game.lobby;
 
 import ogo.spec.game.multiplayer.GameProto.Token;
 import ogo.spec.game.multiplayer.client.TokenChangeListener;
 import ogo.spec.game.model.Game;
 import ogo.spec.game.model.Change;
 import ogo.spec.game.graphics.view.GUI;
 
 import java.util.LinkedList;
 
 /**
  * Main game class.
  */
 public class GameRun implements TokenChangeListener
 {
 
     protected int nextId;
     protected long lastMessage = -1;
     protected int counter = 0;
     protected Game game;
 
     /**
      * Run the game.
      */
     public GameRun(Game game)
     {
         this.game = game;
 
         startGraphics();
     }
 
     /**
      * Start the graphics.
      */
     void startGraphics()
     {
         new GUI(game);
     }
 
     // other methods
 
 
     // network methods
     // These methods run in the network thread
 
     /**
      * Obtain the queue from the game state.
      *
      * @return Game state changes queue
      */
     LinkedList<Change> getGameChanges()
     {
         LinkedList<Change> changes = new LinkedList<Change>();
 
        while ((Change change = game.poll()) != null) {
             changes.add(change);
         }
 
         return changes;
     }
 
     /**
      * Obtain the queue from the token.
      *
      * @param token Token to obtain changes from
      *
      * @return Token changes queue
      */
     LinkedList<Change> getTokenChanges(Token.Builder token)
     {
         LinkedList<Change> changes = new LinkedList<Change>();
 
         return changes;
     }
 
     /**
      * Merge info into the token.
      *
      * This method will merge the two token chains. One from the current game
      * state, and one from the token sent by the previous host. The data from
      * the previous token should be preferred.
      *
      * @param token Token to be processed
      */
     public Token.Builder mergeInfo(Token.Builder token)
     {
         return token;
     }
 
     /**
      * Copy the received token, and create a token builder from it.
      *
      * @return new token
      */
     Token.Builder copyToken(Token token)
     {
         Token.Builder builder = Token.newBuilder();
 
         builder.mergeFrom(token);
 
         return builder;
     }
 
     /**
      * Keep stats.
      */
     void runStats()
     {
         counter++;
         long time = System.currentTimeMillis();
         if(lastMessage == -1 || time - lastMessage >  1000){
             long diff = time - lastMessage;
             System.out.println("TPS: " + counter + "/" + diff + " = " + 1000.0*counter/diff);
             lastMessage = time;
             counter = 0;
         }
     }
 
     /**
      * Called when the token has changed.
      *
      * Note that this will be called from the network layer. Which runs in a
      * different thread than the rest of this class.
      */
     public Token tokenChanged(Token token)
     {
         runStats();
         nextId = token.getLastId();
 
         // first copy the token
         Token.Builder builder = copyToken(token);
 
         mergeInfo(builder);
 
         builder.setLastId(nextId);
 
         return builder.build();
     }
 }

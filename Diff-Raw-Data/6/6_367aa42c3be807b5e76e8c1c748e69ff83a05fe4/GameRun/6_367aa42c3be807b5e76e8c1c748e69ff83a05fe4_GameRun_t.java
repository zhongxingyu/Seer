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
 import java.util.List;
 
 /**
  * Main game class.
  */
 public class GameRun implements TokenChangeListener
 {
 
     protected int nextId;
     protected long lastMessage = -1;
     protected int counter = 0;
    protected int playerId; // the ID of the player behind this computer
     protected Game game;
 
     /**
      * Run the game.
      */
    public GameRun(Game game, int playerId)
     {
         this.game = game;
        this.playerId = playerId;
 
         startGraphics();
     }
 
     /**
      * Start the graphics.
      */
     void startGraphics()
     {
         new GUI(game, null); // TODO: replace null reference with player object
     }
 
     // other methods
 
 
     // network methods
     // These methods run in the network thread
 
     /**
      * Create a Change from a Token.Change object.
      *
      * @param change The change from the token
      *
      * @return The new change
      */
     Change createChangeFromTokenChange(Token.Change change)
     {
         Change newChange = new Change();
 
         switch (change.getType()) {
             case MOVE_CREATURE:
                 newChange.type = Change.ChangeType.MOVE_CREATURE;
                 newChange.x = change.getX();
                 newChange.y = change.getY();
                 break;
             case HEALTH:
                 newChange.type = Change.ChangeType.HEALTH;
                 newChange.newValue = change.getNewValue();
                 break;
             case ENERGY:
                 newChange.type = Change.ChangeType.ENERGY;
                 newChange.newValue = change.getNewValue();
                 break;
             case ATTACKING_CREATURE:
                 newChange.type = Change.ChangeType.ATTACKING_CREATURE;
                 //newChange.newValue = game.getCreature(change.getOtherCreatureId());
                 break;
         }
 
         newChange.tick = change.getTick();
 
         // TODO: get the player from the game
         //newChange.player = game.getPlayer(change.getPlayerId());
         //newChange.creature = game.getCreature(change.getCreatureId());
 
         return newChange;
     }
 
     /**
      * Obtain the queue from the game state.
      *
      * @return Game state changes queue
      */
     LinkedList<Change> getGameChanges()
     {
         LinkedList<Change> changes = new LinkedList<Change>();
 
         Change change;
 
         while ((change = game.poll()) != null) {
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
 
         List<Token.Change> tokenChanges = token.getMessageList();
 
         for (Token.Change change : tokenChanges) {
             changes.add(createChangeFromTokenChange(change));
         }
 
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
      *
      * TODO: Implement merging
      */
     public Token.Builder mergeInfo(Token.Builder token)
     {
         LinkedList<Change> gameChanges = getGameChanges();
         LinkedList<Change> tokenChanges = getTokenChanges(token);
 
         // merge the two change lists
         // when we revert a change from game, also apply this to the game
         // state
         // when we add a change from token, also apply this to the game state
 
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

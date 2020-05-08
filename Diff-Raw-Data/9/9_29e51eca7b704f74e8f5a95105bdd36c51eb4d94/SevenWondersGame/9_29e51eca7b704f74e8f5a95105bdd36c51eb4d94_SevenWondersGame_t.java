 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonArray;
 import com.steamedpears.comp3004.SevenWonders;
 import com.steamedpears.comp3004.routing.Router;
 import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class SevenWondersGame extends Changeable implements Runnable{
     public static final String PROP_GAME_CARDS = "cards";
     public static final int MAX_AGES = 3;
     public static int TURN_LENGTH = 1000*60*2; // 2 minutes
     private static Logger log = Logger.getLogger(SevenWondersGame.class);
 
     private List<Player> players;
     private Set<Player> localPlayers;
     private Set<Card> discard;
     private Map<String, Card> cards;
     private List<List<Card>> deck;
     private int age;
     private int maxPlayers; //useless?
     private Router router;
     private final ExecutorService pool;
     private Map<Future, Player> runningPlayers;
     private boolean gameOver;
 
     public SevenWondersGame(Router router){
         this.players = new ArrayList<Player>();
         this.localPlayers = new HashSet<Player>();
         this.discard = new HashSet<Card>();
         this.age = 1;
         this.router = router;
         this.pool = Executors.newFixedThreadPool(SevenWonders.MAX_PLAYERS+1);
         this.gameOver = false;
     }
 
     public boolean applyCommands(Map<Player, PlayerCommand> commands){
         log.debug("Applying player commands");
         for(Player player: commands.keySet()){
             PlayerCommand command = commands.get(player);
             try {
                 player.applyCommand(command);
             } catch (Exception e) {
                 try {
                     log.debug("Player made invalid move, using null move");
                     command = PlayerCommand.getNullCommand(player);
                     commands.put(player, command);
                     player.applyCommand(command);
                 } catch (Exception e1) {
                     //OH GOD EVERYTHING IS BROKEN
                     e1.printStackTrace();
                     System.exit(-1);
                 }
             }
         }
 
         for(Player player: commands.keySet()){
             PlayerCommand command = commands.get(player);
             player.finalizeCommand(command);
         }
 
         if(shouldDeal()){
             discardExtraCards();
             runMilitaryConflict();
             if(age>MAX_AGES){
                 gameOver = true;
                 announceChange(this);
             }
         }
 
         return gameOver;
     }
 
     private void runMilitaryConflict() {
         Map<Player, Map<String, Integer>> assets = new HashMap<Player, Map<String, Integer>>();
         for(Player p: players){
             assets.put(p, p.getAssets());
         }
         Player oldPlayer = players.get(players.size()-1);
         for(Player curPlayer: players){
             int oldPlayerMilitary = Asset.getAsset(assets.get(oldPlayer), Asset.ASSET_MILITARY_POWER);
             int newPlayerMilitary = Asset.getAsset(assets.get(curPlayer), Asset.ASSET_MILITARY_POWER);
             if(oldPlayerMilitary>newPlayerMilitary){
                 oldPlayer.registerMilitaryVictory(age);
                 curPlayer.registerMilitaryDefeat(age);
             }else if(newPlayerMilitary>oldPlayerMilitary){
                 curPlayer.registerMilitaryVictory(age);
                 oldPlayer.registerMilitaryDefeat(age);
             }
         }
     }
 
     private void takeTurnsInternal(){
         changeHands();
         announceChange(this);
         runningPlayers = new HashMap<Future, Player>();
 
         for(Player player: localPlayers){
             Future future = pool.submit(player);
             runningPlayers.put(future, player);
         }
 
         Set<Future> finishedPlayers = new HashSet<Future>();
 
         long startTime = new Date().getTime();
 
         while(runningPlayers.size()>0){
             try{
                 Thread.sleep(100);
             }catch(InterruptedException ex){
                 ex.printStackTrace();
                 System.exit(-1);
             }
 
             boolean timeUp = (new Date().getTime())-startTime>TURN_LENGTH;
 
             finishedPlayers.clear();
 
             for(Future future: runningPlayers.keySet()){
                 if(future.isDone()){
                     log.debug("Player returned with command");
                     finishedPlayers.add(future);
                     Player player = runningPlayers.get(future);
                     router.registerMove(player, player.getCurrentCommand());
                 }else if(timeUp){
                     log.debug("Player timed out");
                     finishedPlayers.add(future);
                     Player player = runningPlayers.get(future);
                     future.cancel(true);
                     router.registerMove(player, PlayerCommand.getNullCommand(player));
                 }
             }
 
             for(Future future: finishedPlayers){
                 runningPlayers.remove(future);
             }
         }
     }
 
     public boolean isDone(){
         return runningPlayers==null || runningPlayers.size()==0;
     }
 
     public void takeTurns(){
         log.debug("Taking turns");
         pool.submit(this);
     }
 
     public void run(){
         takeTurnsInternal();
     }
 
     private void changeHands(){
         log.debug("Changing hands");
         if(shouldDeal()){
             deal();
             age++;
 
         }else{
             rotateHands();
         }
     }
 
     private void discardExtraCards(){
         for(Player player: players){
             List<Card> hand = player.getHand();
             while(hand.size()>0){
                 Card card = hand.get(0);
                 hand.remove(card);
                 discard(card);
             }
         }
     }
 
     private boolean shouldDeal(){
         return players.get(0).getHand().size()<2;
     }
 
     private void rotateHands(){
         log.debug("Rotating hands");
         if(getAge()==2){  //rotate cards to the right
             Player curPlayer = players.get(players.size()-1);
             List<Card> oldHand = curPlayer.getHand();
             List<Card> newHand;
             for(int i=0; i<players.size(); ++i){
                 curPlayer = players.get(i);
                 newHand = curPlayer.getHand();
                 curPlayer.setHand(oldHand);
                 oldHand = newHand;
             }
         }else{ //rotate cards to the left
             Player curPlayer = players.get(0);
             List<Card> oldHand = curPlayer.getHand();
             List<Card> newHand;
             for(int i=players.size()-1; i>=0; --i){
                 curPlayer = players.get(i);
                 newHand = curPlayer.getHand();
                 curPlayer.setHand(oldHand);
                 oldHand = newHand;
             }
         }
     }
 
     private void deal(){
         log.debug("Dealing hands");
         List<Card> currentDeck = deck.get(age-1);
         List<List<Card>> hands = new ArrayList<List<Card>>();
         for (Player player : players) {
             hands.add(new ArrayList<Card>());
         }
         int numPlayers = players.size();
         for(int i=0; i<currentDeck.size(); ++i){
             hands.get(i%numPlayers).add(currentDeck.get(i));
         }
         for(int i=0; i<players.size(); ++i){
             players.get(i).setHand(hands.get(i));
         }
 
     }
 
     public void discard(Card card){
         discard.add(card);
     }
 
     public void undiscard(Card card){
         discard.remove(card);
     }
 
     public void setCards(JsonArray arr){
         List<Card> cards = Card.parseDeck(arr);
         this.cards = new HashMap<String, Card>();
         for(Card card: cards){
             this.cards.put(card.getId(),card);
         }
     }
 
     public List<List<Card>> generateRandomDeck(int numPlayers){
         List<List<Card>> result = new ArrayList<List<Card>>();
         for(int curAge=0; curAge<MAX_AGES; ++curAge){
             List<Card> ageDeck = new ArrayList<Card>();
             result.add(ageDeck);
         }
         List<Card> guilds = new ArrayList<Card>();
 
         for(Card card: cards.values()){
             if(card.getMinPlayers()==0){
                 guilds.add(card);
             }else if(card.getMinPlayers()<=numPlayers){
                 result.get(card.getAge()-1).add(card);
             }
         }
 
         Collections.shuffle(guilds);
        for(int i=0; i<numPlayers+2; ++i){
             result.get(2).add(guilds.get(i));
         }
 
         for(List<Card> ageDeck: result){
             Collections.shuffle(ageDeck);
         }
 
         return result;
     }
 
     public void setDeck(List<List<Card>> deck){
         this.deck = deck;
     }
 
     public void addPlayer(Player player){
         players.add(player);
     }
 
     public void addLocalPlayer(Player player){
         localPlayers.add(player);
         addPlayer(player);
     }
 
     public List<Player> getPlayers(){
         return players;
     }
 
     public Set<Card> getDiscard(){
         return discard;
     }
 
     public int getAge(){
         return age;
     }
 
     public List<List<Card>> getDeck(){
         return deck;
     }
 
     public Player getPlayerById(int id){
         for(Player player: players){
             if(player.getPlayerId()==id){
                 return player;
             }
         }
         return null;
     }
 
     public Card getCardById(String id){
         return cards.get(id);
     }
 
     public boolean isGameOver(){
         return gameOver;
     }
 
     public Map<Player, Integer> tabulateResults() {
         Map<Player, Integer> results = new HashMap<Player, Integer>();
 
         for(Player player: players){
             results.put(player, player.getFinalVictoryPoints());
         }
 
         return results;
     }
 
 }

 package com.steamedpears.comp3004.models;
 
 import com.google.gson.JsonObject;
 import com.steamedpears.comp3004.routing.Router;
 import sun.security.krb5.internal.crypto.DesMacCksumType;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class SevenWondersGame extends Thread{
     public static final String PROP_GAME_CARDS = "cards";
     public static final int MAX_AGES = 3;
 
     private List<Player> players;
     private Set<Player> localPlayers;
     private Set<Card> discard;
     private Map<String, Card> cards;
     private List<List<Card>> deck;
     private int age;
     private int maxPlayers; //useless?
     private Router router;
 
     public SevenWondersGame(Router router){
         this.players = new ArrayList<Player>();
         this.localPlayers = new HashSet<Player>();
         this.discard = new HashSet<Card>();
         this.age = 1;
         this.router = router;
     }
 
     public void handleMoves(Map<Player, PlayerCommand> commands){
         //TODO: locally apply these moves sent from the Router
         //note: all player moves must be verified before any player move is actually applied
         //at the end just call takeTurn()? Maybe?
     }
 
     private void takeTurn(){
         changeHands();
         Set<Player> runningPlayers = new HashSet<Player>();
 
         for(Player player: localPlayers){
             player.start();
             runningPlayers.add(player);
         }
 
         while(runningPlayers.size()>0){
             //TODO: timeout slow players
             for(Player player: runningPlayers){
                 if(!player.isAlive()){
                     router.registerMove(player, player.getCurrentCommand());
                 }
             }
             try{
                 Thread.sleep(100);
             }catch(InterruptedException ex){
                 ex.printStackTrace();
                 System.exit(-1);
             }
         }
     }
 
     public void run(){
         takeTurn();
     }
 
     private void changeHands(){
         if(players.get(0).getHand().size()==0){
             deal();
         }else{
             rotateHands();
         }
     }
 
     private void rotateHands(){
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
         List<Card> currentDeck = deck.get(age-1);
         List<List<Card>> hands = new ArrayList<List<Card>>();
         for(int i=0; i<players.size(); ++i){
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
 
     public void setCards(JsonObject obj){
         List<Card> cards = Card.parseDeck(obj.getAsJsonArray(PROP_GAME_CARDS));
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
                 result.get(card.getAge()).add(card);
             }
         }
 
         Collections.shuffle(guilds);
         for(int i=0; i<numPlayers; ++i){
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
 }

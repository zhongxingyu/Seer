 package poker.arturka;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mordavolt
  * Date: 7/16/13
  * Time: 1:48 PM
  */
 public class Players {
     private Map<Integer,Player> playerList;
     public Players(){
         playerList=new HashMap<Integer, Player>();
     }
 
     public void addPlayer(int id){
         Player tempPlayer = new Player(id);
         tempPlayer.toggleInGame();
         playerList.put(id,tempPlayer);
     }
 
     public void removePlayer(int id){
         playerList.get(id).toggleInGame();
         playerList.remove(id);
     }
 
     /**
      * Returns random player from list.
      * @return Random player.
      */
     public Player getRandomPlayer(){
         Random random=new Random();
         return playerList.get(random.nextInt(playerList.size()));
     }
 
     /**
      * Returns a player that is next in table.
      * @param player Player next to whom is the return player.
      * @return Next player to player param.
      */
     public Player getNextPlayer(Player player){
         for(int i=0;i<playerList.size();i++){
             if(playerList.get(i).equals(player)){
                 int j=i+1;
                 if (j>=playerList.size()){
                     j-=playerList.size();
                 }
                 while(!playerList.get(j).isInGame()){
                     if (++j>=playerList.size()){
                         j-=playerList.size();
                     }
                 }
                 return playerList.get(j);
             }
         }
         return null;
     }
 
     /**
      * Returns te current dealer.
      * @return Current dealer.
      */
     public Player getDealer(){
        for(Player player:getPlayersList()){
            if(player.isDealer()){
                return player;
             }
         }
         return null;
     }
 
     /**
      * Returns next dealer from the list.
      * If there was no previous dealer, returns random player.
      * @return Next player that has to be a dealer.
      */
     public Player nextDealer(){
         Player oldDealer;
         oldDealer=getDealer();
         if (oldDealer!=null){
             oldDealer.toggleDealer();
             getNextPlayer(oldDealer).toggleDealer();
         }else{
             getRandomPlayer().toggleDealer();
         }
         return oldDealer;
     }
 
     /**
      * Gets sum of bets from all players.
      * @return Sum of all bets
      */
     public int getPot(){
         int pot=0;
         for(Player player:getPlayersList()){
             pot+=player.getBet();
         }
         return pot;
     }
 
     /**
      * Returns a list of players with cards on hands.
      * Returns all players that still haven't folded and
      * are still in game.
      * @return List of players with cards on hand.
      */
     public List<Player> playersLeft(){
         List<Player> tempList=new ArrayList<Player>();
         for(Player player:getPlayersList()){
             if(!player.hasFolded()&&player.isInGame()){
                 tempList.add(player);
             }
         }
         return tempList;
     }
 
     /**
      * Returns all players that are all-in.
      * @return List of all-in players.
      */
     public List<Player> allInPlayers(){
         List<Player> tempList=new ArrayList<Player>();
         for(Player player:getPlayersList()){
             if(!player.hasFolded()&&player.getCash()==0&&player.isInGame()){
                 tempList.add(player);
             }
         }
         return tempList;
     }
 
     /**
      * Returns all players in a List form.
      * @return List of all players.
      */
     public List<Player> getPlayersList(){
         List<Player> tempList=new ArrayList<Player>();
         tempList.addAll(tempList);
         return tempList;
     }
 
     /**
      * Returns List of players valid to send to client.
      * Returns List of players where all players except for safePlayer
      * have their hand replaced by {null,null}.
      * @param safePlayer The user whose cards are not wiped.
      * @return List of players, safe to ship to end user.
      */
     public List<Player> getSafeList(Player safePlayer){
         List<Player> tempList=new ArrayList<Player>();
         Player tempPlayer;
         for(Player player:getPlayersList()){
             tempPlayer=new Player(player);
             if(!tempPlayer.equals(safePlayer)){
                 tempPlayer.giveCards(null,null);
             }
             tempList.add(tempPlayer);
         }
         return tempList;
     }
 
     /**
      * Outputs a List of players by the strength of their hand.
      * @return List of players, null if none.
      */
 	public List<Player> getBestPlayers() {
 		HandEvaluator evaluator = new HandEvaluator(playersLeft());
 		List<Player> playerHandRanking =  evaluator.getPlayerHandEvaluation();
 		if (!playerHandRanking.isEmpty())
 			return playerHandRanking;
 		return null;
 	}
 
     /**
      * Subtracts money from player bets, that ar smaller than given.
      * Checks all the players, if their bet is smaller than bet parameter takes it all,
      * if it is bigger than bet subtracts bet parameter from it.
      * @param bet Sets the limit.
      * @return All the money collected from players.
      */
     public int fetchBets(int bet) {
         int cash=0;
         for(Player player:getPlayersList()){
             if (player.getBet()<bet){
                 cash+=player.getBet();
                 player.setBet(0);
             }else{
                 cash+=bet;
                 player.reduceBet(bet);
             }
         }
         return cash;
     }
 }

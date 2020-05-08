 package poker.arturka;
 
 import commands.FRCallCommand;
 import commands.FRCheckCommand;
 import commands.FlopCommand;
 import commands.TurnRiverCommand;
 import message.data.ClientResponse;
 import poker.server.Room;
 import message.data.ClientTurn;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Game implements Runnable {
 
     private Deck deck;
     private Players players;
     private int blind;
     private List<Card> table;
     private int maxBet;
     private int state;
     private Room room;
 
     public Game(Room room){
         deck=new Deck();
         players=new Players();
         blind=30;
         maxBet=0;
         table=new ArrayList<Card>();
         state=0;
         this.room=room;
         for(int id: room.getUsers()){
             players.addPlayer(id);
         }
     }
 
     private void splitPot(){
 
     }
 
     private void endGame(){
         if (players.playersLeft().size()>1){
 
         }else{
 
         }
     }
 
     private boolean raiseBet(int playerPot){
         if (playerPot>maxBet){
             maxBet=playerPot;
             return true;
         }else{
             return false;
         }
     }
 
     public void run() {
 //        for(int id: Room.getUsers()){
 //            players.addPlayer(id);
 //        }
         players.nextDealer();
         Player nextPlayer=players.getNextPlayer(players.getDealer());
         if (!nextPlayer.bet(blind/2)){
             nextPlayer.bet(nextPlayer.getCash());
         }
         raiseBet(nextPlayer.getBet());
         nextPlayer=players.getNextPlayer(nextPlayer);
         if (!nextPlayer.bet(blind)){
             nextPlayer.bet(nextPlayer.getCash());
         }
         raiseBet(nextPlayer.getBet());
         deck.shuffleDeck();
         Player currentPlayer=players.getNextPlayer(players.getDealer());
         do{
             currentPlayer.giveCards(deck.getTopCard(),deck.getTopCard());
         }while(players.getDealer()!=currentPlayer);
         Player firstBetter=players.getNextPlayer(nextPlayer);
         //todo abstractions, rewrite to actual methods
         Player better=firstBetter;
         ClientResponse move;
         while(state<4){
             do{
                 if(!better.hasFolded()&&better.getCash()>0){
                     if(better.getBet()==maxBet){
                         move = room.sendToUser(better.getId(),new FRCheckCommand());
                     }else{
                         move = room.sendToUser(better.getId(),new FRCallCommand());
                     }
                     switch(move.turn){
                         case FOLD:
                             better.toggleFold();
                             continue;
                         case CHECK:
                             continue;
                         case CALL:
                             better.bet(maxBet-better.getBet());
                             continue;
                         case RAISE:
                             better.bet(move.getBet());
                             raiseBet(better.getBet());
                             firstBetter=better;
                             continue;
                         case EXIT:
                             better.toggleFold();
                             players.removePlayer(better.getId());
                     }
                     if(players.playersLeft().size()<2){
                         endGame();
                     }
                 }
                 better=players.getNextPlayer(better);
             }while (better==firstBetter);
             switch (state){
                 case 0:
                     table.add(deck.getTopCard());
                     table.add(deck.getTopCard());
                     table.add(deck.getTopCard());
                     room.Broadcast(new FlopCommand(table.get(0),table.get(1),table.get(2)));
                     state++;
                     break;
                 case 1:
                     deck.getTopCard();
                     table.add(deck.getTopCard());
                    room.Broadcast(new TurnRiverCommand(table.get(3),TurnRiverCommand.RorT.Turn));
                     state++;
                     break;
                 case 2:
                     deck.getTopCard();
                     table.add(deck.getTopCard());
                    room.Broadcast(new TurnRiverCommand(table.get(4),TurnRiverCommand.RorT.River));
                     state++;
                     break;
                 default:
                     state++;
             }
         }
         endGame();
     }
 }

 package de.htwg.wzzrd.control;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
import java.util.List;
 import java.util.Map;
 
 import de.htwg.wzzrd.model.Card;
 import de.htwg.wzzrd.model.CardStack;
 import de.htwg.wzzrd.model.IPacket;
 import de.htwg.wzzrd.model.Player;
 import de.htwg.wzzrd.model.ServerPlayer;
 import de.htwg.wzzrd.model.TableInterface;
 import de.htwg.wzzrd.model.network.packet.ConnectionLostPacket;
 import de.htwg.wzzrd.model.network.packet.Error;
 import de.htwg.wzzrd.model.network.packet.JoinGamePacket;
 import de.htwg.wzzrd.model.network.packet.PlayCardPacket;
 import de.htwg.wzzrd.model.network.packet.PlayerJoined;
 import de.htwg.wzzrd.model.network.packet.UpdateCardsOnHandPacket;
 
 /**
  * This Class manages the Players and offers methods to send packets to one or
  * all clients. Also has some methods to make our life easier, like dealing n
  * cards to each player.
  * 
  * @author Michael
  */
 public class PlayerManager {
     private Map<String, ServerPlayer> playerlist = new HashMap<String, ServerPlayer>();
    private List<String> joinorder = new LinkedList<String>();
 
     public void addPlayer(JoinGamePacket packet) throws IOException {
         playerlist.put(packet.getName(), packet.getServerPlayer());
        joinorder.add(packet.getName());
         sendToAll(new PlayerJoined(packet.getName()));
     }
 
     public void sendToAll(IPacket packet) throws IOException {
         for (ServerPlayer player : playerlist.values()) {
             player.send(packet);
         }
     }
 
     public void sendToOne(String playername, IPacket packet) throws IOException {
         if (!joinorder.contains(playername)) {
             throw new IllegalArgumentException("Player does not exist!");
         }
         playerlist.get(playername).send(packet);
     }
 
     public void removePlayer(ConnectionLostPacket packet) {
         playerlist.remove(packet.getName());
         joinorder.remove(packet.getName());
     }
 
     public ServerPlayer getPlayerAtIndex(int index) {
         return playerlist.get(joinorder.get(index));
     }
 
     public String getPlayerNameAtIndex(int index) {
         return joinorder.get(index);
     }
 
     public int getPlayercount() {
         return playerlist.size();
     }
 
     public void sendErrorPacket(String player, String text) throws IOException {
         sendToOne(player, new Error(text));
     }
 
     /**
      * 
      * @return
      */
     public Player[] getPlayerArray() {
         Player[] temp = new Player[joinorder.size()];
         for (int i = 0; i < temp.length; i++) {
             temp[i] = new Player(joinorder.get(i));
         }
         return temp;
     }
 
     /**
      * Deals count cards from cardstack to each of the players. Automatically
      * sends the dealed cards to the Client. Throws an IllegalArgumentException
      * when not enough cards are available.
      * 
      * @param cardstack
      *            the cardstack we're taking the cards from
      * @param count
      *            how many cards each player gets
      * @throws IOException
      *             if there's an error in the underlying GameNetwork
      */
     public void dealCards(CardStack cardstack, int count) throws IOException {
         if (cardstack.cardCount() < count * playerlist.size()) {
             throw new IllegalArgumentException("Cannot deal more cards than the cardstack has.");
         }
         for (ServerPlayer player : playerlist.values()) {
             ArrayList<Card> cards = new ArrayList<Card>();
             for (int i = 0; i < count; i++) {
                 Card card = cardstack.popCard();
                 cards.add(card);
                 player.addCard(card);
             }
             player.send(new UpdateCardsOnHandPacket(cards));
         }
     }
 
     public void playCard(PlayCardPacket p, TableInterface table) throws IOException {
         if (playerlist.get(p.getSender()).hasCard(p.getCard())) {
             table.addlaid(playerlist.get(p.getSender()), p.getCard());
             playerlist.get(p.getSender()).removeCard(p.getCard());
         } else {
             sendErrorPacket(p.getSender(), "You do not have this card! This incident will be reported.");
         }
     }
 }

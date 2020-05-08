 package de.schelklingen2008.canasta.shared;
 
 import junit.framework.TestCase;
 import de.schelklingen2008.canasta.client.Constants;
 import de.schelklingen2008.canasta.model.Card;
 import de.schelklingen2008.canasta.model.GameModel;
 import de.schelklingen2008.canasta.model.Player;
 import de.schelklingen2008.canasta.model.Rank;
 import de.schelklingen2008.canasta.model.Suit;
 
 public class GameModelTest extends TestCase
 {
 
     public void testInitialState()
     {
         GameModel logic = new GameModel(new String[] { "Player 1", "Player 2" });
 
         // how many cards are in the discard pile?
         assertEquals("discard pile has wrong size", 1, logic.getDiscard().size());
 
         // how many players are there?
         assertEquals("playernumber is not two", 2, logic.getPlayers().length);
         for (Player player : logic.getPlayers())
         {
             // has every player the right card number in hand?
             assertEquals("player " + player.getName() + " has wrong card number in hands",
                          Constants.GAME_INITIAL_CARD_COUNT, player.getHand().size());
             // is the outlay of every player empty?
             assertEquals("player " + player.getName() + " has outlay", 0, player.getOutlay().size());
         }
     }
 
     public void testCardCounts()
     {
         GameModel logic = new GameModel(new String[] { "Player 1", "Player 2" });
 
         Player[] players = logic.getPlayers();
 
         int talonSize = logic.getTalon().size();
         int discardSize = logic.getDiscard().size();
         int handSizes[] = new int[logic.getPlayers().length];
 
         int i = 0;
         for (Player player : players)
         {
             handSizes[i] = player.getHand().size();
             i++;
         }
 
         logic.drawCard(players[0]);
         assertEquals(talonSize - 1, logic.getTalon().size());
         assertEquals(discardSize, logic.getDiscard().size());
         assertEquals(handSizes[0] + 1, players[0].getHand().size());
         assertEquals(handSizes[1], players[1].getHand().size());
 
         logic.drawCard(players[1]);
         assertEquals(talonSize - 1, logic.getTalon().size());
         assertEquals(discardSize, logic.getDiscard().size());
         assertEquals(handSizes[0] + 1, players[0].getHand().size());
         assertEquals(handSizes[1], players[1].getHand().size());
 
         logic.discardCard(players[0], 0);
         assertEquals(talonSize - 1, logic.getTalon().size());
         assertEquals(discardSize + 1, logic.getDiscard().size());
         assertEquals(handSizes[0], players[0].getHand().size());
         assertEquals(handSizes[1], players[1].getHand().size());
 
         logic.drawCard(players[1]);
         assertEquals(talonSize - 2, logic.getTalon().size());
         assertEquals(discardSize + 1, logic.getDiscard().size());
         assertEquals(handSizes[0], players[0].getHand().size());
         assertEquals(handSizes[1] + 1, players[1].getHand().size());
     }
 
     public void testMelding()
     {
         GameModel logic = new GameModel(new String[] { "Player 1", "Player 2" });
 
         Player[] players = logic.getPlayers();
 
         int handSizes[] = new int[logic.getPlayers().length];
         int i = 0;
         for (Player player : players)
         {
             handSizes[i] = player.getHand().size();
             i++;
 
             assertEquals(0, player.getOutlay().size());
         }
 
         logic.meldCards(players[0], new int[] { 0, 1, 2, 3, 4, 5, 6 });
 
         assertEquals(1, players[0].getOutlay().size());
         assertEquals(7, players[0].getOutlay().get(0).size());
 
     }
 
     public void testGetRank()
     {
 
         Card testCards1[] = new Card[] { new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.ACE, Suit.DIAMONDS),
                 new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.ACE, Suit.DIAMONDS),
                 new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.ACE, Suit.DIAMONDS),
                 new Card(Rank.ACE, Suit.DIAMONDS), new Card(Rank.ACE, Suit.DIAMONDS) };
 
         Card testCards2[] = new Card[] { new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.DIAMONDS),
                 new Card(Rank.JOKER, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.DIAMONDS),
                 new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.TWO, Suit.DIAMONDS),
                 new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.DIAMONDS) };
 
         Card testCards3[] = new Card[] { new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.ACE, Suit.DIAMONDS),
                 new Card(Rank.JOKER, Suit.DIAMONDS), new Card(Rank.QUEEN, Suit.DIAMONDS),
                 new Card(Rank.THREE, Suit.DIAMONDS), new Card(Rank.TWO, Suit.DIAMONDS),
                 new Card(Rank.QUEEN, Suit.DIAMONDS), new Card(Rank.TEN, Suit.DIAMONDS) };
 
         assertEquals(Rank.ACE, GameModel.getRank(testCards1));
        assertEquals(Rank.ACE, GameModel.getRank(testCards2));
        assertEquals(Rank.ACE, GameModel.getRank(testCards3));
     }
 }

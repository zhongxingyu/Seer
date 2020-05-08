 package de.schelklingen2008.jipbo.shared;
 
 import junit.framework.TestCase;
 import de.schelklingen2008.jipbo.client.Constants;
 import de.schelklingen2008.jipbo.model.Card;
 import de.schelklingen2008.jipbo.model.GameModel;
 
 public class GameModelTest extends TestCase
 {
 
    private String[]  mPlayerNames = new String[] { "Elitsa", "Fabio" };
    private GameModel gameModel    = new GameModel(mPlayerNames);
 
     public void testCreateGame() throws Exception
     {

         assertEquals("Should be Elitsa", "Elitsa", gameModel.getPlayerNameIndexOf(0));
         assertEquals("Should be Fabio", "Fabio", gameModel.getPlayerNameIndexOf(1));
 
         assertEquals("There should be 2 players", 2, gameModel.getPlayerSize());
 
         assertEquals("This should be 0", 0, gameModel.getPlayerIDByName(new String("Elitsa")));
         assertEquals("This should be 1", 1, gameModel.getPlayerIDByName(new String("Fabio")));
 
         try
         {
             gameModel.getPlayerIDByName(new String("Harry"));
             fail("Harry is not a player. An exception should be thrown.");
         }
         catch (IllegalStateException e)
         {
             // Das ist die erwartete Exception.
         }
 
         assertEquals("There should be " + (162 - gameModel.getPlayerSize() * Constants.DURATION - 5) + " cards left.",
                      162 - gameModel.getPlayerSize() * Constants.DURATION - 5, gameModel.getStockCards().size());
 
         assertEquals("There should be 5 piles.", 5, gameModel.getBuildPile().length);
 
         assertEquals("There should be 4 piles.", 4, gameModel.getPlayerIndexOf(0).getDiscardPile().length);
 
         Card[] DrawPileCards = gameModel.getPlayerIndexOf(0).getDrawPile();
 
         for (int i = 0; i < DrawPileCards.length; i++)
         {
             if (DrawPileCards[i].getNumber() == -2)
             {
                 fail("First player must have cards");
             }
         }
 
         int stockPileSize = gameModel.getPlayerIndexOf(0).getStockPile().size();
         for (int i = 0; i < 5; i++)
         {
             gameModel.getPlayerIndexOf(0).removeLastStockPile();
         }
         assertEquals("There must be five cards out of the StockPile", stockPileSize - 5, gameModel.getPlayerIndexOf(0)
                                                                                                   .getStockPile()
                                                                                                   .size());
 
         assertEquals("There should be a vacant place in the draw pile.", -2, gameModel.getPlayerIndexOf(0)
                                                                                       .getDrawPile()[0].getNumber());
 
         int firstCard = gameModel.getPlayerByName("Elitsa").getDrawPile()[0].getNumber();
         gameModel.placeCardInDiscardPile("Elitsa", 0, firstCard, 2);
         assertEquals("Should be empty (-2)", -2, gameModel.getPlayerByName("Elitsa").getDrawPile()[0].getNumber());
         assertEquals("Should be the old card from hand", firstCard, gameModel.getPlayerByName("Elitsa")
                                                                              .getDiscardPile()[2].getNumber());
 
         assertEquals("There should be a vacant place in the discard pile.", -2,
                      gameModel.getPlayerIndexOf(0).getDiscardPile()[0].getNumber());
 
         assertEquals("The card from the discard pile should have been replaced.", -2,
                      gameModel.getPlayerIndexOf(0).getDiscardPile()[1].getNumber());
 
         // assertEquals("There should be no cards in the discard pile.", -2, gameModel.getPlayerIndexOf(0)
         // .getDiscardPile());
 
         for (int i = 0; i < gameModel.getPlayerIndexOf(0).getStockPile().size(); i++)
         {
             gameModel.getPlayerIndexOf(0).removeLastStockPile();
         }
         assertEquals("There should be no cards left in the Stockpile and there should be a winner.", 0,
                      gameModel.getPlayerIndexOf(0).getStockPile().size());
 
         // for (int i = 0; i < gameModel.getPlayerIndexOf(0).getDrawPile().length; i++)
         // {
         // gameModel.getPlayerIndexOf(0).removeDrawPileCard(0);
         // }
         // assertEquals("There should be a vacant place in the drawpile.", -2,
         // gameModel.getPlayerIndexOf(0).getDrawPile().length);
 
         for (int i = 0; i < DrawPileCards.length; i++)
         {
             gameModel.getPlayerIndexOf(0).setDrawPile(DrawPileCards);
         }
         assertEquals("The next player should have received 5 cards for his drawpile. ", 5,
                      gameModel.getPlayerIndexOf(0).getDrawPile());
 
     }
 }

 package com.randombit.uskoci.rest.gamecontrol;
 
 import com.randombit.uskoci.card.dao.MongoDBCard;
 import com.randombit.uskoci.card.model.Card;
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.List;
 
 public class GameControllerRestAdapterIntegrationTest {
     GameControllerRestAdapter restAdapter;
 
     @Before
     public void setUp() throws Exception {
         restAdapter = new GameControllerRestAdapterImpl();
     }
 
     @Test
     public void gameFor4PlayersIntegrationTest() throws Exception {
         GameStatusResponse gameStatus = startGameFor4();
         gameStatus = playerDrawsACard(gameStatus);
         gameStatus = playerPlaysACard(gameStatus);
         gameStatus = playerSetsNextTurn(gameStatus);
     }
 
     private GameStatusResponse playerDrawsACard(GameStatusResponse gameStatus) {
         String playerId = gameStatus.currentPlayerId;
         GameStatusMessage gameMessage = new GameStatusMessage(playerId, "drawcard", "", "0");
         return getResponse(gameMessage);
     }
 
     private GameStatusResponse playerPlaysACard(GameStatusResponse gameStatus) {
         String playerId = gameStatus.currentPlayerId;
 
         String cardId = pickAResourceCard(gameStatus, playerId);
 
         if (!"".equals(cardId)) {
             GameStatusMessage gameMessage = new GameStatusMessage(playerId, "playcard", cardId, "0");
 
             GameStatusResponse newStatus = getResponse(gameMessage);
             List<String> playersCards = newStatus.playersCards.get(playerId);
             Assert.assertFalse("Players hand does not contain the card that has been played", playersCards.contains(cardId));
             return newStatus;
         }   else {
             return gameStatus;
         }
     }
 
     private String pickAResourceCard(GameStatusResponse gameStatus, String playerId) {
         String resourceCardId = "";
         List<String> playersCardIds = gameStatus.playersCards.get(playerId);
         for (String cardId:playersCardIds) {
             Card cardInHand = MongoDBCard.instance.getModel().get(cardId);
             if ("resource".equals(cardInHand.getType())) {
                 resourceCardId = cardInHand.getId();
             }
         }
         return resourceCardId;
     }
 
     private GameStatusResponse playerSetsNextTurn(GameStatusResponse gameStatus) {
         String playerId = gameStatus.currentPlayerId;
         GameStatusMessage gameMessage = new GameStatusMessage(playerId, "setnextturn", "", "0");
 
         GameStatusResponse newStatus = getResponse(gameMessage);
        Assert.assertFalse("Player is no longer on the move", playerId.equals(newStatus.currentPlayerId))

“
 
         return newStatus;
     }
 
     private GameStatusResponse getResponse(GameStatusMessage gameMessage) {
         GameStatusResponse newGameStatus = restAdapter.getResponse(gameMessage, "0");
         responseIsValid(gameMessage, newGameStatus);
         return newGameStatus;
     }
 
     private GameStatusResponse startGameFor4() {
         GameStatusMessage gameMessage = new GameStatusMessage("1", "startgame", "", "0");
         GameStatusResponse gameStatusResponse = restAdapter.getResponse(gameMessage, "0");
 
         responseIsValid(gameMessage, gameStatusResponse);
         Assert.assertTrue("Game is started", gameStatusResponse.gameStarted);
 
         return gameStatusResponse;
     }
 
     private void responseIsValid(GameStatusMessage gameMessage, GameStatusResponse gameStatusResponse) {
         Assert.assertNotNull("response is sent", gameStatusResponse);
         lastMessageIsResentInResponse(gameMessage, gameStatusResponse);
         responseIsNotEmpty(gameStatusResponse);
     }
 
     private void responseIsNotEmpty(GameStatusResponse gameStatusResponse) {
         Assert.assertFalse("Current player id is sent", "".equals(gameStatusResponse.currentPlayerId));
     }
 
     private void lastMessageIsResentInResponse(GameStatusMessage testGameStatusMessage, GameStatusResponse gameStatusResponse) {
         Assert.assertEquals("Last action is resent in response", testGameStatusMessage, gameStatusResponse.lastAction);
     }
 }

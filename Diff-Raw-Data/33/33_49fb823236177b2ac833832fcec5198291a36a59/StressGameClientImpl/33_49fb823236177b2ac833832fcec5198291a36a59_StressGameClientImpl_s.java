 package org.vaadin.artur.stresscardgame.ui;
 
 import java.io.Serializable;
 import java.util.logging.Logger;
 
 import org.vaadin.artur.stresscardgame.engine.IllegalPlayException;
 import org.vaadin.artur.stresscardgame.engine.PlayerInfo;
 import org.vaadin.artur.stresscardgame.engine.StressGameEngine;
 import org.vaadin.artur.stresscardgame.engine.data.CardInfo;
 import org.vaadin.artur.stresscardgame.engine.data.DeckCounts;
 import org.vaadin.artur.stresscardgame.engine.data.StressGameClient;
 import org.vaadin.artur.stresscardgame.engine.event.GameStartedEvent;
 
 import com.vaadin.shared.Position;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Notification.Type;
 
 public class StressGameClientImpl implements StressGameClient,
         CardPlayListener, Serializable {
 
     private StressGameLayout gameLayout;
     private StressGameEngine gameEngine;
     private int playerNumber;
 
     public StressGameClientImpl(StressGameLayout gameLayout,
             StressGameEngine gameEngine) {
         this.gameLayout = gameLayout;
         gameLayout.addCardPlayListener(this);
         this.gameEngine = gameEngine;
     }
 
     @Override
    public void gameStarted(GameStartedEvent event) {
        playerNumber = event.getPlayerNumber();
        getLogger().fine(this + " (" + playerNumber + "):  Game started");

        // Game started, refresh layout
        refreshLayout();
        PlayerInfo playerInfo = event.getPlayerInfo().get(1 - playerNumber);
        String location = "???";
        if (playerInfo.getLocation() != null) {
            location = playerInfo.getLocation().getRegionName() + ", "
                    + playerInfo.getLocation().getCountryName();
        }
        gameLayout.setOtherPlayerText("Opponent: " + playerInfo.getName());
     }
 
     private void refreshLayout() {
         gameLayout.refreshOwnVisibleCards(gameEngine
                 .getVisibleCards(playerNumber));
         gameLayout.refreshOpponentVisibleCards(gameEngine
                 .getVisibleCards(1 - playerNumber));
         gameLayout.refreshPlayPiles(gameEngine.getPlayPileTopCards());
 
         DeckCounts deckCounts = gameEngine.getDeckCounts();
         gameLayout.setDeckCounts(deckCounts.getPlayerDecks().get(playerNumber),
                 deckCounts.getPlayerDecks().get(1 - playerNumber), deckCounts
                         .getPlayPiles().get(0), deckCounts.getPlayPiles()
                         .get(1));
 
     }
 
     @Override
     public void movePerformed() {
         getLogger().fine(this + " (" + playerNumber + "):  Move performed");
         gameLayout.getUI().runSafely(new Runnable() {
             @Override
             public void run() {
                 refreshLayout();
             }
         });
     }
 
     @Override
     public void playCard(CardInfo card, int pile) {
         getLogger().fine(
                 this + " (" + playerNumber + "):  playCard(" + card + ","
                         + pile + "). Top of pile: "
                         + gameEngine.getPlayPileTopCards().get(pile));
 
         try {
             gameEngine.playCard(playerNumber, card, pile);
         } catch (IllegalPlayException e) {
             getLogger().info(
                     "Failed to play " + card + " on "
                             + gameEngine.getPlayPileTopCards().get(pile));
         }
     }
 
     private static Logger getLogger() {
         return Logger.getLogger(StressGameClientImpl.class.getName());
     }
 
     @Override
     public void redeal() {
         getLogger().info("New cards because of no moves remaining");
 
         gameLayout.getUI().runSafely(new Runnable() {
             @Override
             public void run() {
                 Notification n = new Notification("No more moves, new cards!",
                         Type.TRAY_NOTIFICATION);
                 n.setDelayMsec(500);
                 n.setPosition(Position.MIDDLE_CENTER);
                 n.show(gameLayout.getUI().getPage());
 
                 refreshLayout();
             }
         });
     }
 
     @Override
     public void pileChoice() {
         getLogger().fine(this + " (" + playerNumber + "):  Pile choice!");
         gameLayout.getUI().runSafely(new Runnable() {
             @Override
             public void run() {
                 gameLayout.setChoosePileMode(new PileChoiceListener() {
                     @Override
                     public void pileChosen(int pile) {
                         gameEngine.pileChosen(playerNumber, pile);
                     }
                 });
             }
         });
 
     }
 
     @Override
     public void pileChoiceResolved() {
         getLogger().fine(
                 this + " (" + playerNumber + "):  Pile choice resolved");
         gameLayout.getUI().runSafely(new Runnable() {
             @Override
             public void run() {
                 gameLayout.setChoosePileMode(null);
                 refreshLayout();
             }
         });
     }
 
     @Override
     public void announceWinner(final int playerNumber) {
         getLogger().fine(
                 this + " (" + playerNumber + "):  announceWinner("
                         + playerNumber);
         gameLayout.getUI().runSafely(new Runnable() {
             @Override
             public void run() {
                 Notification n = new Notification("", Type.TRAY_NOTIFICATION);
                 n.setPosition(Position.MIDDLE_CENTER);
                 if (playerNumber == StressGameClientImpl.this.playerNumber) {
                     n.setCaption("YOU HAVE WON THE GAME");
                 } else {
                     n.setCaption("You have lost the game");
                 }
 
                 n.show(gameLayout.getUI().getPage());
             }
         });
 
     }
 
 }

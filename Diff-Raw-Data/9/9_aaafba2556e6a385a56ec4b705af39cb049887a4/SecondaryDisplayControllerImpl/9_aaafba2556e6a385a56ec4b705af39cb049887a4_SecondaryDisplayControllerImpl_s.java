 package demo.client.local.game;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.ErraiBus;
 import org.jboss.errai.bus.client.api.base.MessageBuilder;
 import org.jboss.errai.bus.client.api.messaging.MessageBus;
 import org.jboss.errai.ui.client.widget.ListWidget;
 
 import com.google.gwt.canvas.client.Canvas;
 
 import demo.client.local.lobby.Client;
 import demo.client.shared.Command;
 import demo.client.shared.GameRoom;
 import demo.client.shared.Player;
 import demo.client.shared.ScoreTracker;
 
/*
 * A controller class for a Block Drop game. Handles game loop and user input.
 */
 class SecondaryDisplayControllerImpl implements SecondaryDisplayController {
 
   private ListWidget<ScoreTracker, ScorePanel> scoreList;
   private Canvas nextCanvas;
 
   @Inject
   private MessageBus messageBus = ErraiBus.get();
 
   public SecondaryDisplayControllerImpl(ListWidget<ScoreTracker, ScorePanel> scoreList, Canvas nextCanvas) {
     this.scoreList = scoreList;
     this.nextCanvas = nextCanvas;
 
     // Initiate score tracker.
     GameRoom room = Client.getInstance().getGameRoom();
     List<ScoreTracker> scoreTrackers = scoreList.getValue();
     scoreTrackers.addAll(room.getScoreTrackers().values());
     Collections.sort(scoreTrackers);
 
     getScoreTracker().select();
     scoreList.getWidget(getScoreTracker()).setSelected(true);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#selectNextPlayer()
    */
   @Override
   public void selectNextPlayer() {
     ScoreTracker current = getSelectedTracker();
     if (current != null) {
       int index = scoreList.getValue().indexOf(current);
       int next = (index + 1) % scoreList.getValue().size();
       scoreList.getValue().get(index).deselect();
       scoreList.getWidget(index).setSelected(false);
       scoreList.getValue().get(next).select();
       scoreList.getWidget(next).setSelected(true);
       MessageBuilder.createMessage("Game" + Client.getInstance().getGameRoom().getId())
               .command(Command.SWITCH_OPPONENT).withValue(getSelectedTracker().getPlayer()).noErrorHandling()
               .sendNowWith(messageBus);
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#selectLastPlayer()
    */
   @Override
   public void selectLastPlayer() {
     ScoreTracker current = getSelectedTracker();
     if (current != null) {
       int index = scoreList.getValue().indexOf(current);
       int last = (index - 1 + scoreList.getValue().size()) % scoreList.getValue().size();
       scoreList.getValue().get(index).deselect();
       scoreList.getWidget(index).setSelected(false);
       scoreList.getWidget(last).setSelected(true);
       scoreList.getValue().get(last).select();
       MessageBuilder.createMessage("Game" + Client.getInstance().getGameRoom().getId())
       .command(Command.SWITCH_OPPONENT).withValue(getSelectedTracker().getPlayer()).noErrorHandling()
       .sendNowWith(messageBus);
     }
   }
 
   private ScoreTracker getSelectedTracker() {
     for (ScoreTracker tracker : scoreList.getValue()) {
       if (tracker.isSelected()) {
         return tracker;
       }
     }
     return null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#updateScore(int)
    */
   @Override
   public void updateScore(int numFullRows) {
     ScoreTracker scoreTracker = getScoreTracker();
     scoreTracker.updateScore(numFullRows);
     updateAndSortScore(scoreTracker);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#updateAndSortScore(demo.client.shared.
    * ScoreTracker)
    */
   @Override
   public void updateAndSortScore(ScoreTracker scoreTracker) {
     List<ScoreTracker> modelList = scoreList.getValue();
     if (modelList.contains(scoreTracker)) {
       // Remove out-of-date score from list (different instance)
      modelList.remove(scoreTracker);
     }
     modelList.add(scoreTracker);
     Collections.sort(scoreList.getValue(), Collections.reverseOrder());
     for (int i = 0; i < modelList.size(); i++) {
       scoreList.getWidget(i).setSelected(modelList.get(i).isSelected());
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#getScoreTracker()
    */
   @Override
   public ScoreTracker getScoreTracker() {
     List<ScoreTracker> trackers = scoreList.getValue();
     for (ScoreTracker t : trackers) {
       if (t.getPlayer().equals(Client.getInstance().getPlayer())) {
         return t;
       }
     }
     return null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * demo.client.local.game.SecondaryDisplayController#drawBlockToNextCanvas(demo.client.local.game
    * .Block)
    */
   @Override
   public void drawBlockToNextCanvas(Block nextBlock) {
     // Clear everything.
     nextCanvas.getContext2d().setFillStyle("lightgrey");
     nextCanvas.getContext2d().fillRect(0, 0, Size.NEXT_COORD_WIDTH, Size.MAIN_COORD_HEIGHT);
 
     // Draw title.
     nextCanvas.getContext2d().setFillStyle("black");
     nextCanvas.getContext2d().setFont("bold 20px sans-serif");
     nextCanvas.getContext2d().fillText("Next Block", 10, 20);
 
     nextBlock.draw(2 * Size.MAIN_BLOCK_SIZE + nextBlock.getCentreColDiff() * Size.MAIN_BLOCK_SIZE, 2
             * Size.MAIN_BLOCK_SIZE + nextBlock.getCentreRowDiff() * Size.MAIN_BLOCK_SIZE, nextCanvas.getContext2d());
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#getTarget()
    */
   @Override
   public Player getTarget() {
     Player target = getSelectedTracker().getPlayer();
     return getScoreTracker().getPlayer().equals(target) ? null : target;
   }
 }

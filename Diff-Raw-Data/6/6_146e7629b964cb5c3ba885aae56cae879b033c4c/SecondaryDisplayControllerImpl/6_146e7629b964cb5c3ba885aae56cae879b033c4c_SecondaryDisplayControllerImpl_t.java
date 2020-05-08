 package demo.client.local.game.controllers;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.jboss.errai.bus.client.ErraiBus;
 import org.jboss.errai.bus.client.api.base.MessageBuilder;
 import org.jboss.errai.bus.client.api.messaging.MessageBus;
 import org.jboss.errai.ui.client.widget.ListWidget;
 
 import com.google.gwt.canvas.client.Canvas;
 
 import demo.client.local.game.gui.Block;
 import demo.client.local.game.gui.ScorePanel;
 import demo.client.local.game.tools.Positioner;
 import demo.client.local.game.tools.Size;
 import demo.client.local.game.tools.Size.SizeCategory;
 import demo.client.local.lobby.Client;
 import demo.client.shared.message.Command;
 import demo.client.shared.meta.GameRoom;
 import demo.client.shared.meta.Player;
 import demo.client.shared.meta.ScoreTracker;
 
 /**
  * An implementation of {@link demo.client.local.game.controllers.SecondaryDisplayController
  * SecondaryDisplayController}
  * 
  * @author mbarkley <mbarkley@redhat.com>
  * 
  */
 public class SecondaryDisplayControllerImpl implements SecondaryDisplayController {
 
   private ListWidget<ScoreTracker, ScorePanel> scoreList;
   private Canvas nextCanvas;
 
   @Inject
   private MessageBus messageBus = ErraiBus.get();
 
   /**
    * Create a SecondaryDisplayControllerImpl instance.
    * 
    * @param scoreList
    *          The list of players and their scores.
    * @param nextCanvas
    *          The canvas on which to display the upcoming block.
    */
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
 
   /**
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
 
   /**
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
 
   /**
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
 
   /**
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#updateAndSortScore(demo.client.shared.meta.ScoreTracker)
    */
   @Override
   public void updateAndSortScore(ScoreTracker scoreTracker) {
     List<ScoreTracker> modelList = scoreList.getValue();
     if (modelList.contains(scoreTracker)) {
       // Remove out-of-date score from list (different instance)
       ScoreTracker oldTracker = modelList.get(modelList.indexOf(scoreTracker));
       modelList.remove(oldTracker);
       if (oldTracker.isSelected())
         scoreTracker.select();
     }
     modelList.add(scoreTracker);
     Collections.sort(scoreList.getValue(), Collections.reverseOrder());
     for (int i = 0; i < modelList.size(); i++) {
       scoreList.getWidget(i).setSelected(modelList.get(i).isSelected());
     }
   }
 
   /**
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
 
   /**
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#drawBlockToNextCanvas(demo.client.local.game
    *      .Block)
    */
   @Override
   public void drawBlockToNextCanvas(Block nextBlock) {
     Size size = Size.getSize(SizeCategory.NEXT);
     nextBlock = new Block(nextBlock.getModel(), SizeCategory.NEXT);
     // Clear everything.
     nextCanvas.getContext2d().clearRect(0, 0, size.getCoordWidth(), size.getCoordHeight());
 
     // Draw title.
//    nextCanvas.getContext2d().setFillStyle("black");
//    nextCanvas.getContext2d().setFont("bold 120% sans-serif");
//    nextCanvas.getContext2d().fillText("Next Block", 10, 20);
     
     Positioner pos = new Positioner(nextBlock.getModel());
 
     double x = size.getCoordWidth() / 2 - pos.getColShift() * size.getBlockSize();
     double y = size.getCoordHeight() * 2 / 5;
     nextBlock.draw(x, y, nextCanvas.getContext2d());
   }
 
   /**
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.SecondaryDisplayController#getTarget()
    */
   @Override
   public Player getTarget() {
     Player target = getSelectedTracker().getPlayer();
     return getScoreTracker().getPlayer().equals(target) ? null : target;
   }
 
   /**
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.controllers.SecondaryDisplayController#removeTracker(demo.client.shared.meta.Player)
    */
   @Override
   public void removeTracker(Player player) {
     List<ScoreTracker> modelList = scoreList.getValue();
     for (int i = 0; i < modelList.size(); i++) {
       if (modelList.get(i).getPlayer().equals(player)) {
         ScoreTracker removed = modelList.get(i);
         if (removed.isSelected()) {
           int j = (i + 1) % modelList.size();
           modelList.get(j).select();
           scoreList.getWidget(j).setSelected(true);
         }
         modelList.remove(i);
         break;
       }
     }
   }
 
   /**
    * (non-Javadoc)
    * 
    * @see demo.client.local.game.controllers.SecondaryDisplayController#selectPlayerByIndex(int)
    */
   @Override
   public void selectPlayerByIndex(int i) {
     ScoreTracker selected = getSelectedTracker();
     selected.deselect();
     scoreList.getWidget(selected).setSelected(false);
     scoreList.getValue().get(i).select();
     scoreList.getWidget(i).setSelected(true);
 
     MessageBuilder.createMessage("Game" + Client.getInstance().getGameRoom().getId()).command(Command.SWITCH_OPPONENT)
             .withValue(getSelectedTracker().getPlayer()).noErrorHandling().sendNowWith(messageBus);
   }
 }

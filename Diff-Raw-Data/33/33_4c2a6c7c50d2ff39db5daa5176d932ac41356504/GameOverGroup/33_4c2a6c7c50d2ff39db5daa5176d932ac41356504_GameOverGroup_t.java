 package ca.couchware.wezzle2d.ui.group;
 
 import ca.couchware.wezzle2d.manager.GroupManager;
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.manager.BoardManager.AnimationType;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.ResourceFactory.LabelBuilder;
 import ca.couchware.wezzle2d.event.GameEvent;
 import ca.couchware.wezzle2d.event.IGameListener;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.ui.*;
 import java.util.EnumSet;
 
 /**
  *
  * @author cdmckay
  */
 public class GameOverGroup extends AbstractGroup implements IGameListener
 {         
     
     /**
      * The header label.
      */
     private ITextLabel headerLabel;
     
     /**
      * The final score header label.
      */
     private ITextLabel scoreHeaderLabel;
     
     /**
      * The final score label.
      */
     private ITextLabel scoreLabel;
     
     /**
      * The restart button.
      */
     private IButton restartButton;
     
     /**
      * The continue button.
      */
     private IButton continueButton;
     
     /**
      * The constructor.
      * @param layerMan
      */    
     public GameOverGroup(ManagerHub hub)
     {    
         // Sanity check.
         if( hub == null)
             throw new IllegalArgumentException("hub must not be null.");
         
         // Create the game over header.
         headerLabel = new LabelBuilder(400, 181)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .color(hub.settingsMan.getColor(Key.GAME_COLOR_PRIMARY)).size(26).text("Game over :(")
                 .visible(false).build();
         hub.layerMan.add(headerLabel, Layer.UI);
         entityList.add(headerLabel);
         
         // Create the final score header.
         scoreHeaderLabel = new LabelBuilder(400, 234)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .color(hub.settingsMan.getColor(Key.GAME_COLOR_PRIMARY)).size(14)
                 .text("Your final score was")
                 .visible(false).build();
         hub.layerMan.add(scoreHeaderLabel, Layer.UI); 
         entityList.add(scoreHeaderLabel);
         
         // Create the final score label.
         scoreLabel = new LabelBuilder(400, 270)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 .color(hub.settingsMan.getColor(Key.GAME_COLOR_PRIMARY)).size(30)
                 .text("0").visible(false).build();
         hub.layerMan.add(scoreLabel, Layer.UI); 
         entityList.add(scoreLabel);
         
         // Create restart button.
         restartButton = new Button.Builder(400, 345)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))
                 //.type(SpriteButton.Type.THIN)
                 .text("Restart").normalOpacity(70).visible(false).build();
         hub.layerMan.add(restartButton, Layer.UI);
         entityList.add(restartButton);
         
         // Create continue button, using the restart button as a template.
         continueButton = new Button.Builder((Button) restartButton)
                 .y(405).text("Continue").build();
         hub.layerMan.add(continueButton, Layer.UI);
         entityList.add(continueButton);
     }        
     
     public void setScore(final int score)
     {
         this.scoreLabel.setText(String.format("%,d", score));
     }      
     
     /**
      * Override the update logic method.
      * @param game The game state.
      */    
     public void updateLogic(Game game, ManagerHub hub)
     {
         // Hide the screen.
         hub.groupMan.hideGroup(
                 GroupManager.Class.GAME_OVER,
                 GroupManager.Layer.BOTTOM,
                 !game.isCompletelyBusy());
                        
         // Reset the stat man.
        Game.resetGame(hub, game, this.restartButton.isActivated());
 
         // Create board and make it invisible.
         hub.boardMan.setVisible(false);
         hub.boardMan.generateBoard(hub.itemMan.getItemList(), hub.levelMan.getLevel());                    
 
         // Unpause the game.
         // Don't worry! The game won't pass updates to the 
         // timer unless the board is shown.  In hindsight, this
         // is kind of crappy, but whatever, we'll make it prettier
         // one day.
         hub.timerMan.setPaused(false);
 
         // Reset the timer.
         //hub.timerMan.resetCurrentTime();
 
         // Start the board show animation.  This will
         // make the board visible when it's done.
         game.startBoardShowAnimation(AnimationType.ROW_FADE);        
         
         // Deactivate button.
         this.restartButton.setActivated(false);
         this.continueButton.setActivated(false);        
     }
 
     public void gameStarted(GameEvent event)
     {
         // Intentionally blank.        
     }
 
     public void gameReset(GameEvent event)
     {
         // Intentionally blank.
     }
 
     public void gameOver(GameEvent event)
     {
         // Set the score to whatever the score manager has now.
         this.setScore(event.getTotalScore());
     }
             
 }

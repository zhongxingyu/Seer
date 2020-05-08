 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.tutorial;
 
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.ManagerHub;
 import ca.couchware.wezzle2d.Refactorer;
 import ca.couchware.wezzle2d.Refactorer.RefactorSpeed;
 import ca.couchware.wezzle2d.Rule;
 import ca.couchware.wezzle2d.animation.AnimationAdapter;
 import ca.couchware.wezzle2d.animation.FadeAnimation;
 import ca.couchware.wezzle2d.animation.IAnimation;
 import ca.couchware.wezzle2d.graphics.EntityGroup;
 import ca.couchware.wezzle2d.graphics.IPositionable.Alignment;
 import ca.couchware.wezzle2d.manager.BoardManager.Direction;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.piece.Piece;
 import ca.couchware.wezzle2d.piece.PieceType;
 import ca.couchware.wezzle2d.ui.IButton;
 import ca.couchware.wezzle2d.ui.ITextLabel;
 import ca.couchware.wezzle2d.ui.SpeechBubble;
 import ca.couchware.wezzle2d.ui.Button;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 
 /**
  *
  * @author cdmckay
  */
 public abstract class AbstractTutorial implements ITutorial 
 {
         
     /** Has this tutorial been initalized? Initially false. */
     protected boolean initialized = false;
     
     /** Has the tutorial been completed? Initially false. */
     protected boolean done = false;
     
     /** The refactorer. */
     protected Refactorer refactorer;
     
     /** The name of the tutorial. */
     protected String name;      
     
     /** Is the menu visible? */
     protected boolean menuShown = false;
     
     /** The text that directs the user. */
     protected List<ITextLabel> labelList;
     
     /** A speech bubble that indicates where the user should press. */
     protected SpeechBubble bubble;       
     
     /** The repeat button. */
     protected IButton repeatButton;
     
     /** The continue button. */
     protected IButton continueButton;
 
     /** The piece the user started the tutorial with. */
     protected Piece pieceBeforeInitialization;
     
     /** The list of rules. */
     private List<Rule> ruleList = new ArrayList<Rule>();        
     
     /**
      * Create a new tutorial that is activated when the associated rule is
      * true.
      * 
      * @param rule
      */    
     public AbstractTutorial(Refactorer refactorer, String name) 
     { 
         // Set the refactorer.
         this.refactorer = refactorer;
         
         // Set the tutorial name.
         this.name = name;
     }
     
     public void initialize(final Game game, final ManagerHub hub)
     {
         // Set the activataed variable.
         this.initialized = true;              
 
         // Save the manager states.
         hub.boardMan.saveState();
         hub.statMan .saveState();
         hub.scoreMan.saveState();
              
         // Stop the piece manager from dropping.
         game.getTileDropper().setDropOnCommit(false);
         
         // Stop the timer.
         hub.timerMan.setStopped(true);
 
         // Hide the piece preview.
         game.getUI().setTraditionalPiecePreviewVisible(false);
         game.getUI().setOverlayPiecePreviewVisible(false);
 
         // Remember the piece we entered with.
         this.pieceBeforeInitialization = hub.pieceMan.getPiece();
 
         // Create repeat button.
         Button templateButton = new Button.Builder(400, 0)
                 .alignment(EnumSet.of(Alignment.MIDDLE, Alignment.CENTER))                
                 .text("")
                 .normalOpacity( 80 )                
                 .visible(false)
                 .build();
 
         repeatButton = new Button
                 .Builder(templateButton).y(330).text("Repeat").build();
         
         hub.layerMan.add(repeatButton, Layer.EFFECT);
               
         continueButton = new Button
                 .Builder(templateButton).y(390).text("Continue").build();
 
         hub.layerMan.add(continueButton, Layer.EFFECT);
 
         // Run the tutorials initialization.
         tutorialInitialize( game, hub );
 
         // Run any post initialization.
         postInitialize( game, hub );
 
         // Run the repeat tutorial method, that sets up the things that must
         // be reset each time the tutorial is run.
         repeat( game, hub );
     }
 
     /**
      * This method should contain all initialization logic for an individual
      * tutorial.
      */
     protected abstract void tutorialInitialize(final Game game, final ManagerHub hub);
 
     /**
      * This will be run at the end of the initialize method.
      *
      * @param game
      * @param hub
      */
     private void postInitialize(final Game game, final ManagerHub hub)
     {
         if (this.labelList != null && !this.labelList.isEmpty())
         {
             EntityGroup text = new EntityGroup( this.labelList );
             text.setOpacity( 0 );
             IAnimation fade = new FadeAnimation.Builder(FadeAnimation.Type.IN, text)
                     .duration(300).maxOpacity(100).build();
 
             hub.gameAnimationMan.add( fade );
         }
     }
 
     protected boolean update(final Game game, final ManagerHub hub)
     {        
         // If the move count is not 0, then the tutorial is over.
         if (hub.statMan.getMoveCount() != 0 && !menuShown)
         {               
             // Stop the piece animation.
             hub.pieceMan.hidePieceGrid();
             hub.pieceMan.hideShadowPieceGrid();
             hub.pieceMan.stopAnimation();
             
             // Lock the whole board.
             hub.pieceMan.clearRestrictionBoard();
             hub.pieceMan.reverseRestrictionBoard();
             
             // Fade the board out.            
             final EntityGroup entityGroup = hub.boardMan.getTileRange(
                     hub.boardMan.getNumberOfCells() / 2,
                     hub.boardMan.getNumberOfCells() - 1);
             
             IAnimation fadeTiles = new FadeAnimation.Builder(FadeAnimation.Type.OUT, entityGroup)
                     .wait(0).duration(500).build();
             
             fadeTiles.addAnimationListener(new AnimationAdapter()
             {                
                 @Override
                 public void animationFinished()
                 { entityGroup.setVisible(false); }
             });           
             
             hub.gameAnimationMan.add(fadeTiles);
                                     
             // Fade in two new buttons.
             FadeAnimation fade;                        
              
             //f = new FadeAnimation(FadeType.IN, 100, 500, repeatButton);
             //f.setMaxOpacity(70);
             repeatButton.setOpacity( 0 );
             fade = new FadeAnimation.Builder(FadeAnimation.Type.IN, repeatButton)
                     .wait(100).duration(300).maxOpacity(100).build();
             hub.gameAnimationMan.add( fade );
                          
             //f = new FadeAnimation(FadeType.IN, 100, 500, continueButton);
             //f.setMaxOpacity(70);
             continueButton.setOpacity( 0 );
             fade = new FadeAnimation.Builder(FadeAnimation.Type.IN, continueButton)
                     .wait(100).duration(300).maxOpacity(100).build();
             hub.gameAnimationMan.add( fade );
 
             // Menu is now shown.
            menuShown = true;
            hub.layerMan.hide(Layer.PIECE_GRID);
             
             return true;
         }
         else if (menuShown)
         {
             if (repeatButton.isActivated())
             {
                 repeat(game, hub);
             }
             else if (continueButton.isActivated())
             {
                 return false;
             }
         }      
         
         return true;
     }
     
     public void addRule(Rule rule)
     {
         ruleList.add(rule);
     }
 
     public void removeRule(Rule rule)
     {
         ruleList.remove(rule);
     }
     
     /**
      * Evaluates the tutorial activation rules.  Returns true if they are
      * met, false otherwise.
      * 
      * @param game
      * @return
      */
     public boolean evaluateRules(Game game, ManagerHub hub)
     {
         // Check all the rules.  If any of them are false, return false.
         for (Rule rule : ruleList)
             if (!rule.evaluate(game, hub))
                 return false;
         
         // If all the rules are true, then return true.        
         return true;
     }
     
     /**
      * Updates the tutorial's internal logic.  If the tutorial is activated,
      * it will continue with the tutorial.  If the tutorial is not activated,
      * it will evaluate the activation rules to see if it should be activated.
      * 
      * @param game
      */
     public void updateLogic(Game game, ManagerHub hub)
     {                
         // If we're activated, run the tutorial logic.
         if (initialized && !update(game, hub))
         {
             initialized = false;
             finish(game, hub);
             done = true;
         }
     }       
     
     public void finish(final Game game, final ManagerHub hub)
     {        
        // Make sure the piece grid is visible again.
        hub.layerMan.show(Layer.PIECE_GRID);

         // Write out completion.
         hub.settingsMan.setBool(this.getHasRunSettingsKey(), true);
 
         // Load the score managers state.
         hub.boardMan.loadState();
         hub.statMan .loadState();
         hub.scoreMan.loadState();
         
         EntityGroup buttons = new EntityGroup(repeatButton, continueButton);
 
         IAnimation fadeButtons = new FadeAnimation
                 .Builder(FadeAnimation.Type.OUT, buttons)
                 .duration( 300 )
                 .build();
 
         fadeButtons.addAnimationListener( new AnimationAdapter()
         {
 
             @Override
             public void animationFinished()
             {
                 hub.layerMan.remove(repeatButton, Layer.EFFECT);
                 repeatButton.dispose();
                 repeatButton = null;
 
                 hub.layerMan.remove(continueButton, Layer.EFFECT);
                 continueButton.dispose();
                 continueButton = null;
             }
 
         });
 
         hub.gameAnimationMan.add( fadeButtons );                               
 
         hub.layerMan.remove(bubble, Layer.EFFECT);
         bubble.dispose();
         bubble = null;
 
         for (ITextLabel label : labelList)
         {
             hub.layerMan.remove(label, Layer.EFFECT);
             label.dispose();
         }
         labelList = null;
         
         // Remove the restriction board.
         hub.pieceMan.clearRestrictionBoard();
         
         // Turn on tile drops.
         game.getTileDropper().setDropOnCommit(true);
         
         // Start the timer.        
         hub.timerMan.setStopped(false);
         
         // Reset the refactor speed.
         RefactorSpeed speed = game.getDifficulty().getStrategy().getRefactorSpeed();
         refactorer.setRefactorSpeed(speed);
 
         // Reset the piece preview.
         boolean showTraditional = hub.settingsMan.getBool(Key.USER_PIECE_PREVIEW_TRADITIONAL);
         boolean showOverlay = hub.settingsMan.getBool(Key.USER_PIECE_PREVIEW_OVERLAY);
         game.getUI().setTraditionalPiecePreviewVisible(showTraditional);
         game.getUI().setOverlayPiecePreviewVisible(showOverlay);
 
         // Reset the piece we had before the tutorial ran.
         hub.pieceMan.setPiece(pieceBeforeInitialization);
     }   
 
     protected void repeat(final Game game, final ManagerHub hub)
     {        
         // Make sure to reset the gravity.
         hub.boardMan.setGravity(EnumSet.of(Direction.DOWN, Direction.LEFT));
         
         // Reset move counter.
         hub.statMan.resetMoveCount();
         
         // Reset the scores.
         hub.scoreMan.resetScore();
         
         // Create the fake board.
         createBoard(game, hub);
         
         // Fade board in.
         final EntityGroup e = hub.boardMan.getTileRange(
                 hub.boardMan.getNumberOfCells() / 2,
                 hub.boardMan.getNumberOfCells() - 1);
         
         e.setVisible(false);
         e.setOpacity(0);
         IAnimation a = new FadeAnimation.Builder(FadeAnimation.Type.IN, e)
                 .wait(0).duration(300).build();
         
         hub.gameAnimationMan.add(a);        
         
         // Change the piece to the dot.
         hub.pieceMan.setPiece(PieceType.DOT.getPiece());
         
         // Make sure buttons aren't visible.
         repeatButton.setVisible(false);
         if (repeatButton.isActivated())
         {
             repeatButton.setActivated(false);
         }
         
         continueButton.setVisible(false);
         if (continueButton.isActivated())
         {
             continueButton.setActivated(false);
         }
         
         // Menu is not shown.
         menuShown = false;
        hub.layerMan.show(Layer.PIECE_GRID);
         
         // Make sure the bubble opacity is full.
         bubble.setOpacity(100);            
         
         // Piece is visible.
         hub.pieceMan.showPieceGrid();
         hub.pieceMan.startAnimation(hub.timerMan);
     }    
     
     protected abstract void createBoard(final Game game, final ManagerHub hub);
     
     public boolean isInitialized()
     {
         return initialized;
     }   
 
     public boolean isDone()
     {
         return done;
     }
 
     public boolean isMenuShowing()
     {
         return menuShown;
     }
 
     public String getName()
     {
         return name;
     }
 
     public boolean hasRun(final ManagerHub hub)
     {
         return hub.settingsMan.getBool(this.getHasRunSettingsKey());
     }
     
 }

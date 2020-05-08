 
 package vooga.scroller.level_management;
 
 import java.awt.Dimension;
 import java.util.HashMap;
 import java.util.Map;
 import util.Location;
 import vooga.scroller.level_editor.Level;
 import vooga.scroller.scrollingmanager.ScrollingManager;
 import vooga.scroller.sprites.test_sprites.MarioLib;
 import vooga.scroller.sprites.test_sprites.MarioLib.MovingPlatformOne;
 import vooga.scroller.util.Pixmap;
 import vooga.scroller.util.PlatformerConstants;
 import vooga.scroller.view.View;
 /**
  * Instantiates all of the levels for gameplay.
  * 
  * @author Scott Valentine
  *
  */
 public class LevelFactory {
     
     private LevelManager myLevelManager;
     
     public LevelFactory(LevelManager lm){
         myLevelManager = lm;
     }
     
     /**
      * Generates levels to be displayed by the view and played by the model.
      * 
      * @param view is the view used for level information.
      * @return a List of all levels that will be played in the game.
      */
     public Map<Integer,Level> generateLevels (ScrollingManager myScrollingManager, View view) {
         Level myCurrLevel = new Level(1, myScrollingManager, view);
         
         // TODO: this will ideally read in levels from file and create instances of each level
         // This works for demos
         
         
         myCurrLevel.addSprite(new MarioLib.Coin( 
                                 new Location(view.getWidth() - 400, view.getHeight() - 250)
                                 ));   
         
         myCurrLevel.addSprite(new MarioLib.Coin( 
                                                 new Location(view.getWidth() - 400, view.getHeight() - 350)
                                                 ));   
 
         myCurrLevel.addSprite(new MarioLib.Koopa( 
                                  new Location(view.getWidth() - 300, view.getHeight() - 275)  
                                  ));   
         
         
         myCurrLevel.addSprite(new MarioLib.Koopa( 
                                  new Location(view.getWidth() - 200, view.getHeight() - 350)  
                                        ));   
         
         myCurrLevel.addSprite(new MarioLib.Platform( 
                                     new Location(500, 600)
                                     ));
         myCurrLevel.addSprite(new MarioLib.Platform( 
                                                     new Location(700, 600)
                                                     ));
         
//        myCurrLevel.addSprite(new MarioLib.Platform( 
//                                                    new Location(900, 500)
//                                                    ));
         myCurrLevel.addSprite(new MarioLib.Platform( 
                                                     new Location(1100, 500)
                                                     ));
         
         myCurrLevel.addSprite(new MarioLib.Platform( 
                                                     new Location(1300, 400)
                                                     ));
         myCurrLevel.addSprite(new MarioLib.Platform( 
                                                     new Location(1500, 400)
                                                     ));
        
        myCurrLevel.addSprite(new MarioLib.MovingPlatformTwo( 
                                                    new Location(900, 500)
                                                    )); 
 
         myCurrLevel.addSprite(new MarioLib.Turtle(
                                   new Location(view.getWidth() - 500, view.getHeight() - 75)
                                   ));   
         
         myCurrLevel.addSprite(new MovingPlatformOne( 
                                                     new Location(view.getWidth() - 80, view.getHeight() - 20)
                                                     )); 
         
 
         myCurrLevel.addSprite(new MarioLib.MovingPlatformOne( 
                                                     new Location(400, 200)
                                                     )); 
         
         myCurrLevel.addSprite(new MarioLib.MovingPlatformTwo( 
                                                              new Location(200, 200)
                                                              )); 
         
         // adding levelportal --> acts as portal between levels.
         
         
         Level secondLevel = new Level(2, myScrollingManager, view);
         
         StartPoint exit= new StaticStartPoint(myCurrLevel, new Location(900,500));
         
         
         LevelPortal portal = new LevelPortal(new Pixmap("brick9.gif"), new Location(900,900),
                                new Dimension(50,50), exit, myLevelManager);
         
         myCurrLevel.addSprite(portal);
         
         myCurrLevel.setSize(PlatformerConstants.DEFAULT_LEVEL_SIZE);
         Map<Integer,Level> l = new HashMap<Integer,Level>();
         l.put(myCurrLevel.getID(),myCurrLevel);
         l.put(secondLevel.getID(), secondLevel);
         return l;
     }
 
 }

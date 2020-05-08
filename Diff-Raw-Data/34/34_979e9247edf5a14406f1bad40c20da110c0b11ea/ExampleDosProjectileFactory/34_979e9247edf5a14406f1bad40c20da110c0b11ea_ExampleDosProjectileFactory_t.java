 package vooga.towerdefense.factories.examplesfactories;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.action.actionlist.Move;
 import vooga.towerdefense.action.actionlist.MoveToTarget;
 import vooga.towerdefense.attributes.AttributeConstants;
 import vooga.towerdefense.attributes.AttributeManager;
 import vooga.towerdefense.factories.GameElementFactory;
 import vooga.towerdefense.factories.definitions.DefinitionConstants;
 import vooga.towerdefense.factories.definitions.GameElementDefinition;
 import vooga.towerdefense.factories.definitions.ProjectileDefinition;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.util.Location;
 
 /**
  * 
  * Example projectile that moves towards targets.
  * Actions and attributes are manually defined here.
  * 
  * @author Matthew Roy
  * @edited Xu Rui
  * 
  */
 public class ExampleDosProjectileFactory extends GameElementFactory {
 
     private GameElementDefinition def;
     private AttributeManager AM;
     
     public ExampleDosProjectileFactory(){
         def = new ProjectileDefinition();
         AM = getDefaultAM();
     }
     public GameElement createElement (Location putHere) {
         GameElement myProjectile;
         if (putHere != null) {
             myProjectile = new GameElement(def.getImage(), putHere,
                                      new Dimension(25,25), AM);
         }
         else {
             myProjectile = new GameElement(def.getImage(),
                                      def.getCenter(), def.getSize(), AM);
         }
 
         ArrayList<Action> actions = new ArrayList<Action>();
 
         actions.add(new MoveToTarget(myProjectile.getCenter(), new Location(400, 400), AM.getAttribute(AttributeConstants.MOVE_SPEED)));
 
         myProjectile.addActions(actions);
         return myProjectile;
     }
 
     /**
      * @param myStart
      * @param center
      * @return
      */
     public GameElement createElement (GameElement myStart, GameElement e, GameMap map) {
         initialize(map);
         GameElement myProjectile;
         if (myStart != null) {
             myProjectile = new GameElement(def.getImage(), myStart.getCenter(),
                                           new Dimension(30, 30), AM);
         }
         else {
             myProjectile = new GameElement(def.getImage(),
                                           def.getCenter(), def.getSize(), AM);
         }
 
         myProjectile.addActions(createActions(myProjectile));
         return myProjectile;
     }
 
     /**
      * Manually add game element actions (for testing only actions will be predefined in level editor)
      */
     @Override
     public List<Action> createActions(GameElement element) {
         ArrayList<Action> actions = new ArrayList<Action>();
         actions.add(new Move(element.getCenter(), element.getAttributeManager()
                 .getAttribute(AttributeConstants.MOVE_SPEED), element.getAttributeManager()
                  .getAttribute(AttributeConstants.DIRECTION)));
         return actions;
     }
 
 }

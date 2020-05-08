 package vooga.towerdefense.factories.examplesfactories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.action.actionlist.FollowPath;
 import vooga.towerdefense.action.actionlist.Move;
 import vooga.towerdefense.action.actionlist.OnDeath;
 import vooga.towerdefense.action.actionlist.RemoveGameElement;
 import vooga.towerdefense.attributes.AttributeConstants;
 import vooga.towerdefense.attributes.DefaultAttributeManager;
 import vooga.towerdefense.factories.GameElementFactory;
 import vooga.towerdefense.factories.definitions.GameElementDefinition;
 import vooga.towerdefense.factories.definitions.UnitDefinition;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.Path;
 import vooga.towerdefense.util.Location;
 
 
 /**
  * @author Matthew Roy
  * @author Jimmy Longley
  * 
  */
 @Deprecated
 public class ExampleUnitFactory extends GameElementFactory {
 
     private GameMap myGameMap;
 
     /**
      * @param elementName
      * @param definition
      */
     public ExampleUnitFactory (String elementName,
                                GameElementDefinition definition, GameMap gameMap) {
         super(elementName, definition);
         myGameMap = gameMap;
     }
 
     public GameElement createElement (Location putHere) {
         return createElement(putHere, new UnitDefinition());
     }
 
     public GameElement createElement (Location putHere, UnitDefinition myDef) {
         UnitDefinition myDefinition = myDef;
         DefaultAttributeManager AM = new DefaultAttributeManager();
         AM.replaceAttributeValue(AttributeConstants.MOVE_SPEED, 100);
 
         GameElement myGameElement;
         if (putHere != null) {
             myGameElement = new GameElement(myDefinition.getImage(), putHere,
                                            myDefinition.getSize(), AM, "unit");
         }
         else {
             myGameElement = new GameElement(myDefinition.getImage(),
                                            myDefinition.getCenter(), myDefinition.getSize(), AM, "unit");
         }
         
         myGameElement.addActions(createActions(myGameElement));
         return myGameElement;
     }
     
     /**
      * Manually add game element actions (for testing only actions will be predefined in level editor)
      */
     @Override
     public List<Action> createActions(GameElement element) {
         Path path = myGameMap.getShortestPath(element.getCenter(),
                myGameMap.default_end_location);
     	ArrayList<Action> actions = new ArrayList<Action>();
         actions.add(new FollowPath(element, path));
         actions.add(new Move(element.getCenter(), element.getAttributeManager()
                 .getAttribute(AttributeConstants.MOVE_SPEED), element
                 .getAttributeManager().getAttribute(
                                                     AttributeConstants.DIRECTION)));
         Action myDeath = new OnDeath(element);
         myDeath.addFollowUpAction(new RemoveGameElement(myGameMap, element));
         actions.add(myDeath);
         return actions;
     }
     
 
 }

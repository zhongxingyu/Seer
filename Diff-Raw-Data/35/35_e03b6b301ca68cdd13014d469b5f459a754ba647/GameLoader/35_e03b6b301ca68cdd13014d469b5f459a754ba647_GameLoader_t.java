 package vooga.towerdefense.gameeditor.gameloader;
 
 import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
 import java.util.List;
import java.util.Map;
 import util.XMLTool;
 import vooga.towerdefense.controller.Controller;
import vooga.towerdefense.factories.elementfactories.GameElementFactory;
 import vooga.towerdefense.gameeditor.gameloader.MapXMLLoader;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.model.levels.Level;
 import vooga.towerdefense.model.rules.Rule;
 import vooga.towerdefense.view.TDView;
 
 /**
  * GameLoader loads in the information from the XML file and
  *      starts the game.
  *
  * @author Angelica Schwartz
  */
 public class GameLoader {
     
     private XMLTool myXMLTool;
    private Map<String, GameElementFactory> gameElements;
     
     public GameLoader(String xmlFilePath) {
         myXMLTool = new XMLTool(xmlFilePath);
        gameElements = new HashMap<String, GameElementFactory>();
     }
     
     public List<GameMap> loadMaps() {
         MapXMLLoader mapLoader = new MapXMLLoader(myXMLTool);
         List<GameMap> maps = mapLoader.loadMaps();
         return maps;
     }
     
     public TDView loadView(Controller controller) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
         ViewXMLLoader loader = new ViewXMLLoader(myXMLTool);
         return loader.makeView(controller);
     }
     
    public void loadElements() {
        GameElementXMLLoader loader = new GameElementXMLLoader(myXMLTool);
        gameElements = loader.loadGameElementFactories();
    }
    
     public List<Level> loadLevels(GameModel model) {
         LevelsXMLLoader loader = new LevelsXMLLoader(myXMLTool);
         return loader.getLevels(model);
     }
     
     public List<Rule> loadRules(GameModel model) {
         RulesXMLLoader loader = new RulesXMLLoader(myXMLTool);
         return loader.getRules(model);
     }
     
     public void startGame(Controller controller) {
         controller.start();
     }
 
 }

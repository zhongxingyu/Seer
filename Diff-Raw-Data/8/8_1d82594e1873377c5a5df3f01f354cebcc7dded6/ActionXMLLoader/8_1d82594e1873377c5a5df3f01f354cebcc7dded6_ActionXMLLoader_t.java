 
 package vooga.towerdefense.gameeditor.gameloader.xmlloaders;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import org.w3c.dom.Element;
 import util.XMLTool;
 import vooga.towerdefense.action.Action;
 import vooga.towerdefense.factories.actionfactories.ActionFactory;
 import vooga.towerdefense.gameelements.GameElement;
 import vooga.towerdefense.model.GameMap;
 import vooga.towerdefense.model.Player;
 
 /**
  * This class is responsible for loading Action objects
  * from an XML file.
  * 
  * @author Erick Gonzalez
  */
 public class ActionXMLLoader {
     private static final String PARAMETERS_TAG = "parameter";
     private static final String WAVE_ACTION_FACTORIES_CLASSPATH = 
             "vooga.towerdefense.factories.waveactionfactories.";
     private static final String ACTION_FACTORIES_CLASSPATH = 
            "vooga.towerdefense.factories.actionfactories.";
     private static final String WAVE = "Wave";
     private static final String FACTORY = "Factory";
     private XMLTool myXMLTool;
 
     /**
      * 
      * @param xmlTool an XMLTool containing the xml document
      */
     public ActionXMLLoader (XMLTool xmlTool) {
         myXMLTool = xmlTool;
     }
 
     /**
      * Given an XML element representing the <actions> tag, returns
      * a List of Action objects under this tag. Because no
      * GameElement parameter is specified, these actions do not
      * act on a specific GameElement.
      * 
      * @param actionsElement the xml element for the <actions> tag
      * @param gameMap a game map
      * @param player a player object
      * @return a list of actions under the actionsElement
      */
     public List<Action> loadActions (Element actionsElement, GameMap gameMap, Player player) {
         return loadActions(null, actionsElement, gameMap, player);
     }
 
     
     /**
      * Given an XML element representing the <actions> tag, returns a List of
      * Action objects under this tag. Because a GameElement parameter is specified,
      * these actions will act on this specified GameElement object.
      * 
      * @param e a game element upon which the list of actions will act on
      * @param actionsElement the XML element represening the <actions> tag
      * @param gameMap a game map object
      * @param player a player object
      * @return a list of actions acting on the given GameElement
      */
     public List<Action> loadActions (GameElement e, Element actionsElement, GameMap gameMap, 
                                      Player player) {
         List<ActionFactory> actionFactories = loadActionFactories(actionsElement, gameMap, player);
        System.out.println(actionFactories);
 
         List<Action> actions = new ArrayList<Action>();
         for (ActionFactory af : actionFactories) {
             actions.add(af.createAction(e));
         }
 
         return actions;
     }
 
     /**
      * Given an XML element representing the <actions> tag, returns a List of
      * ActionFactory objects under this tag.  
      * 
      * @param actionsElement the XML element representing the <actions> tag
      * @param gameMap a game map object
      * @param player a player object
      * @return a list of ActionFactory objects acting on the given GameElement
      */
     public List<ActionFactory> loadActionFactories (Element actionsElement, GameMap gameMap, 
                                                     Player player) {        
         List<Element> subElements = myXMLTool.getChildrenList(actionsElement);
 
         List<ActionFactory> actionFactories = new ArrayList<ActionFactory>();
         for (Element e : subElements) {
             actionFactories.add(loadActionFactory(e, gameMap, player));
         }
         return actionFactories;
     }
 
     @SuppressWarnings("rawtypes")
     private ActionFactory loadActionFactory (Element actionElement, GameMap gameMap, Player player) {
         String actionName = myXMLTool.getTagName(actionElement);
         
         List<String> parameterStrings = new ArrayList<String>();
         List<ActionFactory> subActionFactories = new ArrayList<ActionFactory>();
 
         List<Element> subElements = myXMLTool.getChildrenList(actionElement);
 
         for (Element subElement: subElements) {
             String subElementName = myXMLTool.getTagName(subElement);
             if (subElementName.equals(PARAMETERS_TAG)) {
                 parameterStrings.add(loadParameterString(subElement));
             }
             else {                
                 subActionFactories.add(loadActionFactory(subElement, gameMap, player));
             }
         }
         
         try {
             String classPath = "";
             if (actionName.contains(WAVE)) {
                 classPath = WAVE_ACTION_FACTORIES_CLASSPATH;
             } 
             else {
                 classPath = ACTION_FACTORIES_CLASSPATH;
             }
             classPath += actionName + FACTORY;
            System.out.println(classPath);
             Class actionFactoryClass = Class.forName(classPath);
 
             Constructor[] constructors = actionFactoryClass.getDeclaredConstructors();
             // ActionFactory objects have only one constructor
             Constructor constructor = constructors[0];
             ActionFactory af = (ActionFactory) constructor.newInstance(parameterStrings.toArray());
             
             af.addFollowUpActionsFactories(subActionFactories);
             af.initialize(gameMap, player);
             return af;
         }
         catch (InstantiationException e) {
             return null;
         }
         catch (IllegalAccessException e) {
             return null;
         }
         catch (IllegalArgumentException e) {
             return null;
         }
         catch (InvocationTargetException e) {
             return null;
         }
         catch (ClassNotFoundException e) {
             return null;
         }        
     }
 
     private String loadParameterString (Element parameterElement) {
         return myXMLTool.getContent(parameterElement);
     }
 }

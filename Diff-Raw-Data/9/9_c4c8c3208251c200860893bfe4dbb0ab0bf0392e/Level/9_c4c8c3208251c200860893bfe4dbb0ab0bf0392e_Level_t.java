 package vooga.towerdefense.model.levels;
 
 import java.util.List;
 
 import vooga.towerdefense.action.Action;
import vooga.towerdefense.action.actionlist.LevelTimerAction;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.model.rules.Rule;
 
 public class Level {
 
 	List<Action> myActions;
 	List<Rule> myRules;
 	
 	public Level(List<Action> actions, List<Rule> rules) {
 		myActions = actions;
 		myRules = rules;
 	}
 	
 	public void start(GameModel model) {
 		model.addActions(myActions);
 	}
 	
 	public List<Rule> getRules() {
 		return myRules;
 	}
 
 	public List<Action> getActions() {
 		return myActions;
 	}
 	
 	public Integer getRemainingTime() {
 		for(Action action : myActions) {
 			if(action instanceof LevelTimerAction) {
 				return ((LevelTimerAction) action).getRemainingTime();
 			}
 		}
 		return null;
 	}
 }

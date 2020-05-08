 package ai;
 
 import gameCharacter.GameCharacter;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import utils.Location;
 import app.RPGame;
 
 import com.golden.gamedev.object.Timer;
 
 /**
  * Path finder that chooses and executes the action
  * that brings the enemy closer to the player.
  * @author jameshong
  *
  */
 public class GreedyPathFindingAI extends AbstractPathFindingAI{

	private static final double MINIMUM_DISTANCE = 50;
 	
 	public GreedyPathFindingAI(RPGame game, GameCharacter character) {
 		super(game, character);
 		this.player = game.getPlayer();
 		calcInterval = new Timer(CALCULATION_INTERVAL);
 	}
 	
 	@Override
 	public int nextAction(){
 		Location currTile = character.getLocation();
 		Location playerTile = player.getCharacter().getLocation();
 		List<ActionTransition> adjActions = getAdjacentActions(currTile,playerTile);
 				
		if(adjActions.get(0) != null && adjActions.get(0).distToGoal() > MINIMUM_DISTANCE){
 			return adjActions.get(0).direction;
 		}
 		else return -1;
 	}
 	
 	public List<ActionTransition> getAdjacentActions(Location currTile,Location goalTile){
 		
 		ArrayList<ActionTransition> adjActions = new ArrayList<ActionTransition>();
 		for(int i = 0; i <= 3; i++){
 			ActionTransition nextAction = 
 					new ActionTransition(currTile,goalTile,i);
 			if(!adjActions.contains(nextAction) && nextAction.intermTile != null)
 				adjActions.add(nextAction);
 		}
 		Collections.sort(adjActions);
 		return adjActions;
 	}
 	
 	@Override
 	public void update(long elapsedTime) {
 		if(!calcInterval.action(elapsedTime))
 			return;
 		int nextDirection = nextAction();
 
 		if ((!character.isActive() || nextDirection != character.getCurrentDirection())
 				&& nextDirection != -1){
 			setActive(true);
 			character.setActiveDirection(nextDirection);
 			character.setVelocity(0.05);
 		} else if (nextDirection == -1){
 			setActive(false);
 			character.stop();
 		}
 	}
 
 }

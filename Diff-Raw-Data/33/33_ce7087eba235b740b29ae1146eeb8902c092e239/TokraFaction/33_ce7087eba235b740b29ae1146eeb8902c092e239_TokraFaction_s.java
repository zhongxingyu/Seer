 package faction;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import planning.AttackTask;
 import planning.DefendTask;
 import planning.ResearchTask;
 import planning.SabotageTask;
 import planning.Task;
 import universe.*;
 
 public class TokraFaction extends Faction 
 {
 	public TokraFaction()
 	{
 		factionName = "Tok'ra";
 		
 		aggression = 50;
 		diplomacy = 80;
 		science = 20;
 		
 		World hw = Universe.generateWorld();
 		hw.setName("Vorash");
 		
 		setHomeWorld(hw);
 		
 		increaseTroops(5);
 		
 		tech.defensiveCapabilities = 2.5;
 		tech.offensiveCapabilities = .5;
 	//	tech.hyperdriveEfficiency = 0;
 		tech.resourceEfficiency = .2;
 	}
 
 	protected ArrayList<Task> getTaskList() {
 		ArrayList<Task> taskList = new ArrayList<Task>();
 		for(Faction enemy : getEnemies()) {
 			for(World to : enemy.getControlledWorlds()) {
 				for(World from : getControlledWorlds()) {
 					Task attack = new AttackTask(from, to, enemy, 
 							new Random().nextInt(50), null);
 					if(attack.canPerform(this))
 						taskList.add(attack);
 				}
 			}
 		}
 		
 		for(Faction faction : Universe.factions) {
 			if(faction != this) {
 				for(World world : faction.getControlledWorlds()) {
 					Task sabotage = new SabotageTask(world, null);
 					if(sabotage.canPerform(this))
 						taskList.add(sabotage);
 				}
 			}
 		}
 		Task defend = new DefendTask(getHomeWorld(), null);
 		
 		Task research = new ResearchTask(null);
 		if(defend.canPerform(this))
 			taskList.add(defend);
 		
 		if(research.canPerform(this))
 			taskList.add(research);
 		
 		return taskList;
 	}
 	
 	public boolean didWin() {
 		for(Faction faction : Universe.factions)
 			if(faction instanceof GoauldFaction)
 				return faction.isDefeated();
 		return false;
 	}
 	
 	
 }

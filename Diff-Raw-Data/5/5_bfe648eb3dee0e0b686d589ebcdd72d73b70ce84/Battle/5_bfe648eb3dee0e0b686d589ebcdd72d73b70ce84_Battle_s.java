 package riskyspace.logic;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import riskyspace.model.Colony;
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 import riskyspace.model.Territory;
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.view.swingImpl.RenderArea;
 
 
 public class Battle {
 	
 	// initiatives is temporary solution for demo, this probably belongs elsewhere and
 	// should be possible to change (or change itself according to ships initiative in fleets)
 	private static final int MAX_INITIATIVE = 5;
 	
 	public static String doBattle(Territory territory) {
 		if (territory.getFleets().isEmpty()) {
 			throw new IllegalArgumentException("Battle can not occur in empty territories");
 		}
 		List<Fleet> player1fleets = new ArrayList<Fleet>();
 		List<Fleet> player2fleets = new ArrayList<Fleet>();
 		
 		player1fleets.add(territory.getFleets().get(0));
 		for (int i = 1; i < territory.getFleets().size(); i++) {
 			if (territory.getFleets().get(i).getOwner() != player1fleets.get(0).getOwner()) {
 				player2fleets.add(territory.getFleet(i));
 			} else {
 				player1fleets.add(territory.getFleet(i));
 			}
 		}
 		/*
 		 * Create a BattleGroups for each player with the fleets and colony if there is one
 		 * and it belongs to that player.
 		 */
 		Colony colony = territory.getColony();
 		BattleGroup bg1 = new BattleGroup(player1fleets, territory.hasColony() && colony.getOwner() == player1fleets.get(0).getOwner() ? colony : null);
 		BattleGroup bg2 = new BattleGroup(player2fleets, territory.hasColony() && colony.getOwner() != player1fleets.get(0).getOwner() ? colony : null);
 		if (bg1.isDefeated() || bg2.isDefeated()) {
 			throw new IllegalArgumentException("There need to be two players' fleets or Planet " +
 					"in the territory to battle");
 		}
 		bg1.setOwner(player1fleets.isEmpty() ? colony.getOwner() : player1fleets.get(0).getOwner());
 		bg2.setOwner(player2fleets.isEmpty() ? colony.getOwner() : player2fleets.get(0).getOwner());
 		/*
 		 * Battle loop until one or both fleets are defeated
 		 */
 		System.out.println("BATTLE AT " + territory);
 		while (!bg1.isDefeated() && !bg2.isDefeated()) {
 			List<Integer> planetAttacks1 = bg1.getPlanetAttacks();
 			List<Integer> planetAttacks2 = bg2.getPlanetAttacks();
 			List<Integer> targets1 = new ArrayList<Integer>();
 			List<Integer> targets2 = new ArrayList<Integer>();
 			for (int i = 0; i < planetAttacks1.size(); i++) {
 				targets1.add((int)(Math.random()*bg2.numberOfUnits()));
 			}
 			for (int i = 0; i < planetAttacks2.size(); i++) {
 				targets2.add((int)(Math.random()*bg1.numberOfUnits()));
 			}
 			bg2.takeDamage(planetAttacks1, targets1);
 			bg1.takeDamage(planetAttacks2, targets2);
 			
 			for (int i = MAX_INITIATIVE; i >= 0; i--) {
 				/*
 				 * Get attacks from each fleet for this initiative
 				 */
 				List<Integer> attacks1 = bg1.getAttacks(i);
 				List<Integer> attacks2 = bg2.getAttacks(i);
 
 				/*
 				 * Get targets for each attack for each fleet
 				 */
 				List<Integer> targetIndex1 = new ArrayList<Integer>();
 				List<Integer> targetIndex2 = new ArrayList<Integer>();
 				for (int j = 0; j < attacks1.size(); j++) {
 					targetIndex1.add((int) (Math.random()*(bg2.numberOfUnits())));
 				}
 				for (int j = 0; j < attacks2.size(); j++) {
 					targetIndex2.add((int) (Math.random()*(bg1.numberOfUnits())));
 				}
 				
 				/*
 				 * Both BattleGroups take damage
 				 */
 				bg2.takeDamage(attacks1, targetIndex1);
 				bg1.takeDamage(attacks2, targetIndex2);
 			}
 		}
 		
 		/*
 		 * Remove empty Fleets from territory
 		 */
 		List<Fleet> destroyedFleets = new ArrayList<Fleet>();
 		for (Fleet fleet : territory.getFleets()) {
 			if (fleet.fleetSize() == 0) {
 				destroyedFleets.add(fleet);
 			}
 		}
 		territory.removeFleets(destroyedFleets);
 		for (Fleet fleet : destroyedFleets) {
 			Event evt = new Event(Event.EventTag.FLEET_REMOVED, fleet);
 			EventBus.INSTANCE.publish(evt);
 		}
 		/*
 		 * Remove colony if the owner lost
 		 */
 		Player winner = !bg1.isDefeated() ? bg1.getOwner() : bg2.getOwner();
 		if (territory.hasColony()) {
 			if (territory.getColony().getOwner() != winner) {
 				territory.getPlanet().destroyColony();
 			}
 		}
 		if (territory.controlledBy() == Player.WORLD) {
 			return "Both fleets were destroyed!";
 		}
 		return winner + " won the battle!";
 	}
 
 	private static class BattleGroup {
 		private List<Fleet> fleets = null;
 		private Colony colony = null;
 		private Player owner = null;
 		
 		BattleGroup(List<Fleet> fleets, Colony colony) {
 			this.fleets = fleets;
 			this.colony = colony;
 		}
 
 		List<Integer> getAttacks(int initiative) {
 			List<Integer> attacks = new ArrayList<Integer>();
 			for (int fleetIndex = 0; fleetIndex < fleets.size(); fleetIndex++) {
 				for (int j = 0; j < fleets.get(fleetIndex).getAttacks(initiative).size(); j++) {
 					attacks.add(fleets.get(fleetIndex).getAttacks(initiative).get(j));
 				}
 			}
 			return attacks;
 		}
 		
 		List<Integer> getPlanetAttacks() {
 			if (colony != null) {
 				return colony.getAttacks();
 			} else {
 				return new ArrayList<Integer>();
 			}
 		}
 		
		void takeDamage(List<Integer> attacks, List<Integer> targetIndexes) {
 			/*
 			 * Each fleet will have a list of attacks and targetIndexes
 			 */
 			Map<Fleet, List<Integer>> fleetTargetIndexes = new HashMap<Fleet, List<Integer>>();
 			Map<Fleet, List<Integer>> fleetAttacks = new HashMap<Fleet, List<Integer>>();
 			
 			for (int i = 0; i < fleets.size(); i++) {
 				fleetTargetIndexes.put(fleets.get(i), new ArrayList<Integer>());
 				fleetAttacks.put(fleets.get(i), new ArrayList<Integer>());
 			}
 			
 			for (int i = 0; i < targetIndexes.size(); i++) {
 				if (colony != null && targetIndexes.get(i) == numberOfUnits()-1) {
 					if (colony.takeDamage(attacks.get(i))) {
 						colony = null;
 					}
 				} else {
 					int indexMod = 0;
 					for (int j = 0; j < fleets.size(); j++) {
 						/* BG: [0,1,2,3,4]  length == 5
 						 * F1: [0,1,2]		length == 3
 						 * F2: [0,1]		length == 2
 						 * [0,1,2] => [0,1,2]
 						 * [3,4]   => [0,1]
 						 */ 
 						 if (targetIndexes.get(i) < fleets.get(j).fleetSize() - indexMod) {
 							 /*
 							  * Split the data from the large list into the hit fleets
 							  * Lists.
 							  */
 							 fleetTargetIndexes.get(fleets.get(j)).add(targetIndexes.get(i));
 							 fleetAttacks.get(fleets.get(j)).add(attacks.get(i));
 							 /*
 							  * We found the hit fleet, now break for the next targetIndex.
 							  */
 							 break;
 						 } else {
 						 	indexMod += fleets.get(j).fleetSize();
 						 }
 					}
 				}
 			}
 			/*
 			 * Loop through all fleets and have them take damage for
 			 * every hit on that fleet.
 			 */
 			for (Fleet fleet : fleets) {
 				/*
 				 * Tell each fleet to take damage earlier set to that fleet.
 				 */
 				fleet.takeDamage(fleetAttacks.get(fleet), fleetTargetIndexes.get(fleet));
 			}
 		}
 		
		int numberOfUnits() {
 			int units = 0;
 			for (int i = 0; i < fleets.size(); i++) {
 				units += fleets.get(i).fleetSize();
 			}
 			if (colony != null) {
 				units++;
 			}
 			return units;
 		}
 		
 		public boolean isDefeated() {
 			return numberOfUnits() == 0;
 		}
 		
 		public void setOwner(Player player) {
 			owner = player;
 		}
 		
 		public Player getOwner() {
 			return owner;
 		}
 	}
 }

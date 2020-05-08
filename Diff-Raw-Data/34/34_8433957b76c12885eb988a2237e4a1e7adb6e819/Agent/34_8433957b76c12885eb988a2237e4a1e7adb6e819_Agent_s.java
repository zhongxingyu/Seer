 package risk.ai;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import risk.game.Country;
 import risk.game.Player;
 import risk.game.PlayerColor;
 
 public class Agent extends Player {
 
 	private ArrayList<Path> currentPaths;
 	private Path usedPath;
 
 	public Agent(PlayerColor c) {
 		super(c);
 		this.currentPaths = new ArrayList<Path>();
 	}
 
 	private void buildPathsForAllCountries() {
 		currentPaths.clear();
		for (Country c : this.controledCountries) {
 			if (c == null) {
 				continue;
 			}
 			if (c.hasEnemyNeighbor() && c.getNumberOfArmies() > 1) {
 				Path p = new Path(c);
 				Country t = p.getCurrentNode().returnNextNeighbor();
 				while (!(p.getCurrentPosition() == 0 && t == null)) {
 					while (t != null) {
 						if (p.armiesLeft()) {
 							if(!p.checkIfInPath(t)) {
 								p.addNode(new Node(t));
 							}
 							t = p.getCurrentNode().returnNextNeighbor();
 						} else {
 							t = null;
 						}
 					}
 					currentPaths.add(p.clone());
 					while (t == null) {
 						if (p.getCurrentPosition() == 0) {
 							break;
 						}
 						p.deleteNode();
 						t = p.getCurrentNode().returnNextNeighbor();
 					}
 				}
 			}
 		}
 //		for (Path p : currentPaths) {
 //			System.out.print("New path (Probability: " + p.getProbabilityOfSuccess() + "; Armies: " + p.getRoot().getCountry().getNumberOfArmies() + "): ");
 //			for (Node n : p.getNodes()) {
 //				System.out.print(n.getCountry().getName() + " -> ");
 //			}
 //			System.out.println();
 //		}
 	}
 
 	@Override
 	public void newArmies(int continentArmies) {
 		int receivedArmies = continentArmies + this.getReceivedArmies()
 				+ this.getRiskCardBonuesArmies();
 		int defenseArmies = receivedArmies / 2 + receivedArmies % 2;
 		int attackArmies = receivedArmies / 2;
		Collections.sort(this.controledCountries);
 		int i = 0;
 		while (defenseArmies > 0) {
			if (i >= this.controledCountries.size()) {
 				i = 0;
 			}
			Country c = this.controledCountries.get(i++);
 			if (c.isContinentBorder()) {
 				c.increasseNumberOfArmies(1);
 				defenseArmies--;
 			}
 		}
 		buildPathsForAllCountries();
 		if(currentPaths.size()>0)
 			usedPath = currentPaths.get(0);
 			{for (Path p: currentPaths) {
 				if (p.getProbabilityOfSuccess() > usedPath.getProbabilityOfSuccess()) {
 					usedPath = p;
 				}
 			}
 			System.out.print("New path (Probability: " + usedPath.getProbabilityOfSuccess() + "; Armies: " + usedPath.getRoot().getCountry().getNumberOfArmies() + "): ");
 			for (Node n : usedPath.getNodes()) {
 				System.out.print(n.getCountry().getName() + " -> ");
 			}
 			System.out.println();
 			usedPath.getRoot().getCountry().increasseNumberOfArmies(attackArmies);
 		
 		}
 	}
 
 	@Override
 	public void attack() {
 		Country currentAttacker = usedPath.getRoot().getCountry();
 		int i = 1;
 		while (currentAttacker.getNumberOfArmies() > 1 && i < usedPath.getNodes().size()) {
 			if (this.attack(currentAttacker, this.usedPath.getNodes().get(i).getCountry())) {
 				currentAttacker = this.usedPath.getNodes().get(i).getCountry();
 				i++;
 			}
 		}
 
 	}
 
 	@Override
 	public void repositionArmies() {
 		// TODO Auto-generated method stub
 
 	}
 	private int diceWert() {
 		return (int) (Math.random()* (7 - 1) + 1);
 	}
 	
 	private boolean attack(Country attacker, Country defender){
 		int abbruch= 0;
 		while(abbruch!= 1){
 			int attackerDice [] = null;
 			int defenderDice[] = null;
 			if(attacker.getNumberOfArmies() >3){
 				attackerDice = new int[3];
 			}
 			else{
 				attackerDice = new int[attacker.getNumberOfArmies()];
 			}
 			if(defender.getNumberOfArmies() >2){
 				defenderDice = new int[2];
 			}
 			else{
 				defenderDice = new int[defender.getNumberOfArmies()];
 			}		
 			for(int i = 0; i <attackerDice.length; i++)
 			{
 				attackerDice[i] = diceWert();
 			}
 			for(int i = 0; i <defenderDice.length; i++)
 			{
 				defenderDice[i] = diceWert();
 			}
 			//sortieren
 			Arrays.sort(defenderDice);
 			Arrays.sort(attackerDice);
 			if(defenderDice.length == 2)
 				sortTwo(defenderDice);
 			if(attackerDice.length == 2)
 				sortTwo(attackerDice);
 			if(attackerDice.length == 3)
 				sortThree(attackerDice);
 			
 			if(attackerDice[0]>
 			defenderDice[0])
 				defender.increasseNumberOfArmies(-1);
 			else if(attackerDice[0] <= defenderDice[0])
 				attacker.increasseNumberOfArmies(-1);
 			if(defenderDice.length == 2 && attackerDice.length > 1 && attacker.getNumberOfArmies() > 1 && defender.getNumberOfArmies() > 1)
 			{
 				if(attackerDice[1]> defenderDice[1])
 					defender.increasseNumberOfArmies(-1);
 				else if(attackerDice[1] <= defenderDice[1])
 					attacker.increasseNumberOfArmies(-1);
 			}
 			
 			if(attacker.getNumberOfArmies() <= 1 || defender.getNumberOfArmies() <= 0 )
 				abbruch = 1;
 		}
 		if(attacker.getNumberOfArmies() == 1) {
 			return false;
 		} else {
 			defender.setPlayer(attacker.getPlayer());
 			defender.setNumberOfArmies(attacker.getNumberOfArmies() - 1);
 			attacker.setNumberOfArmies(1);
 			defender.updateColor();
 			return true;
 		}
 		
 		
 	}
 	
 	
 	private void sortTwo(int array[]){
 		int temp = array[0];
 		array[0] = array[1];
 		array[1] = temp;
 	}
 	private void sortThree(int array[]){
 		int temp = array[0];
 		array[0] = array[2];
 		array[2] = temp;
 	}
 
 }

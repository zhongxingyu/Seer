 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import bonzai.api.AI;
 import bonzai.api.Duck;
 import bonzai.api.Entity;
 import bonzai.api.Farmhand;
 import bonzai.api.FarmhandAction;
 import bonzai.api.GameState;
 import bonzai.api.Position;
 import bonzai.api.Tile;
 import bonzai.api.Item;
 import bonzai.api.Item.Type;
 import bonzai.api.list.DuckList;
 
 
 public class CompetitorAI implements AI {
 
 	//Codes for role assignment
 	private final int R_DUCK_FETCH = 0;
 	private final int R_RECON = 1;
 	private final int R_GRIEF = 2;
 	private final int R_PATHS = 3;
 	int quoteCount = 15;
 	int lastQuote = 1;
 	int haveSpoken;
 	int talkers = 0;
 	private boolean boughtBucket = false;
 	private ArrayList<Duck> enemyDucks =  new ArrayList<Duck>();
 
 	private HashMap<Integer, Integer> roles = new HashMap<Integer, Integer>();
 	private ArrayList<Duck> ourDucks = new ArrayList<Duck>();
 	
 	Position homePosition = new Position(-1,-1);
 	
 	@Override
 	public Collection<FarmhandAction> turn(GameState state) {
 		quoteCount++;
 		haveSpoken = 0;
 
 		enemyDucks.clear();
 		//Set the home base position
 		if (homePosition.equals(new Position(-1, -1)))
 			homePosition = state.getMyBase().getPosition();
 		
 		ArrayList<FarmhandAction> actions = new ArrayList<FarmhandAction>();
 
 		//Number of workers to assign to each role
 		int totalWorkers = state.getMyFarmhands().size();
 		
 		int grief = totalWorkers/2;
 		int duckFetch = totalWorkers - grief;
 
 		//role assignment
 		for (int index = 0; index < totalWorkers; index++) {
 			if (duckFetch > 0) {
 				roles.put(index, R_DUCK_FETCH);
 				//System.out.println("Adding Fetcher " + duckFetch);
 				duckFetch--;
 			}
 			else if (grief > 0) {
 				//System.out.println("Adding grief " + grief);
 				roles.put(index, R_GRIEF);
 				grief--;
 			}
 		}
 
 		/* Print out what each farmhand is going to do for this turn
 		for (Integer i : roles.keySet()) {
 			System.out.println("Farmhand " + i + " is doing " + roles.get(i));
 		}
 		 */
 
 		//Clear out the HashMap
 		ourDucks.clear();
 		
 		int index = 0;
 		for (Farmhand farmhand : state.getMyFarmhands()) {
 			if (roles.get(index) == R_DUCK_FETCH)
 				actions.add(duckFetch(state, farmhand));
 			else if (roles.get(index) == R_GRIEF && !farmhand.isStumbled())
 				actions.add(grief(state, farmhand));
 			else
 				actions.add(noJob(state, farmhand));
 			index++;
 		}
 
 		return actions;
 	}
 
 	private FarmhandAction noJob(GameState state, Farmhand farmhand) {
 		return farmhand.shout(quote());
 	}
 
 	private FarmhandAction buildPaths(GameState state, Farmhand farmhand) {
 		return farmhand.shout(quote());
 	}
 
 
 	private FarmhandAction grief(GameState state, Farmhand farmhand) {
 		// Get the closest visible duck owned by other team
 		DuckList ducks = state.getDucks().getNotHeld();
 		ducks.removeAll(state.getMyDucks());
 		ducks.removeAll(enemyDucks);
 		Duck closestDuck = ducks.getClosestTo(farmhand);
 		if (closestDuck != null || !enemyDucks.isEmpty()) {
 			if(closestDuck == null){
 				closestDuck = enemyDucks.get(0);
 			}
 			else{
 				enemyDucks.add(closestDuck);
 			}
 			int dx = farmhand.getX() - closestDuck.getX();
 			int dy = farmhand.getY() - closestDuck.getY();
 
 			// If not adjacent to the duck
 			if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
 
 				int newX = farmhand.getX()
 						+ (int) Math.signum(closestDuck.getX()
 								- farmhand.getX());
 				int newY = farmhand.getY()
 						+ (int) Math.signum(closestDuck.getY()
 								- farmhand.getY());
 				// Move closer to the duck, if the tile is crossable
 				if (state.isTileEmpty(newX, newY)
 						&& state.getTile(newX, newY).canFarmhandCross()) {
 					return farmhand.move(newX, newY);
 				}
 			} 
 		}
 		return farmhand.shout(quote());
 	}
 
 	private FarmhandAction recon(GameState state, Farmhand farmhand) {
 		return farmhand.shout(quote());
 	}
 
 
 	private FarmhandAction duckFetch(GameState state, Farmhand farmhand) {
 		Entity item = farmhand.getHeldObject();
 		DuckList currentDucks = state.getMyDucks().getNotHeld();
 		Position farmhandPosition = farmhand.getPosition();
 		DuckList possibleDucks = state.getMyDucks().getNotHeld();
 		possibleDucks.removeAll(ourDucks);
 		Duck closest = possibleDucks.getClosestTo(farmhandPosition);
 		ourDucks.add(closest);
 		
 		//Find out if there is a duck in adjacent square
 		Duck adjacentDuck = null;
 		for (Position p : getAdjacent(state, farmhandPosition)) {
 			if (p.equals(closest.getPosition()))
 				adjacentDuck = closest;
 		}
 
 		//if we are holding a duck, we want to make progress back to the base
 		//otherwise we want to go get a duck
 		if (item instanceof Duck) {
 			//System.out.println("Holding duck");
 			if (farmhandPosition.equals(homePosition))
 				farmhand.dropItem(homePosition);
 			return farmhand.move(shortestPath(state, farmhandPosition, homePosition));
 		}
 		else if (adjacentDuck != null) {
 			//System.out.println("Picking up duck");
 			return farmhand.pickUp(adjacentDuck);
 		}
 		else {
 			//need to find the closest duck and go towards it
 			//System.out.println("Going twoards duck: " + closest.getPosition());
 			if (closest != null)
 				return farmhand.move(shortestPath(state, farmhandPosition, closest.getPosition()));
 			else
 				return farmhand.shout("No ducks nearby!");
 		}
 	}
 
 	private Position shortestPath(GameState state,
 			Position farmhandPosition, Position destination) {
 
 		//Get all of the adjacents, to the current position, whichever one
 		//of them is closest to the destination return that one
 
 		double distance = -1;
 		Position closest = null;
 		for (Position p : getAdjacent(state, farmhandPosition)) {
 			double currentDistance = distance(p, destination);
 			if (distance < 0 || currentDistance < distance) {
 				distance = currentDistance;
 				closest = p;
 			}
 		}
 		//System.out.println(closest);
 		return closest;
 	}
 
 	/* Works apparently */
 	private ArrayList<Position> getAdjacent(GameState state, Position toCheck) {
 		ArrayList<Position> adjacentPositions = new ArrayList<Position>();
 		ArrayList<Position> possible = new ArrayList<Position>();
 
 		//Check all 8 corresponding squares
 		possible.add(new Position(toCheck.getX() - 1, toCheck.getY() + 1));
 		possible.add(new Position(toCheck.getX(), toCheck.getY() + 1));
 		possible.add(new Position(toCheck.getX() + 1, toCheck.getY() + 1));
 		possible.add(new Position(toCheck.getX() + 1, toCheck.getY()));
 		possible.add(new Position(toCheck.getX() + 1, toCheck.getY() - 1));
 		possible.add(new Position(toCheck.getX(), toCheck.getY() - 1));
 		possible.add(new Position(toCheck.getX()-1, toCheck.getY() - 1));
 		possible.add(new Position(toCheck.getX() - 1, toCheck.getY()));
 
 		for (Position p : possible) {
 			if (state.getTile(p) != null && validTile(state.getTile(p))) {
 				adjacentPositions.add(p);
 			}
 		}
 		/*
 		System.out.println(toCheck);
 		System.out.println(adjacentPositions);
 		 */
 		return adjacentPositions;
 	}
 
 	private boolean validTile(Tile t) {
 		return t.canFarmhandCross();
 	}
 
 	private double distance(Position p1, Position p2) {
 		return Math.sqrt(
 				Math.pow((p1.getX() - p2.getX()), 2) + 
 				Math.pow((p1.getY() - p2.getY()), 2)
 				);
 	}
 
 	private String quote(){
 		if(talkers < 0 && haveSpoken == 0){
 			talkers = talkers*-1;
 		}
 		
 		if(talkers < 1)
 			talkers--;
 		
 		
 		if(quoteCount >= 10){
 			quoteCount = 0;
 			int minimum = 1;
 			int maximum = 12;
 			lastQuote = minimum + (int)(Math.random()*maximum); 
 		}
 		System.out.println(quoteCount +"   "+ lastQuote);
 		
 		String quote;
 		switch(lastQuote){
 			case 1:
 				quote =  "There's a snake in my boot!";
 				break;
 			case 2:
 				quote =  "Your Mom goes to college";
 				break;
 			case 3:
 				quote =  "Loud Noises!";
 				break;
 			case 4:
 				quote =  "I love lamp";
 				break;
 			case 5:
 				quote =  "I'm going East";
 				break;
 			case 6:
 				quote =  "You're killin me smalls";
 				break;
 			case 7:
 				quote =  "Inconceivable!";
 				break;
 			case 8:
 				quote =  "I CAN HAZ DUCK";
 				break;
 			case 9:
 				quote =  "The duck that will pierce the heavens.";
 				break;
 			case 10:
 				quote =  "Tree fiddy";
 				break;
 			case 11:
 				quote =  "Nope!";
 				break;
 			case 12:
 				quote =  "I'm griefing";
 				break;
 			default:
 				quote =  "Sup, nigga";
 		}
 		
 		if(quote.length() > 20*haveSpoken){
 			haveSpoken++;
			return quote.substring(20*haveSpoken);
 		}
 		else{
 			haveSpoken++;
 			return quote;
 		}
 
 	}
 }

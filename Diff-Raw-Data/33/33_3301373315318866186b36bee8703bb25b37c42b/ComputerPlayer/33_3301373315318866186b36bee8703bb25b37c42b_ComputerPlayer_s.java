 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
 public class ComputerPlayer extends Player
 {
 
 	public ComputerPlayer(int number)
 	{
 		super(number);
 		// TODO Auto-generated constructor stub
 	}
 
 	public ComputerPlayer(int number, Color color)
 	{
 		super(number, color);
 		// TODO Auto-generated constructor stub
 	}
 
 
 
 	public void update(Universe u)
 	{
 		System.out.println("Updating player " + this.getNumber());
 		this.makeMoves(u);
 	}
 
 	/**
 	 * @param u
 	 * 
 	 * Find the nearest neutral/enemy planet for every planet the ComputerPlayer owns.
 	 * If the planet is deemed a valid target, and the ComputerPlayer isn't already
 	 * attacking it, send a Fleet of up to 90% of ships available on the current planet.
 	 */
 	private void makeMoves(Universe u)
 	{
 		Planet[] ownedPlanets = u.getPlayerPlanets(this);
 		// For each planet the ComputerPlayer owns:
 		for (Planet launchPlanet : ownedPlanets)
 		{
 			Planet[] planetsArr = u.getPlanets();
 			Planet closestTarget = planetsArr[0];
 			// find closest planet which is a valid target
 			for (int i=1; i < planetsArr.length; i++)
 			{
 				Planet possibleTarget = planetsArr[i];
 				if (this.validTarget(launchPlanet, possibleTarget) && (launchPlanet.distanceFrom(possibleTarget) < launchPlanet.distanceFrom(closestTarget)))
 				{
 					closestTarget = possibleTarget;
 				}
 			}
 			// Is this a good attack from this planet?
 			int numberOfAttackingShips = this.shipsToAttack(launchPlanet, closestTarget);
 			if (numberOfAttackingShips == 0) continue;            
 
 			this.initiateMovement(launchPlanet, closestTarget, numberOfAttackingShips);
 		}
 	}
 
 	private boolean validTarget(Planet launchPlanet, Planet targetPlanet)
 	{
 		if (targetPlanet.belongsTo(this)) {
 			return false;
 		}
 
 		if (!launchPlanet.canReach(targetPlanet)) {
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * @param launchPlanet
 	 * @param targetPlanet
 	 * @return the number of ships you should send from the launchPlanet to
 	 * attack the targetPlanet. 0 if you shouldn't attack.
 	 */
 	private int shipsToAttack(Planet launchPlanet, Planet targetPlanet)
 	{
 		Map<Player,Integer> targetShips = targetPlanet.getShips();
 		int targetEnemyShips = 0;
 		for (Player player : targetShips.keySet()) {
 			if (player != this) targetEnemyShips += targetShips.get(player);
 		}
 
 		int shipsAvailable = launchPlanet.getPlayerShips(this);
 		int attackMargin = 5;   // we want to attack with this many more ships than the enemy
 		int idealAttackStrength = (targetEnemyShips + attackMargin);
 
 		if (targetEnemyShips == 0 && shipsAvailable > 6) {	// if there's no enemy ships on the planet, attack with all ships, bar 2
 			return (shipsAvailable - 2);
 		}
 
 		if (shipsAvailable < idealAttackStrength) {
 			return 0;
 		}
 		return idealAttackStrength;
 	}
 
 }

 package vooga.towerdefense.action;
 
 import vooga.towerdefense.attributes.Targetable;
 import vooga.towerdefense.attributes.Upgradable;
 import vooga.towerdefense.gameElements.GameElement;
 import vooga.towerdefense.gameElements.Projectile;
 import vooga.towerdefense.gameElements.Unit;
 import vooga.towerdefense.util.Location;
 
 /**
  * this should be used for fetching information about the game from the gameMap. 
  * @author gouzhen-1
  *
  */
public interface InfoBridge {
 	
 	public Unit[] getUnits();
 	public GameElement[] getGameElements();
 	public Targetable[] getTargetables();
 	public Upgradable[] getUpgradables();
 	
 	/**
 	 * return a list containing the desired number of targetables 
 	 * within the given radius of the source location. PS: always return the closest ones
 	 * @param source
 	 * @param radius
 	 * @param howMany
 	 * @return
 	 */
 	public Targetable[] getTargetsWithinRadiusOfGivenLocation(Location source,double radius, int howMany);
 	public void addGameElement(Projectile projectile);
 	
 
 }

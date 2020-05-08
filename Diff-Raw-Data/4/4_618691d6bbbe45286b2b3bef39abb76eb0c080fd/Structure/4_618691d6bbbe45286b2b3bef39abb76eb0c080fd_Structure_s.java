 package projectrts.model.core.entities;
 
 
 
 import projectrts.model.core.Player;
 import projectrts.model.core.Position;
 import projectrts.model.core.abilities.BuildTowerAbility;
 import projectrts.model.core.abilities.IAbility;
 
 
 
 /**
  * A simple Structure
  * @author Filip Brynfors
  *
  */
 public class Structure extends PlayerControlledEntity {
	private BuildTowerAbility buildTowerAbility;
 	
 	/**
 	 * Spawns a Structure at the provided position.
 	 * @param spawnPos Spawn position
 	 */
 	public Structure(Position spawnPos, Player owner){
 		super(spawnPos, owner, 500);
		abilities.add((buildTowerAbility = new BuildTowerAbility()));
 	}
 	
 	@Override
 	public float getSize() {
 		// TODO Change this later
 		return 1;
 	}
 
 	@Override
 	public void update(float tpf){
 		//TODO: Add a micro AI for attacking the structure has offensive abilities
 	}
 
 	@Override
 	public String getName() {
 		// TODO Change name
 		return "Basic Structure";
 	}
 
 	@Override
 	public float getSightRange() {
 		// TODO Change this later
 		return 10;
 	}
 	
 }

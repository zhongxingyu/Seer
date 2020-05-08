 package projectrts.model.entities.abilities;
 
 import projectrts.model.entities.AbstractAbility;
 import projectrts.model.entities.IBuildStructureAbility;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.entities.structures.Wall;
 
 //TODO Jakob: Make abstract constructBuilding class
 /**
  * An ability for building Barracks
  * @author Jakob Svensson
  *
  */
 public class BuildWallAbility extends AbstractConstructAbility implements IMovableAbility, IBuildStructureAbility{
 	private static float buildTime = 1; 
 	private static int buildCost = 50; 
 	private float size = 1; //TODO: Sync with Barracks class
 
 	static {
 		AbilityFactory.INSTANCE.registerAbility(BuildWallAbility.class.getSimpleName(), new BuildWallAbility());
 	}
 
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		super.initialize(entity, moveAbility);
 		this.setBuildCost(buildCost);
 		this.setBuildTime(buildTime);
 		this.setEntityToTrain(Wall.class.getSimpleName());
		this.setSizeOfBuilding(size);
 	}
 
 	@Override
 	public String getName() {
 		return BuildWallAbility.class.getSimpleName();
 	}
 
 	@Override
 	public AbstractAbility createAbility(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		BuildWallAbility newAbility = new BuildWallAbility();
 		newAbility.initialize(entity, moveAbility);
 		return newAbility;
 	}
 
	
 }

 package projectrts.model.abilities;
 
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.Player;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.world.Position;
 import projectrts.model.world.World;
 
 /**
  * An ability for building Barracks
  * @author Jakob Svensson
  *
  */
 public class AbstractConstructAbility extends AbstractAbility implements IUsingMoveAbility, IBuildStructureAbility {
 	private PlayerControlledEntity entity;
 	private float buildTime; 
 	private int buildCost; 
 	private static float cooldown = 0.5f;
 	private Position buildPos;
 	private float buildTimeLeft;
 	private AbstractAbility moveAbility;
 	private float size;
 	private String entityToTrain;
 
 	static {
 		AbilityFactory.INSTANCE.registerAbility(AbstractConstructAbility.class.getSimpleName(), new AbstractConstructAbility());
 	}
 
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		this.entity = entity;
 		this.setCooldown(cooldown);
 		this.moveAbility = moveAbility;
 	}
 
 	@Override
 	public String getName() {
 		return AbstractConstructAbility.class.getSimpleName();
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			if(Position.getDistance(entity.getPosition(),buildPos)<size*1.5){
 				//If in range of buildingPosition
 				moveAbility.setFinished(true);
 				if(buildTimeLeft<=0){
 					EntityManager.getInstance().addNewPCE(entityToTrain, (Player)entity.getOwner(),buildPos);
 					setFinished(true);
 					buildTimeLeft =buildTime;
 				}else{
 					buildTimeLeft-=tpf;
 				}
 			}else{
 				// Not in range
 				
 				if(!moveAbility.isActive()){
 					moveAbility.useAbility(buildPos);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void useAbility(Position target) {
 		Player owner = (Player)entity.getOwner();
 		if(owner.getResources()>=buildCost){//TODO Jakob: Notify view somehow when not enough resources
 			owner.modifyResource(-buildCost); 
 			buildPos = target;
 			setActive(true);
 			setFinished(false);
 			buildTimeLeft=buildTime;
			World.getInstance().setNodesOccupied(World.getInstance().getNodeAt(target)//TODO Jakob: Set unoccupied if ability is aborted
 					, getSizeOfBuilding(), EntityManager.getInstance().requestNewEntityID());
 		}
 	}
 
 	@Override
 	public AbstractAbility createAbility(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		AbstractConstructAbility newAbility = new AbstractConstructAbility();
 		newAbility.initialize(entity, moveAbility);
 		return newAbility;
 	}
 
 	@Override
 	public float getSizeOfBuilding() {
 		return size;
 	}
 	
 	protected void setSizeOfBuilding(float size){
 		this.size=size;
 	}
 	
 	protected void setBuildTime(float buildTime) {
 		this.buildTime = buildTime;
 	}
 
 	protected void setBuildCost(int buildCost) {
 		this.buildCost = buildCost;
 	}
 	
 	protected void setEntityToTrain(String name){
 		this.entityToTrain=name;
 	}
 	
 	@Override
 	public void abortAbility(){
 		super.abortAbility();
 		World.getInstance().setNodesOccupied(World.getInstance().getNodeAt(buildPos)
 				, getSizeOfBuilding(), 0);
 		
 	}
 	
 
 }

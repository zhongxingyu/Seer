 package projectrts.model.abilities;
 
 import projectrts.model.entities.AbstractEntity;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.entities.Resource;
 import projectrts.model.world.Position;
 /**
  * An ability for moving to a resource and mine from it
  * @author Jakob Svensson
  *
  */
 public class MineResourceAbility extends AbstractAbility implements IUsingMoveAbility, IGatherAbility {
 	private Resource targetResource;
 	private PlayerControlledEntity entity;
 	private AbstractAbility moveAbility;
	private int resourceCarriedAmount = RESOURCE_CARRIED_AMOUNT;
	private final float recoveryTime = 0.3f;
 	private float miningCooldown = 0;
 	private int range = 1;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(MineResourceAbility.class.getSimpleName(), new MineResourceAbility());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		this.entity = entity;
 		this.moveAbility = moveAbility;
 	}
 	
 	@Override
 	public String getName() {
 		return "FetchResource";
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			
 			if(inRange(targetResource)){
 				//If in range of resource
 				//Check cooldown and mine resource or reduce cooldown as appropriate.
 				moveAbility.setFinished(true);
 				if (miningCooldown <= 0) { 
 					resourceCarriedAmount += targetResource.mine();
 					miningCooldown = recoveryTime;
 				} else {
 					miningCooldown -= tpf; 
 				}
 				
				if(resourceCarriedAmount >= resourceCarriedAmount) {
 					setFinished(true);
 				}
 				
 			}else{
 				// Not in range
 				if(!moveAbility.isActive()){
 					moveAbility.useAbility(targetResource.getPosition());
 				}
 			}
 		}
 		
 	}
 
 	@Override
 	public void useAbility(Position target) {
 		this.targetResource = (Resource) EntityManager.getInstance().getNPCEAtPosition(target);
 		resourceCarriedAmount = 0;
 		setActive(true);
 		setFinished(false);
 	}
 	
 	@Override
 	public AbstractAbility createAbility(PlayerControlledEntity entity, MoveAbility moveAbility) {
 		MineResourceAbility newAbility = new MineResourceAbility();
 		newAbility.initialize(entity, moveAbility);
 		return newAbility;
 	}
 
 	private boolean inRange(AbstractEntity target)
 	{
 		return (Position.getDistance(entity.getPosition(), target.getPosition()) < range + (target.getSize()/2)*1.5);
 	}
 	
 }

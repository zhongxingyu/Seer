 package projectrts.model.entities.abilities;
 
 import projectrts.model.entities.AbstractAbility;
 import projectrts.model.entities.EntityManager;
 import projectrts.model.entities.PlayerControlledEntity;
 import projectrts.model.entities.units.Warrior;
 import projectrts.model.player.Player;
 import projectrts.model.utils.Position;
 //TODO Jakob: Maybe extract common code with trainWorkerAbility to a abstract class
 /**
  * A class that trains a Warrior
  * @author Jakob Svensson
  *
  */
 public class TrainWarriorAbility extends AbstractAbility{
 	private PlayerControlledEntity structure;
 	private static float buildTime = 7; 
 	private static int buildCost = 100; 
 	private static float cooldown = 0.5f;
 	private Position spawnPos;
 	private float buildTimeLeft;
 	
 	static {
 		AbilityFactory.INSTANCE.registerAbility(TrainWarriorAbility.class.getSimpleName(), new TrainWarriorAbility());
 	}
 	
 	/**
 	 * When subclassing, invoke this to initialize the ability.
 	 */
 	protected void initialize() {
 		this.setCooldown(cooldown);
 	}
 	
 	@Override
 	public String getName() {
 		return TrainWarriorAbility.class.getSimpleName();
 	}
 
 	@Override
 	public void update(float tpf) {
 		if(isActive() && !isFinished()){
 			if(buildTimeLeft<=0){
 				EntityManager.getInstance().addNewPCE(Warrior.class.getSimpleName(), (Player)structure.getOwner(),spawnPos);
 				setFinished(true);
 				buildTimeLeft =buildTime;
 			}else{
 				buildTimeLeft-=tpf;
 			}
 			System.out.println(buildTimeLeft);
 		}
 	}
 
 	@Override
 	public void useAbility(PlayerControlledEntity caster, Position target) {
 		structure = caster;
 		Player owner = (Player)structure.getOwner();
 		if(owner.getResources()>=buildCost){//TODO Jakob: Notify view somehow when not enough resources
 			owner.modifyResource(-buildCost); 
			spawnPos = new Position(structure.getPosition().getX()+structure.getSize(),
					structure.getPosition().getY()+structure.getSize()); //TODO Plankton: Decide spawnPos
 			setActive(true);
 			setFinished(false);
 			buildTimeLeft=buildTime;
 		}
 	}
 
 	@Override
 	public AbstractAbility createAbility() {
 		TrainWarriorAbility newAbility = new TrainWarriorAbility();
 		newAbility.initialize();
 		return newAbility;
 	}
 
 }

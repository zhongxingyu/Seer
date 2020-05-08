 package projectrts.model.core.abilities;
 
 import projectrts.model.core.Position;
 import projectrts.model.core.entities.PlayerControlledEntity;
 import projectrts.model.core.utils.ModelUtils;
 
 /**
  * An ability for attacking
  * @author Filip Brynfors
  *
  */
 public class AttackAbility extends AbstractAbility {
 	private PlayerControlledEntity attacker;
 	private PlayerControlledEntity target;
 	
 	private MoveAbility moveAbility = new MoveAbility();
 	
 	
 	public AttackAbility(){
 		super(1);
 	}
 	
 	@Override
 	public String getName() {
 		return "Attack";
 	}
 	
 	@Override
 	public void useAbility(PlayerControlledEntity attacker, Position pos){
 		this.attacker = attacker;
 		target = ModelUtils.INSTANCE.getPlayerControlledEntityAtPosition(pos);
 		
 		//TODO: Are these needed?
		setActive(true);
 		setFinished(false);
 		
 	}
 
 	@Override
 	public void update(float tpf) {
 		updateCooldown(tpf);		
 		
 		
 		if(isActive() && !isFinished()){
 			
 			//attacker.getRange();
 			if(ModelUtils.INSTANCE.getDistance(attacker.getPosition(), target.getPosition())>1){
 				//Out of range
 				
 				if(!moveAbility.isActive()){
 					moveAbility.useAbility(attacker, target.getPosition());
 				}
 				
 				moveAbility.update(tpf);
 				if(moveAbility.isFinished()){
 					moveAbility.setActive(false);
 					moveAbility.setFinished(false);
 				}
 				
 			} else {
 				//In range
 				if(getRemainingCooldown()<=0){
 					//TODO: The amount of dmg should be attacker.getDamage()
 				
 					target.takeDamage(50);
 					
 					this.setAbilityUsed();
 					
 					//TODO: Not setting finnished = true?
 					
 				}
 			}
 		}
 	}
 
 }

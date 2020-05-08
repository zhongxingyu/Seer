 package entitySystems;
 
 import utils.Circle;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 
 import components.AttackComponent;
 import components.HealthComponent;
 import components.PositionComponent;
 import components.SpatialComponent;
 
 import entityFramework.ComponentMapper;
 import entityFramework.EntityProcessingSystem;
 import entityFramework.IComponent;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 
 /**
  * 
  * @author Gustav
 * 
  */
 public class AttackResolveSystem extends EntityProcessingSystem {
 
 	protected AttackResolveSystem(IEntityWorld world,
 			Class<? extends IComponent>[] componentsClasses) {
		super(world, componentsClasses);
 	}
 
 	private ComponentMapper<AttackComponent> aCM;
 	private ComponentMapper<SpatialComponent> sCM;
 	private ComponentMapper<PositionComponent> pCM;
 	private ComponentMapper<HealthComponent> hCM;
 
 	@Override
 	public void initialize() {
 		aCM = ComponentMapper.create(this.getWorld().getDatabase(),
 				AttackComponent.class);
 		pCM = ComponentMapper.create(this.getWorld().getDatabase(),
 				PositionComponent.class);
 		sCM = ComponentMapper.create(this.getWorld().getDatabase(),
 				SpatialComponent.class);
 		hCM = ComponentMapper.create(this.getWorld().getDatabase(),
 				HealthComponent.class);
 	}
 
 	@Override
 	protected void processEntitys(ImmutableSet<IEntity> entities) {
 		for (IEntity entity : entities) {
 			AttackComponent attaCom = aCM.getComponent(entity);
 			PositionComponent posiCom = pCM.getComponent(entity);
 
 			ImmutableList<IEntity> gEntities = ImmutableList.of();
 			for (int i = 0; i < attaCom.getTargetGroups().size(); i++) {
 				gEntities.addAll(this.getWorld().getEntityManager()
 						.getEntityGroup(attaCom.getTargetGroups().get(i)));
 			}
 
 			for (IEntity targetEntity : gEntities) {
 				SpatialComponent targetSpatiCom = sCM
 						.getComponent(targetEntity);
 				PositionComponent targetPosiCom = pCM
 						.getComponent(targetEntity);
 
 				Boolean itGotHit = Circle.intersects(attaCom.getBounds(),
 						posiCom.getPosition(), targetSpatiCom.getBounds(),
 						targetPosiCom.getPosition());
 
 				if (itGotHit) {
 					HealthComponent healCom = hCM.getComponent(targetEntity);
 					healCom.addHealthPoints(-attaCom.getDamage());
 					System.out.println("Someone got hit for "
 							+ attaCom.getDamage() + " damage!");
 				}
 			}
 
 		}
 
 	}
 }

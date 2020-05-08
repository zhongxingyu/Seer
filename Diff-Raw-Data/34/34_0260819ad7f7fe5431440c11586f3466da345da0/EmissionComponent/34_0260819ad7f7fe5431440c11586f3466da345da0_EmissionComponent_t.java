 package atl.space.components.emission;
 
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import atl.space.components.Component;
 import atl.space.components.Triggerable;
 import atl.space.entities.Entity;
 
 public abstract class EmissionComponent extends Component implements Triggerable {
 	public EmissionComponent(String id){
 		super(id);
 	}
 	
 	public EmissionComponent(EmissionComponent ec){
		super(ec.getId());
 	}
 	
 	@Override
 	public void trigger(List<Entity> entities) {
 		if (canEmit()) { 
 			Entity temp = buildEmission();
 			temp.position = new Vector3f(owner.position);
 			applyEffect(temp);
 			entities.add(temp);
 		}
 	}
 	
 	protected abstract Entity buildEmission();
 	protected abstract boolean canEmit();
 	protected abstract void applyEffect(Entity temp);
 
 	@Override
 	public void update(int delta, List<Entity> entities) {
 		//do nothing
 	}
 }

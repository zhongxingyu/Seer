 package atl.space.components.emission;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import atl.space.components.Component;
 import atl.space.entities.Entity;
 
 public class MissileLauncherTestComponent extends FTLauncherComponent {
 	
	public MissileLauncherTestComponent(Vector3f dir,
 			float speed) {
		super(dir, speed);
 	}
 
 	@Override
 	protected Entity buildEmission() {
 		//TODO this
 		return null;
 	}
 
 	@Override
 	protected boolean canEmit() {
 		return true;
 	}
 
 	@Override
 	public Component clone() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }

 package atl.space.components.emission;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import atl.space.components.turn.TurningComponent;
 import atl.space.entities.Entity;
 
 public abstract class FacingLauncherComponent extends LauncherComponent {
 	//works with things that have TurningComponents
 	public FacingLauncherComponent(FacingLauncherComponent flc){
 		super(flc);
 	}
 	public FacingLauncherComponent(Vector3f expspeed){
 		super(expspeed);
 	}
	public FacingLauncherComponent(Entity emission, Vector3f expdir, float speed){
 		super();
 		expulsionSpeed = new Vector3f(expdir);
 		Entity.restrictLength(expulsionSpeed, speed);
 	}
 	
 	public List<String> getPrerequisiteIDs(){
     	ArrayList<String> prids = new ArrayList<String>(1);
 		prids.add("turning");
     	return prids;
     }
 	
 	/*public Component clone(){
 		return new FacingLauncherComponent(this);
 	}*/
 	
 	public void update(int delta, List<Entity> entities) {
 		TurningComponent tc = (TurningComponent)owner.getComponent("turning");
 		float lengthy = expulsionSpeed.length();
 		expulsionSpeed.normalise();
 		Vector3f.add(expulsionSpeed, tc.turn, expulsionSpeed);
 		Entity.restrictLength(expulsionSpeed, lengthy);
 		//Makes the launcher point a new way but at the same speed
 	}
 
 }

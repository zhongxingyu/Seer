 package atl.space.components.emission;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import atl.space.components.turn.TurnControlComponent;
 import atl.space.entities.Entity;
 
 public abstract class FTLauncherComponent extends FacingLauncherComponent {
 	
 	public Vector3f target;
 	//Sets the emission to track the target.
 	// Needs the emission to have a TurnControlComponent
	public FTLauncherComponent(Entity emission, Vector3f dir, float speed) {
		super(emission, dir, speed);
 		// id = "emission";
 		// System.out.println("set up FTLauncherComponent");
 	}
 	
 	public FTLauncherComponent(FTLauncherComponent ftlc){
 		super(ftlc);
 	}
 	
 	public List<String> getPrerequisiteIDs(){
     	ArrayList<String> prids = new ArrayList<String>(2);
 		prids.add("turning");
 		prids.add("movement");
     	return prids;
     }
 	
 	/*public Component clone(){
 		return new FTLauncherComponent(this);
 	}*/
 	
 	public void setTarget(Vector3f target) {
 		this.target = target;
 	}
 
 	
 	@Override
 	protected void applyEffect(Entity temp) {
 		super.applyEffect(temp);
 		if (temp.hasComponent("turncontrol")) {
 			// System.out.println("Setting target");
 			TurnControlComponent tcc = (TurnControlComponent) temp
 					.getComponent("turncontrol");
 			tcc.setTarget(target);
 			tcc.initiateTurn();
 		} else {
 			if(DEBUG) System.err.println("DEBUG: No turn control in emission");
 		}
 	}
 }

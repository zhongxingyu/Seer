 package myGame.motors;
 
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.AbstractControl;
 import com.jme3.scene.control.Control;
 
 public class MotorControl extends AbstractControl {
 
 	private RigidBodyControl	rbControl;
 
 	public Motor				motors[]	= new Motor[] {
 			new Motor(),
 			new Motor(),
 			new Motor(),
 			new Motor()					};
 
 	public MotorControl(RigidBodyControl quad_phy) {
 		rbControl = quad_phy;
 	}
 
 	@Override
 	public Control cloneForSpatial(Spatial spatial) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void controlRender(RenderManager rm, ViewPort vp) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	protected void controlUpdate(float tpf) {
 		float force = 0;
 		for (int i = 0; i < 4; i++) {
 			motors[i].update();
 			force = motors[i].getCurrentForce();
			Vector2f angle = new Vector2f(FastMath.cos(i * FastMath.PI),
					FastMath.sin(i * FastMath.PI));
 			Vector3f tAngle = new Vector3f(angle.x, 0, angle.y).mult(13);
 			/** apply lift thrust */
 			rbControl.applyImpulse(
 					getSpatial().getLocalRotation().mult(
 							Vector3f.UNIT_Y.mult(force)),
 					getSpatial().getLocalRotation().mult(
 							tAngle));
 		}
 	}
 
 }

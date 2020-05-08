 package net.juniorbl.jtoyracing.core.camera;
 
 import com.jme.input.ChaseCamera;
import com.jme.input.thirdperson.ThirdPersonMouseLook;
 import com.jme.math.FastMath;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.scene.Spatial;
 
import java.util.HashMap;

 /**
  * Camera that follows a vehicle.
  *
  * @version 1.0 Jun 22, 2008
  * @author Carlos Luz Junior
  */
 public final class VehicleChaseCamera extends ChaseCamera {
 
 	/**
 	 * Far distance from the car.
 	 */
 	private static final int FAR_DISTANCE = 50;
 
 	/**
 	 * Close distance from the car.
 	 */
 	private static final int CLOSE_DISTANCE = 30;
 
 	/**
 	 * Vehicle chase camera.
 	 */
 	private static ChaseCamera vehicleChaseCamera;
 
 	/**
 	 * Constructs a vehicle camera.
 	 *
 	 * @param camera the original camera.
 	 * @param vehicleToFollow a vehicle to follow.
 	 * @param properties the camera properties.
 	 */
 	private VehicleChaseCamera(Camera camera, Spatial vehicleToFollow, HashMap properties) {
 		super(camera, vehicleToFollow, properties);
 	}
 
 	/**
 	 * Return a instance of vehicle camera.
 	 *
 	 * @param camera an original camera.
 	 * @param vehicleToFollow a vehicle to follow.
 	 * @return instance of vehicle camera.
 	 */
 	public static ChaseCamera getInstance(Camera camera, Spatial vehicleToFollow) {
 		HashMap properties = loadProperties();
 		vehicleChaseCamera = new VehicleChaseCamera(camera, vehicleToFollow, properties);
 		vehicleChaseCamera.setMaxDistance(FAR_DISTANCE);
 		vehicleChaseCamera.setMinDistance(FAR_DISTANCE);
 		vehicleChaseCamera.setStayBehindTarget(true);
 		return vehicleChaseCamera;
 	}
 
 	/**
 	 * Loads the initial properties for the camera.
 	 *
 	 * @return the properties loaded.
 	 */
 	private static HashMap<String, Object> loadProperties() {
 		Vector3f targetOffset = new Vector3f();
 		targetOffset.y = 4;
 		HashMap<String, Object> properties = new HashMap<String, Object>();
 		properties.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
 		properties.put(ChaseCamera.PROP_INITIALSPHERECOORDS, new Vector3f(15, 0, 30 * FastMath.DEG_TO_RAD));
 		properties.put(ChaseCamera.PROP_TARGETOFFSET, targetOffset);
 		return properties;
 	}
 
 	/**
 	 * Modify the player's vision by changing the distance of the camera from the car.
 	 */
 	public void changeVision() {
 		if (vehicleChaseCamera != null) {
 			if (vehicleChaseCamera.getMaxDistance() == FAR_DISTANCE) {
 				vehicleChaseCamera.setMaxDistance(CLOSE_DISTANCE);
 				vehicleChaseCamera.setMinDistance(CLOSE_DISTANCE);
 			} else {
 				vehicleChaseCamera.setMaxDistance(FAR_DISTANCE);
 				vehicleChaseCamera.setMinDistance(FAR_DISTANCE);
 			}
 		}
 	}
 }

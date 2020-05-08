 package util;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import util.math.MathUtils;
 import util.math.Quaternion;
 
 public class Camera {
 	private Vector3f centerPosition = new Vector3f(0f, 0f, -1f);  // world space coordinates.
 	private Vector3f eyePosition = new Vector3f(0f, 0f, 0f);      // world space coordinates.
 
 	// Orientation of camera axes.
 	private Quaternion orientation = new Quaternion(0f, 0f, 0f, 1f); // Identity.
 	
 	// Standard reference axes.
 	public static final Vector3f X_AXIS = new Vector3f(1f, 0f, 0f);
 	public static final Vector3f Y_AXIS = new Vector3f(0f, 1f, 0f);
 	public static final Vector3f Z_AXIS = new Vector3f(0f, 0f, 1f);
 	
     private Vector3f l = new Vector3f(-1f, 0f, 0f);  // left local camera vector.
     private Vector3f f = new Vector3f(0f, 0f, -1f);  // forward local camera vector.
     private Vector3f u = new Vector3f(0f, 1f, 0f);   // up local camera vector.
 
 	// --------------------------------------------------------------------------
 	public void setPosition(float x, float y, float z) {
 		eyePosition.x = x;
 		eyePosition.y = y;
 		eyePosition.z = z;
 	}
 
 	// --------------------------------------------------------------------------
 	public void setPosition(Vector3f position) {
 		setPosition(position.x, position.y, position.z);
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Translates the Camera using world space axes.
 	 * 
 	 * @param x - movement magnitude along world space x-axis.
 	 * @param y - movement magnitude along world space y-axis.
 	 * @param z - movement magnitude along world space z-axis.
 	 */
 	public void translate(float x, float y, float z) {
 		eyePosition.x += x;
 		eyePosition.y += y;
 		eyePosition.z += z;
 	}
 
 	/**
 	 * Translates the Camera using world space axes.
 	 * 
 	 * @param vec - Vector with movement magnitudes along world space x, y, and
 	 *            z axes.
 	 */
 	// --------------------------------------------------------------------------
 	public void translate(Vector3f vec) {
 		translate(vec.x, vec.y, vec.z);
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Translates the camera relative to itself.
 	 * 
 	 * @param left - translation distance along camera's local left-direction.
 	 * @param up - translation distance along camera's local up-direction.
 	 * @param forward - translation distance along camera's local forward-direction.
 	 */
 	public void translateRelative(float left, float up, float forward) {
 		eyePosition.x += (left * l.x) + (up * u.x) + (forward * f.x);
 		eyePosition.y += (left * l.y) + (up * u.y) + (forward * f.y);
 		eyePosition.z += (left * l.z) + (up * u.z) + (forward * f.z);
 	}
 
 	// --------------------------------------------------------------------------
 	public void translateRelative(Vector3f vec) {
 		translateRelative(vec.x, vec.y, vec.z);
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * @return a new {@link Vector3f} representing the current eye position of
 	 *         the Camera.
 	 */
 	public Vector3f getPosition() {
 		return new Vector3f(eyePosition.x, eyePosition.y, eyePosition.z);
 	}
 	
 	// --------------------------------------------------------------------------
 	/**
 	 * Places the camera world position coordinates into dest.
 	 */
 	public void getPosition(Vector3f dest) {
 	    dest.x = eyePosition.x;
 	    dest.y = eyePosition.y;
 	    dest.z = eyePosition.z;
 	}
 
 	// --------------------------------------------------------------------------
     /**
      * Position the camera at (eyeX, eyeY, eyeZ) in world space, so that it is
      * facing the point (centerX, centerY, centerZ) in the center of its view,
      * and with up vector given by (upX, upY, upZ).
      * 
      * @param eyeX
      * @param eyeY
      * @param eyeZ
      * @param centerX
      * @param centerY
      * @param centerZ
      * @param upX
      * @param upY
      * @param upZ
      */
 	public void lookAt(float eyeX, float eyeY, float eyeZ, float centerX,
 			float centerY, float centerZ, float upX, float upY, float upZ) {
 
 		centerPosition.x = centerX;
 		centerPosition.y = centerY;
 		centerPosition.z = centerZ;
 
 		eyePosition.x = eyeX;
 		eyePosition.y = eyeY;
 		eyePosition.z = eyeZ;
 		
 		// f = centerPosition - eyePosition.
 		Vector3f.sub(centerPosition, eyePosition, f);
 		
 		// Do nothing if centerPosition ~= eyePostion.
 		if (f.length() <= MathUtils.EPSILON) return;
 		
 		f.normalise();
 
 		u.x = upX;
 		u.y = upY;
 		u.z = upZ;
 
 		// l = u x f
 		Vector3f.cross(u, f, l);
 		l.normalise();
 	
 		// u = f x l
 		Vector3f.cross(f, l, u);
 		
 		// Flip f so it points along camera's local z-axis.
 		f.scale(-1f);
 		
 		// Flip l so it points along camera's local x-axis.
 		l.scale(-1f);
 		
 		// Construct orientation from the basis vectors s, u, f.
 		orientation.fromAxes(l, u, f);
 		
 		// Reset camera's f and l vectors.
 		f.scale(-1f);
 		l.scale(-1f);
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Position the Camera at <code>eyePos</code> in world space, so that it is
 	 * facing with the point <code>centerPos</code> in the center of its view,
 	 * and with up direction given by <code>upDir</code>.
 	 * 
 	 * @param eyePos - world space position of Camera.
 	 * @param centerPos - world space target the Camera is pointed at.
 	 * @param upDir - world space up direction for Camera.
 	 */
 	public void lookAt(Vector3f eyePos, Vector3f centerPos, Vector3f upDir) {
 		lookAt(eyePos.x, eyePos.y, eyePos.z, centerPos.x, centerPos.y,
 				centerPos.z, upDir.x, upDir.y, upDir.z);
 	}
 
 
 	// --------------------------------------------------------------------------
 	public void lookAt(float centerX, float centerY, float centerZ) {
 		centerPosition.x = centerX;
 		centerPosition.y = centerY;
 		centerPosition.z = centerZ;
 
 		// f = center - eye.
 		Vector3f.sub(centerPosition, eyePosition, f);
 
 		// If center ~= eyePosition, do nothing.
 		if (f.lengthSquared() <= MathUtils.EPSILON) return;
 
 		f.normalise();
 
 		// The following projects u onto the plane defined by the point eyePosition,
 		// and the normal f. The goal is to rotate u so that it is orthogonal to f,
 		// while attempting to keep u's orientation close to its previous direction.
 		{
 			// Borrow l vector for calculation, so we don't have to allocate a
 			// new vector.
 			// l = eye + u
 			Vector3f.add(eyePosition, u, l);
 
			// t = -1 * (f dot u)
			float t = -1f * Vector3f.dot(f, u);
 
 			// Move point l in the normal direction, f, by t units so that it is
 			// on the plane.
 			l.x += t * f.x;
 			l.y += t * f.y;
 			l.z += t * f.z;
 
 			// u = l - eye.
 			Vector3f.sub(l, eyePosition, u);
 			u.normalise();
 		}
 
 		// Update l vector given new f and u vectors.
 		// l = u x f
 		Vector3f.cross(u, f, l);
 
 		// If f and u are no longer orthogonal, make them so.
 		if (Vector3f.dot(f, u) > MathUtils.EPSILON) {
 			// u = f x l
 			Vector3f.cross(f, l, u);
 			u.normalise();
 		}
 		
         // Flip f so it points along camera's local z-axis.
 		f.scale(-1f);
 		
 		// Flip l so it points along camera's local x-axis.
 		l.scale(-1f);
 		
 		// Construct orientation from the basis vectors l, u, f, which make up the
 		// camera's local right handed coordinate system.
 		orientation.fromAxes(l, u, f);
 		
 		// Reset camera's f and l vectors, so f points forward, and l points to the
 		// left, as seen by the camera.
 		f.scale(-1f);
 		l.scale(-1f);
 	}
 
 	// --------------------------------------------------------------------------
 	public void lookAt(Vector3f centerPos) {
 		lookAt(centerPos.x, centerPos.y, centerPos.z);
 	}
 
 	// --------------------------------------------------------------------------
 	public void rotate(Vector3f axis, float angle) {
 		Quaternion p = new Quaternion(axis, angle);
 		Quaternion.mult(p, orientation, orientation);
 		orientation.normalize();
 	}
 	
 	// --------------------------------------------------------------------------
 	public void roll(float angle) {
 		Vector3f localZAxis = new Vector3f(Z_AXIS);
 		orientation.rotate(localZAxis);
 		
 		Quaternion q = new Quaternion(localZAxis, angle);
 		
 		// Update camera's local left and up vectors.
 		q.rotate(l);
 		q.rotate(u);
 		
 		// orientation = q * orientation.
 		Quaternion.mult(q, orientation, orientation);
 	}
 	
 	// --------------------------------------------------------------------------
 	public void pitch(float angle) {
 		Vector3f localXAxis = new Vector3f(X_AXIS);
 		orientation.rotate(localXAxis);
 		
 		Quaternion q = new Quaternion(localXAxis, angle);
 		
 		// Update camera's local up and forward vectors.
 		q.rotate(u);
 		q.rotate(f);
 		
 		// orientation = q * orientation.
 		Quaternion.mult(q, orientation, orientation);
 	}
 	
 	// --------------------------------------------------------------------------
 	public void yaw(float angle) {
 		Vector3f localYAxis = new Vector3f(Y_AXIS);
 		orientation.rotate(localYAxis);
 		
 		Quaternion q = new Quaternion(localYAxis, angle);
 		
 		// Update camera's local left and forward vectors.
 		q.rotate(l);
 		q.rotate(f);
 		
 		// orientation = q * orientation.
 		Quaternion.mult(q, orientation, orientation);
 	}
 
 	// --------------------------------------------------------------------------
 	/**
 	 * Gets a new {@link Matrix4f} view matrix representation for this Camera.
 	 * In OpenGL parlance the view matrix transforms points from World Space to
 	 * Camera Space.
 	 * 
 	 * @return a new view matrix
 	 */
 	public Matrix4f getViewMatrix() {
 		orientation.normalize();
 		
 		// Each column of the viewMatrix describes a camera basis vector using
 		// world space coordinates.
 		//
 		// | s_x | u_x | f_x | 0 |
 		// | s_y | u_y | f_y | 0 |
 		// | s_z | u_z | f_z | 0 |
 		// |  0  |  0  |  0  | 1 |
 		Matrix4f viewMatrix = orientation.toRotationMatrix();
 		
 		// Transpose the viewMatrix so that each column describes a world space
 		// basis vector using camera space coordinates.
 		//
 		// | s_x | s_y | s_z | 0 |
 		// | u_x | u_y | u_z | 0 |
 		// | f_x | f_y | f_z | 0 |
 		// |  0  |  0  |  0  | 1 |
 		viewMatrix.transpose();
 
 		// Apply inverse translation from world space origin to
 		// the camera's eye position.
 		Vector3f dist = new Vector3f();
 		dist.x = (-1) * eyePosition.x;
 		dist.y = (-1) * eyePosition.y;
 		dist.z = (-1) * eyePosition.z;
 		viewMatrix.translate(dist);
 		
 
 		return viewMatrix;
 	}
 }

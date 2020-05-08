 package org.jtrace.cameras;
 
 import org.jtrace.Jay;
 import org.jtrace.primitives.Point3D;
 import org.jtrace.primitives.Vector3D;
 
 public class PinHoleCamera extends Camera {
 
 	public PinHoleCamera(Point3D eye, Point3D lookAt, Vector3D up) {
 		this.eye = eye;
 		this.lookAt = lookAt;
 		this.up = up;
 		
 		computeUVW();
 	}
 	
 	public Jay createJay(int r, int c, int vres, int hres) {
		double viewPlaneX = (c - 0.5 * (hres - 1.0));
		double viewPlaneY = (r - 0.5 * (vres - 1.0));
 		
 		Vector3D dU = u.multiply(viewPlaneX);
 		Vector3D dV = v.multiply(viewPlaneY);
 		Vector3D dW = w.multiply(viewPlaneDistance);
 		
 		Vector3D jayDirection = dU.add(dV).subtract(dW).normal();
 		
 		return new Jay(eye, jayDirection);
 	}
 }

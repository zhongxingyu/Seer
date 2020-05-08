 package edu.hsbremen.animvisu.util;
 
 import org.lwjgl.util.vector.Vector3f;
 
 public class VectorUtil {
 
 	public static Vector3f createNormal(Vector3f p1, Vector3f p2, Vector3f p3) {
 		Vector3f temp1 = new Vector3f();
 		Vector3f temp2 = new Vector3f();
 		Vector3f temp3 = new Vector3f();
 		
		Vector3f.sub(p2, p1, temp1);
		Vector3f.sub(p3, p1, temp2);
 		Vector3f.cross(temp1, temp2, temp3);
 		temp3.normalise();
 		return temp3;
 	}
 
 }

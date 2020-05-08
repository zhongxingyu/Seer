 package de.tum.in.jrealityplugin.jogl;
 
 import javax.vecmath.AxisAngle4d;
 import javax.vecmath.Matrix4d;
 import javax.vecmath.Vector3d;
 
 public class Util {
 	public static AxisAngle4d rotateFromTo(Vector3d from, Vector3d to) {
 		double angle = from.angle(to);
 		Vector3d axis = new Vector3d();
 		axis.cross(from, to);
		if (axis.epsilonEquals(new Vector3d(), 1E-8))
			return new AxisAngle4d(new Vector3d(1,0,0), 0);
 		axis.normalize();
 		return new AxisAngle4d(axis, angle);
 	}
 
 	public static float[] matrix4dToFloatArray(Matrix4d m) {
 		return new float[] { (float) m.m00, (float) m.m01, (float) m.m02,
 				(float) m.m03, (float) m.m10, (float) m.m11, (float) m.m12,
 				(float) m.m13, (float) m.m20, (float) m.m21, (float) m.m22,
 				(float) m.m23, (float) m.m30, (float) m.m31, (float) m.m32,
 				(float) m.m33 };
 	}
 }

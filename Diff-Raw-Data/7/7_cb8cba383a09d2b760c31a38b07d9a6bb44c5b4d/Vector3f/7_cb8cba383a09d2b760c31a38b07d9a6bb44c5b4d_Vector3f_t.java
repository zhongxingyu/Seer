 package yang.math.objects;
 
 
 
 public class Vector3f extends Point3f{
 
 	public final static Vector3f ZERO = new Vector3f(0,0,0);
 	public final static Vector3f ONE = new Vector3f(1,1,1);
 	public final static Vector3f RIGHT = new Vector3f(1,0,0);
 	public final static Vector3f LEFT = new Vector3f(-1,0,0);
 	public final static Vector3f UP = new Vector3f(0,1,0);
 	public final static Vector3f DOWN = new Vector3f(0,-1,0);
 	public final static Vector3f FORWARD = new Vector3f(0,0,1);
 	public final static Vector3f BACKWARD = new Vector3f(0,0,-1);
 	public static final Vector3f POSITIVE_Z = new Vector3f(0,0,1);
 	public static final Vector3f NEGATIVE_Z = new Vector3f(0,0,-1);
 
 	public Vector3f() {
 		set(0,0,0);
 	}
 
 	public Vector3f(float x,float y,float z) {
 		set(x,y,z);
 	}
 
 	public Vector3f(Point3f values) {
 		set(values);
 	}
 
 	public float magn() {
 		return (float)Math.sqrt(mX*mX + mY*mY + mZ*mZ);
 	}
 
 	public float sqrMagn() {
 		return mX*mX + mY*mY + mZ*mZ;
 	}
 
 	@Override
 	public void set(float x,float y,float z) {
 		mX = x;
 		mY = y;
 		mZ = z;
 	}
 
 	@Override
 	public void set(float x,float y) {
 		mX = x;
 		mY = y;
 	}
 
 	public void set(float xyz) {
 		mX = xyz;
 		mY = xyz;
 		mZ = xyz;
 	}
 
 	public void setNormalized(float x,float y,float z) {
 		float dist = (float)Math.sqrt(x*x+y*y+z*z);
 		if(dist==0) {
 			mX = 0;
 			mY = 0;
 			mZ = 0;
 		}else{
 			dist = 1/dist;
 			mX = x*dist;
 			mY = y*dist;
 			mZ = z*dist;
 		}
 	}
 
 	public float setNormalized(Vector3f vector) {
 		float dist = (float)Math.sqrt(vector.mX*vector.mX+vector.mY*vector.mY+vector.mZ*vector.mZ);
 		if(dist==0) {
 			mX = 0;
 			mY = 0;
 			mZ = 0;
 		}else{
			mX = vector.mX/dist;
			mY = vector.mY/dist;
			mZ = vector.mZ/dist;
 		}
 		return dist;
 	}
 
 	public float normalize() {
 		final float d = (float)(Math.sqrt(mX*mX+mY*mY+mZ*mZ));
 		if(d!=0) {
 			mX /= d;
 			mY /= d;
 			mZ /= d;
 		}
 		return d;
 	}
 
 	public float dot(Vector3f vector) {
 		return mX*vector.mX + mY*vector.mY + mZ*vector.mZ;
 	}
 
 	public float dot(float x,float y,float z) {
 		return mX*x + mY*y + mZ*z;
 	}
 
 	public void cross(Vector3f lhsVector,Vector3f rhsVector) {
 		mX = lhsVector.mY*rhsVector.mZ - lhsVector.mZ*rhsVector.mY;
 		mY = lhsVector.mZ*rhsVector.mX - lhsVector.mX*rhsVector.mZ;
 		mZ = lhsVector.mX*rhsVector.mY - lhsVector.mY*rhsVector.mX;
 	}
 
 	public void cross(float lhsX,float lhsY,float lhsZ, float rhsX,float rhsY,float rhsZ) {
 		mX = lhsY*rhsZ - lhsZ*rhsY;
 		mY = lhsZ*rhsX - lhsX*rhsZ;
 		mZ = lhsX*rhsY - lhsY*rhsX;
 	}
 
 	@Override
 	public Vector3f clone() {
 		return new Vector3f(mX,mY,mZ);
 	}
 
 	@Override
 	public void setAlphaBeta(float alpha, float beta, float distance) {
 		mX = (float)(Math.sin(alpha)*Math.cos(beta)) * distance;
 		mY = (float)(Math.sin(beta)) * distance;
 		mZ = (float)(Math.cos(alpha)*Math.cos(beta)) * distance;
 	}
 
 	public void createOrthoVec(Vector3f ortho) {
 		final float absX = Math.abs(ortho.mX);
 		final float absY = Math.abs(ortho.mY);
 		final float absZ = Math.abs(ortho.mZ);
 		if(absX<=absY && absX<=absZ) {
 			setNormalized(0,-ortho.mZ,ortho.mY);
 		}else if(absY<=absX && absY<=absZ) {
 			setNormalized(-ortho.mZ,0,ortho.mX);
 		}else{
 			setNormalized(-ortho.mY,ortho.mX,0);
 		}
 	}
 
 	public void applyQuaternion(Quaternion quaternion, Vector3f vector) {
 		quaternion.applyToVector(this,vector);
 	}
 
 	public boolean isZero() {
 		return mX==0 && mY==0 && mZ==0;
 	}
 
 	public void setOne() {
 		mX = 1;
 		mY = 1;
 		mZ = 1;
 	}
 
 	public void rotateX(float angle) {
 		final float sinA = (float)Math.sin(angle);
 		final float cosA = (float)Math.cos(angle);
 		final float y = cosA*mY - sinA*mZ;
 		final float z = sinA*mY + cosA*mZ;
 		mY = y;
 		mZ = z;
 	}
 
 	public void rotateY(float angle) {
 		final float sinA = (float)Math.sin(angle);
 		final float cosA = (float)Math.cos(angle);
 		final float x = cosA*mX + sinA*mZ;
 		final float z = -sinA*mX + cosA*mZ;
 		mX = x;
 		mZ = z;
 	}
 
 	public void rotateZ(float angle) {
 		final float sinA = (float)Math.sin(angle);
 		final float cosA = (float)Math.cos(angle);
 		final float x = cosA*mX - sinA*mY;
 		final float y = sinA*mX + cosA*mY;
 		mX = x;
 		mY = y;
 	}
 
 	public void rotateEuler(float yaw, float pitch, float roll) {
 		rotateZ(roll);
 		rotateX(pitch);
 		rotateY(yaw);
 	}
 
 	public void setFromTo(Point3f fromPoint,Point3f toPoint) {
 		mX = toPoint.mX-fromPoint.mX;
 		mY = toPoint.mY-fromPoint.mY;
 		mZ = toPoint.mZ-fromPoint.mZ;
 	}
 
 	public void setFromToDirection(Point3f fromPoint, Point3f toPoint) {
 		mX = toPoint.mX-fromPoint.mX;
 		mY = toPoint.mY-fromPoint.mY;
 		mZ = toPoint.mZ-fromPoint.mZ;
 		normalize();
 	}
 
 	public void setMagnitute(float newMagnitute) {
 		float d = (float)(Math.sqrt(mX*mX+mY*mY+mZ*mZ));
 		if(d!=0) {
 			d = 1f/d * newMagnitute;
 			mX *= d;
 			mY *= d;
 			mZ *= d;
 		}
 	}
 
 	public void setMaxMagnitute(float maxMagnitute) {
 		float d = (float)(Math.sqrt(mX*mX+mY*mY+mZ*mZ));
 		if(d>maxMagnitute) {
 			d = 1f/d * maxMagnitute;
 			mX *= d;
 			mY *= d;
 			mZ *= d;
 		}
 	}
 
 	public float getManhattanDistance() {
 		return (mX<0?-mX:mX)+(mY<0?-mY:mY)+(mZ<0?-mZ:mZ);
 	}
 
 	public float magnXY() {
 		return (float)Math.sqrt(mX*mX + mY*mY);
 	}
 
 	public float magnXZ() {
 		return (float)Math.sqrt(mX*mX + mZ*mZ);
 	}
 
 	public float getAngleXY(Vector3f otherVector) {
 		float fac = this.mX*otherVector.mY>this.mY*otherVector.mX?1:-1;
 		return fac*(float)Math.acos((mX*otherVector.mX + mY*otherVector.mY)/(this.magnXY()*otherVector.magnXY()));
 	}
 
 	public float getAngleXZ(Vector3f otherVector) {
 		float fac = this.mX*otherVector.mZ>this.mZ*otherVector.mX?1:-1;
 		return fac*(float)Math.acos((mX*otherVector.mX + mZ*otherVector.mZ)/(this.magnXZ()*otherVector.magnXZ()));
 	}
 
 }

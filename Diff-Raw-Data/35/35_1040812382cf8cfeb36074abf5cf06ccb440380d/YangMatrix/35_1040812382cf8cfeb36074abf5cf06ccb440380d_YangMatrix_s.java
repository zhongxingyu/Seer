 package yang.math.objects.matrix;
 
 import yang.math.MatrixOps;
 import yang.math.objects.Vector3f;
 
 //TODO normal inversion only 3x3, autocreate inversed mat, save orthogonal etc information
 
 public class YangMatrix {
 
 	public static final YangMatrix IDENTITY = new YangMatrix();
 	
 	public static float DEFAULT_NEAR = 1;
 	public static float DEFAULT_FAR = -1;
 	
 	//Indices: Row-Column
 	public static final int M00 = 0;
 	public static final int M01 = 4;
 	public static final int M02 = 8;
 	public static final int M03 = 12;
 	public static final int M10 = 1;
 	public static final int M11 = 5;
 	public static final int M12 = 9;
 	public static final int M13 = 13;
 	public static final int M20 = 2;
 	public static final int M21 = 6;
 	public static final int M22 = 10;
 	public static final int M23 = 14;
 	public static final int M30 = 3;
 	public static final int M31 = 7;
 	public static final int M32 = 11;
 	public static final int M33 = 15;
 	
 	public float[] mMatrix;
 	public float[] mInverted;
 	protected float[] mMatrixBack;
 	protected float[] mTempMat1,mTempMat2;
 	protected float[][] mStack;
 	public int mStackPointer;	//pre increment
 
 	public static float TO_RAD_FACTOR = (float) Math.PI / 180;
 	public static float TO_DEG_FACTOR = 180 / (float) Math.PI;
 	
 	public static void identity4f(float[] matrix) {
 		matrix[0] = 1;
 		matrix[1] = 0;
 		matrix[2] = 0;
 		matrix[3] = 0;
 		matrix[4] = 0;
 		matrix[5] = 1;
 		matrix[6] = 0;
 		matrix[7] = 0;
 		matrix[8] = 0;
 		matrix[9] = 0;
 		matrix[10] = 1;
 		matrix[11] = 0;
 		matrix[12] = 0;
 		matrix[13] = 0;
 		matrix[14] = 0;
 		matrix[15] = 1;
 	}
 
 	public static void identity3f(float[] matrix) {
 		matrix[0] = 1;
 		matrix[1] = 0;
 		matrix[2] = 0;
 		matrix[3] = 0;
 		matrix[4] = 1;
 		matrix[5] = 0;
 		matrix[6] = 0;
 		matrix[7] = 0;
 		matrix[8] = 1;
 	}
 	
 	public YangMatrix() {
 		mMatrix = new float[16];
 		mMatrixBack = new float[16];
 		mTempMat1 = new float[16];
 		mTempMat2 = new float[16];
 		mInverted = null;
 		mStack = null;
 		mStackPointer = -1;
 		loadIdentity();
 	}
 	
 	public void initStack(int capacity) {
 		mStack = new float[capacity][16];
 	}
 	
 	public void stackClear() {
 		mStackPointer = -1;
 	}
 	
 	public void stackGoto(int absoluteIndex) {
 		mStackPointer = absoluteIndex;
 	}
 	
 	public void stackDecPointer(int dec) {
 		mStackPointer-=dec;
 	}
 	
 	public void stackIncPointer(int dec) {
 		mStackPointer+=dec;
 	}
 	
 	public void stackPush() {
 		System.arraycopy(mMatrix,0,mStack[++mStackPointer],0,16);
 	}
 	
 	public void stackPop() {
 		System.arraycopy(mStack[mStackPointer--],0,mMatrix,0,16);
 	}
 	
 	public void stackGetTop() {
 		System.arraycopy(mStack[mStackPointer],0,mMatrix,0,16);
 	}
 	
 	public void stackRefreshTop() {
 		System.arraycopy(mMatrix,0,mStack[mStackPointer],0,16);
 	}
 	
 	public void stackGet(int index) {
 		System.arraycopy(mStack[mStackPointer-index],0,mMatrix,0,16);
 	}
 	
 	public void stackSet(int index) {
 		System.arraycopy(mMatrix,0,mStack[mStackPointer-index],0,16);
 	}
 
 	public void loadIdentity() {
 		MatrixOps.setIdentity(mMatrix);
 	}
 
 	public void translate(float x, float y, float z) {
 		mMatrix[12] += mMatrix[0]*x + mMatrix[4]*y + mMatrix[8]*z;
 		mMatrix[13] += mMatrix[1]*x + mMatrix[5]*y + mMatrix[9]*z;
 		mMatrix[14] += mMatrix[2]*x + mMatrix[6]*y + mMatrix[10]*z;
 		mMatrix[15] += mMatrix[3]*x + mMatrix[7]*y + mMatrix[11]*z;
 	}
 
 	public void scale(float x, float y, float z) {
 		for(int i=0;i<4;i++) {
 			mMatrix[i] *= x;
 			mMatrix[i+4] *= y;
 			mMatrix[i+8] *= z;
 		}
 	}
 	
 	public void postScale(float x, float y, float z) {
		mMatrix[0] *= x;
		mMatrix[5] *= y;
		mMatrix[10] *= z;
 	}
 	
 	public void scale(float x, float y) {
 		scale(x, y, 1);
 	}
 
 	public void scale(float s) {
 		scale(s, s, s);
 	}
 
 	public void rotateX(float angle) {
 		MatrixOps.setRotationX(mTempMat1,angle);
 		MatrixOps.multiply(mMatrixBack,mMatrix,mTempMat1);
 		float[] swap = mMatrix;
 		mMatrix = mMatrixBack;
 		mMatrixBack = swap;
 	}
 
 	public void rotateY(float angle) {
 		MatrixOps.setRotationY(mTempMat1,angle);
 		MatrixOps.multiply(mMatrixBack,mMatrix,mTempMat1);
 		float[] swap = mMatrix;
 		mMatrix = mMatrixBack;
 		mMatrixBack = swap;
 	}
 
 	public void rotateZ(float angle) {
 		MatrixOps.setRotationZ(mTempMat1,angle);
 		MatrixOps.multiply(mMatrixBack,mMatrix,mTempMat1);
 		float[] swap = mMatrix;
 		mMatrix = mMatrixBack;
 		mMatrixBack = swap;
 	}
 	
 	public void rotateZAround(float angle,float anchorX,float anchorY) {
 		translate(-anchorX, -anchorY);
 		rotateZ(angle);
 		translate(anchorX,anchorY);
 	}
 
 	public void multiplyRight(float[] rhsMatrix) {
 		
 		MatrixOps.multiply(mMatrixBack,mMatrix,rhsMatrix);
 		
 		float[] swap = mMatrix;
 		mMatrix = mMatrixBack;
 		mMatrixBack = swap;
 	}
 
 	public void multiplyLeft(float[] lhsMatrix) {
 
 		MatrixOps.multiply(mMatrixBack,lhsMatrix,mMatrix);
 		
 		float[] swap = mMatrix;
 		mMatrix = mMatrixBack;
 		mMatrixBack = swap;
 	}
 
 	public void multiply(float[] lhsMatrix,float[] rhsMatrix) {
 		MatrixOps.multiply(mMatrix,lhsMatrix,rhsMatrix);
 	}
 	
 	public void multiply(YangMatrix lhsMatrix,YangMatrix rhsMatrix) {
 		MatrixOps.multiply(mMatrix,lhsMatrix.mMatrix,rhsMatrix.mMatrix);
 	}
 	
 	public void set(YangMatrix src) {
 		System.arraycopy(src.mMatrix,0, this.mMatrix,0, 16);
 	}
 	
 	public boolean asInverted(float[] target) {
 		return MatrixOps.invert(target,mMatrix,mTempMat1,mTempMat2);
 	}
 	
 	//TODO very inefficient!
 	public boolean asNormalTransform4f(float[] target) {
 		if(!refreshInverted())
 			return false;
 		target[0] = mInverted[0];
 		target[1] = mInverted[4];
 		target[2] = mInverted[8];
 		target[3] = 0;
 		target[4] = mInverted[1];
 		target[5] = mInverted[5];
 		target[6] = mInverted[9];
 		target[7] = 0;
 		target[8] = mInverted[2];
 		target[9] = mInverted[6];
 		target[10] = mInverted[10];
 		target[11] = 0;
 		target[12] = 0;
 		target[13] = 0;
 		target[14] = 0;
 		target[15] = 1;
 		return true;
 	}
 	
 	public boolean asNormalTransform3f(float[] target) {
 		if(!refreshInverted())
 			return false;
 		target[0] = mInverted[0];
 		target[1] = mInverted[4];
 		target[2] = mInverted[8];
 		target[3] = mInverted[1];
 		target[4] = mInverted[5];
 		target[5] = mInverted[9];
 		target[6] = mInverted[2];
 		target[7] = mInverted[6];
 		target[8] = mInverted[10];
 		return true;
 	}
 	
 	public boolean refreshInverted() {
 		if(mInverted==null)
 			mInverted = new float[16];
 		return MatrixOps.invert(mInverted,mMatrix,mTempMat1,mTempMat2);
 	}
 	
 	public void invert() {
 		MatrixOps.invert(mMatrixBack,mMatrix,mTempMat1,mTempMat2);
 		
 		float[] swap = mMatrix;
 		mMatrix = mMatrixBack;
 		mMatrixBack = swap;
 	}
 
 	public float get(int index) {
 		return mMatrix[index];
 	}
 	
 	public float get(int row, int column) {
 		return mMatrix[column*4+row];
 	}
 
 	public void set(int row, int column, float value) {
 		mMatrix[column*4+row] = value;
 	}
 
 	public void setColumn(int i, float x, float y, float z) {
 		mMatrix[i*4] = x;
 		mMatrix[i*4+1] = y;
 		mMatrix[i*4+2] = z;
 	}
 	
 	public void setColumn(int i, float x, float y, float z, float w) {
 		mMatrix[i*4] = x;
 		mMatrix[i*4+1] = y;
 		mMatrix[i*4+2] = z;
 		mMatrix[i*4+3] = w;
 	}
 	
 	public void setRow(int i, float x, float y, float z) {
 		mMatrix[i] = x;
 		mMatrix[4+i] = y;
 		mMatrix[8+i] = z;
 	}
 	
 	public void setRow(int i, float x, float y, float z, float w) {
 		mMatrix[i] = x;
 		mMatrix[4+i] = y;
 		mMatrix[8+i] = z;
 		mMatrix[12+i] = w;
 	}
 
 	public void setRowMajor(double[][] matrix) {
 		mMatrix[0] = (float)matrix[0][0];
 		mMatrix[1] = (float)matrix[1][0];
 		mMatrix[2] = (float)matrix[2][0];
 		mMatrix[3] = (float)matrix[3][0];
 		mMatrix[4] = (float)matrix[0][1];
 		mMatrix[5] = (float)matrix[1][1];
 		mMatrix[6] = (float)matrix[2][1];
 		mMatrix[7] = (float)matrix[3][1];
 		mMatrix[8] = (float)matrix[0][2];
 		mMatrix[9] = (float)matrix[1][2];
 		mMatrix[10] = (float)matrix[2][2];
 		mMatrix[11] = (float)matrix[3][2];
 		mMatrix[12] = (float)matrix[0][3];
 		mMatrix[13] = (float)matrix[1][3];
 		mMatrix[14] = (float)matrix[2][3];
 		mMatrix[15] = (float)matrix[3][3];
 	}
 
 	public void setColumnMajor(double[][] matrix) {
 		mMatrix[0] = (float)matrix[0][0];
 		mMatrix[1] = (float)matrix[0][1];
 		mMatrix[2] = (float)matrix[0][2];
 		mMatrix[3] = (float)matrix[0][3];
 		mMatrix[4] = (float)matrix[1][0];
 		mMatrix[5] = (float)matrix[1][1];
 		mMatrix[6] = (float)matrix[1][2];
 		mMatrix[7] = (float)matrix[1][3];
 		mMatrix[8] = (float)matrix[2][0];
 		mMatrix[9] = (float)matrix[2][1];
 		mMatrix[10] = (float)matrix[2][2];
 		mMatrix[11] = (float)matrix[2][3];
 		mMatrix[12] = (float)matrix[3][0];
 		mMatrix[13] = (float)matrix[3][1];
 		mMatrix[14] = (float)matrix[3][2];
 		mMatrix[15] = (float)matrix[3][3];
 	}
 
 	public void multiplyRight(YangMatrix rhsMatrix) {
 		multiplyRight(rhsMatrix.mMatrix);
 	}
 
 	public void multiplyLeft(YangMatrix lhsMatrix) {
 		multiplyLeft(lhsMatrix.mMatrix);
 	}
 
 	public void translate(float x, float y) {
 		translate(x, y, 0);
 	}
 
 	public float[] asFloatArrayDeep() {
 		return MatrixOps.createMatrixCopy(mMatrix);
 	}
 	
 	public float apply2DX(float x, float y) {
 		return MatrixOps.applyFloatMatrixX2D(mMatrix,x,y);
 	}
 
 	public float apply2DY(float x, float y) {
 		return MatrixOps.applyFloatMatrixY2D(mMatrix,x,y);
 	}
 
 	public void apply2D(float x, float y, float[] target, int targetOffset) {
 		MatrixOps.applyFloatMatrix2D(mMatrix,x,y,target,targetOffset);
 	}
 	
 	public void apply3D(float x, float y, float z, float[] target, int targetOffset) {
 		MatrixOps.applyFloatMatrix3D(mMatrix,x,y,z,target,targetOffset);
 	}
 	
 	public void apply3D(float x, float y, float z,Vector3f target) {
 		MatrixOps.applyFloatMatrix3D(mMatrix, x, y, z, target);
 	}
 	
 	public void apply3DNormalized(float x, float y, float z, float[] target, int targetOffset) {
 		MatrixOps.applyFloatMatrix3DNormalized(mMatrix,x,y,z,target,targetOffset);
 	}
 	
 	protected void setColumn(int col, Vector3f values) {
 		setColumn(col,values.mX,values.mY,values.mZ);
 	}
 	
 	protected void setRow(int row, Vector3f values) {
 		setRow(row,values.mX,values.mY,values.mZ);
 	}
 
 	public void scaleX(float value) {
 		scale(value,1,1);
 	}
 	
 	public void scaleY(float value) {
 		scale(1,value,1);
 	}
 	
 	public void scaleZ(float value) {
 		scale(1,1,value);
 	}
 
 	public void setTranslationOnly() {
 		setRow(0,1,0,0);
 		setRow(1,0,1,0);
 		setRow(2,0,0,1);
 	}
 
 	public void applyToArray(float[] source, int vertexCount, boolean zComponent, float preShiftX, float preShiftY, float postShiftX, float postShiftY,float[] target, int targetOffset) {
 		int sourceOffset = 0;
 		float[] matrix = this.mMatrix;
 		if(!zComponent) {
 			for(int i=0;i<vertexCount;i++) {
 				float x = source[sourceOffset++]+preShiftX;
 				float y = source[sourceOffset++]+preShiftY;
 				target[targetOffset++] = matrix[0]*x+matrix[4]*y+matrix[12]+postShiftX;
 				target[targetOffset++] = matrix[1]*x+matrix[5]*y+matrix[13]+postShiftY;
 			}
 		}else{
 			for(int i=0;i<vertexCount;i++) {
 				float x = source[sourceOffset++]+preShiftX;
 				float y = source[sourceOffset++]+preShiftY;
 				float z = source[sourceOffset++];
 				target[targetOffset++] = matrix[0]*x+matrix[4]*y+matrix[8]*z+matrix[12]+postShiftX;
 				target[targetOffset++] = matrix[1]*x+matrix[5]*y+matrix[9]*z+matrix[13]+postShiftY;
 				target[targetOffset++] = matrix[2]*x+matrix[6]*y+matrix[10]*z+matrix[14];
 			}
 		}
 	}
 	
 	
 	@Override
 	public String toString() {
 		return MatrixOps.matToString(mMatrix);
 	}
 
 	public static void setBase4f(float[] target,Vector3f vec1, Vector3f vec2, Vector3f vec3) {
 		target[0] = vec1.mX;
 		target[1] = vec1.mY;
 		target[2] = vec1.mZ;
 		target[3] = 0;
 		target[4] = vec2.mX;
 		target[5] = vec2.mY;
 		target[6] = vec2.mZ;
 		target[7] = 0;
 		target[8] = vec3.mX;
 		target[9] = vec3.mY;
 		target[10] = vec3.mZ;
 		target[11] = 0;
 		target[12] = 0;
 		target[13] = 0;
 		target[14] = 0;
 		target[15] = 1;
 	}
 	
 	public void setBase(Vector3f vec1, Vector3f vec2, Vector3f vec3) {
 		setBase4f(mMatrix,vec1,vec2,vec3);
 	}
 	
 	public void multiplyBaseVectorsRight(Vector3f vec1, Vector3f vec2, Vector3f vec3) {
 		setBase4f(mTempMat1,vec1,vec2,vec3);
 		multiplyRight(mTempMat1);
 	}
 	
 	public void multiplyBaseVectorsLeft(Vector3f vec1, Vector3f vec2, Vector3f vec3) {
 		setBase4f(mTempMat1,vec1,vec2,vec3);
 		multiplyLeft(mTempMat1);
 	}
 	
 }

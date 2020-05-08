 package main;
 
 import java.util.*;
 
 import math.matrices.*;
 import openGLCLInterfaces.Enumerations.BufferTarget;
 import openGLCLInterfaces.Enumerations.BufferUsage;
 import openGLCLInterfaces.openGL.buffers.*;
 
 /**
  * A container class for matrices used in shader calculations. Currently holds
  * projection matrix and modelview matrix. It manages a uniform buffer which is
  * bound to port 0.
  */
 public class MatrixContainer {
 
 	/**
 	 * Matrices than can be individually edited.
 	 */
 	public enum MatrixType {
 		projectionMatrix, modelViewMatrix
 	}
 
 	private static Matrix4x4[] matrices = new Matrix4x4[2];
 	private static MatrixType currentMatrix = MatrixType.projectionMatrix;
 	private static BufferObject uniformBuffer = new BufferObject(BufferTarget.UNIFORM_BUFFER,
 			BufferUsage.STREAM_DRAW);
 	private static List<Matrix4x4>[] matrixHistory = initArray(2);
 
 	static {
 		// initialize all matrices to identity
 		for (int i = 0; i < matrices.length; i++) {
 			matrices[i] = (Matrix4x4) MatrixDB.generateIdentityMatrix(4);
 			matrixHistory[i] = new LinkedList<Matrix4x4>();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	/**
 	 * This is a workaround for the java restriction to not have generic arrays.
 	 * @param size The size of the desired generic array.
 	 * @return Returns a generic array.
 	 */
 	private static LinkedList<Matrix4x4>[] initArray(int size) {
 		return new LinkedList[size];
 	}
 
 	/**
 	 * @return Returns the index of the currently active matrix in the matrices
 	 *         array.
 	 */
 	private static int getCurrentMatrixIndex() {
 		return currentMatrix.ordinal();
 	}
 
 	/**
 	 * Pushes the the current matrix into the stack for later retrieval using
 	 * popMatrix().
 	 */
 	public static void pushMatrix() {
 		matrixHistory[getCurrentMatrixIndex()].add(getCurrentMatrix());
 	}
 
 	/**
 	 * Pops a matrix out of the stack, restoring it to the active matrix and
 	 * deleting it from the stack.
 	 */
 	public static void popMatrix() {
 		if (matrixHistory[getCurrentMatrixIndex()].size() == 0)
 			return;
 		int latestIndex = matrixHistory[getCurrentMatrixIndex()].size() - 1;
 		setCurrentMatrix(matrixHistory[getCurrentMatrixIndex()].get(latestIndex));
 		matrixHistory[getCurrentMatrixIndex()].remove(latestIndex);
 	}
 
 	/**
 	 * This method is used to switch the currently selected matrix from
 	 * perspective to modelview mode and vice versa.
 	 * 
 	 * @param type
 	 *            The type of the matrix that should be activated.
 	 */
 	public static void activeMatrix(MatrixType type) {
 		currentMatrix = type;
 	}
 
 	/**
 	 * Overwrites the current matrix with an identity matrix.
 	 */
 	public static void loadIdentity() {
 		setCurrentMatrix((Matrix4x4) MatrixDB.generateIdentityMatrix(4));
 	}
 
 	/**
 	 * @return Returns the current matrix.
 	 */
 	public static Matrix4x4 getCurrentMatrix() {
 		return matrices[getCurrentMatrixIndex()];
 	}
 
 	/**
 	 * Overwrites the current matrix with the matrix specified.
 	 * 
 	 * @param matrix
 	 *            The matrix, the current matrix should be set to.
 	 */
 	public static void setCurrentMatrix(Matrix4x4 matrix) {
 		matrices[getCurrentMatrixIndex()] = matrix;
 	}
 
 	/**
 	 * Multiplies the current matrix with the matrix specified.
 	 * 
 	 * @param matrix
 	 *            The matrix, the current matrix should be multiplied with.
 	 */
 	public static void multiplyCurrentMatrix(Matrix matrix) {
 		setCurrentMatrix((Matrix4x4) getCurrentMatrix().multiply(matrix));
 	}
 
 	/**
 	 * Applies a matrix describing a translation.
 	 * 
 	 * @param direction
 	 *            The translation vector.
 	 */
 	public static void translate(Vector3 direction) {
 		multiplyCurrentMatrix(MatrixDB.generateTranslationMatrix(direction));
 	}
 
 	/**
 	 * Applies a matrix describing a translation.
 	 * 
 	 * @param x
 	 *            The x translation.
 	 * @param y
 	 *            The x translation.
 	 * @param z
 	 *            The z translation.
 	 */
 	public static void translate(float x, float y, float z) {
 		multiplyCurrentMatrix(MatrixDB.generateTranslationMatrix(Matrix.vec3(x, y, z)));
 	}
 
 	/**
 	 * Applies a matrix describing a translation.
 	 * 
 	 * @param direction
 	 *            The translation vector.
 	 */
 	public static void translate(Vector2 direction) {
 		multiplyCurrentMatrix(MatrixDB.generateTranslationMatrix((Vector3) direction.resize(1, 3)));
 	}
 
 	/**
 	 * Applies a matrix describing a translation.
 	 * 
 	 * @param x
 	 *            The x translation.
 	 * @param y
 	 *            The x translation.
 	 */
 	public static void translate(float x, float y) {
 		multiplyCurrentMatrix(MatrixDB.generateTranslationMatrix(Matrix.vec3(x, y, 0)));
 	}
 
 	/**
 	 * Applies a matrix describing a scale transformation.
 	 * 
 	 * @param scale
 	 *            The scale factor in x, y and z direction.
 	 */
 	public static void scale(Vector3 scale) {
 		multiplyCurrentMatrix(MatrixDB.generateScaleMatrix(scale));
 	}
 
 	/**
 	 * Applies a matrix describing a scale transformation.
 	 * 
 	 * @param x
 	 *            The scale factor in x direction.
 	 * @param y
 	 *            The scale factor in y direction.
 	 * @param z
 	 *            The scale factor in z direction.
 	 */
 	public static void scale(float x, float y, float z) {
 		multiplyCurrentMatrix(MatrixDB.generateScaleMatrix(Matrix.vec3(x, y, z)));
 	}
 
 	/**
 	 * Applies a matrix describing a scale transformation.
 	 * 
 	 * @param scale
 	 *            The scale factor in x and y direction.
 	 */
 	public static void scale(Vector2 scale) {
 		Vector3 v = (Vector3) scale.resize(1, 3);
 		v.setX(1);
 		multiplyCurrentMatrix(MatrixDB.generateScaleMatrix(v));
 	}
 
 	/**
 	 * Applies a matrix describing a scale transformation.
 	 * 
 	 * @param x
 	 *            The scale factor in x direction.
 	 * @param y
 	 *            The scale factor in y direction.
 	 */
 	public static void scale(float x, float y) {
 		multiplyCurrentMatrix(MatrixDB.generateScaleMatrix(Matrix.vec3(x, y, 1)));
 	}
 
 	/**
 	 * Applies a matrix describing a rotation around a specific axis.
 	 * 
 	 * @param angle
 	 *            Specifies the angle of rotation, in radians.
 	 * @param axis
 	 *            Specifies the axis, around which the rotation will be
 	 *            performed.
 	 */
 	public static void rotate(float angle, Vector3 axis) {
 		multiplyCurrentMatrix(MatrixDB.generateRotationMatrix(angle, axis));
 	}
 
 	/**
 	 * Applies a matrix describing a rotation around a specific axis.
 	 * 
 	 * @param angle
 	 *            Specifies the angle of rotation, in radians.
 	 * @param x
 	 *            Specifies the x component of the axis, around which the
 	 *            rotation will be performed.
 	 * @param y
 	 *            Specifies the y component of the axis, around which the
 	 *            rotation will be performed.
 	 * @param z
 	 *            Specifies the z component of the axis, around which the
 	 *            rotation will be performed.
 	 */
 	public static void rotate(float angle, float x, float y, float z) {
 		multiplyCurrentMatrix(MatrixDB.generateRotationMatrix(angle, Matrix.vec3(x, y, z)));
 	}
 
 	/**
 	 * Applies a perspective projection.
 	 * 
 	 * @param fovy
 	 *            Specifies the field of view angle, in radians, in the y
 	 *            direction.
 	 * @param aspect
 	 *            Specifies the aspect ratio that determines the field of view
 	 *            in the x direction. The aspect ratio is the ratio of x (width)
 	 *            to y (height).
 	 * @param zNear
 	 *            Specifies the distance from the viewer to the near clipping
 	 *            plane (always positive).
 	 * @param zFar
 	 *            Specifies the distance from the viewer to the far clipping
 	 *            plane (always positive).
 	 */
 	public static void perspective(float fovy, float aspect, float zNear, float zFar) {
 		multiplyCurrentMatrix(MatrixDB.generatePerspectiveMatrix(fovy, aspect, zNear, zFar));
 	}
 
 	/**
 	 * Applies a orthogonal projection.
 	 * 
 	 * @param left
 	 *            Specify the coordinates for the left vertical clipping plane.
 	 * @param right
 	 *            Specify the coordinates for the right vertical clipping plane.
 	 * @param bottom
 	 *            Specify the coordinates for the bottom horizontal clipping
 	 *            plane.
 	 * @param top
 	 *            Specify the coordinates for the top horizontal clipping plane.
 	 * @param near
 	 *            Specify the distances to the nearer depth clipping plane. This
 	 *            value is negative if the plane is to be behind the viewer.
 	 * @param far
 	 *            Specify the distances to the farther depth clipping plane.
 	 *            This value is negative if the plane is to be behind the
 	 *            viewer.
 	 */
 	public static void orthogonal(float left, float right, float bottom, float top, float near,
 			float far) {
 		multiplyCurrentMatrix(MatrixDB
 				.generateOrthogonalMatrix(left, right, bottom, top, near, far));
 	}
 
 	/**
 	 * Applies a matrix describing a view transformation.
 	 * 
 	 * @param eye
 	 *            Specifies the position of the eye point.
 	 * @param center
 	 *            Specifies the position of the reference point.
 	 * @param up
 	 *            Specifies the direction of the up vector.
 	 */
 	public static void lookAt(Vector3 eye, Vector3 center, Vector3 up) {
 		multiplyCurrentMatrix(MatrixDB.generateLookAtMatrix(eye, center, up));
 	}
 
 	private static Matrix3x3 calculateNormalMatrix() {
 		return (Matrix3x3) matrices[MatrixType.modelViewMatrix.ordinal()].resize(3, 3).getInverse()
 				.transpose();
 	}
 
 	/**
 	 * Uploads the matrices to the graphics card.
 	 */
 	public static void uploadMatrices() {
 		uniformBuffer.bind(0);
 		// put all matrices into single float array
 		float[] data = new float[2 * 16 + (3 * 4)];
 		for (int i = 0; i < matrices.length; i++) {
 			System.arraycopy(matrices[i].get1DData(), 0, data, i * 16, 16);
 		}
		System.arraycopy(calculateNormalMatrix().resize(4, 3).get1DData(), 0, data, 2 * 16, 3 * 4);
 		uniformBuffer.bufferData(data);
 	}
 }

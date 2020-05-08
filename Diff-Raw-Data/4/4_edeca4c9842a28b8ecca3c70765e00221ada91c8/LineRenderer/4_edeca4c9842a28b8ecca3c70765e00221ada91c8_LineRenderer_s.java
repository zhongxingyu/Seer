 package de.tum.in.jrealityplugin.jogl;
 
 import java.util.Collection;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import org.apache.commons.math.geometry.Rotation;
 import org.apache.commons.math.geometry.Vector3D;
 import org.apache.commons.math.linear.LUDecompositionImpl;
 import org.apache.commons.math.linear.MatrixUtils;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.RealVector;
 
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 
 import de.tum.in.jrealityplugin.jogl.Line.LineType;
 
 public class LineRenderer extends Renderer<Line> {
 
 	private ShaderProgram program = null;
 
 	private int transformLoc;
 	private int originLoc;
 	private int directionLoc;
 	private int radiusLoc;
 	private int colorLoc;
 	private int lengthLoc;
 
 	@Override
 	public void dispose(GL gl) {
 		if (program != null)
 			program.destroy(gl.getGL2());
 	}
 
 	@Override
 	public boolean init(GL gl) {
 		GL2 gl2 = gl.getGL2();
 
 		program = new ShaderProgram();
 		ShaderCode vertexShader = loadShader(
 				GL2.GL_VERTEX_SHADER,
 				getClass()
 						.getResource(
 								"/de/tum/in/jrealityplugin/resources/shader/cylinder.vert"));
 		if (!vertexShader.compile(gl2))
 			return false;
 		ShaderCode fragmentShader = loadShader(
 				GL2.GL_FRAGMENT_SHADER,
 				getClass()
 						.getResource(
 								"/de/tum/in/jrealityplugin/resources/shader/cylinder.frag"));
 		if (!fragmentShader.compile(gl2))
 			return false;
 
 		if (!program.add(vertexShader))
 			return false;
 		if (!program.add(fragmentShader))
 			return false;
 		if (!program.link(gl.getGL2(), null))
 			return false;
 
 		transformLoc = gl2.glGetUniformLocation(program.program(),
 				"cylinderTransform");
 		originLoc = gl2
 				.glGetUniformLocation(program.program(), "cylinderPoint");
 		directionLoc = gl2.glGetUniformLocation(program.program(),
 				"cylinderDirection");
 		radiusLoc = gl2.glGetUniformLocation(program.program(),
 				"cylinderRadius");
 		colorLoc = gl2.glGetUniformLocation(program.program(), "cylinderColor");
 		lengthLoc = gl2.glGetUniformLocation(program.program(),
 				"cylinderLength");
 
 		return true;
 	}
 
 	@Override
 	public void render(JOGLRenderState jrs, Collection<Line> lines) {
 		if (lines.isEmpty())
 			return;
 
 		GL2 gl2 = jrs.gl.getGL2();
 
 		// Get the model view matrix
 		RealMatrix modelView = jrs.camera.getTransform();
 
 		// Get the inverse of the projection matrix
 		RealMatrix invProjection = new LUDecompositionImpl(
 				jrs.camera.getPerspectiveTransform()).getSolver().getInverse();
 		
 		// Coordinates of the normalized view frustum
 		RealVector[] f = new RealVector[] {
 				MatrixUtils.createRealVector(new double[] { -1, -1,  1, 1 }),
 				MatrixUtils.createRealVector(new double[] { -1, -1, -1, 1 }),
 				MatrixUtils.createRealVector(new double[] { -1,  1, -1, 1 }),
 				MatrixUtils.createRealVector(new double[] {  1,  1, -1, 1 }),
 				MatrixUtils.createRealVector(new double[] {  1,  1,  1, 1 }),
 				MatrixUtils.createRealVector(new double[] {  1, -1,  1, 1 })
 		};
 
 		// Transform view frustum into camera space
 		for (int i = 0; i < 6; ++i) {
 			f[i] = invProjection.operate(f[i]);
 			f[i].mapDivideToSelf(f[i].getEntry(3));
 		}
 		
 		Vector3D[] frustumVertices = new Vector3D[6];
 		for (int i = 0; i < 6; ++i) {
 			frustumVertices[i] = new Vector3D(f[i].getEntry(0),
 					f[i].getEntry(1), f[i].getEntry(2));
 		}
 		
 		// Compute plane representation of view frustum planes		
 		Vector3D[] frustumNormals = new Vector3D[6];
 		double[] frustumOrigin = new double[6];
 
 		for (int i = 0; i < 6; ++i) {
 			Vector3D v1 = frustumVertices[(i + 1) % 6]
 					.subtract(frustumVertices[i]);
 			Vector3D v2 = frustumVertices[(i + 2) % 6]
 					.subtract(frustumVertices[i]);
 			frustumNormals[i] = Vector3D.crossProduct(v1, v2);
 			frustumOrigin[i] = -Vector3D.dotProduct(frustumNormals[i],
 					frustumVertices[i]);
 		}
 
 		// Draw each line
 		gl2.glUseProgram(program.program());
 		for (Line l : lines) {
 			
 			// All computations are made in camera space, so first
 			// transform the two points of the line into camera space
 			// by multiplying with the modelview matrix			
 			double[] tmp = modelView.operate(new double[] { l.p1.getX(),
 					l.p1.getY(), l.p1.getZ(), 1 });
 			Vector3D p1 = new Vector3D(tmp[0], tmp[1], tmp[2]);
 			tmp = modelView.operate(new double[] { l.p2.getX(), l.p2.getY(),
 					l.p2.getZ(), 1 });
 			Vector3D p2 = new Vector3D(tmp[0], tmp[1], tmp[2]); 
 			
 			// Compute orientation of the cylinder and its length, assuming
 			// a line segment is about to be drawn
 			Vector3D direction = p2.subtract(p1);
 			double cylinderLength = direction.getNorm();
 			direction = direction.normalize();
 
 			gl2.glUniform3f(originLoc, (float) p1.getX(), (float) p1.getY(),
 					(float) p1.getZ());
 			gl2.glUniform3f(directionLoc, (float) direction.getX(),
 					(float) direction.getY(), (float) direction.getZ());
 
 			// In case, no line segment should be drawn, a ray or line is drawn
 			// So the intersection points with the view frustum are computed
 			if (l.lineType != LineType.SEGMENT) {
 				double min = Double.MAX_VALUE;
 				double max = Double.MIN_VALUE;
 				for (int i = 0; i < 6; ++i) {
 					double lambda = linePlaneIntersection(p1, direction,
 							frustumNormals[i], frustumOrigin[i]);
 					if (lambda == Double.MAX_VALUE)
 						continue;
 					else {
 						min = Math.min(min, lambda);
 						max = Math.max(max, lambda);
 					}
 				}
 
 				// For each line or ray, the second point is to be shifted to
 				// infinity. As we can only see the ray/line until it leaves
 				// the view frustum, the point is shifted to the
 				// ray/line-frustum intersection, with maximum distance to p1
 				cylinderLength = 0;
 				p2 = new Vector3D(1, p1, max, direction);
 				if (l.lineType == LineType.LINE) {
 					// In case we want to draw a line, the first point should
 					// be shifted to infinity as well, here, it is shifted to
 					// the minimum intersection point
 					cylinderLength = -1;
 					p1 = p1.add(min, direction);
 				}
 			}
 
			// After defining shifted the end points of the ray/line to the
			// maximal visible positions, he boundbox is needed
 			
 			// Length of the OBB
 			double dist = Vector3D.distance(p1, p2) / 2.0;
 			//double dist = Math.max(Vector3D.distance(p1, p2), 2.0 * l.radius) / 2.0;
 			
 			//Vector3D axis = p1.subtract(p2);
 			//Vector3D axis = direction;
 			
 			// Midpoint of OBB
 			Vector3D avg = new Vector3D(0.5, p1, 0.5, p2);
 			
 			// Translate OBB's center to origin
 			RealMatrix translationMatrix = MatrixUtils
 					.createRealIdentityMatrix(4);
 			translationMatrix.setColumn(3,
 					new double[] { avg.getX(), avg.getY(), avg.getZ(), 1 });
 			
 			// Rotate x-axis to OBB main direction
 			Rotation rotation = new Rotation(Vector3D.PLUS_I, direction);
 			RealMatrix rotationMatrix = MatrixUtils.createRealIdentityMatrix(4);
 			rotationMatrix.setSubMatrix(rotation.getMatrix(), 0, 0);
 			
 			// Scale OBB to fit the size of the segment/ray/line
 			RealMatrix scaleMatrix = MatrixUtils
 					.createRealDiagonalMatrix(new double[] { dist, l.radius,
 							l.radius, 1 });
 			
 			// Compose the final transformation matrix for [-1,1]^3 by first
 			// scaling the OBB, then rotating it and finaling translating it
 			// into the final the line fitting position
 			RealMatrix cylinder = translationMatrix.multiply(rotationMatrix)
 					.multiply(scaleMatrix);
 			
 			gl2.glUniformMatrix4fv(transformLoc, 1, true,
 					Util.matrixToFloatArray(cylinder), 0);
 
 			// Draw [-1,1]^3 cube which is transformed into the OBB during
 			// processing the vertices on gpu
 			gl2.glUniform1f(lengthLoc, (float) cylinderLength);
 			gl2.glUniform1f(radiusLoc, (float) l.radius);
 			gl2.glUniform3fv(colorLoc, 1, l.color.getColorComponents(null), 0);
 			//gl2.glFlush();
 			gl2.glBegin(GL2.GL_QUADS);
 				gl2.glVertex3d(-1, -1, -1);
 				gl2.glVertex3d(-1, -1, 1);
 				gl2.glVertex3d(-1, 1, 1);
 				gl2.glVertex3d(-1, 1, -1);
 	
 				gl2.glVertex3d(1, -1, 1);
 				gl2.glVertex3d(1, -1, -1);
 				gl2.glVertex3d(1, 1, -1);
 				gl2.glVertex3d(1, 1, 1);
 	
 				gl2.glVertex3d(1, -1, 1);
 				gl2.glVertex3d(-1, -1, 1);
 				gl2.glVertex3d(-1, -1, -1);
 				gl2.glVertex3d(1, -1, -1);
 	
 				gl2.glVertex3d(-1, 1, 1);
 				gl2.glVertex3d(1, 1, 1);
 				gl2.glVertex3d(1, 1, -1);
 				gl2.glVertex3d(-1, 1, -1);
 	
 				gl2.glVertex3d(1, 1, 1);
 				gl2.glVertex3d(-1, 1, 1);
 				gl2.glVertex3d(-1, -1, 1);
 				gl2.glVertex3d(1, -1, 1);
 	
 				gl2.glVertex3d(-1, -1, -1);
 				gl2.glVertex3d(-1, 1, -1);
 				gl2.glVertex3d(1, 1, -1);
 				gl2.glVertex3d(1, -1, -1);
 			gl2.glEnd();
 		}
 		gl2.glUseProgram(0);
 	}
 
 	private double linePlaneIntersection(Vector3D p1, Vector3D direction,
 			Vector3D normal, double distance) {
 		double denom = Vector3D.dotProduct(direction, normal);
 		if (Math.abs(denom) < 10E-8)
 			return Double.MAX_VALUE;
 		
 		double lambda = -(Vector3D.dotProduct(p1, normal) + distance) / denom;
 		return lambda;
 	}
 
 }

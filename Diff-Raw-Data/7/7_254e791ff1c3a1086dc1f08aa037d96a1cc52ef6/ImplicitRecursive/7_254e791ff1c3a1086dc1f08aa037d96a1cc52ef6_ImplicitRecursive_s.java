 package munk.graph.function;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.j3d.Shape3D;
 import javax.vecmath.Point3f;
 
 import munk.graph.marching.MarchingCubes;
 import munk.graph.marching.Triangle;
 
 import com.sun.j3d.utils.geometry.GeometryInfo;
 import com.sun.j3d.utils.geometry.NormalGenerator;
 
 public class ImplicitRecursive {
 	
 	private float xMin;
 	private int xLength;
 	
 	private float yMin;
 	private int yLength;
 	
 	private float zMin;
 	private int zLength;
 	
 	private float stepsize;
 	private float xStepsize;
 	private float yStepsize;
 	private float zStepsize;
 	
 	private int startX;
 	private int startY;
 	private int startZ;
 	
 	private Triangle[] newTriangle;
 	private List<Point3f> triangles = new ArrayList<Point3f>(3000);
 	private boolean[][][] visited;
 	
 	private static final MarchingCubes MARCHER = new MarchingCubes();
 	private static final float ISOLEVEL = 0;
 	
 	private Shape3D plot;
 	
 	private boolean debug = false;
 
 	public ImplicitRecursive(String expression, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax, float stepsize) {
 		this.xMin = xMin;
 		this.yMin = yMin;
 		this.zMin = zMin;
 		this.stepsize = stepsize;
 		
 		xStepsize = stepsize;
 		yStepsize = stepsize;
 		zStepsize = stepsize;
 		
 		xLength = (int) Math.ceil((xMax - xMin) / stepsize) + 1;
 		yLength = (int) Math.ceil((yMax - yMin) / stepsize) + 1;
 		zLength = (int) Math.ceil((zMax - zMin) / stepsize) + 1;
 		
 		visited = new boolean[zLength][yLength][xLength];
 	}
 	
 	public Shape3D getPlot() {
 		if (plot == null)
 			plot = plot();
 		return plot;
 	}
 	
 	public Shape3D plot() {
 		Point3f startCube =  findStartCube();
 		System.out.println(startCube);
 		if (startCube != null)
 			marchCubes(startCube);
 		else return null;
 
 		// Build geometry from triangles
 		if (triangles.size() >= 3) {
 			GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
 			Point3f[] points = new Point3f[triangles.size()];
 			gi.setCoordinates((Point3f[]) triangles.toArray(points));
 			NormalGenerator ng = new NormalGenerator();
 			ng.generateNormals(gi);
 			return new Shape3D(gi.getGeometryArray());
 		} else 
 			return null;
 	}
 	private void marchCubes(Point3f startcube) {
 		Point3f[] corners = initPoint3fArray(8);
 		float[] values = new float[8];
 		newTriangle = initTriangleArray(MarchingCubes.MAX_TRIANGLES_RETURNED);
 		
 		corners[0].x = startcube.x;
 		corners[0].y = startcube.y;
 		corners[0].z = startcube.z;
 		values[0] = value(corners[0]);
 		
 		corners[1].x = startcube.x + xStepsize;
 		corners[1].y = startcube.y;
 		corners[1].z = startcube.z; 
 		values[1] = value(corners[1]);
 		
 		corners[2].x = startcube.x + xStepsize;
 		corners[2].y = startcube.y + yStepsize;
 		corners[2].z = startcube.z;
 		values[2] = value(corners[2]);
 		
 		corners[3].x = startcube.x;
 		corners[3].y = startcube.y + yStepsize;
 		corners[3].z = startcube.z;
 		values[3] = value(corners[3]);
 		
 		corners[4].x = startcube.x;
 		corners[4].y = startcube.y;
 		corners[4].z = startcube.z + zStepsize;
 		values[4] = value(corners[4]);
 		
 		corners[5].x = startcube.x + xStepsize;
 		corners[5].y = startcube.y;
 		corners[5].z = startcube.z + zStepsize;
 		values[5] = value(corners[5]);
 		
 		corners[6].x = startcube.x + xStepsize;
 		corners[6].y = startcube.y + yStepsize;
 		corners[6].z = startcube.z + zStepsize;
 		values[6] = value(corners[6]);
 		
 		corners[7].x = startcube.x;
 		corners[7].y = startcube.y + yStepsize;
 		corners[7].z = startcube.z + zStepsize;
 		values[7] = value(corners[7]);
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0)
 			addTriangles(nTriangles);
 		
 		
 		marchFace0(values, corners, startX, startY - 1, startZ);
 		marchFace1(values, corners, startX + 1, startY, startZ);
 		marchFace2(values, corners, startX, startY + 1, startZ);
 		marchFace3(values, corners, startX - 1, startY, startZ);
 		marchFace4(values, corners, startX, startY, startZ + 1);
 		marchFace5(values, corners, startX, startY, startZ - 1);
 	}
 	
 	public Point3f findStartCube() {
 		float[] values = new float[8];
 		float z = zMin;
 		
 		float[][] lower = bottomLayerValues();
 		float[][] upper = new float[yLength][xLength];
 		
 		for (int k = 0; k < zLength - 1; k++) {
 		
 			calcEdges(upper, z+stepsize);
 			float y = yMin;
 			for (int j = 0; j < yLength - 1; j++) {
 				
 				float x = xMin;
 				for (int i = 0; i < xLength - 1; i++) {
 					values[0] = lower[j][i];
 					
 					values[1] = lower[j][i+1];
 					
 					values[2] = lower[j+1][i+1];
 					
 					values[3] = lower[j+1][i];
 					
 					values[4] = upper[j][i];
 					
 					values[5] = upper[j][i+1];
 					
 					values[6] = value(x + stepsize, y + stepsize, z + stepsize);
 					
 					values[7] = upper[j+1][i];	
 					
 					upper[j+1][i+1] = values[6];
 					
 					if (MarchingCubes.isInside(values, ISOLEVEL)) {
 						startX = i;
 						startY = j;
 						startZ = k;
 						return new Point3f(x, y, z);
 					}
 					
 					x += stepsize;
 				}
 				
 				y += stepsize;
 			}
 			
 			float[][] tmp = upper;
 			upper = lower;
 			lower = tmp;
 			
 			z += stepsize;
 		}
 			
 		
 		return null;
 	}
 	
 	void marchFace0(float[] prevValues, Point3f[] prevCorners, int x, int y, int z) {
 		if (debug)
 			System.out.println("0:  " + "[" + x + ", " + y + ", " + z + "]");
 		if (!validPosition(x, y, z) || isVisited(x, y, z))
 			return;
 		
 		markAsVisited(x, y, z);
 		float[] values = new float[8];
 		values[2] = prevValues[1];
 		values[3] = prevValues[0];
 		values[6] = prevValues[5];
 		values[7] = prevValues[4];
 		
 		Point3f[] corners = new Point3f[8];
 		corners[2] = prevCorners[1];
 		corners[3] = prevCorners[0];
 		corners[6] = prevCorners[5];
 		corners[7] = prevCorners[4];
 		
 		Point3f corner0 = new Point3f(corners[3]);
 		corner0.y -= yStepsize;
 		corners[0] = corner0;
 		Point3f corner1 = new Point3f(corners[2]);
 		corner1.y -= yStepsize;
 		corners[1] = corner1;
 		Point3f corner4 = new Point3f(corners[7]);
 		corner4.y -= yStepsize;
 		corners[4] = corner4;
 		Point3f corner5 = new Point3f(corners[6]);
 		corner5.y -= yStepsize;
 		corners[5] = corner5;
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0) {
 			addTriangles(nTriangles);
 
 			//	marchFace0(values, corners, x, y - 1, z);
 			marchFace1(values, corners, x + 1, y, z);
 			marchFace2(values, corners, x, y + 1, z);
 			marchFace3(values, corners, x - 1, y, z);
 			marchFace4(values, corners, x, y, z + 1);
 			marchFace5(values, corners, x, y, z - 1);
 		}
 	}
 	
 	void marchFace1(float[] prevValues, Point3f[] prevCorners, int x, int y, int z) {
 		if (debug)
 			System.out.println("1:  " + "[" + x + ", " + y + ", " + z + "]");
 		if (!validPosition(x, y, z) || isVisited(x, y, z))
 			return;
 		
 		markAsVisited(x, y, z);
 		float[] values = new float[8];
 		values[0] = prevValues[1];
 		values[3] = prevValues[2];
 		values[4] = prevValues[5];
 		values[7] = prevValues[6];
 		
 		Point3f[] corners = new Point3f[8];
 		corners[0] = prevCorners[1];
 		corners[3] = prevCorners[2];
 		corners[4] = prevCorners[5];
 		corners[7] = prevCorners[6];
 		
 		Point3f corner1 = new Point3f(corners[0]);
 		corner1.x += xStepsize;
 		corners[1] = corner1;
 		values[1] = value(corner1);
 		
 		Point3f corner2 = new Point3f(corners[3]);
 		corner2.x += xStepsize;
 		corners[2] = corner2;
 		values[2] = value(corner2);
 		
 		Point3f corner5 = new Point3f(corners[4]);
 		corner5.x += xStepsize;
 		corners[5] = corner5;
 		values[5] = value(corner5);
 		
 		Point3f corner6 = new Point3f(corners[7]);
 		corner6.x += xStepsize;
 		corners[6] = corner6;
 		values[6] = value(corner6);
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0) {
 			addTriangles(nTriangles);
 
 			marchFace0(values, corners, x, y - 1, z);
 //			marchFace1(values, corners, x + 1, y, z);
 			marchFace2(values, corners, x, y + 1, z);
 			marchFace3(values, corners, x - 1, y, z);
 			marchFace4(values, corners, x, y, z + 1);
 			marchFace5(values, corners, x, y, z - 1);
 		}
 	}
 	
 	void marchFace2(float[] prevValues, Point3f[] prevCorners, int x, int y, int z) {
 		if (debug)
 			System.out.println("2:  " + "[" + x + ", " + y + ", " + z + "]");
 		if (!validPosition(x, y, z) || isVisited(x, y, z))
 			return;
 		
 		markAsVisited(x, y, z);
 		float[] values = new float[8];
 		values[1] = prevValues[2];
 		values[0] = prevValues[3];
 		values[4] = prevValues[7];
 		values[5] = prevValues[6];
 		
 		Point3f[] corners = new Point3f[8];
 		corners[1] = prevCorners[2];
 		corners[0] = prevCorners[3];
 		corners[4] = prevCorners[7];
 		corners[5] = prevCorners[6];
 		
 		Point3f corner2 = new Point3f(corners[1]);
 		corner2.y += xStepsize;
 		corners[2] = corner2;
 		values[2] = value(corner2);
 		
 		Point3f corner3 = new Point3f(corners[0]);
 		corner3.y += xStepsize;
 		corners[3] = corner3;
 		values[3] = value(corner3);
 		
 		Point3f corner6 = new Point3f(corners[5]);
 		corner6.y += xStepsize;
 		corners[6] = corner6;
 		values[6] = value(corner6);
 		
 		Point3f corner7 = new Point3f(corners[4]);
 		corner7.y += xStepsize;
 		corners[7] = corner7;
 		values[7] = value(corner7);
 		
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0) {
 			addTriangles(nTriangles);
 
 			marchFace0(values, corners, x, y - 1, z);
 			marchFace1(values, corners, x + 1, y, z);
 //			marchFace2(values, corners, x, y + 1, z);
 			marchFace3(values, corners, x - 1, y, z);
 			marchFace4(values, corners, x, y, z + 1);
 			marchFace5(values, corners, x, y, z - 1);
 		}
 	}
 	
 	void marchFace3(float[] prevValues, Point3f[] prevCorners, int x, int y, int z) {
 		if (debug)
 			System.out.println("3:  " + "[" + x + ", " + y + ", " + z + "]");
 		if (!validPosition(x, y, z) || isVisited(x, y, z))
 			return;
 		
 		markAsVisited(x, y, z);
 		float[] values = new float[8];
 		values[1] = prevValues[0];
 		values[2] = prevValues[3];
 		values[5] = prevValues[4];
 		values[6] = prevValues[7];
 		
 		Point3f[] corners = new Point3f[8];
 		corners[1] = prevCorners[0];
 		corners[2] = prevCorners[3];
 		corners[5] = prevCorners[4];
 		corners[6] = prevCorners[7];
 		
 		Point3f corner0 = new Point3f(corners[1]);
 		corner0.x -= xStepsize;
 		corners[0] = corner0;
 		values[0] = value(corner0);
 		
 		Point3f corner3 = new Point3f(corners[2]);
 		corner3.x -= xStepsize;
 		corners[3] = corner3;
 		values[3] = value(corner3);
 		
 		Point3f corner4 = new Point3f(corners[5]);
 		corner4.x -= xStepsize;
 		corners[4] = corner4;
 		values[4] = value(corner4);
 		
 		Point3f corner7 = new Point3f(corners[6]);
 		corner7.x -= xStepsize;
 		corners[7] = corner7;
 		values[7] = value(corner7);
 		
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0) {
 			addTriangles(nTriangles);
 
 			marchFace0(values, corners, x, y - 1, z);
 			marchFace1(values, corners, x + 1, y, z);
 			marchFace2(values, corners, x, y + 1, z);
 //			marchFace3(values, corners, x - 1, y, z);
 			marchFace4(values, corners, x, y, z + 1);
 			marchFace5(values, corners, x, y, z - 1);
 		}
 	}
 	
 	void marchFace4(float[] prevValues, Point3f[] prevCorners, int x, int y, int z) {
 		if (debug)
 			System.out.println("4:  " + "[" + x + ", " + y + ", " + z + "]");
 		if (!validPosition(x, y, z) || isVisited(x, y, z))
 			return;
 		
 		markAsVisited(x, y, z);
 		float[] values = new float[8];
 		values[0] = prevValues[4];
 		values[1] = prevValues[5];
 		values[2] = prevValues[6];
 		values[3] = prevValues[7];
 		
 		Point3f[] corners = new Point3f[8];
 		corners[0] = prevCorners[4];
 		corners[1] = prevCorners[5];
 		corners[2] = prevCorners[6];
 		corners[3] = prevCorners[7];
 		
 		Point3f corner4 = new Point3f(corners[0]);
 		corner4.z += zStepsize;
 		corners[4] = corner4;
 		values[4] = value(corner4);
 		
 		Point3f corner5 = new Point3f(corners[1]);
 		corner5.z += zStepsize;
 		corners[5] = corner5;
 		values[5] = value(corner5);
 		
 		Point3f corner6 = new Point3f(corners[2]);
 		corner6.z += zStepsize;
 		corners[6] = corner6;
 		values[6] = value(corner6);
 		
 		Point3f corner7 = new Point3f(corners[3]);
 		corner7.z += zStepsize;
 		corners[7] = corner7;
 		values[7] = value(corner7);
 		
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0) {
 			addTriangles(nTriangles);
 
 			marchFace0(values, corners, x, y - 1, z);
 			marchFace1(values, corners, x + 1, y, z);
 			marchFace2(values, corners, x, y + 1, z);
 			marchFace3(values, corners, x - 1, y, z);
 //			marchFace4(values, corners, x, y, z + 1);
 			marchFace5(values, corners, x, y, z - 1);
 		}
 	}
 	
 	void marchFace5(float[] prevValues, Point3f[] prevCorners, int x, int y, int z) {
 		if (debug)
 			System.out.println("5:  " + "[" + x + ", " + y + ", " + z + "]");
 		if (!validPosition(x, y, z) || isVisited(x, y, z))
 			return;
 		
 		markAsVisited(x, y, z);
 		float[] values = new float[8];
 		values[4] = prevValues[0];
 		values[5] = prevValues[1];
 		values[6] = prevValues[2];
 		values[7] = prevValues[3];
 		
 		Point3f[] corners = new Point3f[8];
 		corners[4] = prevCorners[0];
 		corners[5] = prevCorners[1];
 		corners[6] = prevCorners[2];
 		corners[7] = prevCorners[3];
 		
 		Point3f corner0 = new Point3f(corners[4]);
 		corner0.z -= zStepsize;
 		corners[0] = corner0;
 		values[0] = value(corner0);
 		
 		Point3f corner1 = new Point3f(corners[5]);
 		corner1.z -= zStepsize;
 		corners[1] = corner1;
 		values[1] = value(corner1);
 		
 		Point3f corner2 = new Point3f(corners[6]);
 		corner2.z -= zStepsize;
 		corners[2] = corner2;
 		values[2] = value(corner2);
 		
 		Point3f corner3 = new Point3f(corners[7]);
 		corner3.z -= zStepsize;
 		corners[3] = corner3;
 		values[3] = value(corner3);
 		
 		
 		int nTriangles = MARCHER.marchCube(values, corners, newTriangle, ISOLEVEL);
 		if (nTriangles > 0) {
 			addTriangles(nTriangles);
 
 			marchFace0(values, corners, x, y - 1, z);
 			marchFace1(values, corners, x + 1, y, z);
 			marchFace2(values, corners, x, y + 1, z);
 			marchFace3(values, corners, x - 1, y, z);
 			marchFace4(values, corners, x, y, z + 1);
 //			marchFace5(values, corners, x, y, z - 1);
 		}
 	}
 	
 	private boolean validPosition(int x, int y, int z) {
 		return (x >= 0 && x < xLength) && (y >= 0 && y < yLength) && (z >= 0 && z < zLength);
 	}
 
 	private void markAsVisited(int x, int y, int z) {
 		visited[z][y][x] = true;		
 	}
 
 	private boolean isVisited(int x, int y, int z) {
 		return visited[z][y][x];
 	}
 	
 	private float[][] bottomLayerValues() {
 		float[][] result = new float[yLength][xLength];
 		
 		float y = yMin;
 		for (int j = 0; j < yLength; j++) {
 			float x = xMin;
 			for (int i = 0; i < xLength; i++) {
 				result[j][i] = value(x, y, zMin);
 				x += stepsize;
 			}
 			y += stepsize;
 		}
 		
 		return result;
 	}
 	
 	private void calcEdges(float[][] upperValues, float z) {
 		float y = yMin;
 		float x = xMin;
 		for (int j = 0; j < upperValues.length; j++) {
 			upperValues[j][0] = value(x, y, z);
 			y += stepsize;
 		}
 		
 		y = yMin;
 		for (int i = 0; i < upperValues[0].length; i++) {
 			upperValues[0][i] = value(x, y, z);
 			x += stepsize;
 		}
 	}
 	
 	private float value(float x, float y, float z) {
 //		return x*x + y*y + z*z - 1;
 		return (float) (z*z - x*(Math.cos(y)*Math.cos(x)));
 	}
 	
 	private float value(Point3f point) {
 		return value(point.x, point.y, point.z);
 	}
 	
 	private Triangle[] initTriangleArray(int N) {
 		Triangle[] result = new Triangle[N];
 		for (int q = 0; q < N; q++) {
 			result[q] = new Triangle();
 		}
 		return result;
 	}
 	
 	private void addTriangles(int nFacets) {
 		System.out.println(nFacets);
 		for (int q = 0; q < nFacets; q++) {
 			addVerticesToList(newTriangle[q], triangles);
 		}
 	}
 	
 	private void addVerticesToList(Triangle tri, List<Point3f> list) {
 		for (Point3f vertex : tri) {
 			Point3f newVertex = (Point3f) vertex.clone();
 			list.add(newVertex);
 		}
 	}
 	
 	private Point3f[] initPoint3fArray(int N) {
 		Point3f[] result = new Point3f[N];
 		for (int q = 0; q < N; q++) {
 			result[q] = new Point3f();
 		}
 		return result;
 	}
 	
 }

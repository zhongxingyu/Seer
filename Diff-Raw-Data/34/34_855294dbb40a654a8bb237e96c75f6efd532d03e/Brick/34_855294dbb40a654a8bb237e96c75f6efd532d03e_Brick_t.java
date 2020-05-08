 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brick.math;
 
 import brick.image.TransformsChangeNotifyer;
 import brick.image.Wall;
 import java.awt.Graphics2D;
 
 /**
  *
  *         0-{-1, 1, 1}-------3-{ 1, 1, 1}
  *        /|                 /|
  *       / |                / |
  *      /  |               /  |
  *     /   |              /   |
  *    1-{-1, 1,-1}-------2-{ 1, 1,-1}
  *    |    |             |    |
  *    |    |             |    |
  *    |    4-{-1,-1, 1}--|----7-{ 1, -1, 1}
  *    |   /              |   /
  *    |  /               |  /
  *    | /                | /
  *    |/                 |/
  *    5-{-1,-1,-1}-------6-{ 1,-1,-1}
  * 
  *     0---3
  *     | 4 |
  * 0---1---2---3---0
  * | 0 | 1 | 2 | 3 |
  * 4---5---6---7---4
  *	   | 5 |
  *     4---7
  *
  * @author Szymon
  */
 public class Brick implements TransformsChangeNotifyer {
 
 	public static double dgToRad = (double) Math.PI / 180;
 	public int[] buff;
 	private Matrix4x4 transform = new Matrix4x4();
 	private Matrix4x4 scale = new Matrix4x4();
 	private Matrix4x4 rotX = new Matrix4x4();
 	private Matrix4x4 rotY = new Matrix4x4();
 	private Matrix4x4 rotZ = new Matrix4x4();
 	private Matrix4x4 endMatrix = new Matrix4x4();
 	private Matrix4x4 to2DMatrix = new Matrix4x4();
 	Wall[] walls;
 	private static final double SIZE = 100;
 	private static final double[][] CORNERS = {
 		/* 0 */{-SIZE, SIZE, SIZE, 1},
 		/* 1 */ {-SIZE, SIZE, -SIZE, 1},
 		/* 2 */ {SIZE, SIZE, -SIZE, 1},
 		/* 3 */ {SIZE, SIZE, SIZE, 1},
 		/* 4 */ {-SIZE, -SIZE, SIZE, 1},
 		/* 5 */ {-SIZE, -SIZE, -SIZE, 1},
 		/* 6 */ {SIZE, -SIZE, -SIZE, 1},
 		/* 7 */ {SIZE, -SIZE, SIZE, 1}
 	};
 	private final Matrix1x4[] originalCorners3D = new Matrix1x4[8];
 	private final Matrix1x4[] corners3D = new Matrix1x4[8];
 	private final int[][] corners2D = new int[8][2];
 	final boolean[] visible = {true, true, true, true, true, true};
 
 	public Brick() {
 		System.out.println("Tworze kostke");
 		for (int i = 0; i < 8; ++i) {
 			originalCorners3D[i] = new Matrix1x4(CORNERS[i]);
 		}
 		to2DMatrix.data[2][2] = 0;
 		Wall[] tmpWalls = {new Wall(), new Wall(), new Wall(), new Wall(), new Wall(), new Wall()};
 		setWalls(tmpWalls);
 		recalc();
 	}
 
 	public void setWalls(Wall[] walls) {
 		if (walls == null) {
 			return;
 		}
 
 		if (walls.length != 6) {
 			throw new ArrayIndexOutOfBoundsException("Wrong number of walls: " + walls.length);
 		}
 
 		this.walls = walls;
 		/*     0---3
 		 *     | 4 |
 		 * 0---1---2---3---0
 		 * | 0 | 1 | 2 | 3 |
 		 * 4---5---6---7---4
 		 *	   | 5 |
 		 *     4---7
 		 */
 		walls[0].setCorners(corners2D[0], corners2D[1], corners2D[5], corners2D[4]);
 		walls[1].setCorners(corners2D[1], corners2D[2], corners2D[6], corners2D[5]);
 		walls[2].setCorners(corners2D[2], corners2D[3], corners2D[7], corners2D[6]);
 		walls[3].setCorners(corners2D[3], corners2D[0], corners2D[4], corners2D[7]);
 		walls[4].setCorners(corners2D[0], corners2D[3], corners2D[2], corners2D[1]);
 		walls[5].setCorners(corners2D[5], corners2D[6], corners2D[7], corners2D[4]);
 	}
 
 	public void paint(Graphics2D g, int width, int height) {
 		for (int i = 0; i < 6; ++i) {
 			if (visible[i]) {
 				walls[i].paint(g, width, height);
 			}
 		}
 //		for (int[] c1 : corners2D) {
 //			for(int[] c2: corners2D) {
 //				g.drawLine(c1[0] + width/2, c1[1] + height/2, c2[0] + width/2, c2[1] + height/2);
 //			}
 //		}
 	}
 
 	private void calcCorners() {
 		Matrix1x4 d3, d2;
 		int[] corner;
 		
 		for (int i = 0; i < 8; ++i) {
 			d3 = originalCorners3D[i].product(endMatrix);
 			corners3D[i] = d3;
 			d2 = d3.product(to2DMatrix);
 			System.out.println("Corner: " + i + ", 3D: " + d3 + "\n           2D: " + d2);
 			corner = corners2D[i];
 			double focalPointFactor = (double) screenDistance / (screenDistance + brickDistance + d3.data[2]);
 			corner[0] = (int) (focalPointFactor * d2.data[0]);
 			corner[1] = (int) (focalPointFactor * d2.data[1]);
 			System.out.println("x: " + corner[0] + " y: " + corner[1]);
 		}
 	}
 
 	public void recalc() {
 		endMatrix = transform.product(rotX.product(rotY.product(rotZ.product(scale))));
 		System.out.println("    scale: " + scale);
 		System.out.println("     rotZ: " + rotZ);
 		System.out.println("     rotY: " + rotY);
 		System.out.println("     rotX: " + rotX);
 		System.out.println("transform: " + transform);
 		System.out.println("endMatrix: " + endMatrix);
 		calcCorners();
 		// TODO: określanie które ściany mają być widoczne?
 	}
 
 	private int index(int which) {
 		int index = -1;
 		switch (which) {
 			case X:
 				index = 0;
 				break;
 			case Y:
 				index = 1;
 				break;
 			case Z:
 				index = 2;
 				break;
 			default:
 				throw new ArrayIndexOutOfBoundsException("Bad axis given.");
 		}
 		return index;
 	}
 
 	public void setTransform(int which, double value) {
 		transform.data[index(which)][3] = value;
 
 	}
 
 	public void setAngle(int which, double value) {
 		value *= dgToRad;
 		double sin = Math.sin(value);
 		double cos = Math.cos(value);
 		switch (which) {
 			case X: {
 				double[][] tmp = {
 					{1, 0, 0, 0},
 					{0, cos, sin, 0},
 					{0, -sin, cos, 0},
 					{0, 0, 0, 1}
 				};
 				rotX.data = tmp;
 				break;
 			}
 			case Y: {
 				double[][] tmp = {
 					{cos, 0, -sin, 0},
 					{0, 1, 0, 0},
 					{sin, 0, cos, 0},
 					{0, 0, 0, 1}
 				};
 				rotY.data = tmp;
 				break;
 			}
 			case Z: {
 				double[][] tmp = {
 					{cos, sin, 0, 0},
 					{-sin, cos, 0, 0},
					{0, 0, 1, 0},
					{0, 0, 0, 1}
 				};
 				rotZ.data = tmp;
 				break;
 			}
 			default:
 				throw new ArrayIndexOutOfBoundsException("Bad axis given.");
 		}
 		//recalc();
 	}
 
 	public void setScale(int which, double value) {
 		int index = index(which);
		
		scale.data[index][index] = value;
		
 		//recalc();
 	}
 
 	public Wall[] getWalls() {
 		return walls;
 	}
	protected int screenDistance = 300;
 
 	public int getScreenDistance() {
 		return screenDistance;
 	}
 
 	public void setScreenDistance(int screenDistance) {
 		this.screenDistance = screenDistance;
 		to2DMatrix.data[3][2] = 1d / screenDistance;
 
 	}
	protected int brickDistance = 300;
 
 	public int getBrickDistance() {
 		return brickDistance;
 	}
 
 	public void setBrickDistance(int brickDistance) {
 		this.brickDistance = brickDistance;
 	}
 }

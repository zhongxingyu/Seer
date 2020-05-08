 package cube;
 
 import java.util.ArrayList;
 import objectTypes.Vector3D;
 import core.MatrixTranslator;
 
 
 /**
  * The graphical interpretation of a Cube in three dimensions
  * @author Johan
  *     _____________________
  *    |                     |
  *    |                     |
  *    |                     |
  *    |                     |
  *    |  / \    4           |
  *    |   |                 |
  *    | up|                 |
  *    |   |                 |
  *    |_____________________|_______________________________________________________________
  *    |                     |                     |                    |                    |
  *    |                     |                     |                    |                    |
  *    |                     |                     |                    |                    |
  *    |    / \     0        |      / \ 1          |  / \   2           |  / \     3         |
  *    |     |               |       |             |   |                |   |                |
  *    |   up|               |     up|             |   |up              | up|                |
  *    |     |               |       |             |   |                |   |                |
  *    |                     |                     |                    |                    |
  *    |_____________________|_____________________|____________________|____________________|
  *    |                     |
  *    |                     |
  *    |                     |
  *    |  / \                |
  *    | up|      5          |
  *    |   |                 |
  *    |   |                 |
  *    |                     |
  *    |_____________________|
  *    
  */
 public class Cube {
 	//This is not measured in actual pixels!
 	private static final float SIDEWIDTH=300.0f;
 	private static final float RADIUS=SIDEWIDTH/2;
 	private Side[] sides=new Side[6];
 	private int squaresPerSide;
 	private float rotX, rotY, rotZ;
 
 	/**
 	 * Initializes the cube divided into 6 sides with 
 	 * sideWidth*sideWidth number of squares on each side
 	 * @param sideWidth The number of squares 
 	 */
 	public Cube(int squaresPerSide){
 		if (squaresPerSide<1) {
 			throw new IllegalArgumentException("The cube must have at least one field per side!");
 		}
 		setRotX(0);
 		setRotY(0);
 		setRotZ(0);
 
 		this.squaresPerSide=squaresPerSide;
 		//Creating sides
 		sides[0]=new Side(new Vector3D(1, 0, 0), new Vector3D(0, 0, 1));
 		sides[1]=new Side(new Vector3D(0, 1, 0), new Vector3D(0, 0, 1));
 		sides[2]=new Side(new Vector3D(-1, 0, 0),new Vector3D(0, 0, 1));
 		sides[3]=new Side(new Vector3D(0, -1, 0), new Vector3D(0, 0, 1));
 		sides[4]=new Side(new Vector3D(0, 0, 1),new Vector3D(-1, 0, 0));
 		sides[5]=new Side(new Vector3D(0, 0, -1), new Vector3D(1, 0, 0));
 		//Now we just have to manually merge the different sides together according to the crude ASCII-art above
 		sides[0].mergeEdge(Direction.RIGHT, sides[1]);
 		sides[1].mergeEdge(Direction.RIGHT, sides[2]);
 		sides[2].mergeEdge(Direction.RIGHT, sides[3]);
 		sides[3].mergeEdge(Direction.RIGHT, sides[0]);
 
 		sides[5].mergeEdge(Direction.RIGHT, sides[1]);
 		sides[5].mergeEdge(Direction.UP, sides[0]);
 		sides[5].mergeEdge(Direction.DOWN, sides[2]);
 		sides[5].mergeEdge(Direction.LEFT, sides[3]);
 
 		sides[4].mergeEdge(Direction.DOWN, sides[0]);
 		sides[4].mergeEdge(Direction.RIGHT, sides[1]);
 		sides[4].mergeEdge(Direction.UP, sides[2]);
 		sides[4].mergeEdge(Direction.LEFT, sides[3]);
 	}
 	/**
 	 * Returns all renderable objects on the cube which are visible from the viewers perspective
 	 * @param mt The translator that is used during the rendering process
 	 * @return An ArrayList of Renderables 
 	 * 
 	 */
 	public ArrayList<Vector3D[]> getGrid(MatrixTranslator mt){
 		ArrayList<Vector3D[]> lines=new ArrayList<Vector3D[]>();
 		for (int i = 0; i < sides.length; i++) {
 			if (mt.sideVisible(sides[i].getCorners(),sides[i].getNormal())) {
 				lines.addAll(sides[i].getGrid());
 			}
 		}
 		return  lines;
 	}
 	/**
 	 * Returns an arraylist containing the pairs of 3D-vectors which
 	 * mark the lines forming the arrows
 	 * @param mt
 	 * @return
 	 */
 	public ArrayList<Vector3D[]> getArrows(MatrixTranslator mt){
 		ArrayList<Vector3D[]> arrows=new ArrayList<Vector3D[]>();
 		int l=sides.length;
 		for (int i = 0; i < l ; i++) {
 			if (mt.sideVisible(sides[i].getCorners(), sides[i].getNormal())) {
 				arrows.addAll(sides[i].getArrow());
 			}
 		}
 		return arrows;
 	}
 	/**
 	 * Gets a single square from one of the Cubes sides
 	 * @param Side is numbered from 0-5
 	 * @param x
 	 * @param y
 	 * @return a Square-object
 	 */
 	public Square getSquare(int side,int x,int y){
 		return sides[side].getSquare(x, y);
 	}
 	/**
 	 * Gets all visible squares
 	 * @param mt
 	 * @return
 	 */
 	public ArrayList<Square> getSquares(MatrixTranslator mt){
 		ArrayList<Square> squares=new ArrayList<Square>();
 		for (int i = 0; i < sides.length; i++) {
 			if (mt.sideVisible(sides[i].getCorners(), sides[i].getNormal())) {
 				squares.addAll(sides[i].getSquares());
 			}
 		}
 
 
 		return squares;
 	}
 
 	public void setRotX(float rotX) {
 		this.rotX = rotX;
 	}
 	public float getRotX() {
 		return rotX;
 	}
 
 	public void setRotY(float rotY) {
 		this.rotY = rotY;
 	}
 	public float getRotY() {
 		return rotY;
 	}
 
 	public void setRotZ(float rotZ) {
 		this.rotZ = rotZ;
 	}
 	public float getRotZ() {
 		return rotZ;
 	}
 
 	/**
 	 * A representation of a side
 	 * 
 	 * _____________________
 	 *|                     |
 	 *|       <-------      |
 	 *|        cross        |
 	 *|                     |
 	 *|  /|\     ________\  |
 	 *|   |      _|  n   /  |
 	 *|   |up               |
 	 *|                     |
 	 *|                     |
 	 *|_____________________|
 	 * The figure shows one side watched from the outside
 	 * cross points to the left on all sides
 	 * 
 	 */
 	private class Side{
 		//cross is the crossproduct of n and up and thus cross is 
 		//pointing to the left when looking at it from the outside
 		private final Vector3D n,up,cross;
 		private final Vector3D[] corners=new Vector3D[4];
 		private Square[][] squaregrid=new Square[squaresPerSide][squaresPerSide];
 
 		/**
 		 * Initializes a side in the Cube
 		 * @param sideWidth
 		 * @param normal MUST HAVE THE FORMAT (0 or 1,0 or 1,0 or 1) and have length 1
 		 * @param upDirection MUST HAVE SAME FORMAT AS NORMAL
 		 */
 		private Side(Vector3D normal, Vector3D upDirection){
 			checkVector(normal);
 			checkVector(upDirection);
 
 			n=normal;
 			up=upDirection;
 			cross=Vector3D.crossProduct(n, up);
 			initializeSquares();
 
 			//Setting corners in
 			//upper left
 			corners[0]=Vector3D.add(Vector3D.add(Vector3D.scalarMultiplication(n, RADIUS), Vector3D.scalarMultiplication(up, RADIUS)),Vector3D.scalarMultiplication(cross, RADIUS));
 			//upper right
 			corners[1]=Vector3D.add(corners[0],Vector3D.scalarMultiplication(cross, -2*RADIUS));
 			//lower right
 			corners[2]=Vector3D.add(corners[1], Vector3D.scalarMultiplication(up, -2*RADIUS));
 			//lower left
 			corners[3]=Vector3D.add(corners[2], Vector3D.scalarMultiplication(cross, 2*RADIUS));
 		}
 
 		/**
 		 * Initializes the squares containing all information on what's on the Cube
 		 */
 		private void initializeSquares(){
 
 			//One very long line of code which determines the upper left corner of the side
 			Vector3D startVector=Vector3D.add(Vector3D.add(Vector3D.scalarMultiplication(n, RADIUS),Vector3D.scalarMultiplication(up, RADIUS)),Vector3D.scalarMultiplication(cross, RADIUS));
 			Vector3D upperLeft=startVector;
 			//A vector pointing right with the length SIDEWIDTH/squaresPerSide
 			Vector3D horisontalSpacing= Vector3D.scalarMultiplication(cross, -SIDEWIDTH/squaresPerSide);
 			//A vector pointing down with the length SIDEWIDTH/squaresPerSide
 			Vector3D verticalSpacing=Vector3D.scalarMultiplication(up, -SIDEWIDTH/squaresPerSide);
 			Vector3D upperRight;
 			for (int y = 0; y < squaregrid.length; y++) {
 				upperLeft=Vector3D.add(startVector, Vector3D.scalarMultiplication(verticalSpacing, y));
 				for (int x = 0; x < squaregrid.length; x++) {
 					upperRight=Vector3D.add(upperLeft,horisontalSpacing);
 					squaregrid[x][y]=new Square(upperLeft, upperRight, Vector3D.add(upperRight,verticalSpacing),Vector3D.add(upperLeft,verticalSpacing));
 					if (x>0) {//If this isn't the leftmost Square, set the square to the left as a neighbor to this one
 						squaregrid[x][y].setNeighbor(Direction.LEFT, squaregrid[x-1][y], Direction.RIGHT);
 					}
 					if(y>0){//If this isn't the topmost Square, set the Square above this one as a neighbor
 						squaregrid[x][y].setNeighbor(Direction.UP, squaregrid[x][y-1], Direction.DOWN);
 					}
 					upperLeft=upperRight;
 				}
 			}
 
 		}
 		/**
 		 * Here comes the tricky bit, attempts to merge an entire edge of this Side
 		 * with an edge from another side. Especially tricky is the fact that the meaning of
 		 * "up" or "left" differs from side to side 
 		 * @param d The direction of the side to merge i.e. the upper,right,lower or left edge of this Side
 		 * @param otherSide The Side to merge to
 		 * @param otherD The direction of the other side's edge
 		 * 
 		 *    _______________
 		 *   |               |\
 		 *   | this          | \otherSide
 		 *   |       d(right)| _\
 		 *   |    ---------->||\|
 		 *   |               |  \ 
 		 *   |_______________|  |\otherD(left)
 		 *                    \ | \
 		 *                     \|  \
 		 *                      
 		 */
 		public void mergeEdge(Direction d,Side otherSide){
 			//Calculates which edge of the other Side that should be merged
 			Direction otherD=null;//The direction of the neighbors neighbor, that is which direction is towards this Side?
 			boolean reverseEdgeList=false;//Determines whether we have to reverse the order by which elements are merged
 			Vector3D crossProd=null;//defines the cross-product of the up-vectors of the two sides
 			//Here comes a monstrously long if-statement to determine which of the 16 possible cases
 			//is the one to apply on this case
 			if (d==Direction.RIGHT||d==Direction.LEFT) {
 				//right-left and left-right
 				if (this.up.equals(otherSide.up)) {//If this is the simplest of all merges, that is if a Square's right neighbor has the Square as it's left neighbor and vice versa
 					otherD=Direction.opposite(d);
 					//reverseEdgeList=false;
 				}//right-right and left-left
 				else if(this.up.equals(Vector3D.scalarMultiplication(otherSide.up, -1))){//If one of the sides is oriented "up side down" compared to the first.
 					otherD=d;
 					reverseEdgeList=true;
 				}else{//If it wasn't the neighbors right or left side
 					crossProd=Vector3D.crossProduct(this.up, otherSide.up);
 					//right-down and left-down
 					if (crossProd.equals(this.cross)) {//If the cross-product is pointing left
 						otherD=Direction.DOWN;
 						if (d==Direction.LEFT) {
 							reverseEdgeList=true;
 						}
 //						else{//d==right
 //							reverseEdgeList=false;
 //						}
 					}//right-up and left-up
 					else{//crossProd==-cross
 						otherD=Direction.UP;
						if (d==Direction.RIGHT) {
 							reverseEdgeList=true;
 						}
//						else{//d=left
 //							reverseEdgeList=false;
 //						}
 					}
 				}
 			}else{//If direction is UP or DOWN
 				crossProd=Vector3D.crossProduct(this.up, otherSide.up);
 				//down-right and up-left
 				if (crossProd.equals(this.n)) {
 					if (d==Direction.UP) {
 						reverseEdgeList=true;
 						otherD=Direction.LEFT;
 					}else{//d=DOWN
 						//reverseEdgeList=false;
 						otherD=Direction.RIGHT;
 					}
 				}//up-right and down left
 				else if(crossProd.equals(Vector3D.scalarMultiplication(n, -1))){
 					if (d==Direction.UP) {
 						reverseEdgeList=true;
 						otherD=Direction.RIGHT;
 					}else{//d=DOWN
 						//reverseEdgeList=false;
 						otherD=Direction.LEFT;
 					}
 				}//up-down and down-down
 				else if(crossProd.equals(this.cross)){
 					otherD=Direction.DOWN;
 					if (d==Direction.DOWN) {
 						reverseEdgeList=true;
 					}
 //					else{//d=UP
 //						reverseEdgeList=false;
 //					}
 				}else{//crossProd==-this.cross 
 					//up-up and down-up
 					otherD=Direction.UP;
 					if (d==Direction.UP) {
 						reverseEdgeList=true;
 					}
 //					else{//d=DOWN
 //						reverseEdgeList=false;
 //					}
 
 				}
 			}
 
 			Square[] edge1=this.getEdge(d,false);//This side's edge-array is NEVER reversed
 			Square[] edge2=otherSide.getEdge(otherD, reverseEdgeList);//The other Side's edge-array MIGHT be reversed
 			int l=edge1.length;
 			for (int i = 0; i < l; i++) {//merging squares...
 
 				edge1[i].setNeighbor(d, edge2[i], otherD);
 			}
 
 		}
 		/**
 		 * 
 		 * @param d the direction of the edge. I.e. left,right,up or down
 		 * @param reverse determines whether the array of Squares should be reversed prior to returning it.
 		 * @return An array containing all squares along the specified edge
 		 */
 		private Square[] getEdge(Direction d,boolean reverse){
 			Square[] temp;
 			switch (d) {
 			case LEFT:
 				temp= squaregrid[0];
 				break;
 			case RIGHT:
 				temp= squaregrid[squaresPerSide-1];
 				break;
 			case UP:
 				temp=new Square[squaresPerSide];//This will contain all Squares of the form squaregrid[x][0]
 				for (int x = 0; x < squaresPerSide; x++) {
 					temp[x]=squaregrid[x][0];
 				}
 				break;
 			case DOWN:
 				temp=new Square[squaresPerSide];//This will contain all Squares of the form squaregrid[x][squaresPerSide-1]
 				for (int x = 0; x < squaresPerSide; x++) {
 					temp[x]=squaregrid[x][squaresPerSide-1];
 				}
 				break;
 			default:
 				throw new IllegalArgumentException("We should never ever get here????");
 			}
 			if (reverse) {
 				int l=temp.length/2,l2=temp.length;
 				Square s;
 				for (int i = 0; i < l; i++) {//Reversing order...
 					s=temp[i];
 					temp[i]=temp[l2-1-i];
 					temp[l2-1-i]=s;
 				}
 			}
 			return temp;
 		}
 		/**
 		 * gets a specified square, defined in a coordinate system starting with 0,0 in the Side's upper left corner
 		 * @param x 
 		 * @param y
 		 */
 		public Square getSquare(int x,int y){
 			return squaregrid[x][y];
 		}
 
 
 		public ArrayList<Square> getSquares(){
 			ArrayList<Square> squares=new ArrayList<Square>();
 			for (int x = 0; x < squaregrid.length; x++) {
 				for (int y = 0; y < squaregrid[x].length; y++) {
 					squares.add(squaregrid[x][y]);
 				}
 			}
 			return squares;
 		}
         /**
          * Returns the corners in this order upper left,upper right,lower right,lower left
          * That is, in a circular fashion
          *   ----->
          *  /|\   |
          *   |    |
          *   |   \|/
          *   <-----     
          *@return an array containing 4 3D-vectors that represent the Side's corners
          */
 		public Vector3D[] getCorners(){
 			return corners;
 		}
 		/**
 		 * 
 		 * @return the normal of this Side. pointing outwards when observed from a point far away from the cube
 		 */
 		public Vector3D getNormal(){
 			return n;
 		}
 		/**
 		 * Returns pairs of vector coordinates. Each of which represents a straight line defining the 
 		 * edge of the side
 		 * @return a list containing Vector3D[] arrays with two elements per array
 		 */		
 		public ArrayList<Vector3D[]> getGrid(){
 			ArrayList<Vector3D[]> lines=new ArrayList<Vector3D[]>();
 			//Calculates the position of one corner of the side
 			Vector3D startVector=Vector3D.add(Vector3D.scalarMultiplication(cross, RADIUS),Vector3D.add(Vector3D.scalarMultiplication(n, RADIUS),Vector3D.scalarMultiplication(up, RADIUS)));
 			//Calculates the opposite corner
 			Vector3D opposite=Vector3D.subtract(startVector,Vector3D.scalarMultiplication(cross, SIDEWIDTH));
 			//Adding the top line
 			lines.add(new Vector3D[]{startVector,opposite});
 			//This vector represents the space between lines in the grid
 			Vector3D space=Vector3D.scalarMultiplication(up, SIDEWIDTH/squaresPerSide);
 			for (int i = 1; i <= squaresPerSide; i++) {
 				lines.add(new Vector3D[]{Vector3D.subtract(startVector, Vector3D.scalarMultiplication(space, i)),Vector3D.subtract(opposite, Vector3D.scalarMultiplication(space, i))});
 			}
 			//Starting on "vertical" lines
 			//startVector can be reused but space must change direction
 			space=Vector3D.scalarMultiplication(cross, SIDEWIDTH/squaresPerSide);
 			//Changing opposite to "vertical" alignment
 			opposite=Vector3D.subtract(startVector, Vector3D.scalarMultiplication(up, SIDEWIDTH));
 			//Adding "side-line"
 			lines.add(new Vector3D[]{startVector,opposite});
 			for (int i = 1; i <=squaresPerSide ; i++) {
 				lines.add(new Vector3D[]{Vector3D.subtract(startVector, Vector3D.scalarMultiplication(space, i)),Vector3D.subtract(opposite, Vector3D.scalarMultiplication(space, i))});
 			}
 			return lines;
 		}
 		/**
 		 * Get an arrow consisting of 6 pairs of Vectors which graphically points out
 		 * which way is up and which is down
 		 * @return An array consisting of the 6 pairs of 3D-vectors
 		 *   /\
 		 *  /  \
 		 * /_  _\
 		 *   ||
 		 *   ||
 		 */
 		public ArrayList<Vector3D[]> getArrow(){
 			ArrayList<Vector3D[]> arrow=new ArrayList<Vector3D[]>();	
 			//Setting a few helping vectors
 
 			Vector3D leftSpacing=Vector3D.scalarMultiplication(cross, RADIUS);
 			Vector3D upSpacing=Vector3D.scalarMultiplication(up, RADIUS);
 			Vector3D center=Vector3D.scalarMultiplication(n, RADIUS);
 			//Setting left side of arrow
 			arrow.add(new Vector3D[]{Vector3D.add(center, upSpacing),Vector3D.add(Vector3D.add(center, leftSpacing),Vector3D.scalarMultiplication(upSpacing, -1/(float)squaresPerSide))});     
 			arrow.add(new Vector3D[]{Vector3D.add(Vector3D.add(center, leftSpacing),Vector3D.scalarMultiplication(upSpacing, -1/(float)squaresPerSide)),Vector3D.add(center,Vector3D.scalarMultiplication(leftSpacing, 0.3f))});
 			arrow.add(new Vector3D[]{Vector3D.add(center,Vector3D.scalarMultiplication(leftSpacing, 0.3f)),Vector3D.add(Vector3D.scalarMultiplication(upSpacing, -1), Vector3D.add(center,Vector3D.scalarMultiplication(leftSpacing, 0.3f)))});
 			leftSpacing=Vector3D.scalarMultiplication(leftSpacing, -1);
 			//leftspacing is now rightspacing and all operations will be mirrored against the central line
 			arrow.add(new Vector3D[]{Vector3D.add(center, upSpacing),Vector3D.add(Vector3D.add(center, leftSpacing),Vector3D.scalarMultiplication(upSpacing, -1/(float)squaresPerSide))});     
 			arrow.add(new Vector3D[]{Vector3D.add(Vector3D.add(center, leftSpacing),Vector3D.scalarMultiplication(upSpacing, -1/(float)squaresPerSide)),Vector3D.add(center,Vector3D.scalarMultiplication(leftSpacing, 0.3f))});
 			arrow.add(new Vector3D[]{Vector3D.add(center,Vector3D.scalarMultiplication(leftSpacing, 0.3f)),Vector3D.add(Vector3D.scalarMultiplication(upSpacing, -1), Vector3D.add(center,Vector3D.scalarMultiplication(leftSpacing, 0.3f)))});
 			return arrow;
 
 		}
 
 		private void checkVector(Vector3D v){
 			if (v.length()!=1) {
 				throw new IllegalArgumentException("The vector must have the format (a,b,c) where a,b,c kan be 0 or 1 and have length 1");
 			}
 		}
 	}
 }

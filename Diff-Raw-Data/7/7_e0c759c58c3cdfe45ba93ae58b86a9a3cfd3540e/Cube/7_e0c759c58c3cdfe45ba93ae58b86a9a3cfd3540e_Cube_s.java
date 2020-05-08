 /*
  *      Cube.java
  *      
  */
 
 
 public class Cube {
 
     public static int X = 0;
     public static int Y = 1;
     public static int Z = 2;
     public static int BLACK = 3;
     public static int WHITE = 2;
     public static int EMPTY = 0;
     public static int DOME = 1;
     public static int XUP = 0;
     public static int XDOWN = 1;
     public static int YUP = 2;
     public static int YDOWN = 3;
     public static int ZUP = 4;
     public static int ZDOWN = 5;
     public static int NONE = -1;
 
     // Data members
     private int[] location;
     private int[] faces;
     private int color;
     private HashMap<String, Cube> board;
     
     // Constructor
     public Cube (int x, int y, int z, int d1, int d2, int color) {
         location = new int[3];
         location[X] = x;
         location[Y] = y;
         location[Z] = z;
         faces = new int[6];
         faces[d1] = DOME;
         if (d2 != -1) {
             faces[d2] = DOME;
         }
         this.color = color;
     }
     
     public void setBoard(HashMap<String, Cube> b) {
 	this.board = b;
     }
 
     public int getFace(int f) {
 	return faces[f];
     }
     
     public int getX() {
 	return location[X];
     }
 
     public int getY() {
 	return location[Y];
     }
 
     public int getZ() {
 	return location[Z];
     }
 
     public Cube clone() {
 	Cube c = new Cube(location[X], location[Y], location[Z], firstDome(), secondDome(), color);
 	if (this.occupied()) {
 	    // add sceptres
 	}
     }
 
     public int firstDome() {
 	for (int i = 0; i < faces.length; i++) {
 	    if (faces[i] == DOME) {
 		return i;
 	    }
 	}
 	// should NEVER happen
 	System.err.println("UHOH!!!");
 	return NONE;
     }
 
     public int secondDome() {
 	for (int i = firstDome() + 1; i < faces.length; i++) {
 	    if (faces[i] == DOME) {
 		return i;
 	    }
 	}
 	return NONE;
     }
 
     // Methods
     public boolean addSceptre(int f, int color) {
         if (isEmpty(f)) {
             faces[f] = color;
             return true;
         }
         return false;
     }
     
    public int removeSceptre(int f/*, int x */) {
         if (faces[f] == WHITE || faces[f] == BLACK) {
             int temp = faces[f];
             faces[f] = EMPTY;
             // 
             return temp;
         }
         return -1;
     }
     
     public boolean encroach() {
         for (int i = 0; i < faces.length; i++) {
             if (faces[i] == WHITE && color == BLACK) {
                 return true;           
             }
             if (faces[i] == BLACK && color == WHITE) {
 		return true;
 	    }
 	}
 	return false;
     }
     
     public boolean occupied() {
 	for (int i = 0; i < faces.length; i++) {
 	    if (faces[i] == WHITE || faces[i] == BLACK) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public boolean isFree() {
 	if (occupied()) {
 	    return false;
 	}
 	String above = "" + location[X] + ", " + location[Y] + ", " + (location[Z] + 1);
 	if (board.get(above) == null) {
 	    return true;
 	}
 	return false;
     }
 
     public boolean twoSceptreSameColor() {
 	int first = EMPTY;
 	int second = EMPTY;
 	for (int i = 0; i < faces.length; i++) {
 	    if (faces[i] == WHITE || faces[i] == BLACK) {
 		if (first == EMPTY) {
 		    first = faces[i];
 		} else {
 		    second = faces[i];
 		}
 	    }
 	}
 	if (first != EMPTY) {
 	    return first == second;
 	}
 	return false;
     }
 
     public boolean twoSceptreDifferentColor() {
 	int first = EMPTY;
 	int second = EMPTY;
 	for (int i = 0; i < faces.length; i++) {
 	    if (faces[i] == WHITE || faces[i] == BLACK) {
 		if (first == EMPTY) {
 		    first = faces[i];
 		} else {
 		    second = faces[i];
 		}
 	    }
 	}
 	if (first != EMPTY && second != EMPTY) {
 	    return first != second;
 	}
 	return false;
 
     }
     
     public boolean isEmpty(int f) {
         if(faces[f] == EMPTY) {
             return true;
         }
         return false;
     }
     
     public static void main (String args[]) {
         HashMap<String, Cube> b = new HashMap<String, Cube>();
         Cube stuff = new Cube(-1, 0, 2, XUP, NONE, BLACK);
 	stuff.setBoard(b);
         b.put("-1, 0, 2", stuff);
         
      }
 }

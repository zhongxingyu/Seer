 class GeometryTest {
 	public static void main(String[] args) {
 	
 	if (args.length != 0) {
 		switch(Integer.parseInt(args[0])) {
 			case 1:
 				double x = Double.parseDouble(args[1]);
 				double y = Double.parseDouble(args[2]);
 				Point p = new Point(x,y);
 				System.out.println(p);
 				break;
 			case 2:
 				double x1 = Double.parseDouble(args[1]);
 				double x2 = Double.parseDouble(args[2]);
 				double y1 = Double.parseDouble(args[3]);
 				double y2 = Double.parseDouble(args[4]);
 				Line l = new Line(x1,x2,y1,y2);
 				System.out.println(l);
 				break;
 			default:
 				break;
 			}
 		}
 		
		class Square {
 		
 		/**
 		 * k is the length of one side of a square.
 		 */
 		double k;
 		/**
 		 * a,b,c,d are the points of a square
 		 */
 		Point a;
 		Point b;
 		Point c;
 		Point d;
 		
 		/**
 		 * Empty constructor, creates a square at coordinates ((0,0),(1,0),(0,1),(1,1)) with edge length 1
 		 */
 		Square() {
 			a = new Point(0,0);
 			b = new Point(1,0);
 			c = new Point(0,1);
 			d = new Point(1,1);
 			k = 1;
 		}
 		/**
 		 * Constructor that creates specified square
 		 * @param a is the point
 		 * @param k is the edge length
 		 */
 		Square(Point a, double k) {
 			a = a;
 			b = new Point((a.getX()+k),(a.getY()));
 			c = new Point(a.getX(),a.getY()+k);
 			d = new Point(a.getX()+k,a.getY()+k);
 			k = k;
 		}
 		
 		Square(double x1, double y1, double k){
 			a = new Point(x1,y1);
 			this.k = k;
 		}
 		/**
 		 * Move this square to a new position. 
 		 * @param x1 the distance from current position in x direction
 		 * @param y1 the distance from current position in y direction
 		 */
 		void shiftSquare(double x1, double y1){
 			this.a.shift(x1,y1);
 			this.b.shift(x1,y1);
 			this.c.shift(x1,y1);
 			this.d.shift(x1,y1);
 		}
 		
 		/**
 		 * Creates a line from the point a and b
 		 * @param q is the square
 		 * @return the line from the point a and b of the square
 		 */
 		Line lineBack(Square q){
 		Line qline = new Line(q.a,q.b);	
 		return qline;
 		}
 		
 		/**
 		 * Creates a string with the coordinates of the points of the square and the edge length
 		 * @param q is the square
 		 * @return a string with the coordinates of the points and the edge length
 		 */
 		static String toString(Square q){
 			String x = "Punkt a: ("+q.a.getX()+","+q.a.getY()+") "+
 			"Punkt b: ("+q.b.getX()+","+q.b.getY()+") "+
 			"Punkt c: ("+q.c.getX()+","+q.c.getY()+") "+
 			"Punkt d: ("+q.d.getX()+","+q.d.getY()+") "+
			"Kantenlaenge:"+q.k;
 			return x;
 		}
 		
 	}
 	}
 }

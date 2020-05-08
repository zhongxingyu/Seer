 /**
  * 
  * Represents a square
  * @author Grupppe 5
  *
  */
 public class Square {
 		
 		/**
 		 * the width of the square
 		 */
 		private double width;
 		/**
 		 * the bottom left corner of the square
 		 * bottom right corner
 		 * top right corner
 		 * top left corner
 		 */
 		private Point blc;
 		private Point brc;
 		private Point trc;
 		private Point tlc;
 		
 		/**
 		 * creates a square with its bottom left corner at (0,0) and a width of 1
 		 * 
 		 * blc = bottom left corner
 		 * brc = bottom right corner
 		 * trc = top right corner
 		 * tlc = top left corner
 		 */
 		public Square() {
 			this.blc = new Point(0,0);
 			this.brc = new Point(1,0);
 			this.trc = new Point(1,1);
 			this.tlc = new Point(0,1);
 			this.width = 1.0;
 		}
 		
 		/**
 		 * constructs a square with the given parameters
 		 * @param p the point in the bottom left corner of the square
 		 * @param w is the edge length
 		 * 
 		 * 
 		 */
 		public Square(Point p, double w) {
 			this.width = w;
 			this.blc = new Point(p.getX(), p.getY());
			this.brc = new Point(p.getX()+w, p.getY());
			this.trc = new Point(p.getX()+w, p.getY()+w);
			this.tlc = new Point(p.getX(), p.getY()+w);
 			
 		}
 		
 		/**
 		 * Move this square to a new position. 
 		 * @param dx the distance from current position in x direction
 		 * @param dy the distance from current position in y direction
 		 */
 		public void shift(double dx, double dy) {
 			this.blc.shift(dx, dy);
 			this.brc.shift(dx, dy);
 			this.trc.shift(dx, dy);
 			this.tlc.shift(dx, dy);
 		}
 		
 		/**
 		 * 
 		 * @return a line from the bottom left corner of the square to the bottom right corner
 		 */
 		public Line getBottomLine() {
 			return new Line(blc, new Point(blc.getX() + width, blc.getY()));
 		}
 		
 		/**
 		 * 
 		 * @return a string with the coordinates of the bottom left corner
 		 * and the edge length
 		 */
 		@Override
 		public String toString() {
 			return "<Square> bottom left corner: " + this.blc.toString() + " - width: " + this.width;
 		}
 		
 	}

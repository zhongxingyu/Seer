 /**
  * TODO Put here a description of what this class does.
  * 
  * @author gollivam. Created Sep 10, 2012.
  */
 public final class ColorSet {
	double	r = 0, g = 0, b = 0;
 	
 	public ColorSet(final double r, final double g, final double b) {
 		this.r = r;
 		this.g = g;
 		this.b = b;
 	}
 	
 	public ColorSet(final double c) {
 		this(c, c, c);
 	}
 	
 	public void hit(final ColorSet c) {
		hit(c.r, c.g, c.b);
 	}
 	
 	public final void hit(final double r, final double g, final double b) {
 		this.r += r;
 		this.r /= 2.0;
 		
 		this.g += g;
 		this.g /= 2.0;
 		
 		this.b += b;
 		this.b /= 2.0;
 	}
 	
 	public final String toString() {
 		return "(" + r + "," + g + "," + b + ")";
 		
 	}
 }

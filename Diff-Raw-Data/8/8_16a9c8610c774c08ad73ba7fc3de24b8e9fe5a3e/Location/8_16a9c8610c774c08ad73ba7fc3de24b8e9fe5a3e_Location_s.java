 package jason.environment.grid;
 
 public final class Location {
    public final int x, y;
     
     public Location(int x, int y) {
         this.x = x;
         this.y = y;
     }
     
 	/** calculates the Manhattan distance between two points */
     public int distance(Location l) {
         return Math.abs(x - l.x) + Math.abs(y - l.y);
     }
 
     /** calculates the Euclidean distance between two points */
     public double distanceEuclidean(Location l) {
         return Math.sqrt(Math.pow(x - l.x, 2) + Math.pow(y - l.y, 2));
     }
 
     /** returns Math.max( Math.abs(this.x - l.x) , Math.abs(this.y - l.y)) */
     public int maxBorder(Location l) {
         return Math.max( Math.abs(this.x - l.x) , Math.abs(this.y - l.y));
     }
 
     public  boolean isNeigbour(Location l) {
         return 
             distance(l) == 1 ||
             equals(l) ||
             Math.abs(x - l.x) == 1 && Math.abs(y - l.y) == 1;
     }
     
     @Override
 	public int hashCode() {
 		final int PRIME = 31;
 		int result = 1;
 		result = PRIME * result + x;
 		result = PRIME * result + y;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) return true;
 		if (getClass() != obj.getClass()) return false;
 		final Location other = (Location) obj;
 		if (x != other.x) return false;
 		if (y != other.y) return false;
 		return true;
 	}
     
     public Object clone() {
         return new Location(x,y);
     }
     
     public String toString() {
     	return (x + "," + y);
     }
 }

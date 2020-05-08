 package aritzh.ld27.entity.ai.astar;
 
 /**
  * @author Aritz Lopez
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class Node implements Comparable<Node> {
 
 
     private NodeMap map;
     private Node parent = null;
     private int F, G, H;
     private int x;
     private int y;
     private boolean solid;
 
     public Node(int x, int y, boolean isSolid) {
         this.x = x;
         this.y = y;
         this.solid = isSolid;
     }
 
     public int getHForParent(Node parent) {
         if (parent != null) {
             return parent.H + (this.x != parent.x && this.y != parent.y ? 14 : 10);
         } else {
             return 0;
         }
     }
 
     private void recalculateF() {
         this.H = this.getHForParent(this.parent);
         this.F = this.H + this.G;
     }
 
     private void recalculateG() {
         this.G = Math.abs(this.x - this.map.end.x) + Math.abs(this.y - this.map.end.y);
     }
 
     public Node getParent() {
         return this.parent;
     }
 
     public void setParent(Node parent) {
         this.parent = parent;
         this.recalculateF();
     }
 
     /**
      * Compares this object with the specified object for order.  Returns a
      * negative integer, zero, or a positive integer as this object is less
      * than, equal to, or greater than the specified object.
      * <p/>
      * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
      * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
      * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
      * <tt>y.compareTo(x)</tt> throws an exception.)
      * <p/>
      * <p>The implementor must also ensure that the relation is transitive:
      * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
      * <tt>x.compareTo(z)&gt;0</tt>.
      * <p/>
      * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
      * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
      * all <tt>z</tt>.
      * <p/>
      * <p>It is strongly recommended, but <i>not</i> strictly required that
      * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
      * class that implements the <tt>Comparable</tt> interface and violates
      * this condition should clearly indicate this fact.  The recommended
      * language is "Note: this class has a natural ordering that is
      * inconsistent with equals."
      * <p/>
      * <p>In the foregoing description, the notation
      * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
      * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
      * <tt>0</tt>, or <tt>1</tt> according to whether the value of
      * <i>expression</i> is negative, zero or positive.
      *
      * @param o the object to be compared.
      * @return a negative integer, zero, or a positive integer as this object
      *         is less than, equal to, or greater than the specified object.
      * @throws NullPointerException if the specified object is null
      * @throws ClassCastException   if the specified object's type prevents it
      *                              from being compared to this object.
      */
     @Override
     public int compareTo(Node o) {
         return this.F > o.F ? 1 : this.F == o.F ? 0 : -1;
     }
 
     public int getF() {
         return this.F;
     }
 
     public int getX() {
         return this.x;
     }
 
     public int getY() {
         return this.y;
     }
 
     public boolean isSolid() {
         return this.solid;
     }
 
     @Override
     public int hashCode() {
         int result = 17;
         result = 31 * result + this.x;
         result = 31 * result + this.y;
         result = 31 * result + (this.solid ? 1 : 0);
         return result;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || this.getClass() != o.getClass()) return false;
 
         Node node = (Node) o;
 
         return this.solid == node.solid && this.x == node.x && this.y == node.y;
     }
 
     @Override
     public String toString() {
        return "(" + this.x + ", " + this.y + ")" + (this.solid ? " S" : "");
     }
 
     public void setMap(NodeMap map) {
         this.map = map;
         this.recalculateG();
         this.recalculateF();
     }
 
     public Node[] getNeighbors(boolean onlyOrthogonal) {
         if (onlyOrthogonal) {
             return new Node[]{
                     this.map.getNode(this.x + 1, this.y),
                     this.map.getNode(this.x - 1, this.y),
                     this.map.getNode(this.x, this.y + 1),
                     this.map.getNode(this.x, this.y - 1)
             };
         } else {
             Node[] ret = new Node[8];
             int idx = 0;
             for (int y = this.y - 1; y <= this.y + 1; y++) {
                 for (int x = this.x - 1; x <= this.x + 1; x++) {
                     if (((x == this.x) && (y == this.y)) || (x < 0) || (y < 0) || (x >= this.map.width) || (y >= this.map.height))
                         continue;
                     ret[idx++] = this.map.getNode(x, y);
                 }
             }
             return ret;
         }
     }
 
     public int getH() {
         return this.H;
     }
 }

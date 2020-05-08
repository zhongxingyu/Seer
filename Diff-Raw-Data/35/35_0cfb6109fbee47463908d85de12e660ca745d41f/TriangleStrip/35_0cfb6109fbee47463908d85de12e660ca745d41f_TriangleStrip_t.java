 package kcsaba.vision.data.mesh;
 
 /**
  * A mesh composed of triangle strips.
  * @author Kazó Csaba
  */
 public interface TriangleStrip extends TriangleMesh {
 	/**
 	 * Returns the number of strips this mesh is composed of.
	 * @return the number of strips
 	 */
 	public int getStripCount();
 	/**
 	 * Returns the points of the specified strip.
 	 * @param index the index of the strip to return
 	 * @return the points of the specified strip, containing at least three points. Each three consecutive
 	 * points form a triangle (i.e. points (0, 1, 2), points (1, 2, 3) etc.).
 	 * @throws IndexOutOfBoundsException if the index is invalid
 	 */
 	public PointList getStrip(int index);
 	
 	/**
 	 * Returns the number of points making up the specified strip.
 	 * @param index the index of the strip
 	 * @return the number of points in the strip
 	 * @throws IndexOutOfBoundsException if the index is invalid
 	 */
 	public int getStripLength(int index);
 }

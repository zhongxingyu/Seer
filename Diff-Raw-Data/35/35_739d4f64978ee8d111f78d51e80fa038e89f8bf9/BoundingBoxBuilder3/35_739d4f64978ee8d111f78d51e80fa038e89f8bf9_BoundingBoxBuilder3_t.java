 /**
  *
  */
 package org.jmist.toolkit;
 
 /**
  * Builds a box that contains the elements added to it.
  * @author bkimmel
  */
 public class BoundingBoxBuilder3 {
 
 	/**
 	 * Default constructor.
 	 * Initializes the bounding box builder to return an empty box.
 	 */
 	public BoundingBoxBuilder3() {
 		this.reset();
 	}
 
 	/**
 	 * Gets the smallest box that bounds everything that has been
 	 * added since the last reset.
 	 * @return The current bounding box.
 	 * @see #reset()
 	 */
 	public Box3 getBoundingBox() {
 		if (this.isEmpty()) {
 			return Box3.EMPTY;
 		}
 
 		return new Box3(minimumX, minimumY, minimumZ, maximumX, maximumY, maximumZ);
 	}
 
 	/**
 	 * Resets the bounding box builder to return the empty box.
 	 */
 	public void reset() {
 		minimumX = Double.NaN;
 	}
 
 	/**
 	 * Extends the bounding box to encompass the given point.
 	 * @param p The point to extend the bounding box to.
 	 */
 	public void add(Point3 p) {
 		if (this.isEmpty()) {
 			minimumX = maximumX = p.x();
 			minimumY = maximumY = p.y();
 			minimumZ = maximumZ = p.z();
 		} else {
 			minimumX = Math.min(p.x(), minimumX);
 			minimumY = Math.min(p.x(), minimumY);
 			minimumZ = Math.min(p.x(), minimumZ);
 			maximumX = Math.max(p.x(), maximumX);
 			maximumY = Math.max(p.x(), maximumY);
 			maximumZ = Math.max(p.x(), maximumZ);
 		}
 	}
 
 	/**
 	 * Extends the bounding box to encompass the given sphere.
 	 * @param sphere The sphere to include in the bounding box.
 	 */
 	public void add(Sphere sphere) {
		if (!sphere.isEmpty()) {
			double r = sphere.getRadius();
			Point3 c = sphere.getCenter();
 
			if (this.isEmpty()) {
				minimumX = c.x() - r;
				minimumY = c.y() - r;
				minimumZ = c.z() - r;
				maximumX = c.x() + r;
				maximumY = c.y() + r;
				maximumZ = c.z() + r;
			} else {
				minimumX = Math.min(c.x() - r, minimumX);
				minimumY = Math.min(c.y() - r, minimumY);
				minimumZ = Math.min(c.z() - r, minimumZ);
				maximumX = Math.max(c.x() + r, maximumX);
				maximumY = Math.max(c.y() + r, maximumY);
				maximumZ = Math.max(c.z() + r, maximumZ);
			}
 		}
 	}
 
 	/**
 	 * Extends the bounding box to encompass the given box.
 	 * @param box The box to include in the bounding box.
 	 */
 	public void add(Box3 box) {
 		if (!box.isEmpty()) {
 			if (this.isEmpty()) {
 				minimumX = box.getMinimumX();
 				minimumY = box.getMinimumY();
 				minimumZ = box.getMinimumZ();
 				maximumX = box.getMaximumX();
 				maximumY = box.getMaximumY();
 				maximumZ = box.getMaximumZ();
 			} else {
 				minimumX = Math.min(box.getMinimumX(), minimumX);
 				minimumY = Math.min(box.getMinimumX(), minimumY);
 				minimumZ = Math.min(box.getMinimumX(), minimumZ);
 				maximumX = Math.max(box.getMaximumX(), maximumX);
 				maximumY = Math.max(box.getMaximumX(), maximumY);
 				maximumZ = Math.max(box.getMaximumX(), maximumZ);
 			}
 		}
 	}
 
 	/**
 	 * Indicates whether the bounding box is currently empty.
 	 * Equivalent to this.getBoundingBox().isEmpty().
 	 * @return A value indicating whether the bounding box is
 	 * 		currently empty.
 	 * @see #getBoundingBox(), {@link Box3#isEmpty()}
 	 */
 	public boolean isEmpty() {
 		return Double.isNaN(minimumX);
 	}
 
 	private double minimumX, minimumY, minimumZ;
 	private double maximumX, maximumY, maximumZ;
 
 }

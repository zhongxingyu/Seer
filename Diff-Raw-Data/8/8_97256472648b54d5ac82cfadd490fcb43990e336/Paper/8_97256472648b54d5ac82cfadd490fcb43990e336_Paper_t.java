 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.model;
 
import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;

 import javax.vecmath.Point2d;
 
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 
 /**
  * A basic class for paper.
  * 
  * @author Martin Pecka
  */
 public class Paper extends cz.cuni.mff.peckam.java.origamist.model.jaxb.Paper
 {
     /**
      * Return the relative dimensions of the paper (so that one of them will be 1.0 and the second one will be <= 1.0).
      * 
      * @return The relative dimensions of the paper.
      */
     public DoubleDimension getRelativeDimensions()
     {
         if (this.size.getWidth() >= this.size.getHeight()) {
             return new DoubleDimension(1.0, size.getHeight() / size.getWidth());
         } else {
             return new DoubleDimension(size.getWidth() / size.getHeight(), 1.0);
         }
     }
 
     /**
      * Check if the given point in relative coordinates lies in the paper boundaries.
      * 
      * @param p The point to check.
      * @return True if the point lies in this paper's boundaries.
      */
     public boolean containsRelative(Point2d p)
     {
         DoubleDimension dim = getRelativeDimensions();
        if (p.getX() < -EPSILON || p.getY() < -EPSILON)
             return false;
        if (p.getX() > dim.getWidth() + EPSILON || p.getY() > dim.getHeight() + EPSILON)
             return false;
         return true;
     }
 
     /**
      * @return The length of one relative unit of this paper in meters (actually this is the length of the longer side
      *         in meters).
      * 
      * @throws IllegalStateException If this paper is specified by a relative unit which doesn't have its reference
      *             dimension specified.
      */
     public double getOneRelInMeters() throws IllegalStateException
     {
         if (size.getUnit() == Unit.REL) {
             if (size.getReferenceLength() != null)
                 return size.getReferenceLength();
             throw new IllegalStateException(
                     "Cannot convert from a relative dimension without reference dimension to an absloute dimension.");
         }
         return UnitHelper.convertTo(Unit.REL, Unit.M, 1, size.getUnit(), size.getMax());
     }
 
     @Override
     public void setSize(UnitDimension value)
     {
         if (Unit.REL.equals(value.getUnit()))
             throw new IllegalStateException("Cannot set relative dimensions to paper.");
         value.setReference(value.getUnit(), value.getMax());
         super.setSize(value);
     }
 }

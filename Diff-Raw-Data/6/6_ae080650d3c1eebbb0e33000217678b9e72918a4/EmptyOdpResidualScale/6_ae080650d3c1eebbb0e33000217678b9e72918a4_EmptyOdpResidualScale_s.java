 package org.jreserve.jrlib.bootstrap.odp.scale;
 
 import org.jreserve.jrlib.bootstrap.odp.residuals.OdpResidualTriangle;
 import org.jreserve.jrlib.linkratio.LinkRatio;
 import org.jreserve.jrlib.triangle.claim.ClaimTriangle;
 import org.jreserve.jrlib.vector.AbstractVector;
 
 /**
  * EmptyOdpResidualScale has a scale parameter of 1, for each valid 
  * development period, and NaN for development periods outside the boudns.
  * Valid developmetn periods are 
 * `0 <= development && development < {@link OdpResidualTriangle#getDevelopmentCount() source.getDevelopmentCount()}`.
  * @author Peter Decsi
  * @version 1.0
  */
 public class EmptyOdpResidualScale 
     extends AbstractVector<OdpResidualTriangle> 
     implements OdpResidualScale {
 
     private final static double SCALE = 1d;
     
     private int developments;
 
     public EmptyOdpResidualScale(OdpResidualTriangle source) {
         super(source);
     }
     
     @Override
     public OdpResidualTriangle getSourceOdpResidualTriangle() {
         return source;
     }
 
     @Override
     public LinkRatio getSourceLinkRatios() {
         return source.getSourceLinkRatios();
     }
 
     @Override
     public ClaimTriangle getSourceTriangle() {
         return source.getSourceTriangle();
     }
 
     @Override
     public double getFittedValue(int accident, int development) {
         return source.getFittedValue(accident, development);
     }
 
     @Override
     public double[][] toArrayFittedValues() {
         return source.toArrayFittedValues();
     }
     
     @Override
     public int getLength() {
         return developments;
     }
     
     @Override
     public double getValue(int development) {
         return (0<=development && development<developments)? 
                 SCALE : 
                 Double.NaN;
     }
     
     @Override
     protected void recalculateLayer() {
         doRecalculate();
     }
     
     private void doRecalculate() {
         developments = source.getDevelopmentCount();
     }
 
 }

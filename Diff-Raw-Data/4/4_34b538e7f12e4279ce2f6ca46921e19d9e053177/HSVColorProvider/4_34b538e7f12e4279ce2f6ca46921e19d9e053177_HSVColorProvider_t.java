 /**
  * 
  */
 package org.esa.beam.dataViewer3D.data.color;
 
 import java.awt.Color;
 
 /**
  * A color provider returning colors based on HSV spectrum.
  * 
  * @author Martin Pecka
  */
 public class HSVColorProvider extends AbstractColorProvider
 {
 
     @Override
     public Color getColor(double sample, double weight)
     {
         final double val = (sample < min ? min : (sample > max ? max : sample));
        // we scale the hue to interval [0, 0.8], because if it would go up to 1.0, the color for min and max would be
        // the same
        return Color.getHSBColor((float) ((val - min) / difference) * 0.8f, 1f, (float) weight);
     }
 
 }

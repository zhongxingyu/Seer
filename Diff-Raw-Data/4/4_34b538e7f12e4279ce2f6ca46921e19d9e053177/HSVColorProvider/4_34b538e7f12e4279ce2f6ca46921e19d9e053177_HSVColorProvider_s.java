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
        return Color.getHSBColor((float) ((val - min) / difference), 1f, (float) weight);
     }
 
 }

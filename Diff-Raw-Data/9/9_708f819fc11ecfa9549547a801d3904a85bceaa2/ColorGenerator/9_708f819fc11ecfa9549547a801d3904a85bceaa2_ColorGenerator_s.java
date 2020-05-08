 package org.aldeon.gui.colors;
 
 import javafx.scene.paint.Color;
 import org.bouncycastle.crypto.prng.RandomGenerator;
 
 import java.util.Random;
 
 public class ColorGenerator {
     public static Color getColorForSeed(int seed){
        Random r = new Random(seed);
         double h,s,b;
        h=r.nextDouble()*360;
        s=r.nextDouble()%0.2+0.8;
        b=r.nextDouble()%0.2+0.8;
         return Color.hsb(h,s,b);
     }
 }

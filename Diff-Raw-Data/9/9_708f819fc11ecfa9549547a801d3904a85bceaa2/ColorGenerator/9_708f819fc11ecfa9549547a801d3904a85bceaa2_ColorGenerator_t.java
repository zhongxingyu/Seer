 package org.aldeon.gui.colors;
 
 import javafx.scene.paint.Color;
 import org.bouncycastle.crypto.prng.RandomGenerator;
 
 import java.util.Random;
 
 public class ColorGenerator {
     public static Color getColorForSeed(int seed){
        seed=Math.abs(seed);
         double h,s,b;
        h=((seed*0.12345)%1.0)*360;
        s=(seed*0.23456)%0.2+0.8;
        b=(seed*0.34567)%0.2+0.8;
         return Color.hsb(h,s,b);
     }
 }

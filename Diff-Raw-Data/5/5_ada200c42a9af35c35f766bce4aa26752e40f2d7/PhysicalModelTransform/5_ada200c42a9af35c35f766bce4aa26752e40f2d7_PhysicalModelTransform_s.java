 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.util.ui;
 
 import java.awt.geom.AffineTransform;
 
 /**
  *
  * @author epsilon
  */
 public class PhysicalModelTransform extends AffineTransform {
 
     public static final double SCALE_LOWER_LIMITE = 1e-15;
     private int defaultX;
     private int defaultY;
     private double defaultScale;
 
     public void unitScaleAndSetOrigin(int originX, int originY) {
         setToIdentity();
         translate(originX, originY);
         scale(1, -1);
     }
 
     void translateOrigin(int dx, int dy) {
         preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
     }
 
     public void scaleByCenter(int centerX, int centerY, double scale) {
         AffineTransform tranformBack = new AffineTransform(this);
        setToTranslation(centerX, centerX);
         scale(scale, scale);
        translate(-centerX, -centerX);
         if (Math.abs(getScaleX()) < SCALE_LOWER_LIMITE || Math.abs(getScaleY()) < SCALE_LOWER_LIMITE) {
             setTransform(tranformBack);
         } else {
             concatenate(tranformBack);
         }
     }
 
     public void setDefault(int centerX, int centerY, double scale) {
         defaultX = centerX;
         defaultY = centerY;
         defaultScale = scale;
     }
 
     public void resetToDefault() {
         unitScaleAndSetOrigin(defaultX, defaultY);
         scaleByCenter(0, 0, defaultScale);
     }
 }

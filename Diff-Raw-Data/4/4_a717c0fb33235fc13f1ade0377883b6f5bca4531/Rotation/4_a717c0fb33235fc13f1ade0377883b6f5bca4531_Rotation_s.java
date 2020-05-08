 /* @(#)$Id$*/
 
 package org.podval.imageio;
 
 
 public enum Rotation {
 
   NOTHING(0), LEFT(-90), RIGHT(90), OVER(180);
 
 
   private static Rotation valueOf(int degrees) {
     degrees = degrees % 360;
 
     Rotation result;
     if (degrees ==   0) { result = NOTHING; } else
     if (degrees == -90) { result = LEFT   ; } else
     if (degrees ==  90) { result = RIGHT  ; } else
     if (degrees == 180) { result = OVER   ; } else {
       throw new IllegalArgumentException();
     }
 
     return result;
   }
 
 
   private Rotation(int degrees) {
     this.degrees = degrees;
   }
 
 
   private int getDegrees() {
     return degrees;
   }
 
 
   private final int degrees;
 
 
   public Rotation left() {
     return valueOf(getDegrees() + LEFT.getDegrees());
   }
 
 
   public Rotation right() {
     return valueOf(getDegrees() + RIGHT.getDegrees());
   }
 
 
   public Rotation over() {
     return valueOf(getDegrees() + OVER.getDegrees());
   }
 
 
   public Rotation inverse() {
     return valueOf(-getDegrees());
   }
 }

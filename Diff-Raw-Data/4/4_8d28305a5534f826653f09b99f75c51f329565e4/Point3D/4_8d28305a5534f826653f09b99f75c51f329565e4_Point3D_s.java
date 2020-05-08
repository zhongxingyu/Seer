 package info.jeppes.ZoneCore.TriggerBoxes;
 
 import org.bukkit.Location;
 
 /**
  * Author: Jeppe Boysen Vennekilde
  *
  * This document is Copyright ©() and is the intellectual property of the author.
  *
  * TERMS AND CONDITIONS
  * 0. USED TERMS
  * OWNER - The original author(s) of the program
  * USER - End user of the program, person installing/using the program.
  *
  * 1. LIABILITY
  * THIS PROGRAM IS PROVIDED 'AS IS' WITH NO WARRANTIES, IMPLIED OR OTHERWISE.
  * THE OWNER OF THIS PROGRAM TAKES NO RESPONSIBILITY FOR ANY DAMAGES INCURRED
  * FROM THE USE OF THIS PROGRAM.
  *
  * 2. REDISTRIBUTION
  * This program may only be distributed where uploaded, mirrored, or otherwise
  * linked to by the OWNER solely. All mirrors of this program must have advance
  * written permission from the OWNER. ANY attempts to make money off of this
  * program (selling, selling modified versions, adfly, sharecash, etc.) are
  * STRICTLY FORBIDDEN, and the OWNER may claim damages or take other action to
  * rectify the situation.
  *
  * 3. DERIVATIVE WORKS/MODIFICATION
  * This program is provided freely and may be decompiled and modified for
  * private use, either with a decompiler or a bytecode editor. Public
  * distribution of modified versions of this program require advance written
  * permission of the OWNER and may be subject to certain terms.
  */
 public class Point3D {
     
     public double x;
     public double y;
     public double z;
     
     public Point3D(){
         x = 0;
         y = 0;
         z = 0;
     }
     public Point3D(Location location){
         this.x = location.getX();
         this.z = location.getZ();
         this.y = location.getY();
     }
     public Point3D(double x, double y, double z){
         this.x = x;
         this.y = y;
         this.z = z;
     }
     
     public final void setX(double x) {
         this.x = x;
     }
 
     public final void setY(double y) {
         this.y = y;
     }
     
     public final void setZ(double z) {
         this.z = z;
     }
     
     public final double getX() {
         return x;
     }
 
     public final double getY() {
         return y;
     }
     
     public final double getZ() {
         return z;
     }
 
     public void setLocation(double x, double y, double z) {
         setX(x);
         setY(y);
         setZ(z);
     }
 
     @Override
     public String toString(){
         return this.getClass().getName()+"[x="+x+",z="+z+",y="+y+"]";
     }
     
     public String toSaveString(){
         return toSaveString(this);
     }
     
     public static String toSaveString(Point3D point){
         return toSaveString(point.getX(),point.getY(),point.getZ());
     }
    public static String toSaveString(double x, double z, double y){
        return x+","+z+","+y;
     }
     
     public static Point3D toPoint3D(String saveString){
         String[] split = saveString.split(",");
         return new Point3D(java.lang.Double.parseDouble(split[0]),java.lang.Double.parseDouble(split[1]),java.lang.Double.parseDouble(split[2]));
     }
 }

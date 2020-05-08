 package org.rev317.api.methods;
 
 import java.awt.Point;
 
 public enum Equipment {
     HEAD(0, new Point(639, 228)),
     CAPE(1, new Point(601, 263)),
     NECK(2, new Point(644, 266)),
     AMMO(3, new Point(684, 267)),
     WEAPON(4, new Point(588, 307)),
     BODY(5, new Point(644, 308)),
     SHIELD(6, new Point(700, 305)),
     LEGS(7, new Point(644, 346)),
     HANDS(8, new Point(589, 386)),
     FEET(9, new Point(644, 387)),
     RING(10, new Point(700, 389));
 
     private int index;
     private Point point;
 
     Equipment(int index, Point point) {
         this.index = index;
         this.point = point;
     }
 
     public int getIndex() {
         return index;
     }
 
     public Point getPoint() {
         return point;
     }
 
     public int getEquippedId() {
        if (Players.getLocal().getEquipment()[getIndex()] >= 512){
            return Players.getLocal().getEquipment()[getIndex()] - 512;
        }else{
            return 0;
        }
     }
 }

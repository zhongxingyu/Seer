 /*
 
   Adapted into Processing library 5th Feb 2006 Tom Carden
   from "simple Arcball use template" 9.16.03 Simon Greenwold
 
   Heavily updated and moved to github in March 2012.
    
   Copyright (c) 2003 Simon Greenwold
   Copyright (c) 2006, 2012 Tom Carden
 
   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.
 
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General
   Public License along with this library; if not, write to the
   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
   Boston, MA  02111-1307  USA
 
 */
 
 package com.processinghacks.arcball;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 import java.awt.event.MouseEvent;
 
 public class Arcball {
 
   PApplet parent;
   PVector center;
   float radius;
   
   Quat qNow = new Quat(); 
   Quat qDrag;
   float dragFactor = 0.99f;
 
   /** defaults to radius of mag(width, height)/2 */
   public Arcball(PApplet parent) {
     this(null, 0, parent);
   }
 
   public Arcball(PVector center, float radius, PApplet parent) {
 
     if (center == null) {
       float w = parent.g.width;
       float h = parent.g.height;
       if (radius == 0) {
         radius = PApplet.mag(w, h) / 2.0f;
       }
       center = new PVector(w / 2.0f, h / 2.0f);
     }
 
     this.parent = parent;
 
     parent.registerMouseEvent(this);
     parent.registerPre(this);
 
     this.center = center;
     this.radius = radius;
   }
   
   public void reset() {
     qNow = new Quat(); 
     qDrag = null;    
   }
 
   public void mouseEvent(MouseEvent event) {
     int id = event.getID();
     if (id == MouseEvent.MOUSE_DRAGGED) {
       mouseDragged();
     } 
     else if (id == MouseEvent.MOUSE_PRESSED) {
       mousePressed();
     }
     else if (id == MouseEvent.MOUSE_RELEASED) {
       mouseReleased();
     }
   }
 
   public void mousePressed() {
     qDrag = null;
   }
   
   public void mouseReleased() {
     updateDrag();
   }  
 
   public void mouseDragged() {
     if (!parent.mousePressed) return;
     updateDrag();
     qNow = Quat.mult(qNow, qDrag);
   }
   
   private void updateDrag() {
     PVector pMouse = new PVector(parent.pmouseX, parent.pmouseY);
     PVector mouse = new PVector(parent.mouseX, parent.mouseY);
     PVector from = mouseOnSphere(pMouse);
     PVector to = mouseOnSphere(mouse);
     qDrag = new Quat(from.dot(to), from.cross(to));
   }
 
   public void pre() {
     if (dragFactor > 0.0 && !parent.mousePressed && qDrag != null && qDrag.w < 0.999999) {
       qDrag.scaleAngle(dragFactor);
       qNow = Quat.mult(qNow, qDrag);
     }
   }
 
  PVector mouseOnSphere(PVector mouse) {
     PVector v = new PVector();
     v.x = (mouse.x - center.x) / radius;
     v.y = (mouse.y - center.y) / radius;
 
     float mag = v.x * v.x + v.y * v.y;
     if (mag > 1.0f) {
       v.normalize();
     }
     else {
       v.z = PApplet.sqrt(1.0f - mag);
     }
     return v;
   }
 
  float getAngle() {
     return qNow.getAngle();
   }
   
  PVector getAxis() {
     return qNow.getAxis();
   }
 
   // Quat!
 
   static class Quat {
         
     float w, x, y, z;
 
     Quat() {
       reset();
     }
 
     Quat(float w, PVector v) {
       this.w = w;
       x = v.x;
       y = v.y;
       z = v.z;
     }
 
     Quat(float w, float x, float y, float z) {
       this.w = w;
       this.x = x;
       this.y = y;
       this.z = z;
     }
 
     void reset() {
       w = 1.0f;
       x = 0.0f;
       y = 0.0f;
       z = 0.0f;
     }
 
     void set(float w, float x, float y, float z) {
       this.w = w;
       this.x = x;
       this.y = y;
       this.z = z;
     }
 
     void set(float w, PVector v) {
       this.w = w;
       x = v.x;
       y = v.y;
       z = v.z;
     }
 
     void set(Quat q) {
       w = q.w;
       x = q.x;
       y = q.y;
       z = q.z;
     }
 
     static Quat mult(Quat q1, Quat q2) {
       Quat res = new Quat();
       res.w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
       res.x = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y;
       res.y = q1.w * q2.y + q1.y * q2.w + q1.z * q2.x - q1.x * q2.z;
       res.z = q1.w * q2.z + q1.z * q2.w + q1.x * q2.y - q1.y * q2.x;
       return res;
     }
 
     float[] getValue() {
       // transforming this quat into an angle and an axis vector...
 
       float[] res = new float[4];
 
       float sa = (float) Math.sqrt(1.0f - w * w);
       if (sa < PApplet.EPSILON) {
         sa = 1.0f;
       }
 
       res[0] = (float) Math.acos(w) * 2.0f;
       res[1] = x / sa;
       res[2] = y / sa;
       res[3] = z / sa;
 
       return res;
     }
     
     float getAngle() {
       return PApplet.acos(w) * 2.0f;
     }
     
     PVector getAxis() {
       float sa = (float) PApplet.sqrt(1.0f - w * w);
       if (sa < PApplet.EPSILON) {
         sa = 1.0f;
       }
       return new PVector(x / sa, y / sa, z / sa);      
     }
 
     // these are a bit sketchy because they've been written without concern for whetherthe quat remains a unit quat :-/
 
     void scaleAngle(float scale) {
       setAngle(scale * getAngle());
     }    
     
     void setAngle(float angle) {
       PVector axis = getAxis();
       w = PApplet.cos(angle / 2.0f);
       float scale = PApplet.sin(angle / 2.0f);
       x = axis.x * scale;
       y = axis.y * scale;
       z = axis.z * scale;      
     }
     
   } // Quat
 
 }
 

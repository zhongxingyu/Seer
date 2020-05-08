 /**
  * Inertial Partitioning
  * Copyright (C) 2013  Vy Thuy Nguyen
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  * 
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
  * Boston, MA  02110-1301, USA.
  */
 
 
 package entity;
 
 import javax.persistence.*;
 
 /**
  * @author              Vy Thuy Nguyen
  * @version             1.0 Mar 18, 2013
  * Last modified:       
  */
 @Entity
 public class DeadPoint 
 {
     @Id
     @GeneratedValue (strategy = GenerationType.AUTO)
     private long id;
     
     /**
      * The x coordinate
      */
     private int x;
     
     /**
      * The y coordinate
      */
     private int y;
     
     @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
     AnnotFloorPlan annotFloorPlan;
     
     public DeadPoint()
     {
         
     }
     
    public DeadPoint(int x, int y, AnnotFloorPlan afp)
     {
         this.x = x;
         this.y = y;
        this.annotFloorPlan = afp;
     }
     
     public void setAnnotFloorPlan(AnnotFloorPlan afp)
     {
         this.annotFloorPlan = afp;
     }
     
     public int getX()
     {
         return x;
     }
     
     public int getY()
     {
         return y;
     }
     
     public void setX(int x)
     {
         this.x = x;
     }
     
     public void setY(int y)
     {
         this.y = y;
     }
     
     
     public long getId()
     {
         return id;
     }
 
     @Override
     public boolean equals(Object rhs)
     {
         if (rhs == null || !(rhs instanceof DeadPoint))
         {
             return false;
         }
         else
         {
             DeadPoint dpRhs = (DeadPoint)rhs;
             return (dpRhs.x == this.x && dpRhs.y == this.y);
         }
     }
 
     @Override
     public int hashCode()
     {
         int hash = 7;
         hash = 67 * hash + this.x;
         hash = 67 * hash + this.y;
         return hash;
     }
 }

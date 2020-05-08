 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.common.cell;
 
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import java.io.Serializable;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  * The transform for a cell.
  * 
  * @author paulby
  */
 @ExperimentalAPI
 public class CellTransform implements Serializable {
 
     private Quaternion rotation;
     private Vector3f translation;
     private Vector3f scale = new Vector3f(1,1,1);
     
      /**
      * Constructor that takes translation, rotation, and scaling. Any/all of
      * the three arguments may be null.
      */
     public CellTransform(Quaternion rotate, Vector3f translate, Vector3f scale) {
         this(rotate, translate);
         if (scale == null) {
             this.scale = new Vector3f(1,1,1);
         } else
             this.scale = scale.clone();
     }
     
     /**
      * Create a cell transform. Either (or both) values may be null
      * 
      * @param quat
      * @param translation
      */
     public CellTransform(Quaternion rotate, Vector3f translation) {
         
         if (rotate==null)
             this.rotation = new Quaternion();
         else
             this.rotation = rotate.clone();
 
         if (translation==null)
             this.translation = new Vector3f();
         else
             this.translation = translation.clone();
 
     }
 
     private CellTransform(CellTransform orig) {
         this.rotation = new Quaternion(orig.rotation);
         this.translation = new Vector3f(orig.translation);
         this.scale = new Vector3f(orig.scale);
     }
     
     public CellTransform clone(CellTransform result) {
         if (result==null)
             return new CellTransform(this);
         
         result.set(this);
         return result;
     }
     
     /**
      * set this object to have the same state as source
      * @param source
      */
     private void set(CellTransform source) {
         this.rotation.set(source.rotation);       
         this.scale.set(source.scale);        
         this.translation.set(source.translation);
     }
 
     /**
      * Transform the BoundingVolume 
      * @param ret
      */
     public void transform(BoundingVolume ret) {
         assert(ret!=null);
         ret.transform(rotation,translation, scale, ret);
     }
     
     /**
      * Transform the vector ret by this transform. ret is modified and returned.
      * @param ret
      */
     public Vector3f transform(Vector3f ret) {
        rotation.multLocal(ret);
        scale.multLocal(ret);
         ret.addLocal(translation);
         
         return ret;
     }
     
     /**
      * Multiply this transform by t1. This transform will be update
      * and the result returned
      * 
      * @param transform
      * @return this
      */
     public CellTransform mul(CellTransform t1) {
         rotation.multLocal(t1.rotation);
         scale.multLocal(t1.scale);
 //        System.out.print(translation +"  + "+t1.translation);
         translation.addLocal(t1.translation);
 //        System.out.println(" = "+translation);
         return this;
     }
     
     /**
      * Subtract t1 from this transform, modifying this transform and
      * returning this transform.
      * 
      * @param t1
      * @return
      */
     public CellTransform sub(CellTransform t1) {
         rotation.subtract(t1.rotation);
         scale.subtract(t1.scale);
         translation.subtract(t1.translation);
         return this;
     }
     
     /**
      * Populates translation with the translation of this CellTransform, if translation
      * is null, a new Vector3f will be created and returned
      * 
      * @param translation object to return (to avoid gc)
      * @return the translation for this transform
      */
     public Vector3f getTranslation(Vector3f translation) {
         if (translation==null)
             return new Vector3f(this.translation);
         
         translation.set(this.translation);
         return translation;
     }
     
     /**
      * Set the translation.
      * @param translation set the translation for this transform
      */
     public void setTranslation(Vector3f translation) {
         this.translation = translation;
         if (this.translation==null)
             this.translation = new Vector3f();
     }
 
     /**
      * Get the rotation portion of this transform. Populates the rotation 
      * paramter with the current rotation and returns it, if rotation is null
      * a new Quaternion is returned.
      * 
      * @param rotation object to return (to avoid gc)
      * @return the rotation quaternion for this transform
      */
     public Quaternion getRotation(Quaternion rotation) {
         if (rotation==null)
             rotation = new Quaternion(this.rotation);
         else
             rotation.set(this.rotation);
         
         return rotation;
     }
 
     /**
      * Set the rotation portion of this transform
      * @param rotation set the rotation for this transform
      */
     public void setRotation(Quaternion rotation) {
         this.rotation = new Quaternion(rotation);
     }
     
         /**
      * Returns the scaling vector as an array of doubles to scale each axis.
      * Sets the value of the scale into the argument (if given). If a null
      * argument is passed, then this method creates and returns a new Vector3f
      * object.
      * 
      * @param scale Populate this object with the scale if non-null
      * @return The scaling factors
      */
     public Vector3f getScaling(Vector3f scale) {
         if (scale == null) {
             scale = new Vector3f(this.scale);
         }
         else {
             scale.set(this.scale);
         }
         return scale;
     }
     
     /**
      * Sets the scaling vector for this cell transform
      * 
      * @param scale The new scaling factor
      */
     public void setScaling(Vector3f scale) {
         this.scale = new Vector3f(scale);
     }
 
     /**
      * Invert the transform
      */
     public void invert() {
         rotation.inverseLocal();
         scale.multLocal(-1);
         translation.multLocal(-1);
     }
     
     @Override
     public boolean equals(Object o) {
         boolean ret = true;
         
         if (o instanceof CellTransform) {
             CellTransform e = (CellTransform)o;
             
             if (e.rotation!=null && !e.rotation.equals(rotation))
                 ret = false;
             else if (e.rotation==null && rotation!=null)
                 ret = false;
             
             if (e.translation!=null && !e.translation.equals(translation))
                 ret = false;
             else if (e.translation==null && translation!=null)
                 ret = false;
             
             if (e.scale!=null && !e.scale.equals(scale))
                 ret = false;
             else if (e.scale==null && scale!=null)
                 ret = false;
         } else {
             ret = false;
         }
         
         
         return ret;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 13 * hash + (this.rotation != null ? this.rotation.hashCode() : 0);
         hash = 13 * hash + (this.translation != null ? this.translation.hashCode() : 0);
         hash = 13 * hash + (this.scale != null ? this.scale.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
         return "Trans=" + translation.toString() + "," +
 	       "Rot=" + rotation.toString() + "," +
 	       "Scale=" + scale.toString();
     }
 }

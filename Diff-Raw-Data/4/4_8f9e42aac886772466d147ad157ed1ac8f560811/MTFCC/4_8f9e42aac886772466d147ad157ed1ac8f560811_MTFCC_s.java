 /**
  * Copyright 2011 Jason Ferguson.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.shared.model;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
 * MTFCC.java
 * <p/>
  * Represents the MAF/TIGER Feature Class Code for a Location.
  *
  * @author Jason Ferguson
  */
 @Entity
 @Table(name = "MTFCC")
 @SuppressWarnings("unused")
 public class MTFCC implements Serializable, IsSerializable {
 
     // Entity features
     private Long id;
     private String mtfccCode;
     private String featureClass;
     private String superClass;
     private boolean point;
     private boolean linear;
     private boolean areal;
     private String featureClassDescription;
 
     public MTFCC() {
     }
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Column(name = "MTFCCID")
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "MTFCCCODE")
     public String getMtfccCode() {
         return mtfccCode;
     }
 
     public void setMtfccCode(String mtfccCode) {
         this.mtfccCode = mtfccCode;
     }
 
     @Column(name = "FEATURECLASS")
     public String getFeatureClass() {
         return featureClass;
     }
 
     public void setFeatureClass(String featureClass) {
         this.featureClass = featureClass;
     }
 
     @Column(name = "SUPERCLASS")
     public String getSuperClass() {
         return superClass;
     }
 
     public void setSuperClass(String superClass) {
         this.superClass = superClass;
     }
 
  //   @Column(name = "POINT")
     @Transient
     public boolean isPoint() {
         return point;
     }
 
     public void setPoint(boolean point) {
         this.point = point;
     }
 
 //    @Column(name = "LINEAR1")        // LINEAR seems to be a reserved word
     @Transient
     public boolean isLinear() {
         return linear;
     }
 
     public void setLinear(boolean linear) {
         this.linear = linear;
     }
 
 //    @Column(name = "AREAL")
     @Transient
     public boolean isAreal() {
         return areal;
     }
 
     public void setAreal(boolean areal) {
         this.areal = areal;
     }
 
     @Column(name = "FEATURECLASSDESCRIPTION")
     public String getFeatureClassDescription() {
         return featureClassDescription;
     }
 
     public void setFeatureClassDescription(String featureClassDescription) {
         this.featureClassDescription = featureClassDescription;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         MTFCC mtfcc = (MTFCC) o;
 
         if (id != null ? !id.equals(mtfcc.id) : mtfcc.id != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : 0;
     }
 }

 package com.debortoliwines.openerp.reporting.di;
 
 /*
  *   This file is part of OpenERPJavaReportHelper
  *
  *   OpenERPJavaAPI is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   OpenERPJavaAPI is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with OpenERPJavaAPI.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   Copyright 2012 De Bortoli Wines Pty Limited (Australia)
  */
 
 import com.debortoliwines.openerp.api.Field.FieldType;
 
 /**
  * Holds available and selected field information.
  * An instance is unique by:
  * 1.  ModelPathName
  * 2.  InstanceNumber
  * 3.  FieldName
  * Equality check, checks for those three values.  It allows for unique and quick lookups in lists.
  * 
  * @author Pieter van der Merwe
  * @since  Jan 5, 2012
  */
 public class OpenERPFieldInfo implements Cloneable{
 
   private final String modelName;
   private final String fieldName;
   private final OpenERPFieldInfo parentField;
   private final FieldType fieldType;
   private int instanceNum;
   private String renamedFieldName;
   private final String relatedChildModelName;
 
   /**
    * Default constructor
    * @param modelName Model name that this field belongs to
    * @param instanceNum The instance number of the model object.  This will allow you for example to have two 
    *                    res.partner.address instances on the same line.
    * @param fieldName Field name this object holds data for.
    * @param renamedFieldName The user defined field name that can be used by report designers to uniquely identify the field.
    * @param parentField The parent field object.  This is important to calculate the ModelPath. [res.partner].[res.partner.address] etc.
    * @param fieldType OpenERPJavaAPI field type of this field
    * @param relatedChildModelName If this field is a related field, the child model it relates to.
    */
   public OpenERPFieldInfo(String modelName, int instanceNum, String fieldName, String renamedFieldName, OpenERPFieldInfo parentField, FieldType fieldType, String relatedChildModelName){
     this.modelName = modelName;
     this.fieldName = fieldName;
     this.parentField = parentField;
     this.fieldType = fieldType;
     this.relatedChildModelName = relatedChildModelName;
     this.instanceNum = instanceNum;
     this.renamedFieldName = renamedFieldName;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj instanceof OpenERPFieldInfo){
       OpenERPFieldInfo target = (OpenERPFieldInfo) obj;
 
       return target.getModelPathName().equals(this.getModelPathName())
           && target.getFieldName().equals(this.getFieldName())
           && target.getInstanceNum() == this.getInstanceNum();
     }
     else return super.equals(obj);
   }
 
   @Override
   public OpenERPFieldInfo clone() {
     return new OpenERPFieldInfo(modelName, instanceNum, fieldName, renamedFieldName, parentField, fieldType, relatedChildModelName);
   }
 
   /**
    * Get the parent field object.  Can be used to traverse the tree.
    * @return
    */
   public OpenERPFieldInfo getParentField() {
     return parentField;
   }
 
   /**
    * Gets the model this field relates to
    * @return
    */
   public String getModelName() {
     return modelName;
   }
 
   /**
    * Get the OpenERPJavaAPI field type for this field
    * @return
    */
   public FieldType getFieldType() {
     return fieldType;
   }
 
   /**
    * If this field is a relation field (MANY2ONE etc.) get the child model name
    * @return
    */
   public String getRelatedChildModelName() {
     return relatedChildModelName;
   }
 
   /**
    * Get the field name
    * @return
    */
   public String getFieldName() {
     return fieldName;
   }
 
   /**
    * Get the instance number for this field
    * @return
    */
   public int getInstanceNum() {
     return instanceNum;
   }
 
   /**
    * Adds one to the instance number.
    * If you want to create a new instance, increment the value to a unique number.
    */
   public void incrementInstanceNum() {
     this.instanceNum++;
   }
 
   /**
    * Sets the instance number for this field
    * If you want to create a new instance of this field, increment the value to a unique number.
    * @param instanceNum
    */
   public void setInstanceNum(int instanceNum) {
     this.instanceNum = instanceNum;
   }
 
   /**
    * Get the user defined field name
    * @return
    */
   public String getRenamedFieldName() {
     return renamedFieldName;
   }
 
   /**
    * Set the user defined field name
    * @param renamedFieldName
    */
   public void setRenamedFieldName(String renamedFieldName) {
     this.renamedFieldName = renamedFieldName;
   }
 
   /**
    * Generates the model path for this field to located it.
    * It is important because for example res.company may be linked at multiple levels (partner, child partner) etc.
    * For example: [res.partner].[res.company]
    * @return
    */
   public String getModelPathName(){
     if (parentField == null){
       return "[" + modelName + "]";
     }
     else {
      return parentField.getModelPathName() + ".[" + modelName + "]";
     }
   }
 }

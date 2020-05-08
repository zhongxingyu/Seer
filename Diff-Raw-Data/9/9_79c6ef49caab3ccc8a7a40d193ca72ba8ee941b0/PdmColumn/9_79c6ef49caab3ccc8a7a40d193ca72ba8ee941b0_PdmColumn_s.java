 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.connector.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * GoodData PDM column
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class PdmColumn {
 
 
     // data types
     public static final String PDM_COLUMN_TYPE_TEXT = "VARCHAR(128)";
    public static final String PDM_COLUMN_TYPE_LONG_TEXT = "VARCHAR(128)";
     public static final String PDM_COLUMN_TYPE_DATE = "INT";
     public static final String PDM_COLUMN_TYPE_INT = "INT";
     public static final String PDM_COLUMN_TYPE_LONG = "BIGINT";
     public static final String PDM_COLUMN_TYPE_FLOAT = "DECIMAL(15,4)";    
 
     // constraints
     public static final String PDM_CONSTRAINT_INDEX_UNIQUE = "UNIQUE";
     public static final String PDM_CONSTRAINT_INDEX_MULTIPLE = "MULTIPLE";
     public static final String PDM_CONSTRAINT_AUTOINCREMENT = "AUTOINCREMENT";
     public static final String PDM_CONSTRAINT_PK = "PRIMARY KEY";
 
     //name
     private String name;
 
     // type
     private String type;
 
     // source column that the column sourceColumn
     private String sourceColumn;
 
     // column constraints
     private List<String> constraints = new ArrayList<String>();
 
     // LDM type reference
     private String ldmTypeReference;
 
     //data format
     private String format;
     
     // initial values
     private List<String> elements = null;
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      */
     public PdmColumn(String name, String type) {
         setName(name);
         setType(type);
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param constraints column constraints
      */
     public PdmColumn(String name, String type, String[] constraints) {
         this(name, type);
         if(constraints != null && constraints.length > 0)
             for(String c : constraints)
                 this.constraints.add(c);
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param constraints column constraints
      * @param sourceColumn the source column that the column sourceColumn
      */
     public PdmColumn(String name, String type, String[] constraints, String sourceColumn) {
         this(name, type, constraints);
         setSourceColumn(sourceColumn);
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param sourceColumn the source column that the column sourceColumn
      */
     public PdmColumn(String name, String type, String sourceColumn) {
         this(name, type);
         setSourceColumn(sourceColumn);
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param constraints column constraints
      * @param sourceColumn the source column that the column sourceColumn
      * @param ldmTypeReference LDM type that the column represents (ATTRIBUTE | LABEL | FACT)
      */
     public PdmColumn(String name, String type, String[] constraints, String sourceColumn, String ldmTypeReference) {
         this(name, type, constraints);
         setSourceColumn(sourceColumn);
         setLdmTypeReference(ldmTypeReference);
 
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param constraints column constraints
      * @param sourceColumn the source column that the column sourceColumn
      * @param ldmTypeReference LDM type that the column represents (ATTRIBUTE | LABEL | FACT)
      * @param format column format (e.g. for date columns) 
      */
     public PdmColumn(String name, String type, String[] constraints, String sourceColumn, String ldmTypeReference,
                      String format) {
         this(name, type, constraints, sourceColumn, ldmTypeReference);
         setFormat(format);
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param sourceColumn the source column that the column sourceColumn
      * @param ldmTypeReference LDM type that the column represents (ATTRIBUTE | LABEL | FACT) 
      */
     public PdmColumn(String name, String type, String sourceColumn, String ldmTypeReference) {
         this(name, type);
         setSourceColumn(sourceColumn);
         setLdmTypeReference(ldmTypeReference);
     }
 
     /**
      * Constructor
      * @param name column name
      * @param type column type
      * @param sourceColumn the source column that the column sourceColumn
      * @param ldmTypeReference LDM type that the column represents (ATTRIBUTE | LABEL | FACT)
      * @param format column format (e.g. for date columns)
      */
     public PdmColumn(String name, String type, String sourceColumn, String ldmTypeReference, String format) {
         this(name, type, sourceColumn, ldmTypeReference);
         setFormat(format);
     }
 
 
     /**
      * Name getter
      * @return column name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Name setter
      * @param name column name
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Type getter
      * @return column type
      */
     public String getType() {
         return type;
     }
 
     /**
      * Type setter
      * @param type column type
      */
     public void setType(String type) {
         this.type = type;
     }
 
     /**
      * Represented source column getter
      * @return the source column that the column sourceColumn
      */
     public String getSourceColumn() {
         return sourceColumn;
     }
 
     /**
      * Represented source column setter
      * @param sourceColumn represented source column
      */
     public void setSourceColumn(String sourceColumn) {
         this.sourceColumn = sourceColumn;
     }
 
     /**
      * Constraints getter
      * @return column constraints
      */
     public List<String> getConstraints() {
         return constraints;
     }
 
     /**
      * Constraints setter
      * @param constraints column constraints
      */
     public void setConstraints(List<String> constraints) {
         this.constraints = constraints;
     }
 
     /**
      * LDM type reference getter
      * @return ldm type reference
      */
     public String getLdmTypeReference() {
         return ldmTypeReference;
     }
 
     /**
      * LDM type reference setter
      * @param ldmTypeReference LDM type that the column represents (ATTRIBUTE | LABEL | FACT)
      */
     public void setLdmTypeReference(String ldmTypeReference) {
         this.ldmTypeReference = ldmTypeReference;
     }
 
     /**
      * True if the column is primary key
      * @return true if the column is primary key
      */
     public boolean isPrimaryKey() {
         for(String constraint : getConstraints())
             if(PDM_CONSTRAINT_PK.equals(constraint))
                 return true;
         return false;
     }
 
     /**
      * True if the column is AUTOINCREMENT
      * @return true if the column is AUTOINCREMENT
      */
     public boolean isAutoIncrement() {
         for(String constraint : getConstraints())
             if(PDM_CONSTRAINT_AUTOINCREMENT.equals(constraint))
                 return true;
         return false;
     }
 
     /**
      * True if the column is UNIQUE
      * @return true if the column is UNIQUE
      */
     public boolean isUnique() {
         for(String constraint : getConstraints())
             if(PDM_CONSTRAINT_INDEX_UNIQUE.equals(constraint))
                 return true;
         return false;
     }
 
     /**
      * Format getter
      * @return column format
      */
     public String getFormat() {
         return format;
     }
 
     /**
      * Format setter
      * @param format column format
      */
     public void setFormat(String format) {
         this.format = format;
     }
     
     /**
      * initial elements setter
      * @return initial elements
      */
     public List<String> getElements() {
 		return elements;
 	}
     
     /**
      * initial elements getter
      * @param elements initial elements
      */
 	public void setElements(List<String> elements) {
 		this.elements = elements;
 	}
 
 	@Override
 	public String toString() {
 		return name + "(" + type + ")";
 	}
 }

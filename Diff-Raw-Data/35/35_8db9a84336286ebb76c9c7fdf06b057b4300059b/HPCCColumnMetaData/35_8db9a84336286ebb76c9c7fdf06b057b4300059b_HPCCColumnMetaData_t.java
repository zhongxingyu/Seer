 /*##############################################################################
 
 Copyright (C) 2011 HPCC Systems.
 
 All rights reserved. This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 ############################################################################## */
 
 package org.hpccsystems.jdbcdriver;
 
 import java.util.List;
 
 /**
  * @author rpastrana
  */
 
 public class HPCCColumnMetaData
 {
     public enum ColumnType
     {
         FIELD,
         CONSTANT,
        FUNCTION,
         CONTENTMODIFIER;
     }
 
     private String                   columnName;
     private int                      index;
     private int                      sqlType;
 
     private String                   eclType;
     private String                   tableName;
     private int                      columnSize;
     private int                      decimalDigits;
     private int                      radix;
     private String                   nullable;
     private String                   remarks;
     private String                   columnDefault;
     private int                      paramType;
     private String                   javaClassName;
     private String                   constantValue = null;
     private ColumnType              columnType;
     private String                   alias = null;
     private List<HPCCColumnMetaData> funccols = null;
     private boolean                  isDistinct = false;
     private ECLFunction              contentModifier = null;
 
     public HPCCColumnMetaData(String columnName, int index, int sqlType, String constant, String eclType)
     {
         constantValue = constant;
         this.columnName = columnName;
         this.index = index;
         this.sqlType = sqlType;
         this.paramType = HPCCDatabaseMetaData.procedureColumnUnknown;
         javaClassName = HPCCJDBCUtils.convertSQLtype2JavaClassName(this.sqlType);
         this.eclType = eclType;
         columnType = ColumnType.CONSTANT;
     }
 
     public HPCCColumnMetaData(String columnName, int index, int sqlType)
     {
         this.columnName = columnName;
         this.index = index;
         this.sqlType = sqlType;
         this.paramType = HPCCDatabaseMetaData.procedureColumnUnknown;
         javaClassName = HPCCJDBCUtils.convertSQLtype2JavaClassName(this.sqlType);
         columnType = ColumnType.FIELD;
     }
 
     public HPCCColumnMetaData(String columnName, int index, List<HPCCColumnMetaData> columns)
     {
         this.columnName = columnName;
         this.index = index;
         this.sqlType = java.sql.Types.OTHER;
         this.paramType = HPCCDatabaseMetaData.procedureColumnUnknown;
         javaClassName = HPCCJDBCUtils.convertSQLtype2JavaClassName(this.sqlType);
        columnType = ColumnType.FUNCTION;
         funccols = columns;
     }
 
     public void setConstantValue(String value)
     {
         constantValue = value;
     }
 
     public boolean isConstant()
     {
         return constantValue != null;
     }
 
     public String getConstantValue()
     {
         return constantValue;
     }
 
     public String getColumnName()
     {
         return columnName;
     }
 
     public String getColumnNameOrAlias()
     {
         if (alias != null)
             return alias;
         else
             return columnName;
     }
 
     public void setColumnName(String columnName)
     {
         this.columnName = columnName;
     }
 
     public int getIndex()
     {
         return index;
     }
 
     public void setIndex(int index)
     {
         this.index = index;
     }
 
     public int getParamType()
     {
         return paramType;
     }
 
     public void setParamType(int paramType)
     {
         this.paramType = paramType;
     }
 
     public void setSqlType(int type)
     {
         sqlType = type;
     }
 
     public int getSqlType()
     {
         return sqlType;
     }
 
     public String getEclType()
     {
         return eclType;
     }
 
     public void setEclType(String eclType)
     {
         this.eclType = eclType;
         this.sqlType = HPCCJDBCUtils.mapECLtype2SQLtype(eclType);
     }
 
     public String getTableName()
     {
         return tableName;
     }
 
     public void setTableName(String tableName)
     {
         this.tableName = tableName;
     }
 
     public int getColumnSize()
     {
         return columnSize;
     }
 
     public void setColumnSize(int columnSize)
     {
         this.columnSize = columnSize;
     }
 
     public int getDecimalDigits()
     {
         return decimalDigits;
     }
 
     public void setDecimalDigits(int decimalDigits)
     {
         this.decimalDigits = decimalDigits;
     }
 
     public int getRadix()
     {
         return radix;
     }
 
     public void setRadix(int radix)
     {
         this.radix = radix;
     }
 
     public String getNullable()
     {
         return nullable;
     }
 
     public void setNullable(String nullable)
     {
         this.nullable = nullable;
     }
 
     public String getRemarks()
     {
         return remarks;
     }
 
     public void setRemarks(String remarks)
     {
         this.remarks = remarks;
     }
 
     public String getColumnDefault()
     {
         return columnDefault;
     }
 
     public void setColumnDefault(String columnDefault)
     {
         this.columnDefault = columnDefault;
     }
 
     public String getJavaClassName()
     {
         return javaClassName;
     }
 
     public ColumnType getColumnType()
     {
         return columnType;
     }
 
     public void setColumnType(ColumnType columnType)
     {
         this.columnType = columnType;
     }
 
     public List<HPCCColumnMetaData> getFunccols()
     {
         return funccols;
     }
 
     public String getAlias()
     {
         return alias;
     }
 
     public void setAlias(String alias)
     {
         this.alias = alias;
     }
 
     public boolean isDistinct()
     {
         return isDistinct;
     }
 
     public void setDistinct(boolean isDistinct)
     {
         this.isDistinct = isDistinct;
     }
 
     public ECLFunction getContentModifier()
     {
         return contentModifier;
     }
 
     public String getContentModifierStr()
     {
         return contentModifier != null ? contentModifier.getEclFunction() : null;
     }
 
     public boolean hasContentModifier()
     {
         return contentModifier != null;
     }
 
     public void setContentModifier(ECLFunction contentModifier)
     {
         this.contentModifier = contentModifier;
     }
 
     @Override
     public String toString()
     {
         return "Name: " + this.columnName + " SQL Type: " + sqlType + " ECL Type: " + eclType;
     }
 }

 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following 
  * conditions are provided as a summary of the NSL but the NSL remains the 
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL. 
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only 
  *    (as Java .class files or a .jar file containing the .class files) and only 
  *    as part of an application that uses The Software as part of its primary 
  *    functionality. No distribution of the package is allowed as part of a software 
  *    development kit, other library, or development tool without written consent of 
  *    Netspective Corporation. Any modified form of The Software is bound by 
  *    these same restrictions.
  * 
  * 3. Redistributions of The Software in any form must include an unmodified copy of 
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective 
  *    Corporation and may not be used to endorse products derived from The 
  *    Software without without written consent of Netspective Corporation. "Sparx" 
  *    and "Netspective" may not appear in the names of products derived from The 
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the 
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind. 
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING 
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.      
  *
  * @author Shahid N. Shah
  */
  
 /**
 * $Id: AbstractColumn.java,v 1.1 2002-01-20 14:53:20 snshah Exp $
  */
 
 package com.netspective.sparx.xif.dal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class AbstractColumn implements Column
 {
     public static long COLUMNFLAG_ISREQUIRED = 1;
     public static long COLUMNFLAG_ISUNIQUE = COLUMNFLAG_ISREQUIRED * 2;
     public static long COLUMNFLAG_ISNATURALPRIMARYKEY = COLUMNFLAG_ISUNIQUE * 2;
     public static long COLUMNFLAG_ISSEQUENCEDPRIMARYKEY = COLUMNFLAG_ISNATURALPRIMARYKEY * 2;
     public static long COLUMNFLAG_ISINDEXED = COLUMNFLAG_ISSEQUENCEDPRIMARYKEY * 2;
     public static long COLUMNFLAG_SQLDEFNHASSIZE = COLUMNFLAG_ISINDEXED * 2;
     public static long COLUMNFLAG_CUSTOMSTART = COLUMNFLAG_SQLDEFNHASSIZE * 2;
 
     public static String SIZE_REPLACEMENT_FMT = "%size%";
 
     private Table parentTable;
     private String name;
     private int size = -1;
     private int indexInRow;
     private Map sqlDefn = new HashMap();
     private String description;
     private long flags;
     private ForeignKey foreignKey;
     private String foreignKeyRef;
     private String sequenceName;
     private short foreignKeyRefType;
     private String defaultSqlExprValue;
     private String dataClassName;
     private List dependentFKeys;
 
     static public String convertColumnNameForMapKey(String name)
     {
         return name.toLowerCase();
     }
 
     public AbstractColumn(Table table, String name)
     {
         setParentTable(table);
         setName(name);
         parentTable.addColumn(this);
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getNameForMapKey()
     {
         return name.toLowerCase();
     }
 
     public void setName(String value)
     {
         name = value;
     }
 
     public Table getParentTable()
     {
         return parentTable;
     }
 
     public void setParentTable(Table value)
     {
         parentTable = value;
     }
 
     public String getSequenceName()
     {
         return sequenceName;
     }
 
     public void setSequenceName(String value)
     {
         sequenceName = value;
     }
 
     public String getSqlDefn(String dbms)
     {
         String defn = (String) sqlDefn.get(dbms == null ? "ansi" : dbms);
         if(defn != null)
             return flagIsSet(COLUMNFLAG_SQLDEFNHASSIZE) ? replaceValueInStr(defn, SIZE_REPLACEMENT_FMT, Integer.toString(size)) : defn;
         else
             return null;
     }
 
     public void setSqlDefn(String dbms, String value)
     {
         if(value.indexOf(SIZE_REPLACEMENT_FMT) >= 0)
             setFlag(COLUMNFLAG_SQLDEFNHASSIZE);
 
         sqlDefn.put(dbms == null ? "ansi" : dbms, value);
     }
 
     public String getDescription()
     {
         return description;
     }
 
     public void setDescription(String value)
     {
         description = value;
     }
 
     public int getSize()
     {
         return size;
     }
 
     public void setSize(int value)
     {
         size = value;
     }
 
     public String getDataClassName()
     {
         return dataClassName;
     }
 
     public void setDataClassName(String value)
     {
         dataClassName = value;
     }
 
     public ForeignKey getForeignKey()
     {
         return foreignKey;
     }
 
     public void setForeignKey(ForeignKey value)
     {
         foreignKey = value;
     }
 
     public void setForeignKeyRef(short type, String value)
     {
         foreignKeyRefType = type;
         foreignKeyRef = value;
     }
 
     public List getDependentForeignKeys()
     {
         return dependentFKeys;
     }
 
     public void registerForeignKeyDependency(ForeignKey fKey)
     {
         if(dependentFKeys == null) dependentFKeys = new ArrayList();
         dependentFKeys.add(fKey);
         fKey.getSourceColumn().getParentTable().registerForeignKeyDependency(fKey);
     }
 
     public int getIndexInRow()
     {
         return indexInRow;
     }
 
     public void setIndexInRow(int value)
     {
         indexInRow = value;
     }
 
     public String getDefaultSqlExprValue()
     {
         return defaultSqlExprValue;
     }
 
     public void setDefaultSqlExprValue(String value)
     {
         defaultSqlExprValue = value;
     }
 
     public boolean isIndexed()
     {
         return flagIsSet(COLUMNFLAG_ISINDEXED);
     }
 
     public boolean isNaturalPrimaryKey()
     {
         return flagIsSet(COLUMNFLAG_ISNATURALPRIMARYKEY);
     }
 
     public boolean isPrimaryKey()
     {
         return isNaturalPrimaryKey() || isSequencedPrimaryKey();
     }
 
     public boolean isRequired()
     {
         return flagIsSet(COLUMNFLAG_ISREQUIRED);
     }
 
     public boolean isSequencedPrimaryKey()
     {
         return flagIsSet(COLUMNFLAG_ISSEQUENCEDPRIMARYKEY);
     }
 
     public boolean isUnique()
     {
         return flagIsSet(COLUMNFLAG_ISUNIQUE);
     }
 
     public void setIsIndexed(boolean flag)
     {
         setOrClearFlag(COLUMNFLAG_ISINDEXED, flag);
     }
 
     public void setIsNaturalPrimaryKey(boolean flag)
     {
         setOrClearFlag(COLUMNFLAG_ISNATURALPRIMARYKEY, flag);
     }
 
     public void setIsRequired(boolean flag)
     {
         setOrClearFlag(COLUMNFLAG_ISREQUIRED, flag);
     }
 
     public void setIsSequencedPrimaryKey(boolean flag)
     {
        setOrClearFlag(COLUMNFLAG_ISNATURALPRIMARYKEY, flag);
     }
 
     public void setIsUnique(boolean flag)
     {
         setOrClearFlag(COLUMNFLAG_ISUNIQUE, flag);
     }
 
     protected long getFlags()
     {
         return flags;
     }
 
     protected boolean flagIsSet(long flag)
     {
         return (flags & flag) == 0 ? false : true;
     }
 
     protected void setFlag(long flag)
     {
         flags |= flag;
     }
 
     protected void setOrClearFlag(long flag, boolean set)
     {
         if(set) flags |= flag; else flags &= ~flag;
     }
 
     protected void clearFlag(long flag)
     {
         flags &= ~flag;
     }
 
     public void finalizeDefn()
     {
         if(foreignKeyRefType != ForeignKey.FKEYTYPE_NONE)
         {
             Schema schema = getParentTable().getParentSchema();
             setForeignKey(schema.getForeignKey(this, foreignKeyRefType, foreignKeyRef));
         }
     }
 
     static public String replaceValueInStr(String srcStr, String findStr, String replStr)
     {
         if(srcStr == null || findStr == null || replStr == null)
             return null;
 
         int findLoc = srcStr.indexOf(findStr);
         if(findLoc >= 0)
         {
             StringBuffer sb = new StringBuffer(srcStr);
             sb.replace(findLoc, findLoc + findStr.length(), replStr);
             return sb.toString();
         }
         else
             return srcStr;
     }
 
 }

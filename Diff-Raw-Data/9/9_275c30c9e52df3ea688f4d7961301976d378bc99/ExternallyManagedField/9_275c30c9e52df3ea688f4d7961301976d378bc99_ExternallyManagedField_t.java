 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: ExternallyManagedField.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Models a field managed outside QueryJ.
  */
 package org.acmsl.queryj.tools.maven;
 
 /**
  * Models an externally-managed field.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 public class ExternallyManagedField
 {
     /**
      * The name.
      * @parameter property="name"
      */
     private String m__strName;
     
     /**
      * The table name.
      * @parameter property="tableName"
      */
     private String m__strTableName;
     
     /**
      * The keyword.
      * @parameter property="keyword"
      */
     private String m__strKeyword;
     
     /**
      * The retrieval query.
      * @parameter property="retrievalQuery"
      */
     private String m__strRetrievalQuery;
 
     /**
      * Specifies the name.
      * @param name such name.
      */
     protected final void immutableSetName(final String name)
     {
         m__strName = name;
     }
 
     /**
      * Specifies the name.
      * @param name such name.
      */
     public void setName(final String name)
     {
         immutableSetName(name);
     }
 
     /**
      * Returns the name.
      * @return such value.
      */
     protected final String immutableGetName()
     {
         return m__strName;
     }
 
     /**
      * Returns the name.
      * @return such value.
      */
     protected String getName()
     {
         return immutableGetName();
     }
 
     /**
      * Specifies the table name.
      * @param tableName such name.
      */
     protected final void immutableSetTableName(final String tableName)
     {
         m__strTableName = tableName;
     }
 
     /**
      * Specifies the table name.
      * @param tableName such name.
      */
     public void setTableName(final String tableName)
     {
         immutableSetTableName(tableName);
     }
 
     /**
      * Returns the table name.
      * @return such value.
      */
     protected final String immutableGetTableName()
     {
         return m__strTableName;
     }
     
     /**
      * Returns the table name.
      * @return such value.
      */
     protected String getTableName()
     {
         return immutableGetTableName();
     }
 
     /**
      * Specifies the keyword.
      * @param keyword such value.
      */
     protected final void immutableSetKeyword(final String keyword)
     {
         m__strKeyword = keyword;
     }
 
     /**
      * Specifies the keyword.
      * @param keyword such value.
      */
     public void setKeyword(final String keyword)
     {
         immutableSetKeyword(keyword);
     }
 
     /**
      * Returns the keyword.
      * @return such value.
      */
     protected final String immutableGetKeyword() 
     {
         return m__strKeyword;
     }
     
     /**
      * Returns the keyword.
      * @return such value.
      */
     protected String getKeyword() 
     {
         return immutableGetKeyword();
     }
 
     /**
      * Specifies the retrieval query.
      * @param query such query.
      */
     protected final void immutableSetRetrievalQuery(final String query)
     {
         m__strRetrievalQuery = query;
     }
 
     /**
      * Specifies the retrieval query.
      * @param query such query.
      */
     public void setRetrievalQuery(final String query)
     {
         immutableSetRetrievalQuery(query);
     }
 
     /**
      * Returns the retrieval query.
      * @return such value.
      */
     protected final String immutableGetRetrievalQuery()
     {
         return m__strRetrievalQuery;
     }
     
     /**
      * Returns the retrieval query.
      * @return such value.
      */
     protected String getRetrievalQuery()
     {
         return immutableGetRetrievalQuery();
     }
 
     /**
      * {@inheritDoc}
      */
     public String toString()
     {
         return
             toString(
                 getClass().getName(),
                 getTableName(),
                 getName(),
                 getKeyword(),
                 getRetrievalQuery());
     }
 
     /**
      * Builds a text representation of given externally-managed field
      * information.
      * @param className the class name.
      * @param tableName the table name.
      * @param name the field name.
      * @param keyword the keyword.
      * @param retrievalQuery the retrieval query.
      * @return the formatted text.
      */
     protected String toString(
        final String className,
         final String tableName,
         final String name,
         final String keyword,
         final String retrievalQuery)
     {
         StringBuilder result = new StringBuilder();
 
         result.append(className);
         result.append(":{ table : \"");
         result.append(tableName);
         result.append("\", column : \"");
         result.append(name);
 
         if (keyword != null)
         {
             result.append("\", keyword : \"");
 
             result.append(keyword);
         }
 
         if (retrievalQuery != null)
         {
             result.append("\", retrieval-query : \"");
             result.append(retrievalQuery);
         }
 
         result.append("\" }");
 
         return result.toString();
     }
 }

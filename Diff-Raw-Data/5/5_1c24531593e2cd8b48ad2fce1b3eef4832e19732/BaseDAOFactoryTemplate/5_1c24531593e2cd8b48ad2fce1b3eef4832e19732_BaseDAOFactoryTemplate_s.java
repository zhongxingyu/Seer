 /*
                         QueryJ
 
     Copyright (C) 2002-2005  Jose San Leandro Armendariz
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
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Is able to generate base DAO factories according to
  *              database metadata.
  *
  */
 package org.acmsl.queryj.tools.templates.dao;
 
 /*
  * Importing some project-specific classes.
  */
 import org.acmsl.queryj.tools.customsql.CustomSqlProvider;
 import org.acmsl.queryj.tools.metadata.MetadataManager;
 import org.acmsl.queryj.tools.templates.BasePerTableTemplate;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 /*
  * Importing some JDK classes.
  */
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Is able to create <code>DAOFactory</code> interfaces for each
  * table in the persistence model.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro</a>
  */
 public class BaseDAOFactoryTemplate
     extends  BasePerTableTemplate
 {
     /**
      * Builds a <code>BaseDAOFactoryTemplate</code> using given
      * information.
      * @param tableName the table name.
      * @param metadataManager the database metadata manager.
      * @param customSqlProvider the CustomSqlProvider instance.
      * @param packageName the package name.
      * @param engineName the engine name.
      * @param engineVersion the engine version.
      * @param quote the identifier quote string.
      * @param basePackageName the base package name.
      * @param repositoryName the repository name.
      */
     public BaseDAOFactoryTemplate(
         final String tableName,
         final MetadataManager metadataManager,
         final CustomSqlProvider customSqlProvider,
         final String packageName,
         final String engineName,
         final String engineVersion,
         final String quote,
         final String basePackageName,
         final String repositoryName)
     {
         super(
             tableName,
             metadataManager,
             customSqlProvider,
             packageName,
             engineName,
             engineVersion,
             quote,
             basePackageName,
             repositoryName);
     }
 
     /**
      * Retrieves the string template group.
      * @return such instance.
      */
     protected StringTemplateGroup retrieveGroup()
     {
        return retrieveGroup("/org/acmsl/queryj/dao/BaseDAO.stg");
     }
 
     /**
      * Retrieves the template name.
      * @return such information.
      */
     public String getTemplateName()
     {
        return "ResultSetExtractor";
     }
 }

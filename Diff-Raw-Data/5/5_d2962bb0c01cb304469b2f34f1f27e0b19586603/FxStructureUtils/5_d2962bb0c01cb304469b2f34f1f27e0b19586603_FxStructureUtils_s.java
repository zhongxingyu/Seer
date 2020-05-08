 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.ejb.beans.structure;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import static com.flexive.core.DatabaseConst.*;
 import static com.flexive.core.DatabaseConst.TBL_PROPERTY_OPTIONS;
 import static com.flexive.core.DatabaseConst.TBL_STRUCT_ASSIGNMENTS;
 
 /**
  * Server side utility methods for structures
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 class FxStructureUtils {
 
     private static final Log LOG = LogFactory.getLog(FxStructureUtils.class);
 
     /**
      * Remove all properties that are no longer referenced
      *
      * @param con a valid connection
      * @throws java.sql.SQLException on errors
      */
     public static void removeOrphanedProperties(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_PROPERTIES + ML + " WHERE ID NOT IN(SELECT DISTINCT APROPERTY FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE APROPERTY IS NOT NULL)");
             stmt.executeUpdate("DELETE FROM " + TBL_PROPERTY_OPTIONS + " WHERE ID NOT IN(SELECT DISTINCT APROPERTY FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE APROPERTY IS NOT NULL)");
             int removed = stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_PROPERTIES + " WHERE ID NOT IN(SELECT DISTINCT APROPERTY FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE APROPERTY IS NOT NULL)");
             if (removed > 0)
                 LOG.info(removed + " orphaned properties removed.");
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 
     /**
      * Remove all groups that are no longer referenced
      *
      * @param con a valid connection
      * @throws java.sql.SQLException on errors
      */
     public static void removeOrphanedGroups(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_GROUPS + ML + " WHERE ID NOT IN(SELECT DISTINCT AGROUP FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP IS NOT NULL)");
             stmt.executeUpdate("DELETE FROM " + TBL_GROUP_OPTIONS + " WHERE ID NOT IN(SELECT DISTINCT AGROUP FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP IS NOT NULL)");
             int removed = stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_GROUPS + " WHERE ID NOT IN(SELECT DISTINCT AGROUP FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP IS NOT NULL)");
             if (removed > 0)
                 LOG.info(removed + " orphaned groups removed.");
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 }

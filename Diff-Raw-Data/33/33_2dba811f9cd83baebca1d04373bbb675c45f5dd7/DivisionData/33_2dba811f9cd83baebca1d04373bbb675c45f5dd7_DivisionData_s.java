 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.shared.configuration;
 
 import java.io.Serializable;
 import java.util.regex.Pattern;
 
 /**
  * Container for division data read from the global configuration.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 public class DivisionData implements Serializable {
     private static final long serialVersionUID = -7804463451172463152L;
     /**
      * Division to be used for automated tests
      */
     public static final int DIVISION_TEST = -2;
     /**
      * Global configuration division
      */
     public static final int DIVISION_GLOBAL = 0;
 
     protected int id = -1;
     protected boolean available;
     protected String dataSource = null;
     protected String domainRegEx = null;
     protected Pattern domainPattern = null;
     protected String dbVendor = null;
     protected String dbVersion = null;
     protected String dbDriverVersion = null;
 
     /**
      * Constructor.
      *
      * @param id              the division ID
      * @param available       if division is available
      * @param dataSource      datasource JNDI path
      * @param domainRegEx     domain name matcher
      * @param dbVendor        database vendor
      * @param dbVersion       database version
      * @param dbDriverVersion database jdbc driver version
      */
     public DivisionData(int id, boolean available, String dataSource, String domainRegEx, String dbVendor,
                         String dbVersion, String dbDriverVersion) {
         this.id = id;
         this.available = available;
         this.dataSource = dataSource;
         this.domainRegEx = domainRegEx;
         this.dbVendor = dbVendor;
         this.dbVersion = dbVersion;
         this.dbDriverVersion = dbDriverVersion;
         try {
             this.domainPattern = Pattern.compile(domainRegEx);
         } catch (Exception e) {
             this.domainPattern = null;
         }
     }
 
     /**
      * Returns an editable division data object initialized with this instance. Note that the
      * edit object does not influence the original division data object, so it is safe to create
      * edit objects from system division data entries without cloning them first.
      *
      * @return an editable division data object initialized with this instance
      */
     public DivisionDataEdit asEditable() {
         return new DivisionDataEdit(this);
     }
 
     /**
      * Returns the JNDI path to this division's datasource.
      *
      * @return the JNDI path to this division's datasource.
      */
     public String getDataSource() {
         return dataSource;
     }
 
     /**
     * The regular expression describing matching domains for this divison.
      *
     * @return the regular expression describing matching domains for this divison.
      */
     public String getDomainRegEx() {
         return domainRegEx;
     }
 
     /**
      * Returns true when the given domain matches, false otherwise.
      *
      * @param domain the domain to be checked
      * @return true when the given domain matches, false otherwise.
      */
     public boolean isMatchingDomain(String domain) {
         return domainPattern != null && domainPattern.matcher(domain).lookingAt();
     }
 
     /**
      * Get the DB Vendor
      *
      * @return DB Vendor
      */
     public String getDbVendor() {
         return dbVendor;
     }
 
     /**
      * Get the DB Version
      *
      * @return DB Version
      */
     public String getDbVersion() {
         return dbVersion;
     }
 
     /**
      * Get the database jdbc driver version
      *
      * @return database jdbc driver version
      */
     public String getDbDriverVersion() {
         return dbDriverVersion;
     }
 
     /**
      * Is this division available (ie can a connection be retrieved)
      *
      * @return available
      */
     public boolean isAvailable() {
         return available;
     }
 
     /**
      * Return the unique ID of this division.
      *
      * @return the unique ID of this division.
      */
     public int getId() {
         return id;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         DivisionData that = (DivisionData) o;
 
         if (id != that.id) return false;
         if (!dataSource.equals(that.dataSource)) return false;
         if (!domainRegEx.equals(that.domainRegEx)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result;
         result = id;
         result = 31 * result + dataSource.hashCode();
         result = 31 * result + domainRegEx.hashCode();
         return result;
     }
 
     /**
      * Checks if the given division ID is valid (i.e. if the division
      * parameter has been set). This method does not check if the given division
      * is available, i.e. if corresponding entries exist in the global configuration.
      *
      * @param divisionId the division ID to be checked
      * @return true for valid division IDs
      */
     public static boolean isValidDivisionId(int divisionId) {
         return divisionId > 0 || divisionId == DIVISION_TEST || divisionId == DIVISION_GLOBAL;
     }
 }

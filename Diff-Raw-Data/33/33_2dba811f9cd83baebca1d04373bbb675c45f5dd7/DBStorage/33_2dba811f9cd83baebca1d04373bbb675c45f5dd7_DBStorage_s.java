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
 package com.flexive.core.storage;
 
 import com.flexive.core.search.DataFilter;
 import com.flexive.core.search.DataSelector;
 import com.flexive.core.search.SqlSearch;
 import com.flexive.core.search.cmis.impl.CmisSqlQuery;
 import com.flexive.core.search.cmis.impl.sql.SqlDialect;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.TypeStorageMode;
 
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.util.Date;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 /**
  * Database vendor specific storage
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public interface DBStorage {
 
     /**
      * Get the name of the storage vendor (DB vendor name)
      *
      * @return name of the storage vendor (DB vendor name)
      */
     String getStorageVendor();
 
     /**
      * Can this storage handle the requested database?
      *
      * @param dbm database meta data
      * @return if storage can handle the requested database
      */
     boolean canHandle(DatabaseMetaData dbm);
 
     /**
      * Get the ContentStorage singleton instance
      *
      * @param mode used storage mode
      * @return ContentStorage singleton instance
      * @throws FxNotFoundException if no implementation was found
      */
     ContentStorage getContentStorage(TypeStorageMode mode) throws FxNotFoundException;
 
     /**
      * Get the EnvironmentLoader singleton instance
      *
      * @return EnvironmentLoader singleton instance
      */
     EnvironmentLoader getEnvironmentLoader();
 
     /**
      * Get the SequencerStorage singleton instance
      *
      * @return SequencerStorage singleton instance
      */
     SequencerStorage getSequencerStorage();
 
     /**
      * Get the TreeStorage singleton instance
      *
      * @return TreeStorage singleton instance
      */
     TreeStorage getTreeStorage();
 
     /**
      * Get the LockStorage singleton instance
      *
      * @return LockStorage singleton instance
      */
     LockStorage getLockStorage();
 
     /**
      * Get a data selector for a sql search
      *
      * @param search current SqlSearch to operate on
      * @return data selector
      * @throws FxSqlSearchException on errors
      */
     DataSelector getDataSelector(SqlSearch search) throws FxSqlSearchException;
 
     /**
      * Get a data filter for a sql search
      *
      * @param con    an open and valid connection
      * @param search current SqlSearch to operate on
      * @return DataFilter
      * @throws FxSqlSearchException on errors
      */
     DataFilter getDataFilter(Connection con, SqlSearch search) throws FxSqlSearchException;
 
     /**
      * Get the CMIS SQL Dialect implementation
      *
      * @param environment      environment
      * @param contentEngine    content engine in use
      * @param query            query
      * @param returnPrimitives return primitives?
      * @return CMIS SQL Dialect implementation
      */
     SqlDialect getCmisSqlDialect(FxEnvironment environment, ContentEngine contentEngine, CmisSqlQuery query, boolean returnPrimitives);
 
     /**
      * Get the database vendor specific Boolean expression
      *
      * @param flag the flag to get the expression for
      * @return database vendor specific Boolean expression for <code>flag</code>
      */
     String getBooleanExpression(boolean flag);
 
     /**
      * Get the boolean <code>true</code> expression string for the database vendor
      *
      * @return the boolean <code>true</code> expression string for the database vendor
      */
     String getBooleanTrueExpression();
 
     /**
      * Get the boolean <code>false</code> expression string for the database vendor
      *
      * @return the boolean <code>false</code> expression string for the database vendor
      */
     String getBooleanFalseExpression();
 
     /**
      * Escape reserved words properly if needed
      *
      * @param query the query to escape
      * @return escaped query
      */
     String escapeReservedWords(String query);
 
     /**
      * Get a database vendor specific "IF" function
      *
      * @param condition the condition to check
      * @param exprtrue  expression if condition is true
      * @param exprfalse expression if condition is false
      * @return database vendor specific "IF" function
      */
     String getIfFunction(String condition, String exprtrue, String exprfalse);
 
     /**
      * Get the database vendor specific operator to query regular expressions
      *
      * @param column column to match
      * @param regexp regexp to match the column against
      * @return database vendor specific operator to query regular expressions
      */
     String getRegExpLikeOperator(String column, String regexp);
 
     /**
      * Get the database vendor specific statement to enable or disable referential integrity checks.
      * When in a transaction, be sure to check {@link #isDisableIntegrityTransactional()}
      * since not all databases support this in a transactional context.
      *
      * @param enable enable or disable checks?
      * @return database vendor specific statement to enable or disable referential integrity checks
      */
     String getReferentialIntegrityChecksStatement(boolean enable);
 
     /**
      * Return true if calling {@link #getReferentialIntegrityChecksStatement(boolean)} is possible
      * in a transactional context.
      *
      * @return  true if calling {@link #getReferentialIntegrityChecksStatement(boolean)} is possible
      * in a transactional context
      */
     boolean isDisableIntegrityTransactional();
 
     /**
      * Get the sql code of the statement to fix referential integrity when removing selectlist items
      *
      * @return sql code of the statement to fix referential integrity when removing selectlist items
      */
     String getSelectListItemReferenceFixStatement();
 
     /**
      * Get a database vendor specific timestamp of the current time in milliseconds as Long
      *
      * @return database vendor specific timestamp of the current time in milliseconds as Long
      */
     String getTimestampFunction();
 
     /**
      * Get a database vendor specific concat statement
      *
      * @param text array of text to concatenate
      * @return concatenated text statement
      */
     String concat(String... text);
 
     /**
      * Get a database vendor specific concat_ws statement
      *
      * @param delimiter the delimiter to use
      * @param text      array of text to concatenate
      * @return concatenated text statement
      */
     String concat_ws(String delimiter, String... text);
 
     /**
      * If a database needs a " ... from dual" to generate valid queries, it is returned here
      *
      * @return from dual (or equivalent) if needed
      */
     String getFromDual();
 
     /**
      * Get databas evendor specific limit statement
      *
      * @param hasWhereClause does the query already contain a where clause?
      * @param limit          limit
      * @return limit statement
      */
     String getLimit(boolean hasWhereClause, long limit);
 
     /**
      * Get database vendor specific limit/offset statement
      *
      * @param hasWhereClause does the query already contain a where clause?
      * @param limit          limit
      * @param offset         offset
      * @return limit/offset statement
      */
     String getLimitOffset(boolean hasWhereClause, long limit, long offset);
 
     /**
      * Get database vendor specific limit/offset statement using the specified variable name
      *
      * @param var            name of the variable to use
      * @param hasWhereClause does the query already contain a where clause?
      * @param limit          limit
      * @param offset         offset
      * @return limit/offset statement
      */
     String getLimitOffsetVar(String var, boolean hasWhereClause, long limit, long offset);
 
     /**
      * Get the statement to get the last content change timestamp
      *
      * @param live live version included?
      * @return statement to get the last content change timestamp
      */
     String getLastContentChangeStatement(boolean live);
 
     /**
      * Format a date to be used in a query condition (properly escaped)
      *
      * @param date the date to format
      * @return formatted date
      */
     String formatDateCondition(Date date);
 
     /**
      * Correctly escape a flat storage column if needed
      *
      * @param column name of the column
      * @return escaped column (if needed)
      */
     String escapeFlatStorageColumn(String column);
 
     /**
      * Returns true if the SqlError is a foreign key violation.
      *
      * @param exc the exception
      * @return true if the SqlError is a foreign key violation
      */
     boolean isForeignKeyViolation(Exception exc);
 
     /**
      * Returns true if the given exception was caused by a query timeout.
      *
      * @param e the exception to be examined
      * @return true if the given exception was caused by a query timeout
      * @since 3.1
      */
     boolean isQueryTimeout(Exception e);
 
     /**
      * Does the database rollback a connection if it encounters a constraint violation? (eg Postgres does...)
      *
      * @return database rollbacks a connection if it encounters a constraint violation
      */
     boolean isRollbackOnConstraintViolation();
 
     /**
      * Returns true if the SqlError is a unique constraint violation.
      *
      * @param exc the exception
      * @return true if the SqlError is a unique constraint violation
      */
     boolean isUniqueConstraintViolation(Exception exc);
 
     /**
      * Returns true if the given SqlException indicates a deadlock.
      *
      * @param exc the exception
      * @return true if the given SqlException indicates a deadlock.
      * @since 3.1
      */
     boolean isDeadlock(Exception exc);
 
     /**
      * When accessing the global configuration - does the config table has to be prefixed with the schema?
      * (eg in postgres no schemas are supported for JDBC URL's hence it is not required)
      *
      * @return access to configuration tables require the configuration schema to be prepended
      */
     boolean requiresConfigSchema();
 
     /**
      * Get a connection to the database using provided parameters and (re)create the database and/or schema
      *
      * @param database          name of the database
      * @param schema            name of the schema
      * @param jdbcURL           JDBC connect URL
      * @param jdbcURLParameters optional JDBC URL parameters
      * @param user              name of the db user
      * @param password          password
      * @param createDB          create the database?
      * @param createSchema      create the schema?
      * @param dropDBIfExist     drop the database if it exists?
      * @return an open connection to the database with the schema set as default
      * @throws Exception on errors
      */
     Connection getConnection(String database, String schema, String jdbcURL, String jdbcURLParameters, String user, String password, boolean createDB, boolean createSchema, boolean dropDBIfExist) throws Exception;
 
     /**
      * Initialize a configuration schema
      *
      * @param con         an open and valid connection to the database
      * @param schema      the schema to create
      * @param dropIfExist drop the schema if it exists?
      * @return success
      * @throws Exception on errors
      */
     boolean initConfiguration(Connection con, String schema, boolean dropIfExist) throws Exception;
 
     /**
      * Initialize a division
      *
      * @param con         an open and valid connection to the database
      * @param schema      the schema to create
      * @param dropIfExist drop the schema if it exists?
      * @return success
      * @throws Exception on errors
      */
     boolean initDivision(Connection con, String schema, boolean dropIfExist) throws Exception;
 
     /**
      * Export all data of a division to an OutputStream as ZIP
      *
      * @param con an open and valid connection to the database to be exported
      * @param out OutputStream that will be used to create the zip file
      * @throws Exception on errors
      */
     void exportDivision(Connection con, OutputStream out) throws Exception;
 
     /**
      * Import a complete division from a zip stream
      *
      * @param con an open and valid connection
     * @param zip  zip archive that contains an exported divison
      * @throws Exception on errors
      */
     void importDivision(Connection con, ZipFile zip) throws Exception;
 }

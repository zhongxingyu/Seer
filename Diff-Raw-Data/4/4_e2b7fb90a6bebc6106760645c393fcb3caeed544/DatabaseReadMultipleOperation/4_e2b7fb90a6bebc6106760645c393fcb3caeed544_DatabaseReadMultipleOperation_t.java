 /*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.persist.jdbc.operation;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.Database;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
 
 /**
  * Adapter to simplify writing of database reads which read multiple items, 
  * which takes care of requesting and returning connections, transaction 
  * management, query preparation and exception handling. To use this adapter you 
  * just need to define anonymous class and override method performOperation to 
  * provide the actual database read. Optionally you may want to override one of 
  * the handleXXX methods to provide custom error handling. 
  * 
  * This class is optimized for preparing queries, which need to retrieve multiple
  * items from the database. 
  *
  * Example of method in factory which reads data using query produced by its schema 
  *
  * public int[] getActualIds(
  *    final int  iDomainId,
  *    String     strIds, 
  *    SimpleRule listSecurityData
  * ) throws OSSException
  * {
  *    int[] arrActual = null;
  *
  *    DatabaseReadOperation dbop = new DatabaseReadMultipleOperation(
  *       this, m_schema.getSelectActualIds(strIds, listSecurityData), 
  *       m_schema, dataType)
  *    {
  *       protected Object performOperation(
  *          DatabaseFactoryImpl dbfactory,
  *          Connection          cntConnection,
  *          PreparedStatement   pstmQuery
  *       ) throws OSSException,
  *                SQLException
  *       {
  *          pstmQuery.setInt(1, iDomainId);
  *          return DatabaseUtils.loadMultipleIntsAsArray(pstmQuery);
  *       }         
  *    };
  *    arrActual = (int[])dbop.executeRead();         
  *    
  *    return arrActual;
  * }
  *
  * @author bastafidli
  */
 public abstract class DatabaseReadMultipleOperation extends DatabaseReadOperation 
 {
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor to use when the database read doesn't require any 
     * prepared statement.
     * 
     * @param factory - factory which is executing this operation
     */
    public DatabaseReadMultipleOperation(
       DatabaseFactory factory
    )
    {
       this(factory, null, null);
    }
 
    /**
     * Constructor to use when database read doesn't require any prepared 
     * statement.
     * 
     * @param factory - factory which is executing this operation
     * @param strQueryToPrepare - query which should be used to construct 
     *                            prepared statement which will be passed in to 
     *                            executeUpdate
     * @param schema - database schema used with this operation
     */
    public DatabaseReadMultipleOperation(
       DatabaseFactory factory,
       String          strQueryToPrepare,
       DatabaseSchema  schema
    )
    {
       super(factory, strQueryToPrepare, schema);
    }   
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Prepare the query if it was specified using the provided connection. 
     * 
     * @param dbfactory - database factory executing this operation
     * @param cntConnection - ready to use connection to perform the database
     *                        operation. No need to return this connection.
     * @param strQuery - query to prepare, might be null or empty if there is
     *                   nothing to prepare
     * @return PreparedStatement - prepared statement for query passed in as a 
     *                             parameter to the constructor. If no query was 
     *                             passed into constructor, this will be null.
     * @throws OSSException - an error has occurred
     * @throws SQLException - an error has occurred
     */
   @Override
    protected PreparedStatement prepareQuery(
       DatabaseFactory dbfactory,
       Connection      cntConnection,
       String          strQuery
    ) throws OSSException,
             SQLException
    {
       // Since this operation always loads multiple results always call the
       // special method with extra arguments
       PreparedStatement pstmQuery = null;
       
       if ((strQuery != null) && (strQuery.length() > 0))
       {
          Database database;
          int      iTypeSelectListResultSet;
          int      iConcurrencySelectListResultSet;
          
          database = dbfactory.getDatabase();
          iTypeSelectListResultSet = database.getSelectListResultSetType();
          iConcurrencySelectListResultSet = database
                                               .getSelectListResultSetConcurrency();
 
          pstmQuery = cntConnection.prepareStatement(strQuery, 
                         iTypeSelectListResultSet, 
                         iConcurrencySelectListResultSet);
       }
       
       return pstmQuery;
    }
 }

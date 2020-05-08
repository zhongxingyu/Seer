 /*
 * Copyright (C) 2006 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 import org.opensubsystems.core.data.BasicDataObject;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseSchema;
 
 /**
  * Adapter to simplify writing of database updates that inserts single data 
  * object to the database. The adapter takes care of requesting and returning 
  * connections, transaction management and exception handling. To use this 
  * adapter you just need to create an instance of this class and call 
  * executeUpdate method.
  *
  * Example of method in factory which creates single data object: 
  *
  * public int create(
  *    final BasicDataObject data
  * ) throws OSSException
  * {
  *    DatabaseCreateSingleDataObjectOperation dbop 
  *       = new DatabaseCreateSingleDataObjectOperation(
  *            this, m_schema.getInsertMyData(), m_schema.isInDomain(), m_schema,
  *            dataType, data);
  *    dbop.executeUpdate();
  *      
  *    return ((Integer)dbop.getReturnData()).intValue();
  * }
  *
  * @author OpenSubsystems
  */
 public class DatabaseCreateSingleDataObjectOperation extends DatabaseUpdateOperation
 {
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor to use when database update doesn't require any prepared
     * statement.
     * 
     * @param factory - factory which is executing this operation
     * @param strQueryToPrepare - query which should be used to construct prepared
     *                            statement which will be passed in to executeUpdate
     * @param schema - database schema used with this operation
     * @param data - data used for operation
     */
    public DatabaseCreateSingleDataObjectOperation(
       DatabaseFactory          factory,
       String                   strQueryToPrepare,
       ModifiableDatabaseSchema schema,
       Object                   data
    )
    {
       super(factory, strQueryToPrepare, schema, DatabaseOperations.DBOP_INSERT, data);
    }
 
    // Overridden methods //////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
   @Override
    protected void performOperation(
       DatabaseFactory     dbfactory, 
       Connection          cntConnection, 
       PreparedStatement   pstmQuery
    ) throws OSSException, SQLException
    {
       BasicDataObject objData = (BasicDataObject)m_data;
       int iIndex = 1;
       iIndex = setValuesForInsert(pstmQuery, objData, iIndex);
       
       dbfactory.getDatabase().insertAndFetchGeneratedValues(
          cntConnection, pstmQuery, m_dbschema.isInDomain(), 
          ((ModifiableDatabaseSchema)m_dbschema).getModifiableTableNames().get(
             m_factory.getDataDescriptor().getDataTypeAsObject()).toString(), 
          iIndex, objData);
       setReturnData(objData);
    }
 }

 /*
  * Copyright (C) 2006 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.opensubsystems.core.data.DataObject;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.Database;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseSchema;
 
 /**
  * Adapter to simplify writing of batched database updates, which takes care of
  * requesting and returning connections, transaction management and 
  * exception handling. To use this adapter you just need to create an instance
  * of this class and call executeUpdate method.
  *
  * Example of method in factory which creates collection of data using batch 
  * update 
  *
  * public int create(
  *    final Collection colDataObject
  * ) throws OSSException
  * {
  *    DatabaseBatchUpdateOperation dbop = new DatabaseBatchUpdateOperation(
  *       this, m_schema.getUpdateMyData(), m_schema, dataType, colDataObject);
  *    dbop.executeUpdate();
  *      
  *    return ((Integer)dbop.getReturnData()).intValue();
  * }
  *
  * @version $Id: DatabaseUpdateMultipleDataObjectsOperation.java,v 1.1 2009/04/22 05:41:57 bastafidli Exp $
  * @author Julian Legeny
  * @code.reviewer Miro Halas
  * @code.reviewed 1.9 2008/06/26 04:09:36 bastafidli
  */
 public class DatabaseUpdateMultipleDataObjectsOperation<T> extends DatabaseUpdateOperation
 {
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor
     * 
     * @param factory - factory which is executing this operation
     * @param query - query to update data
     * @param schema - schema to set the data to the statement
     * @param colDataObject - collection of data objects that will be updated
     */
    public DatabaseUpdateMultipleDataObjectsOperation(
       DatabaseFactory          factory,
       String                   query,
       ModifiableDatabaseSchema schema,
       Collection<T>            colDataObject
    ) 
    {
       super(factory, query, schema, DatabaseUpdateOperation.DBOP_UPDATE, 
             colDataObject);
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
   @Override
    protected void performOperation(
       DatabaseFactory   dbfactory,
       Connection        cntConnection, 
       PreparedStatement pstmQuery
    ) throws OSSException,
             SQLException
    {
       int[]       arrUpdatedReturn;
       int         iBatchedCount = 0;
       Iterator<T> items;
      DataObject  data;
       int         size;
       int         iTotalUpdatedReturn = 0;
       int         iBatchSize;
       Database    database;
       
       database = dbfactory.getDatabase();
       iBatchSize = database.getBatchSize();
       
       size =  ((Collection<T>)m_data).size();
       for (items = ((Collection<T>)m_data).iterator(); items.hasNext();)
       {
          data = (DataObject)items.next();
          // prepare data if necessary (update object values)
          prepareData(data);
          // set values for prepared statement
          setValuesForUpdate(pstmQuery, data, 1);
          pstmQuery.addBatch();
          iBatchedCount++;
 
          // test if there is time to execute batch
          if (((iBatchedCount % iBatchSize) == 0) 
             || (iBatchedCount == size))
          {
             arrUpdatedReturn = pstmQuery.executeBatch();
             iTotalUpdatedReturn += arrUpdatedReturn.length; 
          }
       }
       // TODO: Performance: Consider defining setReturnData(int)
       // so we do not have to create extra object
       setReturnData(new Integer(iTotalUpdatedReturn));
    }
 }

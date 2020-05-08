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
 
 import org.opensubsystems.core.error.OSSDataNotFoundException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.error.OSSInconsistentDataException;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseSchema;
 
 /**
  * Adapter to simplify writing of database updates which deletes single data 
  * object, which takes care of requesting and returning connections, transaction 
  * management, query preparation and exception handling.
  * 
  * Example of method in factory which deletes single data object:
  *  
  *  public void delete(
  *     final int iId,
  *     final int iDomainId
  *  ) throws OSSException
  *  {
  *     if (GlobalConstants.ERROR_CHECKING)
  *     {
  *        assert iId != DataObject.NEW_ID 
  *               : "Cannot delete data, which wasn't created yet.";
  *     }
  *
  *     DatabaseUpdateOperation dbop = new DatabaseDeleteSingleDataObjectOperation(
  *        this, m_schema.getDeleteMyDataById(), m_schema, iId, iDomainId);
  *     dbop.executeUpdate();
  *  }
  *
  * @author OpenSubsystems
  */
 public class DatabaseDeleteSingleDataObjectOperation extends DatabaseUpdateOperation
 {
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Schema to use to execute database dependent operations.
     */
    private ModifiableDatabaseSchema m_schema;
 
    /**
     * Data type of the related object to be deleted cascade.
     */
    private int m_iDataType;
 
    /**
     * ID of data object.
     */
    private int m_iId;
 
    /**
     * ID of domain the data object belongs to.
     */
    private int m_iDomainId;
    
    // Constructors /////////////////////////////////////////////////////////////
 
    /**
     * Copy constructor to use when database update doesn't require any prepared
     * statement.
     * @param factory - factory which is executing this operation
     * @param strQuery - sql query that has to be processed
     * @param schema - schema to use to execute database dependent operations
     * @param iId - ID of data object to delete
     * @param iDomainId - ID of domain the data object belongs to
     */
    public DatabaseDeleteSingleDataObjectOperation(
       DatabaseFactory          factory,
       String                   strQuery,
       ModifiableDatabaseSchema schema,
       int                      iId,
       int                      iDomainId
    )
    {
       super(factory, strQuery, schema, DatabaseUpdateOperation.DBOP_DELETE, null);
       
       m_schema = schema;
       m_iDataType = factory.getDataDescriptor().getDataType();
       m_iId = iId;
       m_iDomainId = iDomainId;
    }
 
    /**
     * {@inheritDoc}
     */
    protected void performOperation(
       DatabaseFactory     dbfactory, 
       Connection          cntConnection, 
       PreparedStatement   pstmQuery
    ) throws OSSException, SQLException
    {
       m_schema.deleteRelatedData(cntConnection, m_iDataType, m_iId);
 
       int iDeleted;
       
       pstmQuery.setInt(1, m_iId);
       if (m_schema.isInDomain())
       {
          // set up domain ID parameter if data object is in domain
          pstmQuery.setInt(2, m_iDomainId);
       }
       iDeleted = pstmQuery.executeUpdate();
       
       if (iDeleted == 0)
       {
          throw new OSSDataNotFoundException(
                       "Data to delete cannot be found in the database");
       }
       else if (iDeleted != 1)
       {
          throw new OSSInconsistentDataException(
                       "Inconsistent database contains multiple ("
                       + iDeleted + ") data object with the same ID");
       }
    }
 }

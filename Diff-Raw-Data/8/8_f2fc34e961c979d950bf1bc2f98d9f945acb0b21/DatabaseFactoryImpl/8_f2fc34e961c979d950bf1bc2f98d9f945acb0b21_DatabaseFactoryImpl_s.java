 /*
 * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 package org.opensubsystems.core.persist.jdbc.impl;
 
 import org.opensubsystems.core.data.DataDescriptor;
 import org.opensubsystems.core.data.DataDescriptorManager;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.Database;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.util.OSSObject;
 
 /**
  * Base class for all database factories. This class is mainly collecting 
  * reusable code that can be useful for other factories.
  *
  * @author bastafidli
  */
 public abstract class DatabaseFactoryImpl extends OSSObject 
                                              implements DatabaseFactory
 {
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Database on which this database factory should operate.
     */
    private Database m_database;
    
    /**
     * Class identifying data descriptor for the object. This is private so that 
     * we can maintain the cached object value without fear that these two won't 
     * be in sync.
     */
    private Class<DataDescriptor> m_clsDataDescriptor;
    
    // Cached values ////////////////////////////////////////////////////////////
    
    /**
     * Data descriptor describing the current data object. This is a cached copy
     * and in order to ensure it is in sync with the source, it is private.
     */
    private DataDescriptor m_dataDescriptor;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Default constructor for the factory providing the default view of the data 
     * objects.
     * 
     * @param database - database on which this database factory should operate 
     * @param clsDataDescriptor - class identifying data descriptor for the data 
     *                            objects managed by this factory.
     * @throws OSSException - an error has occurred
     */
    public DatabaseFactoryImpl(
       Database              database,
       Class<DataDescriptor> clsDataDescriptor
    ) throws OSSException
    {
       super();
       
       m_database = database;
       // Construct the descriptor immediately so if there is an exception if
       // happens here so that we can call all the other methods without worrying
       // about exception handling
       m_clsDataDescriptor = clsDataDescriptor;
 
       // Cached some values for convenient access from factories.
       m_dataDescriptor = DataDescriptorManager.getInstance(m_clsDataDescriptor);
 
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public Database getDatabase(
    )
    {
       return m_database;
    }
 
    /**
     * {@inheritDoc}
     */
    public Class<DataDescriptor> getDataDescriptorClass(
    )
    {
       return m_clsDataDescriptor;
    }
    
    /**
     * {@inheritDoc}
     */
    public DataDescriptor getDataDescriptor(
    )
    {
       return m_dataDescriptor;
    }
 }

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
 
 package org.opensubsystems.core.persist.jdbc;
 
 import java.util.List;
 
 import org.opensubsystems.core.error.OSSDynamicClassException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.DataFactory;
 import org.opensubsystems.core.util.ClassFactory;
 import org.opensubsystems.core.util.GlobalConstants;
 
 /**
  * Class factory responsible for instantiation of database factories. The 
  * correct factory is instantiated based on the factory interface or default 
  * implementation and currently active database instantiate correct database 
  * factory which should be used for this database.
  * 
  * Assuming name of the class aaa.AAAFactory
  * 1. try class aaa.db.AAADatabaseFactory
  * 
  * @author bastafidli
  */
 public class DatabaseFactoryClassFactory extends ClassFactory<DatabaseFactory>
 {
    /**
     * Constructor
     */
    public DatabaseFactoryClassFactory()
    {
       super(DatabaseFactory.class);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DatabaseFactory verifyInstance(
       Object objInstance
    ) throws OSSException
    {
       // Make sure that the instantiated class is of type DataFactory 
       // and DatabaseFactory
       if ((objInstance != null) && (!(objInstance instanceof DataFactory))
          && (!(objInstance instanceof DatabaseFactory)))
       {
          throw new OSSDynamicClassException("Instantiated class "
                                             + " doesn't implements DataFactory"
                                             + " and DatabaseFactory"
                                             + " interface and is of type "
                                             + objInstance.getClass().getName());
                       
       }
       
       return super.verifyInstance(objInstance);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createDefaultClassNames(
       String       strClassIdentifier,
       String       strModifier,
       List<String> lstClassNames
    ) throws OSSException
    {
      int           iIndex;
      int           iIndex2;
      StringBuilder sbClassName = new StringBuilder();
       
       // Assuming name of the class aaa.AAAFactory
       
       // Find package separator
       iIndex = strClassIdentifier.lastIndexOf('.');
       // Find end of name
       iIndex2 = strClassIdentifier.lastIndexOf("Factory");
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert iIndex2 != -1 
                 : "The factory class identifier is expected to end with name Factory";
       }
       // Check if it was found to do not produce StringIndexOutOfBoundsException
       if (iIndex2 != -1)
       {
          // Transform to the class aaa.db.AAADatabaseFactory
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append("db.");
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, iIndex2));
          sbClassName.append("DatabaseFactory");
          lstClassNames.add(sbClassName.toString());
       }
    }
 }

 /*
  *************************************************************************
  * Copyright (c) 2004, 2005 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *  
  *************************************************************************
  */
 
 package org.eclipse.birt.data.engine.executor;
 
 import org.eclipse.birt.data.engine.odi.IParameterMetaData;
 import org.eclipse.birt.data.engine.odaconsumer.DataTypeUtil;
 
 /**
  * Implementation class of the ODI IParameterMetaData interface.
  */
 public class ParameterMetaData implements IParameterMetaData
 {
     org.eclipse.birt.data.engine.odaconsumer.ParameterMetaData		m_odaMetaData;
     
     ParameterMetaData( org.eclipse.birt.data.engine.odaconsumer.ParameterMetaData odaMetaData )
     {
         assert odaMetaData != null;
         m_odaMetaData = odaMetaData;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#isInputMode()
      */
     public Boolean isInputMode()
     {
         return m_odaMetaData.isInputMode();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#isOutputMode()
      */
     public Boolean isOutputMode()
     {
         return m_odaMetaData.isOutputMode();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getName()
      */
     public String getName()
     {
    	if ( m_odaMetaData.getName( ) != null )
			return m_odaMetaData.getName( );
    	else
    		return m_odaMetaData.getNativeName( );
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getPosition()
      */
     public int getPosition()
     {
         return m_odaMetaData.getPosition();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getValueClass()
      */
     public Class getValueClass()
     {
         // maps ODA metadata java.sql.Types to class
         return DataTypeUtil.toTypeClass( m_odaMetaData.getDataType() );
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#isOptional()
      */
     public Boolean isOptional()
     {
         return m_odaMetaData.isOptional();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getDefaultInputValue()
      */
     public String getDefaultInputValue()
     {
         return m_odaMetaData.getDefaultValue();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getNativeTypeName()
      */
     public String getNativeTypeName()
     {
         return m_odaMetaData.getNativeTypeName();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getScale()
      */
     public int getScale()
     {
         return m_odaMetaData.getScale();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#getPrecision()
      */
     public int getPrecision()
     {
         return m_odaMetaData.getPrecision();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.birt.data.engine.odi.IParameterMetaData#isNullable()
      */
     public Boolean isNullable()
     {
         return m_odaMetaData.isNullable();
     }
 
 }

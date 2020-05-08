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
 
 package org.opensubsystems.core.util;
 
 import java.util.List;
 
 import org.opensubsystems.core.error.OSSException;
 
 /**
  * Class factory responsible for instantiation of classes whose implementation
  * depends on some kind of aspect or technology identified by modifier. This 
  * class factory tries to instantiate the correct class based on what modifier 
  * is currently used.
  * 
  * Assuming name of the interface aaa.AAA
  * 1. try class name aaa.modifier.ModifierAAAImpl
  * 2. try class name aaa.modifier.ModifierAAA
  * 3. try class name aaa.impl.ModifierAAAImpl
  * 4. try class name aaa.ModifierAAAImpl
  * 5. try class name aaa.ModifierAAA
  * 6. try class name aaa.modifier.AAAImpl
  * 7. try class name aaa.modifier.AAA
  * 8. try class in the form of aaa.impl.AAAImpl (from base class)
  * 9. try aaa.AAAImpl (from base class)
  * 10. try directly class aaa.AAA (from base class)
  * 
  * @author bastafidli
  */
 public class ModifierClassFactory<T> extends ImplementationClassFactory<T>
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Modifier used to construct classes.
     */
    protected String m_strModifier;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor
     * 
     * @param type - type for objects instantiated by this factory 
     * @param strModifier - modifier that should be used to construct the classes
     */
    public ModifierClassFactory(
       Class<? extends T> type,
       String             strModifier
    )
    {
       super(type);
       m_strModifier = strModifier;
    }
   
    // Helper methods ///////////////////////////////////////////////////////////
    
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
      int          iIndex;
      StringBuffer sbClassName = new StringBuffer();
       
       // Assuming name of the interface aaa.AAA
       
       // Find package separator
       iIndex = strClassIdentifier.lastIndexOf('.');
 
       // First try class name aaa.modifier.ModifierAAAImpl
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier);            
       }
       sbClassName.append("Impl");
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       
       // Then try class name aaa.modifier.ModifierAAA
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier);            
       }
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       
       // Then try class name aaa.impl.ModifierAAAImpl
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append("impl");
          sbClassName.append(".");
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append("impl");
          sbClassName.append(".");
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier);            
       }
       sbClassName.append("Impl");
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       
       // Then try class name aaa.ModifierAAAImpl
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier);            
       }
       sbClassName.append("Impl");
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       
       // Then try class name aaa.ModifierAAA
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append(strModifier);
          sbClassName.append(strClassIdentifier);            
       }
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       
       // Then try class aaa.modifier.AAAImpl
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strClassIdentifier);            
       }
       sbClassName.append("Impl");
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
 
       // Then try class aaa.modifier.AAA
       if (iIndex != -1)
       {
          // There is a package
          sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strClassIdentifier.substring(iIndex + 1, 
                                strClassIdentifier.length()));
       }
       else
       {
          sbClassName.append(strModifier.toLowerCase());
          sbClassName.append(".");
          sbClassName.append(strClassIdentifier);            
       }
       lstClassNames.add(sbClassName.toString());
       
       super.createDefaultClassNames(strClassIdentifier, strModifier, lstClassNames);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getModifier(
    ) throws OSSException
    {
       return m_strModifier;
    }         
 }

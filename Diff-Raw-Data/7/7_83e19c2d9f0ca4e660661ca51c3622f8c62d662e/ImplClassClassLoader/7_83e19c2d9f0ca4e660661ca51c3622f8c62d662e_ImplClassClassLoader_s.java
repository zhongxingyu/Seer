 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.classloader;
 
 import java.net.URLClassLoader;
 
 import org.eclipse.jubula.tools.constants.CommandConstants;
 
 /**
  * This Classloader tries to load the classes with the given ClassLoaders.
  * {@inheritDoc}
  *
  * @author BREDEX GmbH
  * @created 04.05.2006
  */
 public class ImplClassClassLoader extends JBUrlClassLoader {
 
     /** The alternative ClassLoader*/ 
     private ClassLoader m_componentCL;
     
     
     /**
      * Constructor.
      * @param autServerCL the ClassLoader of the AUT-Server
      * @param componentCL ClassLoader of the componentCL
      */
     public ImplClassClassLoader(URLClassLoader autServerCL, 
         ClassLoader componentCL) {
         
         super(autServerCL.getURLs(), autServerCL);
         m_componentCL = componentCL;
     }
 
     /**
      * 
      * {@inheritDoc}
      * @param name
      * @param resolve
      * @return
      * @throws ClassNotFoundException
      */
     protected synchronized Class loadClass(String name, boolean resolve) 
         throws ClassNotFoundException {
         
         Class c = null;
         // in case of c is an ImplClass
        if (name.startsWith(CommandConstants.SWING_IMPLCLASS_PACKAGE)
             || name.startsWith(CommandConstants.SWT_IMPLCLASSES_PACKAGE)
             || (name.indexOf(CommandConstants.JUBULA_EXTENSION_PACKAGE) 
                != -1)) {
             
             return implLoadClass(name, resolve);
         }
         try {
             // in case of any other class not involved with ImplClasses
             c = super.getParent().loadClass(name);
         } catch (ClassNotFoundException e) {
             // e.g. in case of special AUT-component used in ImplClasses
             c = implLoadClass(name, resolve);
         }
         return c;
     }
     
     /**
      * Tries to load the classes with this ClassLoader and then, if not
      * successfull, with the alternative ClassLoader.
      * @param name name
      * @param resolve resolve
      * @return Class
      * @throws ClassNotFoundException ClassNotFoundException
      */
     private Class implLoadClass(String name, boolean resolve) 
         throws ClassNotFoundException {
         
         Class c = null;
         try {
             // first try with parent
             c = super.loadClass(name, resolve);
         }  catch (ClassNotFoundException e) {
             // alternative try
             c = m_componentCL.loadClass(name);
         } 
         return c;
     }
     
     
     /**
      * This method tries to load the classes with the ClassLoaders given to the 
      * Constructor.
      * 
      * @param name the name of the Class
      * {@inheritDoc}
      * @return the loaded Class
      * @throws ClassNotFoundException if no class was found.
      */
     public Class loadClass(String name) throws ClassNotFoundException {
         return loadClass(name, false);
     }
     
 }

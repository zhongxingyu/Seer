 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.context.resolver.structureddocument;
 
 import java.util.List;
 
 import org.eclipse.jst.jsf.context.resolver.IDocumentContextResolver;
 
 /**
  * Resolves meta-data for a particular context
  * 
  * This interface may sub-classed or implemented by clients
  * 
  * @author cbateman
  *
  */
 public interface IMetadataContextResolver extends IDocumentContextResolver 
 {
     /**
      * @param key 
     * @param uri
     * @param elementName
     * @param attribute
      * @return a list of one or more String values associated with key
      * for the current context location. 
      */
     List getPropertyValue(String key);
 }

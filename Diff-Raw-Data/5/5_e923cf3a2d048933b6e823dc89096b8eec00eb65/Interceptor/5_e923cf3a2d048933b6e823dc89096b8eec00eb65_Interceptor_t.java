 /*******************************************************************************
  * Copyright (c) 2008, 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.swordfish.core;
 
 import java.util.Map;
 
 import javax.jbi.messaging.MessageExchange;
 
 
 /**
  *  The base interceptor interface that provides the processing logic. Interceptors
 *  are registered as OSGI services in order to be plugged into the NMR by the
  *  Swordfish core.
  *
  */
 public interface Interceptor {
 
 	/**
 	 * Name of the only mandatory property which must be provided when an
 	 * interceptor is registered. It is used by the Strategy components
 	 * plugged into the Swordfish core in order to decide if and in which
 	 * place an Interceptor has to be invoked.
 	 */
 	String TYPE_PROPERTY = "type";
 
     /**
      * @param exchange the messageExchange to be processed
     * @throws RuntimeException if the processing error occurred and some
      *  specific error handling activities should take place in the InterceptorExceptionListener
      */
     void process(MessageExchange exchange) throws SwordfishException;
 
 
     /**
      * By using this method the implementation class can supply properties
      * associated with the current interceptor e.g priority. These properties
      * are merged with the properties provided at OSGI service registration
      * and via Swordfish configuration. Details see at
      * {@link FilterStrategy#filter(java.util.List, ReadOnlyRegistry, java.util.List)}
      * and
      * {@link SortingStrategy#sort(java.util.List, ReadOnlyRegistry, java.util.List)}.
      * @return Properties in a Map of String keys and arbitrary values.
      */
     Map<String,?> getProperties();
 
 }

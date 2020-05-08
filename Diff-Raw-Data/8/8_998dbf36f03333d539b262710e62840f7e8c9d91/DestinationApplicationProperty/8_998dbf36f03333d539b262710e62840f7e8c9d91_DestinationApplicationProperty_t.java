 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.properties;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.facade.properties.AbstractProperty;
 
 /**
  * FoG consists of a partial routing service. It is possible to utilize source routing in order to signalize the
  * path from the source to the destination. However it is also possible to use lose source routing. Via that property it
  * is possible to define the path to the destination partially. Once the last hop of the predefined path is reached,
  * the address of the target is evaluated. If the entire path to the target can be provided, that path is inserted into
  * the packet. If a node consists of the knowlege of how to reach the target node it does not necessarily know the gate
  * number that leads to the target application. Therefore you can use the node name as target and add this requirement in
  * order to route your packet to that application. Normally it only makes sense in case you wish to establish a connection
  * to a target application.
  * 
  */
 public class DestinationApplicationProperty extends AbstractProperty
 {
 	private static final long serialVersionUID = 8299856490405894908L;
 
 	/**
 	 * Stores the application name space
 	 */
 	private Namespace mAppNamespace = null;
 	
 	/**
 	 * Stores the name of the application
 	 */
 	private Name mAppName = null;
 	
 	/**
 	 * Stores an optional application parameter
 	 */
 	private Object mAppParameter;
 	
 	/**
 	 * Constructor 
 	 * 
 	 * @param pAppNamespace the name space of the application (can be null)
 	 * @param pAppName the name of the application (can be null)
 	 * @param pAppParameter the parameter for the application (can be null)
 	 */
 	public DestinationApplicationProperty(Namespace pAppNamespace, Name pAppName, Object pAppParameter)
 	{
 		mAppNamespace = pAppNamespace;
 		mAppName = pAppName;
 		mAppParameter = pAppParameter;
 	}
 	
 	/**
 	 * Constructor 
 	 * 
	 * @param pAppNamespace the name space of the application (can be null)
 	 * @param pAppName the name of the application (can be null)
 	 * @param pAppParameter the parameter for the application (can be null)
 	 */
	public DestinationApplicationProperty(Namespace pAppNamespace)
 	{
		this(pAppNamespace, null, null);
 	}
 
 	/**
 	 * Returns the application name
 	 *  
 	 * @return the application name
 	 */
 	public Name getAppName()
 	{
 		return mAppName;
 	}
 	
 	/**
 	 * Returns the application parameter
 	 * 
 	 * @return the application parameter
 	 */
 	public Object getAppParameter()
 	{
 		return mAppParameter;
 	}
 	
 	/**
 	 * Returns the application name space
 	 * 
 	 * @return the application name space
 	 */
 	public Namespace getAppNamespace()
 	{
 		return mAppNamespace;
 	}
 
 	/**
 	 * Generates a descriptive string about this object
 	 * 
 	 * @return the descriptive string
 	 */
 	public String toString()
 	{
 		return getClass().getSimpleName() + "(DestAppl=" + mAppNamespace + "://" + (mAppName != null ? mAppName: "" ) + ", AppParams=" +  mAppParameter + ")";
 	}
 }

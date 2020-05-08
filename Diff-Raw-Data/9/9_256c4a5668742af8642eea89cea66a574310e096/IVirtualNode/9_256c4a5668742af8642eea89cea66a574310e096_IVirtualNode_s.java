 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.hierarchical.clustering;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.routing.naming.hierarchical.HRMID;
 
 /**
 * This class is used to represent nodes within the HRM system. Such a virtual node can be either clusters 
 * to which addresses are assigned to or it can be connection end points that are associated to participants of the
 * clusters at the lowest hierarchical level. 
 * 
 * It is called virtual node because it can be either a physical node or a super node that aggregates multiple nodes 
 * (physical nodes) or super nodes (clusters). 
  */
 public interface IVirtualNode extends Name
 {
 	/**
 	 * 
 	 * @return The address of that specific node can be returned here
 	 */
 	public HRMID getHrmID();
 }

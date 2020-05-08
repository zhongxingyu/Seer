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
 * This class is used to identify nodes within the HRM routing. Such an identifier can be either for a cluster, 
 * to which addresses are assigned, or for a CEP that is associated to members of a clusters at the lowest hierarchical level. 
 * It can be either a physical node or a logical node (a cluster) that aggregates multiple physical nodes. 
  */
 public interface IVirtualNode extends Name
 {
 	/**
 	 * 
 	 * @return The address of that specific node can be returned here
 	 */
 	public HRMID getHrmID();
 }

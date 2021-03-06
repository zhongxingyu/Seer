 /*******************************************************************************
  *  Copyright (c) 2007, 2010 IBM Corporation and others.
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  * 
  *  Contributors:
  *     IBM Corporation - initial API and implementation
  *     Sonatype, Inc. - ongoing development
  *******************************************************************************/
 package org.eclipse.equinox.p2.planner;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.equinox.p2.engine.*;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 
 /**
  * Planners are responsible for determining what should be done to a given 
  * profile to reshape it as requested. That is, given the current state of a 
  * profile, a description of the desired end state of that profile and metadata 
  * describing the available IUs, a planner produces a plan that lists the
  * provisioning operands that the engine should perform.  
 * @since 1.0
  */
 public interface IPlanner {
 	/**
 	 * Service name constant for the planner service.
 	 */
 	public static final String SERVICE_NAME = IPlanner.class.getName();
 
 	/**
 	 * 
 	 * @param profileChangeRequest the request to be evaluated
 	 * @param context the context in which the request is processed
 	 * @param monitor a monitor on which planning
 	 * @return the plan representing the system that needs to be
 	 */
 	public IProvisioningPlan getProvisioningPlan(IProfileChangeRequest profileChangeRequest, ProvisioningContext context, IProgressMonitor monitor);
 
 	public IProvisioningPlan getDiffPlan(IProfile currentProfile, IProfile targetProfile, IProgressMonitor monitor);
 
 	public IProfileChangeRequest createChangeRequest(IProfile profileToChange);
 
 	public IInstallableUnit[] updatesFor(IInstallableUnit iu, ProvisioningContext context, IProgressMonitor monitor);
 }

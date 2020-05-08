 /*-
  * Copyright (c) 2013 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.dawb.common.services;
 
 /**
  * This service can be called to set the class loader on a thread.
  * The class loader will include classes which commonly are communicated between
  * two data analysis processes. For instance if workflows and the UI are 
  * communicating.
  * 
  * The class loader will include things like AbstractDataset and beans used
 * to communicate tool data.
  * 
  * Usage:
  * <code>
  * IClassLoaderService service = (IClassLoaderService)PlatformUI.getWorkbench().getService(IClassLoaderService.class);
  * 
  * try {
  *     service.setDataAnalysisClassLoaderActive(true);
  *     
  *     // Communicate
  *     
  * } finally {
  *     service.setDataAnalysisClassLoaderActive(false);
  * }
  *     
  *     
  */
 public interface IClassLoaderService {
 
 	/**
	 * Call to activate a class loader with data analysis classes available
 	 *  
 	 * @param active
 	 */
 	public void setDataAnalysisClassLoaderActive(boolean active);
 }

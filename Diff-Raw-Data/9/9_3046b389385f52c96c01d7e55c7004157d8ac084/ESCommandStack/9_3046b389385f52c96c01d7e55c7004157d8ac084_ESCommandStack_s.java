 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.changetracking;
 

 /**
  * Command Stack for EMFStore. Allows to register observer for command start and completion.
  * 
  * @author mkoegel
  */
 public interface ESCommandStack {
 	/**
 	 * Add a command stack observer.
 	 * 
 	 * @param observer the observer
 	 */
 	void addCommandStackObserver(ESCommandObserver observer);
 
 	/**
 	 * Remove COmmand stack observer.
 	 * 
 	 * @param observer the observer
 	 */
 	void removeCommandStackObserver(ESCommandObserver observer);
 }

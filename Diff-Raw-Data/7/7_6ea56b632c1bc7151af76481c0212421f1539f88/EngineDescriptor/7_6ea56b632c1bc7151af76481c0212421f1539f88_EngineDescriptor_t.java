 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.match.service;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.emf.compare.EMFComparePlugin;
 import org.eclipse.emf.compare.match.api.MatchEngine;
 
 /**
  * Contribution representation one may give throught the "match engine"
  * extension point.
  * 
  * @author Cedric Brun <cedric.brun@obeo.fr>
  * 
  */
 public class EngineDescriptor implements Comparable {
 	protected String priority;
 
 	protected String fileExtension;
 
 	protected String engineClassName;
 
 	protected IConfigurationElement element;
 
 	/**
 	 * Initializer from IConfigurationElement
 	 * 
 	 * @param element
 	 */
 	public EngineDescriptor(IConfigurationElement element) {
 		this.element = element;
 		fileExtension = getAttribute("fileExtension", "*"); //$NON-NLS-1$ //$NON-NLS-2$
 		priority = getAttribute("priority", "low");  //$NON-NLS-1$//$NON-NLS-2$
 		engineClassName = getAttribute("engineClass", null); //$NON-NLS-1$
 	}
 
 	/**
 	 * Return a descriptor attribute
 	 * 
 	 * @param name
 	 * @param defaultValue
 	 * @return the descriptor attribute
 	 */
 	private String getAttribute(String name, String defaultValue) {
 		String value = element.getAttribute(name);
 		if (value != null)
 			return value;
 		if (defaultValue != null)
 			return defaultValue;
 		throw new IllegalArgumentException("Missing " + name + " attribute");//$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	/**
 	 * Return the file extension the engine should handle
 	 * 
 	 * @return the file extension
 	 */
 	public String getFileExtension() {
 		return fileExtension;
 	}
 
 	/**
 	 * Return the engine priority
 	 * 
 	 * @return the engine priority
 	 */
 	public String getPriority() {
 		return priority.toLowerCase();
 	}
 
 	/**
 	 * Return the engine class name
 	 * 
 	 * @return the engine class name
 	 */
 	public String getEngineClassName() {
 		return engineClassName;
 	}
 
 	private MatchEngine engine = null;
 
 	/**
 	 * 
 	 * @return the engine instance
 	 */
 	public MatchEngine getEngineInstance() {
 		if (engine == null)
 		{
 			try {
 				engine = (MatchEngine) element
 						.createExecutableExtension("engineClass"); //$NON-NLS-1$
 			} catch (CoreException e) {
 				EMFComparePlugin.getDefault().log(e, false);
 			}
 		}
 		return engine;
 	}
 
 	private int getPriorityValue(String priority) {
 		if (priority.equals("lowest")) //$NON-NLS-1$
 			return 1;
 		if (priority.equals("low")) //$NON-NLS-1$
 			return 2;
 		if (priority.equals("normal")) //$NON-NLS-1$
 			return 3;
 		if (priority.equals("high")) //$NON-NLS-1$
 			return 4;
 		if (priority.equals("highest")) //$NON-NLS-1$
 			return 5;
 		return 0;
 	}
 
 	@Override
 	public int hashCode() {
 		final int PRIME = 31;
 		int result = 1;
 		result = PRIME * result
 				+ ((engineClassName == null) ? 0 : engineClassName.hashCode());
 		result = PRIME * result
 				+ ((fileExtension == null) ? 0 : fileExtension.hashCode());
 		result = PRIME * result
 				+ ((priority == null) ? 0 : priority.hashCode());
 		return result;
 	}
 
 	public int compareTo(Object other) {
 		if (other instanceof EngineDescriptor) {
 			int nombre1 = getPriorityValue(((EngineDescriptor) other)
 					.getPriority());
 			int nombre2 = getPriorityValue(this.getPriority());
 			if (nombre1 > nombre2)
 				return -1;
 			else if (nombre1 == nombre2)
 				return 0;
 			else
 				return 1;
 		} 
 		return 1;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		final EngineDescriptor other = (EngineDescriptor) obj;
 		if (engineClassName == null) {
 			if (other.engineClassName != null)
 				return false;
 		} else if (!engineClassName.equals(other.engineClassName))
 			return false;
 		if (fileExtension == null) {
 			if (other.fileExtension != null)
 				return false;
 		} else if (!fileExtension.equals(other.fileExtension))
 			return false;
 		if (priority == null) {
 			if (other.priority != null)
 				return false;
 		} else if (!priority.equals(other.priority))
 			return false;
 		return true;
 	}
 }

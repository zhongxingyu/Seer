 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 
 /**
  * Extension point must contain nature entry and priority entry. Other entries
  * aren't required.
  * 
  * @author Haiodo
  * 
  */
 public class BasicDLTKExtensionManager {
 	public final static String PRIORITY_ATTR = "priority";
 
 	private final static String NATURE_ATTR = "nature";
 	private Map extensions;
 
 	private String extensionPoint = null;
	private String identifier = NATURE_ATTR;
	
	protected void setIdentifierValue(String identifier ) {
		this.identifier = identifier;
		if( this.extensions != null ) {
			this.extensions = null;
		}
	}
 
 	public static class ElementInfo {
 		int level;
 		protected IConfigurationElement config;
 		public Object object;
 
 		protected ElementInfo(IConfigurationElement config) {
 			this.config = config;
 		}
 
 		public IConfigurationElement getConfig() {
 			return this.config;
 		}
 	}
 
 	public BasicDLTKExtensionManager(String extensionPoint) {
 		this.extensionPoint = extensionPoint;
 		Assert.isNotNull(this.extensionPoint);
 	}
 
 	private void initialize() {
 		if (extensions != null) {
 			return;
 		}
 
 		extensions = new HashMap(5);
 		IConfigurationElement[] cfg = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(this.extensionPoint);
 
 		for (int i = 0; i < cfg.length; i++) {
 			String nature = cfg[i].getAttribute(this.identifier);
 			ElementInfo oldInfo = (ElementInfo) extensions.get(nature);
 			if (oldInfo != null) {
 				int lev = getLevel(cfg[i]);
 				if (lev <= oldInfo.level) {
 					continue;
 				}
 			}
 			ElementInfo info = createInfo(cfg[i]);
 			info.level = getLevel(info.config);
 			extensions.put(nature, info);
 		}
 	}
 
 	protected ElementInfo getElementInfo(String nature) {
 		initialize();
 		return (ElementInfo) extensions.get(nature);
 	}
 
 	protected int getLevel(IConfigurationElement config) {
 		String priority = config.getAttribute(PRIORITY_ATTR);
 		if (priority == null) {
 			return 0;
 		}
 		try {
 			int parseInt = Integer.parseInt(priority);
 			return parseInt;
 		} catch (NumberFormatException ex) {
 			return 0;
 		}
 	}
 
 	public ElementInfo[] getElementInfos() {
 		initialize();
 		Collection values = extensions.values();
 		return (ElementInfo[]) values.toArray(new ElementInfo[values.size()]);
 	}
 
 	/**
 	 * Values config, nature, level are setted in current class.
 	 * 
 	 * @param config
 	 * @return
 	 */
 	protected ElementInfo createInfo(IConfigurationElement config) {
 		return new ElementInfo(config);
 	}
 
 	public String findScriptNature(IProject project)
 			throws CoreException {
 		try {
 			String[] natureIds = project.getDescription().getNatureIds();
 			for (int i = 0; i < natureIds.length; i++) {
 				String natureId = natureIds[i];
 
 				if (getElementInfo(natureId) != null) {
 					return natureId;
 				}
 			}
 		} catch (CoreException e) {
 			return null;
 		}
 
 		return null;
 	}
 }

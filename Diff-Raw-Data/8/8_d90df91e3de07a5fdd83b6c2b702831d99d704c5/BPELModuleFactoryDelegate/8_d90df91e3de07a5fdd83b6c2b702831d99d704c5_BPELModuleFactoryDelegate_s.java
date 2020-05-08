 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.bpel.runtimes.module;
 
 import org.eclipse.core.resources.IProject;
 import org.jboss.ide.eclipse.as.wtp.core.modules.JBTProjectModuleDelegate;
 import org.jboss.ide.eclipse.as.wtp.core.modules.JBTProjectModuleFactory;
 import org.jboss.tools.bpel.runtimes.IBPELModuleFacetConstants;
 
 public class BPELModuleFactoryDelegate extends JBTProjectModuleFactory {
 	public static final String FACTORY_ID = "org.jboss.tools.bpel.runtimes.module.moduleFactory";
 	public static final String MODULE_TYPE = IBPELModuleFacetConstants.BPEL_MODULE_TYPE;
	private static BPELModuleFactoryDelegate factDelegate;
 
	public static BPELModuleFactoryDelegate getFactory() {
		if (factDelegate == null)
			factDelegate = (BPELModuleFactoryDelegate)getFactory(FACTORY_ID);
		return factDelegate;
 	}
 
 	public BPELModuleFactoryDelegate() {
 		super(MODULE_TYPE, IBPELModuleFacetConstants.BPEL_PROJECT_FACET);
 	}
 
 	protected JBTProjectModuleDelegate createDelegate(IProject project) {
 		return new BPELModuleDelegate(project);
 	}
 }

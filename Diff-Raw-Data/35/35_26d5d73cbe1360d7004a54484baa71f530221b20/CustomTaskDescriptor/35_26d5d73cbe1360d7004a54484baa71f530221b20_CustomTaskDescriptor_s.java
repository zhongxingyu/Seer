 /*******************************************************************************
  * Copyright (c) 2011, 2012 Red Hat, Inc.
  *  All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Red Hat, Inc. - initial API and implementation
  *
  * @author Bob Brodt
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.core.runtime;
 
 import org.eclipse.bpmn2.modeler.core.features.activity.task.ICustomTaskFeatureContainer;
 
 public class CustomTaskDescriptor extends ModelExtensionDescriptor {
 
 	protected ICustomTaskFeatureContainer featureContainer;
 	protected boolean permanent;
 	
 	public CustomTaskDescriptor(String id, String name) {
 		super(id,name);
 	}
 	
 	public ICustomTaskFeatureContainer getFeatureContainer() {
 		return featureContainer;
 	}
 
 	public void setFeatureContainer(ICustomTaskFeatureContainer featureContainer) {
 		this.featureContainer = featureContainer;
 	}
 	
 	public boolean isPermanent() {
 		return permanent;
 	}
 
 	public void setPermanent(boolean permanent) {
 		this.permanent = permanent;
 	}
 }

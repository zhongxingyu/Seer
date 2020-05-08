 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.stepper.steps;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.stepper.StepperAttributeUtil;
 import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepAttributes;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.stepper.nls.Messages;
 
 /**
  * An abstract step implementation.
  */
 public abstract class AbstractStep extends ExecutableExtension implements IStep {
 	// List of string id's of the step dependencies.
 	private final List<String> dependencies = new ArrayList<String>();
 
 	// Map of parameters of the step reference
 	private Map<String,String> parameters = new HashMap<String,String>();
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#isSingleton()
 	 */
 	@Override
 	public boolean isSingleton() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 		super.doSetInitializationData(config, propertyName, data);
 
 		// Read in the list of required step or step id's if specified.
 		dependencies.clear();
 		IConfigurationElement[] requires = config.getChildren("requires"); //$NON-NLS-1$
 		for (IConfigurationElement require : requires) {
 			String value = require.getAttribute("id"); //$NON-NLS-1$
 			if (value == null || value.trim().length() == 0) {
 				throw new CoreException(new Status(IStatus.ERROR,
 								CoreBundleActivator.getUniqueIdentifier(),
 								0,
 								NLS.bind(Messages.AbstractStep_error_missingRequiredAttribute, "dependency id (requires)",  getLabel()), //$NON-NLS-1$
 								null));
 			}
 			if (!dependencies.contains(value.trim())) {
 				dependencies.add(value.trim());
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#initializeFrom(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void initializeFrom(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
 		Assert.isNotNull(context);
 		Assert.isNotNull(data);
 		Assert.isNotNull(fullQualifiedId);
 		Assert.isNotNull(monitor);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#cleanup(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void cleanup(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor) {
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#rollback(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.core.runtime.IStatus, org.eclipse.tcf.te.runtime.stepper.interfaces.IFullQualifiedId, org.eclipse.core.runtime.IProgressMonitor, org.eclipse.tcf.te.runtime.interfaces.callback.ICallback)
 	 */
 	@Override
 	public void rollback(IStepContext context, IPropertiesContainer data, IStatus status, IFullQualifiedId fullQualifiedId, IProgressMonitor monitor, ICallback callback) {
 		if (callback != null) {
 			callback.done(this, Status.OK_STATUS);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#getTotalWork(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext, org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer)
 	 */
 	@Override
 	public int getTotalWork(IStepContext context, IPropertiesContainer data) {
		return 10;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#getDependencies()
 	 */
 	@Override
 	public String[] getDependencies() {
 		return dependencies.toArray(new String[dependencies.size()]);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#setParameters(java.util.Map)
 	 */
 	@Override
 	public void setParameters(Map<String,String> parameters) {
 		if (parameters != null) {
 			this.parameters = parameters;
 		}
 		else {
 			this.parameters = Collections.EMPTY_MAP;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStep#getParameters()
 	 */
 	@Override
 	public Map<String, String> getParameters() {
 		return parameters;
 	}
 
 	/**
 	 * Invoke the specified callback and pass on the status and user defined data object.
 	 *
 	 * @param data The data giving object. Must not be <code>null</code>.
 	 * @param fullQualifiedId The full qualfied id for this step. Must not be <code>null</code>.
 	 * @param callback The callback.
 	 * @param status The status.
 	 * @param data The callback data.
 	 */
 	public final void callback(IPropertiesContainer data, IFullQualifiedId fullQualifiedId, ICallback callback, IStatus status, Object callbackData) {
 		Assert.isNotNull(data);
 		Assert.isNotNull(fullQualifiedId);
 		Assert.isNotNull(callback);
 		Assert.isNotNull(status);
 
 		callback.setProperty(IStep.CALLBACK_PROPERTY_DATA, callbackData);
 		callback.done(this, status);
 	}
 
 	/**
 	 * Returns the active context that is currently used.
 	 *
 	 * @param context The step context. Must not be <code>null</code>.
 	 * @param data The data giving object. Must not be <code>null</code>.
 	 * @param fullQualifiedId The full qualfied id for this step. Must not be <code>null</code>.
 	 * @return The active context or <code>null</code>.
 	 */
 	protected Object getActiveContext(IStepContext context, IPropertiesContainer data, IFullQualifiedId fullQualifiedId) {
 		Assert.isNotNull(data);
 		Assert.isNotNull(fullQualifiedId);
 		Object activeContext = StepperAttributeUtil.getProperty(IStepAttributes.ATTR_ACTIVE_CONTEXT, fullQualifiedId, data);
 		if (activeContext == null)
 			activeContext = context.getContextObject();
 
 		return activeContext;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep#getCancelTimeout()
 	 */
 	@Override
 	public int getCancelTimeout() {
 		// default timeout is 1 minute
 	    return 60000;
 	}
 }

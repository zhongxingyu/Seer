 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.runtime.stepper.extensions;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.expressions.EvaluationResult;
 import org.eclipse.core.expressions.Expression;
 import org.eclipse.core.expressions.ExpressionConverter;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
 import org.eclipse.tcf.te.runtime.extensions.ExecutableExtensionProxy;
 import org.eclipse.tcf.te.runtime.interfaces.extensions.IExecutableExtension;
 import org.eclipse.tcf.te.runtime.stepper.StepperManager;
 import org.eclipse.tcf.te.runtime.stepper.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IExtendedStep;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStep;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupIterator;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroupable;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.tracing.ITraceIds;
 import org.eclipse.tcf.te.runtime.stepper.nls.Messages;
 
 /**
  * A default step group implementation.
  */
 public class StepGroup extends ExecutableExtension implements IStepGroup {
 
 	private boolean locked;
 	private String baseOn;
 
 	private final List<ReferenceSubElement> references = new ArrayList<ReferenceSubElement>();
 
 	private ExecutableExtensionProxy<IStepGroupIterator> iteratorProxy = null;
 
 	/**
 	 * Constant to be returned in case the step group contains no steps.
 	 */
 	protected final static IStepGroupable[] NO_STEPS = new IStepGroupable[0];
 
 	/**
 	 * Step group reference sub element.
 	 */
 	protected final static class ReferenceSubElement implements org.eclipse.core.runtime.IExecutableExtension {
 		private String id;
 		private String secondaryId;
 		private String insertBefore;
 		private String insertAfter;
 		private String overwrite;
 		private boolean removable;
 		private boolean hidden;
 		private boolean disable;
 		private boolean singleton;
 		private final List<String> dependencies = new ArrayList<String>();
 		private Expression expression;
 		private Map<String,String> parameters = new HashMap<String, String>();
 
 		/**
 		 * Returns the id of the referenced step or step group.
 		 *
 		 * @return The id of the referenced step or step group.
 		 */
 		public String getId() {
 			return id;
 		}
 
 		/**
 		 * Returns the secondary id of the referenced step or step group.
 		 *
 		 * @return The secondary id or <code>null</code>.
 		 */
 		public String getSecondaryId() {
 			return secondaryId;
 		}
 
 		/**
 		 * Sets the secondary id of the referenced step or step group.
 		 *
 		 * @return The secondary id or <code>null</code>.
 		 */
 		public void setSecondardId(String secondaryId) {
 			this.secondaryId = secondaryId;
 		}
 
 		/**
 		 * Returns the id of the step or step group the referenced
 		 * step or group shall be inserted before.
 		 *
 		 * @return The id or <code>null</code>.
 		 */
 		public String getInsertBefore() {
 			return insertBefore;
 		}
 
 		/**
 		 * Returns the id of the step or step group the referenced
 		 * step or group shall be inserted after.
 		 *
 		 * @return The id or <code>null</code>.
 		 */
 		public String getInsertAfter() {
 			return insertAfter;
 		}
 
 		/**
 		 * Returns the id of the step or step group the referenced
 		 * step or group do overwrite.
 		 *
 		 * @return The id or <code>null</code>.
 		 */
 		public String getOverwrite() {
 			return overwrite;
 		}
 
 		/**
 		 * Returns if or if not the referenced step or step group
 		 * can be removed by the user from the group.
 		 *
 		 * @return <code>True</code> if removable, <code>false</code> otherwise.
 		 */
 		public boolean isRemovable() {
 			return removable;
 		}
 
 		/**
 		 * Returns if or if not the referenced step or step group
 		 * is hidden.
 		 *
 		 * @return <code>True</code> if hidden, <code>false</code> otherwise.
 		 */
 		public boolean isHidden() {
 			return hidden;
 		}
 
 		/**
 		 * Returns if or if not to disable the referenced step or step group.
 		 *
 		 * @return <code>True</code> if to disable, <code>false</code> otherwise.
 		 */
 		public boolean isDisable() {
 			return disable;
 		}
 
 		/**
 		 * Returns if or if not the referenced step or step group is a singleton.
 		 *
 		 * @return <code>True</code> if singleton, <code>false</code> otherwise.
 		 */
 		public boolean isSingleton() {
 			return singleton;
 		}
 
 		/**
 		 * Returns the list of dependencies.
 		 * <p>
 		 * The step or step group id might be fully qualified using the form
 		 * <code>&quot;primaryId##secondaryId</code>. The <code>secondaryId</code> is optional.
 		 *
 		 * @return The list of dependencies or an empty list.
 		 */
 		public String[] getDependencies() {
 			return dependencies.toArray(new String[dependencies.size()]);
 		}
 
 		/**
 		 * Returns the enablement expression which is associated with this reference.
 		 *
 		 * @return The enablement expression or <code>null</code>.
 		 */
 		public Expression getEnablement() {
 			return expression;
 		}
 
 		/**
 		 * Returns the parameters for the step.
		 *
 		 * @return The parameters or an empty Map.
 		 */
 		public Map<String,String> getParameters() {
 			return parameters;
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
 		 */
 		@Override
 		public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 			if (config == null) {
 				return;
 			}
 
 			String value = config.getAttribute("id"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.id = value.trim();
 			}
 
 			value = config.getAttribute("secondaryId"); //$NON-NLS-1$
 			setSecondardId(value != null && value.trim().length() > 0 ? value.trim() : null);
 
 			value = config.getAttribute("insertBefore"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.insertBefore = value.trim();
 			}
 
 			value = config.getAttribute("insertAfter"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.insertAfter = value.trim();
 			}
 
 			value = config.getAttribute("overwrite"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.overwrite = value.trim();
 			}
 
 			value = config.getAttribute("removable"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.removable = Boolean.parseBoolean(value.trim());
 			}
 
 			value = config.getAttribute("hidden"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.hidden = Boolean.parseBoolean(value.trim());
 			}
 
 			value = config.getAttribute("disable"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.disable = Boolean.parseBoolean(value.trim());
 			}
 
 			value = config.getAttribute("singleton"); //$NON-NLS-1$
 			if (value != null && value.trim().length() > 0) {
 				this.singleton = Boolean.parseBoolean(value.trim());
 			}
 
 			// Read in the list of dependencies if specified.
 			dependencies.clear();
 			IConfigurationElement[] requires = config.getChildren("requires"); //$NON-NLS-1$
 			for (IConfigurationElement require : requires) {
 				value = require.getAttribute("id"); //$NON-NLS-1$
 				if (value == null || value.trim().length() == 0) {
 					throw new CoreException(new Status(IStatus.ERROR,
 									CoreBundleActivator.getUniqueIdentifier(),
 									0,
 									NLS.bind(Messages.AbstractStep_error_missingRequiredAttribute,
 													"dependency id (requires)", //$NON-NLS-1$
 													config.getName()),
 													null));
 				}
 				if (!dependencies.contains(value.trim())) {
 					dependencies.add(value.trim());
 				}
 			}
 
 			// Read the sub elements of the extension
 			IConfigurationElement[] enablements = config.getChildren("enablement"); //$NON-NLS-1$
 			// The "enablement" element is the only expected one
 			if (enablements != null && enablements.length > 0) {
 				expression = ExpressionConverter.getDefault().perform(enablements[0]);
 			}
 
 			// Read the sub elements of the extension
 			IConfigurationElement[] params = config.getChildren("parameter"); //$NON-NLS-1$
 			// The "paramter" element is the only expected one
 			if (params != null && params.length > 0) {
 				for (IConfigurationElement parameter : params) {
 					parameters.put(parameter.getAttribute("name"), parameter.getAttribute("value")); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (getId() != null && obj instanceof ReferenceSubElement) {
 				boolean secondaryIdEquals = false;
 				if (getSecondaryId() == null) {
 					secondaryIdEquals = ((ReferenceSubElement)obj).getSecondaryId() == null;
 				}
 				else {
 					secondaryIdEquals = getSecondaryId().equals(((ReferenceSubElement)obj).getSecondaryId());
 				}
 
 				return getId().equals(((ReferenceSubElement)obj).getId()) && secondaryIdEquals;
 			}
 			return super.equals(obj);
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			return getId() != null ? getId().hashCode() + (getSecondaryId() != null ? getSecondaryId().hashCode() : 0) : super.hashCode();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			StringBuffer buffer = new StringBuffer(getClass().getSimpleName());
 			buffer.append(": "); //$NON-NLS-1$
 			buffer.append("id = " + getId()); //$NON-NLS-1$
 			buffer.append(", secondaryId = " + getSecondaryId()); //$NON-NLS-1$
 			buffer.append(", insertBefore = " + getInsertBefore()); //$NON-NLS-1$
 			buffer.append(", insertAfter = " + getInsertAfter()); //$NON-NLS-1$
 			buffer.append(", overwrite = " + getOverwrite()); //$NON-NLS-1$
 			buffer.append(", removable = " + isRemovable()); //$NON-NLS-1$
 			buffer.append(", hidden = " + isHidden()); //$NON-NLS-1$
 			buffer.append(", disable = " + isDisable()); //$NON-NLS-1$
 			buffer.append(", singleton = " + isSingleton()); //$NON-NLS-1$
 			buffer.append(", parameters = " + getParameters()); //$NON-NLS-1$
 			return buffer.toString();
 		}
 	}
 
 	/**
 	 * Constructor.
 	 */
 	public StepGroup() {
 		super();
 		locked = false;
 		baseOn = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.AbstractContextStepGroup#isLocked()
 	 */
 	@Override
 	public boolean isLocked() {
 		return locked;
 	}
 
 	/**
 	 * Returns the id of the step group this step group is
 	 * initially based on.
 	 *
 	 * @return The id or <code>null</code>.
 	 */
 	protected String getBaseOn() {
 		return baseOn;
 	}
 
 	/**
 	 * Returns the references.
 	 */
 	protected List<ReferenceSubElement> getReferences() {
 		return references;
 	}
 
 	/**
 	 * Check for duplicates of the referenced step or step group. The check will fail if multiple
 	 * occurrence of a step or step group are found and the step or step group is supposed to be a
 	 * singleton.
 	 *
 	 * @param steps The list of steps. Must not be <code>null</code>.
 	 * @param reference The reference. Must not be <code>null</code>.
 	 *
 	 * @throws CoreException If multiple occurrences of singleton references are found.
 	 */
 	protected void checkForDuplicates(List<IStepGroupable> steps, ReferenceSubElement reference) throws CoreException {
 		Assert.isNotNull(steps);
 		Assert.isNotNull(reference);
 
 		// If the reference overwrites another reference, it is not a duplicate
 		String overwrite = reference.getOverwrite();
 		if (overwrite != null && overwrite.length() > 0) {
 			return;
 		}
 
 		boolean checkFailed = false;
 
 		for (IStepGroupable step : steps) {
 			if (step.getExtension().getId().equals(reference.getId())) {
 				// We've found an existing groupable with the reference id.
 				// If either the groupable, the reference or the extension is
 				// marked singleton, than this is an failure.
 				checkFailed = step.isSingleton() || reference.isSingleton()
 								|| (step.getExtension() instanceof IExtendedStep
 												&& ((IExtendedStep)step.getExtension()).isSingleton());
 				if (checkFailed) {
 					break;
 				}
 			}
 		}
 
 		if (checkFailed) {
 			throw new CoreException(new Status(IStatus.ERROR,
 							CoreBundleActivator.getUniqueIdentifier(),
 							MessageFormat.format(Messages.StepGroup_error_multipleSingletonOccurrences,
 											NLS.bind(Messages.StepGroup_error_stepGroup, getLabel()),
 											NLS.bind(Messages.StepGroup_error_referencedStepOrGroup, reference.getId()))
 							));
 		}
 	}
 
 	/**
 	 * Replace all references to a given step or step group with another one.
 	 *
 	 * @param steps The list of steps. Must not be <code>null</code>.
 	 * @param oldId The id of the step or step group to replace. Must not be <code>null</code>.
 	 * @param replacement The replacement. Must not be <code>null</code>.
 	 * @param reference The reference sub element. Must not be <code>null</code>.
 	 *
 	 * @return The list of affected groupable's or an empty list.
 	 */
 	protected List<IStepGroupable> onOverwrite(List<IStepGroupable> steps, String oldId, IExecutableExtension replacement, ReferenceSubElement reference) {
 		Assert.isNotNull(steps);
 		Assert.isNotNull(oldId);
 		Assert.isNotNull(replacement);
 		Assert.isNotNull(reference);
 
 		List<IStepGroupable> affected = new ArrayList<IStepGroupable>();
 
 		// Isolate primary and secondary id
 		String primaryId = oldId;
 		String secondaryId = null;
 
 		String[] splitted = oldId.split("##", 2); //$NON-NLS-1$
 		if (splitted.length == 2) {
 			primaryId = splitted[0];
 			secondaryId = splitted[1];
 		}
 
 		for (IStepGroupable step : steps) {
 			// A step is clearly affected if the primary id and the secondary
 			// id (if any) matches the overwritten id
 			if (step.getExtension().getId().equals(primaryId)
 							&& (secondaryId == null || secondaryId.equals(step.getSecondaryId()))) {
 				if (step instanceof StepGroupable) {
 					StepGroupable groupable = ((StepGroupable)step);
 					// Update the grouped extension
 					groupable.setExtension(replacement);
 					// Update the groupable secondary id
 					groupable.setSecondaryId(reference.getSecondaryId());
 					// Add the groupable to the list of affected steps
 					affected.add(step);
 				}
 			}
 
 			// A step is affected as well if the step depends on the overwritten step
 			// In this case we have to update the dependencies.
 			List<String> dependencies = new ArrayList<String>(Arrays.asList(step.getDependencies()));
 			if (dependencies.contains(oldId)) {
 				String fullId = replacement.getId() + (reference.getSecondaryId() != null ? "##" + reference.getSecondaryId() : ""); //$NON-NLS-1$ //$NON-NLS-2$
 				// We have to replace the dependency at the exact position within the list
 				dependencies.set(dependencies.indexOf(oldId), fullId);
 				if (step instanceof StepGroupable) {
 					((StepGroupable)step).setDependencies(dependencies.toArray(new String[dependencies.size()]));
 				}
 			}
 		}
 
 		return affected;
 	}
 
 	/**
 	 * Insert the step before the specified step or step group. If no step or
 	 * step group with the given id exist, the step is added to the end.
 	 *
 	 * @param steps The list of steps. Must not be <code>null</code>.
 	 * @param id The id of the step or step group where to insert the new step before. Must not be <code>null</code>.
 	 * @param newStep The step to add. Must not be <code>null</code>.
 	 * @param reference The reference sub element. Must not be <code>null</code>.
 	 *
 	 * @return The list of affected groupable's or an empty list.
 	 */
 	protected List<IStepGroupable> onInsertBefore(List<IStepGroupable> steps, String id, IExecutableExtension newStep, ReferenceSubElement reference) {
 		Assert.isNotNull(steps);
 		Assert.isNotNull(id);
 		Assert.isNotNull(newStep);
 		Assert.isNotNull(reference);
 
 		List<IStepGroupable> affected = new ArrayList<IStepGroupable>();
 
 		// Isolate primary and secondary id
 		String primaryId = id;
 		String secondaryId = null;
 
 		String[] splitted = id.split("##", 2); //$NON-NLS-1$
 		if (splitted.length == 2) {
 			primaryId = splitted[0];
 			secondaryId = splitted[1];
 		}
 
 		// Always loop over all steps in case the anchor step is available
 		// multiple times. In such case, the new step is inserted at all
 		// occurrences.
 		for (int i = 0; i < steps.size(); i++) {
 			IStepGroupable step = steps.get(i);
 			if (!step.getExtension().getId().equals(primaryId)) {
 				continue;
 			}
 			if (secondaryId != null && !secondaryId.equals(step.getSecondaryId())) {
 				continue;
 			}
 
 			// Create a new groupable object for inserting
 			IStepGroupable groupable = new StepGroupable(newStep, reference.getSecondaryId());
 			// Insert the new step at the current position
 			steps.add(i, groupable);
 			// And increase the counter --> Otherwise we would see the
 			//                              same step we want to insert before again!
 			i++;
 			// Add the new groupable to the list of affected steps
 			affected.add(groupable);
 		}
 
 		// If the step could not be added, add to the end of the list
 		if (affected.isEmpty()) {
 			IStepGroupable groupable = new StepGroupable(newStep, reference.getSecondaryId());
 			steps.add(groupable);
 		}
 
 		return affected;
 	}
 
 	/**
 	 * Insert the step after the specified step or step group. If no step or
 	 * step group with the given id exist, the step is added to the end.
 	 *
 	 * @param steps The list of steps. Must not be <code>null</code>.
 	 * @param id The id of the step or step group where to insert the new step after. Must not be <code>null</code>.
 	 * @param newStep The step to add. Must not be <code>null</code>.
 	 * @param reference The reference sub element. Must not be <code>null</code>.
 	 *
 	 * @return The list of affected groupable's or an empty list.
 	 */
 	protected List<IStepGroupable> onInsertAfter(List<IStepGroupable> steps, String id, IExecutableExtension newStep, ReferenceSubElement reference) {
 		Assert.isNotNull(steps);
 		Assert.isNotNull(id);
 		Assert.isNotNull(newStep);
 		Assert.isNotNull(reference);
 
 		List<IStepGroupable> affected = new ArrayList<IStepGroupable>();
 
 		// Isolate primary and secondary id
 		String primaryId = id;
 		String secondaryId = null;
 
 		String[] splitted = id.split("##", 2); //$NON-NLS-1$
 		if (splitted.length == 2) {
 			primaryId = splitted[0];
 			secondaryId = splitted[1];
 		}
 
 		// Always loop over all steps in case the anchor step is available
 		// multiple times. In such case, the new step is inserted at all
 		// occurrences.
 		for (int i = 0; i < steps.size(); i++) {
 			IStepGroupable step = steps.get(i);
 			if (!step.getExtension().getId().equals(primaryId)) {
 				continue;
 			}
 			if (secondaryId != null && !secondaryId.equals(step.getSecondaryId())) {
 				continue;
 			}
 
 			// Create a new groupable object for inserting
 			IStepGroupable groupable = new StepGroupable(newStep, reference.getSecondaryId());
 			// Insert the new groupable after the current step or at the end (if i + 1 == steps.size())
 			steps.add(i + 1, groupable);
 			// Add the new groupable to the list of affected steps
 			affected.add(groupable);
 		}
 
 		// If the step could not be added, add to the end of the list
 		if (affected.isEmpty()) {
 			IStepGroupable groupable = new StepGroupable(newStep, reference.getSecondaryId());
 			steps.add(groupable);
 		}
 
 		return affected;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup#getSteps(org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext)
 	 */
 	@Override
 	public IStepGroupable[] getSteps(IStepContext context) throws CoreException {
 		Assert.isNotNull(context);
 
 		// The list of resolved steps for the specified type and mode
 		List<IStepGroupable> steps = new ArrayList<IStepGroupable>();
 
 		// If this step group is based on another step group, we have to get the resolved
 		// steps from there first.
 		if (getBaseOn() != null) {
 			IStepGroup baseStepGroup = StepperManager.getInstance().getStepGroupExtManager().getStepGroup(getBaseOn(), true);
 			// If the base step group cannot be found, that's an error. We cannot continue
 			// without the base group.
 			if (baseStepGroup == null) {
 				throw new CoreException(new Status(IStatus.ERROR,
 								CoreBundleActivator.getUniqueIdentifier(),
 								MessageFormat.format(Messages.StepGroup_error_missingBaseStepGroup,
 												NLS.bind(Messages.StepGroup_error_stepGroup, getLabel()),
 												NLS.bind(Messages.StepGroup_error_referencedBaseGroup, getBaseOn()))
 								));
 			}
 
 			// Add all the steps from the base step group now to the list
 			steps.addAll(Arrays.asList(baseStepGroup.getSteps(context)));
 		}
 
 		// Now process the references and modify the steps list accordingly
 		for (ReferenceSubElement reference : getReferences()) {
 			// Get the step or step group for the referenced id. Try the steps first.
 			IExecutableExtension candidate = getCandidate(reference.getId(), reference);
 
 			// If the candidate is null here, that's an error as a referenced step is missing.
 			if (candidate == null) {
 				throw new CoreException(new Status(IStatus.ERROR,
 								CoreBundleActivator.getUniqueIdentifier(),
 								MessageFormat.format(Messages.StepGroup_error_missingReferencedStep,
 												NLS.bind(Messages.StepGroup_error_stepGroup, getLabel()),
 												NLS.bind(Messages.StepGroup_error_referencedStepOrGroup, reference.getId()))
 								));
 			}
 
 			// Check if the step is valid for the current contexts.
 			boolean valid = isValidReference(reference, context);
 
 			if (!valid) {
 				CoreBundleActivator.getTraceHandler().trace(
 								"StepGroup#getSteps: SKIPPED reference = '" + candidate.getLabel() + "'." //$NON-NLS-1$ //$NON-NLS-2$
 								+ " Not valid for contexts '" + context,  //$NON-NLS-1$
 								0, ITraceIds.TRACE_STEPPING, IStatus.WARNING, this);
 				continue;
 			}
 
 			// Check for duplicates of singleton references.
 			checkForDuplicates(steps, reference);
 
 			// Check for the steps own dependencies to be valid for the current type id and mode
 			if (candidate instanceof IStep) {
 				checkForDependenciesValid((IStep)candidate, context);
 			}
 
 			// Will contain the list of affected groupables from the whole list
 			List<IStepGroupable> affectedGroupables = new ArrayList<IStepGroupable>();
 
 			// Check first for overwriting groupables
 			String overwrite = reference.getOverwrite();
 			if (overwrite != null && overwrite.length() > 0) {
 				affectedGroupables.addAll(onOverwrite(steps, overwrite, candidate, reference));
 			} else {
 				// overwrite is not set -> process insertBefore or insertAfter
 				String insertBefore = reference.getInsertBefore();
 				String insertAfter = reference.getInsertAfter();
 
 				// If neither one is specified, the step or step group will be to the end of the list
 				if ((insertBefore == null || insertBefore.length() == 0)
 								&& (insertAfter == null || insertAfter.length() == 0)) {
 					IStepGroupable groupable = new StepGroupable(candidate, reference.getSecondaryId());
 					steps.add(groupable);
 					affectedGroupables.add(groupable);
 				} else {
 					// insertBefore comes first
 					if (insertBefore != null && insertBefore.length() > 0) {
 						affectedGroupables.addAll(onInsertBefore(steps, insertBefore, candidate, reference));
 					} else {
 						affectedGroupables.addAll(onInsertAfter(steps, insertAfter, candidate, reference));
 					}
 				}
 			}
 
 			// Process the groupable attributes on all affected groupables
 			for (IStepGroupable step : affectedGroupables) {
 				if (!(step instanceof StepGroupable)) {
 					continue;
 				}
 
 				StepGroupable groupable = (StepGroupable)step;
 				groupable.setDependencies(reference.getDependencies());
 
 				if (!reference.isRemovable() && groupable.isRemovable()) {
 					groupable.setRemovable(reference.isRemovable());
 				}
 				if (reference.isHidden() && !groupable.isHidden()) {
 					groupable.setHidden(reference.isHidden());
 				}
 				if (reference.isDisable() && !groupable.isDisabled()) {
 					groupable.setDisabled(reference.isDisable());
 				}
 				if (reference.isSingleton() && !groupable.isSingleton()) {
 					groupable.setSingleton(reference.isSingleton());
 				}
 			}
 		}
 
 		return !steps.isEmpty() ? steps.toArray(new IStepGroupable[steps.size()]) : NO_STEPS;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.extensions.ExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 		references.clear();
 		super.setInitializationData(config, propertyName, data);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.extensions.AbstractStepGroup#doSetInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
 	 */
 	@Override
 	public void doSetInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
 		super.doSetInitializationData(config, propertyName, data);
 
 		if (iteratorProxy == null) {
 			iteratorProxy = new ExecutableExtensionProxy<IStepGroupIterator>(config) {
 				@Override
 				protected String getExecutableExtensionAttributeName() {
 					return "iterator"; //$NON-NLS-1$
 				}
 			};
 		}
 
 		if (!locked) {
 			String lockedAttribute = config.getAttribute("locked"); //$NON-NLS-1$
 			if (lockedAttribute != null) {
 				this.locked = Boolean.parseBoolean(lockedAttribute);
 			}
 		}
 
 		if (baseOn == null || baseOn.trim().length() == 0) {
 			String baseOnAttribute = config.getAttribute("baseOn"); //$NON-NLS-1$
 			if (baseOnAttribute != null && baseOnAttribute.trim().length() > 0) {
 				this.baseOn = baseOnAttribute.trim();
 			}
 		}
 
 		Map<String, Integer> occurrences = new HashMap<String, Integer>();
 		IConfigurationElement[] childElements = config.getChildren("references"); //$NON-NLS-1$
 		for (IConfigurationElement childElement : childElements) {
 			IConfigurationElement[] references = childElement.getChildren("reference"); //$NON-NLS-1$
 			for (IConfigurationElement reference : references) {
 				ReferenceSubElement candidate = new ReferenceSubElement();
 				candidate.setInitializationData(reference, reference.getName(), null);
 				// If multiple references to the same step or step group exist, check
 				// for the secondaryId
 				if (occurrences.containsKey(candidate.getId())) {
 					// Occurrences are counted up always
 					int number = occurrences.get(candidate.getId()).intValue() + 1;
 					occurrences.put(candidate.getId(), Integer.valueOf(number));
 
 					if (candidate.getSecondaryId() == null) {
 						// secondaryId not explicitly set -> auto set
 						candidate.setSecondardId(Integer.toString(number));
 					}
 				} else {
 					// remember the occurrence of the reference
 					occurrences.put(candidate.getId(), Integer.valueOf(1));
 				}
 
 				// References are not sorted out here. That's the task of the resolver.
 				this.references.add(candidate);
 			}
 		}
 	}
 
 
 	/**
 	 * Checks if all dependencies of the given step are available
 	 * and valid for the given type id and mode.
 	 *
 	 * @param step The step. Must not be <code>null</code>.
 	 * @param context The context. Must not be <code>null</code>.
 	 *
 	 * @throws CoreException If a required step or step group is not available or not valid.
 	 */
 	protected void checkForDependenciesValid(IStep step, IStepContext context) throws CoreException {
 		Assert.isNotNull(step);
 		Assert.isNotNull(context);
 
 		String[] dependencies = step.getDependencies();
 		for (String dependency : dependencies) {
 			IExecutableExtension candidate = getCandidate(dependency, null);
 
 			// If the candidate is null here, that's an error as a required step or step group is missing.
 			if (candidate == null) {
 				throw new CoreException(new Status(IStatus.ERROR,
 								CoreBundleActivator.getUniqueIdentifier(),
 								MessageFormat.format(Messages.StepGroup_error_missingRequiredStep,
 												NLS.bind(Messages.StepGroup_error_step, step.getLabel()),
 												NLS.bind(Messages.StepGroup_error_requiredStepOrGroup, dependency))
 								));
 			}
 		}
 	}
 
 	/**
 	 * Returns a candidate for the given reference.
	 * @param reference The reference.
 	 * @return The candidate. Can be either an IStep or IStepGroup.
 	 */
 	protected IExecutableExtension getCandidate(String id, ReferenceSubElement reference) {
 		Assert.isNotNull(id);
 		IExecutableExtension candidate = StepperManager.getInstance().getStepExtManager().getStep(id, true);
 		if (candidate == null) {
 			candidate = StepperManager.getInstance().getStepGroupExtManager().getStepGroup(id, true);
 		}
 		else if (reference != null && candidate instanceof IStep) {
 			((IStep)candidate).setParameters(reference.getParameters());
 		}
 		return candidate;
 	}
 
 	/**
 	 * Returns if or if not the reference is valid for the given contexts.
 	 *
 	 * @param reference The reference. Must not be <code>null</code>.
 	 * @param context The context or <code>null</code>.
 	 *
 	 * @return <code>True</code> if the step is valid, <code>false</code> otherwise.
 	 */
 	protected boolean isValidReference(ReferenceSubElement reference, IStepContext context) {
 		Assert.isNotNull(reference);
 		// Get the enablement.
 		Expression enablement = reference.getEnablement();
 
 		if (enablement != null) {
 			if (context != null) {
 				// Set the default variable to the context.
 				EvaluationContext evalContext = new EvaluationContext(null, context);
 				// Initialize the evaluation context named variables
 				evalContext.addVariable("context", context.getContextObject()); //$NON-NLS-1$
 				evalContext.addVariable("id", context.getId()); //$NON-NLS-1$
 				// Allow plugin activation
 				evalContext.setAllowPluginActivation(true);
 				// Evaluate the expression
 				try {
 					return enablement.evaluate(evalContext).equals(EvaluationResult.TRUE);
 				} catch (CoreException e) {
 					IStatus status = new Status(IStatus.ERROR, CoreBundleActivator.getUniqueIdentifier(),
 									e.getLocalizedMessage(), e);
 					Platform.getLog(CoreBundleActivator.getContext().getBundle()).log(status);
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.stepper.interfaces.IStepGroup#getStepGroupIterator()
 	 */
 	@Override
 	public IStepGroupIterator getStepGroupIterator() {
 		return iteratorProxy != null ? iteratorProxy.newInstance() : null;
 	}
 }

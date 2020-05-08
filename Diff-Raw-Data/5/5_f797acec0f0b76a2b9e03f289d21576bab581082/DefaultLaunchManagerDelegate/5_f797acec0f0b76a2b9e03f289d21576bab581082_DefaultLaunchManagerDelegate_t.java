 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.launch.core.lm.delegates;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.tcf.te.launch.core.activator.CoreBundleActivator;
 import org.eclipse.tcf.te.launch.core.bindings.LaunchConfigTypeBindingsManager;
 import org.eclipse.tcf.te.launch.core.exceptions.LaunchServiceException;
 import org.eclipse.tcf.te.launch.core.interfaces.IReferencedProjectItem;
 import org.eclipse.tcf.te.launch.core.interfaces.tracing.ITraceIds;
 import org.eclipse.tcf.te.launch.core.lm.LaunchConfigHelper;
 import org.eclipse.tcf.te.launch.core.lm.LaunchConfigSorter;
 import org.eclipse.tcf.te.launch.core.lm.LaunchSpecification;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ICommonLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.IFileTransferLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchAttribute;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchContextLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification;
 import org.eclipse.tcf.te.launch.core.lm.interfaces.IReferencedProjectLaunchAttributes;
 import org.eclipse.tcf.te.launch.core.nls.Messages;
 import org.eclipse.tcf.te.launch.core.persistence.DefaultPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.persistence.filetransfer.FileTransfersPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.persistence.launchcontext.LaunchContextsPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.persistence.projects.ReferencedProjectsPersistenceDelegate;
 import org.eclipse.tcf.te.launch.core.preferences.IPreferenceKeys;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
 import org.eclipse.tcf.te.runtime.extensions.ExecutableExtension;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.services.interfaces.filetransfer.IFileTransferItem;
 
 /**
  * Default launch manager delegate implementation.
  */
 public class DefaultLaunchManagerDelegate extends ExecutableExtension implements ILaunchManagerDelegate {
 
 	/**
 	 * Constructor.
 	 */
 	public DefaultLaunchManagerDelegate() {
 		super();
 	}
 
 	protected void copySpecToConfig(ILaunchSpecification launchSpec, ILaunchConfigurationWorkingCopy wc) {
 		for (ILaunchAttribute attribute : launchSpec.getAllAttributes()) {
 			LaunchConfigHelper.addLaunchConfigAttribute(wc, attribute.getKey(), attribute.getValue());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#initLaunchConfigAttributes(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public void initLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec) {
 		Assert.isNotNull(wc);
 		Assert.isNotNull(launchSpec);
 
 		if (!DefaultPersistenceDelegate.hasAttribute(wc, ICommonLaunchAttributes.ATTR_UUID)) {
 			DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_UUID, UUID.randomUUID().toString());
 		}
 
 		validateLaunchSpecification(launchSpec);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#updateLaunchConfigAttributes(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public void updateLaunchConfigAttributes(ILaunchConfigurationWorkingCopy wc, ILaunchSpecification launchSpec) {
 		Assert.isNotNull(wc);
 		Assert.isNotNull(launchSpec);
 
 		if (!DefaultPersistenceDelegate.hasAttribute(wc, ICommonLaunchAttributes.ATTR_UUID)) {
 			DefaultPersistenceDelegate.setAttribute(wc, ICommonLaunchAttributes.ATTR_UUID, UUID.randomUUID().toString());
 		}
 
 		validateLaunchSpecification(launchSpec);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#updateLaunchConfig(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext, boolean)
 	 */
 	@Override
 	public void updateLaunchConfig(ILaunchConfigurationWorkingCopy wc, ISelectionContext selContext, boolean replace) {
 		Assert.isNotNull(wc);
 		Assert.isNotNull(selContext);
 	}
 
 	@Override
 	public boolean isDefaultAttribute(String attributeKey, Object specValue, Object confValue, ILaunchSpecification launchSpec, ILaunchConfiguration launchConfig, String launchMode) {
 		Assert.isNotNull(attributeKey);
 		Assert.isNotNull(launchConfig);
 		Assert.isNotNull(launchSpec);
 		Assert.isNotNull(launchMode);
 		if (confValue == null && specValue != null) {
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#getMatchingLaunchConfigurations(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification, org.eclipse.debug.core.ILaunchConfiguration[])
 	 */
 	@Override
 	public final ILaunchConfiguration[] getMatchingLaunchConfigurations(ILaunchSpecification launchSpec, ILaunchConfiguration[] launchConfigs) throws LaunchServiceException {
 		if (launchConfigs == null || launchConfigs.length == 0) {
 			return new ILaunchConfiguration[0];
 		}
 		else if (launchSpec != null) {
 			List<LaunchConfigSorter> rankedList = new ArrayList<LaunchConfigSorter>();
 			for (ILaunchConfiguration launchConfig : launchConfigs) {
 				if (CoreBundleActivator.getTraceHandler()
 								.isSlotEnabled(0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING)) {
 					System.out.println("\n***\n"); //$NON-NLS-1$
 				}
 
 				int ranking = 0;
 				try {
 					ranking = getLaunchConfigRanking(launchSpec, launchConfig);
 				}
 				catch (LaunchServiceException e) {
 					switch (e.getType()) {
 					case LaunchServiceException.TYPE_MISSING_LAUNCH_CONFIG_ATTR:
						ranking = -1;
 						break;
 					default:
 						throw e;
 					}
 				}
 
 				int fullMatchRanking = getFullMatchRanking();
 
 				if (CoreBundleActivator.getTraceHandler()
 								.isSlotEnabled(0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING)) {
 					StringBuilder message = new StringBuilder("Ranking launch spec ("); //$NON-NLS-1$
 					message.append(launchSpec.getLaunchConfigName());
 					message.append(") vs launch configuration ("); //$NON-NLS-1$
 					message.append(launchConfig.getName());
 					message.append(") = "); //$NON-NLS-1$
 					message.append(ranking);
 					message.append(" ; full match ranking = "); //$NON-NLS-1$
 					message.append(fullMatchRanking);
 
 					CoreBundleActivator
 					.getTraceHandler()
 					.trace(message.toString(), 0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING, IStatus.INFO, this);
 				}
 
				if (ranking >= 0 && ranking >= fullMatchRanking) {
 					rankedList.add(new LaunchConfigSorter(launchConfig, ranking));
 				}
 			}
 
 			// sort results and write back into array
 			Collections.sort(rankedList);
 			ILaunchConfiguration[] matchingConfigs = new ILaunchConfiguration[rankedList.size()];
 			for (int i = 0; i < rankedList.size(); i++) {
 				matchingConfigs[i] = rankedList.get(i).getConfig();
 			}
 
 			return matchingConfigs;
 		}
 		else {
 			return launchConfigs;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#getLaunchSpecification(java.lang.String, org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection)
 	 */
 	@Override
 	public final ILaunchSpecification getLaunchSpecification(String launchConfigTypeId, ILaunchSelection launchSelection) {
 		ILaunchSpecification spec = null;
 
 		if (isValidLaunchSelection(launchSelection)) {
 			spec = new LaunchSpecification(launchConfigTypeId, launchSelection.getLaunchMode());
 
 			for (ISelectionContext selectionContext : launchSelection.getSelectedContexts()) {
 				// For launch specifications, all selection contexts needs to be set as preferred
 				// for full validation.
 				// otherwise "not preferred" contexts are valid even if they are not for a give
 				// launch configuration type id.
 				boolean oldPref = selectionContext.isPreferredContext();
 				selectionContext.setIsPreferredContext(true);
 				if (LaunchConfigTypeBindingsManager
 								.getInstance()
 								.isValidLaunchConfigType(launchConfigTypeId, launchSelection.getLaunchMode(), selectionContext)) {
 					spec = addLaunchSpecAttributes(spec, launchConfigTypeId, selectionContext);
 				}
 				selectionContext.setIsPreferredContext(oldPref);
 			}
 
 			// If the number of selected contexts is 0, we have to call addLaunchSpecAttributes
 			// one time to add the selection independent attributes.
 			if (launchSelection.getSelectedContexts().length == 0) {
 				spec = addLaunchSpecAttributes(spec, launchConfigTypeId, null);
 			}
 		}
 
 		return spec;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#getDefaultLaunchName(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public String getDefaultLaunchName(ILaunchSpecification launchSpec) {
 		return Messages.DefaultLaunchManagerDelegate_defaultLaunchName;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#getDefaultLaunchName(org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public String getDefaultLaunchName(ILaunchConfiguration launchConfig) {
 		return Messages.DefaultLaunchManagerDelegate_defaultLaunchName;
 	}
 
 	/**
 	 * Add all needed selections to the launch specification.
 	 * <p>
 	 * Subclasses needs to override and super-call this method!
 	 * <p>
 	 * <b>Note:</b> The selection context can be <code>null</code>. In this case, the method
 	 * implementation is expected to add only selection independent attributes!
 	 *
 	 * @param launchSpec The launch specification to add selections to. Must not be
 	 *            <code>null</code>.
 	 * @param launchConfigTypeId The launch configuration type id. Must not be <code>null</code>.
 	 * @param selectionContext The validated selection context with the selection(s) or
 	 *            <code>null</code>
 	 *
 	 * @return The launch specification with attributes from the selection context.
 	 */
 	protected ILaunchSpecification addLaunchSpecAttributes(ILaunchSpecification launchSpec, String launchConfigTypeId, ISelectionContext selectionContext) {
 		Assert.isNotNull(launchSpec);
 		Assert.isNotNull(launchConfigTypeId);
 		return launchSpec;
 	}
 
 	/**
 	 * Returns the ranking of the launch configuration compared to the launch specification.
 	 * <p>
 	 * If all attributes of the launch specification matches with the corresponding attributes of
 	 * the launch configuration, the ranking should be at least as high as the number of attributes
 	 * in the launch specification. The ranking should grow with every additional attribute value
 	 * that is set to default.
 	 * <p>
 	 * If only a set of attributes matches, the ranking should be less than the number of attributes
 	 * in the launch specification.
 	 * <p>
 	 * If no attribute matches zero should be returned.
 	 *
 	 * @param launchSpec The launch specification the launch configuration should be compared to.
 	 * @param launchConfig The launch configuration to find a ranking for.
 	 * @return The ranking of the launch configuration.
 	 * @throws <code>LaunchServiceException</code> exception when mandatory attributes are missing
 	 */
 	private int getLaunchConfigRanking(ILaunchSpecification launchSpec, ILaunchConfiguration launchConfig) throws LaunchServiceException {
 		int ranking = 0;
 		int mandatorys = 0;
 		Map<?, ?> configAttributes = null;
 		Set<?> configKeys = null;
 		try {
 			configAttributes = launchConfig.getAttributes();
 			configKeys = configAttributes.keySet();
 		}
 		catch (Exception e) {
 			return 0;
 		}
 
 		// Read the launch configuration matching mode from the preferences
 		int mode = CoreBundleActivator.getScopedPreferences()
 						.getInt(IPreferenceKeys.PREF_LAUNCH_CONFIG_FIND_CREATE_MODE);
 		if (launchSpec.getAttribute(IPreferenceKeys.PREF_LAUNCH_CONFIG_FIND_CREATE_MODE, null) instanceof Integer) {
 			mode = ((Integer) launchSpec
 							.getAttribute(IPreferenceKeys.PREF_LAUNCH_CONFIG_FIND_CREATE_MODE, null))
 							.intValue();
 		}
 
 		if (CoreBundleActivator.getTraceHandler()
 						.isSlotEnabled(0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING)) {
 			StringBuilder message = new StringBuilder("Ranking launch spec ("); //$NON-NLS-1$
 			message.append(launchSpec.getLaunchConfigName());
 			message.append(") vs launch configuration ("); //$NON-NLS-1$
 			message.append(launchConfig.getName());
 			message.append("): Matching mode = "); //$NON-NLS-1$
 			if (mode == IPreferenceKeys.MODE_ALWAYS_NEW) {
 				message.append(" ALWAYS_NEW "); //$NON-NLS-1$
 			}
 			else if (mode == IPreferenceKeys.MODE_FIRST_MATCHING) {
 				message.append(" FIRST_MATCHING "); //$NON-NLS-1$
 			}
 			else if (mode == IPreferenceKeys.MODE_FULL_MATCH_TARGET) {
 				message.append(" FULL_MATCH_TARGET "); //$NON-NLS-1$
 			}
 			else if (mode == IPreferenceKeys.MODE_FULL_MATCH_LAUNCH_CONFIG) {
 				message.append(" FULL_MATCH_LAUNCH_CONFIG "); //$NON-NLS-1$
 			}
 
 			CoreBundleActivator
 			.getTraceHandler()
 			.trace(message.toString(), 0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING, IStatus.INFO, this);
 		}
 
 		for (ILaunchAttribute specAttribute : launchSpec.getAllAttributes()) {
 			if (!specAttribute.isCreateOnlyAttribute()) {
 				String key = specAttribute.getKey();
 				Object specValue = specAttribute.getValue();
 				Object configValue = configAttributes.get(key);
 				int match = compareAttributeValues(key, specValue, configValue, launchSpec, launchConfig);
 
 				if (CoreBundleActivator.getTraceHandler()
 								.isSlotEnabled(0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING)) {
 					StringBuilder message = new StringBuilder("Launch spec attribute '"); //$NON-NLS-1$
 					message.append(specAttribute.getKey());
 					message.append("': mandatory = "); //$NON-NLS-1$
 					message.append(isMandatoryAttribute(key));
 					message.append("; match = "); //$NON-NLS-1$
 					if (match == NO_MATCH) {
 						message.append("NO_MATCH"); //$NON-NLS-1$
 					}
 					else if (match == PARTIAL_MATCH) {
 						message.append("PARTIAL_MATCH"); //$NON-NLS-1$
 					}
 					else if (match == FULL_MATCH) {
 						message.append("FULL_MATCH"); //$NON-NLS-1$
 					}
 					if (match != FULL_MATCH) {
 						message.append("\n\t\tspecValue = "); //$NON-NLS-1$
 						message.append(specValue != null ? specValue.toString() : "null"); //$NON-NLS-1$
 						message.append("\n\t\tconfigValue = "); //$NON-NLS-1$
 						message.append(configValue != null ? configValue.toString() : "null"); //$NON-NLS-1$
 					}
 
 					CoreBundleActivator
 					.getTraceHandler()
 					.trace(message.toString(), 0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING, IStatus.INFO, this);
 				}
 
 				if (match == PARTIAL_MATCH && mode == IPreferenceKeys.MODE_FULL_MATCH_LAUNCH_CONFIG) {
 					return 0;
 				}
 				if (match > NO_MATCH) {
 					int attrRanking = getAttributeRanking(key);
 					ranking += attrRanking * (attrRanking > 1 ? 4 : 1);
 					if (match == FULL_MATCH) {
 						ranking += attrRanking * (attrRanking > 1 ? 2 : 1);
 					}
 					configKeys.remove(key);
 					if (isMandatoryAttribute(key)) {
 						mandatorys++;
 					}
 				}
 				else {
 					return 0;
 				}
 			}
 			else {
 				if (CoreBundleActivator.getTraceHandler()
 								.isSlotEnabled(0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING)) {
 					StringBuilder message = new StringBuilder("Skipped launch spec attribute '"); //$NON-NLS-1$
 					message.append(specAttribute.getKey());
 					message.append("': is create only attribute"); //$NON-NLS-1$
 
 					CoreBundleActivator
 					.getTraceHandler()
 					.trace(message.toString(), 0, ITraceIds.TRACE_LAUNCHCONFIGURATIONMATCHING, IStatus.INFO, this);
 				}
 			}
 		}
 		if (mandatorys < getNumMandatoryAttributes()) {
 			throw new LaunchServiceException("missing mandatory attribute in ILaunchSpecification", //$NON-NLS-1$
 							LaunchServiceException.TYPE_MISSING_LAUNCH_SPEC_ATTR);
 		}
 
 		Iterator<?> configIt = configKeys.iterator();
 		while (configIt.hasNext()) {
 			String key = (String) configIt.next();
 			Object specValue = launchSpec.getAttribute(key, null);
 			Object configValue = configAttributes.get(key);
 
 			int match = compareAttributeValues(key, specValue, configValue, launchSpec, launchConfig);
 			if (match > NO_MATCH) {
 				int attrRanking = getAttributeRanking(key);
 				ranking += attrRanking * (attrRanking > 1 ? 4 : 1);
 				if (match == FULL_MATCH) {
 					ranking += attrRanking * (attrRanking > 1 ? 2 : 1);
 				}
 			}
 		}
 
 		return ranking;
 	}
 
 	/**
 	 * Returns the number of defined attributes for this launch manager delegate.
 	 *
 	 * @see #getAttributeRanking(String)
 	 */
 	protected int getNumAttributes() {
 		return 0;
 	}
 
 	/**
 	 * Returns the list of mandatory attributes for a launch specification handled by this launch
 	 * manager delegate.
 	 * <p>
 	 * The returned value must not be <code>null</code>, if no attributes are mandatory, an empty
 	 * list should be returned.
 	 *
 	 * @see #getAttributeRanking(String)
 	 */
 	protected List<String> getMandatoryAttributes() {
 		return Collections.emptyList();
 	}
 
 	private boolean validateLaunchSpecification(ILaunchSpecification launchSpec) {
 		boolean valid = true;
 		if (launchSpec == null || !launchSpec.isValid()) {
 			return false;
 		}
 		for (String attribute : getMandatoryAttributes()) {
 			if (!launchSpec.hasAttribute(attribute)) {
 				valid = false;
 				break;
 			}
 		}
 		launchSpec.setIsValid(valid);
 		return valid;
 	}
 
 	/**
 	 * Method for basic launch selection validations like checking the number of project or target
 	 * contexts.
 	 * <p>
 	 * The default implementation returns always <code>true</code>.
 	 *
 	 * @param launchSelection The launch selection to check.
 	 * @return <code>True</code> if the launch selection is valid, <code>false</code> otherwise.
 	 */
 	protected boolean isValidLaunchSelection(ILaunchSelection launchSelection) {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#validate(java.lang.String, org.eclipse.debug.core.ILaunchConfiguration)
 	 */
 	@Override
 	public void validate(String launchMode, ILaunchConfiguration launchConfig) throws LaunchServiceException {
 		StringBuilder missingAttributes = new StringBuilder();
 		for (String attribute : getMandatoryAttributes()) {
 			if (!isValidAttribute(attribute, launchConfig, launchMode)) {
 				if (missingAttributes.length() == 0) {
 					missingAttributes.append(attribute);
 				} else {
 					missingAttributes.append(", "); //$NON-NLS-1$
 					missingAttributes.append(attribute);
 				}
 			}
 		}
 		if (missingAttributes.length() > 0) {
 			throw new LaunchServiceException("Missing launch configuration attributes: " + '\n' + missingAttributes.toString(), LaunchServiceException.TYPE_MISSING_LAUNCH_CONFIG_ATTR); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Validate a single attribute for a given launch configuration and a launch mode. This method
 	 * needs to be overwritten when contributing new launch modes which does not need all attributes.
 	 *
 	 * @param attributeKey The attribute key.
 	 * @param launchConfig The launch configuration.
 	 * @param launchMode The launch mode.
 	 *
 	 * @return <code>True</code>, if the attribute value is valid.
 	 */
 	protected boolean isValidAttribute(String attributeKey, ILaunchConfiguration launchConfig, String launchMode) {
 		try {
 			if (launchConfig == null || !launchConfig.hasAttribute(attributeKey)) {
 				return false;
 			}
 		}
 		catch (CoreException e) {
 			return false;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#validate(org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchSpecification)
 	 */
 	@Override
 	public void validate(ILaunchSpecification launchSpec) throws LaunchServiceException {
 		StringBuilder missingAttributes = new StringBuilder();
 		for (String attribute : getMandatoryAttributes()) {
 			if (launchSpec == null || !launchSpec.hasAttribute(attribute)) {
 				// Remember the missing attribute for adding the list to the exception.
 				if (missingAttributes.length() == 0) {
 					missingAttributes.append(attribute);
 				} else {
 					missingAttributes.append(", "); //$NON-NLS-1$
 					missingAttributes.append(attribute);
 				}
 			}
 		}
 		if (missingAttributes.length() > 0) {
 			throw new LaunchServiceException("Missing launch specification attributes: " + '\n' + missingAttributes.toString(), LaunchServiceException.TYPE_MISSING_LAUNCH_SPEC_ATTR); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns the number of defined mandatory attributes.
 	 *
 	 * @see #getMandatoryAttributes()
 	 * @see #getAttributeRanking(String)
 	 */
 	protected final int getNumMandatoryAttributes() {
 		return getMandatoryAttributes().size();
 	}
 
 	/**
 	 * Returns true if the attribute key is in the list of mandatory attributes.
 	 *
 	 * @see #getMandatoryAttributes()
 	 * @see #getAttributeRanking(String)
 	 */
 	protected final boolean isMandatoryAttribute(String attributeKey) {
 		return getMandatoryAttributes().contains(attributeKey);
 	}
 
 	/**
 	 * Returns the ranking for the given attribute key.
 	 * <p>
 	 * The default ranking is 1, ranking of mandatory and other fundamental attributes should be
 	 * coded as <code>2^n</code>.
 	 * <p>
 	 * The more important an attribute is the higher <code>n</code> should be, <code>n</code> should
 	 * never be less than the number of attributes with a lower ranking.
 	 * <p>
 	 * Multiple attributes can have an equal ranking when one attribute can compensate the absence
 	 * of an other attribute with the same ranking.
 	 *
 	 * <pre>
 	 * Example:
 	 *
 	 *   Attributes a through f
 	 *   Attributes a and b are mandatory, b is more important than a.
 	 *   The attributes c and d should be higher prior than e and f, but should have equal ranking.
 	 *
 	 *   The ranking looks like the following:
 	 *    Attribute      Ranking
 	 *     b              32  (2^5)
 	 *     a              16  (2^4)
 	 *     c, d           4   (2^2)
 	 *     e, f           1   (2^0)
 	 *
 	 *   With this rankings it is not possible to compensate a missing higher prior attribute with
 	 *   one or more lower prior attributes.
 	 *
 	 *   Additional methods returns the following values for this example:
 	 *
 	 *    getNumAttributes()          == 6
 	 *    getMandatoryAttibutes()     == {a, b}
 	 *    getNumMandatoryAttributes() == 2
 	 *    getFullMatchRanking()       >= 48 (the value can be greater than 48 depending
 	 *                                       on the implementation of the launch manager delegate)
 	 * </pre>
 	 *
 	 * @param attributeKey The attribute key for which the ranking should be returned
 	 */
 	protected int getAttributeRanking(String attributeKey) {
 		return 1;
 	}
 
 	/**
 	 * Minimum ranking for a launch configuration to be handled as full match when comparing to a
 	 * launch specification.
 	 * <p>
 	 * Should be overwritten when the method {@link #getAttributeRanking(String)} was overwritten.
 	 *
 	 * @see #getAttributeRanking(String)
 	 */
 	protected int getFullMatchRanking() {
 		return 1;
 	}
 
 	/**
 	 * Compares an attribute value of launch configuration and specification.
 	 *
 	 * If both values are not null, calls {@link #equals(String, Object, Object, ILaunchSpecification, ILaunchConfiguration, String)}
 	 * to compare the values. If one of the values is <code>null</code>, and the not <code>null</code> value is default, <code>true</code>
 	 * is returned. If both values are <code>null</code>, <code>true</code> is returned.
 	 *
 	 * @param attributeKey The attribute key. Must not be <code>null</code>.
 	 * @param specValue The launch specification value.
 	 * @param confValue The launch configuration value.
 	 * @param launchSpec The launch specification which is the source of the <code>specValue</code>. Must not be <code>null</code>.
 	 * @param launchConfig The launch configuration which is the source of the <code>confValue</code>. Must not be <code>null</code>.
 	 * @return NO_MATCH, PARTIAL_MATCH or FULL_MATCH
 	 */
 	protected int compareAttributeValues(String attributeKey, Object specValue, Object confValue, ILaunchSpecification launchSpec, ILaunchConfiguration launchConfig) {
 		Assert.isNotNull(attributeKey);
 		Assert.isNotNull(launchSpec);
 		Assert.isNotNull(launchConfig);
 
 		// values are equal if both are null
 		if (specValue == null && confValue == null) {
 			return FULL_MATCH;
 		}
 		// if a value is null, partial match if values are default
 		else if (specValue == null || confValue == null) {
 			return isDefaultAttribute(attributeKey, specValue, confValue, launchSpec, launchConfig, launchSpec.getLaunchMode()) ? PARTIAL_MATCH : NO_MATCH;
 		}
 		// full match if values are default
 		else if (isDefaultAttribute(attributeKey, specValue, confValue, launchSpec, launchConfig, launchSpec.getLaunchMode())) {
 			return FULL_MATCH;
 		}
 		// use object.equals as default
 		else {
 			Assert.isNotNull(specValue);
 			Assert.isNotNull(confValue);
 			return equals(attributeKey, specValue, confValue, launchSpec, launchConfig, launchSpec.getLaunchMode());
 		}
 	}
 
 	/**
 	 * Compares the attribute value of launch configuration and launch specification.
 	 * <p>
 	 * The handling of null values is implemented in the calling private method
 	 * {@link #compareAttributeValues(String, Object, Object, ILaunchSpecification, ILaunchConfiguration)}.
 	 * When overwriting this method the implementor can be certain both values are not <code>null</code>.
 	 *
 	 * @param attributeKey The attribute key
 	 * @param specValue The launch specification value. Must not be <code>null</code>.
 	 * @param confValue The launch configuration value. Must not be <code>null</code>.
 	 * @param launchSpec The launch specification.
 	 * @param launchConfig The launch configuration which is the source of the <code>confValue</code>.
 	 * @param launchMode The current launch mode.
 	 *
 	 * @return NO_MATCH, PARTIAL_MATCH or FULL_MATCH
 	 */
 	protected int equals(String attributeKey, Object specValue, Object confValue, ILaunchSpecification launchSpec, ILaunchConfiguration launchConfig, String launchMode) {
 		Assert.isNotNull(specValue);
 		Assert.isNotNull(confValue);
 
 		if (ILaunchContextLaunchAttributes.ATTR_LAUNCH_CONTEXTS.equals(attributeKey)) {
 			// get match of list objects
 			int match = specValue.equals(confValue) ? FULL_MATCH : NO_MATCH;
 			// compare objects in the list when they are not already equal
 			if (match != FULL_MATCH) {
 				List<IModelNode> confItems = Arrays.asList(LaunchContextsPersistenceDelegate.decodeLaunchContexts(confValue.toString()));
 				IModelNode[] specItems = LaunchContextsPersistenceDelegate.decodeLaunchContexts(specValue.toString());
 				int i = 0;
 				for (IModelNode item : specItems) {
 					if (confItems.contains(item)) {
 						// spec object can be found in the configuration
 						if (match == NO_MATCH) {
 							// full match on first element in the spec list,
 							// otherwise partial match
 							match = (i == 0) ? FULL_MATCH : PARTIAL_MATCH;
 						}
 					}
 					else if (match == FULL_MATCH) {
 						// reduce full to partial match when spec object wasn't found
 						match = PARTIAL_MATCH;
 					}
 					i++;
 				}
 				// reduce full to partial match when list size is not equal
 				// but all spec values where found in the configuration project list
 				if (match == FULL_MATCH && specItems.length != confItems.size()) {
 					match = PARTIAL_MATCH;
 				}
 			}
 			return match;
 		}
 
 		if (IFileTransferLaunchAttributes.ATTR_FILE_TRANSFERS.equals(attributeKey)) {
 			// get match of list objects
 			int match = specValue.equals(confValue) ? FULL_MATCH : NO_MATCH;
 			// compare objects in the list when they are not already equal
 			if (match != FULL_MATCH) {
 				List<IFileTransferItem> confItems = Arrays.asList(FileTransfersPersistenceDelegate.decodeFileTransferItems(confValue.toString()));
 				IFileTransferItem[] specItems = FileTransfersPersistenceDelegate.decodeFileTransferItems(specValue.toString());
 				int i = 0;
 				for (IFileTransferItem item : specItems) {
 					if (confItems.contains(item)) {
 						// spec object can be found in the configuration
 						if (match == NO_MATCH) {
 							// full match on first element in the spec list,
 							// otherwise partial match
 							match = (i == 0) ? FULL_MATCH : PARTIAL_MATCH;
 						}
 					}
 					else if (match == FULL_MATCH) {
 						// reduce full to partial match when spec object wasn't found
 						match = PARTIAL_MATCH;
 					}
 					i++;
 				}
 				// reduce full to partial match when list size is not equal
 				// but all spec values where found in the configuration project list
 				if (match == FULL_MATCH && specItems.length != confItems.size()) {
 					match = PARTIAL_MATCH;
 				}
 			}
 			return match;
 		}
 
 		if (IReferencedProjectLaunchAttributes.ATTR_REFERENCED_PROJECTS.equals(attributeKey)) {
 			// get match of list objects
 			int match = specValue.equals(confValue) ? FULL_MATCH : NO_MATCH;
 			// compare objects in the list when they are not already equal
 			if (match != FULL_MATCH) {
 				List<IReferencedProjectItem> confItems = Arrays.asList(ReferencedProjectsPersistenceDelegate.decodeReferencedProjectItems(confValue.toString()));
 				IReferencedProjectItem[] specItems = ReferencedProjectsPersistenceDelegate.decodeReferencedProjectItems(specValue.toString());
 				int i = 0;
 				for (IReferencedProjectItem item : specItems) {
 					if (confItems.contains(item)) {
 						// spec object can be found in the configuration
 						if (match == NO_MATCH) {
 							// full match on first element in the spec list,
 							// otherwise partial match
 							match = (i == 0) ? FULL_MATCH : PARTIAL_MATCH;
 						}
 					}
 					else if (match == FULL_MATCH) {
 						// reduce full to partial match when spec object wasn't found
 						match = PARTIAL_MATCH;
 					}
 					i++;
 				}
 				// reduce full to partial match when list size is not equal
 				// but all spec values where found in the configuration project list
 				if (match == FULL_MATCH && specItems.length != confItems.size()) {
 					match = PARTIAL_MATCH;
 				}
 			}
 			return match;
 		}
 
 		return specValue.equals(confValue) ? FULL_MATCH : NO_MATCH;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#showLaunchDialog(int)
 	 */
 	@Override
 	public boolean showLaunchDialog(int situation) {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.launch.core.lm.interfaces.ILaunchManagerDelegate#equals(org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext, org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext)
 	 */
 	@Override
 	public boolean equals(ISelectionContext ctx1, ISelectionContext ctx2) {
 		return (ctx1 == null && ctx2 == null) || (ctx1 != null && ctx1.equals(ctx2));
 	}
 
 	@Override
 	public String getDescription(ILaunchConfiguration config) {
 		return config.getName();
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.ecp.navigator.commands;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecp.common.handler.CreateContainmentHandler;
 import org.eclipse.emf.ecp.common.model.ECPWorkspaceManager;
 import org.eclipse.emf.ecp.common.model.NoWorkspaceException;
 import org.eclipse.emf.ecp.common.model.workSpaceModel.ECPProject;
 import org.eclipse.emf.ecp.common.util.UiUtil;
 import org.eclipse.emf.ecp.navigator.Activator;
 import org.eclipse.emf.ecp.navigator.handler.NewModelElementWizardHandler;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.CompoundContributionItem;
 import org.eclipse.ui.menus.CommandContributionItem;
 import org.eclipse.ui.menus.CommandContributionItemParameter;
 
 /**
  * This class creates a group of commands to create different containments of a
  * model element through context menu. The created commands have all the same ID
  * and are handled with the same handler class {@link CreateMEHandler}.
  * 
  * @author Hodaie
  */
 public class DynamicContainmentCommands extends CompoundContributionItem {
 
 	private static final String COMMAND_ID = "org.eclipse.emf.ecp.navigator.createContaiment";
 	private EObject selectedME;
 	private ComposedAdapterFactory adapterFactory =	new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 	private AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(adapterFactory);
 
 	/**
 	 * . {@inheritDoc}
 	 */
 	@Override
 	protected IContributionItem[] getContributionItems() {
 		// 1. get selected EObject
 		selectedME = UiUtil.getSelectedEObject();
 		if (selectedME == null) {
 			return new IContributionItem[0];
 		}
 		try {
 			if (ECPWorkspaceManager.getInstance().getWorkSpace()
 					.isRootObject(selectedME)) {
 				return createNewWizard(selectedME.eClass());
 			}
 		} catch (NoWorkspaceException e) {
 			Activator.getDefault().logException(e.getMessage(), e);
 		}
 
 		AdapterFactoryItemDelegator delegator = new AdapterFactoryItemDelegator(adapterFactory);
 
 		@SuppressWarnings("unchecked")
 		List<CommandParameter> commandParameters = (List<CommandParameter>) delegator
 				.getNewChildDescriptors(selectedME, AdapterFactoryEditingDomain
 						.getEditingDomainFor(selectedME), null);
 
 		IContributionItem[] commands = createCommands(commandParameters);
 		return commands;
 
 	}
 
 	private IContributionItem[] createNewWizard(EClass eClass) {
 		CommandContributionItemParameter commandParam = new CommandContributionItemParameter(
 				PlatformUI.getWorkbench(), null,
 				"org.eclipse.emf.ecp.navigator.newModelElementWizard",
 				CommandContributionItem.STYLE_PUSH);
 		List<IContributionItem> commands = new ArrayList<IContributionItem>();
 		Map<Object, Object> commandParams = new HashMap<Object, Object>();
 
 		commandParams.put(NewModelElementWizardHandler.COMMAND_ECLASS_PARAM,
 				eClass);
 		commandParam.label = "New Model Element";
 
 		Image image = labelProvider.getImage(eClass);
 		ImageDescriptor imageDescriptor = ImageDescriptor
 				.createFromImage(image);
 		commandParam.icon = imageDescriptor;
 
 		// create command
 		commandParam.parameters = commandParams;
 		CommandContributionItem command = new CommandContributionItem(
 				commandParam);
 		commands.add(command);
 
 		return commands.toArray(new IContributionItem[commands.size()]);
 	}
 
 	/**
 	 * .
 	 * 
 	 * @param commandParameters
 	 *            a list of EReference of containments of selected ME
 	 * @return an array of IContributionsItem (commands) to create different
 	 *         types of containments.
 	 */
 	private IContributionItem[] createCommands(
 			List<CommandParameter> commandParameters) {
 
 		List<IContributionItem> commands = new ArrayList<IContributionItem>();
 		Map<String, List<CommandParameter>> mapping = new HashMap<String, List<CommandParameter>>();
 		for (CommandParameter commandParameter : commandParameters) {
 			if (commandParameter.getValue() instanceof EObject) {
 				EClass eClass = ((EObject) commandParameter.getValue())
 						.eClass();
 				List<CommandParameter> list = mapping.get(eClass.getName());
 				if (list == null) {
 					list = new ArrayList<CommandParameter>();
 					mapping.put(eClass.getName(), list);
 				}
 				list.add(commandParameter);
 			}
 		}
 		for (String eclass : mapping.keySet()) {
 			List<CommandParameter> list = mapping.get(eclass);
 			boolean showReferenceLable = (list.size() > 1);
 			for (CommandParameter commandParameter : list) {
 				EReference containment = commandParameter.getEReference();
 
 				if (!containment.isMany()) {
 					if (selectedME.eGet(containment) != null) {
 						continue;
 					}
 				}
 
 				try {
 					ECPProject project = ECPWorkspaceManager.getInstance().getWorkSpace().getProject(selectedME);
 					if (project != null && project.getMetaModelElementContext().isNonDomainElement(containment.getEReferenceType())) {
 						continue;
 					}
 				} catch (NoWorkspaceException e) {
 					Activator.getDefault().logException(e.getMessage(), e);
 				}
 
 				CommandContributionItemParameter commandParam = new CommandContributionItemParameter(
 						PlatformUI.getWorkbench(), null, COMMAND_ID,
 						CommandContributionItem.STYLE_PUSH);
 
 				Map<Object, Object> commandParams = new HashMap<Object, Object>();
 
 				Object type = commandParameter.getValue();
 				if (type instanceof EObject) {
 					commandParams.put(
 							CreateContainmentHandler.COMMAND_ECLASS_PARAM,
 							((EObject) type).eClass());
 					commandParams.put(
 							CreateContainmentHandler.COMMAND_ECREFERENCE_PARAM,
 							containment.getName());
 					commandParam.label = "New "
 							+ ((EObject) type).eClass().getName();
 					if (showReferenceLable) {
 						commandParam.label = commandParam.label + " ("
 								+ containment.getName() + ")";
 					}
 					commandParam.icon = getImage(((EObject) type).eClass());
 				} else {
 					commandParams.put(
 							CreateContainmentHandler.COMMAND_ECLASS_PARAM,
 							containment.getEReferenceType());
 					commandParam.label = "New "
 							+ containment.getEReferenceType().getName();
 					commandParam.icon = getImage(containment
 							.getEReferenceType());
 
 				}
 
 				// create command
 				commandParam.parameters = commandParams;
 				CommandContributionItem command = new CommandContributionItem(
 						commandParam);
 				commands.add(command);
 			}
 
 		}
 		return commands.toArray(new IContributionItem[commands.size()]);
 	}
 
 	private ImageDescriptor getImage(EClass eClass) {
 		EObject instance = eClass.getEPackage().getEFactoryInstance()
 				.create(eClass);
 		Image image = labelProvider.getImage(instance);
 		ImageDescriptor imageDescriptor = ImageDescriptor
 				.createFromImage(image);
 		return imageDescriptor;
 	}
 	
 	@Override
 	public void dispose() {
 		if (adapterFactory!=null) {
 			adapterFactory.dispose();
 		}
 		super.dispose();
 	}
 
 }

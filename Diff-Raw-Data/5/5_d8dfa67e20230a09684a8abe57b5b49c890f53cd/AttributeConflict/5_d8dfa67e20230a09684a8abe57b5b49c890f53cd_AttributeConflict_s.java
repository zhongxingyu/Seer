 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk
  * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.conflicts;
 
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.DecisionManager;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.Conflict;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.ConflictDescription;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.ConflictOption;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.conflict.options.MergeTextOption;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.util.DecisionUtil;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AttributeOperation;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.UnkownFeatureException;
 
 /**
  * Conflict for two attribute operations.
  * 
  * @author wesendon
  * @author emueller
  */
 public class AttributeConflict extends Conflict {
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param myOperations myOperations, with leading {@link AttributeOperation}
 	 * @param theirOperations theirOperations, with leading {@link AttributeOperation}
 	 * @param leftOperation the operation representing all left operations
 	 * @param rightOperation the operation representing all right operations
 	 * @param decisionManager decisionmanager
 	 */
 	public AttributeConflict(Set<AbstractOperation> myOperations, Set<AbstractOperation> theirOperations,
 		AbstractOperation leftOperation, AbstractOperation rightOperation, DecisionManager decisionManager) {
 		super(myOperations, theirOperations, leftOperation, rightOperation, decisionManager);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected ConflictDescription initConflictDescription(ConflictDescription description) {
 		description.setDescription(DecisionUtil.getDescription("attributeconflict", getDecisionManager()
 			.isBranchMerge()));
 		description.add("myvalue", getMyOperation(AttributeOperation.class).getNewValue());
 		description.add("oldvalue", getMyOperation(AttributeOperation.class).getOldValue());
 		description.add("theirvalue", getTheirOperation(AttributeOperation.class).getNewValue());
 		description.setImage("attribute.gif");
 
 		return description;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initConflictOptions(List<ConflictOption> options) {
 		initOptionsWithOutMerge(options, true);
 	}
 
 	/**
 	 * Allows to init options, without adding a merge text option.
 	 * 
 	 * @param options list of options
 	 * @param withMerge true, if merge text option ({@link MergeTextOption}) should be added
 	 */
 	protected void initOptionsWithOutMerge(List<ConflictOption> options, boolean withMerge) {
 		AttributeOperation attributeOperation = getMyOperation(AttributeOperation.class);
 		ConflictOption myOption = new ConflictOption(attributeOperation.getNewValue(),
 			ConflictOption.OptionType.MyOperation);
 		myOption.setDetailProvider(DecisionUtil.WIDGET_MULTILINE);
 		myOption.addOperations(getMyOperations());
 		options.add(myOption);
 
 		ConflictOption theirOption = new ConflictOption(getTheirOperation(AttributeOperation.class).getNewValue(),
 			ConflictOption.OptionType.TheirOperation);
 		theirOption.setDetailProvider(DecisionUtil.WIDGET_MULTILINE);
 		theirOption.addOperations(getTheirOperations());
 		options.add(theirOption);
 
 		EObject eObject = getDecisionManager().getModelElement(attributeOperation.getModelElementId());
 		EStructuralFeature feature;
 		boolean isMultiline = false;
 		try {
 			feature = attributeOperation.getFeature(eObject);
 			isMultiline = isMultiline(eObject, feature);
 		} catch (UnkownFeatureException e) {
 			// ignore
 		}
 
 		if (withMerge && DecisionUtil.detailsNeeded(this) && isMultiline) {
 			MergeTextOption mergeOption = new MergeTextOption();
 			mergeOption.add(myOption);
 			mergeOption.add(theirOption);
 			options.add(mergeOption);
 		}
 	}
 
 	private boolean isMultiline(EObject eObject, EStructuralFeature attribute) {
 		ComposedAdapterFactory composedAdapterFactory = new ComposedAdapterFactory(
 			ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		AdapterFactoryItemDelegator adapterFactoryItemDelegator = new AdapterFactoryItemDelegator(
 			composedAdapterFactory);
 		IItemPropertyDescriptor propertyDescriptor = adapterFactoryItemDelegator
 			.getPropertyDescriptor(eObject, attribute);
		boolean isMultiLine = propertyDescriptor.isMultiLine(eObject);
 		composedAdapterFactory.dispose();
 		return isMultiLine;
 	}
 }

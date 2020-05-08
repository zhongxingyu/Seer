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
 
 package org.eclipse.bpmn2.modeler.ui.adapters.properties;
 
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.Definitions;
 import org.eclipse.bpmn2.ItemDefinition;
 import org.eclipse.bpmn2.RootElement;
 import org.eclipse.bpmn2.modeler.core.adapters.AdapterUtil;
 import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
 import org.eclipse.bpmn2.modeler.core.adapters.FeatureDescriptor;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 
 /**
  * @author Bob Brodt
  *
  */
 public class ItemDefinitionRefFeatureDescriptor<T extends BaseElement> extends FeatureDescriptor<T> {
 
 	/**
 	 * @param adapterFactory
 	 * @param object
 	 * @param feature
 	 */
 	public ItemDefinitionRefFeatureDescriptor(AdapterFactory adapterFactory, T object, EStructuralFeature feature) {
 		super(adapterFactory, object, feature);
 		// I found a couple of instances where this class was used for references that were NOT
 		// RootElements - just check to make sure here...
 		Assert.isTrue( RootElement.class.isAssignableFrom(feature.getEType().getInstanceClass()) );
 	}
 	
 	@Override
 	public String getLabel(Object context) {
 		return ItemDefinitionPropertiesAdapter.getLabel();
 	}
 
 	@Override
 	public String getDisplayName(Object context) {
 		T object = adopt(context);
 		ItemDefinition itemDefinition = (ItemDefinition) object.eGet(feature);
 		return ItemDefinitionPropertiesAdapter.getDisplayName(itemDefinition);
 	}
 	
 	@Override
 	public EObject createFeature(Resource resource, Object context, EClass eClass) {
 		T object = adopt(context);
 		ItemDefinition itemDefinition = ItemDefinitionPropertiesAdapter.createItemDefinition(object.eResource());
 		return itemDefinition;
 	}
 
 	@Override
 	public Object getValue(Object context) {
 		T object = adopt(context);
 		ItemDefinition itemDefinition = (ItemDefinition) object.eGet(feature);
 		return ItemDefinitionPropertiesAdapter.getStructureRef(itemDefinition);
 	}
 
 	@Override
 	public void setValue(Object context, Object value) {
 		T object = adopt(context);
 		if (value instanceof ItemDefinition) {
 			ItemDefinition itemDefinition = (ItemDefinition) value;
 			if (itemDefinition.eResource()==null) {
 				final Definitions defs = ModelUtil.getDefinitions(object);
 				if (defs!=null) {
 					itemDefinition = (ItemDefinition) value;
     				TransactionalEditingDomain editingDomain = getEditingDomain(itemDefinition);
 					if (editingDomain == null) {
 						defs.getRootElements().add(itemDefinition);
 					} else {
 						final ItemDefinition id = itemDefinition;
 						editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {
 							@Override
 							protected void doExecute() {
 								defs.getRootElements().add(id);
 							}
 						});
 					}
 				}
 			}
 			super.setValue(object, itemDefinition);
 		}
		else if (value==null)
			super.setValue(object, null);
 	}
 
 	@Override
 	public Hashtable<String, Object> getChoiceOfValues(Object context) {
 		T object = adopt(context);
 		return ItemDefinitionPropertiesAdapter.getChoiceOfValues(object);
 	}
 }

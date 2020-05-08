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
 
 package org.eclipse.bpmn2.modeler.core.features;
 
 import org.eclipse.bpmn2.BaseElement;
 import org.eclipse.bpmn2.EndEvent;
 import org.eclipse.bpmn2.Group;
 import org.eclipse.bpmn2.modeler.core.adapters.ExtendedPropertiesAdapter;
 import org.eclipse.bpmn2.modeler.core.di.DIImport;
 import org.eclipse.bpmn2.modeler.core.features.activity.task.ICustomElementFeatureContainer;
 import org.eclipse.bpmn2.modeler.core.merrimac.dialogs.ObjectEditingDialog;
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.preferences.ModelEnablements;
 import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.core.utils.BusinessObjectUtil;
 import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.graphiti.IExecutionInfo;
 import org.eclipse.graphiti.features.IFeatureAndContext;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.features.context.IContext;
 import org.eclipse.graphiti.features.context.ICreateConnectionContext;
 import org.eclipse.graphiti.features.context.IReconnectionContext;
 import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
 import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
 import org.eclipse.graphiti.mm.pictograms.PictogramElement;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * This is the Create Feature base class for all BPMN2 model elements which are considered "connections"
  * e.g. Sequence Flows, Associations, Message Flows and Conversation Links
  * 
  * The Type Parameter "CONNECTION" is the BPMN2 element class, "SOURCE" is the BPMN2 class of the source object of
  * the connection, "TARGET" is the BPMN2 class of the connection target object.
  */
 public abstract class AbstractBpmn2CreateConnectionFeature<
 			CONNECTION extends BaseElement,
 			SOURCE extends EObject,
 			TARGET extends EObject>
 		extends AbstractCreateConnectionFeature
 		implements IBpmn2CreateFeature<CONNECTION, ICreateConnectionContext> {
 
 	protected boolean changesDone = true;
 
 	/**
 	 * Default constructor for this Create Feature
 	 * 
 	 * @param fp - the BPMN2 Modeler Feature Provider
 	 *             @link org.eclipse.bpmn2.modeler.ui.diagram.BPMNFeatureProvider
 	 * @param name - name of the type of object being created
 	 * @param description - description of the object being created
 	 */
 	public AbstractBpmn2CreateConnectionFeature(IFeatureProvider fp,
 			String name, String description) {
 		super(fp, name, description);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.graphiti.features.impl.AbstractFeature#isAvailable(org.eclipse.graphiti.features.context.IContext)
 	 * Returns true if this type of connection is available in the tool palette and context menus. 
 	 */
 	@Override
 	public boolean isAvailable(IContext context) {
 		Object o = null;
 		if (context instanceof ICreateConnectionContext) {
 			ICreateConnectionContext ccc = (ICreateConnectionContext)context;
 			if (ccc.getTargetPictogramElement()!=null) {
 				o = BusinessObjectUtil.getFirstElementOfType(
 						ccc.getTargetPictogramElement(), BaseElement.class);
 			}
 			else if (ccc.getSourcePictogramElement()!=null) {
 				o = BusinessObjectUtil.getFirstElementOfType(
 						ccc.getSourcePictogramElement(), BaseElement.class);
 			}
 		}
 		else if (context instanceof IReconnectionContext) {
 			IReconnectionContext rc = (IReconnectionContext)context;
 			if (rc.getTargetPictogramElement()!=null) {
 				o = BusinessObjectUtil.getFirstElementOfType(
 						rc.getTargetPictogramElement(), BaseElement.class);
 			}
 		}
 		
 		if (o instanceof EndEvent || o instanceof Group)
 			return false;
 		
 		if (o instanceof EObject) {
 			return isModelObjectEnabled((EObject)o);
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.graphiti.func.ICreateConnection#canStartConnection(org.eclipse.graphiti.features.context.ICreateConnectionContext)
 	 * Returns true if the source object is valid for this type of connection. 
 	 */
 	@Override
 	public boolean canStartConnection(ICreateConnectionContext context) {
 		return getSourceBo(context) != null;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.graphiti.features.impl.AbstractCreateFeature#getCreateDescription()
 	 * This is displayed in the Edit -> Undo/Redo menu 
 	 */
 	@Override
 	public String getCreateDescription() {
 		return NLS.bind(Messages.AbstractBpmn2CreateConnectionFeature_Create,
 				ModelUtil.toDisplayName( getBusinessObjectClass().getName()));
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.bpmn2.modeler.core.features.IBpmn2CreateFeature#createBusinessObject(org.eclipse.graphiti.features.context.IContext)
 	 * Creates the business object, i.e. the BPMN2 element
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public CONNECTION createBusinessObject(ICreateConnectionContext context) {
 		PictogramElement sourcePe = context.getSourcePictogramElement();
 		EObject container = BusinessObjectUtil.getBusinessObjectForPictogramElement(sourcePe);
 		Resource resource = container.eResource();
 		EClass eclass = getBusinessObjectClass();
 		ExtendedPropertiesAdapter adapter = ExtendedPropertiesAdapter.adapt(eclass);
 		CONNECTION businessObject = (CONNECTION)adapter.getObjectDescriptor().createObject(resource,eclass);
 		SOURCE source = getSourceBo(context);
 		TARGET target = getTargetBo(context);
 		EStructuralFeature sourceRefFeature = businessObject.eClass().getEStructuralFeature("sourceRef"); //$NON-NLS-1$
 		EStructuralFeature targetRefFeature = businessObject.eClass().getEStructuralFeature("targetRef"); //$NON-NLS-1$
 		if (sourceRefFeature!=null && targetRefFeature!=null) {
 			businessObject.eSet(sourceRefFeature, source);
 			businessObject.eSet(targetRefFeature, target);
 		}
 		putBusinessObject(context, businessObject);
 		changesDone = true;
 		return businessObject;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.bpmn2.modeler.core.features.IBpmn2CreateFeature#getBusinessObject(org.eclipse.graphiti.features.context.IContext)
 	 * Fetches the business object from the Create Context
 	 */
 	@SuppressWarnings("unchecked")
 	public CONNECTION getBusinessObject(ICreateConnectionContext context) {
 		return (CONNECTION) context.getProperty(ContextConstants.BUSINESS_OBJECT);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.bpmn2.modeler.core.features.IBpmn2CreateFeature#putBusinessObject(org.eclipse.graphiti.features.context.IContext, org.eclipse.emf.ecore.EObject)
 	 * Saves the business object in the Create Context.
 	 * If the object is a Custom Element, it is initialized as defined in the extension plugin's plugin.xml
 	 */
 	public void putBusinessObject(ICreateConnectionContext context, CONNECTION businessObject) {
 		context.putProperty(ContextConstants.BUSINESS_OBJECT, businessObject);
 		String id = (String)context.getProperty(ICustomElementFeatureContainer.CUSTOM_ELEMENT_ID);
 		if (id!=null) {
 	    	TargetRuntime rt = TargetRuntime.getCurrentRuntime();
 	    	CustomTaskDescriptor ctd = rt.getCustomTask(id);
 	    	ctd.populateObject(businessObject, businessObject.eResource(), true);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.bpmn2.modeler.core.features.IBpmn2CreateFeature#postExecute(org.eclipse.graphiti.IExecutionInfo)
 	 * Invoked after the graphic has been created to display an optional configuration dialog.
 	 * The configuration dialog popup is enabled/disabled in the user Preferences for BPMN2 Editor.
 	 */
 	public void postExecute(IExecutionInfo executionInfo) {
 		for (IFeatureAndContext fc : executionInfo.getExecutionList()) {
 			IContext context = fc.getContext();
 			if (context instanceof ICreateConnectionContext) {
 				ICreateConnectionContext cc = (ICreateConnectionContext)context;
 				CONNECTION businessObject = getBusinessObject(cc);
 				Bpmn2Preferences prefs = (Bpmn2Preferences) ((DiagramEditor) getDiagramEditor()).getAdapter(Bpmn2Preferences.class);
 				if (prefs!=null && prefs.getShowPopupConfigDialog(businessObject)) {
 					ObjectEditingDialog dialog =
 							new ObjectEditingDialog((DiagramEditor)getDiagramEditor(), businessObject);
 					dialog.open();
 				}
 			}
 		}
 	}
 	
 	protected AddConnectionContext createAddConnectionContext(ICreateConnectionContext context, Object newObject) {
 		AddConnectionContext newContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
 		newContext.setNewObject(newObject);
 		
 		// copy properties into the new context
 		Object value = context.getProperty(ICustomElementFeatureContainer.CUSTOM_ELEMENT_ID);
 		newContext.putProperty(ICustomElementFeatureContainer.CUSTOM_ELEMENT_ID, value);
 		value = context.getProperty(DIImport.IMPORT_PROPERTY);
 		newContext.putProperty(DIImport.IMPORT_PROPERTY, value);
 		value = context.getProperty(ContextConstants.BUSINESS_OBJECT);
 		newContext.putProperty(ContextConstants.BUSINESS_OBJECT, value);
 		return newContext;
 	}
 
 	/**
 	 * Convenience method to check if a model object was disabled in the extension plugin.
 	 * 
 	 * @return true/false depending on if the model object is enabled or disabled.
 	 * If disabled, the object will not be available and will not appear in the tool palette
 	 * or context menus.
 	 */
 	protected boolean isModelObjectEnabled() {
 		ModelEnablements me = getModelEnablements();
 		if (me!=null)
 			return me.isEnabled(getBusinessObjectClass());
 		return false;
 	}
 	
 	protected boolean isModelObjectEnabled(EObject o) {
 		ModelEnablements me = getModelEnablements();
 		if (me!=null) {
 			EClass eclass = (o instanceof EClass) ? (EClass)o : o.eClass();
 			return me.isEnabled(eclass);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean hasDoneChanges() {
 		return changesDone;
 	}
 	
 	protected ModelEnablements getModelEnablements() {
 		DiagramEditor editor = (DiagramEditor) getDiagramEditor();
 		return (ModelEnablements) editor.getAdapter(ModelEnablements.class);
 	}
 
 	/**
 	 * Returns the business object for the connection source shape. If the source object is not valid
 	 * for this connection type, return null.
 	 * 
 	 * @param context - connection create context
 	 * @return true if the source is valid, false if not.
 	 */
 	protected SOURCE getSourceBo(ICreateConnectionContext context) {
 		if (context.getSourceAnchor() != null) {
 			return BusinessObjectUtil.getFirstElementOfType(context.getSourceAnchor().getParent(), getSourceClass());
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the business object for the connection target shape. If the target object is not valid
 	 * for this connection type, return null.
 	 * 
 	 * @param context - connection create context
 	 * @return true if the target is valid, false if not.
 	 */
 	protected TARGET getTargetBo(ICreateConnectionContext context) {
 		if (context.getTargetAnchor() != null) {
 			return BusinessObjectUtil.getFirstElementOfType(context.getTargetAnchor().getParent(), getTargetClass());
 		}
 		return null;
 	}
 
 	/**
 	 * Implementation classes must override these to provide the BPMN2 object source and target classes that are valid for this connection.
 	 */
 	protected abstract Class<SOURCE> getSourceClass();
 	protected abstract Class<TARGET> getTargetClass();
 
 }

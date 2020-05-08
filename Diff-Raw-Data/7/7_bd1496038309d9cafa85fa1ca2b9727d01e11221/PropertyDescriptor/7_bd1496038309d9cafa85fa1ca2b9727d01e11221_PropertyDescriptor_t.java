 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors.properties.internal;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ICellEditorValidator;
 import org.eclipse.jst.jsf.common.metadata.Entity;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.metadataprocessors.AbstractMetaDataEnabledFeature;
 import org.eclipse.jst.jsf.metadataprocessors.IMetaDataEnabledFeature;
 import org.eclipse.jst.jsf.metadataprocessors.MetaDataEnabledProcessingFactory;
 import org.eclipse.jst.jsf.metadataprocessors.features.IDefaultValue;
 import org.eclipse.jst.jsf.metadataprocessors.features.IPossibleValues;
 import org.eclipse.jst.jsf.metadataprocessors.features.IValidValues;
 import org.eclipse.jst.pagedesigner.editors.properties.IPropertyPageDescriptor;
 import org.eclipse.jst.pagedesigner.meta.EditorCreator;
 import org.eclipse.jst.pagedesigner.meta.internal.CellEditorFactoryRegistry;
 import org.eclipse.jst.pagedesigner.properties.ITabbedPropertiesConstants;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.w3c.dom.Element;
 
 /**
  * MD enabled feature of an attribute value runtime type allowing for the tabbed property
  * page to work
  *
  */
 public class PropertyDescriptor extends AbstractMetaDataEnabledFeature
 		implements IMetaDataEnabledFeature, IPropertyPageDescriptor, IAdaptable {
 
 	/**
 	 * Constructor
 	 */
 	public PropertyDescriptor() {
 		// TODO Auto-generated constructor stub
 	}
 
 	//IPropertyPageDescriptor 
 	public String getCategory() {		
 		String cat = getTraitValueAsString(IPropertyPageDescriptor.PROP_DESC_CATEGORY);
 		return cat != null ? cat : ITabbedPropertiesConstants.OTHER_CATEGORY;
 	}
 
 	public CellEditor getCellEditor(Composite parent) {
 //		TODO: allow for override of the factory by using MD
 //		Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(
 //				getMetaDataContext().getEntity(),
 //				IPropertyPageDescriptor.PROP_DESC_CELL_EDITOR);
 //		
 //		if (trait != null) {
 //			String classname = TraitValueHelper.getValueAsString(trait);
 //			if (classname != null && ! classname.equals("")){
 //				try {
 //					Class klass = PDPlugin.getDefault().getBundle().loadClass(classname);
 //					if (klass != null){
 //						//
 //					}
 //				} catch (ClassNotFoundException e) {//
 //					String msg = "Unable to locate cell editor:"+classname+ " for "+toString();
 //					PDPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, PDPlugin.getPluginId(), msg));
 //				}
 //			}
 //		}
 		//otherwise use factory
 		Element element = (Element)IStructuredDocumentContextResolverFactory.INSTANCE.getDOMContextResolver(getStructuredDocumentContext()).getNode();		
 		return CellEditorFactoryRegistry.getInstance().createCellEditor(parent, this, element);
 	}
 
 	public String getDescription() {
 		return getTraitValueAsString("description");
 	}
 
 	public String getLabel() {
 		String label = getTraitValueAsString("display-label");
 		if (label == null) {
 			label = getMetaDataContext().getEntity().getId();
 		}
 		return label;
 	}
 
 	public boolean isRequired() {
 		return getTraitValueAsBoolean("required");
 	}
 
 	public DialogField getDialogFieldEditor() {
 		//TODO: allow for override of the factory by using MD
 //		Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(
 //				getMetaDataContext().getEntity(),
 //				IPropertyPageDescriptor.PROP_DESC_DIALOG_FIELD_EDITOR);
 //		
 //		if (trait != null) {
 //			String classname = TraitValueHelper.getValueAsString(trait);
 //			if (classname != null && ! classname.equals("")){
 //				try {
 //					Class klass = PDPlugin.getDefault().getBundle().loadClass(classname);
 //					if (klass != null){
 //						//
 //					}
 //				} catch (ClassNotFoundException e) {//
 //					String msg = "Unable to locate cell editor:"+classname+ " for "+toString();
 //					PDPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, PDPlugin.getPluginId(), msg));
 //				}
 //			}
 //		}
 		//otherwise use factory
 		EditorCreator creator = EditorCreator.getInstance();
 		return creator.createDialogFieldWithWrapper(getUri(),
 				getTagName(),this, null);
 	}
 
 	public String getTagName() {
 		return getTagEntity().getId();
 	}
 
 	public String getUri() {
		return getMetaDataContext().getEntity().getModel().getCurrentModelContext().getUri();
 	}
 	
 	public String getValueType() {
 		return getTraitValueAsString(MetaDataEnabledProcessingFactory.ATTRIBUTE_VALUE_RUNTIME_TYPE_PROP_NAME);
 	}
 
 	public String getAttributeName() {
		return getMetaDataContext().getEntity().getId();
 	}
 
 	public Object getAdapter(Class adapter) {
 		if (IPropertyDescriptor.class == adapter) {
 			return new PropertyDescriptorAdapter(this);
 		}
 		else if (IPossibleValues.class == adapter) {
 			List pvs = MetaDataEnabledProcessingFactory.getInstance().getAttributeValueRuntimeTypeFeatureProcessors(
 						IPossibleValues.class, 
 						getStructuredDocumentContext(), 
 						getAttributeEntity());
 			if (!pvs.isEmpty())
 				return pvs.get(0);
 		}
 		else if (IDefaultValue.class == adapter) {
 			List dvs = MetaDataEnabledProcessingFactory.getInstance().getAttributeValueRuntimeTypeFeatureProcessors(
 						IDefaultValue.class, 
 						getStructuredDocumentContext(), 
 						getAttributeEntity());
 			if (!dvs.isEmpty())
 				return dvs.get(0);
 		}
 		else if (ICellEditorValidator.class == adapter) {
 			IValidValues vvs = (IValidValues)getAdapter(IValidValues.class);
 			if (vvs != null)
 				return new EditorValidatorAdapter(vvs);			
 		
 		}
 		else if (IInputValidator.class == adapter) {
 				IValidValues vvs = (IValidValues)getAdapter(IValidValues.class);
 				if (vvs != null)	
 					return new EditorValidatorAdapter(vvs);				
 		}
 		else if (IValidValues.class == adapter) {
 			List vvs = MetaDataEnabledProcessingFactory.getInstance().getAttributeValueRuntimeTypeFeatureProcessors(
 						IValidValues.class, 
 						getStructuredDocumentContext(), 
 						getAttributeEntity());
 			if (!vvs.isEmpty())
 				return vvs.get(0);
 		}
 		return null;
 	}
 	
 	private Entity getTagEntity() {
 		return (Entity)getAttributeEntity().eContainer();
 	}
 	
 	private Entity getAttributeEntity() {
 		return getMetaDataContext().getEntity();
 	}
 	
 	public String toString() {
 		return getUri()+"/"+getTagName()+"/"+getAttributeName();
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.pagedesigner.properties.celleditors;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.internal.TraitValueHelper;
 import org.eclipse.jst.jsf.common.metadata.query.TaglibDomainMetaDataQueryHelper;
 import org.eclipse.jst.jsf.common.ui.internal.dialogfield.DialogField;
 import org.eclipse.jst.jsf.metadataprocessors.features.IDefaultValue;
 import org.eclipse.jst.jsf.metadataprocessors.features.IPossibleValues;
 import org.eclipse.jst.pagedesigner.editors.properties.IPropertyPageDescriptor;
 import org.eclipse.jst.pagedesigner.jsp.core.IJSPCoreConstants;
 import org.eclipse.jst.pagedesigner.meta.ITagAttributeCellEditorFactory;
 import org.eclipse.jst.pagedesigner.meta.IAttributeRuntimeValueType;
 import org.eclipse.jst.pagedesigner.ui.dialogfields.ClasspathResourceButtonDialogField;
 import org.eclipse.jst.pagedesigner.ui.dialogfields.ContextableResourceButtonDialogField;
 import org.eclipse.jst.pagedesigner.ui.dialogfields.StyleButtonDialogField;
 import org.eclipse.jst.pagedesigner.utils.StructuredModelUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.w3c.dom.Element;
 
 /**
  * 
  * @author mengbo
  */
 public class CellEditorFactory implements ITagAttributeCellEditorFactory {
 	public CellEditor createCellEditor(Composite parent,
 			IPropertyPageDescriptor attr, Element element) {
 		
 		String type = attr.getValueType();
 		
 		IPossibleValues pvs = (IPossibleValues)attr.getAdapter(IPossibleValues.class);
 		IDefaultValue defaultValue = (IDefaultValue)attr.getAdapter(IDefaultValue.class);
 //		ICellEditorValidator validator = (ICellEditorValidator)attr.getAdapter(ICellEditorValidator.class);
 		CellEditor ed = null;
 		if (IAttributeRuntimeValueType.RELATIVEPATH.equalsIgnoreCase(type)|| IAttributeRuntimeValueType.WEBPATH.equalsIgnoreCase(type)) {
 			IProject project = getProject(element);
 			if (project != null) {
 //				String typeParam = TraitValueHelper.getValueAsString(TaglibDomainMetaDataQueryHelper.getTrait(attr.getMetaDataContext().getEntity(),"type-param"));
 				ResourceDialogCellEditor cellEditor = new ResourceDialogCellEditor(
 						parent);
 				Trait fileExt = TaglibDomainMetaDataQueryHelper.getTrait(attr.getMetaDataContext().getEntity(), "file-extensions");
 				Trait separator = TaglibDomainMetaDataQueryHelper.getTrait(attr.getMetaDataContext().getEntity(), "separator");
 				String[] fileExts = null;
 				if (fileExt != null){
 					List exts = TraitValueHelper.getValueAsListOfStrings(fileExt);
 					fileExts = (String[])exts.toArray(new String[exts.size()]);
 				} 
 				
 				String sep = null;
 				if (separator != null)
 					sep = TraitValueHelper.getValueAsString(separator);
 				
 				if (fileExts != null)
 					cellEditor.setSuffixs(fileExts);
 				if (sep != null)
 					cellEditor.setSeparator(sep);
 
 				cellEditor.setProject(project);
 				cellEditor.setReferredFile(getFile(element));
 
 				if (IAttributeRuntimeValueType.WEBPATH.equalsIgnoreCase(type)) {
 					cellEditor.setWebPath(true);
 				}
 
 				if (IJSPCoreConstants.TAG_DIRECTIVE_INCLUDE.equals(element
 						.getLocalName())
 						|| IJSPCoreConstants.TAG_INCLUDE.equals(element
 								.getLocalName())) {
 					cellEditor.setTransformJSPURL(false);
 				}
 				ed = cellEditor;
 			}
 		} 
 		else if (IAttributeRuntimeValueType.RESOURCEBUNDLE.equals(type)) {
 			ed = new LoadbundleSelectionCellEditor(parent,
 					getProject(element));
 		}
 		else if (IAttributeRuntimeValueType.CSSSTYLE.equalsIgnoreCase(type)) {
 //				String param = getParamterValue(attr, "style");
 ////							.getParameterByName(IAttributeDescriptor.PARAMETER_STYLE);
 //				if (!param.equalsIgnoreCase("STYLE")) {
 //					return null;
 //				}
 				CSSDialogCellEditor cellEditor = new CSSDialogCellEditor(parent,
 						(IDOMElement) element);
 				ed = cellEditor;
 		}
 		else if (pvs != null && pvs.getPossibleValues().size() > 0) {
 //			if (validator != null) 
 //				ed = LabeledStyleComboCellEditor.newInstance(parent, pvs,
 //	            		defaultValue, SWT.DROP_DOWN | SWT.READ_ONLY);
 //			else 
 				ed = LabeledStyleComboCellEditor.newInstance(parent, pvs,
             		defaultValue, SWT.NONE);
 	
 		}
 		
 //		} else if (IAttributeRuntimeValueType.CSSID.equalsIgnoreCase(type)) {
 //		    // TODO: missing case?
 
 		else {
 			ed = new TextCellEditor(parent);
 		}
 //		if (validator != null){
 //			ed.setValidator(validator);
 //		}
 		
 		return ed;
 	}
 
 
 //	private String getParamterValue(IPropertyPageDescriptor attr, String traitKey) {
 //		Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(attr.getMetaDataContext().getEntity(), traitKey);
 //		if (trait != null){
 //			return TraitValueHelper.getValueAsString(trait);
 //		}
 //		return null;
 //	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.meta.NEWMDIAttributeCellEditorFactory#createDialogField(org.eclipse.jst.pagedesigner.meta.IAttributeDescriptor,
 	 *      org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	public DialogField createDialogField(IPropertyPageDescriptor attr) {
 
 		String type = attr.getValueType();
 		
 		IPossibleValues pvs = (IPossibleValues)attr.getAdapter(IPossibleValues.class);
 //		IDefaultValue defaultValue = (IDefaultValue)attr.getAdapter(IDefaultValue.class);
 //		ICellEditorValidator validator = (ICellEditorValidator)attr.getAdapter(ICellEditorValidator.class);
 		
 		
 
 		if (IAttributeRuntimeValueType.RELATIVEPATH.equals(type) ||
 				IAttributeRuntimeValueType.WEBPATH.equals(type)) {
 			Trait fileExt = TaglibDomainMetaDataQueryHelper.getTrait(attr.getMetaDataContext().getEntity(), "file-extensions");
 			Trait seperator = TaglibDomainMetaDataQueryHelper.getTrait(attr.getMetaDataContext().getEntity(), "separator");
 			
 			String[] fileExts = null;
 			if (fileExt != null){
 				List exts = TraitValueHelper.getValueAsListOfStrings(fileExt);
 				fileExts = (String[])exts.toArray(new String[exts.size()]);
 			} 
 			
 			String sep = null;
 			if (seperator != null)
 				sep = TraitValueHelper.getValueAsString(seperator);
 		
 			ContextableResourceButtonDialogField field = new ContextableResourceButtonDialogField();
 			field.setLabelText(attr.getLabel());
 			if (fileExts != null) {
 				field.setSuffixs(fileExts);
 			}
 			
 			if (sep != null) {
 				field.setSeparator(sep);
 			}
 			
 			if ("".equalsIgnoreCase(field.getSeparator())) {
 				field.setResourceDescription(ResourceBoundle
 						.getString("FileCellEditor.Msg"));
 			} else {
 				field.setResourceDescription(ResourceBoundle
 						.getString("FileCellEditor.Msg1"));
 			}
 			field.setWebPath(IAttributeRuntimeValueType.WEBPATH.equals(type));
 			field.setRequired(attr.isRequired());
 			field.setToolTip(attr.getDescription());
 			return field;
 
		} else if (IAttributeRuntimeValueType.CLASSPATH_RESOURCE.equals(type)) {
 			ClasspathResourceButtonDialogField field = new ClasspathResourceButtonDialogField();
 			field.setRequired(attr.isRequired());
 			field.setToolTip(attr.getDescription());
 			return field;
 		} else if (IAttributeRuntimeValueType.CSSSTYLE.equalsIgnoreCase(type)) {
 //			String param = getParamterValue(attr, "style");
 //			if (!"STYLE".equalsIgnoreCase(param)) {
 //				return null;
 //			}
 			StyleButtonDialogField field = new StyleButtonDialogField();
 			field.setRequired(attr.isRequired());
 			field.setToolTip(attr.getDescription());
 			field.setLabelText(attr.getLabel());
 			return field;
 		
 //		// if there is no type or type unknown, then we just return null. and
 //		// system will
 //		// create default (text cell editor).
 		} else if (pvs != null) {
 			MDEnabledComboDialogField field = new MDEnabledComboDialogField(SWT.None);
 			field.setLabelText(attr.getLabel());
 			field.setToolTip(attr.getDescription());
 			field.setRequired(attr.isRequired());
 			return field;
 		}
 		return null;
 	}
 
 	private IProject getProject(Element element) {
 		if (element instanceof IDOMElement) {
 			IDOMModel model = ((IDOMElement) element).getModel();
 			IFile file = StructuredModelUtil.getFileFor(model);
 			if (file != null) {
 				return file.getProject();
 			}
 		}
 		return null;
 	}
 
 	public String[] getSupportedValueTypes() {
 		return null;//default - all!
 	}
 
 	private IFile getFile(Element element) {
 		if (element instanceof IDOMElement) {
 			IDOMModel model = ((IDOMElement) element).getModel();
 			IFile file = StructuredModelUtil.getFileFor(model);
 			return file;
 		}
 		return null;
 	}
 }

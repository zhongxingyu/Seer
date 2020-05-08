 /*******************************************************************************
  * Copyright (c) 2007-2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.outline;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.text.ITextViewer;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySheetEntry;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.eclipse.ui.views.properties.IPropertySource2;
 import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
 import org.eclipse.wst.sse.ui.views.properties.IPropertySourceExtension;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
 import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
 import org.eclipse.wst.xml.core.internal.document.DocumentTypeAdapter;
 import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.eclipse.wst.xml.ui.internal.XMLUIMessages;
 import org.eclipse.wst.xml.ui.internal.properties.EnumeratedStringPropertyDescriptor;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.contentassist.computers.FaceletsELCompletionProposalComputer;
 import org.jboss.tools.jst.jsp.contentassist.computers.JspELCompletionProposalComputer;
 import org.jboss.tools.jst.jsp.editor.IVisualController;
 import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
 import org.jboss.tools.jst.web.kb.IPageContext;
 import org.jboss.tools.jst.web.kb.KbQuery;
 import org.jboss.tools.jst.web.kb.KbQuery.Type;
 import org.jboss.tools.jst.web.kb.PageProcessor;
 import org.jboss.tools.jst.web.kb.taglib.IAttribute;
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 /**
  * @author Kabanovich
  * XMLPropertySourceAdapter extension that overrides
  * creation of property descriptors.
  */
 
 @SuppressWarnings("restriction")
 public class JSPPropertySourceAdapter implements INodeAdapter, IPropertySource, IPropertySourceExtension, IPropertySource2 {//extends XMLPropertySourceAdapter {
 	protected final static String CATEGORY_ATTRIBUTES = XMLUIMessages.XMLPropertySourceAdapter_0;
 	private static final boolean SET_EXPERT_FILTER = false;
 
 	public static interface IQueryFactory {
 		public boolean isAvailable(String attributeName);
 	}
 
 	class QueryFactory implements IQueryFactory {
 		public boolean isAvailable(String attributeName) {
 			if(attributeName.equals("style") //$NON-NLS-1$
 				|| attributeName.equals("class")) { //$NON-NLS-1$)
 				return true;
 			}
 			KbQuery query = getQuery(attributeName);
 			return valueHelper.isAvailable(pageContext, query);
 		}
 	}
 
 	QueryFactory queryFactory = new QueryFactory();
 	
     private	AttributeSorter sorter = null;
 	private Node fNode = null;
 	private boolean fCaseSensitive = true;
 	private IPropertyDescriptor[] fDescriptors = null;
 	private ValueHelper valueHelper = new ValueHelper();
 	IPageContext pageContext;
 	JspELCompletionProposalComputer processor;
 	int offset = 0;
 	KbQuery kbQuery, kbQueryAttr;
 	private Set attributeNames = new HashSet();
 
 	public JSPPropertySourceAdapter(INodeNotifier target) {
 		setTarget(target);
 	}
 	
 	Map getWeights() {
 		return sorter == null ? new HashMap() : sorter.weights;
 	}
 	
 	public void setTarget(INodeNotifier target) {
 		fNode = (target instanceof Node) ? (Node) target : null;
 
 		if (fNode instanceof IDOMNode) {
 			Document ownerDocument = fNode.getOwnerDocument();
 			if (ownerDocument == null && fNode instanceof Document) {
 				// if ownerDocument is null, then it must be the Document Node
 				ownerDocument = (Document) fNode;
 			}
 			DocumentTypeAdapter adapter = ownerDocument == null ? null : (DocumentTypeAdapter) ((INodeNotifier) ownerDocument).getAdapterFor(DocumentTypeAdapter.class);
 			if (adapter != null)
 				fCaseSensitive = adapter.getTagNameCase() == DocumentTypeAdapter.STRICT_CASE;
 			offset = ((IDOMNode)fNode).getStartOffset() + ("" + fNode.getNodeName()).length(); //$NON-NLS-1$
 		}
 
 		if(fNode instanceof Node) {
 			ITextViewer viewer = getTextViewer();
 			// Jeremy: JBIDE-9949: This prevents invocation of CA Proposals Computation in case 
 			// of Text Viewer is not accessible anymore (f.i. in case of closing editor, 
 			// since ITexViewer is acquired from Active Page of Active Workbench Window)
 			//
 			if (viewer != null) {  
 				processor = valueHelper.isFacetets() ? new FaceletsELCompletionProposalComputer() : new JspELCompletionProposalComputer();
 	//			processor.createContext(getTextViewer(), offset);
 				processor.setKeepState(true);
 		        processor.computeCompletionProposals(new CompletionProposalInvocationContext(viewer, offset), new NullProgressMonitor());
 				pageContext = processor.getContext();
 				kbQuery = createKbQuery(processor);
 				kbQuery.setMask(true); 
 				kbQueryAttr = createKbQuery(processor);
 			}
 		}
 	}
 	
 	//TODO move to helper
 	protected ITextViewer getTextViewer() {
 		IEditorPart editor = ModelUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(editor == null) return null;
 		if (editor instanceof JSPMultiPageEditor) {
 			JSPMultiPageEditor jsp = (JSPMultiPageEditor)editor;
 			return jsp.getSourceEditor().getTextViewer();
 		}
 		return null;
 	}
 
 	public void setSorter(AttributeSorter sorter) {
 		this.sorter = sorter;
 	}
 
 	/**
 	 * Returns the current collection of property descriptors.
 	 * 
 	 * @return all valid descriptors.
 	 */
 	public IPropertyDescriptor[] getPropertyDescriptors() {
 		if(fNode == null) {
 			if(fDescriptors == null || fDescriptors.length > 0) {
 				fDescriptors = new IPropertyDescriptor[0];
 			}
 		} else if (fDescriptors == null || fDescriptors.length == 0) {
 			attributeNamesChanged();
 			fDescriptors = createPropertyDescriptors();
 		} else {
 			if(attributeNamesChanged()) {
 				updatePropertyDescriptors();
 			}
 //			return fDescriptors;
 		}
 		return fDescriptors;
 	}
 	
 	private KbQuery getQuery(String attributeName) {
 		kbQueryAttr.setValue(attributeName);
 		kbQueryAttr.setStringQuery(attributeName);
 		return kbQueryAttr;
 	}	
 
 	private IPropertyDescriptor[] createPropertyDescriptors() {
 		if(sorter != null) sorter.clear();
 		CMNamedNodeMap attrMap = null;
 		CMElementDeclaration ed = getDeclaration();
 		if (ed != null) {
 			attrMap = ed.getAttributes();
 		}
 
 		List<IPropertyDescriptor> descriptorList = new ArrayList<IPropertyDescriptor>();
 		List<String> names = new ArrayList<String>();
 		List<String> namesLow = new ArrayList<String>();
 		IPropertyDescriptor descriptor = null;
 
 		Map<String, IAttribute> as = getAttributes();
 
 			for (IAttribute d: as.values()) {
 				descriptor = null;
 				String attrName = d.getName();
 				if (fCaseSensitive) {
 					if (names.contains(attrName)) continue;
 				} else {
 					if (namesLow.contains(attrName.toLowerCase())) continue;
 				}
 				if(attrName.indexOf('*') >=0) continue;
 				descriptor = createJSPPropertyDescriptor(d, attrName, false);
 				if (descriptor != null) {
 					names.add(attrName);
 					namesLow.add(attrName.toLowerCase());
 					descriptorList.add(descriptor);
 				}
 			}
 
 		// add descriptors for existing attributes
 		NamedNodeMap attributes = fNode.getAttributes();
 		if (attributes != null) {
 			for (int i = 0; i < attributes.getLength(); i++) {
 				CMAttributeDeclaration attrDecl = null;
 				Attr attr = (Attr) attributes.item(i);
 				String attrName = attr.getName();
 				if(names.contains(attrName)) continue;
 				if(!fCaseSensitive && namesLow.contains(attrName.toLowerCase())) {
 					continue;
 				}
 				// if metainfo is present for this attribute, use the
 				// CMAttributeDeclaration to derive a descriptor
 				if (attrMap != null) {
 					if (fCaseSensitive)
 						attrDecl = (CMAttributeDeclaration) attrMap.getNamedItem(attrName);
 					else {
 						for (int j = 0; j < attrMap.getLength(); j++) {
 							if (!fCaseSensitive && attrMap.item(j).getNodeName().equalsIgnoreCase(attrName)) {
 								attrDecl = (CMAttributeDeclaration) attrMap.item(j);
 								break;
 							}
 						}
 					}
 				}
 				// be consistent: if there's metainfo, use *that* as the
 				// descriptor ID
 				if (attrDecl != null) {
 					descriptor = createPropertyDescriptor(attrDecl);
 					if(descriptor instanceof TextPropertyDescriptor) {
 						IAttribute a = as.get(attrName);
 						if(a != null) {
 							descriptor = createJSPPropertyDescriptor(a, attr.getName(), false);
 						}
 					}
 					if (descriptor != null) {
 						names.add(attrDecl.getNodeName());
 						namesLow.add(attrDecl.getNodeName().toLowerCase());
 					}
 				}
 				else {
 					String an = attrName;
 					if(an.startsWith("xmlns:")) an = "xmlns:*"; //$NON-NLS-1$ //$NON-NLS-2$
 					IAttribute a = as.get(an);
 					if(a != null) {
 						descriptor = createJSPPropertyDescriptor(a, attr.getName(), false);
 					} else {
 						descriptor = createDefaultPropertyDescriptor(attr.getName(), false);
 					}
 					if (descriptor != null)
 						names.add(attr.getName());
 						namesLow.add(attr.getName().toLowerCase());
 				}
 				if (descriptor != null) {
 					descriptorList.add(descriptor);
 				}
 			}
 		}
 
 		// add descriptors from the metainfo that are not yet listed
 		if (attrMap != null) {
 			for (int i = 0; i < attrMap.getLength(); i++) {
 				CMAttributeDeclaration attrDecl = null;
 				attrDecl = (CMAttributeDeclaration) attrMap.item(i);
 				if (names.contains(attrDecl.getAttrName())) continue;
 				if(!fCaseSensitive && namesLow.contains(attrDecl.getAttrName().toLowerCase())) {
 					continue;
 				}
 				IPropertyDescriptor holdDescriptor = createPropertyDescriptor(attrDecl);
 				if(holdDescriptor instanceof TextPropertyDescriptor) {
 					IAttribute a = as.get(attrDecl.getAttrName());
 					if(a != null) {
 						holdDescriptor = createJSPPropertyDescriptor(a, attrDecl.getAttrName(), false);
 					}
 				}
 				if (holdDescriptor != null) {
 					descriptorList.add(holdDescriptor);
 				}
 			}
 		}
 
 		IPropertyDescriptor[] descriptors = new IPropertyDescriptor[descriptorList.size()];
 		for (int i = 0; i < descriptors.length; i++)
 			descriptors[i] = (IPropertyDescriptor) descriptorList.get(i);
 		return descriptors;
 	}
 
 	protected KbQuery createKbQuery(JspELCompletionProposalComputer processor) {
 		KbQuery kbQuery = new KbQuery();
 
 		String[] parentTags = processor.getParentTags(false);
 		parentTags = add(parentTags, fNode.getNodeName());
 		kbQuery.setPrefix(getPrefix());
 		kbQuery.setUri(processor.getUri(getPrefix()));
 		kbQuery.setParentTags(parentTags);
 		kbQuery.setParent(fNode.getNodeName());
 		kbQuery.setMask(false); 
 		kbQuery.setType(Type.ATTRIBUTE_NAME);
 		kbQuery.setOffset(offset);
 		kbQuery.setValue("");  //$NON-NLS-1$
 		kbQuery.setStringQuery(""); //$NON-NLS-1$
 		
 		return kbQuery;
 	}
 
 	private String[] add(String[] result, String v) {
 		String[] result1 = new String[result.length + 1];
 		System.arraycopy(result, 0, result1, 0, result.length);
 		result1[result.length] = v;
 		return result1;
 	}
 	private String getPrefix() {
 		int i = fNode.getNodeName().indexOf(':');
 		return i < 0 ? null : fNode.getNodeName().substring(0, i);
 	}
 
 
 	private String getCategory(CMAttributeDeclaration attrDecl) {
 		if (attrDecl != null) {
 			if (attrDecl.supports("category")) { //$NON-NLS-1$
 				return (String) attrDecl.getProperty("category"); //$NON-NLS-1$
 			}
 		}
 		return CATEGORY_ATTRIBUTES;
 	}
 
 	private CMElementDeclaration getDeclaration() {
 		if (fNode == null || fNode.getNodeType() != Node.ELEMENT_NODE)
 			return null;
 		Document document = fNode.getOwnerDocument();
 		ModelQuery modelQuery = (document == null) ? null : ModelQueryUtil.getModelQuery(document);
 		if (modelQuery != null) {
 			return modelQuery.getCMElementDeclaration((Element) fNode);
 		}
 		return null;
 	}
 
 	private IPropertyDescriptor createJSPPropertyDescriptor(IAttribute d, String attributeName, boolean hideOnFilter) {
 		if(d != null && sorter != null) {
 			if(d.isRequired()) sorter.setWeight(attributeName, 2);
 			else if(d.isPreferable()) sorter.setWeight(attributeName, 1);
 			else sorter.setWeight(attributeName, 0);
 		}
 		Properties context = new Properties();
 		context.put("node", fNode); //$NON-NLS-1$
 		context.setProperty("nodeName", fNode.getNodeName()); //$NON-NLS-1$
 		context.setProperty("attributeName", attributeName); //$NON-NLS-1$
 		context.put("valueHelper", valueHelper); //$NON-NLS-1$
 		context.put("pageContext", pageContext); //$NON-NLS-1$
 		context.put("processor", processor); //$NON-NLS-1$
 		context.put("queryFactory", queryFactory); //$NON-NLS-1$
 		JSPPropertyDescriptor descriptor = new JSPPropertyDescriptor(context, attributeName, attributeName);
 		descriptor.setCategory(getCategory(null));
 		descriptor.setDescription(attributeName);
 		return descriptor;
 	}
 
 	private IPropertyDescriptor createDefaultPropertyDescriptor(String attributeName, boolean hideOnFilter) {
 		// The descriptor class used here is also used in
 		// updatePropertyDescriptors()
 		TextPropertyDescriptor descriptor = new TextPropertyDescriptor(attributeName, attributeName);
 		descriptor.setCategory(getCategory(null));
 		descriptor.setDescription(attributeName);
 //		if (hideOnFilter && SET_EXPERT_FILTER)
 //			descriptor.setFilterFlags(new String[]{IPropertySheetEntry.FILTER_ID_EXPERT});
 		return descriptor;
 	}
 	
 	/**
 	 * Simplified test that does not take into account 
 	 * if element declaration changed.
 	 */
 	boolean attributeNamesChanged() {
 		Set<String> as = new HashSet<String>();
 		NamedNodeMap attributes = fNode.getAttributes();
 		boolean changed = false;
 		if(attributes != null) {
 			for (int i = 0; i < attributes.getLength(); i++) {
 				Attr attr = (Attr) attributes.item(i);
 				String attrName = attr.getName();
 				as.add(attrName);
 				if(!attributeNames.contains(attrName)) changed = true;
 			}
 		}
 		if(!changed && attributeNames.size() != as.size()) {
 			changed = true;
 		}
 		if(changed) attributeNames = as;
 		return changed;		
 	}
 
 	protected void updatePropertyDescriptors() {
 		if (fDescriptors == null || fDescriptors.length == 0) return;
 
 		Map<String, IAttribute> as = getAttributes();
 
 
 		// List of all names encountered in the tag and defined by the element
 		List<String> declaredNames = new ArrayList<String>();
 		// New descriptor list that will become fDescriptors after all
 		// processing is done
 		List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
 		// Names of the descriptors in the above List
 		Set<String> descriptorNames = new HashSet<String>();
 		Set<String> descriptorNamesLow = new HashSet<String>();
 
 		// Update any descriptors derived from the metainfo
 		CMElementDeclaration ed = getDeclaration();
 		CMNamedNodeMap attrMap = null;
 		if (ed != null) {
 			attrMap = ed.getAttributes();
 		}
 		// Update exiting descriptors; not added to the final list here
 		if (attrMap != null) {
 			// Update existing descriptor types based on metainfo
 			CMAttributeDeclaration attrDecl = null;
 			for (int i = 0; i < attrMap.getLength(); i++) {
 				attrDecl = (CMAttributeDeclaration) attrMap.item(i);
 				String attrName = attrDecl.getAttrName();
 				if (!declaredNames.contains(attrName)) {
 					declaredNames.add(attrName);
 				}
 				for (int j = 0; j < fDescriptors.length; j++) {
 					boolean sameName = (fCaseSensitive && fDescriptors[j].getId().equals(attrDecl.getNodeName())) || (!fCaseSensitive && attrDecl.getNodeName().equals(fDescriptors[j].getId().toString()));
 					if (sameName) {
 						String[] validValues = getValidValues(attrDecl);
 						// Update the descriptor for this
 						// CMAttributeDeclaration (only enumerated values get
 						// updated for now)
 						if (fDescriptors[j] instanceof EnumeratedStringPropertyDescriptor) {
 							((EnumeratedStringPropertyDescriptor) fDescriptors[j]).updateValues(validValues);
 						}
 						// Replace with better descriptor
 						else if (validValues != null && validValues.length > 0) {
 							fDescriptors[j] = createPropertyDescriptor(attrDecl);
 							if(fDescriptors[j] instanceof TextPropertyDescriptor) {
 								IAttribute a = as.get(attrName);
 								if(a != null) {
 									fDescriptors[j] = createJSPPropertyDescriptor(a, attrDecl.getAttrName(), false);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		else {
 			// Update existing descriptors based on not having any metainfo
 			for (int j = 0; j < fDescriptors.length; j++) {
 				// Replace with basic descriptor
 				if (!(fDescriptors[j] instanceof TextPropertyDescriptor)) {
 					fDescriptors[j] = createDefaultPropertyDescriptor((String) fDescriptors[j].getId(), false);
 				}
 			}
 		}
 
 		NamedNodeMap attributes = fNode.getAttributes();
 
 		// Remove descriptors for attributes that aren't present AND aren't
 		// known through metainfo,
 		// do this by only reusing existing descriptors for attributes that
 		// are present or declared
 		if(attributes != null) for (int i = 0; i < fDescriptors.length; i++) {
 			if (fDescriptors[i] != null) {
 				String descriptorName = fDescriptors[i].getId().toString();
 				if ((declaredNames.contains(descriptorName) || (attributes.getNamedItem(descriptorName) != null)) && !descriptorNames.contains(descriptorName)) {
 					descriptorNames.add(descriptorName);
 					descriptorNamesLow.add(descriptorName.toLowerCase());
 					descriptors.add(fDescriptors[i]);
 				}
 			}
 		}
 
 		// Add descriptors for declared attributes that don't already have one
 		if (attrMap != null) {
 			// Update existing descriptor types based on metainfo
 			CMAttributeDeclaration attrDecl = null;
 			for (int i = 0; i < attrMap.getLength(); i++) {
 				attrDecl = (CMAttributeDeclaration) attrMap.item(i);
 				String attrName = attrDecl.getAttrName();
 				if (fCaseSensitive) {
 					if (!descriptorNames.contains(attrName)) {
 						IPropertyDescriptor descriptor = createPropertyDescriptor(attrDecl);
 						if(descriptor instanceof TextPropertyDescriptor) {
 							IAttribute a = as.get(attrName);
 							if(a != null) {
 								descriptor = createJSPPropertyDescriptor(a, attrDecl.getAttrName(), false);
 							}
 						}
 						if (descriptor != null) {
 							descriptorNames.add(attrName);
 							descriptorNamesLow.add(attrName.toLowerCase());
 							descriptors.add(descriptor);
 						}
 					}
 				}
 				else {
 					boolean exists = descriptorNamesLow.contains(attrName.toLowerCase());
 					if (!exists) {
 						descriptorNames.add(attrName);
 						descriptorNamesLow.add(attrName.toLowerCase());
 						IPropertyDescriptor descriptor = createPropertyDescriptor(attrDecl);
 						if(descriptor instanceof TextPropertyDescriptor) {
 							IAttribute a = as.get(attrName);
 							if(a != null) {
 								descriptor = createJSPPropertyDescriptor(a, attrDecl.getAttrName(), false);
 							}
 						}
 						if (descriptor != null) {
 							descriptorNames.add(attrName);
 							descriptorNamesLow.add(attrName.toLowerCase());
 							descriptors.add(descriptor);
 						}
 					}
 				}
 			}
 		}
 
 			for (int i = 0; i < fDescriptors.length; i++) {
 				if (fDescriptors[i] != null) {
 					String descriptorName = fDescriptors[i].getId().toString();
 					if (as.get(descriptorName) != null && !descriptorNames.contains(descriptorName)) {
 						descriptorNames.add(descriptorName);
 						descriptorNamesLow.add(descriptorName.toLowerCase());
 						descriptors.add(fDescriptors[i]);
 					}
 				}
 			}
 			for (IAttribute d: as.values()) {
 				String attrName = d.getName();
 				if (fCaseSensitive) {
 					if (descriptorNames.contains(attrName)) continue;
 				} else {
 					boolean exists = descriptorNamesLow.contains(attrName.toLowerCase());
 					if (exists) continue;
 				}
 				descriptorNames.add(attrName);
 				descriptorNamesLow.add(attrName.toLowerCase());
 				IPropertyDescriptor descriptor = null;
 				descriptor = createJSPPropertyDescriptor(d, attrName, false);
 				descriptors.add(descriptor);
 			}
 		// Add descriptors for existing attributes that don't already have one
 		if (attributes != null) {
 			for (int i = 0; i < attributes.getLength(); i++) {
 				Attr attr = (Attr) attributes.item(i);
 				String attrName = attr.getName();
 				if (fCaseSensitive) {
 					if (!descriptorNames.contains(attrName)) {
 						descriptorNames.add(attrName);
 						descriptorNamesLow.add(attrName.toLowerCase());
 						descriptors.add(createDefaultPropertyDescriptor(attrName, false));
 					}
 				} else {
 					boolean exists = descriptorNamesLow.contains(attrName.toLowerCase());
 					if (!exists) {
 						descriptorNames.add(attrName);
 						descriptorNamesLow.add(attrName.toLowerCase());
 						descriptors.add(createDefaultPropertyDescriptor(attrName, false));
 					}
 				}
 			}
 		}
 
 		// Update fDescriptors
 		IPropertyDescriptor[] newDescriptors = new IPropertyDescriptor[descriptors.size()];
 		for (int i = 0; i < newDescriptors.length; i++)
 			newDescriptors[i] = (IPropertyDescriptor) descriptors.get(i);
 		fDescriptors = newDescriptors;
 	}
 
 	private Stack<Object> fValuesBeingSet = new Stack<Object>();
 
 	public void setPropertyValue(Object nameObject, Object value) {
 		// Avoid cycling - can happen if a closing cell editor causes a
 		// refresh
 		// on the PropertySheet page and the setInput again asks the editor to
 		// close; besides, why apply the same value twice?
 		if (!fValuesBeingSet.isEmpty() && fValuesBeingSet.peek() == nameObject)
 			return;
 		fValuesBeingSet.push(nameObject);
 		String name = nameObject.toString();
 		String valueString = null;
 		if (value != null)
 			valueString = value.toString();
 		NamedNodeMap attrMap = fNode.getAttributes();
 		try {
 			if (attrMap != null) {
 				Attr attr = (Attr) attrMap.getNamedItem(name);
 				if (attr != null) {
 					// EXISTING VALUE
 					// potential out of control loop if updating the value
 					// triggers a viewer update, forcing the
 					// active cell editor to save its value and causing the
 					// loop to continue
 					if (attr.getValue() == null || !attr.getValue().equals(valueString)) {
 						if(valueString == null || valueString.length() == 0 && !isRequiredAttribute(attr.getName())) {
 							fNode.getAttributes().removeNamedItem(attr.getName());
 						} else {
 							if (attr instanceof IDOMNode) {
 								((IDOMNode) attr).setValueSource(valueString);
 								IVisualController controller = valueHelper.getController(); 
 								if(controller != null) controller.visualRefresh();
 							} else {
 								attr.setValue(valueString);
 							}
 						}
 					}
 				}
 				else {
 					// NEW(?) value
 					if (value != null && fNode.getOwnerDocument() != null) { // never create an empty attribute
 						Attr newAttr = fNode.getOwnerDocument().createAttribute(name);
 						if (newAttr instanceof IDOMNode)
 							((IDOMNode) newAttr).setValueSource(valueString);
 						else
 							newAttr.setValue(valueString);
 						attrMap.setNamedItem(newAttr);
 					}
 				}
 			}
 			else {
 				if (fNode instanceof Element) {
 					((Element) fNode).setAttribute(name, valueString);
 				}
 			}
 		}
 		catch (DOMException e) {
 			JspEditorPlugin.getPluginLog().logError(e);
 		}
 		fValuesBeingSet.pop();
 	}
 	
 	boolean isRequiredAttribute(String attributeName) {
 		Map<String, IAttribute> as = getAttributes();
 		IAttribute d = as.get(attributeName);
 //		String query = getQuery(attributeName);
 //		AttributeDescriptor d = valueHelper.getAttributeDescriptor(query);
 		if(d == null) return false; // do not remove unknown attribute? Remove it!
 		return d.isRequired();
 	}
 	
 	private Map<String, IAttribute> getAttributes() {
		return PageProcessor.getInstance().getAttributesAsMap(kbQuery, pageContext);
 	}
 	
 	//////// XMLPropertyDescriptor
 
 	public boolean isAdapterForType(Object type) {
 		return type == IPropertySource.class;
 	}
 
 	public void notifyChanged(INodeNotifier notifier, int eventType, Object changedFeature, Object oldValue, Object newValue, int pos) {
 	}
 
 	public Object getEditableValue() {
 		return null;
 	}
 
 	public Object getPropertyValue(Object nameObject) {
 		String name = nameObject.toString();
 		String returnedValue = null;
 		NamedNodeMap attrMap = fNode.getAttributes();
 		if (attrMap != null) {
 			Node attribute = attrMap.getNamedItem(name);
 			if (attribute != null) {
 				if (attribute instanceof IDOMNode)
 					returnedValue = ((IDOMNode) attribute).getValueSource();
 				else
 					returnedValue = attribute.getNodeValue();
 			}
 		}
 		if (returnedValue == null)
 			returnedValue = ""; //$NON-NLS-1$
 		return returnedValue;
 	}
 
 	public boolean isPropertySet(Object propertyObject) {
 		String property = propertyObject.toString();
 
 		NamedNodeMap attrMap = fNode.getAttributes();
 		if (attrMap != null)
 			return attrMap.getNamedItem(property) != null;
 		return false;
 	}
 
 	public void resetPropertyValue(Object propertyObject) {
 		String property = propertyObject.toString();
 		CMNamedNodeMap attrDecls = null;
 
 		CMElementDeclaration ed = getDeclaration();
 		if (ed != null) {
 			attrDecls = ed.getAttributes();
 		}
 
 		NamedNodeMap attrMap = fNode.getAttributes();
 		if (attrDecls != null) {
 			CMAttributeDeclaration attrDecl = (CMAttributeDeclaration) attrDecls.getNamedItem(property);
 			String defValue = null;
 			if (attrDecl != null) {
 				if (attrDecl.getAttrType() != null) {
 					CMDataType helper = attrDecl.getAttrType();
 					if (helper.getImpliedValueKind() != CMDataType.IMPLIED_VALUE_NONE && helper.getImpliedValue() != null)
 						defValue = helper.getImpliedValue();
 				}
 			}
 			if (defValue != null && defValue.length() > 0) {
 				((Attr) attrMap.getNamedItem(property)).setValue(defValue);
 			}
 			else {
 				attrMap.removeNamedItem(property);
 			}
 		}
 		else {
 			attrMap.removeNamedItem(property);
 		}
 	}
 
 	public boolean isPropertyRemovable(Object id) {
 		return true;
 	}
 
 	public void removeProperty(Object propertyObject) {
 		NamedNodeMap attrMap = fNode.getAttributes();
 		if (attrMap != null) {
 			Node attribute = attrMap.getNamedItem(propertyObject.toString());
 			if (attribute != null) {
 				try {
 					attrMap.removeNamedItem(propertyObject.toString());
 				}
 				catch (DOMException e) {
 					JspEditorPlugin.getPluginLog().logError(e);
 				}
 			}
 		}
 	}
 
 	public boolean isPropertyResettable(Object id) {
 		boolean resettable = false;
 		String property = id.toString();
 		CMNamedNodeMap attrDecls = null;
 
 		CMElementDeclaration ed = getDeclaration();
 		if (ed != null) {
 			attrDecls = ed.getAttributes();
 		}
 
 		if (attrDecls != null) {
 			CMAttributeDeclaration attrDecl = (CMAttributeDeclaration) attrDecls.getNamedItem(property);
 			if (attrDecl != null) {
 				if (attrDecl.getAttrType() != null) {
 					CMDataType helper = attrDecl.getAttrType();
 					if (helper.getImpliedValueKind() != CMDataType.IMPLIED_VALUE_NONE && helper.getImpliedValue() != null) {
 						resettable = true;
 					}
 				}
 			}
 		}
 		return resettable;
 	}
 
 	protected IPropertyDescriptor createPropertyDescriptor(CMAttributeDeclaration attrDecl) {
 		IPropertyDescriptor descriptor = null;
 		CMDataType attrType = attrDecl.getAttrType();
 
 		if (attrType != null) {
 			// handle declarations that provide FIXED/ENUMERATED values
 			if (attrType.getEnumeratedValues() != null && attrType.getEnumeratedValues().length > 0) {
 				descriptor = createEnumeratedPropertyDescriptor(attrDecl, attrType);
 			}
 			else if ((attrDecl.getUsage() == CMAttributeDeclaration.FIXED || attrType.getImpliedValueKind() == CMDataType.IMPLIED_VALUE_FIXED) && attrType.getImpliedValue() != null) {
 				descriptor = createFixedPropertyDescriptor(attrDecl, attrType);
 			}
 			else {
 				// plain text
 				descriptor = createTextPropertyDescriptor(attrDecl);
 			}
 		}
 		else {
 			// no extra information given
 			descriptor = createTextPropertyDescriptor(attrDecl);
 		}
 		return descriptor;
 	}
 
 	private IPropertyDescriptor createTextPropertyDescriptor(CMAttributeDeclaration attrDecl) {
 		return createTextPropertyDescriptor(attrDecl.getAttrName(), getCategory(attrDecl), attrDecl.getUsage());
 	}
 
 	private IPropertyDescriptor createTextPropertyDescriptor(String name, String category, int usage) {
 		TextPropertyDescriptor descriptor = new TextPropertyDescriptor(name, name);
 		descriptor.setCategory(category);
 		descriptor.setDescription(name);
 		if (usage != CMAttributeDeclaration.REQUIRED && SET_EXPERT_FILTER)
 			descriptor.setFilterFlags(new String[]{IPropertySheetEntry.FILTER_ID_EXPERT});
 		return descriptor;
 	}
 
 	private IPropertyDescriptor createEnumeratedPropertyDescriptor(CMAttributeDeclaration attrDecl, CMDataType valuesHelper) {
 		// the displayName MUST be set
 	    	EnumeratedStringPropertyDescriptor descriptor = new EnumeratedStringPropertyDescriptor(attrDecl.getAttrName(), attrDecl.getAttrName(), _getValidStrings(attrDecl, valuesHelper));
 		descriptor.setCategory(getCategory(attrDecl));
 		descriptor.setDescription(attrDecl.getAttrName());
 		if (attrDecl.getUsage() != CMAttributeDeclaration.REQUIRED && SET_EXPERT_FILTER)
 			descriptor.setFilterFlags(new String[]{IPropertySheetEntry.FILTER_ID_EXPERT});
 		return descriptor;
 	}
 
 	private IPropertyDescriptor createFixedPropertyDescriptor(CMAttributeDeclaration attrDecl, CMDataType helper) {
 		// the displayName MUST be set
 		EnumeratedStringPropertyDescriptor descriptor = new EnumeratedStringPropertyDescriptor(attrDecl.getNodeName(), attrDecl.getNodeName(), _getValidFixedStrings(attrDecl, helper));
 		descriptor.setCategory(getCategory(attrDecl));
 		descriptor.setDescription(attrDecl.getAttrName());
 		return descriptor;
 	}
 
 	private String[] getValidValues(CMAttributeDeclaration attrDecl) {
 		if (attrDecl == null)
 			return new String[0];
 
 		String[] validValues = null;
 		CMDataType attrType = attrDecl.getAttrType();
 		if (attrType != null) {
 			validValues = _getValidStrings(attrDecl, attrType);
 		}
 		if (validValues == null)
 			validValues = new String[0];
 		return validValues;
 	}
 
 	private String[] _getValidStrings(CMAttributeDeclaration attrDecl, CMDataType valuesHelper) {
 		String attributeName = attrDecl.getAttrName();
 		List values = new ArrayList(1);
 		boolean currentValueKnown = false;
 		boolean checkIfCurrentValueIsKnown = (fNode.getAttributes() != null && fNode.getAttributes().getNamedItem(attributeName) != null && fNode.getAttributes().getNamedItem(attributeName).getNodeValue() != null);
 		String currentValue = null;
 		if (checkIfCurrentValueIsKnown)
 			currentValue = fNode.getAttributes().getNamedItem(attributeName).getNodeValue();
 
 		if (valuesHelper.getImpliedValueKind() == CMDataType.IMPLIED_VALUE_FIXED && valuesHelper.getImpliedValue() != null) {
 			// FIXED value
 			currentValueKnown = currentValue != null && valuesHelper.getImpliedValue().equals(currentValue);
 			values.add(valuesHelper.getImpliedValue());
 		}
 		else {
 			// ENUMERATED values
 			String[] valueStrings = null;
 			// valueStrings = valuesHelper.getEnumeratedValues();
 			Document document = fNode.getOwnerDocument();
 			ModelQuery modelQuery = (document == null) ? null : ModelQueryUtil.getModelQuery(document);
 			if (modelQuery != null && fNode.getNodeType() == Node.ELEMENT_NODE) {
 				valueStrings = modelQuery.getPossibleDataTypeValues((Element) fNode, attrDecl);
 			}
 			else {
 				valueStrings = attrDecl.getAttrType().getEnumeratedValues();
 			}
 			if (valueStrings != null) {
 				for (int i = 0; i < valueStrings.length; i++) {
 					if (checkIfCurrentValueIsKnown && valueStrings[i].equals(currentValue))
 						currentValueKnown = true;
 					values.add(valueStrings[i]);
 				}
 			}
 		}
 		if (valuesHelper.getImpliedValueKind() != CMDataType.IMPLIED_VALUE_NONE && valuesHelper.getImpliedValue() != null) {
 			if (!values.contains(valuesHelper.getImpliedValue()))
 				values.add(valuesHelper.getImpliedValue());
 		}
 
 		if (checkIfCurrentValueIsKnown && !currentValueKnown && currentValue != null && currentValue.length() > 0)
 			values.add(currentValue);
 		String[] validStrings = new String[values.size()];
 		validStrings = (String[]) values.toArray(validStrings);
 		return validStrings;
 	}
 
 	private String[] _getValidFixedStrings(CMAttributeDeclaration attrDecl, CMDataType helper) {
 		String attributeName = attrDecl.getAttrName();
 		List values = new ArrayList(1);
 		String impliedValue = helper.getImpliedValue();
 		if (impliedValue != null)
 			values.add(impliedValue);
 		boolean checkIfCurrentValueIsIncluded = (fNode.getAttributes() != null && fNode.getAttributes().getNamedItem(attributeName) != null && fNode.getAttributes().getNamedItem(attributeName).getNodeValue() != null);
 		if (checkIfCurrentValueIsIncluded) {
 			String currentValue = null;
 			currentValue = fNode.getAttributes().getNamedItem(attributeName).getNodeValue();
 			if (!currentValue.equals(impliedValue))
 				values.add(currentValue);
 		}
 		String[] validStrings = new String[values.size()];
 		validStrings = (String[]) values.toArray(validStrings);
 		return validStrings;
 	}
 
 }

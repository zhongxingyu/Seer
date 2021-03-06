 /*******************************************************************************
  * Copyright (c) 2001, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.xsd.ui.internal.adapters;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.NotificationImpl;
import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.xsd.ui.internal.adt.actions.BaseSelectionAction;
 import org.eclipse.wst.xsd.ui.internal.adt.actions.ShowPropertiesViewAction;
 import org.eclipse.wst.xsd.ui.internal.adt.design.editparts.model.IActionProvider;
 import org.eclipse.wst.xsd.ui.internal.adt.facade.IADTObjectListener;
 import org.eclipse.wst.xsd.ui.internal.adt.facade.IModel;
 import org.eclipse.wst.xsd.ui.internal.adt.outline.ITreeElement;
 import org.eclipse.wst.xsd.ui.internal.common.actions.AddXSDComplexTypeDefinitionAction;
 import org.eclipse.wst.xsd.ui.internal.common.actions.AddXSDElementAction;
 import org.eclipse.wst.xsd.ui.internal.editor.Messages;
 import org.eclipse.wst.xsd.ui.internal.editor.XSDEditorPlugin;
 import org.eclipse.xsd.XSDAttributeDeclaration;
 import org.eclipse.xsd.XSDAttributeGroupDefinition;
 import org.eclipse.xsd.XSDComplexTypeDefinition;
 import org.eclipse.xsd.XSDConcreteComponent;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDImport;
 import org.eclipse.xsd.XSDInclude;
 import org.eclipse.xsd.XSDModelGroupDefinition;
 import org.eclipse.xsd.XSDPackage;
 import org.eclipse.xsd.XSDRedefine;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.XSDSchemaDirective;
 import org.eclipse.xsd.XSDSimpleTypeDefinition;
 import org.eclipse.xsd.XSDTypeDefinition;
 import org.eclipse.xsd.util.XSDConstants;
 
 public class XSDSchemaAdapter extends XSDBaseAdapter implements IActionProvider, IModel, IADTObjectListener
 {
   protected List types = null;
   protected List children, allChildren;
 
   protected CategoryAdapter fDirectivesCategory;
   protected CategoryAdapter fElementsCategory;
   protected CategoryAdapter fAttributesCategory;
   protected CategoryAdapter fTypesCategory;
   protected CategoryAdapter fGroupsCategory;
 
   /**
    * Create all the category adapters
    * 
    * @param xsdSchema
    */
   protected void createCategoryAdapters(XSDSchema xsdSchema)
   {
     List directivesList = getDirectives(xsdSchema);
     List elementsList = getGlobalElements(xsdSchema);
     List attributesList = getAttributeList(xsdSchema);
     List groups = getGroups(xsdSchema);
     List types = getComplexTypes(xsdSchema);
     types.addAll(getSimpleTypes(xsdSchema));
 
     fDirectivesCategory = new CategoryAdapter(Messages._UI_GRAPH_DIRECTIVES, XSDEditorPlugin.getDefault().getIconImage("obj16/directivesheader"), directivesList, xsdSchema, CategoryAdapter.DIRECTIVES); //$NON-NLS-1$
     fDirectivesCategory.setAllChildren(directivesList);
     registerListener(fDirectivesCategory);
 
     fElementsCategory = new CategoryAdapter(Messages._UI_GRAPH_ELEMENTS, XSDEditorPlugin.getDefault().getIconImage("obj16/elementsheader"), elementsList, xsdSchema, CategoryAdapter.ELEMENTS);  //$NON-NLS-1$
     fElementsCategory.setAllChildren(getGlobalElements(xsdSchema, true));
     registerListener(fElementsCategory);
 
     fAttributesCategory = new CategoryAdapter(Messages._UI_GRAPH_ATTRIBUTES, XSDEditorPlugin.getDefault().getIconImage("obj16/attributesheader"), attributesList, xsdSchema, CategoryAdapter.ATTRIBUTES);   //$NON-NLS-1$
     fAttributesCategory.setAllChildren(attributesList);
     registerListener(fAttributesCategory);
 
     fTypesCategory = new CategoryAdapter(Messages._UI_GRAPH_TYPES, XSDEditorPlugin.getDefault().getIconImage("obj16/typesheader"), types, xsdSchema, CategoryAdapter.TYPES);  //$NON-NLS-1$
     fTypesCategory.setAllChildren(getTypes(xsdSchema, true));
     registerListener(fTypesCategory);
 
     fGroupsCategory = new CategoryAdapter(Messages._UI_GRAPH_GROUPS, XSDEditorPlugin.getDefault().getIconImage("obj16/groupsheader"), groups, xsdSchema, CategoryAdapter.GROUPS); //$NON-NLS-1$
     fGroupsCategory.setAllChildren(groups);
     registerListener(fGroupsCategory);
   }
 
   public List getTypes()
   {
     if (types == null)
     {
       types = new ArrayList();
       XSDSchema schema = (XSDSchema) target;
       List concreteComponentList = new ArrayList();
       for (Iterator i = schema.getContents().iterator(); i.hasNext();)
       {
         XSDConcreteComponent component = (XSDConcreteComponent) i.next();
         if (component instanceof XSDTypeDefinition)
         {
           concreteComponentList.add(component);
         }
       }
       populateAdapterList(concreteComponentList, types);
     }
     return types;
   }
   
   protected boolean isSameNamespace(String ns1, String ns2)
   {
     if (ns1 == null) ns1 = "";
     if (ns2 == null) ns2 = "";
     
     if (ns1.equals(ns2))
     {
       return true;
     }
     return false;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.wst.xsd.ui.internal.adt.outline.ITreeElement#getChildren()
    */
   public ITreeElement[] getChildren()
   {
     XSDSchema xsdSchema = (XSDSchema) getTarget();
 
     children = new ArrayList();
 
     // just set categoryadapters' children if category adapters are
     // already created
     if (fDirectivesCategory != null)
     {
       List directivesList = getDirectives(xsdSchema);
       List elementsList = getGlobalElements(xsdSchema);
       List attributesList = getAttributeList(xsdSchema);
       List groups = getGroups(xsdSchema);
       List types = getComplexTypes(xsdSchema);
       types.addAll(getSimpleTypes(xsdSchema));
 
       fDirectivesCategory.setChildren(directivesList);
       fDirectivesCategory.setAllChildren(directivesList);
       fElementsCategory.setChildren(elementsList);
       fElementsCategory.setAllChildren(getGlobalElements(xsdSchema, true));
       fAttributesCategory.setChildren(attributesList);
       fAttributesCategory.setAllChildren(getAttributeList(xsdSchema, true));
       fTypesCategory.setChildren(types);
       fTypesCategory.setAllChildren(getTypes(xsdSchema, true));
       fGroupsCategory.setChildren(groups);
       fGroupsCategory.setAllChildren(groups);
     }
     else
     {
       createCategoryAdapters(xsdSchema);
     }
 
     children.add(fDirectivesCategory);
     children.add(fElementsCategory);
     children.add(fAttributesCategory);
     children.add(fTypesCategory);
     children.add(fGroupsCategory);
 
     return (ITreeElement[]) children.toArray(new ITreeElement[0]);
   }
 
   public void notifyChanged(final Notification msg)
   {
     class CategoryNotification extends NotificationImpl
     {
       protected Object category;
 
       public CategoryNotification(Object category)
       {
         super(msg.getEventType(), msg.getOldValue(), msg.getNewValue(), msg.getPosition());
         this.category = category;
       }
 
       public Object getNotifier()
       {
         return category;
       }
 
       public Object getFeature()
       {
         return msg.getFeature();
       }
     }
 
     if (children == null)
     {
       getChildren();
     }
     
     Object newValue = msg.getNewValue();
     if (newValue instanceof XSDInclude || newValue instanceof XSDImport || newValue instanceof XSDRedefine)
     {
       CategoryAdapter adapter = getCategory(CategoryAdapter.DIRECTIVES);
       Assert.isTrue(adapter != null);
       XSDSchema xsdSchema = adapter.getXSDSchema();
       adapter.setChildren(getDirectives(xsdSchema));
       adapter.setAllChildren(getDirectives(xsdSchema));
       notifyListeners(new CategoryNotification(adapter), adapter.getText());
       return;
     }
     else if (msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_ElementDeclarations())
     {
       CategoryAdapter adapter = getCategory(CategoryAdapter.ELEMENTS);
       Assert.isTrue(adapter != null);
       XSDSchema xsdSchema = adapter.getXSDSchema();
       adapter.setChildren(getGlobalElements(xsdSchema));
       adapter.setAllChildren(getGlobalElements(xsdSchema, true));
       notifyListeners(new CategoryNotification(adapter), adapter.getText());
       return;
     }
     else if (msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_AttributeDeclarations() ||
              msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_AttributeGroupDefinitions())
     {
       CategoryAdapter adapter = getCategory(CategoryAdapter.ATTRIBUTES);
       Assert.isTrue(adapter != null);
       XSDSchema xsdSchema = adapter.getXSDSchema();
       adapter.setChildren(getAttributeList(xsdSchema));
       adapter.setAllChildren(getAttributeList(xsdSchema, true));
       notifyListeners(new CategoryNotification(adapter), adapter.getText());
       return;
     }
     else if (msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_TypeDefinitions())
     {
       CategoryAdapter adapter = getCategory(CategoryAdapter.TYPES);
       Assert.isTrue(adapter != null);
       XSDSchema xsdSchema = adapter.getXSDSchema();
       List types = getComplexTypes(xsdSchema);
       types.addAll(getSimpleTypes(xsdSchema));
 
       adapter.setChildren(types);
       adapter.setAllChildren(getTypes(xsdSchema, true));
       notifyListeners(new CategoryNotification(adapter), adapter.getText());
       return;
     }
     else if (msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_ModelGroupDefinitions())
     {
       CategoryAdapter adapter = getCategory(CategoryAdapter.GROUPS);
       Assert.isTrue(adapter != null);
       XSDSchema xsdSchema = adapter.getXSDSchema();
       adapter.setChildren(getGroups(xsdSchema));
       adapter.setAllChildren(getGroups(xsdSchema));
       notifyListeners(new CategoryNotification(adapter), adapter.getText());
       return;
     }
     else if (msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_Annotations())
     {
       return;
     }
     else if (msg.getFeature() == XSDPackage.eINSTANCE.getXSDSchema_SchemaLocation())
     {
       notifyListeners(msg, null);
       return;
     }
     
     types = null;
     getTypes();
 
     super.notifyChanged(msg);
   }
   
   public void updateCategories()
   {
     // TODO: revisit this
     getChildren();
   }
   
   public CategoryAdapter getCategory(int category)
   {
     if (children == null) updateCategories(); // init categories
     int length = children.size();
     CategoryAdapter adapter = null;
     for (int i = 0; i < length; i++)
     {
       adapter = (CategoryAdapter) children.get(i);
       if (adapter.getGroupType() ==  category)
       {
         break;
       }
     }
     return adapter;
   }
 
   protected List getDirectives(XSDSchema schema)
   {
     List list = new ArrayList();
     for (Iterator i = schema.getContents().iterator(); i.hasNext();)
     {
       Object o = i.next();
       if (o instanceof XSDSchemaDirective)
       {
         list.add(o);
       }
     }
     List adapterList = new ArrayList();
     populateAdapterList(list, adapterList);
     return adapterList;
   }
   
   protected List getGlobalElements(XSDSchema schema, boolean showFromIncludes)
   {
     List elements = schema.getElementDeclarations();
     List list = new ArrayList();
     for (Iterator i = elements.iterator(); i.hasNext();)
     {
       XSDElementDeclaration elem = (XSDElementDeclaration) i.next();
       if (isSameNamespace(elem.getTargetNamespace(),schema.getTargetNamespace()) && (elem.getRootContainer() == schema || showFromIncludes))
       {
         list.add(elem);
       }
     }
     List adapterList = new ArrayList();
     populateAdapterList(list, adapterList);
     return adapterList;
   }
 
   protected List getGlobalElements(XSDSchema schema)
   {
     return getGlobalElements(schema, false);
   }
 
   /**
    * @param schema
    * @return
    */
   protected List getComplexTypes(XSDSchema schema, boolean showFromIncludes)
   {
     List allTypes = schema.getTypeDefinitions();
     List list = new ArrayList();
     for (Iterator i = allTypes.iterator(); i.hasNext();)
     {
       XSDTypeDefinition td = (XSDTypeDefinition) i.next();
       if (td instanceof XSDComplexTypeDefinition)
       {
         XSDComplexTypeDefinition ct = (XSDComplexTypeDefinition) td;
         if (isSameNamespace(ct.getTargetNamespace(),schema.getTargetNamespace()) && (ct.getRootContainer() == schema || showFromIncludes))
         {
           list.add(ct);
         }
       }
     }
     List adapterList = new ArrayList();
     populateAdapterList(list, adapterList);
     return adapterList;
   }
 
   protected List getComplexTypes(XSDSchema schema)
   {
     return getComplexTypes(schema, false);
   }
   
   protected List getTypes(XSDSchema schema, boolean showFromIncludes)
   {
     List list = getComplexTypes(schema, showFromIncludes);
     list.addAll(getSimpleTypes(schema, showFromIncludes));
     return list;
   }
   
   protected List getAttributeGroupList(XSDSchema xsdSchema, boolean showFromIncludes)
   {
     List attributeGroupList = new ArrayList();
     for (Iterator i = xsdSchema.getAttributeGroupDefinitions().iterator(); i.hasNext();)
     {
       XSDAttributeGroupDefinition attrGroup = (XSDAttributeGroupDefinition) i.next();
       if (isSameNamespace(attrGroup.getTargetNamespace(), xsdSchema.getTargetNamespace()) && (attrGroup.getRootContainer() == xsdSchema || showFromIncludes))
       {
         attributeGroupList.add(attrGroup);
       }
     }
     List adapterList = new ArrayList();
     populateAdapterList(attributeGroupList, adapterList);
     return adapterList;
   }
   
   protected List getAttributeGroupList(XSDSchema xsdSchema)
   {
     return getAttributeGroupList(xsdSchema, false);
   }
 
   protected List getAttributeList(XSDSchema xsdSchema, boolean showFromIncludes)
   {
     List attributesList = new ArrayList();
     for (Iterator iter = xsdSchema.getAttributeDeclarations().iterator(); iter.hasNext();)
     {
       Object o = iter.next();
       if (o instanceof XSDAttributeDeclaration)
       {
         XSDAttributeDeclaration attr = (XSDAttributeDeclaration) o;
         if (attr != null)
         {
           if (attr.getTargetNamespace() != null)
           {
             if (!(attr.getTargetNamespace().equals(XSDConstants.SCHEMA_INSTANCE_URI_2001)))
             {
               if (isSameNamespace(attr.getTargetNamespace(), xsdSchema.getTargetNamespace()) && (attr.getRootContainer() == xsdSchema || showFromIncludes))
               {
                 attributesList.add(attr);
               }
             }
           }
           else
           {
             if (isSameNamespace(attr.getTargetNamespace(), xsdSchema.getTargetNamespace()) && (attr.getRootContainer() == xsdSchema || showFromIncludes))
             {
               attributesList.add(attr);
             }
           }
         }
       }
     }
     
     attributesList.addAll(getAttributeGroupList(xsdSchema, showFromIncludes));
     
     List adapterList = new ArrayList();
     populateAdapterList(attributesList, adapterList);
     return adapterList;
   }
   
   protected List getAttributeList(XSDSchema xsdSchema)
   {
     return getAttributeList(xsdSchema, false);
   }
 
   protected List getSimpleTypes(XSDSchema schema, boolean showFromIncludes)
   {
     List allTypes = schema.getTypeDefinitions();
     List list = new ArrayList();
     for (Iterator i = allTypes.iterator(); i.hasNext();)
     {
       XSDTypeDefinition td = (XSDTypeDefinition) i.next();
       if (td instanceof XSDSimpleTypeDefinition)
       {
         XSDSimpleTypeDefinition st = (XSDSimpleTypeDefinition) td;
         if (isSameNamespace(st.getTargetNamespace(),schema.getTargetNamespace()) && (st.getRootContainer() == schema || showFromIncludes))
         {
           list.add(st);
         }
       }
     }
     List adapterList = new ArrayList();
     populateAdapterList(list, adapterList);
     return adapterList;
   }
   
   protected List getSimpleTypes(XSDSchema schema)
   {
     return getSimpleTypes(schema, false);
   }
 
   protected List getGroups(XSDSchema schema)
   {
     List groups = schema.getModelGroupDefinitions();
     List list = new ArrayList();
     for (Iterator i = groups.iterator(); i.hasNext();)
     {
       XSDModelGroupDefinition group = (XSDModelGroupDefinition) i.next();
       if (isSameNamespace(group.getTargetNamespace(),schema.getTargetNamespace()) && group.getRootContainer() == schema)
       {
         list.add(group);
       }
     }
     List adapterList = new ArrayList();
     populateAdapterList(list, adapterList);
     return adapterList;
   }
   
   public String[] getActions(Object object)
   {
      Collection actionIDs = new ArrayList();
      actionIDs.add(AddXSDElementAction.ID);
      actionIDs.add(AddXSDComplexTypeDefinitionAction.ID);
 
      actionIDs.add(BaseSelectionAction.SEPARATOR_ID);
      actionIDs.add(ShowPropertiesViewAction.ID);
      return (String [])actionIDs.toArray(new String[0]);
   }
 
   public void propertyChanged(Object object, String property)
   {
     notifyListeners(object, property);
   }
  
  public Image getImage()
  {
    return XSDEditorPlugin.getXSDImage("icons/XSDFile.gif"); //$NON-NLS-1$
  }
 }

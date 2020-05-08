 /*******************************************************************************
  * Copyright (c) 2008 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facelet.core.internal.cm.strategy;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jst.jsf.common.dom.TagIdentifier;
 import org.eclipse.jst.jsf.common.internal.managedobject.IManagedObject;
 import org.eclipse.jst.jsf.common.internal.managedobject.ObjectManager.ManagedObjectException;
 import org.eclipse.jst.jsf.common.internal.resource.ResourceSingletonObjectManager;
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCorePlugin;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.AttributeCMAdapter;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.ExternalTagInfo;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.addtagmd.AddTagMDPackage;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.addtagmd.AttributeData;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.addtagmd.AttributeUsage;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.addtagmd.ElementData;
 import org.eclipse.jst.jsf.facelet.core.internal.cm.addtagmd.provider.IResourceProvider;
 import org.eclipse.jst.jsf.facelet.core.internal.util.TagMetadataLoader;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
 
 /**
  * An external meta-data strategy that uses the JSF meta-data framework.
  * 
  * @author cbateman
  * 
  */
 public class MDExternalMetadataStrategy extends
         AbstractExternalMetadataStrategy implements IManagedObject
 {
     private static MySingletonManager MANAGER = new MySingletonManager();
 
     /**
      * @param project
      * @return the instance of the strategy for project or
      */
     public static IExternalMetadataStrategy create(final IProject project)
     {
         try
         {
             return MANAGER.getInstance(project);
         }
         catch (final ManagedObjectException e)
         {
             FaceletCorePlugin.log(
                     "Getting managed instance of tag metadata strategy", e); //$NON-NLS-1$
         }
         return new NullExternalMetadataStrategy();
     }
 
     /**
      * The unique identifier for the strategy.
      */
     public final static String                   STRATEGY_ID = "org.eclipse.jst.jsf.facelet.core.internal.cm.strategy.MDExternalMetadataStrategy"; //$NON-NLS-1$
 
     //    private static final String     VAR         = "var";                                                  //$NON-NLS-1$
     //    private static final String     VALUE       = "value";                                                //$NON-NLS-1$
     //    private static final String     SRC         = "src";                                                  //$NON-NLS-1$
     //    private static final String     NAME        = "name";                                                 //$NON-NLS-1$
     //    private static final String     HOTKEY      = "hotkey";                                               //$NON-NLS-1$
     //    private static final String     TEMPLATE    = "template";                                             //$NON-NLS-1$
     //    private static final String     BINDING     = "binding";                                              //$NON-NLS-1$
     //    private static final String     ID          = "id";                                                   //$NON-NLS-1$
 
     private final IProject                       _project;
     private final TagMetadataLoader              _tagMetadataLoader;
     private final Map<String, MDExternalTagInfo> _cached;
     private final AtomicBoolean                  _isDisposed = new AtomicBoolean(
                                                                      false);
 
     /**
      * Default constructor
      * 
      * @param project
      */
     private MDExternalMetadataStrategy(final IProject project)
     {
         super(STRATEGY_ID, Messages.MDExternalMetadataStrategy_DisplayName);
         _project = project;
         _tagMetadataLoader = new TagMetadataLoader(_project);
         _cached = new HashMap<String, MDExternalTagInfo>();
     }
 
     public void checkpoint()
     {
         // do nothing
     }
 
     public void destroy()
     {
         // currently no persistent state, so just dispose
     }
 
     public void dispose()
     {
         if (_isDisposed.compareAndSet(false, true))
         {
             _cached.clear();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jst.jsf.facelet.core.internal.cm.strategy.
      * AbstractExternalMetadataStrategy
      * #perform(org.eclipse.jst.jsf.common.dom.TagIdentifier)
      */
     @Override
     public ExternalTagInfo perform(final TagIdentifier input) throws Exception
     {
         MDExternalTagInfo tagInfo = _cached.get(input.getUri());
 
         if (tagInfo == null)
         {
             tagInfo = new MDExternalTagInfo(input.getUri(), _tagMetadataLoader);
             _cached.put(input.getUri(), tagInfo);
         }
         return tagInfo;
     }
 
     private static class MDExternalTagInfo extends ExternalTagInfo
     {
         private final String                            _uri;
         private final TagMetadataLoader                 _tagMetadataLoader;
         private final Map<String, InternalNamedNodeMap> _attributes;
 
         public MDExternalTagInfo(final String uri,
                 final TagMetadataLoader tagMetadataLoader)
         {
             _uri = uri;
             _tagMetadataLoader = tagMetadataLoader;
             _attributes = new HashMap<String, InternalNamedNodeMap>();
         }
 
         @Override
         public CMNamedNodeMap getAttributes(final String tagName)
         {
             final InternalNamedNodeMap nodeMap = _attributes.get(tagName);
             final InternalNamedNodeMap[] innerClassNodeMap = new InternalNamedNodeMap[1];
             innerClassNodeMap[0] = nodeMap;
 
             if (nodeMap == null)
             {
                 SafeRunnable.run(new ISafeRunnable()
                 {
                     public void run()
                     {
                         final ElementData data = _tagMetadataLoader
                                 .getElementData(_uri, tagName);
 
                         if (data != null)
                         {
                             innerClassNodeMap[0] = new InternalNamedNodeMap();
 
                             for (final AttributeData attribute : data
                                     .getAttributes())
                             {
                                 innerClassNodeMap[0]
                                         .add(createAttribute(attribute));
                             }
                             _attributes.put(tagName, innerClassNodeMap[0]);
                         }
                         // no meta-data found for this tag, so mark as null
                         // instance so future calls don't bother a re-lookup.
                         else
                         {
                             _attributes.put(tagName,
                                     MDExternalMetadataStrategy.NULL_INSTANCE);
                         }
                     }
 
                     public void handleException(final Throwable exception)
                     {
                         FaceletCorePlugin.log(
                                 "While loading attribute meta-data", exception); //$NON-NLS-1$
                     }
                 });
             }
 
             return innerClassNodeMap[0];
         }
 
         @Override
         public Object getTagProperty(final String tagName, final String key)
         {
             final Object[] value = new Object[1];
             value[0] = null;
 
             SafeRunnable.run(new ISafeRunnable()
             {
                 public void run()
                 {
                     if ("description".equals(key)) //$NON-NLS-1$
                     {
                         value[0] = _tagMetadataLoader.getDescription(_uri,
                                 tagName);
                     }
                 }
 
                 public void handleException(final Throwable exception)
                 {
                     FaceletCorePlugin.log(
                             "While loading tag property meta-data", exception); //$NON-NLS-1$
                 }
             });
             return value[0];
         }
     }
 
     private static class InternalNamedNodeMap implements CMNamedNodeMap
     {
         private final List<CMNode> _nodes = new ArrayList<CMNode>();
 
         public void add(final CMNode node)
         {
             _nodes.add(node);
         }
 
         public int getLength()
         {
             return _nodes.size();
         }
 
         public CMNode getNamedItem(final String name)
         {
             for (final CMNode foundNode : _nodes)
             {
                 if (name.equals(foundNode.getNodeName()))
                 {
                     return foundNode;
                 }
             }
             return null;
         }
 
         public CMNode item(final int index)
         {
             if (index < _nodes.size())
             {
                 return _nodes.get(index);
             }
             return null;
         }
 
         public Iterator<?> iterator()
         {
             return Collections.unmodifiableList(_nodes).iterator();
         }
     }
 
     private final static NullInternalNamedNodeMap NULL_INSTANCE = new NullInternalNamedNodeMap();
 
     private static class NullInternalNamedNodeMap extends InternalNamedNodeMap
     {
 
         @Override
         public void add(final CMNode node)
         {
             // do nothing
         }
 
         @Override
         public int getLength()
         {
             // always empty
             return 0;
         }
 
         @Override
         public CMNode getNamedItem(final String name)
         {
             return null;
         }
 
         @Override
         public CMNode item(final int index)
         {
             return null;
         }
 
         @Override
         public Iterator<?> iterator()
         {
             return Collections.EMPTY_LIST.iterator();
         }
 
     }
 
     // temporary: transfer out to metadata
     final static Map<String, InternalNamedNodeMap> _faceletData;
 
     static
     {
         // final String ID_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_ID_DESCRIPTION;
         // final String BINDING_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_BINDING_DESCRIPTION;
         // final String TEMPLATE_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_TEMPLATE_DESCRIPTION;
         // final String HOTKEY_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_HOTKEY_DESCRIPTION;
         // final String DEFINE_NAME_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_DEFINE_NAME_DESCRIPTION;
         // final String SRC_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_SRC_DESCRIPTION;
         // final String INSERT_NAME_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_INSERT_NAME_DESCRIPTION;
         // final String PARAM_NAME_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_PARAM_NAME_DESCRIPTION;
         // final String PARAM_VALUE_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_PARAM_VALUE_DESCRIPTION;
         // final String REPEAT_VAR_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_REPEAT_VAR_DESCRIPTION;
         // final String REPEAT_VALUE_DESCRIPTION =
         // Messages.MDExternalMetadataStrategy_REPEAT_VALUE_DESCRIPTION;
 
         final Map<String, InternalNamedNodeMap> map = new HashMap<String, InternalNamedNodeMap>();
         // component
         // InternalNamedNodeMap nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(ID, CMAttributeDeclaration.OPTIONAL,
         // ID_DESCRIPTION));
         // nodeMap.add(createAttribute(BINDING, CMAttributeDeclaration.OPTIONAL,
         // BINDING_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_COMPONENT, nodeMap);
         //
         // // composition
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(TEMPLATE,
         // CMAttributeDeclaration.OPTIONAL,
         // TEMPLATE_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_COMPOSITION, nodeMap);
 
         // debug
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(HOTKEY, CMAttributeDeclaration.OPTIONAL,
         // HOTKEY_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_DEBUG, nodeMap);
 
         // decorate
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(TEMPLATE,
         // CMAttributeDeclaration.REQUIRED,
         // TEMPLATE_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_DECORATE, nodeMap);
 
         // define
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(NAME, CMAttributeDeclaration.REQUIRED,
         // DEFINE_NAME_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_DEFINE, nodeMap);
 
         // fragment
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(ID, CMAttributeDeclaration.OPTIONAL,
         // ID_DESCRIPTION));
         // nodeMap.add(createAttribute(BINDING, CMAttributeDeclaration.OPTIONAL,
         // BINDING_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_FRAGMENT, nodeMap);
 
         // include
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(SRC, CMAttributeDeclaration.REQUIRED,
         // SRC_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_INCLUDE, nodeMap);
 
         // insert
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(NAME, CMAttributeDeclaration.OPTIONAL,
         // INSERT_NAME_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_INSERT, nodeMap);
 
         // param
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(NAME, CMAttributeDeclaration.REQUIRED,
         // PARAM_NAME_DESCRIPTION));
         // nodeMap.add(createAttribute(VALUE, CMAttributeDeclaration.REQUIRED,
         // PARAM_VALUE_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_PARAM, nodeMap);
 
         // remove
         // nodeMap = new InternalNamedNodeMap();
         // // no attributes
         // map.put(IFaceletTagConstants.TAG_PARAM, nodeMap);
 
         // repeat
         // nodeMap = new InternalNamedNodeMap();
         // nodeMap.add(createAttribute(VALUE, CMAttributeDeclaration.REQUIRED,
         // REPEAT_VALUE_DESCRIPTION));
         // nodeMap.add(createAttribute(VAR, CMAttributeDeclaration.REQUIRED,
         // REPEAT_VAR_DESCRIPTION));
         // map.put(IFaceletTagConstants.TAG_REPEAT, nodeMap);
 
         _faceletData = Collections.unmodifiableMap(map);
     }
 
     private static CMAttributeDeclaration createAttribute(
             final AttributeData attributeData)
     {
         final AttributeCMAdapter attribute = new AttributeCMAdapter(
                 attributeData.getName(), convertUsageEnum(attributeData
                         .getUsage()));
 
         final ComposedAdapterFactory factory = new ComposedAdapterFactory(
                 ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
         final Object provider = factory.adapt(attributeData,
                 IResourceProvider.class);
 
         // get the description from meta-data and feed through the provider
         // for possible translation
         String description = attributeData.getDescription();
 
         if (provider instanceof IResourceProvider)
         {
             final IResourceProvider resProvider = (IResourceProvider) provider;
             final String translated = resProvider.getTranslatedString(
                     attributeData, AddTagMDPackage.eINSTANCE
                             .getAttributeData_Description());
             description = translated != null ? translated : description;
         }
 
         attribute.setDescription(description);
         return attribute;
     }
 
     private static int convertUsageEnum(final AttributeUsage usage)
     {
         switch (usage)
         {
             case OPTIONAL:
                 return CMAttributeDeclaration.OPTIONAL;
             case REQUIRED:
                 return CMAttributeDeclaration.REQUIRED;
             case FIXED:
                 return CMAttributeDeclaration.FIXED;
             case PROHIBITED:
                 return CMAttributeDeclaration.PROHIBITED;
             default:
                 return CMAttributeDeclaration.OPTIONAL;
         }
     }
 
     private static class MySingletonManager
             extends
             ResourceSingletonObjectManager<MDExternalMetadataStrategy, IProject>
     {
 
         @Override
         protected MDExternalMetadataStrategy createNewInstance(
                 final IProject resource)
         {
             return new MDExternalMetadataStrategy(resource);
         }
 
     }
 }

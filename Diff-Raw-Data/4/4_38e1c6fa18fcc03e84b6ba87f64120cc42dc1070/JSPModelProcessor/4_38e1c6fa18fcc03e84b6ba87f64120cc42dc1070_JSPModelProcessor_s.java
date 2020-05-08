 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.designtime.internal.jsp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.internal.resource.IResourceLifecycleListener;
 import org.eclipse.jst.jsf.common.internal.resource.LifecycleListener;
 import org.eclipse.jst.jsf.common.internal.resource.ResourceLifecycleEvent;
 import org.eclipse.jst.jsf.common.internal.resource.ResourceLifecycleEvent.EventType;
 import org.eclipse.jst.jsf.common.internal.resource.ResourceLifecycleEvent.ReasonType;
 import org.eclipse.jst.jsf.common.metadata.Trait;
 import org.eclipse.jst.jsf.common.metadata.internal.TraitValueHelper;
 import org.eclipse.jst.jsf.common.metadata.query.ITaglibDomainMetaDataModelContext;
 import org.eclipse.jst.jsf.common.metadata.query.TaglibDomainMetaDataQueryHelper;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.IStructuredDocumentContextResolverFactory;
 import org.eclipse.jst.jsf.context.resolver.structureddocument.ITaglibContextResolver;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContext;
 import org.eclipse.jst.jsf.context.structureddocument.IStructuredDocumentContextFactory;
 import org.eclipse.jst.jsf.context.symbol.IComponentSymbol;
 import org.eclipse.jst.jsf.context.symbol.ISymbol;
 import org.eclipse.jst.jsf.context.symbol.SymbolFactory;
 import org.eclipse.jst.jsf.context.symbol.source.AbstractContextSymbolFactory;
 import org.eclipse.jst.jsf.context.symbol.source.ISymbolConstants;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.designtime.DesignTimeApplicationManager;
 import org.eclipse.jst.jsf.designtime.context.DTFacesContext;
 import org.eclipse.jst.jsp.core.internal.domdocument.DOMModelForJSP;
 import org.eclipse.wst.sse.core.StructuredModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 /**
  * Processes a JSP model to determine information of interest about it such
  * as what tags are currently in use.  Listens to the model and updates it's 
  * information when the model changes.
  * 
  * @author cbateman
  *
  */
 public class JSPModelProcessor
 {
     private final static Map<IFile, JSPModelProcessor>  RESOURCE_MAP = 
         new HashMap<IFile, JSPModelProcessor>();
     private final static java.util.concurrent.locks.Lock CRITICAL_SECTION =
         new  ReentrantLock();
     private static LifecycleListener  LIFECYCLE_LISTENER;
 
     /**
      * @param file The file to get the model processor for  
      * @return the processor for a particular model, creating it if it does not
      *         already exist
      * @throws CoreException if an attempt to get the model associated with file
      *         fails due to reasons other than I/O problems
      */
     public static JSPModelProcessor get(IFile file) throws CoreException
     {
         CRITICAL_SECTION.lock();
         try
         {
             if (!file.isAccessible())
             {
                 throw new CoreException(new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID, "File must be accessible"));
             }
 
             JSPModelProcessor processor = RESOURCE_MAP.get(file);
 
             if (processor == null)
             {
                 if (LIFECYCLE_LISTENER == null)
                 {
                     LIFECYCLE_LISTENER = new LifecycleListener(file);
                 }
                 else
                 {
                     LIFECYCLE_LISTENER.addResource(file);
                 }
 
                 processor = new JSPModelProcessor(file,LIFECYCLE_LISTENER);
                 RESOURCE_MAP.put(file, processor);
             }
 
             return processor;
         }
         finally
         {
             CRITICAL_SECTION.unlock();
         }
     }
 
     /**
      * Disposes of the JSPModelProcessor associated with model
      * @param file the model processor to be disposed
      */
     private static void dispose(IFile file)
     {
         CRITICAL_SECTION.lock();
         try
         {
             JSPModelProcessor processor = RESOURCE_MAP.get(file);
 
             if (processor != null)
             {
                 RESOURCE_MAP.remove(file);
                 
                 if (!processor.isDisposed())
                 {
                     processor.dispose();
                     LIFECYCLE_LISTENER.removeResource(file);
                 }
                 
             }
 
             if (RESOURCE_MAP.size() == 0)
             {
                 // if we no longer have any resources being tracked,
                 // then dispose the lifecycle listener
                 LIFECYCLE_LISTENER.dispose();
                 LIFECYCLE_LISTENER = null;
             }
         }
         finally
         {
             CRITICAL_SECTION.unlock();
         }
     }
 
     private final IFile             _file;
     private LifecycleListener       _lifecycleListener;
     private IResourceLifecycleListener  _resListener;
     private boolean                 _isDisposed;
     private Map<Object, ISymbol>    _requestMap;
     private Map<Object, ISymbol>    _sessionMap;
     private Map<Object, ISymbol>    _applicationMap;
     private Map<Object, ISymbol>    _noneMap;
     private long                    _lastModificationStamp;
 
     // used to avoid infinite recursion in refresh.  Must never be null
     private final CountingMutex     _lastModificationStampMonitor = new CountingMutex();
 
     /**
      * Construct a new JSPModelProcessor for model
      * 
      * @param model
      */
     private JSPModelProcessor(final IFile  file, final LifecycleListener lifecycleListener)
     {
         //_model = getModelForFile(file);
         //_modelListener = new ModelListener();
         //_model.addModelLifecycleListener(_modelListener);
         _file = file;
         _lifecycleListener = lifecycleListener;
         _resListener = new IResourceLifecycleListener()
         {
             public EventResult acceptEvent(ResourceLifecycleEvent event)
             {
                 final EventResult result = EventResult.getDefaultEventResult();
 
                 // not interested
                 if (!_file.equals(event.getAffectedResource()))
                 {
                     return result; 
                 }
 
                 if (event.getEventType() == EventType.RESOURCE_INACCESSIBLE)
                 {
                     dispose(_file);
                 }
                 else if (event.getEventType() == EventType.RESOURCE_CHANGED)
                 {
                     // if the file has changed contents on disk, then
                     // invoke an unforced refresh of the JSP file
                     if (event.getReasonType() == ReasonType.RESOURCE_CHANGED_CONTENTS)
                     {
                         refresh(false);
                     }
                 }
 
                 return result;
             }
         };
 
         lifecycleListener.addListener(_resListener);
         
         // a negative value guarantees that refresh(false) will
         // force a refresh on the first run
         _lastModificationStamp = -1;
     }
 
     private DOMModelForJSP getModelForFile(final IFile file)
             throws CoreException, IOException
     {
         final IModelManager modelManager = 
             StructuredModelManager.getModelManager();
 
         IStructuredModel model = modelManager.getModelForRead(file);
 
         if (model instanceof DOMModelForJSP)
         {
             return (DOMModelForJSP) model;
         }
         else if (model != null)
         {
             // only release from read if we don't find a DOMModelForJSP
             // if the model is correct, it will be released in dispose
             model.releaseFromRead();
         }
         
         throw new CoreException
             (new Status(IStatus.ERROR
                         , "org.eclipse.blah"
                         , 0  //$NON-NLS-1$
                         ,"model not of expected type"
                         , new Throwable())); //$NON-NLS-1$
     }
 
     private void dispose()
     {
         if (!_isDisposed)
         {
             // ensure the resource listener is disposed
             _lifecycleListener.removeListener(_resListener);
             _resListener = null;
             _lifecycleListener = null;
 
             if (_requestMap != null)
             {
                 _requestMap.clear();
                 _requestMap = null;
             }
 
             if (_sessionMap != null)
             {
                 _sessionMap.clear();
                 _sessionMap = null;
             }
 
             if (_applicationMap != null)
             {
                 _applicationMap.clear();
                 _applicationMap = null;
             }
 
             if (_noneMap != null)
             {
                 _noneMap.clear();
                 _noneMap = null;
             }
 
             // mark as disposed
             _isDisposed = true;
         }
     }
 
     /**
      * @return true if this model processor has been disposed.  Disposed
      * processors should not be used.
      */
     public boolean isDisposed()
     {
         return _isDisposed;
     }
 
     /**
      * If isModelDirty() returns true, then it means that a call
      * to refresh(false) will trigger a reprocess of the underlying document.
      *
      * @return true if the underlying JSP model is considered to be dirty
      */
     public boolean isModelDirty()
     {
         final long currentModificationStamp = _file.getModificationStamp();
         return _lastModificationStamp != currentModificationStamp;
     }
     
     /**
      * Updates the internal model
      * @param forceRefresh -- if true, always refreshes, if false,
      * then it only refreshes if the file's modification has changed
      * since the last refresh
      * @throws IllegalStateException if isDisposed() == true
      */
     public void refresh(final boolean forceRefresh)
     {
         if (isDisposed())
         {
             throw new IllegalStateException("Processor is disposed for file: "+_file.toString());
         }
 
         synchronized(_lastModificationStampMonitor)
         {
             if (_lastModificationStampMonitor.isSignalled())
             {
                 // if this calls succeeds, then this thread has obtained the
                 // lock already and has called through here before.  
                 // return immediately to ensure that we don't recurse infinitely
                 return;
             }
 
             DOMModelForJSP  model = null;
             try
             {
                 _lastModificationStampMonitor.setSignalled(true);
                 
 
                 // only refresh if forced or if the underlying file has changed
                 // since the last run
                 if (forceRefresh
                         || isModelDirty())
                 {
                     model = getModelForFile(_file);
                     refreshInternal(model);
                     _lastModificationStamp = _file.getModificationStamp();
                 }
             }
             catch (CoreException e) {
                JSFCorePlugin.log(new RuntimeException(e), "Error refreshing internal model");
             } catch (IOException e) {
                 JSFCorePlugin.log(new RuntimeException(e), "Error refreshing internal model");
             }
             // make sure that we unsignal the monitor before releasing the
             // mutex
             finally
             {
                 _lastModificationStampMonitor.setSignalled(false);
             }
         }
     }
     
     private void refreshInternal(DOMModelForJSP model)
     {
         final IStructuredDocumentContext context = 
             IStructuredDocumentContextFactory.INSTANCE.getContext(model.getStructuredDocument(), -1);
         final ITaglibContextResolver taglibResolver =
             IStructuredDocumentContextResolverFactory.INSTANCE.getTaglibContextResolver(context);
         IDOMDocument document = model.getDocument();
         getApplicationMap().clear();
         getRequestMap().clear();
         getSessionMap().clear();
         //long curTime = System.currentTimeMillis();
         recurseChildNodes(model, document.getChildNodes(), taglibResolver);
         //long netTime = System.currentTimeMillis() - curTime;
         //System.out.println("Net time to recurse document: "+netTime);
     }
 
     private void recurseChildNodes(final DOMModelForJSP model,
                                    final NodeList nodes, 
                                     final ITaglibContextResolver taglibResolver)
     {
         for (int i = 0; i < nodes.getLength(); i++)
         {
             final Node child = nodes.item(i);
             
             // process attributes at this node before recursing
             processAttributes(model, child, taglibResolver);
             recurseChildNodes(model, child.getChildNodes(), taglibResolver);
         }
     }
 
     private void processAttributes(final DOMModelForJSP model, final Node node, 
                                     final ITaglibContextResolver taglibResolver)
     {
         if (taglibResolver.hasTag(node))
         {
             final String uri =
                 taglibResolver.getTagURIForNodeName(node);
             final String elementName = node.getLocalName();
             
             for (int i = 0; i < node.getAttributes().getLength(); i++)
             {
                 final Node attribute = node.getAttributes().item(i);
 
                 processSymbolContrib(model, uri, elementName, attribute);
                 processSetsLocale(uri, elementName, attribute);
             }
         }
     }
 
     private void processSymbolContrib(final DOMModelForJSP model, final String uri, final String elementName, Node attribute)
     {
         final SymbolContribAggregator  aggregator =
             SymbolContribAggregator.
                create(_file.getProject(), uri, elementName, attribute.getLocalName());
   
         if (aggregator != null)
         {    
             final AbstractContextSymbolFactory factory = aggregator.getFactory();
             final String symbolName = attribute.getNodeValue();
 
             if (factory != null)
             {
 //                long curTime = System.currentTimeMillis();
                  
                 final List problems = new ArrayList();
                 ISymbol symbol =
                     factory.create(symbolName, 
                                   ISymbolConstants.SYMBOL_SCOPE_REQUEST, //TODO:
                                   IStructuredDocumentContextFactory.INSTANCE.
                                       getContext(model.getStructuredDocument(), 
                                                  attribute),
                                   problems);
 
 //                long netTime = System.currentTimeMillis() - curTime;
 //                System.out.println("Time to process loadBundle: "+netTime);
 
                 if (symbol != null)
                 {
                     updateMap(symbol, aggregator.getScope());
                 }
             }
             else
             {
                 IComponentSymbol componentSymbol = 
                     SymbolFactory.eINSTANCE.createIComponentSymbol();
                 componentSymbol.setName(symbolName);
 
                 updateMap(componentSymbol, aggregator.getScope());
             }
         }
     }
     
     private void processSetsLocale(final String uri, final String elementName, Node attribute)
     {
         LocaleSetAggregator  aggregator = LocaleSetAggregator.create(_file.getProject(), uri, elementName, attribute.getLocalName());
 
         if (aggregator != null)
         {
             DesignTimeApplicationManager  dtAppMgr =
                 DesignTimeApplicationManager.getInstance(_file.getProject());
 
             DTFacesContext facesContext = dtAppMgr.getFacesContext(_file);
             
             if (facesContext != null)
             {
                 facesContext.setLocaleString(attribute.getNodeValue());
             }
         }
     }
 
    /**
      * @param scopeName - one of "request", "session" or "application"
      * @return an unmodifable map containing all known symbols for
      * that scope.  If scopeName is not found, returns the empty map.
      */
     public Map<Object, ISymbol> getMapForScope(String scopeName)
     {
         final Map<Object, ISymbol> map = getMapForScopeInternal(scopeName);
         
         if (map != null)
         {
             return Collections.unmodifiableMap(map);
         }
 
         return Collections.EMPTY_MAP;
     }
 
     private void updateMap(ISymbol symbol, String  scopeName)
     {
         final Map<Object, ISymbol> map = getMapForScopeInternal(scopeName);
 
         if (map != null)
         {
             map.put(symbol.getName(), symbol);
         }
         else
         {
             Platform.getLog(JSFCorePlugin.getDefault().getBundle()).log(new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID, 0, "Scope not found: "+scopeName, new Throwable())); //$NON-NLS-1$
         }
     }
 
     private Map<Object, ISymbol> getMapForScopeInternal(String scopeName)
     {
         if (ISymbolConstants.SYMBOL_SCOPE_REQUEST_STRING.equals(scopeName))
         {
             return getRequestMap();
         }
         else if (ISymbolConstants.SYMBOL_SCOPE_SESSION_STRING.equals(scopeName))
         {
             return getSessionMap();
         }
         else if (ISymbolConstants.SYMBOL_SCOPE_APPLICATION_STRING.equals(scopeName))
         {
             return getApplicationMap();
         }
         else if (ISymbolConstants.SYMBOL_SCOPE_NONE_STRING.equals(scopeName))
         {
             return getNoneMap();
         }
         
         Platform.getLog(JSFCorePlugin.getDefault().getBundle()).log(new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID, 0, "Scope not found: "+scopeName, new Throwable())); //$NON-NLS-1$
         return null;
     
     }
     
     private Map getRequestMap()
     {
         if (_requestMap == null)
         {
             _requestMap = new HashMap<Object, ISymbol>();
         }
         
         return _requestMap;
     }
     
     private Map<Object, ISymbol> getSessionMap()
     {
         if (_sessionMap == null)
         {
             _sessionMap = new HashMap<Object, ISymbol>();
         }
         
         return _sessionMap;
     }
     
     private Map<Object, ISymbol> getApplicationMap()
     {
         if (_applicationMap == null)
         {
             _applicationMap = new HashMap<Object, ISymbol>();
         }
         
         return _applicationMap;
     }
     
     private Map<Object, ISymbol> getNoneMap()
     {
         if (_noneMap == null)
         {
             _noneMap = new HashMap<Object, ISymbol>();
         }
         
         return _noneMap;
     }
 
     /**
      * Aggregates the sets-locale meta-data
      * 
      * @author cbateman
      */
     private static class LocaleSetAggregator
     {
         private final static String SETS_LOCALE = "sets-locale"; //$NON-NLS-1$
         
         static LocaleSetAggregator create(IProject project, 
                                               final String uri, 
                                               final String elementName, final String attributeName)
         {
             final ITaglibDomainMetaDataModelContext mdContext = TaglibDomainMetaDataQueryHelper.createMetaDataModelContext(project, uri);
             Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(mdContext, elementName+"/"+attributeName, SETS_LOCALE); //$NON-NLS-1$
 
             if (TraitValueHelper.getValueAsBoolean(trait))
             {
                 return new LocaleSetAggregator();
             }
 
             return null;
         }
     }
     
     /**
      * Aggregates all the symbol contributor meta-data into a single object
      * 
      * @author cbateman
      *
      */
     private static class SymbolContribAggregator
     {
         private final static String CONTRIBUTES_VALUE_BINDING = 
             "contributes-value-binding"; //$NON-NLS-1$
         private final static String VALUE_BINDING_SCOPE = "value-binding-scope"; //$NON-NLS-1$
         private final static String VALUE_BINDING_SYMBOL_FACTORY = 
             "value-binding-symbol-factory"; //$NON-NLS-1$
 
         /**
          * @param attributeName
          * @return a new instance only if attributeName is a symbol contributor
          */
         static SymbolContribAggregator create(final IProject project, 
                                               final String uri, 
                                               final String elementName, 
                                               final String attributeName)
         {
             final String entityKey = elementName+"/"+attributeName; //$NON-NLS-1$
             final ITaglibDomainMetaDataModelContext mdContext = TaglibDomainMetaDataQueryHelper.createMetaDataModelContext(project, uri);
             Trait trait = TaglibDomainMetaDataQueryHelper.getTrait(mdContext, entityKey, CONTRIBUTES_VALUE_BINDING);
 
             boolean contribsValueBindings = TraitValueHelper.getValueAsBoolean(trait);
 
             if (contribsValueBindings)
             {
                 String scope = null;
                 String symbolFactory = null;
                 
                 trait = TaglibDomainMetaDataQueryHelper.getTrait(mdContext, entityKey, VALUE_BINDING_SCOPE);
                 scope = TraitValueHelper.getValueAsString(trait);
 
                 if (scope != null && !scope.equals("")) //$NON-NLS-1$
                 {
                     trait = TaglibDomainMetaDataQueryHelper.getTrait(mdContext, entityKey, VALUE_BINDING_SYMBOL_FACTORY);
                     symbolFactory = TraitValueHelper.getValueAsString(trait);                      
                 }
 
                 return new SymbolContribAggregator(scope, symbolFactory);
             }
 
             return null;
         }
 
         private final Map   _metadata = new HashMap(4);
 
         SymbolContribAggregator(final String scope, final String factory)
         {
             _metadata.put("scope", scope); //$NON-NLS-1$
             _metadata.put("factory", factory); //$NON-NLS-1$
         }
 
         /**
          * @return the scope
          */
         public String getScope()
         {
             return (String) _metadata.get("scope"); //$NON-NLS-1$
         }
         
         /**
          * @return the factory
          */
         public AbstractContextSymbolFactory getFactory()
         {
             return JSFCommonPlugin.getSymbolFactories().get(_metadata.get("factory")); //$NON-NLS-1$
         }
     }
     
     private static class CountingMutex extends Object
     {
         private boolean _signalled = false;
 
         /**
          * @return true if the state of mutex is signalled
          */
         public synchronized boolean isSignalled() {
             return _signalled;
         }
 
         /**
          * @param signalled
          */
         public synchronized void setSignalled(boolean signalled) {
             this._signalled = signalled;
         }
     }
 }

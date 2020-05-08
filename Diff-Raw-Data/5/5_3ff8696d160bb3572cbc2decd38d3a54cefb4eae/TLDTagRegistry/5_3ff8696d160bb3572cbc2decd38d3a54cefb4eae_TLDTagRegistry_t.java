 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.designtime.internal.view.model.jsp.registry;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jst.jsf.common.internal.managedobject.IManagedObject;
 import org.eclipse.jst.jsf.common.internal.policy.IdentifierOrderedIteratorPolicy;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.Namespace;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.JSFCoreTraceOptions;
 import org.eclipse.jst.jsf.designtime.internal.Messages;
 import org.eclipse.jst.jsf.designtime.internal.view.model.AbstractTagRegistry;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.CompositeTagResolvingStrategy;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.DefaultJSPTagResolver;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TLDNamespace;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.TagIntrospectingStrategy;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.UnresolvedJSPTagResolvingStrategy;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.persistence.PersistedDataTagStrategy;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.CMDocumentFactoryTLD;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDDocument;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDElementDeclaration;
 import org.eclipse.jst.jsp.core.taglib.ITLDRecord;
 import org.eclipse.jst.jsp.core.taglib.ITaglibRecord;
 import org.eclipse.jst.jsp.core.taglib.TaglibIndex;
 
 /**
  * Registry of all tld-defined tags for a particular project classpath
  * 
  * @author cbateman
  * 
  */
 public final class TLDTagRegistry extends AbstractTagRegistry implements
         IManagedObject
 {
     // INSTANCE
     private final IProject                                             _project;
     private final Map<String, TLDNamespace>                            _nsResolved;
     private final CompositeTagResolvingStrategy<TLDElementDeclaration> _resolver;
     private boolean                                                    _hasBeenInitialized = false;
     private final ConcurrentLinkedQueue<LibraryOperation>              _changeOperations   = new ConcurrentLinkedQueue<LibraryOperation>();
     private final Job                                                  _changeJob;
     private final PersistedDataTagStrategy                             _persistedTagStrategy;
     private TagIndexListener                                           _tagIndexListener;
     private final TLDRegistryPreferences                               _prefs;
     private final MyPropertyListener                                   _myPropertyListener;
 
     TLDTagRegistry(final IProject project)
     {
         _project = project;
         _nsResolved = new HashMap<String, TLDNamespace>();
 
         _prefs = new TLDRegistryPreferences(JSFCorePlugin.getDefault().getPreferenceStore());
         _myPropertyListener = new MyPropertyListener();
         _prefs.addListener(_myPropertyListener);
         _prefs.load();
         final IdentifierOrderedIteratorPolicy<String> policy =
             getTagResolvingPolicy();
 
         _resolver = new CompositeTagResolvingStrategy<TLDElementDeclaration>(
                 policy);
 
         // add the strategies
         _resolver.addStrategy(new TagIntrospectingStrategy(_project));
         _resolver.addStrategy(new DefaultJSPTagResolver(_project));
         // makes sure that a tag element will always be created for any
         // given tag definition even if other methods fail
         _resolver.addStrategy(new UnresolvedJSPTagResolvingStrategy());
         _persistedTagStrategy = new PersistedDataTagStrategy(_project);
         _persistedTagStrategy.init();
         _resolver.addStrategy(_persistedTagStrategy);
 
         _changeJob = new ChangeJob(project.getName());
     }
 
     private IdentifierOrderedIteratorPolicy<String>  getTagResolvingPolicy()
     {
         // strategy ordering
         final List<String>  prefOrdering = _prefs.getEnabledIds();
         final List<String> strategyOrdering = new ArrayList<String>(prefOrdering);
         
         // this strategy must always be here, always last
         strategyOrdering.add(UnresolvedJSPTagResolvingStrategy.ID);
 
         final IdentifierOrderedIteratorPolicy<String> policy = new IdentifierOrderedIteratorPolicy<String>(
                 strategyOrdering);
         // exclude things that are not explicitly listed in the policy.  That
         // way preference-based disablement will cause those strategies to
         // be excluded.
         policy.setExcludeNonExplicitValues(true);
         return  policy;
     }
 
     protected final void doDispose()
     {
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions.log("TLDTagRegistry: Disposing for project " //$NON-NLS-1$
                     + _project.toString());
         }
 
         // call checkpoint to flush serializable data
         checkpoint();
         //_persistedTagStrategy.dispose();

        _prefs.removeListener(_myPropertyListener);
        _prefs.dispose();

         _nsResolved.clear();
         _changeOperations.clear();
 
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions
                     .log("TLDTagRegistry: Done disposing registry for " //$NON-NLS-1$
                             + _project.toString());
         }
     }
 
     @Override
     protected void cleanupPersistentState()
     {
        // TODO
     }
 
     public synchronized void checkpoint()
     {
         try
         {
             _persistedTagStrategy.save(_nsResolved);
         }
         catch (IOException e)
         {
            JSFCorePlugin.log(e, "Checkpointing JSP tags failed"); //$NON-NLS-1$
         }
         catch (ClassNotFoundException e)
         {
             JSFCorePlugin.log(e, "Checkpointing JSP tags failed"); //$NON-NLS-1$
         }
     }
 
     @Override
     protected Job getRefreshJob(final boolean flushCaches)
     {
         return new Job(Messages.TLDTagRegistry_RefreshJob + _project.getName())
         {
             @Override
             protected IStatus run(final IProgressMonitor monitor)
             {
                 if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
                 {
                     JSFCoreTraceOptions.log("TLDTagRegistry.refresh: start"); //$NON-NLS-1$
                 }
 
                 synchronized (TLDTagRegistry.this)
                 {
                     if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
                     {
                         JSFCoreTraceOptions
                                 .log("TLDTagRegistry.refresh: start"); //$NON-NLS-1$
                     }
 
                     final List<Namespace> namespaces = new ArrayList(
                             _nsResolved.values());
 
                     if (flushCaches)
                     {
                         _persistedTagStrategy.clear();
                     }
                     // if we aren't flushing caches, then check point the
                     // current namespace data, so it isn't lost when we clear
                     // the namespaces
                     else
                     {
                         checkpoint();
                     }
 
                     _nsResolved.clear();
 
                     fireEvent(new TagRegistryChangeEvent(TLDTagRegistry.this,
                             TagRegistryChangeEvent.EventType.REMOVED_NAMESPACE,
                             namespaces));
                     initialize(true);
 
                     if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
                     {
                         JSFCoreTraceOptions
                                 .log("TLDTagRegistry.refresh: finished"); //$NON-NLS-1$
                     }
                     return Status.OK_STATUS;
                 }
             }
         };
     }
 
     /**
      */
     private void initialize(boolean fireEvent)
     {
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions.log("TLDTagRegistry.initialize: start"); //$NON-NLS-1$
         }
 
         final ITaglibRecord[] tldrecs = TaglibIndex
                 .getAvailableTaglibRecords(_project.getFullPath());
         final List<Namespace> affectedObjects = new ArrayList<Namespace>();
         for (final ITaglibRecord tldrec : tldrecs)
         {
             // defer the event
             final Namespace ns = initialize(tldrec, fireEvent);
 
             if (ns != null)
             {
                 affectedObjects.add(ns);
             }
         }
 
         _hasBeenInitialized = true;
 
         // if tag index listener does exist, add it
         if (_tagIndexListener == null)
         {
             if (JSFCoreTraceOptions.TRACE_TLDREGISTRYMANAGER)
             {
                 JSFCoreTraceOptions
                         .log("TLDRegistryManager: installing tag index listener due to create instance for " //$NON-NLS-1$
                                 + _project.toString());
             }
 
             _tagIndexListener = new TagIndexListener(this);
             TaglibIndex.addTaglibIndexListener(_tagIndexListener);
         }
 
         // if (affectedObjects.size() > 0)
         // {
         // fireEvent(new TagRegistryChangeEvent(this,
         // TagRegistryChangeEvent.EventType.ADDED_NAMESPACE,
         // affectedObjects));
         // }
 
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions.log("TLDTagRegistry.initialize: finished"); //$NON-NLS-1$
         }
     }
 
     TLDNamespace initialize(final ITaglibRecord tagRecord,
             final boolean fireEvent)
     {
         if (tagRecord.getRecordType() == ITLDRecord.URL)
         {
             if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY_CHANGES)
             {
                 JSFCoreTraceOptions.log("TLDTagRegistry.initialize_TagRecord: Initializing new tld record: "+tagRecord.toString()); //$NON-NLS-1$
             }
             long startTime = 0;
             
             if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY_PERF)
             {
                 startTime = System.nanoTime();
             }
             
             final CMDocumentFactoryTLD factory = new CMDocumentFactoryTLD();
             final TLDDocument doc = (TLDDocument) factory
                     .createCMDocument(tagRecord);
             if (doc != null)
             {
                 final TLDNamespace ns = new TLDNamespace(doc, _resolver);
                 _nsResolved.put(doc.getUri(), ns);
     
                 if (fireEvent)
                 {
                     fireEvent(new TagRegistryChangeEvent(this,
                             TagRegistryChangeEvent.EventType.ADDED_NAMESPACE,
                             Collections.singletonList(ns)));
                 }
 
                 if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY_PERF)
                 {
                     System.out.printf("Time to update namespace %s was %d\n", //$NON-NLS-1$
                             ns.getNSUri(), Long.valueOf(System.nanoTime()
                                     - startTime));
                 }
                 return ns;
             }
         }
         else 
         {
             if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY_CHANGES)
             {
                 JSFCoreTraceOptions.log("TLDTagRegistry.initialize_TagRecord: Skipping tag record for "+tagRecord.toString()); //$NON-NLS-1$
             }
 
         }
         // no new namespace
         return null;
     }
 
     void remove(final ITaglibRecord tagRecord)
     {
         // this is safer, since we likely fail to create a TLDDocument for
         // a tagRecord that has been removed.
         final String uri = tagRecord.getDescriptor().getURI();
         final TLDNamespace ns = _nsResolved.remove(uri);
 
         if (ns != null)
         {
             fireEvent(new TagRegistryChangeEvent(this,
                     TagRegistryChangeEvent.EventType.REMOVED_NAMESPACE,
                     Collections.singletonList(ns)));
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jst.jsf.designtime.internal.view.model.jsp.registry.ITagRegistry#getAllTagLibraries()
      */
     public final synchronized Collection<? extends Namespace> getAllTagLibraries()
     {
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions.log("TLDTagRegistry.getAllTagLibraries: start"); //$NON-NLS-1$
         }
         long startTime = 0;
         
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY_PERF)
         {
             startTime = System.nanoTime();
         }
         
         if (!_hasBeenInitialized)
         {
             initialize(false);
         }
 
         final Set<TLDNamespace> allTagLibraries = new HashSet<TLDNamespace>();
         allTagLibraries.addAll(_nsResolved.values());
 
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY_PERF)
         {
             System.out.println("Time to getAllTagLibraries for JSP: "+(System.nanoTime()-startTime)); //$NON-NLS-1$
         }
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions
                     .log("TLDTagRegistry.getAllTagLibraries: finished"); //$NON-NLS-1$
         }
         return allTagLibraries;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jst.jsf.designtime.internal.view.model.jsp.registry.ITagRegistry#getTagLibrary(java.lang.String)
      */
     public final synchronized Namespace getTagLibrary(final String uri)
     {
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions.log("TLDTagRegistry.getTagLibrary: start uri=" //$NON-NLS-1$
                     + uri);
         }
 
         if (!_hasBeenInitialized)
         {
             initialize(false);
         }
 
         final Namespace ns = _nsResolved.get(uri);
 
         if (JSFCoreTraceOptions.TRACE_JSPTAGREGISTRY)
         {
             JSFCoreTraceOptions
                     .log("TLDTagRegistry.getTagLibrary: finished, result=" //$NON-NLS-1$
                             + ns.toString());
         }
         return ns;
     }
 
     @Override
     public String toString()
     {
         return String
                 .format(
                         "TLDRegistry for project %s, isDisposed=%s, hasBeenInitialized=%s, numberOfNamespace=%d", //$NON-NLS-1$
                         _project.toString(), Boolean.valueOf(isDisposed()),
                         Boolean.valueOf(_hasBeenInitialized), Integer
                                 .valueOf(_nsResolved.size()));
     }
 
     void addLibraryOperation(final LibraryOperation operation)
     {
         _changeOperations.add(operation);
         _changeJob.schedule();
     }
 
 
     private class ChangeJob extends Job
     {
         private int _rescheduleTime = -1;
 
         public ChangeJob(final String projectName)
         {
             super(Messages.TLDTagRegistry_UpdateJob + projectName); 
         }
 
         @Override
         protected IStatus run(final IProgressMonitor monitor)
         {
             synchronized (TLDTagRegistry.this)
             {
                 _rescheduleTime = -1;
 
                 LibraryOperation operation = null;
                 final MultiStatus multiStatus = new MultiStatus(
                         JSFCorePlugin.PLUGIN_ID, 0, "Result of change job", //$NON-NLS-1$
                         new Throwable());
                 while ((operation = _changeOperations.poll()) != null)
                 {
                     _rescheduleTime = 10000; // ms
 
                     operation.run();
                     multiStatus.add(operation.getResult());
                 }
 
                 if (_rescheduleTime >= 0 && !monitor.isCanceled())
                 {
                     // if any operations were found on this run, reschedule
                     // to run again in 10seconds based on the assumption that
                     // events may be coming in bursts
                     schedule(_rescheduleTime);
                 }
 
                 return multiStatus;
             }
         }
     }
     
     private class MyPropertyListener extends TLDRegistryPreferences.PropertyListener
     {
         @Override
         public void strategyOrderChanged()
         {
             synchronized(TLDTagRegistry.this)
             {
                 _prefs.load();
                 final IdentifierOrderedIteratorPolicy<String> policy =
                     getTagResolvingPolicy();
                 _resolver.setPolicy(policy);
             }
         }
     }
 }

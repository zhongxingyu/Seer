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
 package org.eclipse.jst.jsf.facelet.core.internal.registry.taglib;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.jst.jsf.common.internal.locator.ILocatorProvider;
 import org.eclipse.jst.jsf.core.internal.tld.IFaceletConstants;
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCorePlugin;
 
 /**
  * Taglib descriptor for a project.
  * 
  * @author cbateman
  *
  */
 public class ProjectTaglibDescriptor implements IProjectTaglibDescriptor
 {
     private final AtomicInteger                      _isInitialized = new AtomicInteger(
                                                                             0);
     private final IProject                           _project;
     private final ILocatorProvider<AbstractFaceletTaglibLocator>      _locatorProvider;
     private final MyChangeListener                   _libChangeListener;
     private final Map<String, IFaceletTagRecord>     _tagRecords;
     private final TagRecordFactory _factory;
     private final AtomicBoolean     _isDisposed = new AtomicBoolean(false);
     private final DefaultStandardTaglibLocator _defaultTaglibLocator;
 
     /**
      * @param project
      * @param factory 
      * @param locatorProvider 
      */
     public ProjectTaglibDescriptor(final IProject project, final TagRecordFactory factory, 
             final ILocatorProvider<AbstractFaceletTaglibLocator> locatorProvider)
     {
         _project = project;
         _tagRecords = new HashMap<String, IFaceletTagRecord>();
         _locatorProvider = locatorProvider;
         _locatorProvider.initialize();
         _factory = factory; 
         _libChangeListener = new MyChangeListener();
         _defaultTaglibLocator = new DefaultStandardTaglibLocator();
         _defaultTaglibLocator.start(project);
     }
 
     private void initialize()
     {
         if (_isInitialized.addAndGet(1) == 1)
         {
             synchronized (this)
             {
                 for (final AbstractFaceletTaglibLocator locator : _locatorProvider.getLocators())
                 {
                     SafeRunner.run(new ISafeRunnable()
                     {
                         public void handleException(final Throwable exception)
                         {
                             FaceletCorePlugin
                                     .log(
                                             "While locating facelet libraries on project: " + _project.getName(), new Exception(exception)); //$NON-NLS-1$
                         }
                         public void run() throws Exception
                         {
                             locator.addListener(_libChangeListener);
                             locator.start(_project);
                             _tagRecords.putAll(locator.locate(_project));
                         }
                     });
                 }
                 // ensure that we add the standard tag libraries if we don't find them 
                 // on the classpath.  The spec doesn't require that taglib's are
                 // included in a JSF impl for these
                 ensureStandardLibraries(_project);
             }
         }
     }
 
     private void ensureStandardLibraries(final IProject project)
     {
         final Map<String, ? extends IFaceletTagRecord>  defaultRecords = _defaultTaglibLocator.locate(project);
         for (final String uri : IFaceletConstants.ALL_FACELET_TAGLIBS)
         {
             if (!_tagRecords.containsKey(uri))
             {
                 IFaceletTagRecord faceletTagRecord = defaultRecords.get(uri);
                 if (faceletTagRecord != null)
                 {
                     _tagRecords.put(uri, faceletTagRecord);
                 }
                 else
                 {
                     FaceletCorePlugin.log("Could not find taglib for uri: "+uri, new Exception()); //$NON-NLS-1$
                 }
             }
         }
     }
 
     public Collection<? extends IFaceletTagRecord> getTagLibraries()
     {
         initialize();
         return Collections.unmodifiableCollection(_tagRecords.values());
     }
 
     void maybeLog(final Exception e)
     {
         if (_isInitialized.get() <= 1)
         {
             FaceletCorePlugin.log("Failed initializing taglib descriptor", e); //$NON-NLS-1$
         }
     }
 
     public IFaceletTagRecord getTagLibrary(final String uri)
     {
         initialize();
         return _tagRecords.get(uri);
     }
 
     public void addListener(final Listener listener)
     {
         for (final AbstractFaceletTaglibLocator locator : _locatorProvider.getLocators())
         {
             locator.addListener(listener);
         }
     }
 
     public void removeListener(final Listener listener)
     {
         for (final AbstractFaceletTaglibLocator locator : _locatorProvider.getLocators())
         {
             locator.removeListener(listener);
         }
     }
 
     public void checkpoint()
     {
         // do nothing
     }
 
     public void destroy()
     {
         // call dispose;  there is no persistent data to cleanup.
         dispose();
     }
 
     public void dispose()
     {
         if (_isDisposed.compareAndSet(false, true))
         {
             for (final AbstractFaceletTaglibLocator locator : _locatorProvider.getLocators())
             {
                try
                {
                    locator.stop();
                } catch (Exception e)
                {
                    FaceletCorePlugin.log("Disposing ProjectTaglibDescriptor", e); //$NON-NLS-1$
                }
             }
             _factory.dispose();
         }
     }
 
     public boolean isDisposed()
     {
         return _isDisposed.get();
     }
 
     private class MyChangeListener extends Listener
     {
         @Override
         public void changed(final TaglibChangedEvent event)
         {
             switch (event.getChangeType())
             {
                 case ADDED:
                     _tagRecords.put(event.getNewValue().getURI(), event
                             .getNewValue());
                 break;
                 case CHANGED:
                     _tagRecords.put(event.getNewValue().getURI(), event
                             .getNewValue());
                 break;
                 case REMOVED:
                     _tagRecords.remove(event.getOldValue());
                 break;
             }
         }
     }
 }

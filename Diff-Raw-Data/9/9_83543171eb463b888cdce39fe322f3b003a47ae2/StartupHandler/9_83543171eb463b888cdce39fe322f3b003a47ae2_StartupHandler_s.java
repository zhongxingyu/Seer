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
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jst.jsf.common.internal.JSPUtil;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.jsfappconfig.JSFAppConfigUtils;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * On workbench startup, registers a part listener that triggers when
  * a JSP editor opens.
  *
  * @author cbateman
  *
  */
 public class StartupHandler implements IStartup
 {
     private final JSPEditorListener    _partListener = new JSPEditorListener();
     private final static boolean    DISABLE_EDITOR_OPEN_REFRESH = System
                                                                         .getProperty("org.eclipse.jst.jsf.jspmodelprocessor.disable.editor.open.refresh") != null; //$NON-NLS-1$
 
     public void earlyStartup()
     {
         PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
         {
             public void run()
             {
                 final IWorkbenchWindow  windows[] =
                     PlatformUI.getWorkbench().getWorkbenchWindows();
 
                 for (final IWorkbenchWindow window : windows)
                 {
                     final IWorkbenchPage pages[] = window.getPages();
                     for (final IWorkbenchPage page : pages)
                     {
                         final IEditorReference[]  editorReferences =
                             page.getEditorReferences();
 
                         for (final IEditorReference editorReference : editorReferences)
                         {
                             if (_partListener.isValidJSPEditor(editorReference))
                             {
                                 _partListener.setJSPModelListener(editorReference);
                             }
                         }
                     }
                     window.getPartService().addPartListener(_partListener);
                 }
 
                 // TODO: register with all windows?
                 PlatformUI.getWorkbench().addWindowListener(new IWindowListener()
                 {
                     public void windowActivated(final IWorkbenchWindow window) {
                         // do nothing
                     }
 
                     public void windowDeactivated(final IWorkbenchWindow window) {
                         // do nothing
                     }
 
                     public void windowClosed(final IWorkbenchWindow window) {
                         window.getPartService().removePartListener(_partListener);
                     }
 
                     public void windowOpened(final IWorkbenchWindow window) {
                         window.getPartService().addPartListener(_partListener);
                     }
                 });
             }
         });
     }
 
     private static class JSPEditorListener implements IPartListener2
     {
         private JSPModelProcessor         _processor;
 
         public void partActivated(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         public void partBroughtToTop(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         public void partClosed(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         public void partDeactivated(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         public void partOpened(final IWorkbenchPartReference partRef) {
             if (isValidJSPEditor(partRef))
             {
                 setJSPModelListener((IEditorReference)partRef);
             }
         }
 
         public void partHidden(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         public void partVisible(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         public void partInputChanged(final IWorkbenchPartReference partRef) {
             // do nothing
         }
 
         private boolean isJSPEditor(final IEditorReference editorRef)
         {
             final IFile file = getIFile(editorRef);
 
             if (file != null)
             {
                 return JSPUtil.isJSPContentType(file);
             }
 
             return false;
         }
 
         /**
          * @param editorRef
          * @return true if the editor is editing the JSP content type and
          * the owning project is a JSF project
          */
         boolean isValidJSPEditor(final IEditorReference editorRef)
         {
             final IFile file = getIFile(editorRef);
 
             return file != null &&
                     JSFAppConfigUtils.isValidJSFProject(file.getProject()) &&
                         isJSPEditor(editorRef);
         }
 
         boolean isValidJSPEditor(final IWorkbenchPartReference partRef)
         {
             if (partRef instanceof IEditorReference)
             {
                 return isValidJSPEditor((IEditorReference)partRef);
             }
 
             return false;
         }
 
         void setJSPModelListener(final IEditorReference editorRef)
         {
             final IFile file = getIFile(editorRef);
 
            if (file != null)
             {
                 try
                 {
                     if (!DISABLE_EDITOR_OPEN_REFRESH)
                     {
                         // implicitly creates if not present
                         _processor = JSPModelProcessor.get(file);
                         _processor.refresh(!JSPModelProcessor.FORCE_REFRESH,
                                 JSPModelProcessor.RUN_ON_CURRENT_THREAD);
                     }
                 } catch (final Exception e)
                 {
                     JSFCorePlugin.getDefault().getLog().log(
                             new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID,
                                     0, "Error acquiring model processor", e)); //$NON-NLS-1$
                 }
             }
         }
 
         IFile getIFile(final IEditorReference editorRef)
         {
             try
             {
                 final IEditorInput editorInput = editorRef.getEditorInput();
                 final Object adapt = editorInput.getAdapter(IFile.class);
 
                 if (adapt instanceof IFile)
                 {
                     return (IFile) adapt;
                 }
             }
             catch (final PartInitException excp)
             {
                 JSFCorePlugin.getDefault().getLog().log(new Status(IStatus.ERROR, JSFCorePlugin.PLUGIN_ID, 0, "Error acquiring editor input",excp)); //$NON-NLS-1$
             }
 
             return null;
         }
     }
 }

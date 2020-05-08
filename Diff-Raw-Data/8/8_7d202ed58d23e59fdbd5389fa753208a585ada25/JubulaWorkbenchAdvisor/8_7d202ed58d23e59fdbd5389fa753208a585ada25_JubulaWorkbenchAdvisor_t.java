 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.persistence.PersistenceException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.bindings.keys.KeySequence;
 import org.eclipse.jface.bindings.keys.KeySequenceText;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jubula.app.i18n.Messages;
 import org.eclipse.jubula.client.core.businessprocess.ExternalTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.progress.OperationCanceledUtil;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.Plugin.ClientStatus;
 import org.eclipse.jubula.client.ui.businessprocess.ProblemsBP;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.search.query.AbstractSearchQuery;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.eclipse.jubula.tools.exception.JBRuntimeException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.service.datalocation.Location;
 import org.eclipse.search.ui.IQueryListener;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.search.ui.NewSearchUI;
 import org.eclipse.search2.internal.ui.SearchView;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.application.IWorkbenchConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.internal.console.ConsoleView;
 import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
 import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
 import org.osgi.framework.Bundle;
 
 
 /**
  * @author BREDEX GmbH
  *  
  */
 public class JubulaWorkbenchAdvisor extends WorkbenchAdvisor {
     /** the logger */
     private static Log log = LogFactory.getLog(JubulaWorkbenchAdvisor.class);
 
     /**
      * Constructs a new <code>JubulaWorkbenchAdvisor</code>.
      */
     public JubulaWorkbenchAdvisor() {
         // do nothing
     }
 
     /**
      * @return String Perspective
      */
     public String getInitialWindowPerspectiveId() {
         return Constants.SPEC_PERSPECTIVE;
     }
 
     /**
      * @param configurer
      *            IWorkbenchConfigurer
      */
     @SuppressWarnings("nls")
     public void initialize(IWorkbenchConfigurer configurer) {
         super.initialize(configurer);
         configurer.setSaveAndRestore(true);
         
         // To get the icons for projects correctly registered
         final String iconsPath = "icons/full/";
         final String pathObject = iconsPath + "obj16/";
         final String problemsViewPath = iconsPath + "etool16/";
         final String elclPath = iconsPath + "elcl16/";
         final String dlclPath = iconsPath + "dlcl16/";
         final String wizbanPath = iconsPath + "wizban/"; //Wizard icons //$NON-NLS-1$
         
         Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);
         declareWorkbenchImage(configurer, ideBundle, 
                 IDE.SharedImages.IMG_OBJ_PROJECT, 
                 pathObject + "prj_obj.gif", true);
         declareWorkbenchImage(configurer, ideBundle, 
                 IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED,
                 pathObject + "cprj_obj.gif", true);
 
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY,
                 problemsViewPath + "problem_category.gif",
                 true);
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH,
                 pathObject + "error_tsk.gif",
                 true);
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH,
                 pathObject + "warn_tsk.gif",
                 true);
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH,
                 pathObject + "info_tsk.gif",
                 true);
 
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED,
                 elclPath + "smartmode_co.gif",
                 true);
         
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED,
                 dlclPath + "smartmode_co.gif",
                true);
        
        declareWorkbenchImage(configurer, ideBundle,
                IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG,
                wizbanPath + "quick_fix.png", 
                true);
        
         declareWorkbenchImage(configurer, ideBundle,
                 IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG,
                 wizbanPath + "saveas_wiz.gif", true); //$NON-NLS-1$
     }
 
     /**
      * {@inheritDoc}
      * @param configurer
      * @return
      */
     public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
         IWorkbenchWindowConfigurer configurer) {
         
         return new JubulaWorkbenchWindowAdvisor(configurer);
     }
     
     /**
      * This hooks up the adapters from the core (mainly resource) 
      * components to the navigator support
      */
     public void preStartup() {
         IDE.registerAdapters();
     }
 
     /**
      * To get the icons for projects correctly registered
      * @param configurerP 
      *      the workbench configurer
      * @param ideBundle 
      *      the bundle
      * @param symbolicName 
      *      the symbolic name
      * @param path 
      *      the path
      * @param shared 
      *      wether it's shared or not
      */
     private void declareWorkbenchImage(IWorkbenchConfigurer configurerP, 
             Bundle ideBundle, String symbolicName, 
             String path, boolean shared) {
         URL url = ideBundle.getEntry(path);
         ImageDescriptor desc = ImageDescriptor.createFromURL(url);
         configurerP.declareImage(symbolicName, desc, shared);
     }
     
     /**
      * Hook the root of the Common Navigator up to the workspace
      * {@inheritDoc}
      */
     public IAdaptable getDefaultPageInput()  {
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         return workspace.getRoot();
     }
     
     /**
      * doing sth after Startup
      * (starts the spec perspective after the application-start)
      */
     public void postStartup() {
         setupPermanentServices();
         try {
             if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                 .getActivePage().getPerspective().getId()
                 .equals(Constants.SPEC_PERSPECTIVE)) {
                 return;
             }
             PlatformUI.getWorkbench().showPerspective(Constants
                 .SPEC_PERSPECTIVE,
                 PlatformUI.getWorkbench().getActiveWorkbenchWindow());
             Plugin.showStatusLine((IWorkbenchPart)null);
         } catch (WorkbenchException e) {
             log.error(Messages.CannotOpenThePerspective 
                     + Constants.SPEC_PERSPECTIVE
                 + StringConstants.LEFT_PARENTHESES + e 
                 + StringConstants.RIGHT_PARENTHESES
                 + StringConstants.DOT);
             Utils.createMessageDialog(MessageIDs.E_NO_PERSPECTIVE, 
                     new Object[]{Constants.SPEC_PERSPECTIVE}, null);
         }
         Plugin.getDefault().setClientStatus(ClientStatus.RUNNING);
     }
 
     /**
      * establish some servoces for the application
      */
     private void setupPermanentServices() {
         // register problem view listeners
         ProblemsBP.getInstance();
 
         // register AutStarter, AutServer, and test listeners
         TestExecutionContributor.getInstance();
         
         propagateDataDir();
         Plugin.getDefault().getPreferenceStore().addPropertyChangeListener(
             new IPropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent event) {
                     propagateDataDir();
                 }
             });
 
         // register search result updater
         registerSearchResultListener();
     }
 
     /**
      * register listener to display new AbstractSearchQuery results
      */
     private void registerSearchResultListener() {
         NewSearchUI.addQueryListener(new IQueryListener() {
             /** {@inheritDoc} */
             public void queryAdded(ISearchQuery query) {
             // handle if necessary
             }
 
             /** {@inheritDoc} */
             public void queryFinished(final ISearchQuery query) {
                 if (query instanceof AbstractSearchQuery) {
                     PlatformUI.getWorkbench().getDisplay().syncExec(
                             new Runnable() {
                                 public void run() {
                                     // see Bugzilla 72661 and 72771
                                     SearchView sv = (SearchView)Plugin
                                            .getView(NewSearchUI.SEARCH_VIEW_ID);
                                     if (sv != null) {
                                         sv.showSearchResult(query
                                                 .getSearchResult());
                                     }
                                 }
                             });
                 }
             }
 
             /** {@inheritDoc} */
             public void queryRemoved(ISearchQuery query) {
             // handle if necessary
             }
 
             /** {@inheritDoc} */
             public void queryStarting(ISearchQuery query) {
             // handle if necessary
             }
         });
     }
 
     /**
      * gets the data directory info from the preferences and sets
      * them in the BP.
      */
     private void propagateDataDir() {
         IPreferenceStore preferenceStore = 
             Plugin.getDefault().getPreferenceStore();
         if (!preferenceStore.getBoolean(
             Constants.DATADIR_WS_KEY)) {
             ExternalTestDataBP.setDataDir(
                 new File(preferenceStore.getString(
                     Constants.DATADIR_PATH_KEY)));
         } else {
             ExternalTestDataBP.setDataDir(
                 Platform.getLocation().toFile());
         }
     }
     
 
     /**
      * catch every occuring RuntimeException
      * {@inheritDoc}
      * @param exception
      */
     public void eventLoopException(Throwable exception) {
         if (exception instanceof RuntimeException) {
             if (OperationCanceledUtil.isOperationCanceled(
                     (RuntimeException)exception)) {
                 // ignore exception that originates from a canceled 
                 // operation
                 return;
             }
             log.error(Messages.UnhandledRuntimeException, exception);
             if (exception instanceof JBRuntimeException) {
                 Utils.createMessageDialog(((JBRuntimeException)exception)
                         .getErrorId());
                 return;
             } else if (exception instanceof PersistenceException) {
                 Utils.createMessageDialog(MessageIDs.E_UNKNOWN_DB_ERROR);
                 return;
             } else if (exception instanceof IllegalStateException) {
                 // Check whether this error is caused by an invalid workspace
                 Location workspace = Platform.getInstanceLocation();
                 if (workspace.isSet()) {
                     File workspaceDir = 
                         new File(workspace.getURL().getFile());
                     while (workspaceDir != null && !workspaceDir.exists()) {
                         workspaceDir = workspaceDir.getParentFile();
                     }
                     if (workspaceDir == null || !workspaceDir.canWrite()) {
                         String displayDir = getWorkspaceLocation();
                         Utils.createMessageDialog(
                             MessageIDs.E_INVALID_WORKSPACE, 
                             new String [] {displayDir}, null);
                         
                         return;
                     }
                 }
             } else {
                 StackTraceElement[] stackTraceArray = exception.getStackTrace();
                 if (!checkStackTrace(exception, stackTraceArray)) {
                     return;
                 }
             }
 
             if (!Plugin.isRCPException(exception) 
                     && !Plugin.isContentAssistException(exception)) {
                 Utils.createMessageDialog(new JBFatalException(exception,
                         MessageIDs.E_UNEXPECTED_EXCEPTION));
             }
         } else {
             super.eventLoopException(exception);
         }
     }
     /**
      * 
      * @param exception -
      * @param stackTraceArray -
      * @return boolean
      */
     private boolean checkStackTrace(Throwable exception,
             StackTraceElement[] stackTraceArray) {
         for (int i = 0; i < stackTraceArray.length; ++i) {
             String className = stackTraceArray[i].getClassName();
             if ((exception instanceof IllegalArgumentException)
                 && KeySequence.class.getName().equals(className)) {
                 // ignore exception from eclipse framework, that occurs 
                 // in key binding table when binding is selected and 
                 // delete or backspace was pressed
                 return false;
             } else if ((exception instanceof IllegalArgumentException)
                     && stackTraceArray[i].toString().contains("mylyn")) {
                 // ignore all mylyn IllegalArgumentExceptions
                 // needed for exporting a local tasks id or URL
                 return false;
             } else if ((exception instanceof ClassCastException)
                     && stackTraceArray[i].toString().contains("mylyn")) {
                 // ignore all mylyn ClassCastExceptions
                 // needed for exporting uncategorised task categorys
                 return false;
             } else if ((exception instanceof NullPointerException)
                 && KeySequenceText.class.getName().equals(className)) {
                 // ignore exception from eclipse framework, that occurs 
                 // in key binding table when cursor is before binding 
                 // and delete was pressed
                 return false;
             } else if ((exception instanceof NullPointerException)
                 && className != null
                 && className.startsWith(ConsoleView.class.getName())) {
                 // ignore exception from ConsoleView, that occurs when 
                 // clicking on an empty field in the ConsoleView toolbar
                 return false;
             } else if ((exception instanceof NullPointerException)
                 && Plugin.isGEFException(exception)) {
                 // ignore exception from GEF
                 return false;
             } else if (exception instanceof NumberFormatException 
                     && IPageLayout.ID_PROBLEM_VIEW.equals(
                             getActivePartId())) {
                 // ignore exception from eclipse framework that occurs 
                 // in problem view when a non-integer is entered into  
                 // the limits field of the view preferences
                 return false;
             } 
             
         }
         return true;
     }
 
     /**
      * @return the location of the currently selected workspace suitable for 
      *         display to the user.
      */
     private String getWorkspaceLocation() {
         String displayDir;
         try {
             displayDir = new File(Platform.getInstanceLocation()
                 .getURL().getFile()).getCanonicalPath();
         } catch (IOException ioe) {
             displayDir = new File(Platform.getInstanceLocation()
                 .getURL().getFile()).getPath();
         }
         return displayDir;
     }
     
     /**
      * 
      * @return the id of the currently active part, or the empty string if 
      *         no part is currently active.
      */
     private String getActivePartId() {
         String emptyString = StringConstants.EMPTY;
 
         IWorkbench wb = PlatformUI.getWorkbench();
         if (wb == null) {
             return emptyString;
         }
         
         IWorkbenchWindow wbWin = wb.getActiveWorkbenchWindow();
         if (wbWin == null) {
             return emptyString;
         }
         
         IWorkbenchPage page = wbWin.getActivePage();
         if (page == null) {
             return emptyString;
         }
 
         IWorkbenchPart part = page.getActivePart();
         if (part == null) {
             return emptyString;
         }
         
         IWorkbenchPartSite site = part.getSite();
         if (site == null) {
             return emptyString;
         }
         
         return site.getId();
     
         
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean preShutdown() {
         try {
             Plugin.getDefault().setClientStatus(ClientStatus.STOPPING);
             // Close all open editors
             IWorkbenchWindow[] allWW = PlatformUI.getWorkbench()
                 .getWorkbenchWindows();
             
             if (allWW != null) {
                 for (int i = 0; i < allWW.length; i++) {
                     IWorkbenchPage[] allWP = allWW[i].getPages();
                     if (allWP != null) {
                         for (int j = 0; j < allWP.length; j++) {
                             boolean areAllClosed = allWP[j]
                                                          .closeAllEditors(true);
                             if (!areAllClosed) {
                                 return false;
                             }
                         }
                     }
                 }
             }
             
             // save the full workspace before quit
             ResourcesPlugin.getWorkspace().save(true, null);
         } catch (final CoreException e) {
             if (log.isErrorEnabled()) {
                 log.error(Messages.UnhandledRuntimeException, e);
             }
         }  
         return super.preShutdown();
     }
     
 }

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
 package org.eclipse.jubula.client.ui.rcp;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.PersistenceException;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.alm.mylyn.core.bp.CommentReporter;
 import org.eclipse.jubula.client.core.businessprocess.ExternalTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.MasterSessionComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.compcheck.ProblemPropagator;
 import org.eclipse.jubula.client.core.businessprocess.progress.OperationCanceledUtil;
 import org.eclipse.jubula.client.core.errorhandling.ErrorMessagePresenter;
 import org.eclipse.jubula.client.core.errorhandling.IErrorMessagePresenter;
 import org.eclipse.jubula.client.core.progress.IProgressConsole;
 import org.eclipse.jubula.client.core.utils.Languages;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.editors.TestResultViewer;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.CompletenessBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ComponentNameReuseBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ImportFileBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ProblemsBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ToolkitBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.rcp.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.rcp.editors.AbstractJBEditor;
 import org.eclipse.jubula.client.ui.rcp.editors.AbstractTestCaseEditor;
 import org.eclipse.jubula.client.ui.rcp.editors.IJBEditor;
 import org.eclipse.jubula.client.ui.rcp.editors.TestJobEditor;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.DirtyStarListContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.DirtyStarListLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.search.query.AbstractQuery;
 import org.eclipse.jubula.client.ui.rcp.widgets.StatusLineContributionItem;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.eclipse.jubula.tools.exception.JBRuntimeException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.search.ui.IQueryListener;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.search.ui.NewSearchUI;
 import org.eclipse.search2.internal.ui.SearchView;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.eclipse.ui.dialogs.ListSelectionDialog;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.ui.internal.WorkbenchWindow;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.IContributedContentsView;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Base class for plug-ins that integrate with the Eclipse platform UI.
  * 
  * @author BREDEX GmbH
  * @created 06.07.2004
  */
 public class Plugin extends AbstractUIPlugin implements IProgressConsole {
     /** plugin id */
     public static final String PLUGIN_ID = "org.eclipse.jubula.client.ui.rcp"; //$NON-NLS-1$
 
     /** maps images to their "generated" (green-tinted) counterparts */
     public static final Map<Image, Image> GENERATED_IMAGES = 
         new HashMap<Image, Image>();
     
     /**
      * @param original The original, or base, image.
      * @return the "cut" version of the image. Client should not 
      *         dispose this image.
      */
     public static final Image TC_DISABLED_IMAGE = new Image(
             IconConstants.TC_IMAGE.getDevice(), 
             IconConstants.TC_IMAGE, SWT.IMAGE_GRAY);
     
     /** name of the Jubula console */
     private static final String JB_CONSOLE_NAME = "Console"; //$NON-NLS-1$
     /** for log messages */
     private static Logger log = LoggerFactory.getLogger(Plugin.class);
     /** single instance of plugin */
     private static Plugin plugin;
     /** StatusLineText */
     private static String statusLineText = Messages.StatusLine_NotConnected;
     /** StatusLineText */
     private static int connectionStatusIcon = Constants.DEFAULT_ICON;
     /** current workbench window */
     private static IWorkbenchWindow workbenchWindow;
     /** <code>CONNECTION_INFO_STATUSLINE_ITEM</code> */
     private static final String CONNECTION_INFO_STATUSLINE_ITEM = "connectionInfo"; //$NON-NLS-1$
     /** <code>AUT_TOOLKIT_STATUSLINE_ITEM</code> */
     private static final String AUT_TOOLKIT_STATUSLINE_ITEM = "autToolKitInfo"; //$NON-NLS-1$
     /** <code>LANG_STATUSLINE_ITEM</code> */
     private static final String LANG_STATUSLINE_ITEM = "lang"; //$NON-NLS-1$
     /** the client status */
     private ClientStatus m_status = ClientStatus.STARTING;
     /** the preference store for this bundle */
     private ScopedPreferenceStore m_preferenceStore = null;
     /** the console */
     private MessageConsole m_console;
     /** standard message stream for the console */
     private MessageConsoleStream m_standardMessageStream;
     /** error message stream for the console */
     private MessageConsoleStream m_errorMessageStream;
     /** the currently running application title */
     private String m_runningApplicationTitle = null;
     
     static {
         GENERATED_IMAGES.put(IconConstants.TC_IMAGE, 
                        IconConstants.getImage("testCase_generated.gif")); //$NON-NLS-1$
         GENERATED_IMAGES.put(IconConstants.TC_REF_IMAGE, 
                        IconConstants.getImage("testCaseRef_generated.gif")); //$NON-NLS-1$
         GENERATED_IMAGES.put(IconConstants.CATEGORY_IMAGE, 
                        IconConstants.getImage("category_generated.gif")); //$NON-NLS-1$
     }
 
     /**
      * Creates an UI plug-in runtime object
      */
     public Plugin() {
         super();
     }
     
     /**
      * The Client Status
      */
     public enum ClientStatus {
         /**
          * Jubula is launched completely
          */
         RUNNING,
         /**
          * Jubula is starting up
          */
         STARTING,
         /**
          * Jubula is shutting down
          */
         STOPPING
     }
     
     /**
      * 
      * @param statust - the new ClientStatus (RUNNING,STOPPING,STARTING)
      */
     public void setClientStatus(ClientStatus statust) {
         m_status = statust;
     }
     
     /**
      * 
      * @return the actual ClientStatus (RUNNING,STARTING,STOPPING)
      */
     public ClientStatus getClientStatus() {
         return m_status;
     }
     
     /**
      * {@inheritDoc}
      */
     public void stop(BundleContext context) throws Exception {
         super.stop(context);
     }
 
     /**
      * {@inheritDoc}
      */
     public IPreferenceStore getPreferenceStore() {
 
         // Create the preference store lazily.
         if (m_preferenceStore == null) {
             // Use the Plugin ID from org.eclipse.jubula.client.ui so that 
             // preferences will continue to be saved to that area. Otherwise,
             // users would lose their preferences after the UI bundle was split
             // into client.ui and client.ui.rcp.
             m_preferenceStore = new ScopedPreferenceStore(
                     InstanceScope.INSTANCE, 
                     Constants.PLUGIN_ID);
         }
 
         return m_preferenceStore;
 
     }
     
     /**
      * @return The display.
      */
     public static Display getDisplay() {
         return PlatformUI.getWorkbench().getDisplay();
     }
 
     /**
      * Sets a text in the Status Line
      * @param text String
      */
     private static void setStatusLineText(String text) {
         statusLineText = text;
         showStatusLine((IWorkbenchPart)null);
     }
 
     /**
      * Gets Status Line Text
      * @return String StatusLine m_text
      */
     public static String getStatusLineText() {
         return statusLineText;
     }
 
     /**
      * Displays a text in the Status Line
      * @param text String
      * @param ampel String
      */
     public static void showStatusLine(final int ampel, final String text) {
         Display.getDefault().asyncExec(new Runnable() {
 
             @SuppressWarnings("synthetic-access")
             public void run() {
                 setConnectionStatusIcon(ampel);
                 setStatusLineText(text);
             }
             
         });
         
     }
 
     /**
      * Displays a m_text in the Status Line
      * @param partFocused IWorkbenchPart
      */
     public static void showStatusLine(IWorkbenchPart partFocused) {
         IWorkbench workbench = getDefault().getWorkbench();
         IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
         if (getActivePage() == null
                 && partFocused == null) {
             return;
         }
         IWorkbenchPart part;
         if (partFocused != null) {
             part = partFocused;
         } else {
             part = getActivePart();    
         }
         IStatusLineManager manager = ((WorkbenchWindow)window)
                 .getStatusLineManager();
         if (part instanceof ViewPart) {
             manager = ((ViewPart)part).getViewSite().getActionBars()
                     .getStatusLineManager();
         } else if (part instanceof EditorPart) {
             manager = ((EditorPart)part).getEditorSite().getActionBars()
                     .getStatusLineManager();
         }
         if (window instanceof WorkbenchWindow) {
             StatusLineContributionItem item = 
                 (StatusLineContributionItem)manager.find(
                     CONNECTION_INFO_STATUSLINE_ITEM);
             if (item == null) {
                 if (getDefault().getWorkbench().getActiveWorkbenchWindow() 
                         != null) {
                     createStatusLineItems();
                     item = (StatusLineContributionItem)manager.find(
                             CONNECTION_INFO_STATUSLINE_ITEM);
                 } 
                 if (item == null) {
                     return;
                 }
             }
             switch (isConnectionStatusIcon()) {
                 case Constants.NO_SERVER:
                     item.setMessage(IconConstants.NO_SERVER_IMAGE, 
                             getStatusLineText());
                     break;
                 case Constants.NO_SC:
                 case Constants.NO_AUT:
                     item.setMessage(IconConstants.NO_CONNECTION_IMAGE, 
                             getStatusLineText());
                     break;
                 case Constants.AUT_UP:
                     item.setMessage(IconConstants.AUT_RUNNING_IMAGE, 
                             getStatusLineText());
                     break;
                 case Constants.RECORDING:
                     item.setMessage(IconConstants.CAM_IMAGE,
                             getStatusLineText());
                     break;
                 case Constants.CHECKING:
                     item.setMessage(IconConstants.CHECK_CAM_IMAGE,
                             getStatusLineText());
                     break;
                 case Constants.MAPPING:
                     item.setMessage(IconConstants.MAP_IMAGE,
                             getStatusLineText());
                     break;
                 case Constants.PAUSED:
                     item.setMessage(IconConstants.PAUSE_IMAGE,
                             getStatusLineText());
                     break;
                 default:
                     item.setMessage(IconConstants.NO_AUT_IMAGE,
                             getStatusLineText());
             }
             manager.update(true);
         }
     }
 
     /**
      * Returns the active View or null.
      * @return a <code>ViewPart</code> value. The active View.
      */
     public static ViewPart getActiveView() {
         IWorkbenchPart view = getActivePart();
         if (view instanceof ViewPart) {
             return (ViewPart)view;
         }
         return null;
     }
 
     /**
      * Returns the active editor or null.
      * @return a <code>EditorPart</code> value. The active Editor.
      */
     public static IEditorPart getActiveEditor() {
         final IWorkbenchPage activePage = getActivePage();
         if (activePage == null) {
             return null;
         }
         final IEditorPart editor = activePage.getActiveEditor();
         if (editor != null) {
             return editor;
         }
         // If a view is maximized, getActiveEditor() returns null.
         // So lets ask all IJBEditors.
         final IEditorReference[] editorReferences = activePage
             .getEditorReferences();
         for (IEditorReference edRef : editorReferences) {
             final IEditorPart editorPart = edRef.getEditor(false);
             if (editorPart instanceof IJBEditor) {
                 IJBEditor gdEd = (IJBEditor)editorPart;
                 if (gdEd.getEditorHelper().isActive()) {
                     return gdEd;
                 }
             }
         }
         return null;
     }
 
     /**
      * @return all references to open editors.
      */
     public static List < IEditorReference > getAllEditors() {
         if (getActivePage() == null) {
             return new ArrayList < IEditorReference > ();
         }
         IEditorReference[] editors = getActivePage().getEditorReferences();
         List < IEditorReference > editorList = 
             new ArrayList < IEditorReference > ();
         for (IEditorReference editor : editors) {
             if (editor.getId().equals(Constants.TEST_CASE_EDITOR_ID)
                 || editor.getId().equals(Constants.OBJECTMAPPINGEDITOR_ID)
                 || editor.getId().equals(Constants.TEST_SUITE_EDITOR_ID)) {
                 editorList.add(editor);
             }
         }
         return editorList;
     }
 
     /**
      * Returns the active part or null.
      * @return an <code>IWorkbenchPart</code> value. The active part.
      */
     public static IWorkbenchPart getActivePart() {
         if (getActivePage() != null) {
             return getActivePage().getActivePart();
         }
         return null;
     }
 
     /**
      * Returns the active page or null.
      * @return an <code>IWorkbenchPage</code> value. The active page.
      */
     public static IWorkbenchPage getActivePage() {
         IWorkbenchWindow ww = PlatformUI.getWorkbench()
                 .getActiveWorkbenchWindow();
         if (ww == null) {
             return null;
         }
         return ww.getActivePage();
     }
 
     /**
      * @param id The ID of the view to find.
      * @return A <code>IViewReference</code> value. The view or null.
      **/
     @Deprecated
     public static IViewPart getView(String id) {
         Validate.notEmpty(id, Messages.Missing_ID);
         if (getActivePage() != null) {
             IViewPart vp = getActivePage().findView(id);
             return vp;
         }
         return null;
     }
     
     /**
      * Writes a line of text to the console.
      * 
      * @param msg the message to print.
      * @param forceActivate whether the console should be activated if it is not
      *                      already open.
      */
     public void writeLineToConsole(String msg, boolean forceActivate) {
         // FIXME zeb this "if" statement is necessary because the DB Tool, which never
         //           starts the workbench because it never intends to use PlatformUI,
         //           depends on and uses methods from org.eclipse.jubula.client.ui.rcp.
         //           Thus, these dependencies should be removed, and then the "if"
         //           statement should thereafter also be removed.
         if (PlatformUI.isWorkbenchRunning()) {
             MessageConsoleStream stream = getStandardConsoleStream();
             if (forceActivate) {
                 stream.getConsole().activate();
             }
             
             stream.println(msg);
         }
     }
 
     /**
      * Writes a line of text to the console.
      * 
      * @param msg the message to print.
      * @param forceActivate whether the console should be activated if it is not
      *                      already open.
      */
     public void writeErrorLineToConsole(String msg, boolean forceActivate) {
         // FIXME zeb this "if" statement is necessary because the DB Tool, which never
         //           starts the workbench because it never intends to use PlatformUI,
         //           depends on and uses methods from ClientGUI.
         //           Thus, these dependencies should be removed, and then the "if"
         //           statement should thereafter also be removed.
         if (PlatformUI.isWorkbenchRunning()) {
             MessageConsoleStream stream = getErrorConsoleStream();
             if (forceActivate) {
                 stream.getConsole().activate();
             }
     
             stream.println(msg);
         }
     }
 
     /**
      * Lazy retrieval of the standard message console stream.
      * 
      * @return the standard message stream for the console
      */
     private synchronized MessageConsoleStream getStandardConsoleStream() {
         if (m_standardMessageStream == null) {
             m_standardMessageStream = getConsole().newMessageStream();
         }
         return m_standardMessageStream;
     }
     
     /**
      * Lazy retrieval of the error message console stream.
      * 
      * @return the error message stream for the console
      */
     private synchronized MessageConsoleStream getErrorConsoleStream() {
         if (m_errorMessageStream == null) {
             m_errorMessageStream = getConsole().newMessageStream();
             final Display d = getDisplay();
             final MessageConsoleStream stream = m_errorMessageStream;
             d.syncExec(new Runnable() {
                 public void run() {
                     stream.setColor(d.getSystemColor(SWT.COLOR_RED));
                 }
             });
         }
         return m_errorMessageStream;
     }
 
     /**
      * Lazy retrieval of the console.
      * 
      * @return the console
      */
     private synchronized MessageConsole getConsole() {
         if (m_console == null) {
             m_console = new MessageConsole(JB_CONSOLE_NAME, null);
             m_console.activate();
             ConsolePlugin.getDefault().getConsoleManager().addConsoles(
                 new IConsole [] {m_console});
 
         }
         return m_console;
     }
     
     /**
      * Shows the view with the given id. 
      * @param id The ID of the view to show.
      * @return the View.
      */
     public static IViewPart showView(String id) {
         return showView(id, null, IWorkbenchPage.VIEW_ACTIVATE);
     }
 
     /**
      * Shows the view with the given id. 
      * 
      * @see org.eclipse.ui.IWorkbenchPage.showView
      * 
      * @param viewID
      *            The ID of the view to show.
      * @param secondaryViewID
      *            the secondaryViewID
      * @param mode
      *            the view mode
      * @return the View.
      */
     public static IViewPart showView(String viewID, String secondaryViewID,
             int mode) {
         Validate.notEmpty(viewID, Messages.Missing_ID);
         if (getActivePage() != null) {
             IViewPart vp = null;
             try {
                 vp = getActivePage().showView(viewID, secondaryViewID, mode);
             } catch (PartInitException e) {
                 ErrorHandlingUtil.createMessageDialog(new JBException(
                         NLS.bind(Messages.PluginCantOpenView + viewID,
                         MessageIDs.E_CLASS_NOT_FOUND), null, null));
             }
             return vp;
         }
         return null;
     }
     
     /**
      * activates the given workbench part if an active page is available
      * 
      * @param part
      *            the part to activate
      */
     public static void activate(IWorkbenchPart part) {
         if (getActivePage() != null) {
             getActivePage().activate(part);
         }
     }
 
     /**
      * @return Returns the connectionStatusIcon.
      */
     public static int isConnectionStatusIcon() {
         return connectionStatusIcon;
     }
 
     /**
      * @param aS The ampelStatus to set.
      */
     private static void setConnectionStatusIcon(int aS) {
         connectionStatusIcon = aS;
         showStatusLine((IWorkbenchPart)null);
     }
 
     
     /**
      * Gets the Editor by title 
      * @param title the title of the editor
      * @return an IWorkbenchPart, the editor or null if not found.
      */
     public static IEditorPart getEditorByTitle(String title) {
         IEditorReference[] editors = getActivePage().getEditorReferences();
         for (int i = 0; i < editors.length; i++) {
             String edTitle = editors[i].getTitle();
             if (title.equals(edTitle)) {
                 return (IEditorPart)editors[i].getPart(false);
             }
         }
         return null;
     }
  
     /**
      * Checks if a view with the given ID is open.
      * @param id The ID of the view.
      * @return a <code>boolean</code> value.
      */
     public static boolean isViewOpen(String id) {
         if (getView(id) != null) {
             return true;
         }
         return false;
     }
 
     /**
      * @return An array of IEditorParts.
      */
     public IEditorPart[] getDirtyEditors() {
         if (getActivePage() != null) {
             return getActivePage().getDirtyEditors();
         }
         return new IEditorPart[0];
     }
 
     /**
      * @return the active Jubula editor or null if no Jubula editor is active
      */
     public AbstractJBEditor getActiveJBEditor() {
         IWorkbenchPage activePage = getActivePage();
         if (activePage == null) { // during shutdown
             return null;
         }
         IEditorPart iEditorPart = getActiveEditor();
         if (iEditorPart != null && iEditorPart instanceof AbstractJBEditor) {
             return (AbstractJBEditor) iEditorPart;
         }
         return null;
     }
     
     /**
      * @return the active TestCaseEditor or null if no TC editor is active
      */
     public AbstractTestCaseEditor getActiveTCEditor() {
         if (getActiveJBEditor() instanceof AbstractTestCaseEditor) {
             return (AbstractTestCaseEditor)getActiveJBEditor();
         }
         return null;
     }
     
     /**
      * @return the active TestJobEditor or null if no TJ editor is active
      */
     public TestJobEditor getActiveTJEditor() {
         IWorkbenchPage activePage = getActivePage();
         if (activePage == null) { // during shutdown
             return null;
         }
         IEditorPart iEditorPart = getActiveEditor();
         if (iEditorPart != null 
                 && iEditorPart instanceof TestJobEditor) {
             return (TestJobEditor)iEditorPart;
         }
         return null;              
     }
 
     /**
      * Closes all opened Jubula editors (ex. TCE, TSE, OME). This method must be
      * called from the GUI thread. If it is called from a thread that is *not*
      * the GUI thread, it will do nothing.
      * 
      * @param alsoProjectIndependent
      *            whether also project independent editors should be closed such
      *            as the testresultviewer
      */
     public static void closeAllOpenedJubulaEditors(
         boolean alsoProjectIndependent) {
         IWorkbenchPage activePage = getActivePage();
         if (activePage != null) {
             List<IEditorReference> editorParts = getAllOpenedJubulaEditors(
                     alsoProjectIndependent);
             activePage.closeEditors(
                     editorParts.toArray(
                             new IEditorReference[editorParts.size()]),
                     false);
         }
     }
     
     /**
      * @param original The original, or base, image.
      * @return the "generated" version of the image. Client should not 
      *         dispose this image.
      */
     public static Image getGeneratedImage(Image original) {
         Image genImage = GENERATED_IMAGES.get(original);
         if (genImage == null) {
             log.error("'Generated' image does not exist."); //$NON-NLS-1$
             genImage = original;
         }
         
         return genImage;
     }
     /**
      * 
      * @param original The original, or base, image.
      * @return the "cut" version of the image. Client should not 
      *         dispose this image.
      */
     public static Image getCutImage(Image original) {
         Image cutImage = GENERATED_IMAGES.get(original);
         if (cutImage == null) {
             cutImage = new Image(original.getDevice(), original, 
                     SWT.IMAGE_GRAY);
             GENERATED_IMAGES.put(original, cutImage);
         }
         
         return cutImage;
     }
 
     /**
      * @param alsoProjectIndependent
      *            True, if also project independent editors should be collected such
      *            as the test result viewer, otherwise false.
      * @return A set of editor references of all opened Jubula editors (i.e. TCE, TSE, OME),
      *         or an empty set. If this method is not called from the GUI thread, the set is empty.
      */
     private static List<IEditorReference> getAllOpenedJubulaEditors(
             boolean alsoProjectIndependent) {
         List<IEditorReference> editorRefs = new ArrayList<IEditorReference>();
         IWorkbenchPage activePage = getActivePage();
         if (activePage != null) {
             for (IEditorReference editorRef: activePage.getEditorReferences()) {
                 IEditorPart editor = editorRef.getEditor(true);
                 if (editor instanceof IJBPart) {
                     if (alsoProjectIndependent
                             || !(editor instanceof TestResultViewer)) {
                         editorRefs.add(editorRef);
                     }
                 }
             }
         }
         return editorRefs;
     }
 
     /**
      * 
      * @return the shell for the active workbench window. Note that dialogs are 
      *         not workbench windows.
      */
     public static Shell getActiveWorkbenchWindowShell() {
         return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
     }
     
     /**
      * This method does not necessarily return the active shell. Rather,
      * it returns the 0th shell in the display's shell array.
      * 
      * @return The actual shell.
      */
     public static Shell getShell() {
         return (PlatformUI.getWorkbench().getDisplay().getShells())[0];
     }
 
     /**
      * checks, if any editor was not saved before starting the AUT
      * @return true, if there are unsaved m_editors, false otherwise
      */
     public boolean anyDirtyStar() {
         return !getProjectDependentDirtyEditors().isEmpty();
     }
 
     /**
      * @return A list of project dependent dirty editors.
      */
     private List<IEditorReference> getProjectDependentDirtyEditors() {
         List<IEditorReference> dirtyEditors = new ArrayList<IEditorReference>();
         for (IEditorReference editorRef: getAllOpenedJubulaEditors(false)) {
             if (editorRef.isDirty()) {
                 dirtyEditors.add(editorRef);
             }
         }
         return dirtyEditors;
     }
 
     /**
      * Opens the {@link ListSelectionDialog} with a preselected list of
      * all project dependent dirty editors. If the list is empty, the dialog
      * is not shown.
      * @return True, if there are not dirty editors or all preselected dirty
      *         editors have been saved, otherwise false.
      */
     public boolean showSaveEditorDialog() {
         List<IEditorReference> dirtyEditors = getProjectDependentDirtyEditors();
         boolean hasDirtyEditors = !dirtyEditors.isEmpty();
         if (hasDirtyEditors) {
             ListSelectionDialog dialog = new ListSelectionDialog(getShell(),
                     dirtyEditors.toArray(), new DirtyStarListContentProvider(),
                     new DirtyStarListLabelProvider(),
                     Messages.StartSuiteActionMessage);
             dialog.setTitle(Messages.StartSuiteActionTitle);
             dialog.setInitialElementSelections(dirtyEditors);
             dialog.open();
             if (dialog.getReturnCode() == Window.OK) {
                 // save all selected editors
                 Object[] editorRefs = dialog.getResult();
                 if (editorRefs != null) {
                     for (Object editorObj : editorRefs) {
                         if (editorObj instanceof IEditorReference) {
                             IEditorReference editorRef = (IEditorReference)
                                     editorObj;
                             IEditorPart editorPart = editorRef.getEditor(true);
                             editorPart.doSave(new NullProgressMonitor());
                         }
                     }
                 }
                 hasDirtyEditors = !getProjectDependentDirtyEditors()
                         .isEmpty();
             }
         }
         setNormalCursor();
         return !hasDirtyEditors;
     }
 
     /** Sets the hour glass cursor. */
     private static void setWaitCursor() {
         getDisplay().syncExec(new Runnable() {
             public void run() {
                 getActiveWorkbenchWindowShell().setCursor(
                         getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
             }
         });
     }
 
     /** Sets the normal (=arrow) cursor. */
     private static void setNormalCursor() {
         getDisplay().syncExec(new Runnable() {
             public void run() {
                 getActiveWorkbenchWindowShell().setCursor(
                         getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
             }
         });
     }
 
     /**
      * @return  IWorkbenchHelpSystem.
      */
     public static IWorkbenchHelpSystem getHelpSystem() {
         return PlatformUI.getWorkbench().getHelpSystem();
     }
 
     /**
      * {@inheritDoc}
      */
     public Object handleError(final Throwable e) {
         Plugin.getDisplay().asyncExec(new Runnable() {
             @SuppressWarnings("synthetic-access")
             public void run() {
                 if (e instanceof RuntimeException
                         && OperationCanceledUtil.isOperationCanceled(
                                 (RuntimeException)e)) {
                     // root cause of exception is the cancelation of an
                     // operation, so it can be safely ignored
                     return;
                 }
                 log.error(Messages.UnhandledThrowable + StringConstants.SPACE
                         + StringConstants.COLON + StringConstants.SPACE, e);
                 if (e instanceof JBRuntimeException) {
                     JBRuntimeException gdEx = (JBRuntimeException)e;
                     ErrorHandlingUtil.createMessageDialog(gdEx);
                 } else if (e instanceof JBException) {
                     JBException gdEx = (JBException)e;
                     ErrorHandlingUtil.createMessageDialog(gdEx, null, null);
                 } else if (isRCPException(e)) {
                     // there are a few bugs in RCP which will trigger
                     // an exception, notably the 
                     // org.eclipse.ui.views.markers.internal.MarkerAdapter.buildAllMarkers()
                     // well known racing condition. Since the error is not
                     // critical, is is ignored.
                     log.error(Messages.InternalRcpError, e);
                     
                 } else if (isGEFException(e)) {
                     // there are a few bugs in GEF which will trigger
                     // an exception, notably the 
                     // org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx.addSourceCommands
                     // well known racing condition. Since the error is not
                     // critical, is is ignored.
                     log.error(Messages.InternalGefError, e);
                     
                 } else if (e instanceof PersistenceException) {
                     ErrorHandlingUtil.createMessageDialog(
                         MessageIDs.E_DATABASE_GENERAL,
                         null, new String [] {e.getLocalizedMessage()});
                 } else {
                     ErrorHandlingUtil.createMessageDialog(
                             new JBFatalException(e,
                             MessageIDs.E_UNEXPECTED_EXCEPTION));
                 }
             }
             
         });
         return null;
     }
     
     /**
      * checks if the exception is caused by a known RCP problem
      * @param t the Throwable which was caught in the error handler
      * @return true if the exception was cause by an RCP bug
      */
     public static boolean isRCPException(Throwable t) {
         Throwable work = t;
         do {
             StackTraceElement[] stack = work.getStackTrace();
             for (StackTraceElement el : stack) {
                 String className = el.getClassName();
                 // check for
                 // org.eclipse.ui.views.markers.internal.MarkerAdapter.buildAllMarkers
                 if (className.indexOf("MarkerAdapter") != -1) { //$NON-NLS-1$
                     return true;
                 }
                 // double click error in help view
                 if (className.indexOf("EngineResultSection") != -1) { //$NON-NLS-1$
                     return true;
                 }
                 // Context Sensitive Help when DSV Cell Editor is open #3291
                 if (className.indexOf("ContextHelpPart") != -1) { //$NON-NLS-1$
                     return true;
                 }
                 // http://bugzilla.bredex.de/84 
                 if (className.indexOf("CompositeImageDescriptor") != -1) { //$NON-NLS-1$
                     return true;
                 }
                 // http://bugzilla.bredex.de/933 
                 if (className.indexOf("EditorStateParticipant") != -1) { //$NON-NLS-1$
                     return true;
                 }
             }
             // Recursive activation on MacOSX on expand tree item #3618
             String detailMessage = work.getMessage();
             if (detailMessage != null && detailMessage.indexOf("WARNING: Prevented recursive attempt to activate part") != -1) { //$NON-NLS-1$
                 return true;
             }
             work = work.getCause();
         } while (work != null);
         return false;
     }
     
     
     /**
      * checks if the exception is caused by a known Content Assist problem
      * @param t the Throwable which was caught in the error handler
      * @return true if the exception was cause by a known Content Assist bug
      */
     public static boolean isContentAssistException(Throwable t) {
 
         Throwable work = t;
         do {
             if (work instanceof NullPointerException) {
                 StackTraceElement[] stack = work.getStackTrace();
                 for (StackTraceElement el : stack) {
                     // check for
                     // NPE in recomputeProposals
                     if (Messages.RecomputeProposals.
                             equals(el.getMethodName())) {
                         return true;
                     }
                 }
             }
             work = work.getCause();
         } while (work != null);
         return false;
     }
 
     /**
      * checks if the exception is caused by a known GEF problem
      * @param t the Throwable which was caught in the error handler
      * @return true if the exception was cause by an GEF bug
      */
     public static boolean isGEFException(Throwable t) {
         Throwable work = t;
         do {
             StackTraceElement[] stack = work.getStackTrace();
             for (StackTraceElement el : stack) {
                 // check for
                 // org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx.addSourceCommands
                 if (el.getClassName().indexOf("DragEditPartsTrackerEx") != -1) { //$NON-NLS-1$
                     return true;
                 }
             }
             work = work.getCause();
         } while (work != null);
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
         ErrorMessagePresenter.setPresenter(new IErrorMessagePresenter() {
             public void showErrorMessage(JBException ex, Object[] params,
                     String[] details) {
                 ErrorHandlingUtil.createMessageDialog(ex, params, details);
             }
 
             public void showErrorMessage(Integer messageID, Object[] params,
                     String[] details) {
                 ErrorHandlingUtil.createMessageDialog(
                         messageID, params, details);
             }
         });
         
         new Thread(new Runnable() {
             public void run() {
                 // init (java)available languages
                 Languages.getInstance();
             }
         }).start();
         registerPermanentServices();
     }
 
     /**
      * register business processes and service that should be available
      * while complete Jubula live cycle
      * - ProgressController
      * - ComponentNamesListBP
      */
     private void registerPermanentServices() {
         // register problem view listeners
         ProblemsBP.getInstance();
 
         // register AutStarter, AutServer, and test listeners
         TestExecutionContributor.getInstance();
         CommentReporter instance = CommentReporter.getInstance();
         instance.setConsole(this);
         
         propagateDataDir();
         Plugin.getDefault().getPreferenceStore().addPropertyChangeListener(
             new IPropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent event) {
                     propagateDataDir();
                 }
             });
 
         // register search result updater
         registerSearchResultListener();
 
         ImportFileBP.getInstance();
         
         // register service for checking completeness
         CompletenessBP.getInstance();
         ProblemPropagator.getInstance();
 
         // register Component Name reuse tracker
         ComponentNameReuseBP.getInstance();
         
         // register service for toolkit
         ToolkitBP.getInstance();
     }
 
     /**
      * displays the working language 
      */
     public static void showLangInfo() {
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 IStatusLineManager manager = getStatusLineManager();
                 StatusLineContributionItem item = 
                     (StatusLineContributionItem)manager.find(
                         LANG_STATUSLINE_ITEM);
                 if (item == null) {
                     return;
                 }
                 if (WorkingLanguageBP.getInstance().getWorkingLanguage() 
                     != null) {
                     
                     item.setText(WorkingLanguageBP.getInstance()
                         .getWorkingLanguage().getDisplayName());
                 } else {
                     item.setText(StringConstants.EMPTY);
                 }
                 manager.update(true);
             }
         });
     }
     
     /**
      * displays the auttoolkit
      * @param toolkit the aut ToolKit of the actual project.
      */
     public static void showAutToolKitInfo(String toolkit) {
         IStatusLineManager manager = getStatusLineManager();
         StatusLineContributionItem item = 
             (StatusLineContributionItem)manager
                 .find(AUT_TOOLKIT_STATUSLINE_ITEM);
         if (item == null) {
             return;
         }
         item.setText(CompSystemI18n.getString(toolkit));
         manager.update(true);
     }
     
     /**
      * create the items for the status line
      */
     public static void createStatusLineItems() {
         IStatusLineManager manager = getStatusLineManager();        
         StatusLineContributionItem connectionItem = 
             new StatusLineContributionItem(
                 CONNECTION_INFO_STATUSLINE_ITEM);
         manager.insertBefore(StatusLineManager.END_GROUP, connectionItem);
         StatusLineContributionItem langItem = 
             new StatusLineContributionItem(LANG_STATUSLINE_ITEM);
         manager.insertBefore(CONNECTION_INFO_STATUSLINE_ITEM, langItem);
         StatusLineContributionItem autToolKitItem = 
             new StatusLineContributionItem(AUT_TOOLKIT_STATUSLINE_ITEM);
         autToolKitItem.setText(StringConstants.EMPTY);
         manager.insertBefore(LANG_STATUSLINE_ITEM, autToolKitItem);
         manager.update(true);
     }
 
     /**
      * @return the status line manager of the current workbench window
      */
     public static IStatusLineManager getStatusLineManager() {
         IWorkbench workbench = getDefault().getWorkbench();
         workbenchWindow = workbench.getActiveWorkbenchWindow();
         if (workbenchWindow == null) {
             Display.getDefault().syncExec(new Runnable() {
                 @SuppressWarnings("synthetic-access")
                 public void run() {
                     Shell shell = getShell();
                     while (shell != null) {
                         Object data = shell.getData();
                         if (data instanceof IWorkbenchWindow) {
                             workbenchWindow = (IWorkbenchWindow) data;
                             break;
                         }
                         shell = (Shell)shell.getParent();
                     }
                 }
             });
         }
         IStatusLineManager manager = ((WorkbenchWindow)workbenchWindow)
             .getStatusLineManager();
         return manager;
     }
 
     /**
      * Resets the status line and mousecursor after a long running operation.
      */
     public static void stopLongRunning() {
         Display.getDefault().syncExec(new Runnable() {
             @SuppressWarnings("synthetic-access")
             public void run() {
                 getStatusLineManager().setErrorMessage(null);
                 setNormalCursor();
             }
         });
     }
 
     /**
      * Sets the given message in the status line and the mousecursor (hourglass)
      * when a long running operation starts.
      * 
      * @param message the message to set
      */
     public static void startLongRunning(final String message) {
         Display.getDefault().syncExec(new Runnable() {
             public void run() {
                 getStatusLineManager().setErrorMessage(
                         IconConstants.LONG_RUNNING_IMAGE, message);
                 startLongRunning();
             }
         });
     }
     
     /**
      * Sets the mousecursor (hourglass) when a long running operation starts.
      * The status line is ignored by this method.
      */
     public static void startLongRunning() {
         Display.getDefault().syncExec(new Runnable() {
             @SuppressWarnings("synthetic-access")
             public void run() {
                 setWaitCursor();
             }
         });
     }
 
     /**
      * 
      * @return the component mapper corresponding to the active editor, or
      *         the master session mapper if the active editor has no 
      *         coresponding mapper or if no editor is currently active.
      */
     public static IComponentNameMapper getActiveCompMapper() {
         IWorkbenchPart activePart = Plugin.getActivePart();
         if (activePart instanceof IContributedContentsView) {
             activePart = 
                 ((IContributedContentsView)activePart).getContributingPart();
         }
         IComponentNameMapper mapper = null;
         if (activePart != null) {
             mapper = (IComponentNameMapper)activePart
                     .getAdapter(IComponentNameMapper.class);
         }
         if (mapper != null) {
             return mapper;
         }
         
         return MasterSessionComponentNameMapper.getInstance();
     }
 
     /**
      * {@inheritDoc}
      */
     public void writeErrorLine(String line) {
         writeErrorLineToConsole(line, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void writeLine(String line) {
         writeLineToConsole(line, true);
     }
 
     /**
      * @return the running application title
      */
     public String getRunningApplicationTitle() {
         if (m_runningApplicationTitle == null) {
             m_runningApplicationTitle = getActiveWorkbenchWindowShell()
                     .getText();
         }
         return m_runningApplicationTitle;
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
                 if (query instanceof AbstractQuery) {
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
      * @return instance of plugin
      */
     public static Plugin getDefault() {
         return plugin;
     }
 
 }

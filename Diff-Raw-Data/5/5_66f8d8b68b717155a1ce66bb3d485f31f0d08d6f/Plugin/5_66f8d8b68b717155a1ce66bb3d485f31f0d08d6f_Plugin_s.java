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
 package org.eclipse.jubula.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.PersistenceException;
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.IErrorListener;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.MasterSessionComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.progress.OperationCanceledUtil;
 import org.eclipse.jubula.client.core.errorhandling.ErrorMessagePresenter;
 import org.eclipse.jubula.client.core.errorhandling.IErrorMessagePresenter;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.persistence.ISpecPersistable;
 import org.eclipse.jubula.client.core.progress.IProgressConsole;
 import org.eclipse.jubula.client.core.utils.IProgressListener;
 import org.eclipse.jubula.client.core.utils.Languages;
 import org.eclipse.jubula.client.core.utils.PrefStoreHelper;
 import org.eclipse.jubula.client.core.utils.ProgressEventDispatcher;
 import org.eclipse.jubula.client.ui.businessprocess.CompletenessBP;
 import org.eclipse.jubula.client.ui.businessprocess.ComponentNameReuseBP;
 import org.eclipse.jubula.client.ui.businessprocess.ToolkitBP;
 import org.eclipse.jubula.client.ui.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.controllers.ProgressController;
 import org.eclipse.jubula.client.ui.editors.AbstractJBEditor;
 import org.eclipse.jubula.client.ui.editors.AbstractTestCaseEditor;
 import org.eclipse.jubula.client.ui.editors.IJBEditor;
 import org.eclipse.jubula.client.ui.editors.TestJobEditor;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.model.TestCaseBrowserRootGUI;
 import org.eclipse.jubula.client.ui.model.TestSuiteGUI;
 import org.eclipse.jubula.client.ui.provider.contentprovider.DirtyStarListContentProvider;
 import org.eclipse.jubula.client.ui.provider.labelprovider.DirtyStarListLabelProvider;
 import org.eclipse.jubula.client.ui.utils.ImageUtils;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.client.ui.views.TestCaseBrowser;
 import org.eclipse.jubula.client.ui.views.TestResultTreeView;
 import org.eclipse.jubula.client.ui.views.TestSuiteBrowser;
 import org.eclipse.jubula.client.ui.views.TreeBuilder;
 import org.eclipse.jubula.client.ui.widgets.StatusLineContributionItem;
 import org.eclipse.jubula.tools.constants.ConfigurationConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.eclipse.jubula.tools.exception.JBRuntimeException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.jarutils.IVersion;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
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
 import org.osgi.framework.BundleContext;
 
 
 /**
  * Base class for plug-ins that integrate with the Eclipse platform UI.
  * 
  * @author BREDEX GmbH
  * @created 06.07.2004
  */
 public class Plugin extends AbstractUIPlugin 
     implements IErrorListener, IProgressConsole {
     
     /** plugin id */
     public static final String PLUGIN_ID = "org.eclipse.jubula.client.ui"; //$NON-NLS-1$
     /**
      * <code>AUT_AGENT_DEFAULT_HOST</code>
      */
     private static final String AUT_AGENT_DEFAULT_HOST = "localhost"; //$NON-NLS-1$
     /** name of the Jubula console */
     private static final String JB_CONSOLE_NAME = "Console"; //$NON-NLS-1$
     /** for log messages */
     private static Log log = LogFactory.getLog(Plugin.class);
     /** single instance of plugin */
     private static Plugin plugin;
     /** StatusLineText */
     private static String statusLineText = Messages.StatusLine_NotConnected;
     /** StatusLineText */
     private static int connectionStatusIcon = Constants.DEFAULT_ICON;
     /** current workbench window */
     private static IWorkbenchWindow workbenchWindow;
     /** m_imageCache */
     private static Map < ImageDescriptor, Image > imageCache = 
         new HashMap < ImageDescriptor, Image > ();
     /** <code>CONNECTION_INFO_STATUSLINE_ITEM</code> */
     private static final String CONNECTION_INFO_STATUSLINE_ITEM = "connectionInfo"; //$NON-NLS-1$
     /** <code>AUT_TOOLKIT_STATUSLINE_ITEM</code> */
     private static final String AUT_TOOLKIT_STATUSLINE_ITEM = "autToolKitInfo"; //$NON-NLS-1$
     /** <code>LANG_STATUSLINE_ITEM</code> */
     private static final String LANG_STATUSLINE_ITEM = "lang"; //$NON-NLS-1$
     
     /** the client status */
     private ClientStatus m_status = ClientStatus.STARTING;
     /** single invisible root instance of view */
     private TestSuiteGUI m_testSuiteBrowserRootGUI = null;
     /** single invisible root instance of view */
     private TestCaseBrowserRootGUI m_testCaseBrowserRootGUI = null;
     /** a list with the unsaved editors*/
     private List < String > m_dirtyEditors;
     /** true, if preference store was initialized */
     private boolean m_isPrefStoreInitialized = false;
     /** the global progress controller */
     private IProgressListener m_progressController = new ProgressController();
     /** the console */
     private MessageConsole m_console;
     /** standard message stream for the console */
     private MessageConsoleStream m_standardMessageStream;
     /** error message stream for the console */
     private MessageConsoleStream m_errorMessageStream;
 
     /**
      * <code>m_treeViewerContainer</code> a list of tree viewer container
      */
     private List<ITreeViewerContainer> m_treeViewerContainer = 
         new ArrayList<ITreeViewerContainer>(2);
     
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
         try {
             ProgressEventDispatcher.removeProgressListener(
                 m_progressController);
         } finally {
             super.stop(context);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public IPreferenceStore getPreferenceStore() {
         if (!m_isPrefStoreInitialized) {
             m_isPrefStoreInitialized = true;
             initializeDefaultPluginPreferences(super.getPreferenceStore());
             initializePrefStoreHelper();
         }
         return super.getPreferenceStore();
     }
     
     /**
      * @return instance of plugin
      */
     public static Plugin getDefault() {
         return plugin;
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
      * @param fileName Object
      * @return Image
      */
     public static Image getImage(String fileName) {
         ImageDescriptor descriptor = null;
         descriptor = getImageDescriptor(fileName);
         //obtain the cached image corresponding to the descriptor
         Image image = imageCache.get(descriptor);
         if (image == null) {
             image = descriptor.createImage();
             imageCache.put(descriptor, image);
         }
         return image;
     }
 
     /**
      * @param name String
      * @return ImageDescriptor from URL
      */
     public static ImageDescriptor getImageDescriptor(String name) {
         return ImageUtils.getImageDescriptor(getDefault().getBundle(), name);
     }
 
     /**
      * TMP model
      * @return TestSuiteBrowserRootGUI
      */
     public TestSuiteGUI getTestSuiteBrowserRootGUI() {
         return m_testSuiteBrowserRootGUI;
     }
 
     /**
      * TMP model
      * @return TestCaseBrowserRootGUI
      */
     public TestCaseBrowserRootGUI getTestCaseBrowserRootGUI() {
         if (m_testCaseBrowserRootGUI == null) {
             IProjectPO currentProject = 
                 GeneralStorage.getInstance().getProject();
             if (currentProject != null) {
                 List<ISpecPersistable> specList = 
                     currentProject.getSpecObjCont().getSpecObjList();
                 List<IReusedProjectPO> reusedList = 
                     new ArrayList<IReusedProjectPO>(
                         currentProject.getUsedProjects());
                 Collections.sort(reusedList);
 
                 // Calls setTestCaseBrowserRootGUI()
                 TreeBuilder.buildTestCaseBrowserTree(specList, reusedList);
             }
         }
         return m_testCaseBrowserRootGUI;
     }
     
     /**
      * @return a list of currently available ITreeViewerContainer
      */
     public List<ITreeViewerContainer> getTreeViewerContainer() {
         return m_treeViewerContainer;
     }
 
     /**
      * TMP model
      * @param execTestSuite TestSuiteBrowserRootGUI
      */
     public void setTestSuiteBrowserRootGUI(TestSuiteGUI execTestSuite) {
         m_testSuiteBrowserRootGUI = execTestSuite;
     }
  
     /**
      * Model of the TestCases.
      * @param specTestSuite TestSuiteBrowserRootGUI
      */
     public void setTestCaseBrowserRootGUI(
             TestCaseBrowserRootGUI specTestSuite) {
         
         m_testCaseBrowserRootGUI = specTestSuite;
     }
 
     /**
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPluginPreferences(
             IPreferenceStore store) {
 
         initializeDefaultPreferencesJubulaBasic(store);
         initializeDefaultPreferencesObjectMapping(store);
         initializeDefaultPreferencesKeyBoardShortCuts(store);
         initializeDefaultPreferencesObservation(store);
         initializeDefaultPreferencesTestResults(store);
         initializeDefaultPreferencesTestData(store); 
         store.setDefault(Constants.PREF_MINORVERSION_KEY, -1);
         store.setDefault(Constants.PREF_MAJORVERSION_KEY, -1);
         if (store.getInt(Constants.PREF_MINORVERSION_KEY) == -1) {
             store.setValue(Constants.PREF_MINORVERSION_KEY,
                 IVersion.JB_PREF_MINOR_VERSION);
             store.setValue(Constants.PREF_MAJORVERSION_KEY,
                 IVersion.JB_PREF_MAJOR_VERSION);
         }
         store.setDefault(Constants.SERVER_SETTINGS_KEY, AUT_AGENT_DEFAULT_HOST
                 + StringConstants.COLON
                 + ConfigurationConstants.AUT_AGENT_DEFAULT_PORT);
         store.setDefault(Constants.ASKSTOPAUT_KEY,
                 Constants.ASKSTOPAUT_KEY_DEFAULT);
         store.setDefault(Constants.USER_KEY, Constants.USER_DEFAULT);
         store.setDefault(Constants.SCHEMA_KEY, Constants.SCHEMA_DEFAULT);
         store.setDefault(Constants.LINK_WITH_EDITOR_TCVIEW_KEY, 
                 Constants.LINK_WITH_EDITOR_TCVIEW_KEY_DEFAULT);
         store.setDefault(Constants.DATADIR_WS_KEY, 
                 Constants.DATADIR_WS_KEY_DEFAULT);
         store.setDefault(Constants.DATADIR_PATH_KEY, 
                 Platform.getLocation().toOSString());
     }
     
     /**
      * initialize the default preferences for a preference page 
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPreferencesJubulaBasic(
             IPreferenceStore store) {
         store.setDefault(Constants.TREEAUTOSCROLL_KEY,
                 Constants.TREEAUTOSCROLL_KEY_DEFAULT);
         store.setDefault(Constants.MINIMIZEONSUITESTART_KEY,
                 Constants.MINIMIZEONSUITESTART_KEY_DEFAULT);
         store.setDefault(Constants.SHOWORIGINALNAME_KEY, 
                 Constants.SHOWORIGINALNAME_KEY_DEFAULT);
         store.setDefault(Constants.PERSP_CHANGE_KEY,
                 Constants.PERSP_CHANGE_KEY_DEFAULT);
         store.setDefault(Constants.NODE_INSERT_KEY,
                 Constants.NODE_INSERT_KEY_DEFAULT);
         store.setDefault(Constants.SHOWCAPINFO_KEY,
                 Constants.SHOWCAPINFO_KEY_DEFAULT);
         store.setDefault(Constants.SHOW_TRANSIENT_CHILDREN_KEY,
                 Constants.SHOW_TRANSIENT_CHILDREN_KEY_DEFAULT);
         store.setDefault(Constants.REMEMBER_KEY,
                 Constants.REMEMBER_KEY_DEFAULT);
         store.setDefault(Constants.AUT_CONFIG_DIALOG_MODE,
             Constants.AUT_CONFIG_DIALOG_MODE_KEY_DEFAULT);
     }
     
     /**
      * initialize the default preferences for a preference page 
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPreferencesTestResults(
             IPreferenceStore store) {
         store.setDefault(Constants.GENERATEREPORT_KEY,
                 Constants.GENERATEREPORT_KEY_DEFAULT);
         store.setDefault(Constants.REPORTGENERATORSTYLE_KEY,
             Constants.REPORTGENERATORSTYLE_KEY_DEFAULT);
         store.setDefault(Constants.OPENRESULTVIEW_KEY,
                 Constants.OPENRESULTVIEW_KEY_DEFAULT);
         store.setDefault(Constants.TRACKRESULTS_KEY,
                 Constants.TRACKRESULTS_KEY_DEFAULT);
         store.setDefault(Constants.RESULTPATH_KEY,
                 Constants.RESULTPATH_KEY_DEFAULT);
         store.setDefault(Constants.MAX_NUMBER_OF_DAYS_KEY,
                 Constants.MAX_NUMBER_OF_DAYS_KEY_DEFAULT);
         store.setDefault(Constants.AUTO_SCREENSHOT_KEY, 
                 Constants.AUTO_SCREENSHOT_KEY_DEFAULT);
         store.setDefault(Constants.TEST_EXEC_RELEVANT,
                 Constants.TEST_EXECUTION_RELEVANT_DEFAULT);
         store.setDefault(Constants.TEST_EXECUTION_RELEVANT_REMEMBER_KEY,
                 Constants.TEST_EXECUTION_RELEVANT_REMEMBER_KEY_DEFAULT);
     }
     
     /**
      * initialize the default preferences for a preference page 
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPreferencesObjectMapping(
             IPreferenceStore store) {
         store.setDefault(Constants.SHOWCHILDCOUNT_KEY,
                 Constants.SHOWCHILDCOUNT_KEY_DEFAULT);
     }
     
     /**
      * initialize the default preferences for a preference page 
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPreferencesTestData(
             IPreferenceStore store) {
         store.setDefault(Constants.REFERENCE_CHAR_KEY,
                 String.valueOf(TestDataConstants.REFERENCE_CHAR_DEFAULT));
         store.setDefault(Constants.ESCAPE_CHAR_KEY,
                 String.valueOf(TestDataConstants.ESCAPE_CHAR_DEFAULT));
         store.setDefault(Constants.FUNCTION_CHAR_KEY,
                 String.valueOf(TestDataConstants.FUNCTION_CHAR_DEFAULT));
         store.setDefault(Constants.PATH_CHAR_KEY,
                 String.valueOf(TestDataConstants.PATH_CHAR_DEFAULT));
         store.setDefault(Constants.VALUE_CHAR_KEY,
                 String.valueOf(TestDataConstants.VALUE_CHAR_DEFAULT));
         store.setDefault(Constants.VARIABLE_CHAR_KEY,
                 String.valueOf(TestDataConstants.VARIABLE_CHAR_DEFAULT));
     } 
     
     /**
      * initialize the default preferences for a preference page 
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPreferencesKeyBoardShortCuts(
             IPreferenceStore store) {
         store.setDefault(Constants.MAPPING_MOD_KEY,
                 Constants.MAPPINGMOD1_KEY_DEFAULT);
         store.setDefault(Constants.MAPPING_TRIGGER_KEY,
                 Constants.MAPPING_TRIGGER_DEFAULT);
         store.setDefault(Constants.MAPPING_TRIGGER_TYPE_KEY,
                 Constants.MAPPING_TRIGGER_TYPE_DEFAULT);
     }
     
     /**
      * initialize the default preferences for a preference page 
      * @param store IPreferenceStore
      */
     private static void initializeDefaultPreferencesObservation(
             IPreferenceStore store) {
         store.setDefault(Constants.RECORDMOD_COMP_MODS_KEY,
                 Constants.RECORDMOD1_KEY_DEFAULT);
         store.setDefault(Constants.RECORDMOD_COMP_KEY_KEY,
                 Constants.RECORDMOD2_KEY_DEFAULT);
         store.setDefault(Constants.RECORDMOD_APPL_MODS_KEY,
                 Constants.RECORDMOD_APPL_MODS_DEFAULT);
         store.setDefault(Constants.RECORDMOD_APPL_KEY_KEY,
                 Constants.RECORDMOD_APPL_KEY_DEFAULT);        
         
         store.setDefault(Constants.CHECKMODE_MODS_KEY,
                 Constants.CHECKMODE_MODS_KEY_DEFAULT);
         store.setDefault(Constants.CHECKMODE_KEY_KEY,
                 Constants.CHECKMODE_KEY_KEY_DEFAULT);
         store.setDefault(Constants.CHECKCOMP_MODS_KEY,
                 Constants.CHECKCOMP_MODS_KEY_DEFAULT);
         store.setDefault(Constants.CHECKCOMP_KEY_KEY,
                 Constants.CHECKCOMP_KEY_KEY_DEFAULT);
         
         store.setDefault(Constants.SHOWRECORDDIALOG_KEY,
                 Constants.SHOWRECORDDIALOG_KEY_DEFAULT);
         
         store.setDefault(Constants.SINGLELINETRIGGER_KEY,
                 Constants.SINGLELINETRIGGER_KEY_DEFAULT);
         store.setDefault(Constants.MULTILINETRIGGER_KEY,
                 Constants.MULTILINETRIGGER_KEY_DEFAULT);
         
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
         // So lets ask all IGDEditors.
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
         //           depends on and uses methods from org.eclipse.jubula.client.ui.
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
                 Utils.createMessageDialog(new JBException(
                         NLS.bind(Messages.PluginCantOpenView + viewID,
                         MessageIDs.E_CLASS_NOT_FOUND), null, null));
             }
             return vp;
         }
         return null;
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
      * @param treeViewID the ID of the tree view
      * @return the selection in the tree view or an <b>empty selection</b>
      */
     public static IStructuredSelection getTreeViewSelection(String treeViewID) {
         Validate.notNull(treeViewID);
         IStructuredSelection returnValue = StructuredSelection.EMPTY;
         ViewPart treeView = null;
         if (treeViewID.equals(Constants.TC_BROWSER_ID)) {
             treeView = (TestCaseBrowser)getView(treeViewID);
         } else if (treeViewID.equals(Constants.TS_BROWSER_ID)) {
             treeView = (TestSuiteBrowser)getView(treeViewID);
         } else if (treeViewID.equals(Constants.TESTRE_ID)) {
             treeView = (TestResultTreeView)getView(treeViewID);
         }
         
         if (treeView != null 
                 && (treeView.getViewSite().getSelectionProvider().getSelection()
                     instanceof IStructuredSelection)) {
             returnValue = (IStructuredSelection)
                 treeView.getViewSite().getSelectionProvider().getSelection();
         }
         return returnValue;
     }
 
     /**
      * @param area The composite.
      * creates a separator
      */
     public static void createSeparator(Composite area) {
         Label label = new Label(area, SWT.SEPARATOR | SWT.HORIZONTAL);
         GridData gridData = new GridData();
         gridData.grabExcessHorizontalSpace = true;
         gridData.horizontalAlignment = GridData.FILL;
         label.setLayoutData(gridData);
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
      * @return A list of unsaved editors.
      */
     public List < String > getDirtyEditorNames() {
         return m_dirtyEditors;
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
      * @param dirtyEditors The dirty editors list to set.
      */
     public void setDirtyEditorNames(List < String > dirtyEditors) {
         m_dirtyEditors = dirtyEditors;
     }
 
     /**
      * @return the active gd editor or null if no gd editor is active
      */
     public AbstractJBEditor getActiveGDEditor() {
         IWorkbenchPage activePage = getActivePage();
         if (activePage == null) { // during shutdown
             return null;
         }
         IEditorPart iEditorPart = getActiveEditor();
         if (iEditorPart != null 
                 && iEditorPart instanceof AbstractJBEditor) {
             return (AbstractJBEditor)iEditorPart;
         }
         return null;              
     }
     
     /**
      * @return the active TestCaseEditor or null if no TC editor is active
      */
     public AbstractTestCaseEditor getActiveTCEditor() {
         if (getActiveGDEditor() instanceof AbstractTestCaseEditor) {
             return (AbstractTestCaseEditor)getActiveGDEditor();
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
      * Closes all opened Jubula editors (ex. TCE, TSE, OME). This method 
      * must be called from the GUI thread. If it is called from a thread that 
      * is *not* the GUI thread, it will do nothing.
      */
     public static void closeAllOpenedJubulaEditors() {
         IWorkbenchPage activePage = getActivePage();
         if (activePage != null) {
             Set<IEditorReference> editorRefSet = 
                 new HashSet<IEditorReference>();
             for (IEditorReference editorRef 
                     : activePage.getEditorReferences()) {
 
                 if (editorRef.getEditor(true) instanceof IJBPart) {
                     editorRefSet.add(editorRef);
                 }
             }
             activePage.closeEditors(
                     editorRefSet.toArray(
                             new IEditorReference[editorRefSet.size()]), 
                     false);
         }
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
         boolean isDirtyStar = false;
         IEditorPart[] editors = getActivePage().getDirtyEditors();
         setDirtyEditorNames(new ArrayList < String > ());
         for (int t = 0; t < editors.length; t++) {
             if (editors[t].isDirty()) {
                 isDirtyStar = true;
                 m_dirtyEditors.add(editors[t].getTitle());
             }
         }
         return isDirtyStar;
     }
 
     /**
      * <p>* opens the ListSelectionDialog and saves all selected editors, which are dirty</p>
      * <p>* starts the AUT, if in the dialog the OK button was pressed</p>
      * @return boolean isSaved?
      */
     public boolean showSaveEditorDialog() {
         ListSelectionDialog dialog = new ListSelectionDialog(getShell(),
                 m_dirtyEditors, new DirtyStarListContentProvider(),
                 new DirtyStarListLabelProvider(),
                 Messages.StartSuiteActionMessage);
         dialog.setTitle(Messages.StartSuiteActionTitle);
         dialog.setInitialSelections(m_dirtyEditors.toArray());
         dialog.open();
         // if OK was pressed in the dialog
         // all selected editors will saved
         if (dialog.getReturnCode() == Window.OK) {
             for (int t = 0; t < dialog.getResult().length; t++) {
                 ((getEditorByTitle(dialog.getResult()[t].toString())))
                         .doSave(new NullProgressMonitor());
             }
             setNormalCursor();
             return true;
         }
         setNormalCursor();
         return false;
     }
 
     /** Sets the hourglas cursor. */
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
                     Utils.createMessageDialog(gdEx);
                 } else if (e instanceof JBException) {
                     JBException gdEx = (JBException)e;
                     Utils.createMessageDialog(gdEx, null, null);
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
                     Utils.createMessageDialog(MessageIDs.E_DATABASE_GENERAL,
                         null, new String [] {e.getLocalizedMessage()});
                 } else {
                     Utils.createMessageDialog(new JBFatalException(e,
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
                 // check for
                 // org.eclipse.ui.views.markers.internal.MarkerAdapter.buildAllMarkers
                 if (el.getClassName().indexOf("MarkerAdapter") != -1) { //$NON-NLS-1$
                     return true;
                 }
                 // double click error in help view
                 if (el.getClassName().indexOf("EngineResultSection") != -1) { //$NON-NLS-1$
                     return true;
                 }
                 // Context Sensitive Help when DSV Cell Editor is open #3291
                 if (el.getClassName().indexOf("ContextHelpPart") != -1) { //$NON-NLS-1$
                     return true;
                 }
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
         plugin = this;
         Platform.addLogListener(new ILogListener() {
             public void logging(IStatus status, String pluginId) {
                 if (status.getException() instanceof RuntimeException) {
                     handleError(status.getException());
                 }
             }
         });
         ErrorMessagePresenter.setPresenter(new IErrorMessagePresenter() {
             public void showErrorMessage(JBException ex, Object[] params,
                     String[] details) {
                 Utils.createMessageDialog(ex, params, details);
             }
 
             public void showErrorMessage(Integer messageID, Object[] params,
                     String[] details) {
                 Utils.createMessageDialog(messageID, params, details);
             }
         });
         
         new Thread(new Runnable() {
             public void run() {
                 // init (java)available languages
                 Languages.getInstance();
             }
         }).start();
         registerPermanentServices();
         super.start(context);
     }
 
     /**
      * register business processes and service that should be available
      * while complete Jubula live cycle
      * - ProgressController
      * - ComponentNamesListBP
      */
     private void registerPermanentServices() {
         // register progress controller
         ProgressEventDispatcher.addProgressListener(
             m_progressController);
 
         // register service for checking completeness
         CompletenessBP.getInstance();
 
         // register Component Name reuse tracker
         ComponentNameReuseBP.getInstance();
         
         // register service for toolkit
         final ToolkitBP toolkitBP = ToolkitBP.getInstance();
         DataEventDispatcher.getInstance().addProjectLoadedListener(
             toolkitBP, true);
         DataEventDispatcher.getInstance().addDataChangedListener(
             toolkitBP, true);
     }
 
     /**
      * Initializes the <code>PrefStoreHelper</code> to set the test data preferences
      */
     private void initializePrefStoreHelper() {
         IPreferenceStore store = super.getPreferenceStore();
         PrefStoreHelper helper = PrefStoreHelper.getInstance();
         helper.setEscapeChar(store.getString(Constants.ESCAPE_CHAR_KEY));
         helper.setFunctionChar(store.getString(Constants.FUNCTION_CHAR_KEY));
         helper.setReferenceChar(store.getString(Constants.REFERENCE_CHAR_KEY));
         helper.setValueChar(store.getString(Constants.VALUE_CHAR_KEY));
         helper.setVariableChar(store.getString(Constants.VARIABLE_CHAR_KEY));
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
                 getStatusLineManager().setErrorMessage(getImage("longRunning.gif"), //$NON-NLS-1$
                     message); 
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
      * Sets the project name shown in the titlebar to the given name and version
      * number.
      * This method must be called from within the GUI thread.
      * 
      * @param projectName  The new name to show in the titlebar. May be 
      *                     <code>null</code>, which indicates that no project
      *                     name should be displayed.
      * @param majorVersion The major version number to show in the titlebar.
      *                     This value is ignored if <code>projectName</code> is
      *                     <code>null</code>. This value may only be 
      *                     <code>null</code> if <code>projectName</code> is also
      *                     <code>null</code>.
      * @param minorVersion The minor version number to show in the titlebar.
      *                     This value is ignored if <code>projectName</code> is
      *                     <code>null</code>. This value may only be 
      *                     <code>null</code> if <code>projectName</code> is also
      *                     <code>null</code>.
      */
     public static void setProjectNameInTitlebar(String projectName, 
             Integer majorVersion, Integer minorVersion) {
         
         StringBuilder sb = new StringBuilder(
                 Messages.ConstantsDefaultTextValue);
         
         Hibernator hibernator = Hibernator.instance();
         if (hibernator != null) {
             String user = hibernator.getCurrentDBUser();
             if (user != null && user.length() != 0) {
                 sb.append(StringConstants.SPACE
                         + StringConstants.MINUS
                         + StringConstants.SPACE)
                         .append(user);
             }
         }
         
         if (projectName != null && projectName.length() != 0) {
             sb.append(StringConstants.SPACE
                     + StringConstants.MINUS
                     + StringConstants.SPACE).append(projectName);
             sb.append(StringConstants.SPACE).append(majorVersion.intValue())
             .append(StringConstants.DOT);
             sb.append(minorVersion.intValue());
         }
         
         getActiveWorkbenchWindowShell().setText(sb.toString());
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
 }

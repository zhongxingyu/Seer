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
 package org.eclipse.jubula.client.ui.rcp.utils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.OMState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ProjectState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.RecordModeState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.TestresultState;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.utils.Languages;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.ProblemsBP;
 import org.eclipse.jubula.client.ui.rcp.controllers.MultipleTCBTracker;
 import org.eclipse.jubula.client.ui.rcp.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.rcp.editors.PersistableEditorInput;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.views.TestCaseBrowser;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.search.ui.NewSearchUI;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Utility class.
  *
  * @author BREDEX GmbH
  * @created 15.02.2005
  */
 @SuppressWarnings("synthetic-access")
 public class Utils {
 
     /** the logger */
     private static Logger log = LoggerFactory.getLogger(Utils.class);
     
     /**
      * Constructor
      */
     private Utils() {
         // do nothing
     }
 
     /**;
      * @return locale object for default language or default
      */
     public static Locale getDefaultLocale() {
         final IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project.getDefaultLanguage() != null) {
             return project
                 .getDefaultLanguage();
         }
         return Locale.getDefault();            
     }
     
     /**
      * @return True, if the server is localhost. False, otherwise.
      */
     public static boolean isLocalhost() {
         IPreferenceStore prefStore = Plugin.getDefault().getPreferenceStore();
         String serverPort = 
                 prefStore.getString(Constants.AUT_AGENT_SETTINGS_KEY);
         String server = serverPort.split(StringConstants.COLON)[0];
         if (server.equals(Messages.UtilsLocalhost1)
             || server.equals(Messages.UtilsLocalhost3)
             || server.startsWith(Messages.UtilsLocalhost2)) {
             return true;
         }           
         return false;
     }
     
     /**
      * Returns the active perspective descriptor or <code>null</code>.
      * 
      * @param activePage
      *            the currently active page - may also be null
      * @return an <code>IPerspectiveDescriptor</code> value. The active
      *         perspective for the currently active page.
      */
     private static IPerspectiveDescriptor getActivePerspective(
             IWorkbenchPage activePage) {
         if (activePage != null) {
             return activePage.getPerspective();
         }
         return null;
     }
     
     /**
      * Opens a perspective with the given ID.
      * @param perspectiveID The ID of the perspective to open.
      * @return True, if the user wants to change the perspective, false otherwise.
      */
     public static boolean openPerspective(String perspectiveID) {
         IWorkbench worbench = PlatformUI.getWorkbench();
         IWorkbenchWindow activeWindow = worbench.getActiveWorkbenchWindow();
         try {
             IPerspectiveDescriptor activePerspective = getActivePerspective(
                     activeWindow.getActivePage());
             if (activePerspective != null
                     && activePerspective.getId().equals(perspectiveID)) {
                 return true;
             }
             final IPreferenceStore preferenceStore = Plugin.getDefault()
                     .getPreferenceStore();
             int value = preferenceStore.getInt(Constants.PERSP_CHANGE_KEY);
             if (value == Constants.PERSPECTIVE_CHANGE_YES) {
                 worbench.showPerspective(perspectiveID, activeWindow);
                 return true;
             } else if (value == Constants.PERSPECTIVE_CHANGE_NO) {
                 return true;
             }
             // if --> value = Constants.PERSPECTIVE_CHANGE_PROMPT:
             String perspectiveName = StringConstants.EMPTY;
             if (perspectiveID.equals(Constants.SPEC_PERSPECTIVE)) {
                 perspectiveName = Messages.UtilsSpecPerspective;
             } else {
                 perspectiveName = Messages.UtilsExecPerspective;
             }
             final int returnCodeYES = 256; // since Eclipse3.2 (not 0)
             final int returnCodeNO = 257; // since Eclipse3.2 (not 1)
             final int returnCodeCANCEL = -1;
             MessageDialogWithToggle dialog = new MessageDialogWithToggle(
                     Plugin.getShell(), Messages.UtilsTitle, null, NLS.bind(
                             Messages.UtilsQuestion, perspectiveName),
                     MessageDialog.QUESTION, new String[] { Messages.UtilsYes,
                         Messages.UtilsNo }, 0, Messages.UtilsRemember,
                     false) {
                 /**
                  * {@inheritDoc}
                  */
                 protected void buttonPressed(int buttonId) {
                     super.buttonPressed(buttonId);
                     preferenceStore.setValue(Constants.REMEMBER_KEY,
                             getToggleState());
                     int val = Constants.PERSPECTIVE_CHANGE_PROMPT;
                     if (getToggleState() && getReturnCode() == returnCodeNO) {
                         val = Constants.PERSPECTIVE_CHANGE_NO;
                     } else if (getToggleState()
                             && getReturnCode() == returnCodeYES) {
                         val = Constants.PERSPECTIVE_CHANGE_YES;
                     }
                     preferenceStore.setValue(Constants.PERSP_CHANGE_KEY, val);
                 }
             };
             dialog.create();
             DialogUtils.setWidgetNameForModalDialog(dialog);
             dialog.open();
             if (dialog.getReturnCode() == returnCodeNO) {
                 return true;
             } else if (dialog.getReturnCode() == returnCodeCANCEL) {
                 return false;
             }
             worbench.showPerspective(perspectiveID, activeWindow);
         } catch (WorkbenchException e) {
             StringBuilder msg = new StringBuilder();
             msg.append(Messages.CannotOpenThePerspective)
                     .append(StringConstants.COLON)
                     .append(StringConstants.SPACE).append(perspectiveID)
                     .append(StringConstants.LEFT_PARENTHESES).append(e)
                     .append(StringConstants.RIGHT_PARENTHESES)
                     .append(StringConstants.DOT);
             log.error(msg.toString());
             ErrorHandlingUtil.createMessageDialog(MessageIDs.E_NO_PERSPECTIVE);
             return false;
         }
         return true;
     }
     
     /**
      * Gets a List of expanded items of the given TreeViewer.
      * @param tv the TreeViewer
      * @return a List of expanded items
      */
     public static List<Object> getExpandedTreeItems(TreeViewer tv) {
         Object[] expandedElems = tv.getExpandedElements();
         return new ArrayList<Object>(Arrays.asList(expandedElems));
     }
     
     /**
      * @return the last browsed path.
      */
     public static String getLastDirPath() {
         return Plugin.getDefault().getPreferenceStore().getString(
             Constants.START_BROWSE_PATH_KEY);
     }
     
     /**
      * Stores the last browsed path.
      * @param path The path to store.
      */
     public static void storeLastDirPath(String path) {
         Plugin.getDefault().getPreferenceStore().setValue(
             Constants.START_BROWSE_PATH_KEY, path);
     }
     
     /**
      * @return A list of all available languages.
      */
     public static List<String> getAvailableLanguages() {
         Languages langUtil = Languages.getInstance();
         java.util.List<String> list = new ArrayList<String>();
         for (Locale locale : langUtil.getSuppLangList()) {
             list.add(langUtil.getDisplayString(locale));
         }    
         return list;
     }
     
     /**
      * clears the content of client
      */
     public static void clearClient() {
         clearClient(false);
     }
     
     /**
      * clears the content of client
      * 
      * @param alsoProjectIndependent
      *            whether also project independent editors should be closed such
      *            as the testresultviewer
      */
     public static void clearClient(final boolean alsoProjectIndependent) {
         final DataEventDispatcher ded = DataEventDispatcher.getInstance();
         TestExecution.getInstance().stopExecution();
         GeneralStorage gs = GeneralStorage.getInstance();
         if (gs != null && Persistor.instance() != null) {
             IProjectPO currProj = gs.getProject();
             if (currProj != null) {
                 gs.setProject(null);
             }
             gs.reset();
         }
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 final ProblemsBP problemsBP = ProblemsBP.getInstance();
                 problemsBP.clearOldProblems();
                 problemsBP.cleanupProblems();
                 TestExecutionContributor.getInstance().getClientTest()
                         .resetToTesting();
                 ded.fireRecordModeStateChanged(RecordModeState.notRunning);
                 ded.fireOMStateChanged(OMState.notRunning);
                 ded.fireProjectStateChanged(ProjectState.closed);
                 Plugin.closeAllOpenedJubulaEditors(alsoProjectIndependent);
                 ded.fireTestresultChanged(TestresultState.Refresh);
                 setTreeViewerInputNull(Constants.TESTRE_ID);
                 for (TestCaseBrowser tcb : MultipleTCBTracker.getInstance()
                         .getOpenTCBs()) {
                     tcb.getTreeViewer().setInput(null);
                 }
                 setTreeViewerInputNull(Constants.TS_BROWSER_ID);
                 setTreeViewerInputNull(Constants.COMPNAMEBROWSER_ID);
                 clearAnalyzeResultPage();
             }
         });
         ded.fireProjectLoadedListener(new NullProgressMonitor());
     }
     /**
      * Clears the ResultPage of the Analyze-Plugin
      */
     private static void clearAnalyzeResultPage() {
 
         ISearchQuery[] querry = NewSearchUI.getQueries();
         for (int i = 0; i < querry.length; i++) {
             NewSearchUI.removeQuery(querry[i]);
         }
     }
     /**
      * @param viewID
      *            the id of the view to set it's tree viewer input to null.
      */
     private static void setTreeViewerInputNull(String viewID) {
         IViewPart view = Plugin.getView(viewID);
         if (view instanceof ITreeViewerContainer) {
             ((ITreeViewerContainer)view).getTreeViewer().setInput(null);
         }
     }
     
     /**
      * Returns the IEditorPart for the given node or null if no editor is
      * opend for the given node
      * 
      * @param po
      *            the persistent object of the wanted editor
      * @return the IEditorPart or null if no editor found
      */
     public static IEditorPart getEditorByPO(IPersistentObject po) {
         IEditorReference editorRef = getEditorRefByPO(po);
         if (editorRef != null) {
             return editorRef.getEditor(false);
         }
         return null;
     }
     /**
      * Returns the IEditorReference for the given node or null if no editor is
      * opend for the given node
      * 
      * @param po
      *            the persistent object of the wanted editor
      * @return the IEditorReference or null if no editor found
      */
     public static IEditorReference getEditorRefByPO(IPersistentObject po) {
         for (IEditorReference editorRef : Plugin.getAllEditors()) {
             PersistableEditorInput pei = null;
             try {
                 pei = (PersistableEditorInput)editorRef.getEditorInput()
                     .getAdapter(PersistableEditorInput.class);
             } catch (PartInitException e) {
                 // do nothing here
             }
             if (pei != null && pei.getNode().equals(po)) {
                 return editorRef;
             }
         }
         return null;
     }
     /**
      * @param exec the exec TC to search in
      * @param name the comp name to get the type for
      * @return the comp type
      */
     public static String getComponentType(IExecTestCasePO exec, String name) {
         return getComponentType(exec, name, false);
     }
     
     /**
      * @param exec the exec TC to search in
      * @param name the comp name to get the type for
      * @param checkForPropagate true if propagation should be checked
      * @return the comp type
      */
     private static String getComponentType(IExecTestCasePO exec, String name, 
             boolean checkForPropagate) {
         
         String type = StringConstants.EMPTY;
         ICompNamesPairPO compNamesPair = exec.getCompNamesPair(name);
         if (compNamesPair != null) {
//            // at first: check the compType directly
//            type = compNamesPair.getType();
//            if (!Constants.EMPTY_STRING.equals(type)) {
//                return type;
//            }
            // if not compType, then search recursively
             if (checkForPropagate) {
                 if (compNamesPair.isPropagated()) {
                     type = searchCompTypeInTree(exec, name);
                     compNamesPair.setType(type);
                     return type;
                 }
             } else {
                 return searchCompTypeInTree(exec, name);
             }
         } 
         if (exec.getCompNamesPairs().isEmpty()) {
             return searchCompTypeInTree(exec, name);
         } 
         for (ICompNamesPairPO pair : exec.getCompNamesPairs()) {
             if (pair.getSecondName() != null
                     && pair.getSecondName().equals(name)) {
                 
                 if (checkForPropagate) {
                     if (pair.isPropagated()) {
                         return getComponentType(exec, pair.getFirstName(), 
                                 true);
                     }
                 } else {
                     return getComponentType(exec, pair.getFirstName(), true);
                 }
             }
         }
         return StringConstants.EMPTY;
     }
 
     /**
      * @param exec the exec TC to search in
      * @param name the comp name to get the type for
      * @return the comp type
      */
     private static String searchCompTypeInTree(IExecTestCasePO exec, 
             String name) {
         
         String type = StringConstants.EMPTY;
         if (exec.getSpecTestCase() != null) {
             for (Object node 
                     : exec.getSpecTestCase().getUnmodifiableNodeList()) {
                 if (node instanceof ICapPO) {
                     ICapPO cap = (ICapPO)node;
                     if (name.equals(cap.getComponentName())) {
                         return cap.getComponentType();
                     }
                 } else if (node instanceof IExecTestCasePO) {
                     type = getComponentType((IExecTestCasePO)node, name, true);
                     if (!StringConstants.EMPTY.equals(type)) {
                         break;
                     }
                 }
             }
         }
         return type;
     }
     
     /**
      * Copies the params of autConfigOrig to autConfigCopy.
      * @param autConfigOrig the orignal autconfig
      * @param autConfigCopy the copy of the original autconfig
      */
     public static void makeAutConfigCopy(Map<String, String> autConfigOrig, 
         Map<String, String> autConfigCopy) {
         
         autConfigCopy.clear();
         final Set<String> autConfigKeys = autConfigOrig.keySet();
         for (String key : autConfigKeys) {
             String value = autConfigOrig.get(key);
             if (value != null && value.length() > 0) {
                 autConfigCopy.put(key, value);
             }
         }
     }
 }

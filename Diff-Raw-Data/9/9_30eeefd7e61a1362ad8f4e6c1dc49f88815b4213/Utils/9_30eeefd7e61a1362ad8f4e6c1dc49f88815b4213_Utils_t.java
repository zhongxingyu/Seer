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
 package org.eclipse.jubula.client.ui.utils;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.OMState;
import org.eclipse.jubula.client.core.events.DataEventDispatcher.RecordModeState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.TestresultState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.utils.Languages;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.controllers.SpecRefreshTreeIterator;
import org.eclipse.jubula.client.ui.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.controllers.TreeIterator;
 import org.eclipse.jubula.client.ui.editors.PersistableEditorInput;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.model.CapGUI;
 import org.eclipse.jubula.client.ui.model.ExecTestCaseGUI;
 import org.eclipse.jubula.client.ui.model.GuiNode;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBRuntimeException;
 import org.eclipse.jubula.tools.messagehandling.Message;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 
 
 /**
  * Utility class.
  *
  * @author BREDEX GmbH
  * @created 15.02.2005
  */
 @SuppressWarnings("synthetic-access")
 public class Utils {
 
     /** the logger */
     private static Log log = LogFactory.getLog(Utils.class);
     
     /** The error dialog */
     private static Dialog dlg = null;
     
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
         String serverPort = prefStore.getString(Constants.SERVER_SETTINGS_KEY);
         String server = serverPort.split(StringConstants.COLON)[0];
         if (server.equals(Messages.UtilsLocalhost1)
             || server.equals(Messages.UtilsLocalhost3)
             || server.startsWith(Messages.UtilsLocalhost2)) {
             return true;
         }           
         return false;
     }
     
     /**
      * Opens a perspective with the given ID.
      * @param perspectiveID The ID of the perspective to open.
      * @return True, if the user wants to change the perspective, false otherwise.
      */
     public static boolean openPerspective(String perspectiveID) {
         try {
             if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                     .getActivePage().getPerspective().getId()
                     .equals(perspectiveID)) {
                 return true;
             }
             int value = Plugin.getDefault().getPreferenceStore()
                     .getInt(Constants.PERSP_CHANGE_KEY);
             if (value == Constants.PERSPECTIVE_CHANGE_YES) {
                 PlatformUI.getWorkbench().showPerspective(perspectiveID,
                         PlatformUI.getWorkbench().getActiveWorkbenchWindow());
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
                             Messages.UtilsQuestion,
                             new Object[] { perspectiveName }),
                     MessageDialog.QUESTION, new String[] { Messages.UtilsYes,
                         Messages.UtilsNo }, 0, Messages.UtilsRemember,
                     false) {
                 /**
                  * {@inheritDoc}
                  */
                 protected void buttonPressed(int buttonId) {
                     super.buttonPressed(buttonId);
                     Plugin.getDefault().getPreferenceStore()
                             .setValue(Constants.REMEMBER_KEY, getToggleState());
                     int val = Constants.PERSPECTIVE_CHANGE_PROMPT;
                     if (getToggleState() && getReturnCode() == returnCodeNO) {
                         val = Constants.PERSPECTIVE_CHANGE_NO;
                     } else if (getToggleState()
                             && getReturnCode() == returnCodeYES) {
                         val = Constants.PERSPECTIVE_CHANGE_YES;
                     }
                     Plugin.getDefault().getPreferenceStore()
                             .setValue(Constants.PERSP_CHANGE_KEY, val);
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
             PlatformUI.getWorkbench().showPerspective(perspectiveID,
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow());
         } catch (WorkbenchException e) {
             StringBuilder msg = new StringBuilder();
             msg.append(Messages.CannotOpenThePerspective)
                     .append(StringConstants.COLON)
                     .append(StringConstants.SPACE).append(perspectiveID)
                     .append(StringConstants.LEFT_PARENTHESES).append(e)
                     .append(StringConstants.RIGHT_PARENTHESES)
                     .append(StringConstants.DOT);
             log.error(msg.toString());
             createMessageDialog(MessageIDs.E_NO_PERSPECTIVE);
             return false;
         }
         return true;
     }
     
     /**
      * A List of selected items of the given TreeViewer
      * @param tv the TreeViewer
      * @return a List of selected items
      */
     public static List<NodeSelection> getSelectedTreeItems(TreeViewer tv) {
         List<NodeSelection> selElements = new ArrayList<NodeSelection>();
         if (tv.getSelection() instanceof IStructuredSelection) {
             IStructuredSelection selection = 
                 (IStructuredSelection)tv.getSelection();
             Iterator selIt = selection.iterator();
             while (selIt.hasNext()) {
                 GuiNode node = (GuiNode)selIt.next();
                 if (node instanceof CapGUI) {
                     CapGUI cap = (CapGUI)node;
                     if (cap.getParentNode().getParentNode() != null 
                             && cap.getParentNode().getParentNode() 
                             instanceof ExecTestCaseGUI) {
                         
                         selElements.add(new NodeSelection(cap.getParentNode()
                                 .getParentNode().getContent(), 
                                 (ICapPO)cap.getContent()));
                     } else {
                         selElements.add(new NodeSelection(cap.getParentNode()
                                 .getContent(), (ICapPO)cap.getContent()));
                     }
                 } else {
                     selElements.add(new NodeSelection(node.getContent(), null));
                 }
             }
         }
         return selElements;
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
      * Restores the state (selected items, expanded items) of the given
      * TreeViewer of <b>GUINodes</b> with the given Lists of expanded 
      * and selected items.
      * @param tv the TreeViewer
      * @param expandedItems a List of expanded items
      * @param selectedItems a List of selected items
      */
     public static void restoreTreeState(TreeViewer tv, 
         List<Object> expandedItems, 
         List<NodeSelection> selectedItems) {
         
         GuiNode newRootGui = (GuiNode)tv.getInput();
         TreeIterator treeIter = new SpecRefreshTreeIterator(newRootGui);
         List<GuiNode> elemsToSelect = new ArrayList<GuiNode>(selectedItems
             .size());
         boolean hasExpandedNode = false;
         while (treeIter.hasNext()) {
             GuiNode newNode = treeIter.next();
             final INodePO content = newNode.getContent();
             for (Object expElement : expandedItems) {
                 if (content != null && content.equals(
                     ((GuiNode)expElement).getContent())) {
                     hasExpandedNode = true;
                     tv.setExpandedState(newNode, true);
                     tv.expandToLevel(newNode, 1); 
                 }
             }
             for (NodeSelection selElem : selectedItems) {
                 if (content != null && content.equals(selElem.getNode())) {
                     if (selElem.getCap() != null) {
                         for (GuiNode guiNode : newNode.getChildren()) {
                             if (guiNode.getContent().equals(selElem.getCap())) {
                                 elemsToSelect.add(guiNode);
                                 break;
                             }
                         }
                     } else {
                         elemsToSelect.add(newNode);
                     }
                 }
             }
         }
         if (!hasExpandedNode) {
             tv.expandToLevel(2);
         }
         tv.setSelection(new StructuredSelection(elemsToSelect));
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
         final DataEventDispatcher ded = DataEventDispatcher.getInstance();
         TestExecution.getInstance().stopExecution();
         GeneralStorage gs = GeneralStorage.getInstance();
         if (gs != null && Hibernator.instance() != null) {
             IProjectPO currProj = gs.getProject();
             if (currProj != null) {
                 gs.setProject(null);
                 ded.fireDataChangedListener(currProj, DataState.Deleted,
                         UpdateState.all);
             }
             gs.reset();
         }
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                TestExecutionContributor.getInstance().getClientTest()
                        .resetToTesting();
                ded.fireRecordModeStateChanged(RecordModeState.notRunning);
                ded.fireOMStateChanged(OMState.notRunning);
                 Plugin.setProjectNameInTitlebar(null, null, null);
                 Plugin.closeAllOpenedJubulaEditors();
                 ded.fireTestresultChanged(TestresultState.Refresh);
                 setTreeViewerInputNull(Constants.TESTRE_ID);
                 setTreeViewerInputNull(Constants.TC_BROWSER_ID);
                 setTreeViewerInputNull(Constants.TS_BROWSER_ID);
                 setTreeViewerInputNull(Constants.COMPNAMEBROWSER_ID);
             }
         });
         ded.fireProjectLoadedListener(new NullProgressMonitor());
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
      * Line-feeds the given String array at \n
      * @param strArray the string to line feed
      * @return the line feeded strings as an array
      */
     private static String[] lineFeed(String[] strArray) {
         List<String> strList = new ArrayList<String>();
         for (String str : strArray) {
             StringTokenizer tok = new StringTokenizer(str, 
                     StringConstants.NEWLINE);
             while (tok.hasMoreElements()) {
                 strList.add(tok.nextToken());
             }
         }
         return strList.toArray(new String[strList.size()]);
     }
     
     /**
      * Open the message dialog and logs the JBException.
      * @param ex the actual JBRuntimeException
      * @return the dialog.
      */
     public static Dialog createMessageDialog(JBRuntimeException ex) {
         Integer messageID = ex.getErrorId();
         Message m = MessageIDs.getMessageObject(messageID);
         if (m != null && m.getSeverity() == Message.ERROR) {
             log.error(Messages.AnErrorHasOccurred + StringConstants.DOT, ex);
         }
         return createMessageDialog(messageID, null,
                 getStackTrace(ex.getCausedBy()));
     }
     
     /**
      * @param throwable the throwable to get the stack trace from
      * @return the stack trace as a string
      */
     public static String[] getStackTrace(Throwable throwable) {
         if (throwable != null) {
             Writer writer = new StringWriter();
             PrintWriter printWriter = new PrintWriter(writer);
             throwable.printStackTrace(printWriter);
             return writer.toString().split(StringConstants.NEWLINE);
         }
         return null;
     }
     
     /**
      * Open the message dialog and logs the JBException.
      * @param ex the actual JBException
      * @return the dialog.
      */
     public static Dialog createMessageDialog(JBException ex) {
         return createMessageDialog(ex, null, null);
     }
     
     
     /**
      * Open the message dialog and logs the JBException.
      * @param ex the actual JBException
      * @param params Parameter of the message text or null, if not needed.
      * @param details use null, or overwrite in MessageIDs hardcoded details.
      * @return the dialog.
      */
     public static Dialog createMessageDialog(JBException ex, 
             Object[] params, String[] details) {
         
         Integer messageID = ex.getErrorId();
         Message m = MessageIDs.getMessageObject(messageID);
         if (m != null && m.getSeverity() == Message.ERROR) {
             log.error(Messages.AnErrorHasOccurred + StringConstants.DOT, ex);
         }
         return createMessageDialog(messageID, params, details);
     }
 
     /**
      * Open the message dialog.
      * <p><b>Use createMessageDialog(JBException ex, Object[] params, String[] details)
      * instead, if you want to get an entry in error log.</b></p>
      * @param messageID the actual messageID
      * @param params Parameter of the message text or null, if not needed.
      * @param details use null, or overwrite in MessageIDs hardcoded details.
      * @return the dialog.
      */
     public static Dialog createMessageDialog(final Integer messageID,
             final Object[] params, final String[] details) {
         Display.getDefault().syncExec(new Runnable() {
             public void run() {
                 dlg = createMessageDialog(messageID, params, details, Plugin
                         .getShell());
             }
         });
         return dlg;
     }
     
     /**
      * Open the message dialog.
      * 
      * @param messageID
      *            the actual messageID
      * @return the dialog.
      */
     public static Dialog createMessageDialog(final Integer messageID) {
         return createMessageDialog(messageID, null, null);
     }
     
     /**
      * Open the message dialog.
      * <p><b>Use createMessageDialog(JBException ex, Object[] params, String[] details)
      * instead, if you want to get an entry in error log.</b></p>
      * @param messageID the actual messageID
      * @param params Parameter of the message text or null, if not needed.
      * @param details use null, or overwrite in MessageIDs hardcoded details.
      * @param parent the parent shell to use for this message dialog
      * @return the dialog.
      */
     @SuppressWarnings("nls")
     public static Dialog createMessageDialog(final Integer messageID, 
         final Object[] params, final String[] details, final Shell parent) {
         String title = StringConstants.EMPTY;
         String message = StringConstants.EMPTY;
         String[] labels = new String[] { Messages.UtilsOK };
         int imageID = MessageDialog.INFORMATION;
         Message msg = MessageIDs.getMessageObject(messageID);
         String[] detail = lineFeed(msg.getDetails());
         if (details != null) {
             detail = lineFeed(details);
         }
         switch (msg.getSeverity()) {
             case Message.ERROR:
                 title = Messages.UtilsError;
                 message = Messages.UtilsErrorOccurred;
                 break;
             case Message.INFO:
                 title = Messages.UtilsInfo1;
                 message = Messages.UtilsInfo2;
                 break;
             case Message.WARNING:
                 title = Messages.UtilsWarning1;
                 message = Messages.UtilsWarning2;
                 break;
             case Message.QUESTION:
                 title = Messages.UtilsRequest1;
                 message = Messages.UtilsRequest2;
                 labels = new String[] {
                     Messages.UtilsYes,
                     Messages.UtilsNo };
                 imageID = MessageDialog.QUESTION;
                 break;
             default:
                 break;
         }
         IStatus[] status = new Status[detail.length];
         for (int i = 0; i < detail.length; i++) {
             status[i] = new Status(msg.getSeverity(), Plugin.PLUGIN_ID,
                     IStatus.OK, detail[i], null);
         }
         if ((msg.getSeverity() == Message.INFO 
                 || msg.getSeverity() == Message.QUESTION)) {
             StringBuilder messageBuilder = new StringBuilder(message);
             messageBuilder.append(msg.getMessage(params));
             messageBuilder.append(StringConstants.NEWLINE);
             for (IStatus s : status) {
                 if (s.getMessage() != Message.NO_DETAILS) {
                     messageBuilder.append(StringConstants.NEWLINE);
                     messageBuilder.append(s.getMessage());
                 }
             }
             dlg = new MessageDialog(parent, title, null, messageBuilder
                     .toString(), imageID, labels, 0);
         } else {
             dlg = new ErrorDialog(new Shell(SWT.ON_TOP), title,
                     message, new MultiStatus(Plugin.PLUGIN_ID, IStatus.OK,
                             status, msg.getMessage(params), null), IStatus.OK
                             | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
         }
         dlg.create();
         DialogUtils.setWidgetNameForModalDialog(dlg);
         dlg.open();
         return dlg;
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
                     if (cap.getComponentName().equals(name)) {
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

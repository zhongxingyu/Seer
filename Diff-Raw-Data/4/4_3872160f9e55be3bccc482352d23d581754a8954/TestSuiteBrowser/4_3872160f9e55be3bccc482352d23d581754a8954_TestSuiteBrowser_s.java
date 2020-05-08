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
 package org.eclipse.jubula.client.ui.rcp.views;
 
 import java.util.Locale;
 
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ILanguageChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IProblemPropagationListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IExecObjContPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingPO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.constants.RCPCommandIDs;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TestExecDropTargetListener;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TreeViewerContainerDragSourceListener;
 import org.eclipse.jubula.client.ui.rcp.editors.TestJobEditor;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.DecoratingCellLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.TestSuiteBrowserContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.TooltipLabelProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.tools.exception.JBFatalException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IDecoratorManager;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 05.07.2004
  */
 @SuppressWarnings("synthetic-access")
 public class TestSuiteBrowser extends AbstractJBTreeView implements
     ITreeViewerContainer, IJBPart, ILanguageChangedListener, 
     IProblemPropagationListener {
 
     /** New-menu */
     public static final String NEW_ID = PlatformUI.PLUGIN_ID + ".NewSubMenu"; //$NON-NLS-1$  
     /** Add-Submenu ID */
     public static final String ADD_ID = PlatformUI.PLUGIN_ID + ".AddSubMenu"; //$NON-NLS-1$
     /** standard logging */
     static final Logger LOG = LoggerFactory.getLogger(TestSuiteBrowser.class);
     /**
      * Creates the SWT controls for this workbench part.
      * @param parent Composite
      */
     public void createPartControl(Composite parent) {
         super.createPartControl(parent);
         ColumnViewerToolTipSupport.enableFor(getTreeViewer());
         getTreeViewer().setContentProvider(
                 new TestSuiteBrowserContentProvider());
         DecoratingCellLabelProvider lp = new DecoratingCellLabelProvider(
                 new TooltipLabelProvider(), Plugin.getDefault()
                         .getWorkbench().getDecoratorManager()
                         .getLabelDecorator());
 
         getTreeViewer().setLabelProvider(lp);
         getTreeViewer().setAutoExpandLevel(DEFAULT_EXPANSION);
         
         setViewerInput();
         Plugin.getHelpSystem().setHelp(getTreeViewer().getControl(),
                 ContextHelpIds.TEST_SUITE_VIEW);
         
         int ops = DND.DROP_MOVE;
         Transfer[] transfers = new Transfer[] {LocalSelectionTransfer
             .getInstance()};
         getTreeViewer().addDragSupport(ops, transfers,
             new TreeViewerContainerDragSourceListener(getTreeViewer()));
         getTreeViewer().addDropSupport(ops, transfers,
             new TestExecDropTargetListener(this));
         
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addLanguageChangedListener(this, true);
         ded.addProblemPropagationListener(this);
         if (GeneralStorage.getInstance().getProject() != null) {
             handleProjectLoaded();
         }
     }
 
     /**
      * Adds a double click listener to the tree view.
      */
     protected void addTreeListener() {
         getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 IStructuredSelection selection = getSuiteTreeSelection();
                 Object firstElement = selection.getFirstElement();
                 if (firstElement instanceof ITestSuitePO) {
                     runCommand(RCPCommandIDs.OPEN_TESTSUITE_EDITOR);
                 } else if (firstElement instanceof IExecTestCasePO) {
                     IExecTestCasePO exec = (IExecTestCasePO) firstElement;
                     if (exec.getParentNode() instanceof ITestSuitePO) {
                         runCommand(RCPCommandIDs.OPEN_TESTSUITE_EDITOR);
                     } else {
                         runCommand(RCPCommandIDs.OPEN_TESTCASE_EDITOR);
                     }
                 } else if (firstElement instanceof ITestJobPO 
                         || firstElement instanceof IRefTestSuitePO) {
                     runCommand(RCPCommandIDs.OPEN_TESTJOB_EDITOR);
                 } else if (firstElement instanceof IExecObjContPO) {
                     runCommand(RCPCommandIDs.NEW_TESTSUITE);
                 }  else if (firstElement instanceof ICapPO) {
                     runCommand(RCPCommandIDs.OPEN_TESTCASE_EDITOR);
                 } else if (firstElement instanceof ICategoryPO) {
                     runCommand(RCPCommandIDs.NEW_TESTSUITE);
                 }
             }
             
             /**
              * runs the given command
              * 
              * @param commandID the commandId to execute
              */
             private void runCommand(String commandID) {
                 CommandHelper.executeCommand(commandID, getSite());
             }
         });
     }
 
     /** {@inheritDoc} */
     protected void createContextMenu(IMenuManager mgr) {
         mgr.add(new GroupMarker("defaultTestSuiteBrowserMarker")); //$NON-NLS-1$
     }
 
     /**
      * @return the selected tree item
      */
     public IStructuredSelection getSuiteTreeSelection() {
         return (getTreeViewer().getSelection() instanceof IStructuredSelection)
             ? (IStructuredSelection)getTreeViewer().getSelection()
                     : StructuredSelection.EMPTY;
     }
 
     /**
      * Asks this part to take focus within the workbench.
      */
     public void setFocus() {
         getTreeViewer().getControl().setFocus();
         Plugin.showStatusLine(this);
     }
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.removeDataChangedListener(this);
         ded.removeLanguageChangedListener(this);
         ded.removeProblemPropagationListener(this);
         super.dispose();
     }
   
     /**
      * {@inheritDoc}
      */
     protected void rebuildTree() {
         setViewerInput();
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleDataChanged(final IPersistentObject po, 
         final DataState dataState, final UpdateState updateState) {
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 // due to checkstyle BUG this indirection is necessary
                 handleDataChangedImpl(po, dataState, updateState);
             }
         });
     }
 
     /**
      * @param po
      *            the PO
      * @param dataState
      *            the data state
      * @param updateState
      *            the update state
      */
     private void handleDataChangedImpl(final IPersistentObject po,
             final DataState dataState, final UpdateState updateState) {
         // changes on the aut do not affect structure of this view
         if (po instanceof IAUTMainPO) {
             getTreeViewer().refresh();
             return;
         }
         if (updateState == UpdateState.onlyInEditor) {
             return;
         }
 
         switch (dataState) {
             case Added:
                 handleDataAdded(po);
                 break;
             case Deleted:
                 handleDataDeleted(po);
                 break;
             case Renamed:
                 if (po instanceof IProjectPO 
                         || po instanceof ITestSuitePO
                         || po instanceof ITestJobPO 
                         || po instanceof ITestCasePO
                         || po instanceof ICategoryPO) {
                     
                     getTreeViewer().refresh();
                 }
                 break;
             case StructureModified:
                 if (po instanceof IProjectPO) {
                     handleProjectLoaded();
                 }
                 if ((po instanceof ISpecTestCasePO)
                         || (po instanceof ITestSuitePO)
                         || (po instanceof ITestJobPO)) {
 
                     // retrieve tree state
                     Object[] expandedElements = 
                         getTreeViewer().getExpandedElements();
                     ISelection selection = getTreeViewer().getSelection();
 
                     // refresh treeview
                     getTreeViewer().refresh();
 
                     // restore tree status
                     getTreeViewer().setExpandedElements(expandedElements);
                     getTreeViewer().setSelection(selection);
                 }
                 if (po instanceof IObjectMappingPO) {
                     getTreeViewer().refresh();
                 }
                 break;
             default:
                 break;
         }
     }
     
     /**
      * @param po The persistent object that was deleted
      */
     private void handleDataDeleted(final IPersistentObject po) {
         
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 if (po instanceof ITestSuitePO
                         || po instanceof ITestJobPO
                         || po instanceof ICategoryPO) {
 
                     getTreeViewer().refresh();
 
                 } else if (po instanceof IProjectPO) {
                     setViewerInput();
                     getTreeViewer().refresh();
                 }
             }
         });
     }
 
     /**
      * @param po
      *            The persistent object that was added
      */
     private void handleDataAdded(IPersistentObject po) {
        if (po instanceof ISpecTestCasePO) {
             return;
         }
         getTreeViewer().refresh();
         getTreeViewer().expandToLevel(getTreeViewer().getAutoExpandLevel());
         getTreeViewer().setSelection(new StructuredSelection(po), true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleLanguageChanged(Locale locale) {
         getTreeViewer().refresh();
     }
 
     /**
      * Adds the given test suite to the selected test job as a reference. This
      * method is only allowed from an editor context since it relies on the
      * Session provided by the editors EditSupport.
      * @param ts the test suite to reference.
      * @param tj the target TestJob
      * @param position the position to insert. If null, the position is 
      * @return the referenced test suite.
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     public INodePO addReferencedTestSuite(ITestSuitePO ts, INodePO tj,
             int position) throws PMReadException, PMAlreadyLockedException,
             PMDirtyVersionException, PMException {
         IRefTestSuitePO exTcGUI = null;
         ITestSuitePO workTs = null;
         workTs = createWorkVersionofTs(ts);
         if (workTs != null) {
             IRefTestSuitePO refTs = TestSuiteBP.addReferencedTestSuite(
                     getEditSupport(), tj, workTs, position);
             DataEventDispatcher.getInstance().fireDataChangedListener(refTs,
                     DataState.Added, UpdateState.onlyInEditor);
         }
         return exTcGUI;
     }
     
     /**
      * @return the EditSupport for the current active editor. This methods
      * throws a JBFatalExecption if called with no IJBEditor subclass active.
      */
     private EditSupport getEditSupport() {
         TestJobEditor edit = getTJEditor();            
         EditSupport editSupport = edit.getEditorHelper().getEditSupport();
         return editSupport;
     }
     
     /**
      * @return the actual active TJ editor
      */
     private TestJobEditor getTJEditor() {
         TestJobEditor edit = Plugin.getDefault().getActiveTJEditor();
         if (edit == null) {
             String msg = Messages.NoActiveTCEditorPleaseFixTheMethod;
             LOG.error(msg); 
             throw new JBFatalException(msg, MessageIDs.E_NO_OPENED_EDITOR);
         }
         return edit;
     }
     
     /**
      * get the WorkVerstion to origTs
      * @param origTs original specTc
      * @return workorigTs or null
      * @throws PMReadException in case of db read error
      * @throws PMDirtyVersionException in case of version conflict (dirty read)
      * @throws PMAlreadyLockedException if the origSpecTc is already locked by another user
      * @throws PMException in case of unspecified db error
      */
     private ITestSuitePO createWorkVersionofTs(
             ITestSuitePO origTs) throws PMReadException, 
             PMAlreadyLockedException, PMDirtyVersionException, PMException {
         ITestSuitePO workTs = null;
         EditSupport editSupport = getEditSupport();
         workTs = (ITestSuitePO)editSupport.createWorkVersion(origTs);
         return workTs;
     }    
 
     /**
      * Sets the input for the tree viewer.
      */
     private void setViewerInput() {
         IProjectPO activeProject = GeneralStorage.getInstance().getProject();
         if (activeProject != null) {
             getTreeViewer().setInput(
                     new IExecObjContPO[] {activeProject.getExecObjCont()});
         } else {
             getTreeViewer().setInput(null);
         }
     }
 
     /** {@inheritDoc} */
     public void problemPropagationFinished() {
         getTreeViewer().getTree().getDisplay().syncExec(new Runnable() {
             public void run() {
                 getTreeViewer().refresh();
                 IDecoratorManager dm = Plugin.getDefault()
                         .getWorkbench().getDecoratorManager();
                 dm.update(Constants.CC_DECORATOR_ID);
             }
         });
     }
 
     /**
      * @return The instance of the TestSuiteBrowser, or null.
      */
     public static TestSuiteBrowser getInstance() {
         IViewPart viewPart = Plugin.getView(Constants.TS_BROWSER_ID);
         if (viewPart != null) {
             return (TestSuiteBrowser) viewPart;
         }
         return null;
     }
 
 }

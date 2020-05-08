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
 package org.eclipse.jubula.client.ui.rcp.editors;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.IRecordListener;
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ObjectMappingEventDispatcher;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.compcheck.CompletenessGuard;
 import org.eclipse.jubula.client.core.businessprocess.db.TestCaseBP;
 import org.eclipse.jubula.client.core.commands.CAPRecordedCommand;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.RecordModeState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IEventHandlerContainer;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.ObjectMappingManager;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMObjectDeletedException;
 import org.eclipse.jubula.client.core.persistence.locking.LockManager;
 import org.eclipse.jubula.client.core.utils.StringHelper;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.UINodeBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.EventHandlerDropTargetListener;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionClipboardTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TCEditorDndSupport;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.TCEditorDropTargetListener;
 import org.eclipse.jubula.client.ui.rcp.dialogs.AddEventHandlerDialog;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.rcp.provider.DecoratingCellLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.EventHandlerContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.TooltipLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.utils.UIIdentitiyElementComparer;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IWorkbenchPartConstants;
 
 /**
  * Editor for SpecTestCases
  *
  * @author BREDEX GmbH
  * @created 05.09.2005
  */
 @SuppressWarnings("synthetic-access")
 public class TestCaseEditor extends AbstractTestCaseEditor 
     implements IRecordListener {
     
     /** Constants for the editor segmentation */
     private static final int[] SASH_WEIGHT = {75, 25};
 
     /** the OM manager for this editor */
     private ObjectMappingManager m_objectMappingManager = 
         new ObjectMappingManager();
 
     /** TreeViewer for ErrorHandling */
     private TreeViewer m_eventHandlerTreeViewer;
 
     /** the current TreeViewer */
     private TreeViewer m_currentTreeViewer;
 
     /** {@inheritDoc} */
     public void createPartControlImpl(Composite parent) {
         super.createPartControlImpl(parent);
         m_eventHandlerTreeViewer.setContentProvider(
                 new EventHandlerContentProvider());
         m_eventHandlerTreeViewer.getControl().setMenu(
                 createContextMenu());
         addDoubleClickListener(CommandIDs.OPEN_SPECIFICATION_COMMAND_ID, 
                 m_eventHandlerTreeViewer);
         ActionListener actionListener = new ActionListener();
         getTreeViewer().addSelectionChangedListener(actionListener);
         getEventHandlerTreeViewer().addSelectionChangedListener(actionListener);
         if (!Plugin.getDefault().anyDirtyStar()) {
             checkAndRemoveUnusedTestData();
         }
     }
 
     /**
      * when objectmapping exists, then lock OM
      * @param monitor
      *      IProgressMonitor
      */
     public void doSave(IProgressMonitor monitor) {
         IPersistentObject inputTC = 
             getEditorHelper().getEditSupport().getWorkVersion();
         ISpecTestCasePO recordTC = CAPRecordedCommand.getRecSpecTestCase();
         boolean isStillObserving = CAPRecordedCommand.isObserving();
         if (isStillObserving && inputTC.equals(recordTC)) {
             int returnCode = showSaveInObservModeDialog();
             if (returnCode == Window.OK) {
                 TestExecutionContributor.getInstance().
                     getClientTest().resetToTesting();
                 DataEventDispatcher.getInstance()
                     .fireRecordModeStateChanged(RecordModeState.notRunning);
                 isStillObserving = false;
             }
         }
         
         if (!isStillObserving) {
             try {
                 m_objectMappingManager.saveMappings();
                 refreshOMProfilesForAUTS();
                 removeIncorrectCompNamePairsInExecTcs();
                 super.doSave(monitor);
             } catch (PMException e) {
                 PMExceptionHandler.handlePMExceptionForMasterSession(e);
             } catch (ProjectDeletedException e) {
                 PMExceptionHandler.handleGDProjectDeletedException();
             } catch (IncompatibleTypeException ite) {
                 ErrorHandlingUtil.createMessageDialog(ite, 
                         ite.getErrorMessageParams(), null);
             }
         }
     }
 
     /**
      * Removes incorrect CompNamePairs from all referencing Test Cases for
      * which a lock can be acquired.
      */
     private void removeIncorrectCompNamePairsInExecTcs() {
         // Find all Test Case References in this project that reference this
         // Test Case
         INodePO workVersion = 
             (INodePO)getEditorHelper().getEditSupport().getWorkVersion();
         List<Long> parentProjectIds = new ArrayList<Long>();
         parentProjectIds.add(workVersion.getParentProjectId());
         List<IExecTestCasePO> execTcRefs = 
             NodePM.getExecTestCases(workVersion.getGuid(), parentProjectIds);
         Set<INodePO> lockedNodePOs = 
             new HashSet<INodePO>();
         
         EntityManager editorSession = 
             getEditorHelper().getEditSupport().getSession();
         for (IExecTestCasePO execTc : execTcRefs) {
             try {
                 INodePO parentNode = execTc.getParentNode();
                 if (parentNode != null) {
                     INodePO editorSessionParentNode = editorSession.find(
                             parentNode.getClass(), parentNode.getId());
                     
                     if (LockManager.instance().lockPO(editorSession, 
                             editorSessionParentNode, true)) {
                         lockedNodePOs.add(editorSessionParentNode);
                     }
                 } else {
                    LOG.info("The parent of ExecTestCase (GUID " + execTc.getGuid()  //$NON-NLS-1$
                             + ") is null. Skipped removal of incorrect compNamePairs."); //$NON-NLS-1$
                 }
             } catch (PMDirtyVersionException e) {
                 // Unable to successfully acquire lock
                 // Pairs for this node will not be updated
                 // Do nothing
             } catch (PMObjectDeletedException e) {
                 // Unable to successfully acquire lock
                 // Pairs for this node will not be updated
                 // Do nothing
             }
         }
         
         // Remove incorrect pairs for nodes for which we were able to acquire
         // a lock.
         for (INodePO node : lockedNodePOs) {
             CompNamesBP.removeIncorrectCompNamePairs(node);
         }
     }
 
 
 
     /**
      * Refresh all AUTs for the current project, so that we can avoid 
      * NonUniqueObjectException for same ObjectMappingProfilePO for multiple 
      * AUTs
      */
     private void refreshOMProfilesForAUTS() {
         EntityManager sess = GeneralStorage.getInstance().getMasterSession();
         for (IAUTMainPO aut 
                 : GeneralStorage.getInstance().getProject()
                     .getAutMainList()) {
             sess.refresh(aut.getObjMap().getProfile());
         }
     }
 
 
 
     /**
      * {@inheritDoc}
      */
     public String getEditorPrefix() {
         return Messages.PluginTC;
     }
     
     /**
      * {@inheritDoc}
      */
     public void capRecorded(final ICapPO newCap, 
         final IComponentIdentifier ci) {
         
         if (newCap == null) {
             ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.E_TEST_STEP_NOT_CREATED);
         } else {
             final IAUTMainPO recordAut = 
                 TestExecution.getInstance().getConnectedAut();
 
             Plugin.getDisplay().syncExec(new Runnable() {
                 public void run() {
                     if (getEditorHelper().requestEditableState() 
                             != JBEditorHelper.EditableState.OK) {
                         return;
                     }
 
                     // Cap added to model
                     // recorded action with default mapping not being 
                     // added to objmap
                     if (!ObjectMappingEventDispatcher.
                             getObjMapTransient().existTechnicalName(ci)) {
                         
                         String capComponentName = 
                             m_objectMappingManager.addMapping(
                                 recordAut, ci, newCap.getComponentName());
 
                         newCap.setComponentName(capComponentName);
                     }
                     getTreeViewer().refresh(false);
 
                     getTreeViewer().setSelection(
                             new StructuredSelection(newCap), true);
                     getEditorHelper().setDirty(true);
                 }
             });
         }
     }
     
     /**
      * @author BREDEX GmbH
      * @created 02.06.2005
      * Sets the actual tree selection of this editor depending of the selected tree. 
      */
     private class TreeFocusListener extends FocusAdapter {
         /** {@inheritDoc} */
         public void focusGained(FocusEvent e) {
             Tree tree = (Tree)e.getSource();
             if (getMainTreeViewer().getTree() == tree) {
                 m_currentTreeViewer = getMainTreeViewer();
             } else if (m_eventHandlerTreeViewer.getTree() == tree) {
                 m_currentTreeViewer = m_eventHandlerTreeViewer;
             }
             m_currentTreeViewer.setSelection(
                     m_currentTreeViewer.getSelection(), true);
         }       
     }
 
     /**
      * @author BREDEX GmbH
      * @created 04.06.2005
      */
     private class MouseDownListener implements Listener {
         /**
          * {@inheritDoc}
          */
         public void handleEvent(Event event) {
             if (SWT.MouseDown == event.type) {
                 Tree tree = (Tree)event.widget;
                 if (getMainTreeViewer().getTree() == tree) {
                     m_currentTreeViewer = getMainTreeViewer();
                 } else if (m_eventHandlerTreeViewer.getTree() == tree) {
                     m_currentTreeViewer = m_eventHandlerTreeViewer;
                 }
             }
         }
     }
     
     /**
      * SelectionListener to en-/disable delete-action
      * 
      * @author BREDEX GmbH
      * @created 02.03.2006
      */
     private class ActionListener implements ISelectionChangedListener {
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("unchecked") 
         public void selectionChanged(SelectionChangedEvent event) {
             if (!(event.getSelection() instanceof IStructuredSelection)) {
                 return;
             }
             IStructuredSelection sel = 
                 (IStructuredSelection)event.getSelection();
             
             if (GeneralStorage.getInstance().getProject() == null
                     || (sel == null || sel.isEmpty())) {
                 getCutTreeItemAction().setEnabled(false);
                 getPasteTreeItemAction().setEnabled(false);
             } else {
                 List<INodePO> selList = sel.toList();
                 enableCutAction(selList);
                 enablePasteAction(selList);
             }
         }
         
         /**
          * en-/disable cut-action
          * @param selList actual selection 
          */
         private void enableCutAction(List<INodePO> selList) {
             getCutTreeItemAction().setEnabled(true);
 
             for (INodePO node : selList) {
                 if (!(node instanceof IExecTestCasePO
                         || node instanceof ICapPO)) {
                     getCutTreeItemAction().setEnabled(false);
                     return;
                 }
             }
         }
 
         /**
          * en-/disable paste-action
          * @param selList actual selection 
          */
         private void enablePasteAction(List<INodePO> selList) {
             
             getPasteTreeItemAction().setEnabled(false);
             LocalSelectionClipboardTransfer transfer = 
                 LocalSelectionClipboardTransfer.getInstance();
             Object cbContents = 
                 getEditorHelper().getClipboard().getContents(transfer);
 
             if (cbContents == null) {
                 return;
             }
 
             for (INodePO guiNode : selList) {
                 if (guiNode == null
                         || !(cbContents instanceof StructuredSelection)
                         || !TCEditorDndSupport.validateDrop(
                                 transfer.getSource(), getTreeViewer(), 
                                 transfer.getSelection(), 
                                 guiNode, false)) {
                     
                     getPasteTreeItemAction().setEnabled(false);
                     return;
                 }
             }
 
             getPasteTreeItemAction().setEnabled(true);
 
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     public Image getDisabledTitleImage() {
         return IconConstants.DISABLED_TC_EDITOR_IMAGE;
     }
     
     /** {@inheritDoc}
      * @see org.eclipse.jubula.client.ui.rcp.editors.TestCaseEditor#reOpenEditor(org.eclipse.jubula.client.core.model.IPersistentObject)
      */
     public void reOpenEditor(IPersistentObject node) throws PMException {
         m_objectMappingManager.clear();
         super.reOpenEditor(node);
         if (node instanceof ISpecTestCasePO) {
             CAPRecordedCommand.setRecSpecTestCase((ISpecTestCasePO)node);
         }
     }
     
     /**
      * Sets the help to the HelpSystem.
      * @param parent the parent composite to set the help id to
      */
     protected void setHelp(Composite parent) {
         Plugin.getHelpSystem().setHelp(parent,
             ContextHelpIds.JB_SPEC_TESTCASE_EDITOR);        
     }
     
     /**
      * Shows information dialog that savin on observation mode is not allowed
      * @return returnCode of Dialog
      */
     private int showSaveInObservModeDialog() {
         MessageDialog dialog = new MessageDialog(Plugin.getShell(), 
             Messages.SaveInObservationModeDialogTitle,
                 null, Messages.SaveInObservationModeDialogQuestion,
                 MessageDialog.QUESTION, new String[] {
                     Messages.DialogMessageButton_YES,
                     Messages.DialogMessageButton_NO }, 0);
         dialog.create();
         DialogUtils.setWidgetNameForModalDialog(dialog);
         dialog.open();
         return dialog.getReturnCode();
     }
 
     @Override
     public ISelection getSelection() {
         if (m_currentTreeViewer == null) {
             return StructuredSelection.EMPTY;
         }
         return m_currentTreeViewer.getSelection();
     }
 
     @Override
     protected void renameGUINode(IPersistentObject po) {
         super.renameGUINode(po);
         m_eventHandlerTreeViewer.refresh(true);
     }
     
     /**
      * Creates the EventHandler part of the editor
      * @param parent Composite
      */
     private void createEventHandlerPart(Composite parent) {
         Composite headLineComposite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(1, true);
         layout.marginWidth = 0;
         layout.marginHeight = 0;
         layout.marginBottom = 0;
         layout.marginTop = 0;
         headLineComposite.setLayout(layout);
         Label headLine = new Label(headLineComposite, SWT.NONE);
         headLine.setText(Messages.TestCaseEditorEHAreaHeadline); 
         ControlDecorator.decorateInfo(headLine,
                 "GDControlDecorator.EventHandler", false); //$NON-NLS-1$
         GridData ehTvGridData = new GridData();
         ehTvGridData.grabExcessHorizontalSpace = true;
         ehTvGridData.grabExcessVerticalSpace = true;
         ehTvGridData.horizontalAlignment = SWT.FILL;
         ehTvGridData.verticalAlignment = SWT.FILL;
         ehTvGridData.verticalSpan = 100;
         GridLayout ehTvLayout = new GridLayout(1, true);
         ehTvLayout.marginWidth = 0;
         ehTvLayout.marginHeight = 0;
         ehTvLayout.marginBottom = 0;
         ehTvLayout.marginTop = 0;
         m_eventHandlerTreeViewer = new TreeViewer(headLineComposite);
         m_eventHandlerTreeViewer.getTree().setLayout(ehTvLayout);
         m_eventHandlerTreeViewer.getTree().setLayoutData(ehTvGridData);
 
         ColumnViewerToolTipSupport.enableFor(m_eventHandlerTreeViewer);
         DecoratingCellLabelProvider lp = new DecoratingCellLabelProvider (
                 new TooltipLabelProvider(), Plugin.getDefault().getWorkbench()
                         .getDecoratorManager().getLabelDecorator());
         m_eventHandlerTreeViewer.setLabelProvider(lp);
         m_eventHandlerTreeViewer.setComparer(new UIIdentitiyElementComparer());
         m_eventHandlerTreeViewer.setUseHashlookup(true);
         m_eventHandlerTreeViewer.getTree()
             .addListener(SWT.MouseDown, new MouseDownListener());
         firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void addInternalSelectionListeners(
             final ISelectionChangedListener editorSelectionChangedListener) {
         
         super.addInternalSelectionListeners(editorSelectionChangedListener);
         m_eventHandlerTreeViewer.addSelectionChangedListener(
                 editorSelectionChangedListener);
     }
     
     @Override
     protected void addDragAndDropSupport(
             int operations, Transfer[] transfers) {
         super.addDragAndDropSupport(operations, transfers);
         m_eventHandlerTreeViewer.addDropSupport(operations, transfers, 
                 new EventHandlerDropTargetListener(this));
     }
 
     /**
      * @return Returns the eventHandlerTreeViewer.
      */
     public TreeViewer getEventHandlerTreeViewer() {
         return m_eventHandlerTreeViewer;
     }
     
     /**
      * Adds the given eventHandlerInput to the given eventHandlerOwner 
      * as an eventHandler.
      * @param eventHandlerInput the ISpecTestCasePO to be the EventHandler
      * @param evHandlerOwner the ISpecTestCasePO to own the EventHandler
      */
     public void addEventHandler(ISpecTestCasePO eventHandlerInput, 
         ISpecTestCasePO evHandlerOwner) {
         
         final EditSupport editSupport = getEditorHelper().getEditSupport();
         ISpecTestCasePO workSpecTcPO = (ISpecTestCasePO)editSupport
             .getWorkVersion();
         ISpecTestCasePO eventHandlerInputPO = eventHandlerInput;
         IEventExecTestCasePO eventHandlerPO = null;
         try {           
             ISpecTestCasePO eventHandlerWorkV = (ISpecTestCasePO)editSupport
                 .createWorkVersion(eventHandlerInputPO);
             eventHandlerPO = NodeMaker.createEventExecTestCasePO(
                 eventHandlerWorkV, workSpecTcPO);
             final int status = openAddEventHandlerDlg(evHandlerOwner, 
                     eventHandlerPO);
             if (Window.OK == status) {
                 editSupport.lockWorkVersion();
                 TestCaseBP.addEventHandler(editSupport, workSpecTcPO, 
                     eventHandlerPO);
                 getEditorHelper().setDirty(true);
                 getEventHandlerTreeViewer().refresh();
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         eventHandlerPO, DataState.Added,
                         UpdateState.onlyInEditor);
             }
         } catch (InvalidDataException e) {
             // no log entry, because it is a use case!
             ErrorHandlingUtil.createMessageDialog(
                 MessageIDs.E_DOUBLE_EVENT, null, 
                 new String[]{NLS.bind(
                         Messages.TestCaseEditorDoubleEventTypeErrorDetail,
                         new Object[]{evHandlerOwner.getName(), 
                             I18n.getString(eventHandlerPO.getEventType())})}); 
         } catch (PMException e) {
             PMExceptionHandler.handlePMExceptionForMasterSession(e);
         }
     }
     
     /**
      * opens the AddEventHandlerDlg.
      * @param eventHandlerCont the SpecTestCasePO
      * @param eventHandler the EventExecTestCasePO
      * @return status the window return code.
      */
     private int openAddEventHandlerDlg(
         IEventHandlerContainer eventHandlerCont, 
         final IEventExecTestCasePO eventHandler) {
         
         AddEventHandlerDialog dialog = 
             new AddEventHandlerDialog(Plugin.getShell(), eventHandler
                     .getSpecTestCase().getName(), eventHandlerCont);
         dialog.addListener(new AddEventHandlerDialog.Listener() {
             public void notifySelected(String eventType, 
                 String reentryType, Integer maxRetries) {
                 
                 String evType = StringHelper.getInstance().getMap()
                     .get(eventType);
                 
                 setEventHandlerProperties(eventHandler, evType, 
                     reentryType, maxRetries);
             }
         });
         int status = dialog.open();
         dialog.close();
         return status;
     }
 
     /**
      * {@inheritDoc}
      */
     public void handlePropertyChanged(boolean isCompNameChanged) {
         super.handlePropertyChanged(isCompNameChanged);
         m_eventHandlerTreeViewer.refresh();
     }
     
     @Override
     protected SashForm createSashForm(Composite parent) {
         SashForm form = super.createSashForm(parent);
         createEventHandlerPart(form);
         form.setWeights(SASH_WEIGHT);   
         return form;
     }
     
     @Override
     public void setInitialInput() {
         super.setInitialInput();
 
         m_eventHandlerTreeViewer.setContentProvider(
                 new EventHandlerContentProvider());  
         ISpecTestCasePO workVersion = getWorkVersion();
         m_eventHandlerTreeViewer.setInput(workVersion);
         m_eventHandlerTreeViewer.expandAll();
         m_eventHandlerTreeViewer.getTree().addFocusListener(
             new TreeFocusListener());
     }
 
     @Override
     protected void initTopTreeViewer(INodePO root) {
         super.initTopTreeViewer(root);
         getMainTreeViewer().getTree().addFocusListener(
                 new TreeFocusListener());
     }
     
     @Override
     protected void refresh() {
         super.refresh();
         getEventHandlerTreeViewer().refresh();
     }
     
     @Override
     protected void handleNodeAdded(INodePO addedNode) {
         super.handleNodeAdded(addedNode);
         if (addedNode instanceof IEventExecTestCasePO) {
             UINodeBP.setSelectionAndFocusToNode(
                     addedNode, m_eventHandlerTreeViewer);
         }
     }
 
     /** {@inheritDoc} */
     protected DropTargetListener getViewerDropAdapter() {
         return new TCEditorDropTargetListener(this);
     }
     
     /** {@inheritDoc} */
     protected void runLocalChecks() {
         super.runLocalChecks();
         ISpecTestCasePO workVersion = getWorkVersion();
         for (INodePO child : workVersion.getAllEventEventExecTC()) {
             CompletenessGuard.checkLocalTestData(child, WorkingLanguageBP
                     .getInstance().getWorkingLanguage());
         }
     }
 
     /**
      * @return the work version to use for this editor
      */
     protected ISpecTestCasePO getWorkVersion() {
         return (ISpecTestCasePO)super.getWorkVersion();
     }
 
     @Override
     protected Iterator<? extends INodePO> getIteratorForNode(INodePO node) {
         if (node instanceof IEventExecTestCasePO) {
             return getWorkVersion().getAllEventEventExecTC().iterator();
         }
 
         return super.getIteratorForNode(node);
     }
     
     /** {@inheritDoc} */
     public void setSelectionImpl(ISelection selection) {
         if (selection instanceof StructuredSelection) {
             StructuredSelection ss = (StructuredSelection) selection;
             Object firstElement = ss.getFirstElement();
             if (firstElement instanceof IEventExecTestCasePO) {
                 getEventHandlerTreeViewer().getTree().setFocus();
                 getEventHandlerTreeViewer().setSelection(selection);
             } else {
                 super.setSelectionImpl(selection);
             }
         }
     }
 }

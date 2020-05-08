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
 package org.eclipse.jubula.client.ui.editors;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.commons.collections.IteratorUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameCache;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameCache;
 import org.eclipse.jubula.client.core.businessprocess.ObjectMappingEventDispatcher;
 import org.eclipse.jubula.client.core.businessprocess.TestCaseParamBP;
 import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
 import org.eclipse.jubula.client.core.businessprocess.db.TimestampBP;
 import org.eclipse.jubula.client.core.commands.CAPRecordedCommand;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.RecordModeState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IDataSetPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataCubeContPO;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.ITimestampPO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.model.ReentryProperty;
 import org.eclipse.jubula.client.core.persistence.CompNamePM;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.ModelParamValueConverter;
 import org.eclipse.jubula.client.core.utils.ParamValueConverter;
 import org.eclipse.jubula.client.core.utils.StringHelper;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.actions.AddNewTestCaseAction;
 import org.eclipse.jubula.client.ui.actions.InsertNewTestCaseAction;
 import org.eclipse.jubula.client.ui.actions.SearchTreeAction;
import org.eclipse.jubula.client.ui.businessprocess.GuiNodeBP;
 import org.eclipse.jubula.client.ui.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.controllers.dnd.LocalSelectionTransfer;
 import org.eclipse.jubula.client.ui.controllers.dnd.TreeViewerContainerDragSourceListener;
 import org.eclipse.jubula.client.ui.events.GuiEventDispatcher;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.contentprovider.TestCaseEditorContentProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchActionConstants;
 
 
 /**
  * @author BREDEX GmbH
  * @created 13.10.2004
  */
 @SuppressWarnings("synthetic-access")
 public abstract class AbstractTestCaseEditor extends AbstractJBEditor {
 
     /** the retarget action to insert new tc */
     private InsertNewTestCaseAction m_insertNewTCAction = 
         new InsertNewTestCaseAction();
     /** the retarget action to add new tc */
     private AddNewTestCaseAction m_addNewTCAction = new AddNewTestCaseAction();
     
     /**
      * Creates the initial Context of this Editor.<br>
      * Subclasses may override this method. 
      * @param parent Composite
      */
     public void createPartControlImpl(Composite parent) {
         createSashForm(parent);
         setParentComposite(parent);
         // sets the input of the trees.
         setInitialInput();
         final DataEventDispatcher dispatcher = 
             DataEventDispatcher.getInstance();
         dispatcher.addPropertyChangedListener(this, true);
         addDragAndDropSupport(DND.DROP_MOVE, 
                 new Transfer[] {LocalSelectionTransfer.getInstance()});
         getEditorHelper().addListeners();
         setActionHandlers();
         addTreeDoubleClickListener(CommandIDs.REFERENCE_TC_COMMAND_ID);
         GuiEventDispatcher.getInstance()
             .addEditorDirtyStateListener(this, true);
         DataEventDispatcher.getInstance().addDataChangedListener(
                 new CentralTestDataUpdateListener(), false);
     }
     
     /**
      * Sets all necessary global action handlers for this editor. This
      * ensures that the editor's actions control the enablement of the 
      * corresponding actions in the main menu.
      */
     protected void setActionHandlers() {
         getEditorSite().getActionBars().setGlobalActionHandler(
                 Constants.NEW_TC_ACTION_ID + INSERT, m_insertNewTCAction);
         getEditorSite().getActionBars().setGlobalActionHandler(
                 Constants.NEW_TC_ACTION_ID + ADD, m_addNewTCAction);
         super.setActionHandlers();
     }
 
     /**
      * adds Drag and Drop support for the trees.
      * 
      * @param operations The DnD operation types to support.
      * @param transfers The DnD transfer types to support.
      */
     protected void addDragAndDropSupport(
             int operations, Transfer[] transfers) {
         getMainTreeViewer().addDragSupport(operations, transfers,
             new TreeViewerContainerDragSourceListener(getTreeViewer()));
         getMainTreeViewer().addDropSupport(operations, transfers, 
             getViewerDropAdapter()); 
     }
 
     /**
      * @return the viewer drop adapter
      */
     protected abstract DropTargetListener getViewerDropAdapter();
 
     /**
      * @param parent the paent of the SashForm.
      * @return the created SashForm.
      */
     protected SashForm createSashForm(Composite parent) {
         SashForm sashForm = new SashForm(parent, SWT.MULTI | SWT.VERTICAL);
         GridLayout compLayout = new GridLayout(1, true);
         compLayout.marginWidth = 0;
         compLayout.marginHeight = 0;
         sashForm.setLayout(compLayout);
         GridData gridData = new GridData (GridData.FILL_BOTH);
         sashForm.setLayoutData(gridData);        
         setControl(sashForm);
         createMainPart(sashForm);
         return sashForm;
     }
     
     /**
      * Refreshes all referenced Test Data Cubes within the context of this 
      * editor when Central Test Data changes.
      * 
      * @author BREDEX GmbH
      * @created 17.03.2011
      */
     private class CentralTestDataUpdateListener 
             implements IDataChangedListener {
 
         /**
          * 
          * {@inheritDoc}
          */
         public void handleDataChanged(IPersistentObject po,
                 DataState dataState, UpdateState updateState) {
 
             if (po instanceof ITestDataCubeContPO 
                     && dataState == DataState.StructureModified 
                     && updateState != UpdateState.notInEditor) {
 
                 ITreeNodeOperation<INodePO> refreshRefDataCubeOp =
                     new ITreeNodeOperation<INodePO>() {
                         public boolean operate(
                                 ITreeTraverserContext<INodePO> ctx,
                                 INodePO parent, INodePO node,
                                 boolean alreadyVisited) {
                             
                             if (node instanceof IParamNodePO) {
                                 IParameterInterfacePO referencedCube = 
                                     ((IParamNodePO)node)
                                         .getReferencedDataCube();
                                 if (referencedCube != null) {
                                     getEditorHelper().getEditSupport()
                                         .getSession().refresh(referencedCube);
                                 }
                             }
                             return true;
                         }
 
                         public void postOperate(
                                 ITreeTraverserContext<INodePO> ctx,
                                 INodePO parent, INodePO node,
                                 boolean alreadyVisited) {
                             // no-op
                         }
                     };
 
                 TreeTraverser refDataCubeRefresher = 
                     new TreeTraverser(
                             (INodePO)getEditorHelper().getEditSupport()
                                 .getWorkVersion(),
                             refreshRefDataCubeOp, true, 2);
                 refDataCubeRefresher.traverse(true);
                 
             }
         }
     }
 
     @Override
     public void setInitialInput() {
         getMainTreeViewer().setContentProvider(
                 new TestCaseEditorContentProvider());  
         
         INodePO workVersion = 
             (INodePO)getEditorHelper().getEditSupport().getWorkVersion();
 
         initTopTreeViewer(workVersion);
     }
 
     /**
      * @param root the root of the TreeViewer.
      */
     protected void initTopTreeViewer(INodePO root) {
         try {
             getMainTreeViewer().getTree().setRedraw(false);
             getMainTreeViewer().setInput(new INodePO[] {root});
         } finally {
             getMainTreeViewer().getTree().setRedraw(true);
             getMainTreeViewer().expandAll();
         }
     }
 
     /**
      * @param monitor IProgressMonitor
      */
     public void doSave(IProgressMonitor monitor) {
         if (!checkCompleteness()) {
             return;
         }
         monitor.beginTask(Messages.EditorsSaveEditors,
                 IProgressMonitor.UNKNOWN);
         try {
             EditSupport editSupport = getEditorHelper().getEditSupport();
             removeIncorrectCompNamePairs();
             fixCompNameReferences();
             final IPersistentObject perObj = editSupport.getWorkVersion();
 
             IWritableComponentNameCache compNameCache = 
                 editSupport.getCompMapper().getCompNameCache();
             Set<IComponentNamePO> renamedCompNames = 
                 new HashSet<IComponentNamePO>(
                         compNameCache.getRenamedNames());
             Set<IComponentNamePO> reuseChangedCompNames = 
                 new HashSet<IComponentNamePO>(); 
             for (String compNameGuid : compNameCache.getReusedNames()) {
                 IComponentNamePO compName = 
                     compNameCache.getCompNamePo(compNameGuid);
                 if (compName != null) {
                     reuseChangedCompNames.add(compName);
                 }
             }
 
             if (perObj instanceof ISpecTestCasePO) {
                 final IProjectPO project = GeneralStorage.getInstance()
                     .getProject();
                 UsedToolkitBP.getInstance().addToolkit((ISpecTestCasePO)perObj, 
                     project);
             }
             TimestampBP.refreshTimestamp((ITimestampPO)perObj);
             editSupport.saveWorkVersion();
             updateObjectMapping();
 
             for (IComponentNamePO compName : renamedCompNames) {
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         compName, DataState.Renamed, UpdateState.all);
             }
 
             for (IComponentNamePO compName : reuseChangedCompNames) {
                 DataEventDispatcher.getInstance().fireDataChangedListener(
                         compName, DataState.ReuseChanged, UpdateState.all);
             }
             
             getEditorHelper().resetEditableState();
             getEditorHelper().setDirty(false);
         } catch (IncompatibleTypeException pmce) {
             handlePMCompNameException(pmce);
         } catch (PMException e) {
             PMExceptionHandler.handlePMExceptionForMasterSession(e);
             try {
                 reOpenEditor(((NodeEditorInput)getEditorInput()).getNode());
             } catch (PMException e1) {
                 PMExceptionHandler.handlePMExceptionForEditor(e, this);
             }
         } catch (ProjectDeletedException e) {
             PMExceptionHandler.handleGDProjectDeletedException();
         } finally {
             monitor.done();
         }
     }
     
     /**
      * Replaces Component Name references with the referenced Component Names
      * and deletes any Component Name references that are no longer used.
      */
     private void fixCompNameReferences() {
         // Replace all reference guids with referenced guids
         INodePO rootNode = 
             (INodePO)getEditorHelper().getEditSupport().getWorkVersion();
         IComponentNameCache compNameCache = getEditorHelper().getEditSupport()
             .getCompMapper().getCompNameCache();
         Iterator iter = rootNode.getNodeListIterator();
         while (iter.hasNext()) {
             INodePO nodePO = (INodePO)iter.next();
             if (nodePO instanceof IExecTestCasePO) {
                 IExecTestCasePO exec = (IExecTestCasePO)nodePO;
                 List<String> toRemove = 
                     new ArrayList<String>();
                 List<ICompNamesPairPO> toAdd = 
                     new ArrayList<ICompNamesPairPO>();
                 for (ICompNamesPairPO pair : exec.getCompNamesPairs()) {
                     String firstName = pair.getFirstName();
                     String secondName = pair.getSecondName();
                     IComponentNamePO firstCompNamePo = 
                         compNameCache.getCompNamePo(firstName);
                     IComponentNamePO secondCompNamePo = 
                         compNameCache.getCompNamePo(secondName);
 
                     if (!(firstCompNamePo.getGuid().equals(firstName)
                             && secondCompNamePo.getGuid().equals(secondName))) {
                         String componentType = pair.getType();
                         
                         toRemove.add(firstName);
                         toAdd.add(
                                 PoMaker.createCompNamesPairPO(
                                         firstCompNamePo.getGuid(), 
                                         secondCompNamePo.getGuid(), 
                                         componentType));
                     }
                 }
                 for (String stringToRemove : toRemove) {
                     exec.removeCompNamesPair(stringToRemove);
                 }
                 for (ICompNamesPairPO pairToAdd : toAdd) {
                     exec.addCompNamesPair(pairToAdd);
                 }
             } else if (nodePO instanceof ICapPO) {
                 ICapPO capPo = (ICapPO)nodePO;
                 String compNameGuid = capPo.getComponentName();
                 IComponentNamePO compNamePo = 
                     compNameCache.getCompNamePo(compNameGuid);
                 if (!compNamePo.getGuid().equals(compNameGuid)) {
                     capPo.setComponentName(compNamePo.getGuid());
                 }
             }
         }
 
         
         // Delete all unused reference comp names
         CompNamePM.removeUnusedCompNames(
                 GeneralStorage.getInstance().getProject().getId(),
                 getEditorHelper().getEditSupport().getSession());
     }
 
     /**
      * Removes incorrect CompNamePair from ExecTC during saving.
      */
     private void removeIncorrectCompNamePairs() {
         INodePO node = 
             (INodePO)getEditorHelper().getEditSupport().getWorkVersion();
         if (node instanceof ISpecTestCasePO
                 || node instanceof ITestSuitePO) {
             for (Object o : node.getUnmodifiableNodeList()) {
                 if (o instanceof IExecTestCasePO) {
                     IExecTestCasePO exec = (IExecTestCasePO)o;
                     ICompNamesPairPO [] pairArray = 
                         exec.getCompNamesPairs().toArray(
                             new ICompNamesPairPO[
                                 exec.getCompNamesPairs().size()]);
                     for (ICompNamesPairPO pair : pairArray) {
                         searchAndSetComponentType(pair);
                         if (pair.getType().equals(StringConstants.EMPTY)) {
                             exec.removeCompNamesPair(pair.getFirstName());
                         }
                     }
                 }
             }
         } else {
             LOG.error(Messages.WrongEditSupportInTestCaseEditor 
                 + StringConstants.COLON + StringConstants.SPACE + node);
         }
     }
     
     
     /**
      * 
      * @param pair the current compNamesPairPO
      */
     private void searchAndSetComponentType(final ICompNamesPairPO pair) {
         if (pair.getType() != null
                 && !StringConstants.EMPTY.equals(pair.getType())) {
             return;
         }
         final IPersistentObject orig = 
             getEditorHelper().getEditSupport().getOriginal();
         if (orig instanceof ISpecTestCasePO || orig instanceof ITestSuitePO) {
             INodePO origNode = (INodePO)orig;
             for (Object node : origNode.getUnmodifiableNodeList()) {
                 if (searchCompType(pair, node)) {
                     return;
                 }
             }
         }
         // if exec was added to an editor session
         if (getStructuredSelection().getFirstElement() != null
                 && (pair.getType() == null || StringConstants.EMPTY.equals(pair
                         .getType()))) {
             searchCompType(pair, getStructuredSelection().getFirstElement());
         }
     }
 
     /**
      * @param pair the current compNamesPairPO
      * @param node the node to search comp type in
      * @return true, if comp type was found
      */
     private boolean searchCompType(final ICompNamesPairPO pair, Object node) {
         if (node instanceof IExecTestCasePO) {
             ISpecTestCasePO specTc = ((IExecTestCasePO)node).getSpecTestCase();
             if (specTc == null) {
                 // Referenced SpecTestCase does not exist
                 return false;
             }
             for (Object childNode : specTc.getUnmodifiableNodeList()) {
 
                 if (childNode instanceof IExecTestCasePO) {
                     IExecTestCasePO exec = (IExecTestCasePO)childNode;
                     for (ICompNamesPairPO cnp : exec.getCompNamesPairs()) {
                         if (cnp.getSecondName().equals(pair.getFirstName())
                             && cnp.isPropagated()) {
 
                             pair.setType(cnp.getType());
                             if (pair.getType() != null
                                     && !StringConstants.EMPTY.equals(pair
                                             .getType())) {
                                 return true;
                             }
                             searchCompType(pair, exec);
                         }                    
                     }                    
                 } else if (childNode instanceof ICapPO) {
                     ICapPO cap = (ICapPO)childNode;
                     if (cap.getComponentName().equals(pair.getFirstName())) {
                         pair.setType(cap.getComponentType());
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     /**
      * Checks, if all fields were filled in correctly.
      * @return True, if all fields were filled in correctly. 
      */
     @SuppressWarnings("unchecked")
     protected boolean checkCompleteness() {
         ISpecTestCasePO testCase = (ISpecTestCasePO)getEditorHelper()
                 .getEditSupport().getWorkVersion();
         if (testCase.getName() == null 
                 || StringConstants.EMPTY.equals(testCase.getName())) {
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, 
                     null, new String[]{Messages.TestCaseEditorNoTcName});
             return false;
         }
         if (testCase.getName().startsWith(BLANK) 
             || testCase.getName().endsWith(BLANK)) { 
             Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, 
                     null, new String[]{Messages.TestCaseEditorWrongTcName});
             return false;
         }
         Iterator iter = testCase.getNodeListIterator();
         while (iter.hasNext()) {
             Object node = iter.next();
             if (Hibernator.isPoSubclass((IPersistentObject)node, 
                 ICapPO.class)) {
                 ICapPO cap = (ICapPO)node;
                 if (cap.getName() == null
                         || StringConstants.EMPTY.equals(cap.getName())) {
                     Utils.createMessageDialog(
                         MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, null, 
                         new String[]{Messages.TestCaseEditorNoCapName});
                     return false;
                 }
                 if (cap.getName().startsWith(BLANK) 
                     || cap.getName().endsWith(BLANK)) { 
                     Utils.createMessageDialog(
                         MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, null, 
                         new String[]{NLS.bind(
                             Messages.TestCaseEditorWrongTsName,
                             new Object[]{cap.getName()})}); 
                     return false;
                 }
                 if (cap.getComponentName() == null
                         || StringConstants.EMPTY.equals(
                                 cap.getComponentName())) {
                     Utils.createMessageDialog(
                         MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, null, 
                         new String[]{NLS.bind(Messages.TestCaseEditorNoCompName,
                             new Object[]{cap.getName()})});
                     return false;
                 }  
                 if (cap.getComponentName().startsWith(BLANK) 
                     || cap.getComponentName().endsWith(BLANK)) { 
                     Utils.createMessageDialog(
                         MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, null, 
                         new String[]{NLS.bind(
                                 Messages.TestCaseEditorWrongCompName2,  
                             new Object[]{cap.getName()})}); 
                     return false;
                 }
             }
         }
         for (Object object : testCase.getAllEventEventExecTC()) {
             IEventExecTestCasePO eventTC = (IEventExecTestCasePO)object;
             if (StringConstants.EMPTY.equals(eventTC.getName())) {
                 Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP,
                     null, new String[]{Messages.TestCaseEditorNoEventTcName});
                 return false;
             }
             if (eventTC.getName().startsWith(BLANK) 
                 || eventTC.getName().endsWith(BLANK)) { 
                 Utils.createMessageDialog(MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP,
                     null, new String[]{NLS.bind(
                             Messages.TestCaseEditorWrongEhName, 
                         new Object[]{eventTC.getName()})}); 
                 return false;
             }
         }
         return checkRefsAndCompNames(testCase);
     }
     
     /**
      * {@inheritDoc}
      */
     protected void checkMasterSessionUpToDate() {
         super.checkMasterSessionUpToDate();
         ITimestampPO node = 
             (ITimestampPO)getEditorHelper().getEditSupport().getWorkVersion();
         TimestampBP.refreshEditorNodeInMasterSession(node);
     }
     
     /**
      * Checks if testdata of the current original testCase contains references.
      * <p>Checks also the propagated compName.
      * @param testCase the currec testCase
      * @return true, if data was not mixed.
      */
     @SuppressWarnings("unchecked")
     private boolean checkRefsAndCompNames(ISpecTestCasePO testCase) {
         ITDManager mgr = testCase.getDataManager();
         Locale locale = WorkingLanguageBP.getInstance().getWorkingLanguage();
         for (int row = 0; row < mgr.getDataSetCount(); row++) {
             IDataSetPO row2 = mgr.getDataSet(row);
             for (int col = 0; col < row2.getColumnCount(); col++) {
                 ITestDataPO data = row2.getColumn(col);
                 String uniqueId = mgr.getUniqueIds().get(col);
                 IParamDescriptionPO desc = 
                     testCase.getParameterForUniqueId(uniqueId);
                 ParamValueConverter conv = 
                     new ModelParamValueConverter(data, testCase, locale, desc);
                 for (String refName : conv.getNamesForReferences()) {
                     Utils.createMessageDialog(
                         MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, null, 
                         new String[]{NLS.bind(
                                 Messages.TestCaseEditorContReference,
                             new Object[]{refName})});  
                     return false;
                 }
                 
             }
         }
         Iterator iter = testCase.getNodeListIterator();
         while (iter.hasNext()) {
             INodePO nodePO = (INodePO)iter.next();
             if (nodePO instanceof IExecTestCasePO) {
                 IExecTestCasePO exec = (IExecTestCasePO)nodePO;
                 for (ICompNamesPairPO pair : exec.getCompNamesPairs()) {
                     if (pair.getSecondName() == null || StringConstants.EMPTY
                         .equals(pair.getSecondName())) {
 
                         Utils.createMessageDialog(
                             MessageIDs.E_CANNOT_SAVE_EDITOR_TC_SP, null, 
                             new String[]{NLS.bind(
                                     Messages.TestCaseEditorMmissingCompName,
                                 new Object[]{StringHelper.getInstance()
                                     .getMap().get(pair.getType()), 
                                     pair.getFirstName(), 
                                     exec.getName()})});
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * 
      */
     private void updateObjectMapping() {
         INodePO rootInput = 
             (INodePO)getEditorHelper().getEditSupport().getWorkVersion();
         for (INodePO node : rootInput.getUnmodifiableNodeList()) {
             if (node instanceof ISpecTestCasePO) {
                 ObjectMappingEventDispatcher.notifyRecordObserver(
                         (ISpecTestCasePO)node);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     protected void fillContextMenu(IMenuManager mgr) {
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         if (getStructuredSelection().getFirstElement() == null) {
             return;
         }
         MenuManager submenuInsert = new MenuManager(
                 Messages.TestCaseEditorInsert, INSERT_ID);
         MenuManager submenuAdd = new MenuManager(Messages.TestSuiteBrowserAdd,
                 ADD_ID);
         MenuManager submenuRefactor = new MenuManager(
                 Messages.TestCaseEditorRefactor, REFACTOR_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REFERENCE_TC_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.NEW_CAP_COMMAND_ID);
         mgr.add(submenuAdd);
         mgr.add(submenuInsert);
         mgr.add(getCutTreeItemAction());
         mgr.add(getPasteTreeItemAction());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.TOGGLE_ACTIVE_STATE_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.EDIT_PARAMETERS_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REVERT_CHANGES_COMMAND_ID);
         mgr.add(new Separator());
         mgr.add(submenuRefactor);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         mgr.add(SearchTreeAction.getAction());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.OPEN_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_SPECIFICATION_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.SHOW_WHERE_USED_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
         submenuInsert.add(m_insertNewTCAction);
         submenuAdd.add(m_addNewTCAction);
         CommandHelper.createContributionPushItem(submenuAdd,
                 CommandIDs.ADD_EVENT_HANDLER_COMMAND_ID);
         CommandHelper.createContributionPushItem(submenuRefactor,
                 CommandIDs.EXTRACT_TESTCASE_COMMAND_ID);
     }
 
     /**
      * Cleanup on closing.
      */
     public void dispose() {
         try {
             if (CAPRecordedCommand.getRecordListener() == this) {
                 CAPRecordedCommand.setRecordListener(null);
                 TestExecutionContributor.getInstance().getClientTest()
                         .resetToTesting();
             }
 
             if (getEditorSite() != null && getEditorSite().getPage() != null) {
                 DataEventDispatcher.getInstance().fireRecordModeStateChanged(
                         RecordModeState.notRunning);
                 removeGlobalActionHandler();
             }
         } finally {
             super.dispose();
         }
     }
 
     /**
      * Removes global action handler to prevent memory leaks. Only clears the 
      * action handlers if handlers for this editor are currently in use. This 
      * means that if another editor has registered its own handlers, then that
      * editor is responsible for clearing its own action handlers.
      */
     private void removeGlobalActionHandler() {
         removeGlobalActionHandler(
             Constants.NEW_TC_ACTION_ID + INSERT, getInsertNewTCAction());
         removeGlobalActionHandler(
             Constants.NEW_TC_ACTION_ID + ADD, getAddNewTCAction());
 
         getEditorSite().getActionBars().updateActionBars();
     }
     
     /**
      * Conditionally clears the global action handler for the given action id.
      * This method does *not* update the action bars. The invoker must perform
      * the update after calling this method.
      * 
      * @param actionId The id of the global action to clear.
      * @param handler The handler that should be removed from the given action
      *                id. If the current handler for the action id is *not*
      *                <code>handler</code>, no changes will be made to the 
      *                action.
      */
     private void removeGlobalActionHandler(String actionId, IAction handler) {
         IActionBars actionBars = getEditorSite().getActionBars();
         if (actionBars.getGlobalActionHandler(actionId) == handler) {
             actionBars.setGlobalActionHandler(actionId, null);        
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void handleDataChanged(IPersistentObject po, DataState dataState, 
         UpdateState updateState) {
         
         if (po instanceof INodePO) {
             switch (dataState) {
                 case Added:
                     INodePO addedNode = (INodePO)po;
                     INodePO editorNode = 
                         (INodePO)getEditorHelper().getEditSupport()
                             .getWorkVersion();
                     if (editorNode.indexOf(addedNode) > -1
                             || (editorNode instanceof ISpecTestCasePO 
                                     && ((ISpecTestCasePO)editorNode)
                                     .getEventExecTcMap().containsValue(po))) {
                         handleNodeAdded(addedNode);
                     }
                     break;
                 case Deleted:
                     if (!(po instanceof IProjectPO)) {
                        INodePO guiNode = 
                            ((INodePO[])getTreeViewer().getInput())[0];
                        GuiNodeBP.setSelectionAndFocusToNode(
                                guiNode, getTreeViewer());
                     } 
                     break;
                 case Renamed:
                     renameGUINode(po);
                     break;
                 case StructureModified:
                     if (!handleStructureModified(po)) {
                         return;
                     }
                     break;
                 case ReuseChanged:
                     // nothing yet!
                     break;
                 default:
                     Assert.notReached();
             }
             getEditorHelper().handleDataChanged(po, dataState, updateState);
         }
     }
 
      /**
       * Handles a PO that has been modified.
       * 
       * @param po The modified object.
       * @return <code>false</code> if an error occurs during handling. 
       *         Otherwise, <code>true</code>.
       */
     private boolean handleStructureModified(IPersistentObject po) {
         if (po instanceof ISpecTestCasePO) {
             final ISpecTestCasePO specTestCasePO = (ISpecTestCasePO)po;
             final INodePO workVersion = (INodePO)getEditorHelper()
                 .getEditSupport().getWorkVersion();
             final List<IExecTestCasePO> execTestCases = NodePM.
                 getInternalExecTestCases(specTestCasePO.getGuid(), 
                     specTestCasePO.getParentProjectId());
             if (!execTestCases.isEmpty() && containsWorkVersionReuses(
                     workVersion, specTestCasePO)) {
                 
                 if (Plugin.getActiveEditor() != this && isDirty()) {
                     Utils.createMessageDialog(
                         MessageIDs.I_SAVE_AND_REOPEN_EDITOR, 
                         new Object[]{getTitle(), 
                             specTestCasePO.getName()}, null);
                     return false;
                 }
                 try {
                     reOpenEditor(getEditorHelper().getEditSupport()
                             .getOriginal());
                 } catch (PMException e) {
                     Utils.createMessageDialog(
                         MessageIDs.E_REFRESH_FAILED, null,
                         new String[] {Messages.ErrorMessageEDITOR_CLOSE});
                     getSite().getPage().closeEditor(this, false);
                 }  
                 return false;
             }
         }
         return true;
     }
     
     /**
      * @param root node, where starts the validation
      * @param specTc changed specTc
      * @return if editor contains an reusing testcase for given specTestCase
      */
     @SuppressWarnings("unchecked")
     private static boolean containsWorkVersionReuses(INodePO root, 
         ISpecTestCasePO specTc) {
         
         final Iterator it = root.getNodeListIterator();
         final List <INodePO> childList = IteratorUtils.toList(it);
         // Add EventHandler to children List!
         if (root instanceof ISpecTestCasePO) { 
             final ISpecTestCasePO rootSpecTc = (ISpecTestCasePO)root;
             childList.addAll(rootSpecTc.getAllEventEventExecTC());
         }
         for (INodePO child : childList) {
             if (child instanceof IExecTestCasePO) {
                 final IExecTestCasePO execTc = (IExecTestCasePO)child;
                 if (specTc.equals(execTc.getSpecTestCase())) {
                     return true;
                 }
                 if (containsWorkVersionReuses(execTc, specTc)) {
                     return true;
                 }
             }
         }    
         return false;
     }
     
     /**
      * Sets the EventHandler properties.
      * @param eventHandler the EventHandlerTc
      * @param eventType the event type
      * @param reentryType the reentry type
      * @param maxRetries the maximum number of retries
      */
     void setEventHandlerProperties(
         IEventExecTestCasePO eventHandler,
         String eventType, String reentryType, 
         Integer maxRetries) {
         
         eventHandler.setEventType(eventType);
         ReentryProperty[] reentryProps = ReentryProperty.REENTRY_PROP_ARRAY;
         for (int i = 0; i < reentryProps.length; i++) {
             if (String.valueOf(reentryProps[i]).equals(reentryType)) {
                 eventHandler.setReentryProp(reentryProps[i]);
                 break;
             }
         }
         Assert.verify(eventHandler.getReentryProp() != null,
             Messages.ErrorWhenSettingReentryProperty 
             + StringConstants.EXCLAMATION_MARK);
 
         eventHandler.setMaxRetries(maxRetries);
     }
     
     /**
      * Sets the selection to the (in the browser selected) correct node.
      * @param selectedNode the selected node of the browser.
      */
     public void setOpeningSelection(INodePO selectedNode) {
         getTreeViewer().setSelection(new StructuredSelection(selectedNode));
     }
 
     /**
      * @return the action
      */
     protected AddNewTestCaseAction getAddNewTCAction() {
         return m_addNewTCAction;
     }
 
     /**
      * @return the action
      */
     protected InsertNewTestCaseAction getInsertNewTCAction() {
         return m_insertNewTCAction;
     }
     
     /**
      * synchronizes the list of parameter unique ids in TDManagers of ExecTestCases
      * and the associated parameter list
      * @param root root node of editor
      */
     @SuppressWarnings("unchecked")
     private void updateTDManagerOfExecTestCases(INodePO root) {
         Iterator it = root.getNodeListIterator();
         while (it.hasNext()) {
             IParamNodePO child = (IParamNodePO)it.next();
             if (child instanceof IExecTestCasePO) {
                 ((IExecTestCasePO)child).synchronizeParameterIDs();
             }
         }     
     }
 
     /**
      * Checks and removes unused TestData of IExecTestCasePOs.
      */
     protected final void checkAndRemoveUnusedTestData() {
         final EditSupport editSupport = getEditorHelper().getEditSupport();
         final IPersistentObject workVersion = editSupport.getWorkVersion();
         if (!(workVersion instanceof INodePO)) {
             return;
         }
         final INodePO nodePO = (INodePO)workVersion;
         final List<IExecTestCasePO> execsWithUnusedTestData = TestCaseParamBP
             .getExecTcWithUnusedTestData(nodePO);
         if (execsWithUnusedTestData.isEmpty()) {
             return;
         }
         
         try {
             editSupport.lockWorkVersion();
             getEditorHelper().setDirty(true);
             updateTDManagerOfExecTestCases(nodePO);
             doSave(new NullProgressMonitor());
         } catch (PMAlreadyLockedException e) {
             // ignore, we are only doing housekeeping
         } catch (PMException e) {
             PMExceptionHandler.handlePMExceptionForMasterSession(e);
         }
     }
 
     /**
      * Refreshes the tree viewer.
      */
     protected void refresh() {
         getTreeViewer().refresh();
     }
 
     /**
      * 
      * @return the receiver's current selection, if it is an 
      *         {@link IStructuredSelection}. Otherwise, returns an empty 
      *         selection.
      */
     protected IStructuredSelection getStructuredSelection() {
         ISelection selection = getSelection();
         if (selection instanceof IStructuredSelection) {
             return (IStructuredSelection)selection;
         }
         
         return StructuredSelection.EMPTY;
     }
     
     /**
      * Refreshes the viewer and updates the expansion state and selection
      * based on the added node.
      * 
      * @param addedNode The node that has been added.
      */
     protected void handleNodeAdded(INodePO addedNode) {
         getTreeViewer().refresh();
        getTreeViewer().expandAll();
         getTreeViewer().setSelection(new StructuredSelection(addedNode));
     }
 }

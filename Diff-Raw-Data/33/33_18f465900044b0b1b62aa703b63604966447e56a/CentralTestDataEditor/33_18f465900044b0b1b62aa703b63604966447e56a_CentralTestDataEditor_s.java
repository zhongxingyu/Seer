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
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerDropAdapter;
 import org.eclipse.jubula.client.core.businessprocess.CentralTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.TestDataCubeBP;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IParamChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataCategoryPO;
 import org.eclipse.jubula.client.core.model.ITestDataCubePO;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.constants.RCPCommandIDs;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.objectmapping.LimitingDragSourceListener;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper.EditableState;
 import org.eclipse.jubula.client.ui.rcp.events.GuiEventDispatcher;
 import org.eclipse.jubula.client.ui.rcp.filter.JBBrowserPatternFilter;
 import org.eclipse.jubula.client.ui.rcp.filter.JBFilteredTree;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.CentralTestDataContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.CentralTestDataLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.wizards.ImportTestDataSetsWizard;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPartConstants;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.menus.CommandContributionItem;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jun 28, 2010
  */
 public class CentralTestDataEditor extends AbstractJBEditor implements
         IParamChangedListener {
 
     /**
      * <code>m_elementsToRefresh</code> set of elements to refresh after saving
      * the editor
      */
     private Set<ITestDataCubePO> m_elementsToRefresh = 
         new HashSet<ITestDataCubePO>();
 
     /**
      * 
      * @author Zeb Ford-Reitz
      * @created Nov 03, 2011
      */
     private class CentralTestDataDropSupport extends ViewerDropAdapter {
     
         /**
          * Constructor
          * 
          * @param viewer The viewer.
          */
         public CentralTestDataDropSupport(Viewer viewer) {
             super(viewer);
         }
         
         @Override
         public boolean validateDrop(Object target, int operation,
                 TransferData transferType) {
 
             ISelection selection = 
                     LocalSelectionTransfer.getTransfer().getSelection();
             if (getTargetCategory(target) != null 
                     && selection instanceof IStructuredSelection) {
                 for (Object element 
                         : ((IStructuredSelection)selection).toArray()) {
                     if (!(element instanceof ITestDataCategoryPO 
                             || element instanceof ITestDataCubePO)) {
                         return false;
                     }
                 }
                 return true;
             }
 
             return false;
         }
         
         @Override
         public boolean performDrop(Object data) {
             if (getEditorHelper().requestEditableState() 
                     != EditableState.OK) {
                 return false;
             }
             ITestDataCategoryPO targetCategory = 
                     getTargetCategory(getCurrentTarget());
             ISelection selection = 
                     LocalSelectionTransfer.getTransfer().getSelection();
             Set<ITestDataCategoryPO> structuresToUpdate = 
                     new HashSet<ITestDataCategoryPO>();
             structuresToUpdate.add(targetCategory);
             for (Object element 
                     : ((IStructuredSelection)selection).toArray()) {
             
                 if (element instanceof ITestDataCategoryPO) {
                     ITestDataCategoryPO category = 
                             (ITestDataCategoryPO)element;
                     if (!CentralTestDataBP.getAncestors(targetCategory)
                                 .contains(category)
                             && category.getParent() != targetCategory) {
                         ITestDataCategoryPO oldParent = 
                                 category.getParent();
                         structuresToUpdate.add(oldParent);
                         oldParent.removeCategory(category);
                         targetCategory.addCategory(category);
                     }
                 } else if (element instanceof ITestDataCubePO) {
                     ITestDataCubePO testData = (ITestDataCubePO)element;
                     ITestDataCategoryPO oldParent = 
                             testData.getParent();
                     structuresToUpdate.add(oldParent);
                     oldParent.removeTestData(testData);
                     targetCategory.addTestData(testData);
                 }
 
             }
 
             List<DataChangedEvent> events = 
                     new LinkedList<DataChangedEvent>();
             for (ITestDataCategoryPO categoryToUpdate 
                     : structuresToUpdate) {
                 
                 events.add(new DataChangedEvent(categoryToUpdate, 
                         DataState.StructureModified, 
                         UpdateState.onlyInEditor));
             }
 
             DataEventDispatcher.getInstance().fireDataChangedListener(
                     events.toArray(new DataChangedEvent[events.size()]));
             
             return true;
         }
 
         /**
          * 
          * @param target The drop target.
          * @return the adjusted drop target. For example, the top-level category
          *         if <code>target</code> is null. Returns <code>null</code>
          *         if no valid drop target can be computed.
          */
         private ITestDataCategoryPO getTargetCategory(Object target) {
             if (target == null) {
                 return (ITestDataCategoryPO)getViewer().getInput();
             }
             
             if (target instanceof ITestDataCategoryPO) {
                 return (ITestDataCategoryPO)target;
             }
             
             return null;
         }
         
 
     } 
     
     /** {@inheritDoc} */
     protected void createPartControlImpl(Composite parent) {
         createMainPart(parent);
         GridData gridData = new GridData(GridData.FILL_BOTH);
         getMainTreeViewer().getControl().setLayoutData(gridData);
         setControl(getMainTreeViewer().getControl());
         getMainTreeViewer().setContentProvider(
                 new CentralTestDataContentProvider());
 
         DecoratingLabelProvider lp = new DecoratingLabelProvider(
                 new CentralTestDataLabelProvider(), Plugin.getDefault()
                         .getWorkbench().getDecoratorManager()
                         .getLabelDecorator());
 
         getMainTreeViewer().setLabelProvider(lp);
         getMainTreeViewer().setComparator(new ViewerComparator() {
             @Override
             public int category(Object element) {
                 if (element instanceof ITestDataCategoryPO) {
                     return 0;
                 }
                 
                 if (element instanceof ITestDataCubePO) {
                     return 1;
                 }
                 
                 return 2;
             }
         });
         
         int ops = DND.DROP_MOVE;
         Transfer[] transfers = 
             new Transfer[] { 
                 LocalSelectionTransfer.getTransfer()};
         ViewerDropAdapter dropSupport = 
                 new CentralTestDataDropSupport(getMainTreeViewer());
         dropSupport.setFeedbackEnabled(false);
         getMainTreeViewer().addDragSupport(ops, transfers, 
                 new LimitingDragSourceListener(getMainTreeViewer(), null));
         getMainTreeViewer().addDropSupport(ops, transfers, dropSupport);
         
         addDoubleClickListener(RCPCommandIDs.EDIT_PARAMETERS, 
                 getMainTreeViewer());
         addFocusListener(getMainTreeViewer());
         getEditorHelper().addListeners();
         setActionHandlers();
         setInitialInput();
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addPropertyChangedListener(this, true);
         ded.addParamChangedListener(this, true);
         GuiEventDispatcher.getInstance()
                 .addEditorDirtyStateListener(this, true);
     }
 
     /** {@inheritDoc} */
     public void dispose() {
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.removeParamChangedListener(this);
         getElementsToRefresh().clear();
         super.dispose();
     }
 
     /**
      * creates and sets the partName
      */
     protected void createPartName() {
         setPartName(Messages.CentralTestDataEditorName);
     }
 
     /**
      * @param mainTreeViewer
      *            the tree viewer
      */
     private void addFocusListener(TreeViewer mainTreeViewer) {
         mainTreeViewer.getTree().addFocusListener(new FocusAdapter() {
             /** {@inheritDoc} */
             public void focusGained(FocusEvent e) {
                 getMainTreeViewer().setSelection(
                         getMainTreeViewer().getSelection(), true);
             }
         });
     }
 
     /** {@inheritDoc} */
     protected void fillContextMenu(IMenuManager mgr) {
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.NEW_TESTDATACUBE);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.NEW_CATEGORY);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.RENAME);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.EDIT_PARAMETERS);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.SHOW_WHERE_USED);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.REVERT_CHANGES);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         mgr.add(CommandHelper.createContributionItem(
                 RCPCommandIDs.FIND,
                 null, Messages.FindContextMenu,
                 CommandContributionItem.STYLE_PUSH));
         mgr.add(new Separator());
         Map<String, String> params = new HashMap<String, String>();
         params.put(RCPCommandIDs.IMPORT_WIZARD_PARAM_ID, 
                 ImportTestDataSetsWizard.ID);
         mgr.add(CommandHelper.createContributionItem(
                 RCPCommandIDs.ECLIPSE_RCP_FILE_IMPORT, 
                 params, null,
                 CommandContributionItem.STYLE_PUSH));
     }
 
     /** {@inheritDoc} */
     protected void setHelp(Composite parent) {
         Plugin.getHelpSystem().setHelp(parent,
                 ContextHelpIds.CENTRAL_TESTDATA_EDITOR);
     }
 
     /** {@inheritDoc} */
     protected void setInitialInput() {
         ITestDataCategoryPO rootPOTop = (ITestDataCategoryPO)getEditorHelper()
                 .getEditSupport().getWorkVersion();
         try {
             getTreeViewer().getTree().getParent().setRedraw(false);
             getTreeViewer().setInput(rootPOTop);
             getTreeViewer().expandAll();
         } finally {
             getTreeViewer().getTree().getParent().setRedraw(true);
         }
     }
 
     /** {@inheritDoc} */
     public void doSave(IProgressMonitor monitor) {
         monitor.beginTask(Messages.EditorsSaveEditors,
                 IProgressMonitor.UNKNOWN);
         EditSupport editSupport = getEditorHelper().getEditSupport();
         try {
             editSupport.saveWorkVersion();
             final EntityManager masterSession = GeneralStorage.getInstance()
                     .getMasterSession();
             IPersistentObject original = editSupport.getOriginal();
             if (original != null) {
                 masterSession.refresh(original);
                 if (original instanceof ITestDataCategoryPO) {
                    for (ITestDataCubePO dataCube 
                            : ((ITestDataCategoryPO) original)
                            .getTestDataChildren()) {
                        masterSession.refresh(dataCube);
                        ITDManager manager = dataCube.getDataManager();
                        masterSession.refresh(manager);
                    }    
                 }
             }
             updateReferencedParamNodes();
 
             getEditorHelper().resetEditableState();
             getEditorHelper().setDirty(false);
         } catch (IncompatibleTypeException pmce) {
             handlePMCompNameException(pmce);
         } catch (PMException e) {
             PMExceptionHandler.handlePMExceptionForMasterSession(e);
             try {
                 reOpenEditor(
                     ((PersistableEditorInput)getEditorInput()).getNode());
             } catch (PMException e1) {
                 PMExceptionHandler.handlePMExceptionForEditor(e, this);
             }
         } catch (ProjectDeletedException e) {
             PMExceptionHandler.handleProjectDeletedException();
         } finally {
             monitor.done();
         }
 
     }
 
     /**
      * update the param nodes which reference test data cube, e.g. because of
      * renaming of test data cube
      */
     private void updateReferencedParamNodes() {
         Set<INodePO> nodesToRefresh = new HashSet<INodePO>();
         for (ITestDataCubePO tdc : getElementsToRefresh()) {
             nodesToRefresh.addAll(TestDataCubeBP.getReuser(tdc));
         }
         EntityManager masterSession = 
             GeneralStorage.getInstance().getMasterSession();
         for (INodePO node : nodesToRefresh) {
             masterSession.refresh(node);
         }
 
         getElementsToRefresh().clear();
     }
 
     /** {@inheritDoc} */
     public Image getDisabledTitleImage() {
         return IconConstants.DISABLED_CTD_EDITOR_IMAGE;
     }
 
     /** {@inheritDoc} */
     public String getEditorPrefix() {
         return Messages.PluginCTD;
     }
 
     /** {@inheritDoc} */
     public void handlePropertyChanged(boolean isCompNameChanged) {
         getMainTreeViewer().refresh();
     }
 
     /** {@inheritDoc} */
     public void handleParamChanged() {
         // assuming that the currently selected element (or rather, 
         // all currently selected elements) have had some kind of param change
         ISelection currentSelection = getMainTreeViewer().getSelection();
         if (currentSelection instanceof IStructuredSelection) {
             for (Object selectedObj 
                     : ((IStructuredSelection)currentSelection).toArray()) {
                 if (selectedObj instanceof ITestDataCubePO) {
                     getElementsToRefresh().add((ITestDataCubePO)selectedObj);
                 }
             }
         }
         getMainTreeViewer().refresh();
     }
 
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         getMainTreeViewer().getTree().setRedraw(false);
         try {
             for (DataChangedEvent e : events) {
                 handleDataChanged(e.getPo(), e.getDataState(),
                         e.getUpdateState());
             }
         } finally {
             getMainTreeViewer().getTree().setRedraw(true);
         }
     }
     
     /** {@inheritDoc} */
     public void handleDataChanged(IPersistentObject po, DataState dataState,
             UpdateState updateState) {
         
         if (po instanceof ITestDataCubePO) {
             if (updateState == UpdateState.onlyInEditor) {
                 getEditorHelper().setDirty(true);
             }
             ITestDataCubePO tdc = (ITestDataCubePO)po;
             handleDataChanged(dataState, tdc);
         } else if (po instanceof ITestDataCategoryPO) {
             if (updateState == UpdateState.onlyInEditor) {
                 getEditorHelper().setDirty(true);
             }
             handleDataChanged(dataState, (ITestDataCategoryPO)po);
         }
 
     }
 
     /**
      * @param dataState
      *            the data state
      * @param testData
      *            the data cube
      */
     private void handleDataChanged(
             DataState dataState, ITestDataCubePO testData) {
         
         switch (dataState) {
             case Added:
                 getTreeViewer().add(testData.getParent(), testData);
                 getTreeViewer().setSelection(new StructuredSelection(testData));
                 break;
             case Deleted:
                 getTreeViewer().remove(testData);
                 break;
             case Renamed:
                 getTreeViewer().update(testData, null);
                 getElementsToRefresh().add(testData);
                 break;
             case ReuseChanged:
                 break;
             case StructureModified:
                 getTreeViewer().update(testData, null);
                 getTreeViewer().setSelection(new StructuredSelection(testData));
                 break;
             default:
                 break;
         }
     }
 
     /**
      * @param dataState
      *            the data state
      * @param category
      *            the category
      */
     private void handleDataChanged(
             DataState dataState, ITestDataCategoryPO category) {
         
         switch (dataState) {
             case Added:
                 getTreeViewer().add(category.getParent(), category);
                 getTreeViewer().setSelection(new StructuredSelection(category));
                 break;
             case Deleted:
                 getTreeViewer().remove(category);
                 break;
             case Renamed:
                 getTreeViewer().update(category, null);
                 break;
             case ReuseChanged:
                 break;
             case StructureModified:
                 getTreeViewer().refresh(category);
                 break;
             default:
                 break;
         }
     }
 
     /**
      * Creates the specification part of the editor
      * 
      * @param parent
      *            Composite.
      */
     protected void createMainPart(Composite parent) {
         final FilteredTree ft = new JBFilteredTree(parent, SWT.MULTI
                 | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
                 new JBBrowserPatternFilter(), true);
         setMainTreeViewer(ft.getViewer());
         getMainTreeViewer().setUseHashlookup(true);
         getSite().setSelectionProvider(this);
         firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
     }
 
     /**
      * @return the elementsToRefresh
      */
     private Set<ITestDataCubePO> getElementsToRefresh() {
         return m_elementsToRefresh;
     }
     
     @Override
     public String getTitleToolTip() {
         return StringUtils.EMPTY;
     }
 }

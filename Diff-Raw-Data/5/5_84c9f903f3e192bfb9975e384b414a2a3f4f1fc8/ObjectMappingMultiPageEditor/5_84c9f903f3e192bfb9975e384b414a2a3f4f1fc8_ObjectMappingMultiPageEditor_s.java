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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerEditor;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TableViewerEditor;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jubula.client.core.businessprocess.CompNameResult;
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP.CompNameCreationContext;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameCache;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.IObjectMappingObserver;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameCache;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.ObjectMappingEventDispatcher;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.businessprocess.db.TimestampBP;
 import org.eclipse.jubula.client.core.commands.AUTModeChangedCommand;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.OMState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingAssoziationPO;
 import org.eclipse.jubula.client.core.model.IObjectMappingCategoryPO;
 import org.eclipse.jubula.client.core.model.IObjectMappingPO;
 import org.eclipse.jubula.client.core.model.IObjectMappingProfilePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.ITimestampPO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.persistence.CompNamePM;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.client.ui.constants.CommandIDs;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.actions.CutTreeItemActionOMEditor;
 import org.eclipse.jubula.client.ui.rcp.actions.OMMarkInAutAction;
 import org.eclipse.jubula.client.ui.rcp.actions.OMSetCategoryToMapInto;
 import org.eclipse.jubula.client.ui.rcp.actions.PasteTreeItemActionOMEditor;
 import org.eclipse.jubula.client.ui.rcp.actions.SearchTreeAction;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.CompletenessBP;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.OMEditorBP;
 import org.eclipse.jubula.client.ui.rcp.constants.RCPCommandIDs;
 import org.eclipse.jubula.client.ui.rcp.controllers.ComponentNameTreeViewerUpdater;
 import org.eclipse.jubula.client.ui.rcp.controllers.JubulaStateController;
 import org.eclipse.jubula.client.ui.rcp.controllers.PMExceptionHandler;
 import org.eclipse.jubula.client.ui.rcp.controllers.TestExecutionContributor;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionClipboardTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionTransfer;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.objectmapping.LimitingDragSourceListener;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.objectmapping.OMDropTargetListener;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.objectmapping.OMEditorDndSupport;
 import org.eclipse.jubula.client.ui.rcp.dialogs.NagDialog;
 import org.eclipse.jubula.client.ui.rcp.editingsupport.AbstractObjectMappingEditingSupport;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper.EditableState;
 import org.eclipse.jubula.client.ui.rcp.events.GuiEventDispatcher;
 import org.eclipse.jubula.client.ui.rcp.events.GuiEventDispatcher.IEditorDirtyStateListener;
 import org.eclipse.jubula.client.ui.rcp.filter.JBFilteredTree;
 import org.eclipse.jubula.client.ui.rcp.filter.ObjectMappingEditorPatternFilter;
 import org.eclipse.jubula.client.ui.rcp.handlers.RevertEditorChangesHandler;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.objectmapping.OMEditorTableContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.objectmapping.OMEditorTreeContentProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.contentprovider.objectmapping.ObjectMappingRow;
 import org.eclipse.jubula.client.ui.rcp.provider.labelprovider.OMEditorTreeLabelProvider;
 import org.eclipse.jubula.client.ui.rcp.provider.selectionprovider.SelectionProviderIntermediate;
 import org.eclipse.jubula.client.ui.rcp.utils.SelectionChecker;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.client.ui.views.ColumnSortListener;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.IMultiTreeViewerContainer;
 import org.eclipse.jubula.communication.message.ChangeAUTModeMessage;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
 import org.eclipse.jubula.tools.xml.businessmodell.ConcreteComponent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IPropertyListener;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPartConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.swt.IFocusService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Editor for managing Object Mapping in Jubula.
  *
  * @author BREDEX GmbH
  * @created Oct 21, 2008
  */
 public class ObjectMappingMultiPageEditor extends MultiPageEditorPart 
                 implements IJBPart, IJBEditor, 
                 IObjectMappingObserver, IEditorDirtyStateListener,
                 IMultiTreeViewerContainer, IPropertyListener {
 
     /** Show-menu */
     public static final String CLEANUP_ID = PlatformUI.PLUGIN_ID + ".CleanupSubMenu"; //$NON-NLS-1$
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(ObjectMappingMultiPageEditor.class);
     
     /** separator for categories shown in the table view of this editor */
     private static final String CAT_SEPARATOR = "/"; //$NON-NLS-1$
     
     /** page index of the split view */
     private static final int SPLIT_PAGE_IDX = 0;
 
     /** page index of the tree view */
     private static final int TREE_PAGE_IDX = 1;
 
     /** page index of the table view */
     private static final int TABLE_PAGE_IDX = 2;
     
     /** the object responsible for handling GDEditor-related tasks */
     private JBEditorHelper m_editorHelper;
     
     /** handles the business process operations for this editor */
     private OMEditorBP m_omEditorBP;
     
     /** the tree viewer for unmapped Component Names in the Split Pane view */
     private TreeViewer m_compNameTreeViewer;
     
     /** the tree viewer for unmapped UI Elements in the Split Pane view */
     private TreeViewer m_uiElementTreeViewer;
     
     /** the tree viewer for mapped Component Names in the Split Pane view */
     private TreeViewer m_mappedComponentTreeViewer;
     
     /** the viewer for presenting the tree view within this editor */
     private TreeViewer m_treeViewer;
     
     /** the viewer for presenting the tree view within this editor */
     private TableViewer m_tableViewer;
 
     /** 
      * the component responsible for handling the profile 
      * configuration page 
      */
     private ObjectMappingConfigComponent m_mappingConfigComponent;
     
     /** current selection within the tree view of this editor */
     private IStructuredSelection m_treeSelection = StructuredSelection.EMPTY;
 
     /** action to cut TreeItems */
     private CutTreeItemActionOMEditor m_cutTreeItemAction;
 
     /** action to paste TreeItems */
     private PasteTreeItemActionOMEditor m_pasteTreeItemAction;
 
     /** updater for tree viewer based on changes to Component Names */
     private ComponentNameTreeViewerUpdater m_treeViewerUpdater;
 
     /** the action to revert all changes in the editor */
     private RevertEditorChangesHandler m_revertEditorChangesAction = 
         new RevertEditorChangesHandler();
 
     /** mapping: page number => selection provider for that page */
     private Map<Integer, ISelectionProvider> m_pageToSelectionProvider =
         new HashMap<Integer, ISelectionProvider>();
     
     /** selection changed listener for this editor */
     private EditorSelectionChangedListener m_editorSelectionChangedListener =
         new EditorSelectionChangedListener();
     
     /**
      * The SelectionChangedListener for this editor.
      * @author BREDEX GmbH
      * @created 04.04.2005
      */
     private class EditorSelectionChangedListener 
         implements ISelectionChangedListener {
         
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void selectionChanged(SelectionChangedEvent event) {
             if (!(event.getSelection() instanceof IStructuredSelection)) {
                 return;
             }
             m_treeSelection = (IStructuredSelection)event.getSelection();
             Object firstElement = m_treeSelection.getFirstElement();
             if (m_treeSelection.size() != 1) {
                 OMSetCategoryToMapInto.setEnabled(false);
                 return;
             }
             OMMarkInAutAction.setEnabled(
                 (firstElement instanceof IObjectMappingAssoziationPO)
                 && isOmmInAutStarted());
             if ((firstElement instanceof IObjectMappingCategoryPO 
                     && (isCorrectMainCategory(
                         (IObjectMappingCategoryPO)firstElement))
                     && getAut().getObjMap().getUnmappedTechnicalCategory()
                         .equals(OMEditorDndSupport.getSection(
                             ((IObjectMappingCategoryPO)firstElement))))
                 && isOmmInAutStarted()) {
                 
                 OMSetCategoryToMapInto.setEnabled(true);
                 return;
             }
             OMSetCategoryToMapInto.setEnabled(false); 
         }
         
         /**
          * @param firstElement the first element of the current selection (<code>CategoryGUI</code>)
          * @return true, if the main category of the selected category is <code>OMUnmappedTechNameGUI</code>
          */
         private boolean isCorrectMainCategory(
                 IObjectMappingCategoryPO firstElement) {
             
             IObjectMappingCategoryPO category = firstElement;
             while (category.getParent() != null) {
                 category = category.getParent();
             }
             return category.equals(
                     getAut().getObjMap().getUnmappedTechnicalCategory());
         }
 
         /**
          * @return true, if the object mapping mode was started in current aut
          */
         private boolean isOmmInAutStarted() {
             if (TestExecution.getInstance().getConnectedAut() != null) {
                 switch (AUTModeChangedCommand.getAutMode()) {
                     case ChangeAUTModeMessage.OBJECT_MAPPING:
                         if (getAut().equals(
                             TestExecution.getInstance().getConnectedAut())) {
                             
                             return true;
                         }
                     default:
                 }
             }
             return false;
         }
     }
 
     /**
      * Always provides an empty selection. Does not track selection listeners.
      *
      * @author BREDEX GmbH
      * @created Jan 20, 2009
      */
     private static class NullSelectionProvider implements ISelectionProvider {
 
         /**
          * {@inheritDoc}
          */
         public void addSelectionChangedListener(
                 ISelectionChangedListener listener) {
 
             // Do nothing
         }
 
         /**
          * {@inheritDoc}
          */
         public ISelection getSelection() {
             return StructuredSelection.EMPTY;
         }
 
         /**
          * {@inheritDoc}
          */
         public void removeSelectionChangedListener(
                 ISelectionChangedListener listener) {
 
             // Do nothing
         }
 
         /**
          * {@inheritDoc}
          */
         public void setSelection(ISelection selection) {
             // Do nothing.
         }
         
     }
     
     /**
      * Strategy for activating cell editors in the object mapping editor's 
      * table view.
      *
      * @author BREDEX GmbH
      * @created Jan 20, 2009
      */
     private static class OMTableEditorActivationStrategy 
             extends ColumnViewerEditorActivationStrategy {
 
         /**
          * Constructor
          * 
          * @param viewer The viewer that will use this strategy.
          */
         public OMTableEditorActivationStrategy(TableViewer viewer) {
             super(viewer);
         }
         
         /**
          * {@inheritDoc}
          */
         protected boolean isEditorActivationEvent(
                 ColumnViewerEditorActivationEvent event) {
             return event.eventType == ColumnViewerEditorActivationEvent
                                             .TRAVERSAL
                 || event.eventType == ColumnViewerEditorActivationEvent
                                             .MOUSE_CLICK_SELECTION
                 || event.eventType == ColumnViewerEditorActivationEvent
                                             .PROGRAMMATIC;
         }
 
     }
     
     /**
      * Sorter for the Object Mapping Editor's tree view.
      *
      * @author BREDEX GmbH
      * @created Mar 10, 2009
      */
     private static class ObjectMappingTreeSorter extends ViewerSorter {
         /**
          * {@inheritDoc}
          */
         public int category(Object element) {
             if (element instanceof IObjectMappingCategoryPO) {
                 return 0;
             } else if (element instanceof IObjectMappingAssoziationPO) {
                 return 1;
             } else if (element instanceof IComponentNamePO) {
                 return 2;
             }
             return super.category(element);
         }
     }
     
     /**
      * Editing support for the Component Name column of the 
      * Object Mapping table.
      *
      * @author BREDEX GmbH
      * @created Apr 2, 2009
      */
     private class ObjectMappingCompNameEditingSupport 
             extends AbstractObjectMappingEditingSupport {
         
         /**
          * Constructor
          * 
          * @param compNameMapper
          *            The mapper to use for finding and modifying 
          *            Component Names.
          * @param viewer
          *            The viewer where the editing will take place.
          */
         public ObjectMappingCompNameEditingSupport(
                 IComponentNameMapper compNameMapper, TableViewer viewer) {
             super(compNameMapper, viewer);
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         protected Object getValue(Object element) {
             ObjectMappingRow row = (ObjectMappingRow)element;
             int logicalNameIndex = row.getLogicalNameIndex();
             if (logicalNameIndex < 0) {
                 return StringConstants.EMPTY;
             }
             return getCompMapper().getCompNameCache().getName(
                     row.getAssociation().getLogicalNames()
                         .get(logicalNameIndex));
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         protected void doSetValue(Object element, Object value) {
             Object oldValue = getValue(element);
             
             boolean isSameValue = oldValue == null 
                 ? value == null : oldValue.equals(value);
             if (!isSameValue) {
                 if (value != null 
                         && value.toString().trim().length() > 0) {
                     if (getEditorHelper().requestEditableState() 
                             != EditableState.OK) {
                         return;
                     }
                     ObjectMappingRow row = 
                         (ObjectMappingRow)element;
                     final IWritableComponentNameMapper mapper = 
                         getCompMapper();
                     String oldGuid = 
                         mapper.getCompNameCache().getGuidForName(
                                 String.valueOf(oldValue));
                     String newGuid = 
                         mapper.getCompNameCache().getGuidForName(
                                 value.toString());
                     if (newGuid == null) {
                         String compType = 
                             ComponentBuilder.getInstance()
                                 .getCompSystem()
                                 .getMostAbstractComponent()
                                 .getType();
                         newGuid = mapper.getCompNameCache()
                             .createComponentNamePO(
                                 value.toString(), compType, 
                                 CompNameCreationContext
                                     .OBJECT_MAPPING).getGuid();
                     }
                     OMEditorDndSupport
                         .checkAndSwapComponentNames(
                                 row.getAssociation(), 
                                 oldGuid, newGuid, 
                                 ObjectMappingMultiPageEditor.this);
                 }
             }
         }
 
     }
     
     /**
      * This class operates on any node of the test suite tree to extract the
      * component names. They are added to the object mapping tree and stored in
      * the member <code>m_componentNames</code>.
      */
     private class CollectLogicalNamesOp implements ITreeNodeOperation<INodePO> {
         /** Number of added component names (nodes). */
         private int m_addedNodeCount = 0;
         /** list of added GuiNodes */
         private List<IObjectMappingAssoziationPO> m_addedNodes = 
             new ArrayList<IObjectMappingAssoziationPO>();
         /** The business process that performs component name operations */
         private CompNamesBP m_compNamesBP = new CompNamesBP();
         
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (Persistor.isPoSubclass(node, ICapPO.class)) {
                 final ICapPO cap = (ICapPO)node;
                 CompNameResult result = 
                     m_compNamesBP.findCompName(ctx.getCurrentTreePath(), 
                             cap, cap.getComponentName(),
                             getCompMapper().getCompNameCache());
                 final IComponentNamePO compNamePo = 
                     getCompMapper().getCompNameCache().getCompNamePo(
                             result.getCompName());
                 if (compNamePo != null) {
                     if (!(cap.getMetaComponentType() 
                                 instanceof ConcreteComponent
                             && ((ConcreteComponent)cap.getMetaComponentType())
                                 .hasDefaultMapping())
                             && m_omEditorBP.getAssociation(
                                     compNamePo.getGuid()) == null) {
                         if (getEditorHelper().requestEditableState() 
                                 != EditableState.OK) {
                             return true;
                         }
 
                         IObjectMappingAssoziationPO assoc = 
                             PoMaker.createObjectMappingAssoziationPO(
                                     null, new ArrayList<String>());
                         try {
                             getCompMapper().changeReuse(
                                     assoc, null, compNamePo.getGuid());
                             getAut().getObjMap().getUnmappedLogicalCategory()
                                 .addAssociation(assoc);
                             m_addedNodes.add(assoc);
                             m_addedNodeCount++;
                         } catch (IncompatibleTypeException e) {
                             ErrorHandlingUtil.createMessageDialog(
                                     e, e.getErrorMessageParams(), null);
                         } catch (PMException pme) {
                             // Should not happen since we are assigning the
                             // Component Name to an unmapped association.
                             // Log it just in case.
                             LOG.error(Messages.ErrorCollectingComponentNames
                                     + StringConstants.DOT, pme);
                         }
                     }
                 }
             }
             return true;
         }
         
         /**
          * {@inheritDoc}
          */
         public void postOperate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             // no op
         }
         
         /**
          * @return Returns the addedNodeCount.
          */
         public int getAddedNodeCount() {
             return m_addedNodeCount;
         }
         
         /**
          * @return Returns the addedNodeCount.
          */
         public List<IObjectMappingAssoziationPO> getAddedNodes() {
             return m_addedNodes;
         }
     }
     
     /** listener for action enablement */
     private ActionListener m_actionListener = new ActionListener();
     
     /** the selection provider for this editor */
     private SelectionProviderIntermediate m_selectionProvider;
     
     /** the active tree viewer */
     private TreeViewer m_activeTreeViewer = null;
     
     /**
      * <code>m_treeFilterText</code>tree Viewer
      */
     private Text m_treeFilterText;
 
     /** selection provider for the split pane view */
     private SelectionProviderIntermediate m_splitPaneSelectionProvider;
     
     /**
      * SelectionListener to en-/disable delete-action
      * 
      * @author BREDEX GmbH
      * @created 02.03.2006
      */
     private class ActionListener implements ISelectionChangedListener {
 
         /**
          * {@inheritDoc}
          * @param event
          */
         public void selectionChanged(SelectionChangedEvent event) {
             if (GeneralStorage.getInstance().getProject() == null
                     || (event.getSelection() == null 
                             || event.getSelection().isEmpty())) {
                 
                 m_cutTreeItemAction.setEnabled(false);
                 m_pasteTreeItemAction.setEnabled(false);
                 return;
             }
             if (event.getSelection() instanceof IStructuredSelection) {
                 IStructuredSelection sel = 
                     (IStructuredSelection)event.getSelection();
                 enableCutAction(sel);
                 enablePasteAction(sel);
             }
         }
 
         /**
          * en-/disable cut-action
          * @param sel actual selection 
          */
         @SuppressWarnings("synthetic-access")
         private void enableCutAction(IStructuredSelection sel) {
             boolean onlyCategoriesSelected = false;
             boolean categoryIsNotInSelList = false;
             boolean onlyMainCategoriesSelected = false;
             boolean mainCategoryIsNotInSelList = false;
             boolean techNamesSelected = false;
             boolean logicNamesSelected = false;
             if (sel != null && !sel.isEmpty()) {
                 int selSize = sel.toList().size();
                 int[] counter = SelectionChecker.selectionCounter(sel);
                 onlyCategoriesSelected = 
                     counter[SelectionChecker.OM_CATEGORY] == selSize;
                 categoryIsNotInSelList = 
                     counter[SelectionChecker.OM_CATEGORY] == 0;
                 onlyMainCategoriesSelected = 
                     counter[SelectionChecker.OM_MAIN_CATEGORY] == selSize;
                 mainCategoryIsNotInSelList = 
                     counter[SelectionChecker.OM_MAIN_CATEGORY] == 0;
                 techNamesSelected = 
                     counter[SelectionChecker.OM_TECH_NAME] > 0;
                 logicNamesSelected = 
                     counter[SelectionChecker.OM_LOGIC_NAME] > 0;
             }
             // FIXME zeb workaround for delete-orphan mapping om categories to 
             //           child om categories
             // workaround: disallow cutting of categories
             if (!categoryIsNotInSelList) {
                 m_cutTreeItemAction.setEnabled(false);
                 return;
             }
             // FIXME zeb end workaround
             if (onlyMainCategoriesSelected) {
                 m_cutTreeItemAction.setEnabled(false);
                 return;
             }
             if (onlyCategoriesSelected) {
                 m_cutTreeItemAction.setEnabled(true);
                 return;
             }
             if (techNamesSelected && logicNamesSelected) {
                 m_cutTreeItemAction.setEnabled(false);
                 return;
             }
             if (categoryIsNotInSelList && mainCategoryIsNotInSelList) {
                 m_cutTreeItemAction.setEnabled(true);
                 return;
             }
             m_cutTreeItemAction.setEnabled(false);
         }
 
         /**
          * 
          * @param toMove The associations on the clipboard.
          * @param targetList The currently selected elements.
          * @return <code>true</code> if the paste operation should be
          *         enabled for the given arguments. Otherwise, 
          *         <code>false</code>.
          */
         private boolean getPasteActionEnablementForAssocs(
                 List<IObjectMappingAssoziationPO> toMove, 
                 List<Object> targetList) {
 
             for (Object target : targetList) {
                 if (target instanceof IObjectMappingCategoryPO) {
                     if (!OMEditorDndSupport.canMoveAssociations(
                             toMove, (IObjectMappingCategoryPO)target, 
                             ObjectMappingMultiPageEditor.this)) {
 
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
             return true;
         }
         
         /**
          * @param targetList The currently selected elements.
          * @return <code>true</code> if the paste operation should be
          *         enabled for the given arguments. Otherwise, 
          *         <code>false</code>.
          */
         private boolean getPasteActionEnablementForCompNames(
                 List<Object> targetList) {
             
             for (Object target : targetList) {
                 if (target instanceof IObjectMappingAssoziationPO) {
                     return true;
                 } else if (target instanceof IObjectMappingCategoryPO) {
                     if (!OMEditorDndSupport.canMoveCompNames(
                             (IObjectMappingCategoryPO)target, 
                             ObjectMappingMultiPageEditor.this)) {
 
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
 
             return true;
         }
         
         /**
          * en-/disable paste-action
          * @param sel actual selection 
          */
         @SuppressWarnings("synthetic-access")
         private void enablePasteAction(IStructuredSelection sel) {
             
             m_pasteTreeItemAction.setEnabled(false);
             LocalSelectionClipboardTransfer transfer = 
                 LocalSelectionClipboardTransfer.getInstance();
             Object cbContents = 
                 getEditorHelper().getClipboard().getContents(transfer);
 
             if (cbContents == null) {
                 return;
             }
             if (transfer.getSource() != null 
                 && !transfer.getSource().equals(getTreeViewer())) {
                 return;
             }
             boolean isEnabled = false;
             if (transfer.containsOnlyType(IObjectMappingAssoziationPO.class)) {
                 // Use logic for validating associations
                 isEnabled = getPasteActionEnablementForAssocs(
                         transfer.getSelection().toList(), 
                         sel.toList());
             } else if (transfer.containsOnlyType(
                     IObjectMappingCategoryPO.class)) {
                 // Use logic for validating categories
                 isEnabled = false;
             } else if (transfer.containsOnlyType(IComponentNamePO.class)) {
                 // Use logic for validating Component Names
                 isEnabled = getPasteActionEnablementForCompNames(sel.toList());
             } else {
                 isEnabled = false;
             }
             
             m_pasteTreeItemAction.setEnabled(isEnabled);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     protected void createPages() {
         if (m_editorHelper == null) {
             m_editorHelper = new JBEditorHelper(this);
         }
         m_omEditorBP = new OMEditorBP(this);
         IObjectMappingPO objMap = getAut().getObjMap();
         if (objMap == null) {
             objMap = PoMaker.createObjectMappingPO();
             getAut().setObjMap(objMap);
         }
         checkMasterSessionUpToDate();
         
         createActions();
 
         // Create menu manager.
         MenuManager menuMgr = new MenuManager();
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager mgr) {
                 fillTreeContextMenu(mgr);
             }
         });
 
         GuiEventDispatcher.getInstance().addEditorDirtyStateListener(
                 this, true);
         getEditorHelper().addListeners();
         getOmEditorBP().collectNewLogicalComponentNames();
         
         int splitPaneViewIndex = addPage(
                 createSplitPanePageControl(getContainer(), menuMgr));
         int treeViewIndex = addPage(createTreePageControl(getContainer(), 
                 menuMgr));
         int tableViewIndex = addPage(createTablePageControl(getContainer()));
         int configViewIndex = addPage(createConfigPageControl(getContainer()));
 
         setPageText(
                 splitPaneViewIndex, 
                 Messages.ObjectMappingEditorSplitPaneView);
         setPageText(
                 treeViewIndex, 
                 Messages.ObjectMappingEditorTreeView);
         setPageText(
                 tableViewIndex, 
                 Messages.ObjectMappingEditorTableView);
 
         setPageText(
                 configViewIndex, 
                 Messages.ObjectMappingEditorConfigView);
         
         m_pageToSelectionProvider.put(splitPaneViewIndex, 
                 m_splitPaneSelectionProvider);
         m_pageToSelectionProvider.put(treeViewIndex, m_treeViewer);
         m_pageToSelectionProvider.put(tableViewIndex, m_tableViewer);
         m_pageToSelectionProvider.put(
                 configViewIndex, new NullSelectionProvider());
         
         m_selectionProvider = new SelectionProviderIntermediate();
         m_selectionProvider.setSelectionProviderDelegate(
                 m_pageToSelectionProvider.get(getActivePage()));
         getSite().setSelectionProvider(m_selectionProvider);
 
         m_selectionProvider.addSelectionChangedListener(m_actionListener);
         m_selectionProvider.addSelectionChangedListener(
                 m_editorSelectionChangedListener);
         
         ObjectMappingEventDispatcher.addObserver(this);
 
         m_treeViewerUpdater = 
             new ComponentNameTreeViewerUpdater(m_treeViewer);
 
         checkAndFixInconsistentData();
         m_treeViewer.expandToLevel(2);
     }
 
     /**
      * Checks whether data from the editor input is inconsistent and fixes
      * any inconsistencies, saving immediately afterward if necessary.
      */
     private void checkAndFixInconsistentData() {
         
         boolean isChanged = false;
         
         IObjectMappingPO objMap = getAut().getObjMap();
         IComponentNameCache compNameCache = 
             getEditorHelper().getEditSupport()
                 .getCompMapper().getCompNameCache();
         
         isChanged |= fixCompNameReferences(objMap, compNameCache);
         isChanged |= removeDeletedCompNames(objMap, compNameCache);
         
         if (isChanged) {
             try {
                 final EditSupport editSupport = m_editorHelper.getEditSupport();
                 editSupport.lockWorkVersion();
                 m_editorHelper.setDirty(true);
                 doSave(new NullProgressMonitor());
             } catch (PMAlreadyLockedException e) {
                 // ignore, we are only doing housekeeping
             } catch (PMException e) {
                 PMExceptionHandler.handlePMExceptionForMasterSession(e);
             }
         }
     }
 
     /**
      * Removes deleted Component Names and empty associations from the given
      * Object Map.
      * 
      * @param objectMap The Object Map to fix.
      * @param compNameCache The cache to use for retrieving Component Names.
      * 
      * @return <code>true</code> if this method call caused any change
      *         (i.e. if any Component Names were removed). 
      *         Otherwise, <code>false</code>.
      */
     private boolean removeDeletedCompNames(
             IObjectMappingPO objectMap, 
             IComponentNameCache compNameCache) {
 
         boolean isChanged = false;
 
         Set<IObjectMappingAssoziationPO> assocsToDelete = 
             new HashSet<IObjectMappingAssoziationPO>();
         
         for (IObjectMappingAssoziationPO assoc : objectMap.getMappings()) {
             if (assoc.getTechnicalName() == null) {
                 Set<String> compNamesToRemove = new HashSet<String>();
                 for (String compNameGuid : assoc.getLogicalNames()) {
                     if (compNameCache.getCompNamePo(compNameGuid) == null) {
                         compNamesToRemove.add(compNameGuid);
                     }
                 }
                 for (String toRemove : compNamesToRemove) {
                     assoc.removeLogicalName(toRemove);
                     isChanged = true;
                 }
                 if (assoc.getLogicalNames().isEmpty()) {
                     isChanged = true;
                     assocsToDelete.add(assoc);
                 }
             }
         }
         for (IObjectMappingAssoziationPO assoc : assocsToDelete) {
             assoc.getCategory().removeAssociation(assoc);
             getEditorHelper().getEditSupport().getSession()
                 .remove(assoc);
         }
 
         return isChanged;
     }
 
     /**
      * Initializes the actions for this editor.
      */
     private void createActions() {
         m_cutTreeItemAction = new CutTreeItemActionOMEditor();
         m_pasteTreeItemAction = new PasteTreeItemActionOMEditor();
     }
 
     /**
      * Creates the profile configuration page of the editor.
      * 
      * @param parent The parent composite.
      * @return the base control of the profile configuration page.
      */
     private Control createConfigPageControl(Composite parent) {
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.verticalSpacing = 3;
         layout.marginWidth = LayoutUtil.MARGIN_WIDTH;
         layout.marginHeight = LayoutUtil.MARGIN_HEIGHT;
         parent.setLayout(layout);
         Composite configComposite = new Composite(parent, SWT.NONE);
         GridData gridData = 
             new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
         configComposite.setLayoutData(gridData);
 
         layout = new GridLayout();
         layout.numColumns = 1;
         layout.verticalSpacing = 3;
         layout.marginWidth = LayoutUtil.MARGIN_WIDTH;
         layout.marginHeight = LayoutUtil.MARGIN_HEIGHT;
         configComposite.setLayout(layout);
         
         m_mappingConfigComponent = new ObjectMappingConfigComponent(
                 configComposite, getAut().getObjMap(), this);
         
         createConfigContextMenu(configComposite);
         
         return configComposite;
     }
     
     /**
      * Creates the tree page of the editor.
      * 
      * @param parent The parent composite.
      * @param contextMenuMgr The manager for the context menu for the created
      *                       tree.
      * @return the base control of the tree view.
      */
     private Control createTreePageControl(Composite parent,
             MenuManager contextMenuMgr) {
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.verticalSpacing = 3;
         layout.marginWidth = LayoutUtil.MARGIN_WIDTH;
         layout.marginHeight = LayoutUtil.MARGIN_HEIGHT;
         parent.setLayout(layout);
         SashForm treeComp = new SashForm(parent, SWT.MULTI);
         GridLayout compLayout = new GridLayout(1, false);
         compLayout.marginWidth = 0;
         compLayout.marginHeight = 0;
         treeComp.setLayout(compLayout);
         GridData gridData = 
             new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_BOTH);
         treeComp.setLayoutData(gridData); 
         final FilteredTree ft = new JBFilteredTree(treeComp, SWT.MULTI
                 | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, 
                 new ObjectMappingEditorPatternFilter(), true);
         m_treeViewer = ft.getViewer();
         setTreeFilterText(ft.getFilterControl());
         setProviders(m_treeViewer, getCompMapper());
         m_treeViewer.setUseHashlookup(true);
         m_treeViewer.setSorter(new ObjectMappingTreeSorter());
         m_treeViewer.setComparer(new PersistentObjectComparer());
         // setup drag&drop support
         int ops = DND.DROP_MOVE;
         Transfer[] transfers = 
             new Transfer[] { 
                 LocalSelectionTransfer.getInstance()};
         m_treeViewer.addDragSupport(ops, transfers,
             new LimitingDragSourceListener(m_treeViewer, getAut()));
         m_treeViewer.addDropSupport(ops, transfers, 
             new OMDropTargetListener(this, m_treeViewer));
         m_omEditorBP = new OMEditorBP(this);
         m_treeViewer.setAutoExpandLevel(2);
         m_treeViewer.setInput(getAut().getObjMap());
         createTreeContextMenu(m_treeViewer, contextMenuMgr);
         JubulaStateController.getInstance().
             addSelectionListenerToSelectionService();
         Plugin.getHelpSystem().setHelp(parent,
             ContextHelpIds.OBJECT_MAP_EDITOR);
         
         configureActionBars();
         
         FocusListener activeTreeListener = new FocusAdapter() {
             public void focusGained(FocusEvent e) {
                 m_activeTreeViewer = m_treeViewer;
             }
         };
         
         m_treeViewer.getTree().addFocusListener(activeTreeListener);
         ft.getFilterControl().addFocusListener(activeTreeListener);
         
         return treeComp;
     }
     
     /**
      * Creates the split pane page of the editor.
      * 
      * @param parent The parent composite.
      * @return the base control of the split pane view.
      * @param contextMenuMgr The manager for the context menu for the created
      *                       trees.
      */
     private Control createSplitPanePageControl(Composite parent,
             MenuManager contextMenuMgr) {
         
         m_splitPaneSelectionProvider = new SelectionProviderIntermediate();
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.verticalSpacing = 3;
         layout.marginWidth = LayoutUtil.MARGIN_WIDTH;
         layout.marginHeight = LayoutUtil.MARGIN_HEIGHT;
         parent.setLayout(layout);
         SashForm mainSash = new SashForm(parent, SWT.VERTICAL);
         SashForm topSash = new SashForm(mainSash, SWT.HORIZONTAL);
 
         m_compNameTreeViewer = createSplitPaneViewer(topSash, 
                 "ObjectMappingEditor.UnAssignedLogic", //$NON-NLS-1$
                 getAut().getObjMap().getUnmappedLogicalCategory(),
                 contextMenuMgr);
 
         m_splitPaneSelectionProvider.setSelectionProviderDelegate(
                 m_compNameTreeViewer);
         
         m_uiElementTreeViewer = createSplitPaneViewer(topSash, 
                 "ObjectMappingEditor.UnAssignedTech", //$NON-NLS-1$
                 getAut().getObjMap().getUnmappedTechnicalCategory(),
                 contextMenuMgr);
 
         m_mappedComponentTreeViewer = createSplitPaneViewer(mainSash,
                 "ObjectMappingEditor.Assigned", //$NON-NLS-1$
                 getAut().getObjMap().getMappedCategory(),
                 contextMenuMgr);
         
         linkSelection(new TreeViewer[] {
             m_compNameTreeViewer, m_uiElementTreeViewer, 
             m_mappedComponentTreeViewer});
         
         Plugin.getHelpSystem().setHelp(parent,
             ContextHelpIds.OBJECT_MAP_EDITOR);
         
         return mainSash;
     }
 
     /**
      * "Links" the selections of the given viewers. This means that the viewer
      * selections are mutually exclusive (i.e. if something is already selected 
      * in viewer 1, and something becomes selected in viewer 2, then the 
      * selection in viewer 1 is cleared). This method also adds the given 
      * viewers to the Split Pane view's selection provider group.
      * 
      * @param treeViewersToLink The viewers to link.
      */
     private void linkSelection(final TreeViewer[] treeViewersToLink) {
         for (final TreeViewer viewer : treeViewersToLink) {
             viewer.addSelectionChangedListener(new ISelectionChangedListener() {
                 public void selectionChanged(SelectionChangedEvent event) {
                     if (event.getSelection() != null 
                             && !event.getSelection().isEmpty()) {
                         
                         m_splitPaneSelectionProvider
                             .setSelectionProviderDelegate(viewer);
                         for (TreeViewer viewerToDeselect : treeViewersToLink) {
                             if (viewer != viewerToDeselect) {
                                 viewerToDeselect.setSelection(
                                         StructuredSelection.EMPTY);
                             }
                         }
                     }
                 }
             });
         }
     }
 
     /**
      * Creates and returns a tree viewer suitable for use in the split pane 
      * view.
      * 
      * @param parent The parent composite for the viewer.
      * @param title the title to display for the viewer.
      * @param topLevelCategory The input for the viewer.
      * @param contextMenuMgr The manager for the context menu for the created
      *                       tree.
      * @return the created viewer.
      */
     private TreeViewer createSplitPaneViewer(
             Composite parent,
             String title,
             IObjectMappingCategoryPO topLevelCategory,
             MenuManager contextMenuMgr) {
 
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new GridLayout());
         Label titleLabel = new Label(composite, SWT.NONE);
         titleLabel.setText(I18n.getString(title));
         titleLabel.setLayoutData(
                 GridDataFactory.defaultsFor(titleLabel).create());
 
         final TreeViewer viewer = new TreeViewer(composite);
         
         viewer.getTree().setLayoutData(
                 GridDataFactory.fillDefaults().grab(true, true).create());
         setProviders(viewer, getCompMapper());
         viewer.setUseHashlookup(true);
         viewer.setSorter(new ObjectMappingTreeSorter());
         viewer.setComparer(new PersistentObjectComparer());
         viewer.setInput(topLevelCategory);
 
         Transfer[] transfers = 
             new Transfer[] { 
                 org.eclipse.jface.util.LocalSelectionTransfer.getTransfer()};
         viewer.addDragSupport(DND.DROP_MOVE, transfers,
                 new LimitingDragSourceListener(viewer, getAut()));
         viewer.addDropSupport(DND.DROP_MOVE, transfers, 
             new OMDropTargetListener(this, viewer));
 
         createTreeContextMenu(viewer, contextMenuMgr);
 
         DialogUtils.setWidgetName(viewer.getTree(), title);
         
         IFocusService focusService = 
             (IFocusService)getSite().getService(IFocusService.class);
         
         focusService.addFocusTracker(viewer.getTree(), title);
         viewer.getTree().addFocusListener(new FocusAdapter() {
             public void focusGained(FocusEvent e) {
                 m_activeTreeViewer = viewer;
             }
         });
         
         return viewer;
     }
     
     /**
      * Registers global action handlers and listeners. 
      */
     private void configureActionBars() {
         getTreeFilterText().addFocusListener(new FocusListener() {
             /** the default cut action */
             private IAction m_defaultCutAction = getEditorSite()
                 .getActionBars().getGlobalActionHandler(
                         ActionFactory.CUT.getId()); 
             
             /** the default paste action */
             private IAction m_defaultPasteAction = getEditorSite()
                 .getActionBars().getGlobalActionHandler(
                     ActionFactory.PASTE.getId());
             
             public void focusGained(FocusEvent e) {
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.CUT.getId(), m_defaultCutAction);
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), m_defaultPasteAction);
                 getEditorSite().getActionBars().updateActionBars();
             }
 
             public void focusLost(FocusEvent e) {
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.CUT.getId(), m_cutTreeItemAction);
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), m_pasteTreeItemAction);
                 getEditorSite().getActionBars().updateActionBars();
             }
         });
         
         getEditorSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.CUT.getId(), m_cutTreeItemAction);
         getEditorSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.PASTE.getId(), m_pasteTreeItemAction);
         getEditorSite().getActionBars().updateActionBars();
     }
 
     /**
      * @param treeFilterText the treeFilterText to set
      */
     public void setTreeFilterText(Text treeFilterText) {
         m_treeFilterText = treeFilterText;
     }
 
     /**
      * @return the treeFilterText
      */
     public Text getTreeFilterText() {
         return m_treeFilterText;
     }
 
     /**
      * 
      * @param viewer The viewer on which to create the context menu.
      * @param menuMgr The manager for the context menu.
      */
     private void createTreeContextMenu(TreeViewer viewer, MenuManager menuMgr) {
         // Create menu.
         Menu menu = menuMgr.createContextMenu(viewer.getControl());
         viewer.getControl().setMenu(menu);
     }
 
     /**
      * fill the tree context menu
      * 
      * @param mgr
      *            IMenuManager
      */
     protected void fillTreeContextMenu(IMenuManager mgr) {
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.NEW_CATEGORY_COMMAND_ID);
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         mgr.add(m_cutTreeItemAction);
         mgr.add(m_pasteTreeItemAction);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.DELETE_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.RENAME_COMMAND_ID);
         mgr.add(SearchTreeAction.getAction());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.EXPAND_TREE_ITEM_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.REVERT_CHANGES_COMMAND_ID);
         mgr.add(new Separator());
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REFRESH_COMMAND_ID);
         mgr.add(new Separator());
         mgr.add(new Separator());
         mgr.add(OMSetCategoryToMapInto.getAction());
         mgr.add(OMMarkInAutAction.getAction());
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.SHOW_WHERE_USED_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.SHOW_RESPONSIBLE_NODE_COMMAND_ID);
         mgr.add(new Separator());
         MenuManager submenuNew = new MenuManager(
                 Messages.ObjectMappingEditorCleanupMenu, CLEANUP_ID);
         CommandHelper.createContributionPushItem(submenuNew,
                 RCPCommandIDs.OME_DELETE_UNUSED_COMPONENT_NAME_COMMAND_ID);
         mgr.add(submenuNew);
     }
 
     /**
      * Create context menu for the configuration editor view.
      * 
      * @param configComposite The composite that holds the configuration page.
      */
     private void createConfigContextMenu(Composite configComposite) {
         // Create menu manager.
         MenuManager menuMgr = new MenuManager();
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager mgr) {
                 fillConfigContextMenu(mgr);
             }
         });
         // Create menu.
         Menu menu = menuMgr.createContextMenu(configComposite);
         setConfigContextMenu(configComposite, menu);
     }
 
     /**
      * Recursively sets the context menu for <code>control</code> and all of its
      * children to <code>menu</code>.
      * 
      * @param control The start point for setting the menu.
      * @param menu The menu to use.
      */
     private void setConfigContextMenu(Control control, Menu menu) {
         control.setMenu(menu);
         if (control instanceof Composite) {
             for (Control child : ((Composite)control).getChildren()) {
                 setConfigContextMenu(child, menu);
             }
         }
     }
     
     /**
      * fill the tree context menu
      * 
      * @param mgr
      *            IMenuManager
      */
     protected void fillConfigContextMenu(IMenuManager mgr) {
         CommandHelper.createContributionPushItem(mgr, 
                 RCPCommandIDs.REVERT_CHANGES_COMMAND_ID);
     }
 
     /**
      * Create context menu for the tree-based editor view.
      */
     private void createTableContextMenu() {
         // Create menu manager.
         MenuManager menuMgr = new MenuManager();
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager mgr) {
                 fillTableContextMenu(mgr);
             }
         });
         // Create menu.
         Menu menu = menuMgr.createContextMenu(m_tableViewer.getControl());
         m_tableViewer.getControl().setMenu(menu);
     }
 
     /**
      * fill the tree context menu
      * 
      * @param mgr
      *            IMenuManager
      */
     protected void fillTableContextMenu(IMenuManager mgr) {
         mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
         CommandHelper.createContributionPushItem(mgr,
                 RCPCommandIDs.REVERT_CHANGES_COMMAND_ID);
         CommandHelper.createContributionPushItem(mgr,
                 CommandIDs.REFRESH_COMMAND_ID);
     }
 
     /**
      * Creates the table page of the editor.
      * 
      * @param parent The parent composite.
      * @return the base control of the table view.
      */
     private Control createTablePageControl(Composite parent) {
         m_tableViewer = new TableViewer(parent, 
                 SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
 
         addLogicalNameColumn(m_tableViewer);
         addTechNameColumn(m_tableViewer);
         addCategoryColumn(m_tableViewer);
         addCompTypeColumn(m_tableViewer);
 
         TableViewerEditor.create(
                 m_tableViewer, 
                 new OMTableEditorActivationStrategy(m_tableViewer), 
                 ColumnViewerEditor.TABBING_VERTICAL 
                     | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR 
                     | ColumnViewerEditor.TABBING_HORIZONTAL);
 
         m_tableViewer.setContentProvider(new OMEditorTableContentProvider());
         m_tableViewer.getTable().setLinesVisible(true);
         m_tableViewer.getTable().setHeaderVisible(true);
         TableColumn sortColumn = m_tableViewer.getTable().getColumn(0); 
         m_tableViewer.getTable().setSortColumn(sortColumn);
         m_tableViewer.getTable().setSortDirection(SWT.DOWN);
         ColumnSortListener sortListener = new ColumnSortListener(
                 m_tableViewer, sortColumn);
         m_tableViewer.setComparator(sortListener);
         for (TableColumn col 
                 : m_tableViewer.getTable().getColumns()) {
             
             col.addSelectionListener(sortListener);
         }
         m_tableViewer.setUseHashlookup(true);
         m_tableViewer.setInput(getAut().getObjMap());
         createTableContextMenu();
         return m_tableViewer.getControl();
     }
 
     /**
      * Adds a "Component Type" column to the given viewer.
      * 
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addCompTypeColumn(TableViewer tableViewer) {
         TableViewerColumn column = 
             new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setText(
                 Messages.ObjectMappingEditorComponentType);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new ColumnLabelProvider() {
 
             public String getText(Object element) {
                 ObjectMappingRow row = (ObjectMappingRow)element;
                 IObjectMappingAssoziationPO assoc = row.getAssociation(); 
                 
                 String text = StringConstants.EMPTY;
                 
                 if (row.getLogicalNameIndex() 
                         != ObjectMappingRow.NO_COMP_NAME) {
                     
                     String compNameGuid = 
                         assoc.getLogicalNames().get(row.getLogicalNameIndex());
                     IComponentNamePO compName = 
                         getCompMapper().getCompNameCache().getCompNamePo(
                                 compNameGuid);
                     
                     text = 
                         compName != null 
                             ? CompSystemI18n.getString(
                                     compName.getComponentType())
                                     : CompSystemI18n.getString(
                                             "CompNamesView.errorText"); //$NON-NLS-1$
                 }
 
                 return text;
             }
         });
         
         return column;
     }
 
     /**
      * Adds a "Category" column to the given viewer.
      * 
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addCategoryColumn(TableViewer tableViewer) {
         TableViewerColumn column = 
             new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.CATEGORY_IMAGE);
         column.getColumn().setText(
                 Messages.ObjectMappingEditorCategory);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new ColumnLabelProvider() {
 
             public String getText(Object element) {
                 ObjectMappingRow row = (ObjectMappingRow)element;
                 List<String> catPath = new ArrayList<String>();
                 IObjectMappingCategoryPO category = 
                     row.getAssociation().getCategory(); 
                 while (category != null) {
                     catPath.add(0, category.getName());
                     category = category.getParent();
                 }
                 StringBuilder sb = new StringBuilder();
                 Iterator<String> it = catPath.iterator();
                 // Skip the first element because it is the top-level
                 // categorization of "Mapped/UnmappedTech/UnmappedLogical".
                 // This information can just as easily be determined in the
                 // table view by looking at the "Tech Name" and "Logical Name"
                 // column values.
                 if (it.hasNext()) {
                     it.next();
                 }
                 while (it.hasNext()) {
                     sb.append(CAT_SEPARATOR).append(it.next());
                 }
                 return sb.toString(); 
             }
         });
         
         return column;
     }
 
     /**
      * Adds a "Logical Name" column to the given viewer.
      * 
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addLogicalNameColumn(TableViewer tableViewer) {
         TableViewerColumn column = 
             new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.LOGICAL_NAME_IMAGE);
         column.getColumn().setText(
                 Messages.ObjectMappingEditorLogicalName);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new ColumnLabelProvider() {
             public String getText(Object element) {
                 ObjectMappingRow row = (ObjectMappingRow)element;
                 int logicalNameIndex = row.getLogicalNameIndex();
                 if (logicalNameIndex < 0) {
                     return null;
                 }
                 return getCompMapper().getCompNameCache().getName(
                         row.getAssociation().getLogicalNames()
                             .get(logicalNameIndex));
             }
         });
         column.setEditingSupport(
                 new ObjectMappingCompNameEditingSupport(
                         getCompMapper(), tableViewer));
         
         return column;
     }
 
     /**
      * Adds a "Technical Name" column to the given viewer.
      * 
      * @param tableViewer The viewer to which the column will be added.
      * @return the added column.
      */
     private TableViewerColumn addTechNameColumn(final TableViewer tableViewer) {
         TableViewerColumn column = 
             new TableViewerColumn(tableViewer, SWT.NONE);
         column.getColumn().setWidth(200);
         column.getColumn().setImage(IconConstants.TECHNICAL_NAME_IMAGE);
         column.getColumn().setText(
                 Messages.ObjectMappingEditorTechnicalName);
         column.getColumn().setMoveable(true);
         column.setLabelProvider(new ColumnLabelProvider() {
             public String getText(Object element) {
                 IObjectMappingAssoziationPO assoc =
                     ((ObjectMappingRow)element).getAssociation();
                 IComponentIdentifier compId = assoc.getTechnicalName();
                 return compId != null ? compId.getComponentName() : null;
             }
             /**{@inheritDoc} */
             public Color getBackground(Object element) {
                 IObjectMappingAssoziationPO assoc =
                     ((ObjectMappingRow)element).getAssociation();
                 IComponentIdentifier compId = assoc.getCompIdentifier();
                 switch (OMEditorTreeLabelProvider.getQualitySeverity(compId)) {
                     case IStatus.OK:
                         return tableViewer.getTable().getDisplay()
                                 .getSystemColor(SWT.COLOR_GREEN);
                     case IStatus.WARNING:
                         return tableViewer.getTable().getDisplay()
                                 .getSystemColor(SWT.COLOR_YELLOW);
                     case IStatus.ERROR:
                         return tableViewer.getTable().getDisplay()
                                 .getSystemColor(SWT.COLOR_RED);
                     default:
                         break;
                 }
                 return null;
             }
         });
 
         return column;
     }
 
     /**
      * {@inheritDoc}
      */
     public void doSave(IProgressMonitor monitor) {
         monitor.beginTask(Messages.EditorsSaveEditors,
                 IProgressMonitor.UNKNOWN);
         boolean errorOccured = false;
         IObjectMappingPO objMap = getAut().getObjMap();
         TimestampBP.refreshTimestamp(objMap);
         try {
             if (getEditorHelper().isDirty()) {
                 EditSupport editSupport = getEditorHelper().getEditSupport();
                 IObjectMappingProfilePO origProfile = 
                     ((IAUTMainPO)editSupport.getOriginal()).getObjMap()
                         .getProfile();
                 IObjectMappingProfilePO workProfile = 
                     ((IAUTMainPO)editSupport.getWorkVersion()).getObjMap()
                         .getProfile();
 
                 IWritableComponentNameCache compNameCache = 
                     editSupport.getCompMapper().getCompNameCache();
                 Set<IComponentNamePO> renamedCompNames = 
                     new HashSet<IComponentNamePO>(
                             compNameCache.getRenamedNames());
                 Set<IComponentNamePO> reuseChangedCompNames = 
                     getCompNamesWithChangedReuse(compNameCache);
 
                 fixCompNameReferences(getAut().getObjMap(), 
                         getEditorHelper().getEditSupport()
                             .getCompMapper().getCompNameCache());
                 
                 editSupport.saveWorkVersion();
                 fireRenamedEvents(renamedCompNames);
                 fireReuseChangedEvents(reuseChangedCompNames);
                 if (getAut().equals(
                         TestExecution.getInstance().getConnectedAut())
                     && !workProfile.equals(origProfile)) {
                     
                     NagDialog.runNagDialog(
                             Plugin.getActiveWorkbenchWindowShell(),
                             "InfoNagger.ObjectMappingProfileChanged", //$NON-NLS-1$
                             ContextHelpIds.OBJECT_MAP_EDITOR); 
                 }
             }
             ComponentNamesBP.getInstance().init();
         } catch (PMException e) {
             PMExceptionHandler.handlePMExceptionForEditor(e, this);
             errorOccured = true;
         } catch (ProjectDeletedException e) {
             PMExceptionHandler.handleGDProjectDeletedException();
             errorOccured = true;
         } catch (IncompatibleTypeException ite) {
             ErrorHandlingUtil.createMessageDialog(
                     ite, ite.getErrorMessageParams(), null);
         } finally {
             monitor.done();
             if (!errorOccured) {
                 try {
                     reOpenEditor(((PersistableEditorInput)getEditorInput())
                         .getNode());
                 } catch (PMException e) {
                     PMExceptionHandler.handlePMExceptionForEditor(e, this);
                 }
             }
         }
     }
 
     /**
      * 
      * @param compNameCache The cache to use for finding Component Names.
      * @return all Component Names marked within <code>compNameCache</code> as
      *         having had their reuse changed.
      */
     private Set<IComponentNamePO> getCompNamesWithChangedReuse(
             IWritableComponentNameCache compNameCache) {
         Set<IComponentNamePO> reuseChangedCompNames = 
             new HashSet<IComponentNamePO>(); 
         for (String compNameGuid : compNameCache.getReusedNames()) {
             IComponentNamePO compName = 
                 compNameCache.getCompNamePo(compNameGuid);
             if (compName != null) {
                 reuseChangedCompNames.add(compName);
             }
         }
         return reuseChangedCompNames;
     }
 
     /**
      * Fires "ReuseChanged" data changed events for each given Component Name.
      * 
      * @param reuseChangedCompNames The Component Names for which to fire 
      *                              the events.
      */
     private void fireReuseChangedEvents(
             Set<IComponentNamePO> reuseChangedCompNames) {
         ArrayList<DataChangedEvent> events = new ArrayList<DataChangedEvent>();
         for (IComponentNamePO compName : reuseChangedCompNames) {
             events.add(new DataChangedEvent(compName, DataState.ReuseChanged,
                     UpdateState.all));
         }
         DataEventDispatcher.getInstance().fireDataChangedListener(
                 events.toArray(new DataChangedEvent[0]));
     }
 
     /**
      * Fires "Renamed" data changed events for each given Component Name.
      * 
      * @param renamedCompNames The Component Names for which to fire the events.
      */
     private void fireRenamedEvents(Set<IComponentNamePO> renamedCompNames) {
         ArrayList<DataChangedEvent> events = new ArrayList<DataChangedEvent>();
         for (IComponentNamePO compName : renamedCompNames) {
             events.add(new DataChangedEvent(compName, DataState.Renamed,
                     UpdateState.all));
         }
         DataEventDispatcher.getInstance().fireDataChangedListener(
                 events.toArray(new DataChangedEvent[0]));
     }
 
     /**
      * Replaces Component Name references with the referenced Component Names
      * and deletes any Component Name references that are no longer used.
      * 
      * @param objectMap The Object Map to fix.
      * @param compNameCache The cache to use for retrieving Component Names.
      * 
      * @return <code>true</code> if this method call caused any change
      *         (i.e. if any references were fixed). 
      *         Otherwise, <code>false</code>.
      */
     private boolean fixCompNameReferences(
             IObjectMappingPO objectMap, 
             IComponentNameCache compNameCache) {
         boolean isChanged = false;
         // Replace all reference guids with referenced guids
         for (IObjectMappingAssoziationPO assoc 
                 : objectMap.getMappings()) {
             Set<String> guidsToRemove = new HashSet<String>();
             for (String compNameGuid : assoc.getLogicalNames()) {
                 IComponentNamePO compNamePo = 
                     compNameCache.getCompNamePo(compNameGuid);
                 if (compNamePo != null 
                         && !compNamePo.getGuid().equals(compNameGuid)) {
                     guidsToRemove.add(compNameGuid);
                 }
             }
             for (String toRemove : guidsToRemove) {
                 isChanged = true;
                 assoc.removeLogicalName(toRemove);
             }
         }
 
         if (isChanged) {
             CompNamePM.removeUnusedCompNames(
                     GeneralStorage.getInstance().getProject().getId(), 
                     getEditorHelper().getEditSupport().getSession());
         }
  
         return isChanged;
     }
 
     /**
      * {@inheritDoc}
      */
     public void doSaveAs() {
         // do nothing
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     /**
      * 
      * @return the aut the editor is editing
      */
     public IAUTMainPO getAut() {
         return (IAUTMainPO)getEditorHelper().getEditSupport().getWorkVersion();
     }
 
     /**
      * Checks if the MasterSession is up to date.
      */
     private void checkMasterSessionUpToDate() {
         ITimestampPO objMap = getAut().getObjMap();
         final boolean isUpToDate = TimestampBP.refreshEditorNodeInMasterSession(
             objMap);
         if (!isUpToDate) {
             CompletenessBP.getInstance().completeProjectCheck();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Image getDisabledTitleImage() {
         return IconConstants.DISABLED_OM_EDITOR_IMAGE;
     }
 
     /**
      * {@inheritDoc}
      */
     public Composite getParentComposite() {
         return getContainer().getParent();
     }
 
     /**
      * {@inheritDoc}
      */
     public void reOpenEditor(IPersistentObject obj) throws PMException {
         getEditorHelper().setDirty(false);
         Object [] expandedElements = m_treeViewer.getExpandedElements();
         getEditorHelper().getEditSupport().close();
         PersistableEditorInput input = new PersistableEditorInput(obj);
         try {
             init(getEditorSite(), input);
             // MultiPageEditorPart sets the selection provider to a 
             // MultiPageSelectionProvider during init. We want to continue
             // using our own selection provider, so we re-set it here.
             m_selectionProvider.setSelectionProviderDelegate(
                     m_pageToSelectionProvider.get(getActivePage()));
             getSite().setSelectionProvider(m_selectionProvider);
             m_treeViewerUpdater =  new ComponentNameTreeViewerUpdater(
                     m_treeViewer);
             setProviders(m_treeViewer, getCompMapper());
             final IObjectMappingPO om = getAut().getObjMap();
             m_treeViewer.setInput(om);
             // Clearing the selection seems to help prevent the behavior 
             // noted in bug 334269
             m_treeViewer.setSelection(StructuredSelection.EMPTY);
             m_tableViewer.setInput(om);
             m_treeViewer.setExpandedElements(expandedElements);
             m_treeViewer.expandToLevel(2);
 
             m_mappingConfigComponent.setInput(om);
             Map<TreeViewer, IObjectMappingCategoryPO> viewerToInput = 
                 new HashMap<TreeViewer, IObjectMappingCategoryPO>();
             viewerToInput.put(m_compNameTreeViewer, 
                     om.getUnmappedLogicalCategory());
             viewerToInput.put(m_uiElementTreeViewer, 
                     om.getUnmappedTechnicalCategory());
             viewerToInput.put(m_mappedComponentTreeViewer, 
                     om.getMappedCategory());
 
             for (TreeViewer splitViewer : viewerToInput.keySet()) {
                 Object [] expandedSplitViewerElements = 
                     splitViewer.getExpandedElements();
                 setProviders(splitViewer, getCompMapper());
                 splitViewer.setInput(viewerToInput.get(splitViewer));
                 splitViewer.setExpandedElements(expandedSplitViewerElements);
                 // Clearing the selection seems to help prevent the behavior 
                 // noted in bug 334269
                 splitViewer.setSelection(StructuredSelection.EMPTY);
             }
         } catch (PartInitException e) {
             getSite().getPage().closeEditor(this, false);
         }
     }
 
     /**
      * Assigns new (Object Mapping related) content and label providers to 
      * the given viewer.
      * 
      * @param viewer The viewer to receive new providers.
      * @param compNameMapper The mapper to use to initialize the providers.
      */
     private static void setProviders(AbstractTreeViewer viewer,
             IWritableComponentNameMapper compNameMapper) {
         viewer.setLabelProvider(
                 new OMEditorTreeLabelProvider(compNameMapper));
         viewer.setContentProvider(
                 new OMEditorTreeContentProvider(compNameMapper));
     }
     
     /**
      * {@inheritDoc}
      */
     public void setFocus() {
         if (getActivePage() == SPLIT_PAGE_IDX) {
             if (!m_compNameTreeViewer.getSelection().isEmpty()) {
                 m_compNameTreeViewer.getControl().setFocus();
             } else if (!m_uiElementTreeViewer.getSelection().isEmpty()) {
                 m_uiElementTreeViewer.getControl().setFocus();
             } else {
                 m_mappedComponentTreeViewer.getControl().setFocus();
             }
         } else {
             super.setFocus();
         }
         Plugin.showStatusLine(this);
     }
 
     /**
      * {@inheritDoc}
      */
     public void fireDirtyProperty(boolean isDirty) {
         for (int i = 0; i < getPageCount(); i++) {
             if (i != getActivePage()) {
                 // Perform refresh
                 if (i == TREE_PAGE_IDX) {
                     Object [] expandedElements = 
                         m_treeViewer.getExpandedElements();
                     m_treeViewer.setInput(getAut().getObjMap());
                     m_treeViewer.refresh();
                     m_treeViewer.setExpandedElements(expandedElements);
                 }
                 if (i == TABLE_PAGE_IDX) {
                     m_tableViewer.setInput(getAut().getObjMap());
                     m_tableViewer.refresh();
                 }
             }
         }
         // fire property for change of dirty state
         firePropertyChange(IEditorPart.PROP_DIRTY);
         if (!isDirty) {
             firePropertyChange(IEditorPart.PROP_INPUT);
         }
     }
 
     /**
      * Signals the editor that a logical name has been added. The editor will
      * react to this message such that the newly added name is contained in the
      * editor's viewers.
      */
     public void logicalNameAdded() {
         for (int i = 0; i < getPageCount(); i++) {
             if (i == getActivePage()) {
                 if (i == TREE_PAGE_IDX) {
                     m_treeViewer.refresh();
                 }
                 if (i == TABLE_PAGE_IDX) {
                     m_tableViewer.refresh();
                 }
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public JBEditorHelper getEditorHelper() {
         return m_editorHelper;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getEditorPrefix() {
         return Messages.ObjectMappingEditorEditor;
     }
 
     /**
      * {@inheritDoc}
      */
     public void initTextAndInput(IEditorSite site, IEditorInput input) {
         setSite(site);
         setInput(input);
         setPartName(getEditorPrefix() + input.getName());
         getEditorSite().getActionBars().getMenuManager();
     }
 
     /**
      * {@inheritDoc}
      */
     public void update(final int event, final Object obj) {
         Plugin.getDisplay().syncExec(new Runnable() {
             @SuppressWarnings("synthetic-access")
             public void run() {
                 switchEvent(event, obj);
             }
 
         });
     }
 
     /**
      * inserts a new Technical Name into GUIModel
      * @param component IComponentIdentifier
      */
     private void createNewTechnicalName(final IComponentIdentifier component) {
         if (getEditorHelper().requestEditableState() != EditableState.OK) {
             return;
         }
         IObjectMappingAssoziationPO techNameAssoc = 
             getAut().getObjMap().addTechnicalName(component, getAut());
         if (techNameAssoc != null) {
             getEditorHelper().setDirty(true);
             if (m_omEditorBP.getCategoryToCreateIn() != null) {
                 m_omEditorBP.getCategoryToCreateIn()
                     .addAssociation(techNameAssoc);
             } else {
                 getAut().getObjMap().getUnmappedTechnicalCategory()
                     .addAssociation(techNameAssoc);
             }
             DataEventDispatcher.getInstance().fireDataChangedListener(
                 techNameAssoc.getCategory(), DataState.StructureModified, 
                 UpdateState.onlyInEditor);
             m_tableViewer.refresh();
         }
         
         if (techNameAssoc == null) {
             // Technical Name already exists
             for (IObjectMappingAssoziationPO assoc : getAut().getObjMap()
                     .getMappings()) {
                 IComponentIdentifier techName = assoc.getTechnicalName();
                 if (techName != null && techName.equals(component)) {
                     techNameAssoc = assoc;
                     techNameAssoc.setCompIdentifier(component);
                     break;
                 }
             }
         }
 
         if (techNameAssoc != null) {
             IStructuredSelection techNameSelection = 
                 new StructuredSelection(techNameAssoc);
             m_treeViewer.setSelection(techNameSelection);
             m_uiElementTreeViewer.setSelection(techNameSelection);
             m_mappedComponentTreeViewer.setSelection(techNameSelection);
             refreshAllViewer();
         }
     }
     
     /**
      * call refresh() for all the different viewers in this editor
      */
     private void refreshAllViewer() {
         m_treeViewer.refresh();
         m_uiElementTreeViewer.refresh();
         m_mappedComponentTreeViewer.refresh();
         m_tableViewer.refresh();
     }
     
     /**
      * executes the right update
      * @param event
      *      int
      * @param obj
      *      Obbject
      */
     private void switchEvent(int event, Object obj) {
         switch(event) {
             case IObjectMappingObserver.EVENT_STEP_RECORDED :
                 IAUTMainPO aut = (IAUTMainPO)obj;
                 if (getAut() == aut) {
                     cleanupNames();
                 }
                 break;
             case IObjectMappingObserver.EVENT_COMPONENT_MAPPED :
                 IAUTMainPO connectedAut = 
                     TestExecution.getInstance().getConnectedAut();
                 if (getAut().equals(connectedAut)) {
                     IComponentIdentifier comp = (IComponentIdentifier)obj;
                     createNewTechnicalName(comp);
                 }
                 break;
             default:
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleEditorDirtyStateChanged(
             IJBEditor gdEditor, boolean isDirty) {
         
         if (gdEditor == this) {
             m_revertEditorChangesAction.setEnabled(isDirty);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public TreeViewer getTreeViewer() {
         return m_treeViewer;
     }
     
     /**
      * 
      * @return the table viewer.
      */
     public TableViewer getTableViewer() {
         return m_tableViewer;
     }
     
     /**
      * @return Returns the omEditorBP.
      */
     public OMEditorBP getOmEditorBP() {
         return m_omEditorBP;
     }
 
     /**
      * removed all not used logical names
      * @return int the number of added items
      */
     public int cleanupNames() {
         int addedItems = 0;
         Set<IObjectMappingAssoziationPO> addedNodes = 
             new HashSet<IObjectMappingAssoziationPO>();
         for (ITestSuitePO ts : TestSuiteBP.getListOfTestSuites()) {
             if (ts.getAut() == null) {
                 continue;
             }
             if (ts.getAut().equals(getAut())) {
                 CollectLogicalNamesOp op = new CollectLogicalNamesOp();
                 TreeTraverser traverser = new TreeTraverser(ts, op);
                 traverser.traverse(true);
                 addedItems += op.getAddedNodeCount();
                 addedNodes.addAll(op.getAddedNodes());
             }
         }
         if (addedItems > 0) {
             getEditorHelper().setDirty(true);
             if (getTreeViewer() != null) {
                 getTreeViewer().setSelection(
                     new StructuredSelection(addedNodes.toArray()));
             }
         }
         if (!isDirty()) {
             try {
                 getEditorHelper().getEditSupport().reinitializeEditSupport();
                 getEditorHelper().resetEditableState();
             } catch (PMException e) {
                 PMExceptionHandler.handlePMExceptionForEditor(e, this);
             }
         }
         return addedItems;
     }
 
     /**
      * {@inheritDoc}
      */
     public void propertyChanged(Object source, int propId) {
         if (propId == IWorkbenchPartConstants.PROP_DIRTY) {
             ((IEditorPart)source).isDirty();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isDirty() {
         return super.isDirty() || getEditorHelper().isDirty();
     }
     
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         getEditorSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.REFRESH.getId(), null);
         IAUTMainPO connectedAut = TestExecution.getInstance().getConnectedAut();
         if (AUTModeChangedCommand.getAutMode() 
                 == ChangeAUTModeMessage.OBJECT_MAPPING
                 && connectedAut != null
                 && connectedAut.equals(getAut())) {
             TestExecutionContributor.getInstance()
                 .getClientTest().resetToTesting();
             DataEventDispatcher.getInstance()
                 .fireOMStateChanged(OMState.notRunning);
         }        
         ObjectMappingEventDispatcher.removeObserver(this);
         getSite().setSelectionProvider(null);
         GuiEventDispatcher.getInstance().removeEditorDirtyStateListener(this);
         
         m_treeViewerUpdater = null;
         if (m_editorHelper != null) {
             m_editorHelper.dispose();
         }
         super.dispose();
     }
     
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Class adapter) {
         Object superAdapter = super.getAdapter(adapter);
         if (superAdapter != null) {
             return superAdapter;
         }
         
         if (m_editorHelper != null) {
             return m_editorHelper.getAdapter(adapter);
         }
         
         return null;
     }
 
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         for (DataChangedEvent e : events) {
             handleDataChanged(e.getPo(), e.getDataState());
         }
     }
     
     /** {@inheritDoc} */
     public void handleDataChanged(IPersistentObject po, DataState dataState) {
 
         getEditorHelper().handleDataChanged(po, dataState);
         if (m_treeViewerUpdater != null) {
             m_treeViewerUpdater.handleDataChanged(po, dataState);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void init(IEditorSite site, 
             IEditorInput input) throws PartInitException {
         super.init(site, input);
         
         if (m_editorHelper == null) {
             m_editorHelper = new JBEditorHelper(this);
         }
         m_editorHelper.init(site, input);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void pageChange(int newPageIndex) {
         super.pageChange(newPageIndex);
 
         m_selectionProvider.setSelectionProviderDelegate(
                 m_pageToSelectionProvider.get(newPageIndex));
         
         switch (newPageIndex) {
             case TREE_PAGE_IDX:
             case SPLIT_PAGE_IDX:
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.CUT.getId(), m_cutTreeItemAction);
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), m_pasteTreeItemAction);
                 break;
             case TABLE_PAGE_IDX:
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), null);
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.REFRESH.getId(), null);
             default:
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.CUT.getId(), null);
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.PASTE.getId(), null);
                 getEditorSite().getActionBars().setGlobalActionHandler(
                         ActionFactory.REFRESH.getId(), null);
                 break;
         }
 
         getEditorSite().getActionBars().updateActionBars();
     }
 
     /**
      * Convenience method for accessing the editor's component name mapper.
      * 
      * @return the editor's component name mapper.
      */
     private IWritableComponentNameMapper getCompMapper() {
         return getEditorHelper().getEditSupport().getCompMapper();
     }
 
     /**
      * {@inheritDoc}
      */
     public TreeViewer getActiveTreeViewer() {
         return m_activeTreeViewer;
     }
 
     /**
      * {@inheritDoc}
      */
     public TreeViewer[] getTreeViewers() {
         return new TreeViewer[] {m_treeViewer, m_compNameTreeViewer, 
             m_uiElementTreeViewer, m_mappedComponentTreeViewer};
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public EntityManager getEntityManager() {
         return getEditorHelper().getEditSupport().getSession();
     }
 }

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
 
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.help.IContext;
 import org.eclipse.help.IContextProvider;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ColumnViewerEditor;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
 import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.IElementComparer;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.jface.viewers.TreeViewerEditor;
 import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ILanguageChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IParamChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IPartClosedListener;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.controllers.propertysources.IPropertyController;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertydescriptors.JBPropertyDescriptor;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.AbstractNodePropertySource;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.CapGUIPropertySource.ActionTypeController;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.CapGUIPropertySource.ComponentNameController;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.CapGUIPropertySource.ComponentTypeController;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.CapGUIPropertySource.ParameterNameController;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.CapGUIPropertySource.ParameterTypeController;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.CapGUIPropertySource.ParameterValueController;
 import org.eclipse.jubula.client.ui.rcp.controllers.propertysources.IParameterPropertyController;
 import org.eclipse.jubula.client.ui.rcp.editors.AbstractJBEditor;
 import org.eclipse.jubula.client.ui.rcp.editors.JBEditorHelper.EditableState;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.contextprovider.JBContextProvider;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.utils.generator.ActionInfo;
 import org.eclipse.jubula.tools.utils.generator.CompSystemProcessor;
 import org.eclipse.jubula.tools.utils.generator.ComponentInfo;
 import org.eclipse.jubula.tools.utils.generator.ParamInfo;
 import org.eclipse.jubula.tools.utils.generator.ToolkitInfo;
 import org.eclipse.jubula.tools.xml.businessmodell.Action;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.HelpEvent;
 import org.eclipse.swt.events.HelpListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.eclipse.ui.part.Page;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.IPropertySource;
 import org.eclipse.ui.views.properties.PropertySheet;
 import org.eclipse.ui.views.properties.TextPropertyDescriptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 19.01.2005
  */
 @SuppressWarnings("synthetic-access")
 public class JBPropertiesView extends Page implements IDataChangedListener, 
     IParamChangedListener, ISelectionProvider, IPartClosedListener, 
     ILanguageChangedListener, IPropertySheetPage, IAdaptable {
     
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(JBPropertiesView.class);
     
     /** The property source */
     private IPropertySource m_propSource;
     
     /** The Tree Viewer of this view */
     private TreeViewer m_treeViewer;
 
     /** The selection from the selection service */
     private IStructuredSelection m_selection;
 
     /** listens for selection changes that influence help context */
     private ISelectionChangedListener m_helpContextListener =
         new HelpContextListener();
     
     /** ISelectionListener */
     private List<ISelectionChangedListener> m_selectionListener = 
         new ArrayList<ISelectionChangedListener>();
 
     /** The actual displayed persistent object */
     private IPersistentObject m_currentPo;
 
     /** shows, if the Properties view is editable or not */
     private boolean m_isEditable;
     
     /** the corresponding part */
     private IWorkbenchPart m_correspondingPart;
 
     /**
      * Label provider for property names.
      *
      * @author BREDEX GmbH
      * @created Apr 7, 2010
      */
     private final class PropertyNameLabelProvider extends ColumnLabelProvider {
         /**
          * {@inheritDoc}
          */
         public String getText(Object element) {
             if (element instanceof IPropertyDescriptor) {
                 return ((IPropertyDescriptor)element).getDisplayName();
             }
             return super.getText(element);
         }
         
         /**
          * {@inheritDoc}
          */
         public Image getImage(Object element) {
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 IPropertyDescriptor propDesc = (IPropertyDescriptor)element;
                 ILabelProvider labelProvider = propDesc.getLabelProvider();
                 return labelProvider.getImage(propDesc.getId());
             }
 
             return super.getImage(element);
         }
         
         /**
          * {@inheritDoc}
          */
         public Color getForeground(Object element) {
             return super.getForeground(element);
         }
 
         /**
          * {@inheritDoc}
          */
         public Color getBackground(Object element) {
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 ILabelProvider labelProvider = 
                     ((IPropertyDescriptor)element).getLabelProvider();
                 if (labelProvider instanceof IColorProvider) {
                     return ((IColorProvider)labelProvider).getBackground(
                             m_propSource);
                 }
             }
 
             return super.getBackground(element);
         }
     }
 
     /**
      * Label provider for property values.
      * 
      * @author BREDEX GmbH
      * @created Apr 7, 2010
      */
     private final class PropertyValueLabelProvider extends ColumnLabelProvider {
         /**
          * {@inheritDoc}
          */
         public String getText(Object element) {
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 ILabelProvider labelProvider = 
                     ((IPropertyDescriptor)element).getLabelProvider();
                 return labelProvider.getText(m_propSource.getPropertyValue(
                         ((IPropertyDescriptor)element).getId()));
             }
 
             return null;
         }
         
         /**
          * {@inheritDoc}
          */
         public Color getForeground(Object element) {
             if (m_propSource instanceof AbstractNodePropertySource) {
                 AbstractNodePropertySource guiNodePropSource =
                     (AbstractNodePropertySource)m_propSource;
                 if (guiNodePropSource.isReadOnly()) {
                     return LayoutUtil.GRAY_COLOR;
                 }
                 
             }
 
             if (!checkEditorPart()) {
                 return LayoutUtil.GRAY_COLOR;
             }
             
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 ILabelProvider labelProvider = 
                     ((IPropertyDescriptor)element).getLabelProvider();
                 if (labelProvider instanceof IColorProvider) {
                     return ((IColorProvider)labelProvider).getForeground(
                             ((IPropertyDescriptor)element).getId());
                 }
             }
 
             return super.getForeground(element);
         }
         
         /**
          * {@inheritDoc}
          */
         public String getToolTipText(Object element) {
             String displayedText = getText(element);
             if (displayedText != null && displayedText.length() > 0) {
                 return displayedText;
             }
             return null;
         }
     }
     
     /**
      * @author BREDEX GmbH
      * @created Sep 20, 2010
      */
     private final class PropertiesElementComparer implements IElementComparer {
         /**
          * {@inheritDoc}
          */
         public int hashCode(Object element) {
             // ignore JBPropertyDescriptor as this leads to incorrect behavior
             // e.g. when deleting params from a spec tc via edit parameters
             if (element instanceof IPropertyDescriptor 
                     && !(element instanceof JBPropertyDescriptor)) {
                 IPropertyDescriptor pd = (IPropertyDescriptor)element;
                 HashCodeBuilder hb = new HashCodeBuilder();
                 hb.append(pd.getCategory());
                 hb.append(pd.getDisplayName());
                 Object id = pd.getId();
                 if (id instanceof IPropertyController) {
                     IPropertyController pdpc = (IPropertyController)id;
                     hb.append(pdpc.getProperty());
                 }
                 return hb.toHashCode();
             }
             return ObjectUtils.hashCode(element);
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean equals(Object a, Object b) {
             // ignore JBPropertyDescriptor as this leads to incorrect behavior
             // e.g. when deleting params from a spec tc via edit parameters
             if (a instanceof IPropertyDescriptor
                     && b instanceof IPropertyDescriptor
                     && !(a instanceof JBPropertyDescriptor 
                             || b instanceof JBPropertyDescriptor)) {
                 IPropertyDescriptor pd1 = (IPropertyDescriptor)a;
                 IPropertyDescriptor pd2 = (IPropertyDescriptor)b;
                 EqualsBuilder eb = new EqualsBuilder();
                 eb.append(pd1.getCategory(), pd2.getCategory());
                 eb.append(pd1.getDisplayName(), pd2.getDisplayName());
                 Object id1 = pd1.getId();
                 Object id2 = pd2.getId();
                 if (id1 instanceof IPropertyController
                         && id2 instanceof IPropertyController) {
                     IPropertyController pd1pc = (IPropertyController)id1;
                     IPropertyController pd2pc = (IPropertyController)id2;
                     eb.append(pd1pc.getProperty(), pd2pc.getProperty());
                 }
                 return eb.isEquals();
             }
             return ObjectUtils.equals(a, b);
         }
     }
 
     /** the context provider for this view */
     private JBContextProvider m_contextProvider = 
         new JBContextProvider();
     /** helpListener of this view */
     private ContextHelpListener m_helpListener = new ContextHelpListener();
     
     /** the Component Name mapper to use when this page is active */
     private IComponentNameMapper m_compMapper;
 
     /** current editor */
     private AbstractJBEditor m_currentEditor = Plugin.getDefault()
             .getActiveJBEditor();
 
     /** the focus manager for the tree viewer */
     private TreeViewerFocusCellManager m_focusCellManager;
     
     /**
      * Constructor.
      * 
      * @param isEditable <code>true</code> if the properties shown in the view
      *                   should initially be editable.
      * @param compMapper the Component Name mapper to use when this page is 
      *                   active. May be <code>null</code>, if no specific
      *                   mapper should be used.
      */
     public JBPropertiesView(
             boolean isEditable, IComponentNameMapper compMapper) {
         super();
         m_compMapper = compMapper;
         m_isEditable = isEditable;
     }
 
     /** 
      * {@inheritDoc}
      */
     public void createPartControl(Composite parent) {
         buildTree(parent);
 
         getSite().setSelectionProvider(this);
         Plugin.getHelpSystem().setHelp(m_treeViewer.getControl(),
                 ContextHelpIds.JB_PROPERTIES_VIEW);
         final DataEventDispatcher dispatcher = 
             DataEventDispatcher.getInstance();
         dispatcher.addDataChangedListener(this, true);
         dispatcher.addParamChangedListener(this, true);
         dispatcher.addPartClosedListener(this, true);
         dispatcher.addLanguageChangedListener(this, true);
         m_treeViewer.getControl().addHelpListener(m_helpListener);
     }
     
     /**
      * Creates a new Tree for this View.
      * @param parent the parent composite
      */
     private void buildTree(Composite parent) {
         GridData layoutData = new GridData(GridData.FILL_BOTH);
         layoutData.grabExcessHorizontalSpace = true;
         Tree tree = new Tree(parent, SWT.BORDER | SWT.HIDE_SELECTION
             | SWT.FULL_SELECTION);
         tree.setLayoutData(layoutData);
         tree.setHeaderVisible(true);
         tree.setLinesVisible(true);
         m_treeViewer = new TreeViewer(tree);
 
         // add expand/collapse column
         TreeViewerColumn expandCollapseColumn = 
             new TreeViewerColumn(m_treeViewer, SWT.NONE);
         expandCollapseColumn.getColumn().setText(StringConstants.EMPTY);
         expandCollapseColumn.getColumn().setWidth(20);
         expandCollapseColumn.getColumn().setResizable(false);
         expandCollapseColumn.setLabelProvider(new CellLabelProvider() {
             
             public void update(ViewerCell cell) {
                 // Nothing to display. Nothing to update.
             }
         });
         
         // add property name column
         TreeViewerColumn propertyNameColumn = 
             new TreeViewerColumn(m_treeViewer, SWT.NONE);
         propertyNameColumn.getColumn().setText(
                 Messages.JubulaPropertiesViewProperty);
         propertyNameColumn.getColumn().setWidth(175);
         propertyNameColumn.setLabelProvider(new PropertyNameLabelProvider());
         
         // add property value column
         TreeViewerColumn propertyValueColumn = 
             new TreeViewerColumn(m_treeViewer, SWT.NONE);
         propertyValueColumn.getColumn().setText(
                 Messages.JubulaPropertiesViewValue);
         propertyValueColumn.getColumn().setWidth(300);
         propertyValueColumn.setLabelProvider(new PropertyValueLabelProvider());
         propertyValueColumn.setEditingSupport(
                 new PropertiesEditingSupport(m_treeViewer));
         
         m_treeViewer.addSelectionChangedListener(m_helpContextListener);
         
         m_treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
         m_treeViewer.setContentProvider(new PropertiesContentProvider());
         ColumnViewerToolTipSupport.enableFor(m_treeViewer);
 
         m_treeViewer.setComparer(new PropertiesElementComparer());
         
         m_focusCellManager = 
             new TreeViewerFocusCellManager(m_treeViewer,
                     new FocusCellOwnerDrawHighlighter(m_treeViewer));
         ColumnViewerEditorActivationStrategy actSupport = 
             new ColumnViewerEditorActivationStrategy(m_treeViewer) {
                 protected boolean isEditorActivationEvent(
                         ColumnViewerEditorActivationEvent event) {
                     return event.eventType 
                             == ColumnViewerEditorActivationEvent.TRAVERSAL
                         || event.eventType 
                             == ColumnViewerEditorActivationEvent.
                                 MOUSE_CLICK_SELECTION
                         || (event.eventType 
                                 == ColumnViewerEditorActivationEvent.KEY_PRESSED
                             && event.keyCode == SWT.CR)
                         || event.eventType 
                             == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
                 }
             };
         
         TreeViewerEditor.create(m_treeViewer, m_focusCellManager, 
                 actSupport, ColumnViewerEditor.TABBING_VERTICAL
                     | ColumnViewerEditor.KEYBOARD_ACTIVATION
                     | ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK);
     }
     
     /** 
      * {@inheritDoc}
      */
     public void setFocus() {
         getControl().setFocus();
     }
  
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         if (m_treeViewer != null) {
             m_treeViewer.removeSelectionChangedListener(m_helpContextListener);
         }
         final DataEventDispatcher dispatcher = 
             DataEventDispatcher.getInstance();
         dispatcher.removeDataChangedListener(this);
         dispatcher.removeParamChangedListener(this);
         dispatcher.removePartClosedListener(this);
         dispatcher.removeLanguageChangedListener(this);
         getSite().setSelectionProvider(null);
         setCurrentEditor(null);
     }
 
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         for (DataChangedEvent e : events) {
             handleDataChanged(e.getPo(), e.getDataState());
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void handleDataChanged(final IPersistentObject po, 
             final DataState dataState) {
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 // indirection necessary due to Checkstyle BUG
                 handleDataChangedImpl(po, dataState);
             }
         });
     }
 
     /**
      * @param po
      *            the po
      * @param dataState
      *            the data state
      */
     private void handleDataChangedImpl(final IPersistentObject po,
             final DataState dataState) {
         if (po == null) {
             return;
         }
         
         // Check if the patent of the current node has been changed.
         // This will happen if a child of a SpecTC is displayed
         // and the editor with that SpecTC is saved.
         boolean parentMatch = false;
         if (m_currentPo instanceof INodePO) {
             INodePO parent = ((INodePO)m_currentPo).getParentNode();
             parentMatch = (parent != null) && po.equals(parent);
         }
         
         if (parentMatch || po.equals(m_currentPo)) {
             switch (dataState) {
                 case Added:
                 case StructureModified:
                     if (po instanceof IPersistentObject) {
                         m_treeViewer.refresh();
                         expandTrackedChanges();
                     }
                     break;
                 case Deleted:
                     clearView();
                     break;
                 case Renamed:
                     m_treeViewer.refresh();
                     expandTrackedChanges();
                     break;
                 default:
             }
         }
     }
 
     /**
      * Expands the tracked changes in the properties view.
      * For some reasons the view needs persuasion to expand the tracked changes sometimes.
      */
     private void expandTrackedChanges() {
         m_treeViewer.setExpandedState(
                 Messages.SpecTestCaseGUIPropertySourceTrackedChangesCategory, 
                 true);
     }
     
     /**
      * Clears the view.
      */
     private void clearView() {
         Plugin.getDisplay().syncExec(new Runnable() {
             public void run() {
                 m_treeViewer.setComparer(null);
                 m_treeViewer.setInput(null);
                 setViewEnabled(false);
                 m_selection = new StructuredSelection();
             }
         });
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleParamChanged() {
         if (Plugin.getActivePart() instanceof PropertySheet) {
             if (((PropertySheet)Plugin
                     .getActivePart()).getCurrentPage() != this) {
                 return;
             }
         }
         m_treeViewer.refresh();
         m_treeViewer.expandToLevel(m_treeViewer.getAutoExpandLevel());
     }
 
     /**
      * Reacts on the changes from the SelectionService of Eclipse.
      * 
      * @param part
      *            the workbench part
      * @param selection
      *            the selection
      */
     private void reactOnChange(IWorkbenchPart part, 
         IStructuredSelection selection) {
         
         m_correspondingPart = part;
         if (m_selection == null) {
             m_selection = new StructuredSelection();
         }
 
         Object firstElement = selection.getFirstElement();
         
         IStructuredSelection oldSelection = m_selection;
         m_selection = selection;
         if (firstElement == null) {
             // e.g. when a project was opened and no view has a selection
             m_treeViewer.setSelection(null);
             m_treeViewer.setInput(null);
             m_currentPo = null;
         } else {
             Object oldFirstElement = oldSelection.getFirstElement();
             if (!firstElement.equals(oldFirstElement)) {
                 if (firstElement instanceof IPersistentObject) {
                     m_currentPo = (IPersistentObject)firstElement;
                 }
                 m_treeViewer.setInput(firstElement);
                 workaroundSpringySelection(m_focusCellManager);
             }
         }
         
         // property informations should be collapsed by default
         m_treeViewer.setExpandedState(Messages.
                 OMTechNameGUIPropertySourcePropertyInformation, 
                 false);
         
        // parameters should be expanded by default
        m_treeViewer.setExpandedState(Messages.
                SpecTestCaseGUIPropertySourceParameter, true);
        
         expandTrackedChanges();
 
         setViewEnabled(!(part instanceof TestCaseBrowser
             || part instanceof TestSuiteBrowser
             || part instanceof JBPropertiesView
             || part instanceof TestResultTreeView 
             || part instanceof CompNamesView));
 
     }
 
     /**
      * Workaround for ticket #3012. Prevents odd cell selection 
      * behavior by pre-emptively setting the focus cell, if it is not already 
      * set. This workaround relies <b>heavily</b> on reflection. so the smallest
      * change to JFace might break it. A try-catch(throwable) block is used to 
      * minimize the damage that such a breakage would cause. Worst-case 
      * scenario: The bug is not fixed and a small performance penalty is 
      * incurred by looking everything up via reflection.
      * 
      * @param focusCellManager The focus manager on which to perform the 
      *                         workaround.
      */
     private void workaroundSpringySelection(
             TreeViewerFocusCellManager focusCellManager) {
         try {
             if (focusCellManager.getFocusCell() == null) {
                 Class focusManagerClass = focusCellManager.getClass();
                 Class abstractFocusManagerClass = 
                     focusManagerClass.getSuperclass();
                 Method getInitialFocusCellMethod = 
                     focusManagerClass.getDeclaredMethod("getInitialFocusCell",  //$NON-NLS-1$
                             new Class[0]);
                 Method setFocusCellMethod = 
                     abstractFocusManagerClass.getDeclaredMethod("setFocusCell",  //$NON-NLS-1$
                             ViewerCell.class);
                 getInitialFocusCellMethod.setAccessible(true);
                 setFocusCellMethod.setAccessible(true);
                 Object initialFocusCellObj = getInitialFocusCellMethod.invoke(
                         focusCellManager, new Object[0]);
                 if (initialFocusCellObj instanceof ViewerCell) {
                     ViewerCell initialFocusCell = 
                         (ViewerCell)initialFocusCellObj;
                     setFocusCellMethod.invoke(focusCellManager, 
                             initialFocusCell);
                 }
             }
         } catch (Throwable t) {
             LOG.info(Messages.ErrorInWorkaroundForSpringySelection, t);
         }
     }
     
     /**
      * Sets the widget in the treeView enabled or disabled.
      * @param enabled The disabled/enabled flag.
      * <p> It is <code>true</code>, if a node in the editor is selected. </p>
      * <p> It is <code>false</code>, if a node in one of the treeViews is selected. </p>
      */
     void setViewEnabled(boolean enabled) {
         m_isEditable = enabled;
         Color bColor = null;
         Color fColor = null;
         if (!enabled) {
             bColor = LayoutUtil.LIGHT_GRAY_COLOR;
             fColor = LayoutUtil.GRAY_COLOR;
         }
         getControl().setBackground(bColor);
         getControl().setForeground(fColor);
     }
 
     /**
      * @return <code>true</code> if the contributing part is the currently 
      *         active editor. Otherwise, <code>false</code>.
      */
     private boolean checkEditorPart() {
         return (Plugin.getActiveEditor() != null
                 && Plugin.getActiveEditor().equals(m_correspondingPart));
     }
 
    /**
      * @param text The text field to set a max. char range.
      * @param propD The propertyDescriptor of the text field.
      */
     private void setMaxChar(Text text, Object propD) {
         if (!m_isEditable) {
             return;
         }
         if (propD instanceof TextPropertyDescriptor) {
             LayoutUtil.setMaxChar(text);
         }
     }
 
     /**
      * Content provider for properties.
      *
      * @author BREDEX GmbH
      * @created Apr 7, 2010
      */
     private final class PropertiesContentProvider 
             implements ITreeContentProvider {
 
         /**
          * {@inheritDoc}
          */
         public Object[] getChildren(Object parentElement) {
             if (parentElement instanceof String) {
                 // category
                 List<IPropertyDescriptor> children = 
                     new ArrayList<IPropertyDescriptor>();
                 for (IPropertyDescriptor propDesc 
                         : m_propSource.getPropertyDescriptors()) {
                     if (parentElement.equals(propDesc.getCategory())) {
                         children.add(propDesc);
                     }
                 }
                 return children.toArray();
             }
             
             return null;
         }
 
         /**
          * {@inheritDoc}
          */
         public Object getParent(Object element) {
             if (element instanceof IPropertyDescriptor) {
                 return ((IPropertyDescriptor)element).getCategory();
             }
             return null;
         }
 
         /**
          * {@inheritDoc}
          */
         public boolean hasChildren(Object element) {
             if (element instanceof String) {
                 // category. assume that it has child elements (because 
                 // otherwise we wouldn't have added the category).
                 return true;
             }
 
             return false;
         }
 
         /**
          * {@inheritDoc}
          */
         public Object[] getElements(Object inputElement) {
             IPropertyDescriptor[] descriptors = 
                 getPropertyDescriptors(inputElement);
             Set<String> categories = new LinkedHashSet<String>();
             Set<IPropertyDescriptor> topLevelDescriptors = 
                 new LinkedHashSet<IPropertyDescriptor>();
 
             for (IPropertyDescriptor descriptor : descriptors) {
                 String category = descriptor.getCategory();
                 if (category == null) {
                     topLevelDescriptors.add(descriptor);
                 } else {
                     categories.add(category);
                 }
             }
 
             List<Object> children = new ArrayList<Object>();
             children.addAll(topLevelDescriptors);
             children.addAll(categories);
             return children.toArray();
         }
 
         /**
          * 
          * @param element The adaptable element.
          * @return the property source for the given <code>element</code>, or 
          *         <code>null</code> if no such property source can be found.
          */
         private IPropertySource getPropertySource(Object element) {
             if (element != null) {
                 Object propertySourceObj = null;
                 if (element instanceof IAdaptable) {
                     IAdaptable adaptable = (IAdaptable)element;
                     propertySourceObj = 
                         adaptable.getAdapter(IPropertySource.class);
                 } else {
                     propertySourceObj = 
                         Platform.getAdapterManager().getAdapter(
                                 element, IPropertySource.class);
                 }
                 if (propertySourceObj instanceof IPropertySource) {
                     return (IPropertySource)propertySourceObj;
                 }
             }
             
             return null;
         }
         
         /**
          * 
          * @param element The element for which to get the descriptors.
          * @return the corresponding property descriptors if <code>element</code>
          *         is a valid gui node. Otherwise returns an empty array.
          */
         private IPropertyDescriptor[] getPropertyDescriptors(Object element) {
             if (element instanceof INodePO) {
                 INodePO guiNodeInput = (INodePO)element;
                 if (isValid(guiNodeInput)) {
                     IPropertySource propertySource = getPropertySource(element);
                     if (propertySource != null) {
                         return propertySource.getPropertyDescriptors();
                     }
                 }
             } else {
                 IPropertySource propertySource = getPropertySource(element);
                 if (propertySource != null) {
                     return propertySource.getPropertyDescriptors();
                 }
             }
             
             return new IPropertyDescriptor[0];
         }
         
         /**
          * 
          * @param node The node to check.
          * @return <code>false</code> if the node is a Test Step with an invalid
          *         component. Otherwise, <code>true</code>. 
          */
         private boolean isValid(INodePO node) {
             return node != null && node.isValid();
         }
         
         /**
          * {@inheritDoc}
          */
         public void dispose() {
             // Nothing to dispose
         }
 
         /**
          * {@inheritDoc}
          */
         public void inputChanged(Viewer viewer, 
                 Object oldInput, Object newInput) {
             m_propSource = getPropertySource(newInput);
         }
         
     }
 
     /**
      * Editing support for properties shown in the view.
      *
      * @author BREDEX GmbH
      * @created Apr 8, 2010
      */
     private final class PropertiesEditingSupport extends EditingSupport {
 
         /**
          * Constructor
          * 
          * @param viewer The viewer.
          */
         public PropertiesEditingSupport(ColumnViewer viewer) {
             super(viewer);
         }
 
         /**
          * {@inheritDoc}
          */
         protected boolean canEdit(Object element) {
             if (m_correspondingPart instanceof IEditorPart) {
 
                 if (element instanceof IPropertyDescriptor) {
                     Object propId = ((IPropertyDescriptor)element).getId();
                     if (m_propSource instanceof AbstractNodePropertySource
                             && propId 
                                 instanceof IParameterPropertyController) {
                         return ((AbstractNodePropertySource)m_propSource)
                             .isParameterEntryEnabled(
                                 (IParameterPropertyController)propId);
                     }
                 }
                 return true;
             }
             
             return false;
         }
 
         /**
          * {@inheritDoc}
          */
         protected CellEditor getCellEditor(Object element) {
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 CellEditor editor = 
                     ((IPropertyDescriptor)element).createPropertyEditor(
                         m_treeViewer.getTree());
                 if (editor != null) {
                     Control editorControl = editor.getControl();
                     if (editorControl instanceof Text) {
                         setMaxChar((Text)editorControl, element);
                     }
                 }
                 return editor;
             }
             return null;
         }
 
         /**
          * {@inheritDoc}
          */
         protected Object getValue(Object element) {
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 return m_propSource.getPropertyValue(
                         ((IPropertyDescriptor)element).getId());
             }
             
             return null;
         }
 
         /**
          * {@inheritDoc}
          */
         protected void setValue(Object element, Object value) {
             if (element instanceof IPropertyDescriptor
                     && m_propSource != null) {
                 IPropertyDescriptor propDesc = (IPropertyDescriptor)element;
                 Object oldValue = 
                     m_propSource.getPropertyValue(propDesc.getId());
                 if (oldValue == null || !oldValue.equals(value)) {
                     if (m_currentEditor.getEditorHelper().requestEditableState()
                             == EditableState.OK) {
                         
                         m_propSource.setPropertyValue(propDesc.getId(), value);
                         if (getCurrentEditor() != null) {
                             getCurrentEditor().getEditorHelper().setDirty(true);
                         }
                     }
                 }
             }
         }
         
     }
 
     /**
      * Updates Help Context based on Selection Changed events.
      *
      * @author BREDEX GmbH
      * @created Apr 8, 2010
      */
     private final class HelpContextListener 
             implements ISelectionChangedListener {
         /**
          * <code>m_oldSelection</code>
          */
         private Object m_oldSelection = null;
 
         /**
          * {@inheritDoc}
          */
         public void selectionChanged(SelectionChangedEvent event) {
             String helpId = ContextHelpIds.PRAEFIX;
             if (!(event.getSelection() instanceof IStructuredSelection)) {
                 return;
             }
             
             Object selectedObj = 
                 ((IStructuredSelection)event.getSelection()).getFirstElement();
             if (!(selectedObj instanceof IPropertyDescriptor) 
                     || ObjectUtils.equals(m_oldSelection, selectedObj)) {
                 return;
             }
             m_oldSelection = selectedObj;
             
             Object descriptorId = ((IPropertyDescriptor)selectedObj).getId();
             
             if (descriptorId instanceof ComponentTypeController
                     || descriptorId instanceof ComponentNameController) {
                 
                 helpId += getCompInfo().getHelpid();
             } else if (descriptorId instanceof ActionTypeController) {
                 helpId += getActionInfo().getHelpid();
             } else if (descriptorId instanceof ParameterNameController) {
                 helpId += getParamInfo(((ParameterNameController)descriptorId)
                         .getName()).getHelpid();
             } else if (descriptorId instanceof ParameterValueController) {
                 helpId += getParamInfo(((ParameterValueController)descriptorId)
                         .getParamDesc().getUniqueId()).getHelpid();
             } else if (descriptorId instanceof ParameterTypeController) {
                 helpId += getParamInfo(((ParameterTypeController)descriptorId)
                         .getName()).getHelpid();
             }
 
             if (ContextHelpIds.PRAEFIX.equals(helpId)) {
                 helpId = ContextHelpIds.JB_PROPERTIES_VIEW;
             }
 
             IWorkbenchHelpSystem helpSystem = Plugin.getHelpSystem();
             helpSystem.setHelp(m_treeViewer.getControl(), helpId);
             if (helpSystem.isContextHelpDisplayed()
                     || Plugin.getView(Constants.ECLIPSE_HELP_VIEW_ID) != null) {
                 helpSystem.displayHelp(helpId);
             }
         }
         
         /**
          * @param paramName the current parameter name
          * @return the current parameter helpID
          */
         private ParamInfo getParamInfo(String paramName) {
             return new ParamInfo(getAction().findParam(paramName), 
                     getActionInfo().getHelpid());
         }
 
         /**
          * @return the current action helpID
          */
         private ActionInfo getActionInfo() {
             CompSystemProcessor processor = new CompSystemProcessor(
                     ComponentBuilder.getInstance().getCompSystem());
             ComponentInfo definingComp = processor.getDefiningComp(
                 getCompInfo(), getAction());
             return new ActionInfo(getAction(), definingComp);
         }
 
         /**
          * @return the current action
          */
         private Action getAction() {
             return getComp().findAction(((ICapPO)m_currentPo)
                 .getActionName());
         }
 
         /**
          * @return the current component helpID
          */
         private ComponentInfo getCompInfo() {
             Component comp = getComp();
             ToolkitInfo tkInfo = CompSystemProcessor.getToolkitInfo(
                     comp.getToolkitDesriptor());
             return new ComponentInfo(comp, tkInfo);
         }
 
         /**
          * @return the current component
          */
         private Component getComp() {
             return ComponentBuilder.getInstance().getCompSystem().findComponent(
                     ((ICapPO)m_currentPo).getComponentType());
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void addSelectionChangedListener(
         ISelectionChangedListener listener) {
 
         m_selectionListener.add(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public ISelection getSelection() {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeSelectionChangedListener(
         ISelectionChangedListener listener) {   
         
         m_selectionListener.remove(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSelection(ISelection selection) {
         // do nothing.
     }
 
     /**
      * {@inheritDoc}
      */
     public void handlePartClosed(IWorkbenchPart part) {
         final IWorkbenchWindow activeWorkbenchWindow = 
             PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
         if (activePage == null) {
             // if Jubula was closed
             return;
         }
         IWorkbenchPart activePart = activePage.getActivePart();
         if (activePart == null) {
             // if the last Jubula perspective was closed
             return;
         }
         if (part == m_correspondingPart || m_correspondingPart == null
                 || part instanceof JBPropertiesView) {
             
             ISelection sel = 
                 activeWorkbenchWindow.getSelectionService().getSelection();
 
             if (sel == null 
                     || !(part instanceof IDataChangedListener) 
                     || part instanceof JBPropertiesView
                     || activePart instanceof JBPropertiesView) {
                 
                 clearView();
                 return;
             }
             if (sel instanceof IStructuredSelection) {
                 reactOnChange(activePart, (IStructuredSelection)sel);
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void handleLanguageChanged(Locale locale) {
         m_treeViewer.refresh();
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getAdapter(Class adapter) {
         if (adapter.equals(IContextProvider.class)) {
             return m_contextProvider;
         } else if (adapter.equals(IPropertySheetPage.class)) {
             return this;
         } else if (adapter.equals(IComponentNameMapper.class) 
                 && m_compMapper != null) {
             return m_compMapper;
         }
         return null;
     }
     
     /**
      * @author BREDEX GmbH
      * @created Jan 22, 2007
      */
     private final class ContextHelpListener implements HelpListener {
         /** {@inheritDoc} */
         public void helpRequested(HelpEvent e) {
             IContext context = m_contextProvider.getContext(
                     m_treeViewer.getControl().getData(ContextHelpIds.HELP)); 
             Plugin.getHelpSystem().displayHelp(context);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void createControl(Composite parent) {
         createPartControl(parent);
     }
 
     /**
      * {@inheritDoc}
      */
     public Control getControl() {
         return m_treeViewer.getControl();
     }
 
     /**
      * {@inheritDoc}
      */
     public void selectionChanged(IWorkbenchPart part, ISelection selection) {
         if (part instanceof IJBPart
                 && selection instanceof IStructuredSelection) {
             reactOnChange(part, (IStructuredSelection)selection);
         }
     }
 
     /**
      * @param currentEditor the currentEditor to set
      */
     public void setCurrentEditor(AbstractJBEditor currentEditor) {
         m_currentEditor = currentEditor;
     }
 
     /**
      * @return the currentEditor
      */
     public AbstractJBEditor getCurrentEditor() {
         return m_currentEditor;
     }
 
 }

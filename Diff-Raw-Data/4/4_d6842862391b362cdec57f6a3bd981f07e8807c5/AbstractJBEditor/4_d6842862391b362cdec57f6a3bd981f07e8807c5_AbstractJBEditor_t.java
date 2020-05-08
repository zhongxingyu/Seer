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
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDecorationContext;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IPropertyChangedListener;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.actions.CutTreeItemActionGDEditor;
 import org.eclipse.jubula.client.ui.actions.PasteTreeItemActionTCEditor;
 import org.eclipse.jubula.client.ui.constants.Layout;
 import org.eclipse.jubula.client.ui.controllers.AbstractPartListener;
 import org.eclipse.jubula.client.ui.controllers.JubulaStateController;
 import org.eclipse.jubula.client.ui.events.GuiEventDispatcher;
 import org.eclipse.jubula.client.ui.handlers.RevertEditorChangesHandler;
 import org.eclipse.jubula.client.ui.provider.SessionBasedLabelDecorator;
 import org.eclipse.jubula.client.ui.provider.labelprovider.GeneralLabelProvider;
 import org.eclipse.jubula.client.ui.utils.CommandHelper;
 import org.eclipse.jubula.client.ui.utils.DisplayableLanguages;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.client.ui.views.IJBPart;
 import org.eclipse.jubula.client.ui.views.ITreeViewerContainer;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.part.EditorPart;
 
 
 /**
  * @author BREDEX GmbH
  * @created Mar 17, 2010
  */
 public abstract class AbstractJBEditor extends EditorPart implements IJBEditor,
         ISelectionProvider, ITreeViewerContainer, IJBPart,
         IPropertyChangedListener {
     /** Add-Submenu ID */
     public static final String ADD_ID = PlatformUI.PLUGIN_ID + ".AddSubMenu"; //$NON-NLS-1$
 
     /** Insert-menu ID */
     public static final String INSERT_ID = PlatformUI.PLUGIN_ID + ".NewSubMenu"; //$NON-NLS-1$
 
     /** Refactor-menu ID */
     public static final String REFACTOR_ID = PlatformUI.PLUGIN_ID
             + ".RefactorSubMenu"; //$NON-NLS-1$
 
     /** <code>BLANK</code> */
     public static final String BLANK = " "; //$NON-NLS-1$
 
     /** postfix for add-action id */
     protected static final String ADD = "_ADD"; //$NON-NLS-1$
 
     /** postfix for insert-action id */
     protected static final String INSERT = "_INSERT"; //$NON-NLS-1$
 
     /** the logger */
     protected static final Log LOG = LogFactory
             .getLog(AbstractJBEditor.class);
 
     /** List of ISelectionChangedListener */
     private List<ISelectionChangedListener> m_selectionChangedListenerList = 
         new ArrayList<ISelectionChangedListener>();
 
     /** TreeViewer for specification */
     private TreeViewer m_mainTreeViewer;
 
     /** the parent composite of this workbench part */
     private Composite m_parentComposite;
     
     /** the helper that assists this editor */
     private JBEditorHelper m_editorHelper;
 
     /** The parent Control. */
     private Control m_control;
     
     /** action to cut TreeItems */
     private CutTreeItemActionGDEditor m_cutTreeItemAction =
         new CutTreeItemActionGDEditor();
     
     /** action to paste TreeItems */
     private PasteTreeItemActionTCEditor m_pasteTreeItemAction = 
         new PasteTreeItemActionTCEditor();
     
     /** the action to revert all changes in the editor */
     private RevertEditorChangesHandler m_revertEditorChangesAction = 
         new RevertEditorChangesHandler();
     
     /** label decorator for main tree viewer */
     private SessionBasedLabelDecorator m_labelDecorator;
     
     /** PartListener of this WokbenchPart */
     private PartListener m_partListener = new PartListener();
     
     /**
      * @author BREDEX GmbH
      * @created 20.09.2006
      */
     private class PartListener extends AbstractPartListener {
         /**
          * {@inheritDoc}
          */
         public void partActivated(IWorkbenchPart part) {
             if (part == AbstractJBEditor.this) {
                 setActionHandlers();
             }
 
             super.partActivated(part);
         }
     }
 
     /**
      * Decoration context to *disable* decoration for editors;
      * see e.g. TestDataDecorator
      * @author BREDEX GmbH
      * @created May 17, 2010
      */
     public class JBEditorDecorationContext implements IDecorationContext {
         /** {@inheritDoc} */
         public String[] getProperties() {
             return null;
         }
 
         /** {@inheritDoc} */
         public Object getProperty(String property) {
             return null;
         }
     }
     
     /**
      * Creates the specification part of the editor
      * 
      * @param parent
      *            Composite.
      */
     protected void createMainPart(Composite parent) {
         setMainTreeViewer(new TreeViewer(parent));
         m_labelDecorator = new SessionBasedLabelDecorator(this, 
                 Plugin.getDefault().getWorkbench().getDecoratorManager()
                     .getLabelDecorator());
         DecoratingLabelProvider lp = new DecoratingLabelProvider(
                 new GeneralLabelProvider(), m_labelDecorator);
         lp.setDecorationContext(new JBEditorDecorationContext());
         getMainTreeViewer().setLabelProvider(lp);
         getMainTreeViewer().setUseHashlookup(true);
         JubulaStateController.getInstance()
                 .addSelectionListenerToSelectionService();
         getSite().setSelectionProvider(this);
         firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
     }
 
     
     /**
      * The SelectionChangedListener for this editor.
      * 
      * @author BREDEX GmbH
      * @created 04.04.2005
      */
     private class EditorSelectionChangedListener implements
             ISelectionChangedListener {
         /**
          * {@inheritDoc}
          */
         public void selectionChanged(SelectionChangedEvent event) {
             Iterator<ISelectionChangedListener> iter = 
                 getSelectionChangedListenerList()
                     .iterator();
             SelectionChangedEvent selChangedEvent = new SelectionChangedEvent(
                     AbstractJBEditor.this, event.getSelection());
             while (iter.hasNext()) {
                 ISelectionChangedListener listener = iter.next();
                 listener.selectionChanged(selChangedEvent);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      * @param adapter the adapter to get
      * @return this TestCaseEditor instance if the adaper matches.
      */
     public Object getAdapter(Class adapter) {
         if (adapter == DisplayableLanguages.class) {
             return new DisplayableLanguages(GeneralStorage.getInstance()
                     .getProject().getLangHelper().getLanguageList());
         } else if (adapter == this.getClass()) {
             return this;
         } else if (adapter == NodeEditorInput.class) {
             return getEditorInput();
         }
         
         Object superAdapter = super.getAdapter(adapter); 
         if (superAdapter != null) {
             return superAdapter;
         }
         
         return getEditorHelper().getAdapter(adapter);
     }
 
     /**
      * @param parentComposite
      *            the parentComposite to set
      */
     public void setParentComposite(Composite parentComposite) {
         m_parentComposite = parentComposite;
     }
 
     /**
      * @return the parentComposite
      */
     public Composite getParentComposite() {
         return m_parentComposite;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addSelectionChangedListener(
             ISelectionChangedListener listener) {
         if (!getSelectionChangedListenerList().contains(listener)) {
             getSelectionChangedListenerList().add(listener);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public ISelection getSelection() {
         if (getMainTreeViewer() == null) {
             return StructuredSelection.EMPTY;
         }
         return getMainTreeViewer().getSelection();
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeSelectionChangedListener(
             ISelectionChangedListener listener) {
         getSelectionChangedListenerList().remove(listener);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSelection(ISelection selection) {
     // do nothing
     }
 
     /**
      * @param mainTreeViewer
      *            the mainTreeViewer to set
      */
     public void setMainTreeViewer(TreeViewer mainTreeViewer) {
         m_mainTreeViewer = mainTreeViewer;
     }
 
     /**
      * @return the topTreeViewer
      */
     public TreeViewer getMainTreeViewer() {
         return m_mainTreeViewer;
     }
 
     /**
      * @param selectionChangedListenerList
      *            the selectionChangedListenerList to set
      */
     public void setSelectionChangedListenerList(
             List<ISelectionChangedListener> selectionChangedListenerList) {
         m_selectionChangedListenerList = selectionChangedListenerList;
     }
 
     /**
      * @return the selectionChangedListenerList
      */
     public List<ISelectionChangedListener> getSelectionChangedListenerList() {
         return m_selectionChangedListenerList;
     }
     
     /**
      * {@inheritDoc}
      */
     public TreeViewer getTreeViewer() {
         return getMainTreeViewer();
     }
     
     /**
      * {@inheritDoc}
      */
     public void handlePropertyChanged(boolean isCompNameChanged) {
         createPartName();
         getMainTreeViewer().refresh();
     }
     
 
     /**
      * creates and sets the partName
      */
     protected void createPartName() {
         String nodeName = 
             getEditorHelper().getEditSupport().getWorkVersion().getName();
         if (nodeName == null) {
             nodeName = StringConstants.EMPTY;
         }
         setPartName(getEditorPrefix() + nodeName);
     }
 
     /**
      * Sets the help to the HelpSystem.
      * @param parent the parent composite to set the help id to
      */
     protected abstract void setHelp(Composite parent);
     
     /**
      * {@inheritDoc}
      */
     public final void createPartControl(Composite parent) {
         if (getEditorHelper() == null) {
             setEditorHelper(new JBEditorHelper(this));
         }
         checkMasterSessionUpToDate();
         createPartName();
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         layout.verticalSpacing = 3;
         layout.marginWidth = Layout.MARGIN_WIDTH;
         layout.marginHeight = Layout.MARGIN_HEIGHT;
         parent.setLayout(layout);
         getEditorSite().getPage().addPartListener(m_partListener);
         setHelp(parent);
         createPartControlImpl(parent);
         createContextMenu();
         addInternalSelectionListeners(new EditorSelectionChangedListener());
         
     }
     
     /**
      * Called by {@link #createPartControl(Composite)}. Warning: Do <b>not</b>
      * call {@link #createPartControl(Composite)} from this method, as this will
      * cause an infinite loop.
      * 
      * @param parent The parent component.
      */
     protected abstract void createPartControlImpl(Composite parent);
     
     /**
      * Adds the given listener to all controls in the editor that should provide
      * a selection. The default implementation adds the listener to the main 
      * tree viewer. Subclasses may extend, but not override.
      * 
      * @param editorSelectionChangedListener 
      *                  The listener that forwards control selection events to 
      *                  workbench selection listeners.
      */
     protected void addInternalSelectionListeners(
             ISelectionChangedListener editorSelectionChangedListener) {
         
         getMainTreeViewer().addSelectionChangedListener(
                 editorSelectionChangedListener);
     }
 
     /**
      * {@inheritDoc}
      */
     public void init(IEditorSite site, IEditorInput input)
         throws PartInitException {
         if (getEditorHelper() == null) {
             setEditorHelper(new JBEditorHelper(this));
         }
         getEditorHelper().init(site, input);
     }
     
     
     /**
      * Reopens the Editor with the changed node
      * @param node the changed node of this editor.
      * @throws PMException if the node can not be loaded
      */
     public void reOpenEditor(IPersistentObject node) throws PMException {
         getEditorHelper().setDirty(false);
         getEditorHelper().getEditSupport().reloadEditSession();
         ((PersistableEditorInput)getEditorInput()).refreshNode();
         try {
             init(getEditorSite(), getEditorInput());
             setInitialInput();
         } catch (PartInitException e) {
             getSite().getPage().closeEditor(this, false);
         }
     }
 
     /**
      * set the initial input
      */
     protected abstract void setInitialInput();
 
     /**
      * @return false
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
     
     /**
      * {@inheritDoc}
      */
     public void initTextAndInput(IEditorSite site, IEditorInput input) {
         setSite(site);
         setInput(input);
         createPartName();
         getEditorSite().getActionBars().getMenuManager();
     }
     
     /**
      * {@inheritDoc}
      */
     public void fireDirtyProperty(boolean isDirty) {
         // fire property for change of dirty state
         firePropertyChange(PROP_DIRTY);
         if (!isDirty) {
             firePropertyChange(PROP_INPUT);
         }
     }
     
     /**
      * Show the status line.
      */
     public void setFocus() {
         getTreeViewer().getTree().setFocus();
         Plugin.showStatusLine(this);
     }
     /**
      * Does nothing.
      */
     public void doSaveAs() {
         // do nothing
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean isDirty() {
         return getEditorHelper().isDirty();
     }
     
     /**
      * fill the context menu
      * @param mgr IMenuManager
      */
     protected abstract void fillContextMenu(IMenuManager mgr);
     
     /**
      * Create context menu.
      * @return the menu
      */
     protected Menu createContextMenu() {
         // Create menu manager.
         MenuManager menuMgr = new MenuManager();
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager mgr) {
                 fillContextMenu(mgr);
             }
         });
         // Create menu.
         Menu menu = menuMgr.createContextMenu(getControl());
         getControl().setMenu(menu);
         getMainTreeViewer().getControl().setMenu(menu);
         // Register menu for extension.
         getSite().registerContextMenu(menuMgr, this);
         return menu;
     }
     
     /**
      * @param pmce PMCompNameException
      */
     protected void handlePMCompNameException(IncompatibleTypeException pmce) {
         Utils.createMessageDialog(pmce.getErrorId(), 
                 pmce.getErrorMessageParams(), null);
         try {
             ComponentNamesBP.getInstance().init();
         } catch (PMException e) {
             LOG.error(e);
             Utils.createMessageDialog(e, null, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         try {
            if (m_labelDecorator != null) {
                m_labelDecorator.dispose();
            }
             DataEventDispatcher ded = DataEventDispatcher.getInstance();
             ded.removePropertyChangedListener(this);
             if (getEditorSite() != null && getEditorSite().getPage() != null) {
                 GuiEventDispatcher.getInstance()
                     .removeEditorDirtyStateListener(this);
             }
             
             if (getSite() != null) {
                 getSite().setSelectionProvider(null);
             }
             
             if (getEditorSite() != null && getEditorSite().getPage() != null) {
                 getEditorSite().getPage().removePartListener(m_partListener);
             }
             
             if (getEditorHelper() != null) {
                 getEditorHelper().dispose();
             }
         } finally {
             super.dispose();
         }
     }
     
     /**
      * Checks if the MasterSession is up to date.
      */
     protected void checkMasterSessionUpToDate() {
         // nothing here
     }
 
     /**
      * Adds DoubleClickListener to Treeview.
      * 
      * @param commandId
      *            the command to execute on double click event
      */
     protected void addTreeDoubleClickListener(final String commandId) {
         getMainTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 CommandHelper.executeCommand(commandId, getSite());
             }
         });
     }
     
     /**
      * @param editorHelper the editorHelper to set
      */
     public void setEditorHelper(JBEditorHelper editorHelper) {
         m_editorHelper = editorHelper;
     }
 
     /**
      * @return the editorHelper
      */
     public JBEditorHelper getEditorHelper() {
         return m_editorHelper;
     }
 
     /**
      * @param control the control to set
      */
     public void setControl(Control control) {
         m_control = control;
     }
 
     /**
      * @return the control
      */
     public Control getControl() {
         return m_control;
     }
 
     /**
      * @param cutTreeItemAction the cutTreeItemAction to set
      */
     public void setCutTreeItemAction(
             CutTreeItemActionGDEditor cutTreeItemAction) {
         m_cutTreeItemAction = cutTreeItemAction;
     }
 
     /**
      * @return the cutTreeItemAction
      */
     public CutTreeItemActionGDEditor getCutTreeItemAction() {
         return m_cutTreeItemAction;
     }
 
     /**
      * @param pasteTreeItemAction the pasteTreeItemAction to set
      */
     public void setPasteTreeItemAction(
             PasteTreeItemActionTCEditor pasteTreeItemAction) {
         m_pasteTreeItemAction = pasteTreeItemAction;
     }
 
     /**
      * @return the pasteTreeItemAction
      */
     public PasteTreeItemActionTCEditor getPasteTreeItemAction() {
         return m_pasteTreeItemAction;
     }
     
     /**
      * Sets all necessary global action handlers for this editor. This
      * ensures that the editor's actions control the enablement of the 
      * corresponding actions in the main menu.
      */
     protected void setActionHandlers() {
         getSite().setSelectionProvider(this);
         getEditorSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.CUT.getId(), getCutTreeItemAction());
         getEditorSite().getActionBars().setGlobalActionHandler(
                 ActionFactory.PASTE.getId(), getPasteTreeItemAction());
         getEditorSite().getActionBars().updateActionBars();
     }
 
     /**
      * @param revertEditorChangesAction the revertEditorChangesAction to set
      */
     public void setRevertEditorChangesAction(
             RevertEditorChangesHandler revertEditorChangesAction) {
         m_revertEditorChangesAction = revertEditorChangesAction;
     }
 
     /**
      * @return the revertEditorChangesAction
      */
     public RevertEditorChangesHandler getRevertEditorChangesAction() {
         return m_revertEditorChangesAction;
     }
     
     /**
      * {@inheritDoc}
      */
     public void handleEditorDirtyStateChanged(IJBEditor editor, 
             boolean isDirty) {
         if (editor == this) {
             getRevertEditorChangesAction().setEnabled(isDirty);
         }
     }
     
     /**
      * Renames a guiNode.
      * 
      * @param po
      *            The corresponding NodePO.
      */
     protected void renameGUINode(IPersistentObject po) {
         getMainTreeViewer().update(po, null);
         // rename the editor based on currently edited node
         createPartName();
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public EntityManager getEntityManager() {
         return getEditorHelper().getEditSupport().getSession();
     }
 }

 package marmik.sbc.task2.peer.swt;
 
 import marmik.sbc.task2.peer.swt.model.Peer;
 import marmik.sbc.task2.peer.swt.model.Posting;
 import marmik.sbc.task2.peer.swt.model.Session;
 import marmik.sbc.task2.peer.swt.model.SidebarEntry;
 import marmik.sbc.task2.peer.swt.model.Topic;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.map.IObservableMap;
 import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.StatusLineManager;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
 import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.preference.JFacePreferences;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Tree;
 
 public class PeerWindow extends org.eclipse.jface.window.ApplicationWindow {
 
   class Sorter extends ViewerSorter {
 
     @Override
     public int category(Object element) {
       if (element instanceof SidebarEntry) {
         SidebarEntry e = (SidebarEntry) element;
         if (e instanceof Topic) {
           Topic t = (Topic) e;
           return t.getPeer().hashCode();
         } else {
           Peer p = (Peer) e;
           return p.hashCode();
         }
       } else
         throw new IllegalArgumentException("Can only handle SidebarEntry, not " + element.getClass().getName());
     }
 
   }
 
   private DataBindingContext bindingContext;
   private TableViewer tableViewer;
   private Tree tree;
   private Table table;
   private Session session;
   private TreeViewer treeViewer;
 
   /**
    * Create the application window
    */
   protected PeerWindow() {
     super(null);
     createActions();
     JFaceResources.getColorRegistry().put(JFacePreferences.COUNTER_COLOR, new RGB(0, 127, 174));
     JFaceResources.getColorRegistry().put("sidebar.local_peer",
         Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN).getRGB());
     addToolBar(SWT.FLAT | SWT.WRAP);
     addMenuBar();
     addStatusLine();
   }
 
   public PeerWindow(Session session) {
     this();
     this.session = session;
   }
 
   /**
    * Create contents of the application window
    *
    * @param parent
    */
   @Override
   protected Control createContents(Composite parent) {
     Composite container = new Composite(parent, SWT.NONE);
     final GridLayout gridLayout = new GridLayout();
     container.setLayout(gridLayout);
 
     final Composite composite_1 = new Composite(container, SWT.NONE);
     composite_1.setLayoutData(new GridData());
     final GridLayout gridLayout_1 = new GridLayout();
     gridLayout_1.numColumns = 2;
     composite_1.setLayout(gridLayout_1);
 
     final Button topicButton = new Button(composite_1, SWT.NONE);
     topicButton.addSelectionListener(new SelectionAdapter() {
       public void widgetSelected(final SelectionEvent e) {
         InputDialog d = new InputDialog(PeerWindow.this.getShell(), "Add Thema", "Name:", null, null);
         d.open();
         session.getLocalPeer().newTopic(d.getValue());
       }
     });
     topicButton.setText("Add Topic");
 
     final Button addPostingButton = new Button(composite_1, SWT.NONE);
     addPostingButton.addSelectionListener(new SelectionAdapter() {
       public void widgetSelected(final SelectionEvent e) {
         if (tableViewer.getSelection() != null) {
           Topic t = (Topic) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
           InputDialog d = new InputDialog(PeerWindow.this.getShell(), "Add Posting", "Name of Posting to "
               + t.getName() + ":", null, null);
           d.open();
           t.createPosting("Autor", d.getValue(), "leer");
         }
       }
     });
     addPostingButton.setText("Add Posting");
 
     final SashForm sashForm = new SashForm(container, SWT.NONE);
     sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
     tableViewer = new TableViewer(sashForm, SWT.BORDER);
     tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
       // Disable selection of instanceof Peer
       private IStructuredSelection previousSelection = null;
 
       public void selectionChanged(SelectionChangedEvent event) {
         IStructuredSelection selection = (IStructuredSelection) event.getSelection();
         if (selection.getFirstElement() instanceof Peer)
           tableViewer.setSelection(previousSelection);
         else
           previousSelection = selection;
       }
     });
 
     tableViewer.setSorter(new Sorter());
     table = tableViewer.getTable();
     final GridData gd_table = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2);
     gd_table.minimumHeight = 50;
     gd_table.widthHint = 160;
     gd_table.minimumWidth = 20;
     table.setLayoutData(gd_table);
 
     final Composite composite = new Composite(sashForm, SWT.NONE);
     final GridLayout compositeGridLayout = new GridLayout();
     compositeGridLayout.marginHeight = 0;
     compositeGridLayout.marginWidth = 0;
     composite.setLayout(compositeGridLayout);
 
     final SashForm sashForm_1 = new SashForm(composite, SWT.VERTICAL);
     sashForm_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
     treeViewer = new TreeViewer(sashForm_1, SWT.BORDER);
     tree = treeViewer.getTree();
     final GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true);
     gd_tree.minimumHeight = 50;
     gd_tree.widthHint = 400;
     gd_tree.minimumWidth = 100;
     tree.setLayoutData(gd_tree);
 
     final ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm_1, SWT.BORDER | SWT.H_SCROLL
         | SWT.V_SCROLL);
     scrolledComposite.setExpandVertical(true);
     scrolledComposite.setExpandHorizontal(true);
 
     final PostingComposite postingComposite = new PostingComposite(scrolledComposite, SWT.NONE);
     postingComposite.setSize(300, 0);
     scrolledComposite.setContent(postingComposite);
     sashForm_1.setWeights(new int[] { 1, 2 });
     sashForm.setWeights(new int[] { 1, 3 });
 
     //
     bindingContext = initDataBindings();
     return container;
   }
 
   /**
    * Create the actions
    */
   private void createActions() {
     // Create the actions
   }
 
   /**
    * Create the menu manager
    *
    * @return the menu manager
    */
   @Override
   protected MenuManager createMenuManager() {
     MenuManager menuManager = new MenuManager("menu");
     return menuManager;
   }
 
   /**
    * Create the toolbar manager
    *
    * @return the toolbar manager
    */
   @Override
   protected ToolBarManager createToolBarManager(int style) {
     ToolBarManager toolBarManager = new ToolBarManager(style);
     return toolBarManager;
   }
 
   /**
    * Create the status line manager
    *
    * @return the status line manager
    */
   @Override
   protected StatusLineManager createStatusLineManager() {
     StatusLineManager statusLineManager = new StatusLineManager();
     statusLineManager.setMessage(null, "");
     return statusLineManager;
   }
 
   @Override
   protected void configureShell(Shell newShell) {
     super.configureShell(newShell);
     newShell.setText("New Application");
   }
 
   /**
    * Return the initial size of the window
    */
   @Override
   protected Point getInitialSize() {
     return new Point(500, 375);
   }
 
   protected DataBindingContext initDataBindings() {
     //
     ObservableListContentProvider listViewerContentProviderList = new ObservableListContentProvider();
     tableViewer.setContentProvider(listViewerContentProviderList);
     tableViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new SidebarLabelProvider()));
     tableViewer.setInput(session.getSidebarEntries());
 
     ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(BeansObservables
        .listFactory(Realm.getDefault(), "replies", Posting.class), null);
     treeViewer.setContentProvider(contentProvider);
     treeViewer.setLabelProvider(new ObservableMapLabelProvider(new IObservableMap[] {
         BeansObservables.observeMap(contentProvider.getKnownElements(), Posting.class, "subject"),
         BeansObservables.observeMap(contentProvider.getKnownElements(), Posting.class, "author") }));
 
     IObservableValue selection = ViewersObservables.observeSingleSelection(tableViewer);
    IObservableValue postings = BeansObservables.observeDetailValue(Realm.getDefault(), selection, "topLevelPosting", Posting.class);
     IObservableValue input = ViewersObservables.observeInput(treeViewer);
 
     //
     DataBindingContext bindingContext = new DataBindingContext();
     bindingContext.bindValue(input, postings, null, null);// new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
 
     //
     //
     return bindingContext;
   }
 
   @Override
   protected void handleShellCloseEvent() {
     super.handleShellCloseEvent();
     session.getBacking().logout();
   }
 }

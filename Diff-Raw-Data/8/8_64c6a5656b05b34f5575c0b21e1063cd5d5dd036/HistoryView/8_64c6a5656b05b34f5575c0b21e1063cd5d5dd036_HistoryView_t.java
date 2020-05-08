 package mishanesterenko.changevisualizer.view;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import mishanesterenko.changevisualizer.activator.ChangeVisualizerPlugin;
 import mishanesterenko.changevisualizer.projectmodel.CustomProject;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
 import org.eclipse.ui.statushandlers.StatusAdapter;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.layout.GridData;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventAdmin;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNLogEntryPath;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 public class HistoryView extends ViewPart {
     public static final String ID = "changeVisualizer.view.history";
 
     public static final String LOG_ENTRY_KEY = "logEntry";
 
     public static final String LOG_ENTRY_PATH_KEY = "logEntryPath";
 
     public static final String VISUALIZATOR_TOPIC = "changeVisualizer/render";
 
     private Color errorColor;
 
     private TableViewer commitViewer;
 
     private Image searchTypeImage;
 
     private boolean messagesIncluded = true;
     private boolean pathsIncluded = true;
     private boolean regexEnabled = true;
 
     private int showCommitCount = 100;
 
     private String searchText = "";
 
     private Text searchTextInput;
 
     private Button filterCommitListButton;
 
     private int errorCount;
 
     private SearchTextInputValidator searchTextValidator;
 
     private ISelectionListener selectionHandler;
 
     private CustomProject currentProject;
 
     private Text showCommitCountInput;
 
     private Text commitDetailedMessageInput;
 
     private ListViewer changedPathsViewer;
 
     private EventAdmin messageService;
     private Button updateCommitListButton;
 
     public HistoryView() {
         ImageDescriptor id = AbstractUIPlugin.imageDescriptorFromPlugin(ChangeVisualizerPlugin.PLUGIN_ID, "icons/searchtype.png");
         searchTypeImage = id.createImage();
     }
 
     @Override
     public void createPartControl(Composite parent) {
         errorColor = getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
         BundleContext ctx = ChangeVisualizerPlugin.getPlugin().getBundle().getBundleContext();
         ServiceReference<EventAdmin> sr = ctx.getServiceReference(EventAdmin.class);
         messageService = ctx.getService(sr);
 
         GridLayout gl_parent = new GridLayout(1, false);
         gl_parent.verticalSpacing = 0;
         gl_parent.horizontalSpacing = 0;
         gl_parent.marginHeight = 0;
         gl_parent.marginWidth = 0;
         parent.setLayout(gl_parent);
 
         createTopPanel(parent);
         createMiddlePanel(parent);
         createBottomPanel(parent);
 
         getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionHandler = new ProjectSelectionHandler());
     }
 
     private MenuManager createSearchTypeMenu() {
         MenuManager mm = new MenuManager();
         mm.add(new MenuCheckAction("Messages", messagesIncluded) {
             @Override
             public void run() {
                 super.run();
                 messagesIncluded = isChecked();
             }
         });
         mm.add(new MenuCheckAction("Paths", pathsIncluded) {
             @Override
             public void run() {
                 super.run();
                 pathsIncluded = isChecked();
             }
         });
         mm.add(new Separator());
         mm.add(new MenuCheckAction("Regex", regexEnabled) {
             @Override
             public void run() {
                 super.run();
                 regexEnabled = isChecked();
                 searchTextValidator.validate();
             }
         });
         return mm;
     }
 
     protected void createTopPanel(final Composite parent) {
         Composite panel = new Composite(parent, SWT.NONE);
         panel.setLayout(new GridLayout(3, false));
         panel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
         { // image with a guy
             Label image = new Label(panel, SWT.NONE);
             image.setImage(searchTypeImage);
             image.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
             image.setMenu(createSearchTypeMenu().createContextMenu(image));
         }
 
         { // search input
             searchTextInput = new Text(panel, SWT.BORDER);
             searchTextInput.setEnabled(false);
             searchTextInput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
             searchTextInput.setText(searchText);
             searchTextInput.addModifyListener(searchTextValidator = new SearchTextInputValidator() {
                 @Override
                 public void modifyText(ModifyEvent e) {
                     super.modifyText(e);
                     searchText = searchTextInput.getText();
                 }
             });
         }
 
         { // go button
             filterCommitListButton = new Button(panel, SWT.PUSH);
             filterCommitListButton.setEnabled(false);
             filterCommitListButton.addSelectionListener(new FilterButtonClickHandler());
             filterCommitListButton.setText("Filter");
             filterCommitListButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
             filterCommitListButton.addSelectionListener(new FilterButtonClickHandler());
         }
     }
 
     protected void createHistoryTable(final Composite parent) {
         commitViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
         Table table = commitViewer.getTable();
         table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         table.setHeaderVisible(true);
         table.setLinesVisible(true);
 
         commitViewer.setContentProvider(ArrayContentProvider.getInstance());
         commitViewer.addSelectionChangedListener(new CommitSelectionHandler());
         commitViewer.setFilters(new ViewerFilter[] { new CommitTableFilter() });
         commitViewer.setComparator(new CommitTableSorter());
 
         TableViewerColumn column = createTableViewerColumn("Revision", 100, 0);
         column.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 SVNLogEntry logEntry = (SVNLogEntry) element;
                 return Long.toString(logEntry.getRevision());
             }
         });
 
         column = createTableViewerColumn("Author", 100, 1);
         column.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 SVNLogEntry logEntry = (SVNLogEntry) element;
                 return logEntry.getAuthor();
             }
         });
 
         column = createTableViewerColumn("Date", 100, 2);
         column.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 SVNLogEntry logEntry = (SVNLogEntry) element;
                 return logEntry.getDate().toString();
             }
         });
 
         column = createTableViewerColumn("Message", 500, 3);
         column.setLabelProvider(new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 SVNLogEntry logEntry = (SVNLogEntry) element;
                 String message = logEntry.getMessage();
                 int nIndex = message.indexOf('\n');
                 int rIndex = message.indexOf('\r');
                 int val = rIndex < 0 || nIndex >= 0 && nIndex < rIndex ? nIndex : rIndex;
                 if (val >= 0) {
                     message = message.substring(0, val);
                 }
                 return message;
             }
         });
     }
 
     protected void createChangedPathsList(final Composite parent) {
         changedPathsViewer = new ListViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
         changedPathsViewer.setContentProvider(ArrayContentProvider.getInstance());
         changedPathsViewer.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 SVNLogEntryPath path = (SVNLogEntryPath) element;
                 return path.getPath();
             }
         });
     }
 
     protected void createMiddlePanel(final Composite parent) {
         Composite panel = new Composite(parent, SWT.NONE);
         {
             FillLayout fl = new FillLayout();
             fl.marginHeight = 0;
             fl.marginWidth = 0;
             panel.setLayout(fl);
             panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         }
 
         SashForm verticalSf = new SashForm(panel, SWT.VERTICAL);
         SashForm horizontalSf = new SashForm(verticalSf, SWT.HORIZONTAL);
 
         createHistoryTable(horizontalSf);
         createChangedPathsList(horizontalSf);
 
         commitDetailedMessageInput = new Text(verticalSf, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
         commitDetailedMessageInput.setEditable(false);
 
         verticalSf.setWeights(new int[] {3, 1});
     }
 
     protected TableViewerColumn createTableViewerColumn(final String title, final int bound, final int colNumber) {
         final TableViewerColumn viewerColumn = new TableViewerColumn(commitViewer, SWT.NONE);
         final TableColumn column = viewerColumn.getColumn();
         column.setText(title);
         column.setWidth(bound);
         column.setResizable(true);
         column.setMoveable(true);
         return viewerColumn;
     }
 
     protected void createBottomPanel(final Composite parent) {
         Composite panel = new Composite(parent, SWT.NONE);
         panel.setLayout(new GridLayout(4, false));
         panel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
         {
             Label l = new Label(panel, SWT.NONE);
             l.setText("Show commit count:");
             l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 
             showCommitCountInput = new Text(panel, SWT.BORDER | SWT.SINGLE | SWT.RIGHT);
             showCommitCountInput.setEnabled(false);
             GridData gd_showCommitCountInput = new GridData(SWT.LEFT, SWT.CENTER, false, false);
             gd_showCommitCountInput.widthHint = 50;
             showCommitCountInput.setLayoutData(gd_showCommitCountInput);
             showCommitCountInput.setText(Integer.toString(showCommitCount));
             {
                 updateCommitListButton = new Button(panel, SWT.NONE);
                 updateCommitListButton.setEnabled(false);
                 updateCommitListButton.setText("Update");
                 updateCommitListButton.addSelectionListener(new UpdateButtonClickHandler());
             }
             new Label(panel, SWT.NONE);
             showCommitCountInput.addModifyListener(new CommitCountValidator() {
                 @Override
                 public boolean validate() {
                     boolean isValid = super.validate();
                     if (isValid) {
                         showCommitCount = Integer.parseInt(showCommitCountInput.getText());
                     }
                     return isValid;
                 }
             });
         }
     }
 
     /**
      * Passing the focus request to the commitViewer's control.
      */
     @Override
     public void setFocus() {
         commitViewer.getControl().setFocus();
     }
 
     @Override
     public void dispose() {
         searchTypeImage.dispose();
         getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionHandler);
         super.dispose();
     }
 
     protected void updateValidStatus() {
         filterCommitListButton.setEnabled(isValid());
         updateCommitListButton.setEnabled(isValid());
     }
 
     protected boolean isValid() {
         return errorCount == 0;
     }
 
     protected void increaseErrorCount() {
         errorCount++;
         updateValidStatus();
     }
 
     protected void decreaseErrorCount() {
         errorCount--;
         errorCount = errorCount < 0 ? 0 : errorCount;
         updateValidStatus();
     }
 
     protected boolean performLoadCommits(final CustomProject project) {
         try {
             final boolean[] isCanceled = new boolean[] {true};
             ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getSite().getShell());
             progressDialog.run(true, true, new IRunnableWithProgress() {
                 @Override
                 public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                     try {
                         loadCommits(project, monitor);
                         isCanceled[0] = monitor.isCanceled();
                     } catch (SVNException e) {
                         throw new InvocationTargetException(e);
                     }
                 }
             });
             return !isCanceled[0];
         } catch (Exception e) {
             Throwable cause = e.getCause();
             cause = cause == null ? e : cause;
             IStatus status = new Status(IStatus.ERROR, ChangeVisualizerPlugin.PLUGIN_ID, cause.getMessage(), cause);
 
             StatusAdapter statusAdapter = new StatusAdapter(status);
             statusAdapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, "Error");
             StatusManager.getManager().handle(statusAdapter, StatusManager.BLOCK);
             return false;
         }
     }
 
     @SuppressWarnings("unchecked")
     protected void loadCommits(final CustomProject project, IProgressMonitor monitor) throws SVNException {
         final int COMMIT_LOAD_PACK = 50;
         monitor = monitor == null ? new NullProgressMonitor() : monitor;
         monitor.beginTask("Loading commits from " + project.getRepositoryLocation(), showCommitCount);
 
         SVNRepository repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(project.getRepositoryLocation()));
         try {
             ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(project.getUsername(),
                     project.getUserPassword());
             repository.setAuthenticationManager(authManager);
 
            Collection<SVNLogEntry> logEntries = repository.log(new String[] {""}, null, -1, -1, true, true);
             Iterator<SVNLogEntry> it = logEntries.iterator();
             SVNLogEntry logEntry = null;
             while (it.hasNext()) {
                 logEntry = it.next();
                 break;
             }
             if (monitor.isCanceled()) {
                 return;
             }
             final List<SVNLogEntry> logEntryArray = new ArrayList<SVNLogEntry>();
             if (logEntry != null) { 
                long fromRevision = logEntry.getRevision();
                 monitor.worked(logEntries.size());
                 while (!monitor.isCanceled() && fromRevision > 1 && logEntries.size() < showCommitCount) {
                     long probableRevision = fromRevision - Math.min(showCommitCount - logEntries.size(), COMMIT_LOAD_PACK);
                     Collection<SVNLogEntry> moreLogEntries =
                             repository.log(new String[] {""}, null, probableRevision, fromRevision - 1, true, true);
                     monitor.worked(moreLogEntries.size());
                     logEntries.addAll(moreLogEntries);
                     fromRevision = probableRevision;
                 }
                 logEntryArray.addAll(logEntries);
                 Collections.reverse(logEntryArray);
             }
             if (!monitor.isCanceled()) {
                 Display.getDefault().syncExec(new Runnable() {
                     @Override
                     public void run() {
                         currentProject = project;
                         commitViewer.setInput(logEntryArray);
                     }
                 });
             }
         } finally {
             monitor.done();
             repository.closeSession();
         }
     }
 
     protected void sendMessage(final SVNLogEntry logEntry, final SVNLogEntryPath path) {
         Map<String, Object> props = new HashMap<String, Object>();
         props.put(LOG_ENTRY_KEY, logEntry);
         props.put(LOG_ENTRY_PATH_KEY, path);
 
         Event event = new Event(VISUALIZATOR_TOPIC, props);
         messageService.postEvent(event);
     }
 
     protected class MenuCheckAction extends Action {
         public MenuCheckAction(final String text, final boolean checked) {
             super(text, IAction.AS_CHECK_BOX);
             setChecked(checked);
         }
     }
 
     protected class SearchTextInputValidator implements ModifyListener {
         private boolean wasValid = true;
 
         public void validate() {
             if (regexEnabled) {
                 try {
                     Pattern.compile(searchTextInput.getText());
                     if(!wasValid) {
                         wasValid = true;
                         decreaseErrorCount();
                         searchTextInput.setBackground(null);
                     }
                 } catch (PatternSyntaxException ee) {
                     if (wasValid) {
                         wasValid = false;
                         increaseErrorCount();
                         searchTextInput.setBackground(errorColor);
                     }
                 }
             } else if (!wasValid) {
                 wasValid = true;
                 decreaseErrorCount();
                 searchTextInput.setBackground(null);
             }
         }
 
         @Override
         public void modifyText(ModifyEvent e) {
             validate();
         }
     }
 
     protected class CommitCountValidator implements ModifyListener {
         private boolean wasValid = true;
 
         public boolean validate() {
             try {
                 if (Integer.parseInt(showCommitCountInput.getText()) <= 0) {
                     throw new NumberFormatException();
                 }
                 if(!wasValid) {
                     wasValid = true;
                     decreaseErrorCount();
                     showCommitCountInput.setBackground(null);
                 }
                 return true;
             } catch (NumberFormatException ee) {
                 if (wasValid) {
                     wasValid = false;
                     increaseErrorCount();
                     showCommitCountInput.setBackground(errorColor);
                 }
                 return false;
             }
         }
 
         @Override
         public void modifyText(ModifyEvent e) {
             validate();
         }
     }
 
     protected class FilterButtonClickHandler extends SelectionAdapter {
         @Override
         public void widgetSelected(SelectionEvent e) {
             super.widgetSelected(e);
             commitViewer.refresh();
         }
     }
 
     protected class UpdateButtonClickHandler extends SelectionAdapter {
         @Override
         public void widgetSelected(SelectionEvent e) {
             super.widgetSelected(e);
             performLoadCommits(currentProject);
         }
     }
 
     protected class ProjectSelectionHandler implements ISelectionListener {
         @Override
         public void selectionChanged(IWorkbenchPart part, ISelection selection) {
             if (selection instanceof IStructuredSelection) {
                 IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                 if (structuredSelection.getFirstElement() instanceof CustomProject
                         && (currentProject !=null && structuredSelection.getFirstElement() != currentProject
                             || currentProject == null)) {
                     boolean loaded = performLoadCommits((CustomProject) structuredSelection.getFirstElement());
                     showCommitCountInput.setEnabled(loaded);
                     searchTextInput.setEnabled(loaded);
                     filterCommitListButton.setEnabled(loaded);
                     updateCommitListButton.setEnabled(loaded);
                 }
             }
         }
     }
 
     protected class CommitSelectionHandler implements ISelectionChangedListener {
         private void updateDetailedMessage(final SVNLogEntry logEntry) {
             String message = logEntry == null ? "" : logEntry.getMessage();
             commitDetailedMessageInput.setText(message);
         }
 
         private void updateChangedPaths(final SVNLogEntry logEntry) {
             changedPathsViewer.setInput(logEntry == null ? null : logEntry.getChangedPaths().values());
         }
 
         @Override
         public void selectionChanged(final SelectionChangedEvent event) {
             IStructuredSelection s = (IStructuredSelection) event.getSelection();
             SVNLogEntry logEntry = (SVNLogEntry) s.getFirstElement();
             updateDetailedMessage(logEntry);
             updateChangedPaths(logEntry);
         }
         
     }
 
     protected class CommitTableFilter extends ViewerFilter {
         private abstract class Matcher {
             public abstract boolean match(String text);
         }
 
         private class SimpleMatcher extends Matcher {
             public String pattern;
 
             public SimpleMatcher(final String matchPattern) {
                 pattern = matchPattern;
             }
 
             @Override
             public boolean match(String text) {
                 return text.toLowerCase().contains(pattern.toLowerCase());
             }
         }
 
         private class RegexMatcher extends Matcher {
             public Pattern pattern;
 
             public RegexMatcher(final Pattern matchPattern) {
                 pattern = matchPattern;
             }
 
             @Override
             public boolean match(String text) {
                 return pattern.matcher(text).find();
             }
         }
 
         @Override
         public boolean select(Viewer viewer, Object parentElement, Object element) {
             if (!isValid()) {
                 return true;
             }
             SVNLogEntry logEntry = (SVNLogEntry) element;
             String filterText = searchText.trim();
             if (filterText.length() > 0) {
                 boolean matched = false;
                 Matcher matcher = regexEnabled ? new RegexMatcher(Pattern.compile(filterText, Pattern.CASE_INSENSITIVE))
                     : new SimpleMatcher(filterText);
                 if (!messagesIncluded || !(matched = matcher.match(logEntry.getMessage()))) {
                     if (pathsIncluded) {
                         for (String path : logEntry.getChangedPaths().keySet()) {
                             if (matched = matcher.match(path)) {
                                 break;
                             }
                         }
                     }
                 }
                 return matched;
             }
 
             return true;
         }
     }
 
     protected class CommitTableSorter extends ViewerComparator {
         @Override
         public int compare(Viewer viewer, Object e1, Object e2) {
             SVNLogEntry entry1 = (SVNLogEntry) e1;
             SVNLogEntry entry2 = (SVNLogEntry) e2;
             return (int) (entry2.getRevision() - entry1.getRevision());
         }
     }
 
 }

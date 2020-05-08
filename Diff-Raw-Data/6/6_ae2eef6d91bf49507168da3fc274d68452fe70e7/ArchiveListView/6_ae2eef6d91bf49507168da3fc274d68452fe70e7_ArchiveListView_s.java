 package net.fornwall.eclipsecoder.archive;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 import net.fornwall.eclipsecoder.languages.LanguageSupportFactory;
 import net.fornwall.eclipsecoder.preferences.EclipseCoderPlugin;
 import net.fornwall.eclipsecoder.util.Utilities;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 import org.eclipse.ui.part.ViewPart;
 import org.xml.sax.SAXException;
 
 /**
  * A view which allows the user to browse and select problems for download and
  * project generation from the TopCoder problem archive.
  */
 public class ArchiveListView extends ViewPart {
 
 	static ArchiveListView instance;
 
 	public static class ListReference {
 		List<ProblemStats> problemStats;
 	}
 
 	private Job updateListJob = new Job(Messages.updatingProblemList) {
 
 		{
 			setUser(true);
 		}
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			if (!EclipseCoderPlugin.isTcAccountSpecified()) {
 				EclipseCoderPlugin.demandTcAccountSpecified();
 				return Status.OK_STATUS;
 			}
 
 			monitor.beginTask(Messages.updatingProblemList, 100);
 			final ListReference listReference = new ListReference();
 			IStatus status = ProblemScraper.downloadProblemStats(monitor, listReference);
 			if (!status.isOK())
 				return status;
 			if (monitor.isCanceled())
 				return Status.CANCEL_STATUS;
 
 			monitor.subTask(Messages.updatingTable);
 
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 				@SuppressWarnings("synthetic-access")//$NON-NLS-1$
 				public void run() {
 					viewer.setInput(listReference.problemStats);
 					for (int i = 0; i < problemListTable.getColumnCount(); i++) {
 						problemListTable.getColumn(i).pack();
 					}
 					setContentDescription(""); //$NON-NLS-1$
 				}
 			});
 			return status;
 		}
 	};
 
 	Table problemListTable;
 
 	TableViewer viewer;
 
 	@Override
 	public void createPartControl(final Composite parent) {
 		ArchiveListView.instance = this;
 
 		fillLocalPullDown(getViewSite().getActionBars().getMenuManager());
 
 		createTable(parent);
 	}
 
 	void createTable(Composite parent) {
 		problemListTable = new Table(parent, SWT.FULL_SELECTION);
 		problemListTable.setHeaderVisible(true);
 		problemListTable.setLinesVisible(true);
 
 		viewer = new TableViewer(problemListTable);
 		viewer.setLabelProvider(new ProblemLabelProvider());
 		viewer.setContentProvider(new ArrayContentProvider());
 
 		Menu rightClickMenu = new Menu(viewer.getTable());
 		MenuItem viewSubmissionsItem = new MenuItem(rightClickMenu, SWT.PUSH);
 		viewSubmissionsItem.setText(Messages.viewSubmissionList);
 		viewSubmissionsItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				ProblemStats stats = (ProblemStats) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
 				if (stats == null) {
 					return;
 				}
 
 				Job job = new SubmissionListFetcherJob(stats);
 				job.setUser(true);
 				job.schedule();
 			}
 		});
 
 		MenuItem viewEditorialItem = new MenuItem(rightClickMenu, SWT.PUSH);
 		viewEditorialItem.setText(Messages.viewMatchEditorial);
 		viewEditorialItem.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final ProblemStats stats = (ProblemStats) ((IStructuredSelection) viewer.getSelection())
 						.getFirstElement();
 				try {
 					// TODO: Cache a bit?
					URLConnection c = new URL("http://www.topcoder.com/tc?module=Static&d1=match_editorials&d2=archive") //$NON-NLS-1$
 							.openConnection();
 					BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
 					try {
 						String line;
 						while ((line = reader.readLine()) != null) {
 							if (line.contains(stats.getContestName())) {
 								String href = Utilities.getMatch(line, "<a href=\"(.*?)\"", 1); //$NON-NLS-1$
 								href = href.replaceAll("&amp;", "&");
 								// jump to the right place directly - older
 								// problem statements are not formatted this
 								// way, though it cannot hurt
 								href += "#" + stats.getProblemId(); //$NON-NLS-1$
 								PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
 										IWorkbenchBrowserSupport.AS_VIEW, ArchiveListView.class.getCanonicalName(),
 										"", "").openURL( //$NON-NLS-1$ //$NON-NLS-2$
										new URL("http://www.topcoder.com" //$NON-NLS-1$
												+ href));
 								return;
 							}
 						}
 					} finally {
 						reader.close();
 					}
 					Utilities.showMessageDialog(Messages.noMatchEditorialFound,
 							Messages.noMatchEditorialFoundDescription);
 				} catch (Exception e1) {
 					Utilities.showException(e1);
 				}
 			}
 		});
 
 		viewer.getTable().setMenu(rightClickMenu);
 
 		for (String columnTitle : ProblemStats.COLUMN_NAMES) {
 			TableColumn column = new TableColumn(problemListTable, SWT.LEFT);
 			column.setText(columnTitle);
 		}
 
 		viewer.setSorter(new ProblemComparator(0, false));
 		problemListTable.setSortColumn(problemListTable.getColumn(0));
 		problemListTable.setSortDirection(SWT.DOWN);
 
 		for (int i = 0; i < problemListTable.getColumnCount(); i++) {
 			final int index = i;
 			problemListTable.getColumn(i).addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					ProblemComparator previousSorter = (ProblemComparator) viewer.getSorter();
 					boolean reversed = false;
 					if (previousSorter.column == index) {
 						reversed = !previousSorter.reversed;
 					}
 					problemListTable.setSortColumn(problemListTable.getColumn(index));
 					problemListTable.setSortDirection(reversed ? SWT.UP : SWT.DOWN);
 					viewer.setSorter(new ProblemComparator(index, reversed));
 				}
 			});
 			problemListTable.getColumn(i).pack();
 		}
 
 		problemListTable.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseDoubleClick(MouseEvent mouseEvent) {
 				ProblemStats stats = (ProblemStats) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
 				if (stats == null) {
 					return;
 				}
 
 				if (!EclipseCoderPlugin.isTcAccountSpecified()) {
 					EclipseCoderPlugin.demandTcAccountSpecified();
 					return;
 				}
 
 				Job job;
 				if ((new File(new File(System.getProperty("user.home")), ".enablefetchmarker")).exists()) {
 					job = new ProblemFetcherJob(stats);
 				} else {
 					job = new SubmissionListFetcherJob(stats);
 				}
 				job.setUser(true);
 				job.schedule();
 			}
 		});
 
 		List<ProblemStats> problemList = ProblemScraper.loadProblemStats();
 
 		if (problemList == null) {
 			setContentDescription(Messages.needToDownloadProblemArchive);
 		} else {
 			setContentDescription(""); //$NON-NLS-1$
 			viewer.setInput(problemList);
 			for (int i = 0; i < problemListTable.getColumnCount(); i++) {
 				problemListTable.getColumn(i).pack();
 			}
 		}
 	}
 
 	private void fillLocalPullDown(IMenuManager menuManager) {
 		ImageDescriptor updateListImage = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
 				ISharedImages.IMG_TOOL_COPY);
 
 		IAction theAction = new DisabledWhileRunningJobAction(Messages.updateListActionName, updateListImage,
 				updateListJob);
 		menuManager.add(theAction);
 
 		String preferedLanguage = EclipseCoderPlugin.getDefault().getPreferenceStore().getString(
 				EclipseCoderPlugin.PREFERENCE_LANGUAGE);
 		final String LANG_GROUP = "lang_group"; //$NON-NLS-1$
 
 		MenuManager langMenuManager = new MenuManager(Messages.useLanguageActionName, null);
 		menuManager.add(new Separator());
 		menuManager.add(langMenuManager);
 
 		langMenuManager.add(new GroupMarker(LANG_GROUP));
 		for (String lang : LanguageSupportFactory.supportedLanguages()) {
 			Action langAction = new Action(lang, IAction.AS_RADIO_BUTTON) {
 				@Override
 				public void run() {
 					if (isChecked()) {
 						EclipseCoderPlugin.getDefault().getPreferenceStore().setValue(
 								EclipseCoderPlugin.PREFERENCE_LANGUAGE, getText());
 					}
 				}
 			};
 			langMenuManager.appendToGroup(LANG_GROUP, langAction);
 			if (lang.equals(preferedLanguage)) {
 				langAction.setChecked(true);
 			}
 		}
 
 	}
 
 	@Override
 	public void setFocus() {
 		// if (downloadButton != null) {
 		// downloadButton.setFocus();
 		// } else if (problemListTable != null) {
 		problemListTable.setFocus();
 		// }
 	}
 }
 
 @SuppressWarnings("serial")//$NON-NLS-1$
 class CanceledException extends SAXException {
 	// just a tag class
 }
 
 class ProblemComparator extends ViewerSorter {
 	public int column;
 
 	public boolean reversed;
 
 	public ProblemComparator(int column, boolean reversed) {
 		this.column = column;
 		this.reversed = reversed;
 	}
 
 	@Override
 	public int compare(Viewer viewer, Object e1, Object e2) {
 		ProblemStats s1 = (ProblemStats) e1;
 		ProblemStats s2 = (ProblemStats) e2;
 		int ret = s1.compareTo(s2, column);
 		return (reversed ? -1 : 1) * ret;
 	}
 
 }
 
 class ProblemLabelProvider extends LabelProvider implements ITableLabelProvider {
 
 	public Image getColumnImage(Object element, int columnIndex) {
 		return null;
 	}
 
 	public String getColumnText(Object element, int columnIndex) {
 		ProblemStats stats = (ProblemStats) element;
 		return stats.getFieldString(columnIndex);
 	}
 
 }

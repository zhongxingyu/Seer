 /*******************************************************************************
  * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.ui.editor;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.reflect.InvocationTargetException;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.Locale;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.IJobChangeListener;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreeExpansionEvent;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.ui.forms.widgets.TableWrapLayout;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.progress.IProgressConstants;
 import org.eclipse.virgo.ide.runtime.core.IServerWorkingCopy;
 import org.eclipse.virgo.ide.runtime.core.ServerCorePlugin;
 import org.eclipse.virgo.ide.runtime.core.artefacts.Artefact;
 import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepository;
 import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactRepositoryManager;
 import org.eclipse.virgo.ide.runtime.core.artefacts.BundleArtefact;
 import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
 import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefactTyped;
 import org.eclipse.virgo.ide.runtime.core.artefacts.LibraryArtefact;
 import org.eclipse.virgo.ide.runtime.core.artefacts.LocalBundleArtefact;
 import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryProvisioningJob;
 import org.eclipse.virgo.ide.runtime.core.provisioning.RepositorySourceProvisiongJob;
 import org.eclipse.virgo.ide.runtime.core.provisioning.RepositoryUtils;
 import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
 import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiPlugin;
 import org.eclipse.virgo.ide.runtime.internal.ui.providers.RepositorySearchResultContentProvider;
 import org.eclipse.virgo.ide.runtime.internal.ui.sorters.RepositoryViewerSorter;
import org.eclipse.virgo.ide.runtime.ui.ServerEditorPageLabelProvider;
 import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.ui.editor.ServerEditorPart;
 
 /**
  * {@link ServerEditorPart} that allows to browse the local and remote bundle repository.
  * 
  * @author Christian Dupuis
  * @author Miles Parker
  * @since 1.0.0
  */
 public class RepositoryBrowserEditorPage extends ServerEditorPart implements ISelectionChangedListener {
 
 	private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
 
 	private Button anaylseButton;
 
 	private Button downloadButton;
 
 	private Button downloadSourcesCheckbox;
 
 	private final IJobChangeListener jobListener = new ArtefactOperationJobListener();
 
 	private IServerWorkingCopy serverWC;
 
 	private Button refreshButton;
 
 	private PropertyChangeListener propertyListener;
 
 	private final RepositorySearchResultContentProvider searchResultContentProvider = new RepositorySearchResultContentProvider();
 
 	private final ColoredRespositoryLabelProvider coloredRespositoryLabelProvider = new ColoredRespositoryLabelProvider();
 
 //	private Tree repositoryTable;
 
 	private CommonViewer repositoryTableViewer;
 
 	private Button searchButton;
 
 	private Button downloadSourcesButton;
 
 	private Tree searchResultTable;
 
 	private CheckboxTreeViewer searchResultTableViewer;
 
 	private Text searchText;
 
 	private Shell shell;
 
 	private Button licenseButton;
 
 	private Button openManifestButton;
 
 	private Link update;
 
 	private static String PROXY_PREF_PAGE_ID = Messages.RepositoryBrowserEditorPage_0;
 
 	@Override
 	public void createPartControl(Composite parent) {
 
 		shell = parent.getShell();
 
 		FormToolkit toolkit = getFormToolkit(parent.getDisplay());
 
 		ScrolledForm form = toolkit.createScrolledForm(parent);
 		toolkit.decorateFormHeading(form.getForm());
 		form.setText(Messages.RepositoryBrowserEditorPage_BundleBrowserLabel);
 		form.setImage(getFormImage());
 		GridLayout layout = new GridLayout(2, true);
 		layout.marginTop = 6;
 		layout.marginLeft = 6;
 		form.getBody().setLayout(layout);
 
 		Section leftSection = createLeftSection(toolkit, form);
 		createRightSection(toolkit, form);
 
 		form.setContent(leftSection);
 		// This reflow breaks the TableWrapLayout around the repository
 		// hyperlink
 		// form.reflow(true);
 
 		initialize();
 	}
 
 	protected Image getFormImage() {
 		return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_VIRGO);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 
 		if (serverWC != null) {
 			serverWC.removeConfigurationChangeListener(propertyListener);
 		}
 		Job.getJobManager().removeJobChangeListener(jobListener);
 	}
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input) {
 		super.init(site, input);
 
 		IServerWorkingCopy ts = (IServerWorkingCopy) server.loadAdapter(IServerWorkingCopy.class, null);
 		serverWC = ts;
 		addChangeListener();
 		initialize();
 	}
 
 	@Override
 	public void setFocus() {
 		if (searchResultTable != null) {
 			searchResultTable.setFocus();
 		}
 	}
 
 	private Section createLeftSection(FormToolkit toolkit, ScrolledForm form) {
 		GridLayout layout;
 		Section leftSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
 		leftSection.setText(Messages.RepositoryBrowserEditorPage_SearchBundlesAndLibraries);
 		leftSection.setDescription(Messages.RepositoryBrowserEditorPage_SearchBundlesAndLibrariesByName);
 		leftSection.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite leftComposite = toolkit.createComposite(leftSection);
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		leftComposite.setLayout(layout);
 		leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		toolkit.paintBordersFor(leftComposite);
 		leftSection.setClient(leftComposite);
 
 		searchText = toolkit.createText(leftComposite, Messages.RepositoryBrowserEditorPage_4);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		searchText.setLayoutData(data);
 		searchText.addKeyListener(new KeyListener() {
 
 			public void keyPressed(KeyEvent e) {
 				if (e.character == SWT.CR || e.character == SWT.LF) {
 					handleSearch();
 				}
 			}
 
 			public void keyReleased(KeyEvent e) {
 			}
 		});
 
 		Composite searchButtonComposite = new Composite(leftComposite, SWT.NONE);
 		searchButtonComposite.setLayout(new GridLayout(1, true));
 		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		searchButtonComposite.setLayoutData(data);
 
 		searchButton = toolkit.createButton(searchButtonComposite, Messages.RepositoryBrowserEditorPage_Search,
 				SWT.PUSH);
 		searchButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleSearch();
 			}
 		});
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = 100;
 		searchButton.setLayoutData(data);
 
 		searchResultTable = toolkit.createTree(leftComposite, SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK);
 
 		searchResultTableViewer = new CheckboxTreeViewer(searchResultTable);
 		searchResultTableViewer.setContentProvider(searchResultContentProvider);
 		searchResultTableViewer.setLabelProvider(coloredRespositoryLabelProvider);
 		searchResultTableViewer.setInput(this); // activate content provider
 		searchResultTableViewer.setSorter(new RepositoryViewerSorter());
 		searchResultTableViewer.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				handleCheckStateChange(event);
 				downloadButton.setEnabled(searchResultTableViewer.getCheckedElements().length > 0);
 			}
 		});
 		searchResultTableViewer.addTreeListener(new ITreeViewerListener() {
 			public void treeCollapsed(TreeExpansionEvent event) {
 			}
 
 			public void treeExpanded(TreeExpansionEvent event) {
 				final Object element = event.getElement();
 				if (searchResultTableViewer.getGrayed(element) == false) {
 					BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
 						public void run() {
 							setSubtreeChecked(element, searchResultTableViewer.getChecked(element), false);
 						}
 					});
 				}
 			}
 		});
 		registerContextMenu(searchResultTableViewer);
 
 		data = new GridData(GridData.FILL_BOTH);
 		data.heightHint = 120;
 		searchResultTable.setLayoutData(data);
 		searchResultTable.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				ISelection selection = searchResultTableViewer.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) selection).getFirstElement();
 					if (element instanceof IArtefact) {
 						anaylseButton.setEnabled(true);
 						licenseButton.setEnabled(true);
 					} else {
 						anaylseButton.setEnabled(false);
 						licenseButton.setEnabled(false);
 					}
 				}
 			}
 		});
 
 		Composite buttonComposite = new Composite(leftComposite, SWT.NONE);
 		buttonComposite.setLayout(new GridLayout(1, true));
 		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		buttonComposite.setLayoutData(data);
 
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = 100;
 		Button selectAllButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_SelectAll,
 				SWT.PUSH);
 		selectAllButton.setLayoutData(data);
 		selectAllButton.setToolTipText(Messages.RepositoryBrowserEditorPage_SelectAllBundlesAndLibraries);
 		selectAllButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				searchResultTableViewer.setCheckedElements(searchResultContentProvider.getElements(searchResultTableViewer.getInput()));
 			}
 		});
 
 		Button deselectAllButton = toolkit.createButton(buttonComposite,
 				Messages.RepositoryBrowserEditorPage_DeselectAllBundlesAndLibraries, SWT.PUSH);
 		deselectAllButton.setToolTipText(Messages.RepositoryBrowserEditorPage_9);
 		deselectAllButton.setLayoutData(data);
 		deselectAllButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				searchResultTableViewer.setCheckedElements(new Object[0]);
 			}
 		});
 
 		// insert vertical space to make the download button stand out
 		toolkit.createLabel(buttonComposite, Messages.RepositoryBrowserEditorPage_10);
 
 		anaylseButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_Analyse, SWT.PUSH);
 		anaylseButton.setEnabled(false);
 		anaylseButton.setLayoutData(data);
 		anaylseButton.setToolTipText(Messages.RepositoryBrowserEditorPage_AnalyseSelected);
 		anaylseButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				ISelection selection = searchResultTableViewer.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) selection).getFirstElement();
 					if (element instanceof IArtefact) {
 						WebUiUtils.openUrl(RepositoryUtils.getRepositoryUrl((IArtefact) element));
 					}
 				}
 			}
 		});
 
 		licenseButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_ViewLicense,
 				SWT.PUSH);
 		licenseButton.setEnabled(false);
 		licenseButton.setLayoutData(data);
 		licenseButton.setToolTipText(Messages.RepositoryBrowserEditorPage_OpenLicense);
 		licenseButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				ISelection selection = searchResultTableViewer.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) selection).getFirstElement();
 					if (element instanceof LibraryArtefact) {
 						WebUiUtils.openUrl(RepositoryUtils.getResourceUrl((LibraryArtefact) element,
 								RepositoryUtils.DOWNLOAD_TYPE_LICENSE));
 					} else if (element instanceof BundleArtefact) {
 						WebUiUtils.openUrl(RepositoryUtils.getResourceUrl((BundleArtefact) element,
 								RepositoryUtils.DOWNLOAD_TYPE_LICENSE));
 					}
 				}
 			}
 		});
 
 		// insert vertical space to make the download button stand out
 		toolkit.createLabel(buttonComposite, ""); //$NON-NLS-1$
 
 		downloadButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_Download, SWT.PUSH);
 		downloadButton.setEnabled(false);
 		downloadButton.setLayoutData(data);
 		downloadButton.setToolTipText(Messages.RepositoryBrowserEditorPage_DownloadSelected);
 		downloadButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				Set<Artefact> artifacts = new LinkedHashSet<Artefact>();
 				Object[] selections = searchResultTableViewer.getCheckedElements();
 				for (Object selection : selections) {
 					if (selection instanceof IArtefact) {
 						artifacts.add((Artefact) selection);
 					}
 				}
 
 				boolean showDialog = ServerUiPlugin.getDefault()
 						.getPreferenceStore()
 						.getBoolean(ServerUiPlugin.PLUGIN_ID + ".download.message"); //$NON-NLS-1$
 
 				if (!showDialog) {
 					MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(shell,
 							Messages.RepositoryBrowserEditorPage_DownloadBundlesAndLibraries,
 							Messages.RepositoryBrowserEditorPage_DownloadTriggerMessage,
 							Messages.RepositoryBrowserEditorPage_DontShowDialog, false, ServerUiPlugin.getDefault()
 									.getPreferenceStore(), ServerUiPlugin.PLUGIN_ID + ".download.message"); //$NON-NLS-1$
 					if (dialog.getReturnCode() != Window.OK) {
 						return;
 					} else {
 						ServerUiPlugin.getDefault()
 								.getPreferenceStore()
 								.setValue(ServerUiPlugin.PLUGIN_ID + ".download.message", //$NON-NLS-1$
 										new Boolean(dialog.getToggleState()));
 					}
 				}
 
 				Set<IRuntime> runtimes = new HashSet<IRuntime>();
 				runtimes.add(getServer().getRuntime());
 				RepositoryProvisioningJob operation = new RepositoryProvisioningJob(runtimes,
 						RepositoryUtils.resolveDependencies(artifacts, false), downloadSourcesCheckbox.getSelection());
 				operation.setProperty(IProgressConstants.ICON_PROPERTY, ServerUiImages.DESC_OBJ_BUNDLE);
 				operation.schedule();
 
 				// reset checked state
 				searchResultTableViewer.setCheckedElements(new Object[0]);
 				downloadButton.setEnabled(false);
 			}
 		});
 
 		TableWrapLayout twLayout = new TableWrapLayout();
 		twLayout.bottomMargin = 0;
 		twLayout.topMargin = 0;
 		twLayout.leftMargin = 0;
 		twLayout.rightMargin = 0;
 
 		Composite wrappedComposite = toolkit.createComposite(leftComposite);
 		wrappedComposite.setLayout(twLayout);
 		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(wrappedComposite);
 
 		downloadSourcesCheckbox = toolkit.createButton(wrappedComposite,
 				Messages.RepositoryBrowserEditorPage_DownloadSourceJars, SWT.CHECK | SWT.WRAP);
 		downloadSourcesCheckbox.setSelection(true);
 		downloadSourcesCheckbox.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
 
 		Link repoLink = new Link(wrappedComposite, SWT.WRAP);
 		repoLink.setText(Messages.RepositoryBrowserEditorPage_SourceReposMessage);
 		repoLink.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				WebUiUtils.openUrl(Messages.RepositoryBrowserEditorPage_SourceRepos);
 			}
 		});
 		repoLink.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
 
 		update = new Link(wrappedComposite, SWT.WRAP);
 		update.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (MessageDialog.openQuestion(shell, Messages.RepositoryBrowserEditorPage_UpdateLocalBundles,
 						Messages.RepositoryBrowserEditorPage_ConfirmIndexMessage)) {
 					ServerCorePlugin.getArtefactRepositoryManager().update();
 				}
 
 			}
 		});
 		setRepositoryDateString();
 		update.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
 
 		Hyperlink disclaimer = toolkit.createHyperlink(wrappedComposite,
 				Messages.RepositoryBrowserEditorPage_FirewallConfigureMessage, SWT.WRAP);
 		disclaimer.addHyperlinkListener(new HyperlinkAdapter() {
 			@Override
 			public void linkActivated(HyperlinkEvent e) {
 				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, PROXY_PREF_PAGE_ID, null, null);
 				dialog.open();
 			}
 		});
 		disclaimer.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
 
 		return leftSection;
 	}
 
 	protected void registerContextMenu(StructuredViewer viewer) {
 		MenuManager searchResultManager = new MenuManager();
 		Menu searchResultPopup = searchResultManager.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(searchResultPopup);
 		getSite().registerContextMenu(searchResultManager, viewer);
 	}
 
 	private void setRepositoryDateString() {
 		Date date = ServerCorePlugin.getArtefactRepositoryManager().getArtefactRepositoryDate();
 		String dateString = dateFormat.format(date);
 		update.setText(Messages.RepositoryBrowserEditorPage_UpdateURL + dateString + ")"); //$NON-NLS-1$
 	}
 
 	protected String getServerName() {
 		return Messages.RepositoryBrowserEditorPage_ServerName;
 	}
 
 	private Section createRightSection(FormToolkit toolkit, ScrolledForm form) {
 		GridLayout layout;
 		Section leftSection = toolkit.createSection(form.getBody(), ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
 		leftSection.setText(Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibraries);
 		leftSection.setDescription(Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibrariesMessage
 				+ getServerName() + ".");
 		leftSection.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite leftComposite = toolkit.createComposite(leftSection);
 		layout = new GridLayout();
 		layout.numColumns = 1;
 		leftComposite.setLayout(layout);
 		leftComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		toolkit.paintBordersFor(leftComposite);
 		leftSection.setClient(leftComposite);
 
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 
 		Composite composite2 = toolkit.createComposite(leftComposite);
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 5;
 		composite2.setLayout(layout);
 		composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
 		toolkit.paintBordersFor(composite2);
 
 		repositoryTableViewer = new CommonViewer(ServerUiPlugin.ARTEFACTS_BROWSER_VIEW_ID, composite2, SWT.BORDER
 				| SWT.SINGLE);
 		repositoryTableViewer.setSorter(new RepositoryViewerSorter());
 
 		registerContextMenu(repositoryTableViewer);
 
 		data = new GridData(GridData.FILL_BOTH);
 		data.heightHint = 120;
 		repositoryTableViewer.getControl().setLayoutData(data);
 
 		Composite buttonComposite = new Composite(composite2, SWT.NONE);
 		buttonComposite.setLayout(new GridLayout(1, true));
 		data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
 		buttonComposite.setLayoutData(data);
 
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = 100;
 		refreshButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_Refresh, SWT.PUSH);
 		refreshButton.setLayoutData(data);
 		refreshButton.setToolTipText(Messages.RepositoryBrowserEditorPage_RefreshMessage);
 		refreshButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				refreshBundleRepository();
 			}
 		});
 
 		downloadSourcesButton = toolkit.createButton(buttonComposite,
 				Messages.RepositoryBrowserEditorPage_InstallSources, SWT.PUSH);
 		downloadSourcesButton.setLayoutData(data);
 		downloadSourcesButton.setToolTipText(Messages.RepositoryBrowserEditorPage_InstallSourcesMessage);
 		downloadSourcesButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				downloadSources();
 			}
 		});
 
 		// insert vertical space to make the download button stand out
 		toolkit.createLabel(buttonComposite, Messages.RepositoryBrowserEditorPage_40);
 
 		openManifestButton = toolkit.createButton(buttonComposite, Messages.RepositoryBrowserEditorPage_OpenManifest,
 				SWT.PUSH);
 		openManifestButton.setLayoutData(data);
 		openManifestButton.setToolTipText(Messages.RepositoryBrowserEditorPage_OpenManifestMessage);
 		openManifestButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent selectionEvent) {
 				ISelection selection = repositoryTableViewer.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) selection).getFirstElement();
 					if (element instanceof IPackageFragmentRoot) {
 						IPackageFragmentRoot fragment = (IPackageFragmentRoot) element;
 						BundleManifestEditor.openExternalPlugin(fragment.getPath().toFile(), "META-INF/MANIFEST.MF"); //$NON-NLS-1$
 					}
 				}
 			}
 		});
 
 		openManifestButton.setEnabled(false);
 		repositoryTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 
 			public void selectionChanged(SelectionChangedEvent event) {
 				ISelection selection = event.getSelection();
 				if (selection instanceof IStructuredSelection) {
 					Object element = ((IStructuredSelection) selection).getFirstElement();
 					if (element instanceof IPackageFragmentRoot) {
 						openManifestButton.setEnabled(true);
 					} else {
 						openManifestButton.setEnabled(false);
 					}
 				}
 			}
 		});
 
 		TableWrapLayout twLayout = new TableWrapLayout();
 		twLayout.bottomMargin = 0;
 		twLayout.topMargin = 0;
 		twLayout.leftMargin = 0;
 		twLayout.rightMargin = 0;
 
 		Composite wrappedComposite = toolkit.createComposite(leftComposite);
 		wrappedComposite.setLayout(twLayout);
 		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(wrappedComposite);
 
 		Link repoLink = new Link(wrappedComposite, SWT.WRAP);
 		repoLink.setText(Messages.RepositoryBrowserEditorPage_NewBundlesMessage);
 		repoLink.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				refreshBundleRepository();
 			}
 
 		});
 		repoLink.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
 
 		return leftSection;
 	}
 
 	private void refreshBundleRepository() {
 		IRunnableWithProgress runnable = new IRunnableWithProgress() {
 
 			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				monitor.subTask(Messages.RepositoryBrowserEditorPage_RefreshingBundlesMessage);
 
 				ServerCorePlugin.getArtefactRepositoryManager().refreshBundleRepository(getServer().getRuntime());
 
 				refreshViewers();
 				monitor.done();
 			}
 		};
 		try {
 			IRunnableContext context = new ProgressMonitorDialog(shell);
 			context.run(true, true, runnable);
 		} catch (InvocationTargetException e1) {
 		} catch (InterruptedException e2) {
 		}
 	}
 
 	protected void downloadSources() {
 		Set<Artefact> artifacts = new LinkedHashSet<Artefact>();
 		ArtefactRepository repository = RepositoryUtils.getRepositoryContents(server.getRuntime());
 		for (IArtefactTyped bundle : repository.getBundles()) {
 			if (bundle instanceof LocalBundleArtefact) {
 				if (!((LocalBundleArtefact) bundle).isSourceDownloaded()) {
 					artifacts.add((LocalBundleArtefact) bundle);
 				}
 			}
 		}
 		Set<IRuntime> runtimes = new HashSet<IRuntime>();
 		runtimes.add(getServer().getRuntime());
 		RepositorySourceProvisiongJob operation = new RepositorySourceProvisiongJob(runtimes, artifacts);
 		operation.setProperty(IProgressConstants.ICON_PROPERTY, ServerUiImages.DESC_OBJ_BUNDLE);
 		operation.schedule();
 	}
 
 	private void handleCheckStateChange(final CheckStateChangedEvent event) {
 		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
 			public void run() {
 				boolean state = event.getChecked();
 				setSubtreeChecked(event.getElement(), state, true);
 				updateParentState(event.getElement());
 			}
 		});
 	}
 
 	private void setSubtreeChecked(Object container, boolean state, boolean checkExpandedState) {
 		// checked state is set lazily on expand, don't set it if container is
 		// collapsed
 
 		Object[] members = searchResultContentProvider.getChildren(container);
 		for (int i = members.length - 1; i >= 0; i--) {
 			Object element = members[i];
 			boolean elementGrayChecked = searchResultTableViewer.getGrayed(element)
 					|| searchResultTableViewer.getChecked(element);
 			if (state) {
 				searchResultTableViewer.setChecked(element, true);
 				searchResultTableViewer.setGrayed(element, false);
 			} else {
 				searchResultTableViewer.setGrayChecked(element, false);
 			} // unchecked state only
 				// needs
 			if ((state || elementGrayChecked)) {
 				setSubtreeChecked(element, state, true);
 			}
 		}
 	}
 
 	private void updateParentState(Object child) {
 		if (child == null || searchResultContentProvider.getParent(child) == null) {
 			return;
 		}
 		Object parent = searchResultContentProvider.getParent(child);
 		boolean childChecked = false;
 		Object[] members = searchResultContentProvider.getChildren(parent);
 		for (int i = members.length - 1; i >= 0; i--) {
 			if (searchResultTableViewer.getChecked(members[i]) || searchResultTableViewer.getGrayed(members[i])) {
 				childChecked = true;
 				break;
 			}
 		}
 		searchResultTableViewer.setGrayChecked(parent, childChecked);
 		updateParentState(parent);
 	}
 
 	protected void addChangeListener() {
 		propertyListener = new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 			}
 		};
 
 		serverWC.addConfigurationChangeListener(propertyListener);
 		Job.getJobManager().addJobChangeListener(jobListener);
 	}
 
 	protected void handleSearch() {
 		String search = searchText.getText();
 		searchResultTableViewer.setInput(RepositoryUtils.searchForArtifacts(search));
 		searchResultTableViewer.expandToLevel(2);
 	}
 
 	protected void initialize() {
 		if (searchResultTable == null) {
 			return;
 		}
 		searchResultTable.removeAll();
 		setErrorMessage(null);
 		intializeViewers();
 	}
 
 	protected void intializeViewers() {
 		shell.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				repositoryTableViewer.setInput(getServer());
 				repositoryTableViewer.expandToLevel(2);
 				searchResultTableViewer.refresh(true);
 				searchResultTableViewer.expandToLevel(2);
 			}
 		});
 	}
 
 	protected void refreshViewers() {
 		shell.getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				repositoryTableViewer.refresh();
 				repositoryTableViewer.collapseAll();
 				searchResultTableViewer.refresh(true);
 				searchResultTableViewer.collapseAll();
 			}
 		});
 	}
 
 	private final class ArtefactOperationJobListener extends JobChangeAdapter {
 
 		@Override
 		public void done(IJobChangeEvent event) {
 			if (event.getJob() instanceof RepositoryProvisioningJob) {
 				if (((RepositoryProvisioningJob) event.getJob()).getRuntimes().contains(getServer().getRuntime())) {
 					intializeViewers();
 				}
 			} else if (event.getJob() instanceof ArtefactRepositoryManager.ArtefactRepositoryUpdateJob) {
 				shell.getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						setRepositoryDateString();
 					}
 				});
 			}
 		}
 	}
 
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (adapter == ISelectionChangedListener.class) {
 			return this;
 		}
 		return super.getAdapter(adapter);
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
 		repositoryTableViewer.setSelection(sel, true);
 		repositoryTableViewer.expandToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
 	}
 
	private class ColoredRespositoryLabelProvider extends ServerEditorPageLabelProvider implements IColorProvider {
 
 		public Color getBackground(Object element) {
 			return null;
 		}
 
 		public Color getForeground(Object element) {
 			ArtefactRepository repository = RepositoryUtils.getRepositoryContents(server.getRuntime());
 			if (repository != null && element instanceof Artefact && repository.contains((IArtefact) element)) {
 				return Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
 			}
 			return null;
 		}
 	}
 
 }

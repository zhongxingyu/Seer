 package org.zend.php.zendserver.deployment.ui.editors;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormText;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
 import org.eclipse.ui.part.FileEditorInput;
 import org.zend.php.zendserver.deployment.core.descriptor.DeploymentDescriptorPackage;
 import org.zend.php.zendserver.deployment.core.descriptor.IDeploymentDescriptor;
 import org.zend.php.zendserver.deployment.ui.Activator;
 import org.zend.php.zendserver.deployment.ui.actions.DeployAppInCloudAction;
 import org.zend.php.zendserver.deployment.ui.actions.ExportApplicationAction;
 import org.zend.php.zendserver.deployment.ui.actions.RunApplicationAction;
 import org.zend.php.zendserver.deployment.ui.editors.ScriptsContentProvider.Script;
 import org.zend.sdklib.application.ZendProject;
 
 public class OverviewPage extends DescriptorEditorPage {
 
 	private TextField name;
 	private TextField summary;
 	private TextField description;
 	private TextField releaseVersion;
 	private TextField apiVersion;
 	private TextField license;
 	private TextField icon;
 	private TextField docRoot;
 	private TextField appDir;
 	private TextField scriptsDir;
 
 	private ImageHyperlink runApplicationLink;
 	private ImageHyperlink runInZendCloudLink;
 	private TreeViewer scriptsTree;
 	
 	private ResourceListSection persistent;
 
 	public OverviewPage(DeploymentDescriptorEditor editor) {
 		super(editor, Messages.OverviewPage_0, Messages.OverviewPage_1);
 		IDeploymentDescriptor descr = editor.getModel();
 
 		name = addField(new TextField(descr,
 				DeploymentDescriptorPackage.PKG_NAME, Messages.OverviewPage_2));
 		summary = addField(new TextField(descr,
 				DeploymentDescriptorPackage.SUMMARY, Messages.OverviewPage_3));
 		description = addField(new TextField(descr,
 				DeploymentDescriptorPackage.PKG_DESCRIPTION,
 				Messages.OverviewPage_4));
 		releaseVersion = addField(new TextField(descr,
 				DeploymentDescriptorPackage.VERSION_RELEASE, "Release Version")); //$NON-NLS-1$
 		apiVersion = addField(new TextField(descr,
 				DeploymentDescriptorPackage.VERSION_API, "API Version")); //$NON-NLS-1$
 
 		license = addField(new FileField(descr,
 				DeploymentDescriptorPackage.EULA,
 				"License", editor.getProject())); //$NON-NLS-1$
 		icon = addField(new FileField(descr, DeploymentDescriptorPackage.ICON,
 				Messages.OverviewPage_8, editor.getProject()));
 		docRoot = addField(new FolderField(descr,
 				DeploymentDescriptorPackage.DOCROOT, Messages.OverviewPage_9,
 				editor.getProject()));
 		scriptsDir = addField(new TextField(descr,
 				DeploymentDescriptorPackage.SCRIPTSDIR,
 				Messages.OverviewPage_10));
 		appDir = addField(new TextField(descr,
 				DeploymentDescriptorPackage.APPDIR, Messages.OverviewPage_11));
 	}
 
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 
 		ScrolledForm form = managedForm.getForm();
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
 
 		createGeneralInformationSection(managedForm);
 		createDeploymentScriptsSection(managedForm);
 		createTestingSection(managedForm);
 		createExportingSection(managedForm);
 		createPersistentResourcesSection(managedForm);
 		createActions();
 
 		form.reflow(true);
 	}
 
 	private void createPersistentResourcesSection(IManagedForm managedForm) {
 		persistent = new ResourceListSection(editor, managedForm, "Persistent resources", "Persistent resources to be kept during upgrade.") {
 			
 			@Override
 			public Object[] getElements(Object input) {
 				List<String> list = editor.getModel().getPersistentResources();
 				return editor.getResourceMapper().getResources(list.toArray(new String[list.size()]));
 			}
 			
 			@Override
 			protected void addPath() {
 				Shell shell = editor.getSite().getShell();
 				IProject root = editor.getProject();
 				String[] newPaths = OpenFileDialog.openMany(shell, root, "Add path", "Select path:", null);
 				if (newPaths == null) {
 					return;
 				}
 				
 				for (int i = 0; i < newPaths.length; i++) {
 					editor.getModel().getPersistentResources().add(newPaths[i]);
 				}
 			}
 			
 			@Override
 			protected void editPath(Object element) {
 				String currPath = (String) element;
 				
 				Shell shell = editor.getSite().getShell();
 				IProject root = editor.getProject();
 				String newPath = OpenFileDialog.open(shell, root, "Change path", "Select path:", currPath.toString());
 				if (newPath == null) {
 					return;
 				}
 				editor.getModel().getPersistentResources().remove(currPath);
 				editor.getModel().getPersistentResources().add(newPath);
 			}
 			
 			@Override
 			protected void removePath(Object element) {
 				String path = (String) element;
 				editor.getModel().getPersistentResources().remove(path);
 			}
 		};
 		
 	}
 
 	private void createDeploymentScriptsSection(IManagedForm managedForm) {
 		ScrolledForm form = managedForm.getForm();
 		FormToolkit toolkit = managedForm.getToolkit();
 
 		Section section = toolkit.createSection(form.getBody(),
 				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
 		section.setText(Messages.OverviewPage_12);
 		section.setDescription(Messages.OverviewPage_13);
 		Composite sectionClient = toolkit.createComposite(section);
 		section.setClient(sectionClient);
 		sectionClient.setLayout(new GridLayout(3, false));
 
 		TableWrapData td = new TableWrapData();
 		td.grabHorizontal = true;
 		td.grabVertical = true;
 		td.rowspan = 2;
 		td.heightHint = 350;
 		section.setLayoutData(td);
 
 		scriptsDir.create(sectionClient, toolkit);
 
 		Label label = toolkit.createLabel(sectionClient,
 				Messages.OverviewPage_14);
 		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gd.horizontalSpan = 3;
 		label.setLayoutData(gd);
 
 		scriptsTree = new TreeViewer(sectionClient, SWT.BORDER);
 		Tree tree = scriptsTree.getTree();
 		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.horizontalSpan = 3;
 		tree.setLayoutData(gd);
 
 		ScriptsContentProvider cp = new ScriptsContentProvider();
 		scriptsTree.setContentProvider(cp);
 		scriptsTree.setLabelProvider(new ScriptsLabelProvider(this));
 		scriptsTree.setInput(cp.model);
 		scriptsTree.expandAll();
 		scriptsTree.addDoubleClickListener(new IDoubleClickListener() {
 
 			public void doubleClick(DoubleClickEvent event) {
 				Object element = ((IStructuredSelection) event.getSelection())
 						.getFirstElement();
 				if (element instanceof ScriptsContentProvider.Script) {
 					ScriptsContentProvider.Script script = (Script) element;
 					IFile file = getScript(script.name);
 					if (!file.exists()) {
 						boolean canCreate = MessageDialog.openQuestion(
 								getSite().getShell(), Messages.OverviewPage_15,
 								Messages.OverviewPage_16);
 						if (!canCreate) {
 							return;
 						}
 					}
 					openScript(script.name);
 				}
 			}
 		});
 	}
 
 	private void openScript(final String name) {
 		Job job = new Job(Messages.OverviewPage_17) {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					IFile file = getScript(name);
 					if (!file.exists()) {
 						createScript(name, monitor);
 						refreshScriptsTree();
 					}
 					openEditor(file);
 				} catch (CoreException e) {
 					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 							e.getMessage(), e);
 				}
 				return Status.OK_STATUS;
 			}
 
 		};
 		job.setUser(true);
 		job.schedule();
 	}
 
 	protected void refreshScriptsTree() {
 		scriptsTree.getControl().getDisplay().asyncExec(new Runnable() {
 
 			public void run() {
 				scriptsTree.refresh(); // update created script icon
 			}
 		});
 	}
 
 	private void createScript(String scriptName, IProgressMonitor monitor)
 			throws CoreException {
 		File projLocation = editor.getProject().getLocation().toFile();
 		ZendProject zp = new ZendProject(projLocation);
 		zp.update(scriptName);
 		editor.getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);
 
 	}
 
 	protected void openEditor(final IFile file) throws PartInitException {
 		final IWorkbenchPage page = getSite().getPage();
 		getSite().getShell().getDisplay().asyncExec(new Runnable() {
 
 			public void run() {
 				try {
 					IEditorDescriptor desc = PlatformUI.getWorkbench()
 							.getEditorRegistry()
 							.getDefaultEditor(file.getName());
 					page.openEditor(new FileEditorInput(file), desc.getId());
 				} catch (PartInitException e) {
 					// TODO Log exception
 					e.printStackTrace();
 				}
 			}
 
 		});
 
 	}
 
 	protected IFile getScript(String scriptName) {
 		try {
 			String folder = editor.getDescriptorContainer().getMappingModel().getFolder(scriptName);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// TODO use mapping.getScriptPath(scriptName)
 		return editor.getProject().getFile(scriptName);
 	}
 
 	private void createExportingSection(IManagedForm managedForm) {
 		ScrolledForm form = managedForm.getForm();
 		FormToolkit toolkit = managedForm.getToolkit();
 		Section section = createStaticSection(toolkit, form.getBody(),
 				Messages.OverviewPage_18);
 		Composite container = createStaticSectionClient(toolkit, section);
 		createClient(container, Messages.OverviewPage_20, toolkit,
 				new HyperlinkAdapter() {
 					public void linkActivated(HyperlinkEvent e) {
 						new ExportApplicationAction().run();
 					}
 				});
 		section.setClient(container);
 	}
 
 	/**
 	 * @param toolkit
 	 * @param parent
 	 * @return
 	 */
 	protected Composite createStaticSectionClient(FormToolkit toolkit,
 			Composite parent) {
 		Composite container = toolkit.createComposite(parent, SWT.NONE);
 		container.setLayout(FormLayoutFactory
 				.createSectionClientTableWrapLayout(false, 1));
 		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
 		container.setLayoutData(data);
 		return container;
 	}
 
 	protected final Section createStaticSection(FormToolkit toolkit,
 			Composite parent, String text) {
 		Section section = toolkit.createSection(parent,
 				ExpandableComposite.TITLE_BAR);
 		section.clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
 		section.setText(text);
 		section.setLayout(FormLayoutFactory
 				.createClearTableWrapLayout(false, 1));
 		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
 		section.setLayoutData(data);
 		return section;
 	}
 
 	protected final FormText createClient(Composite section, String content,
 			FormToolkit toolkit, IHyperlinkListener hyperLink) {
 		FormText text = toolkit.createFormText(section, true);
 		try {
 			text.setText(content, true, false);
 		} catch (SWTException e) {
 			text.setText(e.getMessage(), false, false);
 		}
 		text.addHyperlinkListener(hyperLink);
 		return text;
 	}
 
 	private void createTestingSection(IManagedForm managedForm) {
 		ScrolledForm form = managedForm.getForm();
 		FormToolkit toolkit = managedForm.getToolkit();
 
 		Section section = toolkit.createSection(form.getBody(),
 				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
 		section.setText(Messages.OverviewPage_21);
 		section.setDescription(Messages.OverviewPage_22);
 		Composite sectionClient = toolkit.createComposite(section);
 		section.setClient(sectionClient);
 		sectionClient.setLayout(new GridLayout(1, false));
 
 		runApplicationLink = toolkit.createImageHyperlink(sectionClient,
 				SWT.NONE);
 		runApplicationLink.setText(Messages.OverviewPage_23);
 		runApplicationLink.setImage(Activator.getImageDescriptor(
 				Activator.IMAGE_RUN_APPLICATION).createImage());
 
 		runInZendCloudLink = toolkit.createImageHyperlink(sectionClient,
 				SWT.NONE);
 		runInZendCloudLink.setText(Messages.OverviewPage_24);
 		runInZendCloudLink.setImage(Activator.getImageDescriptor(
 				Activator.IMAGE_ZENDCLOUD_APPLICATION).createImage());
 	}
 
 	private void createGeneralInformationSection(IManagedForm managedForm) {
 		ScrolledForm form = managedForm.getForm();
 		FormToolkit toolkit = managedForm.getToolkit();
 
 		Section section = toolkit.createSection(form.getBody(),
 				Section.DESCRIPTION | Section.TITLE_BAR | Section.EXPANDED);
 		section.setText(Messages.OverviewPage_25);
 		section.setDescription(Messages.OverviewPage_26);
 		Composite sectionClient = toolkit.createComposite(section);
 		section.setClient(sectionClient);
 		sectionClient.setLayout(new GridLayout(3, false));
 
 		TableWrapData td = new TableWrapData();
 		td.grabHorizontal = true;
 		section.setLayoutData(td);
 
 		name.create(sectionClient, toolkit);
 		summary.create(sectionClient, toolkit);
 		description.create(sectionClient, toolkit);
 		apiVersion.create(sectionClient, toolkit);
 		releaseVersion.create(sectionClient, toolkit);
 
 		license.create(sectionClient, toolkit);
 
 		icon.create(sectionClient, toolkit);
 
 		docRoot.create(sectionClient, toolkit);
 		appDir.create(sectionClient, toolkit);
 	}
 
 	private void createActions() {
 		runApplicationLink.addHyperlinkListener(new HyperlinkAdapter() {
 			public void linkActivated(HyperlinkEvent e) {
 				new RunApplicationAction().run();
 			}
 		});
 
 		runInZendCloudLink.addHyperlinkListener(new HyperlinkAdapter() {
 			public void linkActivated(HyperlinkEvent e) {
 				new DeployAppInCloudAction().run();
 			}
 		});
 	}
 
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
 		if (active) {
 			refresh();
 		}
 	}
 
 	@Override
 	public void setFocus() {
 		name.setFocus();
 	}
 
 	public void refresh() {
 		name.refresh();
 		summary.refresh();
 		description.refresh();
 		releaseVersion.refresh();
 		apiVersion.refresh();
 		license.refresh();
 		icon.refresh();
 		docRoot.refresh();
 		appDir.refresh();
 		scriptsDir.refresh();
 		persistent.refresh();
 	}
 }

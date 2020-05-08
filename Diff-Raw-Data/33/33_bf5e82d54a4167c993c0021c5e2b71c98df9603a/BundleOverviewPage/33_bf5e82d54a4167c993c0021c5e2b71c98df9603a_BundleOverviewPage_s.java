 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.ui.editors;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.pde.core.plugin.IPluginModelBase;
 import org.eclipse.pde.internal.ui.PDELabelProvider;
 import org.eclipse.pde.internal.ui.PDEPlugin;
 import org.eclipse.pde.internal.ui.PDEPluginImages;
 import org.eclipse.pde.internal.ui.PDEUIMessages;
 import org.eclipse.pde.internal.ui.editor.FormLayoutFactory;
 import org.eclipse.pde.internal.ui.editor.PDEFormPage;
 import org.eclipse.pde.internal.ui.util.SharedLabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormText;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.virgo.ide.bundlor.internal.core.BundlorCorePlugin;
 import org.eclipse.virgo.ide.bundlor.ui.BundlorUiPlugin;
 import org.eclipse.virgo.ide.export.BundleExportWizard;
 import org.eclipse.virgo.ide.jdt.internal.core.classpath.ServerClasspathContainerUpdateJob;
 import org.eclipse.virgo.ide.manifest.internal.core.BundleManifestManager;
 import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
 import org.eclipse.virgo.ide.ui.StatusHandler;
 
 /**
  * @author Christian Dupuis
  */
 public class BundleOverviewPage extends PDEFormPage implements IHyperlinkListener, IBundleManifestSaveListener {
 
 	public static final String PAGE_ID = "bundle_overview"; //$NON-NLS-1$
 
 	private BundleGeneralInfoSection fInfoSection;
 
 	private static final String BUNDLE_CONTENT_SECTION_TEXT = "<form><p>The content of the bundle is made up of two sections:</p><li style=\"image\" value=\"page\" bindent=\"5\"><a href=\"dependencies\">Dependencies</a>: lists all the bundles required on this bundle's classpath to compile and run.</li><li style=\"image\" value=\"page\" bindent=\"5\"><a href=\"runtime\">Runtime</a>: lists the packages that this bundle exports to other bundles.</li></form>";
 
 	private static final String BUNDLE_ACTION_SECTION_TEXT = "<form><p>Perform common actions on the bundle:</p>"
 			+ "<li style=\"image\" value=\"dependencies\" bindent=\"5\"><a href=\"refreshdependencies\">Refresh Bundle Dependencies</a>: refresh the Bundle Classpath Container to reflect changes in the MANIFEST.MF file.</li>"
 			+ "<li style=\"image\" value=\"export\" bindent=\"5\"><a href=\"exportbundle\">Export Bundle</a>: export the contents of the Bundle to a deployable JAR.</li>"
 			+ "</form>";
 
 	private static final String MANIFEST_ERRORS = "Overview: Please correct one or more errors in the manifest";
 
 	protected ScrolledForm form = null;
 
 	protected IResource resource = null;
 
 	public BundleOverviewPage(FormEditor editor) {
 		super(editor, PAGE_ID, "Overview");
 	}
 
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 		IPluginModelBase model = (IPluginModelBase) ((BundleManifestEditor) this.getEditor()).getAggregateModel();
 		resource = model.getUnderlyingResource();
 		form = managedForm.getForm();
 		FormToolkit toolkit = managedForm.getToolkit();
 		form.setImage(ServerIdeUiPlugin.getImage("full/obj16/osgi_obj.gif"));
 		form.setText(PDEUIMessages.ManifestEditor_OverviewPage_title);
 		fillBody(managedForm, toolkit);
 		updateFormText();
 	}
 
 	public void manifestSaved() {
 		if (resource != null) {
 			updateFormText();
 		}
 	}
 
 	protected void updateFormText() {
 		if (resource != null) {
 			try {
 				// Wait for build
 				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
 				IMarker[] markers = resource.findMarkers(null, true, IResource.DEPTH_ZERO);
 				if (ManifestEditorUtils.hasErrorSeverityMarker(markers)) {
 					form.setText(MANIFEST_ERRORS);
 					form.setImage(ServerIdeUiPlugin.getImage("full/obj16/manifest_error.png"));
 				} else {
 					form.setText(PDEUIMessages.ManifestEditor_OverviewPage_title);
 					form.setImage(ServerIdeUiPlugin.getImage("full/obj16/osgi_obj.gif"));
 				}
 			} catch (OperationCanceledException e) {
				StatusHandler.log(new Status(Status.ERROR, ServerIdeUiPlugin.PLUGIN_ID,
 						"Could not update page title text", e));
 			} catch (InterruptedException e) {
				StatusHandler.log(new Status(Status.ERROR, ServerIdeUiPlugin.PLUGIN_ID,
 						"Could not update page title text", e));
 			} catch (CoreException e) {
				StatusHandler.log(new Status(Status.ERROR, ServerIdeUiPlugin.PLUGIN_ID,
 						"Could not update page title text", e));
 			}
 		}
 	}
 
 	private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {
 		Composite body = managedForm.getForm().getBody();
 		body.setLayout(FormLayoutFactory.createFormTableWrapLayout(true, 2));
 
 		Composite left = toolkit.createComposite(body);
 		left.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
 		left.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 		fInfoSection = new BundleGeneralInfoSection(this, left);
 		managedForm.addPart(fInfoSection);
 		managedForm.addPart(new BundleExecutionEnvironmentSection(this, left));
 
 		Composite right = toolkit.createComposite(body);
 		right.setLayout(FormLayoutFactory.createFormPaneTableWrapLayout(false, 1));
 		right.setLayoutData(new TableWrapData(TableWrapData.FILL));
 		createBundleActionSection(managedForm, right, toolkit);
 		createBundleContentSection(managedForm, right, toolkit);
 
 	}
 
 	private void createBundleContentSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
 		String sectionTitle;
 		sectionTitle = "Bundle Content";
 		Section section = createStaticSection(toolkit, parent, sectionTitle);
 
 		Composite container = createStaticSectionClient(toolkit, section);
 
 		FormText text = createClient(container, BUNDLE_CONTENT_SECTION_TEXT, true, toolkit);
 		PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
 		text.setImage("page", lp.get(PDEPluginImages.DESC_PAGE_OBJ, SharedLabelProvider.F_EDIT)); //$NON-NLS-1$
 		text.addHyperlinkListener(this);
 		section.setClient(container);
 	}
 
 	private void createBundleActionSection(IManagedForm managedForm, Composite parent, FormToolkit toolkit) {
 		String sectionTitle;
 		sectionTitle = "Bundle Actions";
 		Section section = createStaticSection(toolkit, parent, sectionTitle);
 
 		Composite container = createStaticSectionClient(toolkit, section);
 
 		FormText noteText = createClient(
 				container,
 				"<form><p>OSGi dependency meta data in the MANIFEST.MF file can automatically be updated based on dependencies expressed in source code artifacts.</p><p>Java source files, Spring XML configuration, JPA persistence.xml and Hibernate .hbm mapping files will be analysed. The process will create Import-Package and Export-Package headers.</p><li style=\"image\" value=\"manifest\" bindent=\"5\"><a href=\"generate\">Update MANIFEST.MF</a>: automatically generate MANIFEST.MF file based on dependencies in source code artifacts.</li></form>",
 				true, toolkit);
 		noteText.setImage("manifest", ServerIdeUiPlugin.getImage("full/obj16/osgi_obj.gif")); //$NON-NLS-1$
 		noteText.addHyperlinkListener(this);
 
 		Button button = toolkit.createButton(container, "Automatically update MANIFEST.MF file in background.",
 				SWT.CHECK);
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IRunnableWithProgress op = new WorkspaceModifyOperation() {
 					protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {
 						IProject project = resource.getProject();
 						IProjectDescription description = project.getDescription();

 						try {
 							List<ICommand> cmds = Arrays.asList(description.getBuildSpec());
 							List<ICommand> newCmds = new ArrayList<ICommand>(cmds);
							for (ICommand config : cmds) {
								if (config.getBuilderName().equals(BundlorCorePlugin.BUILDER_ID)) {
									if (BundlorUiPlugin.isBundlorBuilding(project)) {
 										newCmds.remove(config);
									} else {
										ICommand command = project.getDescription().newCommand();
										command.setBuilderName(BundlorCorePlugin.BUILDER_ID);
										newCmds.add(config);
 									}
 								}
 							}
 							if (!cmds.equals(newCmds)) {
 								description.setBuildSpec(newCmds.toArray(new ICommand[] {}));
 								project.setDescription(description, monitor);
 							}
 						} catch (CoreException e1) {
 						}
 					}
 				};
 				try {
 					PlatformUI.getWorkbench()
 							.getProgressService()
 							.runInUI(PDEPlugin.getActiveWorkbenchWindow(), op, PDEPlugin.getWorkspace().getRoot());
 				} catch (InvocationTargetException e1) {
 				} catch (InterruptedException e1) {
 				}
 
 			}
 		});
 		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
 		data.indent = 5;
 		button.setLayoutData(data);
 		boolean building = resource != null && BundlorUiPlugin.isBundlorBuilding(resource.getProject());
 		button.setSelection(building);
 
 		toolkit.createLabel(container, "");
 
 		FormText text = createClient(container, BUNDLE_ACTION_SECTION_TEXT, true, toolkit);
 		PDELabelProvider lp = PDEPlugin.getDefault().getLabelProvider();
 		text.setImage("page", lp.get(PDEPluginImages.DESC_PAGE_OBJ, SharedLabelProvider.F_EDIT)); //$NON-NLS-1$
 		text.setImage(
 				"dependencies", JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE_WITH_SOURCE)); //$NON-NLS-1$
 		//TODO Replace these with appropriate images as needed. MTP
 		//text.setImage("export", BeansGraphImages.getImage(BeansGraphImages.IMG_OBJS_EXPORT_ENABLED));
 		text.addHyperlinkListener(this);
 
 		section.setClient(container);
 	}
 
 	protected final Section createStaticSection(FormToolkit toolkit, Composite parent, String text) {
 		Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
 		section.clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
 		section.setText(text);
 		section.setLayout(FormLayoutFactory.createClearTableWrapLayout(false, 1));
 		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
 		section.setLayoutData(data);
 		return section;
 	}
 
 	protected Composite createStaticSectionClient(FormToolkit toolkit, Composite parent) {
 		Composite container = toolkit.createComposite(parent, SWT.NONE);
 		container.setLayout(FormLayoutFactory.createSectionClientTableWrapLayout(false, 1));
 		TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
 		container.setLayoutData(data);
 		return container;
 	}
 
 	protected final FormText createClient(Composite section, String content, boolean parseTags, FormToolkit toolkit) {
 		FormText text = toolkit.createFormText(section, true);
 		try {
 			text.setText(content, parseTags, false);
 		} catch (SWTException e) {
 			text.setText(e.getMessage(), false, false);
 		}
 		return text;
 	}
 
 	public void linkActivated(HyperlinkEvent e) {
 		if (e.getHref().equals("dependencies")) {
 			getEditor().setActivePage(BundleDependenciesPage.PAGE_ID);
 		} else if (e.getHref().equals("runtime")) {
 			getEditor().setActivePage(BundleRuntimePage.PAGE_ID);
 		} else if (e.getHref().equals("refreshdependencies")) {
 			IRunnableWithProgress op = new WorkspaceModifyOperation() {
 				protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException {
 					ServerClasspathContainerUpdateJob.scheduleClasspathContainerUpdateJob(
 							JavaCore.create(resource.getProject()), BundleManifestManager.IMPORTS_CHANGED);
 				}
 			};
 			try {
 				PlatformUI.getWorkbench()
 						.getProgressService()
 						.runInUI(PDEPlugin.getActiveWorkbenchWindow(), op, PDEPlugin.getWorkspace().getRoot());
 			} catch (InvocationTargetException e1) {
 			} catch (InterruptedException e1) {
 			}
 		} else if (e.getHref().equals("generate")) {
 			BundlorUiPlugin.runBundlorOnProject(JavaCore.create(resource.getProject()));
 		} else if (e.getHref().equals("exportbundle")) {
 			Display.getDefault().asyncExec(new Runnable() {
 
 				public void run() {
 					BundleExportWizard wizard = new BundleExportWizard();
 					WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
 					wizard.init(PlatformUI.getWorkbench(),
 							new StructuredSelection(new Object[] { JavaCore.create(resource.getProject()) }));
 					dialog.open();
 				}
 			});
 		}
 	}
 
 	public void linkEntered(HyperlinkEvent e) {
 		// Nothing to do
 	}
 
 	public void linkExited(HyperlinkEvent e) {
 		// Nothing to do
 	}
 
 	/** For JUnit testing only * */
 	public BundleGeneralInfoSection getBundleGeneralInfoSection() {
 		return this.fInfoSection;
 	}
 
 }

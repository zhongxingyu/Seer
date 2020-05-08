 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.aggregator.presentation;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.b3.aggregator.AggregatorFactory;
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.transformer.TransformationManager;
 import org.eclipse.b3.aggregator.transformer.TransformerContextContributor;
 import org.eclipse.b3.aggregator.transformer.ui.TransformerContributorWizardPage;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 
 /**
  * This is a simple wizard for creating a new model file.
  * <!-- begin-user-doc --> <!-- end-user-doc -->
  * 
  * @author Karel Brezina
  */
 public class TransformationWizard extends Wizard implements INewWizard {
 
 	public class InfoPage extends WizardPage {
 
 		private boolean srcNamespaceFound;
 
 		private boolean transformationSequenceExists;
 
 		public InfoPage(String pageId, boolean srcNamespaceFound, boolean transformationSequenceExists) {
 			super(pageId);
 			this.srcNamespaceFound = srcNamespaceFound;
 			this.transformationSequenceExists = transformationSequenceExists;
 
 			setPageComplete(srcNamespaceFound && transformationSequenceExists);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 		 */
 		public void createControl(Composite parent) {
 			Composite composite = new Composite(parent, SWT.NONE);
 			composite.setLayout(new GridLayout(2, false));
 
 			Label label = new Label(composite, SWT.None);
 
 			if(!srcNamespaceFound || !transformationSequenceExists) {
 				label.setText("Transformation from the selected resource model was not defined - transformation is not possible");
 				label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
 			}
 			else
 				label.setText("This wizard will take you through Aggregation Model Transformation");
 
 			setControl(composite);
 		}
 	}
 
 	/**
 	 * This is the one page of the wizard.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public class NewFileCreationPage extends WizardNewFileCreationPage {
 		/**
 		 * Pass in the selection.
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 */
 		public NewFileCreationPage(String pageId, IStructuredSelection selection) {
 			super(pageId, selection);
 		}
 
 		/**
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 */
 		public IFile getModelFile() {
 			return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
 		}
 
 		/**
 		 * The framework calls this to see if the file is correct.
 		 * <!-- begin-user-doc --> <!-- end-user-doc -->
 		 */
 		@Override
 		protected boolean validatePage() {
 			if(super.validatePage()) {
 				String extension = new Path(getFileName()).getFileExtension();
 				if(extension == null || !FILE_EXTENSIONS.contains(extension)) {
 					String key = FILE_EXTENSIONS.size() > 1
 							? "_WARN_FilenameExtensions"
 							: "_WARN_FilenameExtension";
 					setErrorMessage(AggregatorEditorPlugin.INSTANCE.getString(key,
 							new Object[] { FORMATTED_FILE_EXTENSIONS }));
 					return false;
 				}
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private static final String LEGACY_TRANSFORMATION_UI_ID = "org.eclipse.b3.aggregator.editor.legacy_transformation_ui";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_NAME = "name";
 
 	private static final String LEGACY_TRANSFORMATION_ATTR_CLASS = "class";
 
 	/**
 	 * The supported extensions for created files.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public static final List<String> FILE_EXTENSIONS = Collections.unmodifiableList(Arrays.asList(AggregatorEditorPlugin.INSTANCE.getString(
 			"_UI_AggregatorEditorFilenameExtensions").split("\\s*,\\s*")));
 
 	/**
 	 * A formatted list of supported file extensions, suitable for display. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 */
 	public static final String FORMATTED_FILE_EXTENSIONS = AggregatorEditorPlugin.INSTANCE.getString(
 			"_UI_AggregatorEditorFilenameExtensions").replaceAll("\\s*,\\s*", ", ");
 
 	/**
 	 * This caches an instance of the model package.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected AggregatorPackage aggregatorPackage = AggregatorPackage.eINSTANCE;
 
 	/**
 	 * This caches an instance of the model factory.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected AggregatorFactory aggregatorFactory = aggregatorPackage.getAggregatorFactory();
 
 	protected InfoPage infoPage;
 
 	protected List<IWizardPage> contextContributorWizardPages = new ArrayList<IWizardPage>();
 
 	/**
 	 * This is the file creation page.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected NewFileCreationPage newFileCreationPage;
 
 	protected URI srcResourceURI;
 
 	/**
 	 * Remember the selection during initialization for populating the default container.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 */
 	protected IStructuredSelection selection;
 
 	/**
 	 * Remember the workbench during initialization.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	protected IWorkbench workbench;
 
 	/**
 	 * Caches the names of the types that can be created as the root object. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 */
 	protected List<String> initialObjectNames;
 
 	private IFile modelFile;
 
 	private Resource finalResource;
 
 	private TransformationManager manager;
 
 	public TransformationWizard(URI srcResourceURI) {
 		this.srcResourceURI = srcResourceURI;
 		final IConfigurationElement[] transformationsUI = Platform.getExtensionRegistry().getConfigurationElementsFor(
 				LEGACY_TRANSFORMATION_UI_ID);
 
 		TransformationManager.ContributorListener listener = new TransformationManager.ContributorListener() {
 
 			public void contributorFound(IConfigurationElement config, TransformerContextContributor contributor)
 					throws CoreException {
 				for(IConfigurationElement transformationUI : transformationsUI) {
 					if(transformationUI.getAttribute(LEGACY_TRANSFORMATION_ATTR_NAME).equals(
 							config.getAttribute(LEGACY_TRANSFORMATION_ATTR_NAME))) {
 						TransformerContributorWizardPage wizPage = (TransformerContributorWizardPage) transformationUI.createExecutableExtension(LEGACY_TRANSFORMATION_ATTR_CLASS);
 						wizPage.setContextContributor(contributor);
 						contextContributorWizardPages.add(wizPage);
 					}
 				}
 			}
 
 		};
 
 		manager = new TransformationManager(srcResourceURI, listener);
 	}
 
 	/**
 	 * The framework calls this to create the contents of the wizard. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	@Override
 	public void addPages() {
 		infoPage = new InfoPage("InfoPage", manager.isSrcNamespaceFound(), manager.getTransformationSequence() != null
 				&& !manager.getTransformationSequence().isEmpty());
 		infoPage.setTitle("Depricated Resource");
 		infoPage.setDescription("Selected resource needs to be transformed to the up-to-date structure");
 		addPage(infoPage);
 
 		for(IWizardPage contextContributorWizardPage : contextContributorWizardPages) {
 			addPage(contextContributorWizardPage);
 		}
 
 		newFileCreationPage = new NewFileCreationPage("FileCreationPage", selection);
 		newFileCreationPage.setTitle("Target Location");
 		newFileCreationPage.setDescription("Provide location for the transformed model");
 		newFileCreationPage.setFileName(srcResourceURI.lastSegment().replaceAll("\\.[^.]*$", "") + "."
 				+ FILE_EXTENSIONS.get(0));
		newFileCreationPage.setContainerFullPath(Path.fromOSString(srcResourceURI.toPlatformString(true)).makeAbsolute().removeLastSegments(
				1));
 		addPage(newFileCreationPage);
 	}
 
 	public IFile getModelFile() {
 		return modelFile;
 	}
 
 	public Resource getTargetResource() {
 		return finalResource;
 	}
 
 	/**
 	 * This just records the information. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.workbench = workbench;
 		this.selection = selection;
 		setWindowTitle("Aggregator Model Transformation");
 		setDefaultPageImageDescriptor(ExtendedImageRegistry.INSTANCE.getImageDescriptor(AggregatorEditorPlugin.INSTANCE.getImage("full/wizban/NewAggregator.png")));
 	}
 
 	@Override
 	public boolean performFinish() {
 
 		// Do the work within an operation.
 		//
 		IRunnableWithProgress operation = new IRunnableWithProgress() {
 
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				try {
 					monitor.beginTask("Transformation is in progress", IProgressMonitor.UNKNOWN);
 
 					finalResource = manager.transformResource();
 
 					finalResource.setURI(URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true));
 					finalResource.save(null);
 				}
 				catch(Exception e) {
 					throw new InvocationTargetException(e);
 				}
 				finally {
 					monitor.done();
 				}
 			}
 
 		};
 
 		setNeedsProgressMonitor(true);
 
 		modelFile = newFileCreationPage.getModelFile();
 
 		try {
 			getContainer().run(true, false, operation);
 		}
 		catch(InvocationTargetException e) {
 			((WizardPage) getContainer().getCurrentPage()).setErrorMessage("Transformation error: "
 					+ e.getCause().getMessage());
 			return false;
 		}
 		catch(InterruptedException e) {
 			// not cancelable
 		}
 
 		return true;
 	}
 }

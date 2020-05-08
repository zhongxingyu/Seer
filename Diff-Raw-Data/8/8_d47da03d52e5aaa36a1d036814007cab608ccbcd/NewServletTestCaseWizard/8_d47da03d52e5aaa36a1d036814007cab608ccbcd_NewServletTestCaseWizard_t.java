 /*******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc. and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Daniel R. Somerfield - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.ui.internal.cactus;
 
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageOne;
 import org.eclipse.jdt.junit.wizards.NewTestCaseWizardPageTwo;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jst.server.ui.internal.ImageResource;
 import org.eclipse.jst.server.ui.internal.JavaServerUIPlugin;
 import org.eclipse.jst.server.ui.internal.Messages;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 /**
  *
  */
 public class NewServletTestCaseWizard extends Wizard implements INewWizard {
 	private static final String SUPERCLASS_NAME = "org.apache.cactus.ServletTestCase"; //$NON-NLS-1$
 
 	protected static final String[] CLASSES_TO_CHECK = {
 			"org.apache.cactus.ServletTestCase", "junit.framework.TestCase", //$NON-NLS-1$ //$NON-NLS-2$
 			"org.apache.commons.logging.Log", "org.aspectj.lang.JoinPoint", //$NON-NLS-1$ //$NON-NLS-2$
 			"org.apache.commons.httpclient.HttpClient" }; //$NON-NLS-1$
 
 	protected static final String[] REQUIRED_LIBRARIES = {
 			"cactus-1.7.2.jar", "junit-3.8.1.jar", "aspectjrt-1.2.1.jar", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			"commons-logging-1.0.4.jar", "commons-httpclient-2.0.2.jar" }; //$NON-NLS-1$ //$NON-NLS-2$
 
 	private IWorkbench fWorkbench;
 
 	private IStructuredSelection fSelection;
 
 	private NewTestCaseWizardPageTwo fPage2;
 
 	private NewTestCaseWizardPageOne fPage1;
 
 	public NewServletTestCaseWizard() {
 		super();
 		setWindowTitle(Messages.NewServletTestCaseWizard_WindowTitle);
 		setDefaultPageImageDescriptor(ImageResource
 				.getImageDescriptor(ImageResource.IMG_WIZ_CACTUS_TEST));
 	}
 
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		fWorkbench = workbench;
 		fSelection = selection;
 	}
 
 	public void addPages() {
 		super.addPages();
 		fPage2 = new NewTestCaseWizardPageTwo();
 		fPage1 = new CactusPage1(fPage2);
 		addPage(fPage1);
 		fPage1.init(getSelection());
 		addPage(fPage2);
 	}
 
 	private class CactusPage1 extends NewTestCaseWizardPageOne {
 		private Button fBeginButton;
 
 		private Button fEndButton;
 
 		private Button fBeginXXXButton;
 
 		private Button fEndXXXButton;
 
 		private Composite fCactusButtonComposite;
 
 		public CactusPage1(NewTestCaseWizardPageTwo page2) {
 			super(page2);
 		}
 
 		public void init(IStructuredSelection selection) {
 			super.init(selection);
			validateIfJUnitProject();
 		}
 
 		public void createControl(Composite parent) {
 			super.createControl(parent);
 			setSuperClass(SUPERCLASS_NAME, true);
 		}
 
		protected void createJUnit4Controls(Composite composite, int nColumns) {
			// ignore
		}

 		protected IStatus superClassChanged() {
 			String superClassName = getSuperClass();
 			if (superClassName == null || superClassName.trim().equals("")) { //$NON-NLS-1$
 				Status status = new Status(IStatus.ERROR, JavaServerUIPlugin.PLUGIN_ID,
 						IStatus.ERROR,
 						Messages.NewServletTestCaseWizard_WarningMessageSuperclassIsEmpty, null);
 				return status;
 			}
 
 			if (superClassName.equals("org.apache.cactus.ServletTestCase")) {
 				//Short-circuit if this IS ServletTestCase
 				return Status.OK_STATUS;
 			}
 
 			if (getPackageFragmentRoot() != null) {
 				IType type;
 				try {
 					type = resolveClassNameToType(getPackageFragmentRoot().getJavaProject(),
 							getPackageFragment(), superClassName);
 					if (type == null) {
 						Status status = new Status(
 								IStatus.WARNING,
 								JavaServerUIPlugin.PLUGIN_ID,
 								IStatus.WARNING,
 								Messages.NewServletTestCaseWizard_WarningMessageSuperclassDoesNotExist,
 								null);
 						return status;
 					}
 
 					if (type.isInterface()) {
 						Status status = new Status(
 								IStatus.WARNING,
 								JavaServerUIPlugin.PLUGIN_ID,
 								IStatus.WARNING,
 								Messages.NewServletTestCaseWizard_WarningMessageSuperclassIsInterface,
 								null);
 						return status;
 					}
 
 					ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
 					IType[] superTypes = hierarchy.getAllSuperclasses(type);
 					for (int i = 0; i < superTypes.length; i++) {
 						if (superTypes[i].getFullyQualifiedName().equals(
 								"org.apache.cactus.ServletTestCase")) {
 							return Status.OK_STATUS;
 						}
 					}
 
 					Status status = new Status(
 							IStatus.WARNING,
 							JavaServerUIPlugin.PLUGIN_ID,
 							IStatus.WARNING,
 							Messages.NewServletTestCaseWizard_WarningMessageSuperclassNotServletTestCase,
 							null);
 					return status;
 
 				} catch (JavaModelException e) {
 					JavaServerUIPlugin.log(e);
 				}
 			}
 
 			return Status.OK_STATUS;
 		}
 
 		private IType resolveClassNameToType(IJavaProject jproject, IPackageFragment pack,
 				String classToTestName) throws JavaModelException {
 
 			IType type = jproject.findType(classToTestName);
 
 			// search in current package
 			if (type == null && pack != null && !pack.isDefaultPackage()) {
 				type = jproject.findType(pack.getElementName(), classToTestName);
 			}
 
 			// search in java.lang
 			if (type == null) {
 				type = jproject.findType("java.lang", classToTestName); //$NON-NLS-1$
 			}
 			return type;
 		}
 
 		protected IStatus validateIfJUnitProject() {
 			try {
 				if (checkForWebProject()) {
 					checkForCactusLibraries();
 				} else {
 					String msg = Messages.NewServletTestCaseWizard_WarningMessageSelectAWebProject;
 					MessageDialog.openWarning(getShell(),
 							Messages.NewServletTestCaseWizard_WarningTitleWebProjectRequired,
 							msg);
 					return new Status(IStatus.WARNING, JavaServerUIPlugin.PLUGIN_ID,
 							IStatus.ERROR, msg, null);
 				}
 			} catch (CoreException e) {
 				return new Status(IStatus.ERROR, JavaServerUIPlugin.PLUGIN_ID, IStatus.ERROR,
 						"Failed to check for cactus libraries.", e);
 			}
 
 			return Status.OK_STATUS;
 		}
 
 		protected void createTypeMembers(IType type, ImportsManager imports,
 				IProgressMonitor monitor) throws CoreException {
 			super.createTypeMembers(type, imports, monitor);
 			if (fBeginButton.getSelection()) {
 				generateBeginMethod(type, imports);
 			}
 
 			if (fEndButton.getSelection()) {
 				generateEndMethod(type, imports);
 			}
 
 			if (fBeginXXXButton.getSelection()) {
 				generateBeginXXXMethods(type, imports);
 			}
 
 			if (fEndXXXButton.getSelection()) {
 				generateEndXXXMethods(type, imports);
 			}
 		}
 
 		private void generateBeginMethod(IType type, ImportsManager imports)
 				throws JavaModelException {
 			StringBuffer methodBuffer = new StringBuffer();
 			methodBuffer.append("public void begin("); //$NON-NLS-1$
 			methodBuffer.append(imports.addImport("org.apache.cactus.WebRequest")); //$NON-NLS-1$
 			methodBuffer.append(" request) throws "); //$NON-NLS-1$
 			methodBuffer.append(imports.addImport("java.lang.Exception")); //$NON-NLS-1$
 			methodBuffer.append(" {\n\n\t} "); //$NON-NLS-1$
 			type.createMethod(methodBuffer.toString(), null, false, null);
 		}
 
 		private void generateEndMethod(IType type, ImportsManager imports)
 				throws JavaModelException {
 			StringBuffer methodBuffer = new StringBuffer();
 			methodBuffer.append("public void end("); //$NON-NLS-1$
 			methodBuffer.append(imports.addImport("org.apache.cactus.WebRequest")); //$NON-NLS-1$
 			methodBuffer.append(" request) throws "); //$NON-NLS-1$
 			methodBuffer.append(imports.addImport("java.lang.Exception")); //$NON-NLS-1$
 			methodBuffer.append(" {\n\n\t} "); //$NON-NLS-1$
 			type.createMethod(methodBuffer.toString(), null, false, null);
 		}
 
 		private void generateBeginXXXMethods(IType type, ImportsManager imports)
 				throws JavaModelException {
 			generateXXXMethods(type, imports, "begin"); //$NON-NLS-1$
 		}
 
 		private void generateXXXMethods(IType type, ImportsManager imports, String prefix)
 				throws JavaModelException {
 			IMethod[] testMethods = type.getMethods();
 			for (int i = 0; i < testMethods.length; i++) {
 				IMethod testMethod = testMethods[i];
 				String testMethodName = testMethod.getElementName();
 				if (testMethodName.startsWith("test")) //$NON-NLS-1$
 				{
 					String newMethodName = prefix + testMethodName.substring(4);
 					StringBuffer methodBuffer = new StringBuffer();
 					methodBuffer.append("public void "); //$NON-NLS-1$
 					methodBuffer.append(newMethodName);
 					methodBuffer.append("("); //$NON-NLS-1$
 					methodBuffer.append(imports.addImport("org.apache.cactus.WebRequest")); //$NON-NLS-1$
 					methodBuffer.append(" request) throws "); //$NON-NLS-1$
 					methodBuffer.append(imports.addImport("java.lang.Exception")); //$NON-NLS-1$
 					methodBuffer.append(" {\n\n\t} "); //$NON-NLS-1$
 					type.createMethod(methodBuffer.toString(), testMethod, false, null);
 				}
 			}
 		}
 
 		private void generateEndXXXMethods(IType type, ImportsManager imports)
 				throws JavaModelException {
 			generateXXXMethods(type, imports, "end"); //$NON-NLS-1$
 		}
 
 		protected void createMethodStubSelectionControls(Composite composite, int nColumns) {
 			super.createMethodStubSelectionControls(composite, nColumns);
 			createEmptySpace(composite);
 			fCactusButtonComposite = new Composite(composite, SWT.NONE);
 			GridData gd = new GridData();
 			gd.horizontalSpan = 3;
 			fCactusButtonComposite.setLayoutData(gd);
 			GridLayout layout = new GridLayout();
 			layout.numColumns = 1;
 			layout.marginWidth = layout.marginHeight = 0;
 			layout.makeColumnsEqualWidth = true;
 			fCactusButtonComposite.setLayout(layout);
 
 			fBeginButton = createSelectionButton("begin()", fCactusButtonComposite); //$NON-NLS-1$
 			fEndButton = createSelectionButton("end()", fCactusButtonComposite); //$NON-NLS-1$
 			fBeginXXXButton = createSelectionButton(
 					"beginXXX() methods", fCactusButtonComposite); //$NON-NLS-1$
 			fEndXXXButton = createSelectionButton("endXXX() methods", fCactusButtonComposite); //$NON-NLS-1$
 		}
 
 		private Button createSelectionButton(String text, Composite group) {
 			Button button = new Button(group, SWT.CHECK | SWT.LEFT);
 			button.setFont(group.getFont());
 			button.setText(text);
 			GridData data = new GridData();
 			data.horizontalAlignment = SWT.BEGINNING;
 			data.verticalAlignment = GridData.CENTER;
 			button.setLayoutData(data);
 			return button;
 		}
 	}
 
 	protected static Control createEmptySpace(Composite parent) {
 		return createEmptySpace(parent, 1);
 	}
 
 	private static Control createEmptySpace(Composite parent, int span) {
 		Label label = new Label(parent, SWT.LEFT);
 		GridData gd = new GridData();
 		gd.horizontalAlignment = GridData.BEGINNING;
 		gd.grabExcessHorizontalSpace = false;
 		gd.horizontalSpan = span;
 		gd.horizontalIndent = 0;
 		gd.widthHint = 0;
 		gd.heightHint = 0;
 		label.setLayoutData(gd);
 		return label;
 	}
 
 	private IStructuredSelection getSelection() {
 		return fSelection;
 	}
 
 	protected boolean checkForWebProject() throws CoreException {
 		IPackageFragmentRoot root = fPage1.getPackageFragmentRoot();
 		if (root == null)
 			return false;
 
 		IJavaProject project = root.getJavaProject();
 		IFacetedProject facetedProject = ProjectFacetsManager.create(project.getProject());
 		if (facetedProject != null) {
 			Iterator facets = facetedProject.getProjectFacets().iterator();
 			while (facets.hasNext()) {
 				IProjectFacetVersion facet = (IProjectFacetVersion) facets.next();
 				if (facet.getProjectFacet().getId().equals("jst.web")) //$NON-NLS-1$
 					return true;
 			}
 		}
 
 		return false;
 	}
 
 	protected void checkForCactusLibraries() {
 		IPackageFragmentRoot root = fPage1.getPackageFragmentRoot();
 		if (root == null)
 			return;
 
 		IJavaProject project = root.getJavaProject();
 
 		List missingLibraries = new ArrayList();
 		try {
 			for (int i = 0; i < CLASSES_TO_CHECK.length; i++) {
 				IType type = project.findType(CLASSES_TO_CHECK[i]);
 				if (type == null) {
 					missingLibraries.add(REQUIRED_LIBRARIES[i]);
 				}
 			}
 
 			if (missingLibraries.size() > 0) {
 				if (MessageDialog.openQuestion(getShell(),
 						Messages.NewServletTestCaseWizard_ErrorMessageTitleMissingLibrary,
 						NLS.bind(Messages.NewServletTestCaseWizard_ErrorMessageMissingLibrary,
 								missingLibraries.toArray()))) {
 					CactusAddLibrariesProposal.installLibraries(project.getProject());
 				}
 			}
 		} catch (JavaModelException e) {
 			JavaServerUIPlugin.log(e);
 		}
 	}
 
 	public boolean performFinish() {
 		if (finishPage(fPage1.getRunnable())) {
 			IType newClass = fPage1.getCreatedType();
 
 			IResource resource = newClass.getCompilationUnit().getResource();
 			if (resource != null) {
 				BasicNewResourceWizard.selectAndReveal(resource, fWorkbench
 						.getActiveWorkbenchWindow());
 				openResource(resource);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	protected boolean finishPage(IRunnableWithProgress runnable) {
 		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
 		try {
 			PlatformUI.getWorkbench().getProgressService().runInUI(getContainer(), op,
 					ResourcesPlugin.getWorkspace().getRoot());
 		} catch (InvocationTargetException e) {
 			String title = Messages.NewServletTestCaseWizard_ErrorTitleNew; // NewJUnitWizard_op_error_title
 			String message = Messages.NewServletTestCaseWizard_ErrorTitleCreateOfElementFailed; // NewJUnitWizard_op_error_message
 			displayMessageDialog(e, e.getMessage(), getShell(), title, message);
 			return false;
 		} catch (InterruptedException e) {
 			return false;
 		}
 		return true;
 	}
 
 	public static void displayMessageDialog(Throwable t, String exceptionMessage,
 			Shell shell, String title, String message) {
 		StringWriter msg = new StringWriter();
 		if (message != null) {
 			msg.write(message);
 			msg.write("\n\n"); //$NON-NLS-1$
 		}
 		if (exceptionMessage == null || exceptionMessage.length() == 0)
 			msg.write(Messages.NewServletTestCaseWizard_ErrorMessageSeeErrorLog);
 		else
 			msg.write(exceptionMessage);
 		MessageDialog.openError(shell, title, msg.toString());
 	}
 
 	protected void openResource(final IResource resource) {
 		if (resource.getType() == IResource.FILE) {
 			final IWorkbenchPage activePage = getActivePage();
 			if (activePage != null) {
 				final Display display = Display.getDefault();
 				if (display != null) {
 					display.asyncExec(new Runnable() {
 						public void run() {
 							try {
 								IDE.openEditor(activePage, (IFile) resource, true);
 							} catch (PartInitException e) {
 								JavaServerUIPlugin.log(e);
 							}
 						}
 					});
 				}
 			}
 		}
 	}
 
 	private static IWorkbenchPage getActivePage() {
 		IWorkbenchWindow activeWorkbenchWindow = getActiveWorkbenchWindow();
 		if (activeWorkbenchWindow == null)
 			return null;
 		return activeWorkbenchWindow.getActivePage();
 	}
 
 	private static IWorkbenchWindow getActiveWorkbenchWindow() {
 		IWorkbench workbench = JavaServerUIPlugin.getInstance().getWorkbench();
 		if (workbench != null)
 			return workbench.getActiveWorkbenchWindow();
 
 		return null;
 	}
 }

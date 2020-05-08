 package org.eclipse.uml2.diagram.clazz.part;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
 import org.eclipse.gmf.runtime.diagram.core.services.view.CreateDiagramViewOperation;
 import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredLayoutCommand;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
 import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.commands.core.command.CompositeTransactionalCommand;
 import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackageEditPart;
 import org.eclipse.uml2.diagram.common.async.ApplySynchronizationCommand;
 
 /**
  * @generated
  */
 public class UMLNewDiagramFileWizard extends Wizard {
 
 	/**
 	 * @generated
 	 */
 	private WizardNewFileCreationPage myFileCreationPage;
 
 	/**
 	 * @generated
 	 */
 	private ModelElementSelectionPage diagramRootElementSelectionPage;
 
 	/**
 	 * @generated
 	 */
 	private TransactionalEditingDomain myEditingDomain;
 
 	/**
 	 * @NOT-generated
 	 */
	private NewDiagramSynchronizationPage synchronizationPage;
 
 	/**
 	 * @generated NOT
 	 */
 	public UMLNewDiagramFileWizard(URI domainModelURI, EObject diagramRoot, TransactionalEditingDomain editingDomain) {
 		assert domainModelURI != null : "Domain model uri must be specified"; //$NON-NLS-1$
 		assert diagramRoot != null : "Doagram root element must be specified"; //$NON-NLS-1$
 		assert editingDomain != null : "Editing domain must be specified"; //$NON-NLS-1$
 
 		createFileCreationPage(domainModelURI);
 		createDiagramRootSelectorPage(diagramRoot);
 		createSynchronizationPage(editingDomain);
 
 		this.myEditingDomain = editingDomain;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	protected WizardNewFileCreationPage getFileCreationPage() {
 		return myFileCreationPage;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	protected ModelElementSelectionPage getDiagramRootElementSelectionPage() {
 		return diagramRootElementSelectionPage;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	protected TransactionalEditingDomain getEditingDomain() {
 		return myEditingDomain;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private void createSynchronizationPage(TransactionalEditingDomain editingDomain) {
		synchronizationPage = new NewDiagramSynchronizationPage("Select diagram synchronization scheme", editingDomain);
 		synchronizationPage.setTitle("Diagram synchronization");
 		synchronizationPage.setDescription("Select diagram contents and its synchronization mode");
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private void createDiagramRootSelectorPage(EObject diagramRoot) {
 		diagramRootElementSelectionPage = new DiagramRootElementSelectionPage(Messages.UMLNewDiagramFileWizard_RootSelectionPageName);
 		diagramRootElementSelectionPage.setTitle(Messages.UMLNewDiagramFileWizard_RootSelectionPageTitle);
 		diagramRootElementSelectionPage.setDescription(Messages.UMLNewDiagramFileWizard_RootSelectionPageDescription);
 		diagramRootElementSelectionPage.setModelElement(diagramRoot);
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private void createFileCreationPage(URI domainModelURI) {
 		myFileCreationPage = new WizardNewFileCreationPage(Messages.UMLNewDiagramFileWizard_CreationPageName, StructuredSelection.EMPTY);
 		myFileCreationPage.setTitle(Messages.UMLNewDiagramFileWizard_CreationPageTitle);
 		myFileCreationPage.setDescription(NLS.bind(Messages.UMLNewDiagramFileWizard_CreationPageDescription, PackageEditPart.MODEL_ID));
 		IPath filePath;
 		String fileName = URI.decode(domainModelURI.trimFileExtension().lastSegment());
 		if (domainModelURI.isPlatformResource()) {
 			filePath = new Path(domainModelURI.trimSegments(1).toPlatformString(true));
 		} else if (domainModelURI.isFile()) {
 			filePath = new Path(domainModelURI.trimSegments(1).toFileString());
 		} else {
 			// TODO : use some default path
 			throw new IllegalArgumentException("Unsupported URI: " + domainModelURI); //$NON-NLS-1$
 		}
 		myFileCreationPage.setContainerFullPath(filePath);
 		myFileCreationPage.setFileName(UMLDiagramEditorUtil.getUniqueFileName(filePath, fileName, "umlclass")); //$NON-NLS-1$
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public void addPages() {
 		addPage(myFileCreationPage);
 		addPage(diagramRootElementSelectionPage);
 		addPage(synchronizationPage);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	public boolean performFinish() {
 		List affectedFiles = new LinkedList();
 		IFile diagramFile = myFileCreationPage.createNewFile();
 		UMLDiagramEditorUtil.setCharset(diagramFile);
 		affectedFiles.add(diagramFile);
 		URI diagramModelURI = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
 		ResourceSet resourceSet = myEditingDomain.getResourceSet();
 		final Resource diagramResource = resourceSet.createResource(diagramModelURI);
 
 		CompositeTransactionalCommand result = new CompositeTransactionalCommand(myEditingDomain, Messages.UMLNewDiagramFileWizard_InitDiagramCommand);
 		AbstractTransactionalCommand command = getCreateDiagramCommand(diagramResource, affectedFiles);
 		result.add(command);
 		AbstractTransactionalCommand syncCommand = getApplySynchronizationCommand();
 		if (syncCommand != null) {
 			result.add(syncCommand);
 		}
 		try {
 			OperationHistoryFactory.getOperationHistory().execute(result.reduce(), new NullProgressMonitor(), null);
 			diagramResource.save(UMLDiagramEditorUtil.getSaveOptions());
 			UMLDiagramEditorUtil.openDiagram(diagramResource);
 			if (needsLayoutAll()) {
 				layoutAll();
 			}
 		} catch (ExecutionException e) {
 			UMLDiagramEditorPlugin.getInstance().logError("Unable to create model and diagram", e); //$NON-NLS-1$
 		} catch (IOException ex) {
 			UMLDiagramEditorPlugin.getInstance().logError("Save operation failed for: " + diagramModelURI, ex); //$NON-NLS-1$
 		} catch (PartInitException ex) {
 			UMLDiagramEditorPlugin.getInstance().logError("Unable to open editor", ex); //$NON-NLS-1$
 		}
 		return true;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	protected boolean needsLayoutAll() throws ExecutionException {
 		return (synchronizationPage.wasVisible() && synchronizationPage.getSyncRoot() != null);
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	protected void layoutAll() throws ExecutionException {
 		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		DiagramEditPart diagramEditPart = ((IDiagramWorkbenchPart) editorPart).getDiagramEditPart();
 		if (!diagramEditPart.getChildren().isEmpty()) {
 			List<IAdaptable> viewAdapters = new ArrayList<IAdaptable>(diagramEditPart.getChildren().size());
 			for (Object next : diagramEditPart.getChildren()) {
 				if (next instanceof IGraphicalEditPart) {
 					viewAdapters.add(new EObjectAdapter(((IGraphicalEditPart) next).getNotationView()));
 				}
 			}
 			if (!viewAdapters.isEmpty()) {
 				DeferredLayoutCommand layout = new DeferredLayoutCommand(myEditingDomain, viewAdapters, diagramEditPart);
 				OperationHistoryFactory.getOperationHistory().execute(layout, new NullProgressMonitor(), null);
 			}
 		}
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	protected AbstractTransactionalCommand getApplySynchronizationCommand() {
 		if (synchronizationPage.wasVisible() && synchronizationPage.getSyncRoot() != null) {
 			return new ApplySynchronizationCommand(synchronizationPage.getSyncRoot());
 		}
 		return null;
 	}
 
 	/**
 	 * @NOT-generated
 	 */
 	private AbstractTransactionalCommand getCreateDiagramCommand(final Resource diagramResource, List affectedFiles) {
 		AbstractTransactionalCommand command = new AbstractTransactionalCommand(myEditingDomain, Messages.UMLNewDiagramFileWizard_InitDiagramCommand, affectedFiles) {
 
 			protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 				int diagramVID = UMLVisualIDRegistry.getDiagramVisualID(diagramRootElementSelectionPage.getModelElement());
 				if (diagramVID != PackageEditPart.VISUAL_ID) {
 					return CommandResult.newErrorCommandResult(Messages.UMLNewDiagramFileWizard_IncorrectRootError);
 				}
 				Diagram diagram = getDiagram();
 				if (diagram == null) {
 					diagram = ViewService.createDiagram(diagramRootElementSelectionPage.getModelElement(), PackageEditPart.MODEL_ID, UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
 				}
 				diagramResource.getContents().add(diagram);
 
 				return CommandResult.newOKCommandResult();
 			}
 		};
 		return command;
 	}
 
 	protected Diagram getDiagram() {
 		return synchronizationPage.getDiagram();
 	}
 
 	/**
 	 * @generated
 	 */
 	private static class DiagramRootElementSelectionPage extends ModelElementSelectionPage {
 
 		/**
 		 * @generated
 		 */
 		protected DiagramRootElementSelectionPage(String pageName) {
 			super(pageName);
 		}
 
 		/**
 		 * @generated
 		 */
 		protected String getSelectionTitle() {
 			return Messages.UMLNewDiagramFileWizard_RootSelectionPageSelectionTitle;
 		}
 
 		/**
 		 * @generated
 		 */
 		protected boolean validatePage() {
 			if (selectedModelElement == null) {
 				setErrorMessage(Messages.UMLNewDiagramFileWizard_RootSelectionPageNoSelectionMessage);
 				return false;
 			}
 			boolean result = ViewService.getInstance().provides(
 					new CreateDiagramViewOperation(new EObjectAdapter(selectedModelElement), PackageEditPart.MODEL_ID, UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT));
 			setErrorMessage(result ? null : Messages.UMLNewDiagramFileWizard_RootSelectionPageInvalidSelectionMessage);
 			return result;
 		}
 	}
 }

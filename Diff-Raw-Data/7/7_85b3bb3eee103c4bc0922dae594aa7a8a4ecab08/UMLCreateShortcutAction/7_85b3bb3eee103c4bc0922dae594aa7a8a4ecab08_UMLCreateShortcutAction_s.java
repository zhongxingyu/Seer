 package org.eclipse.uml2.diagram.clazz.part;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.IOperationHistory;
 import org.eclipse.core.commands.operations.OperationHistoryFactory;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.gmf.runtime.diagram.ui.commands.CreateCommand;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
 import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
 import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
 import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.uml2.diagram.clazz.edit.commands.UMLCreateShortcutDecorationsCommand;
 
 /**
  * @generated
  */
 public class UMLCreateShortcutAction extends AbstractHandler {
 
 	/**
 	 * @generated NOT 
 	 * FIXME: custom template should make this method generated again [256496]
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IEditorPart diagramEditor = HandlerUtil.getActiveEditorChecked(event);
 		Shell shell = diagramEditor.getEditorSite().getShell();
 		assert diagramEditor instanceof DiagramEditor;
 		TransactionalEditingDomain editingDomain = ((DiagramEditor) diagramEditor).getEditingDomain();
 		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
 		assert selection instanceof IStructuredSelection;
 		assert ((IStructuredSelection) selection).size() == 1;
 		assert ((IStructuredSelection) selection).getFirstElement() instanceof EditPart;
 		EditPart selectedDiagramPart = (EditPart) ((IStructuredSelection) selection).getFirstElement();
 		final View view = (View) selectedDiagramPart.getModel();
 		UMLElementChooserDialog elementChooser = new UMLElementChooserDialog(shell, view);
 		int result = elementChooser.open();
 		if (result != Window.OK) {
 			return null;
 		}
 		URI selectedModelElementURI = elementChooser.getSelectedModelElementURI();
 		final EObject selectedElement;
 		try {
 			selectedElement = editingDomain.getResourceSet().getEObject(selectedModelElementURI, true);
 		} catch (WrappedException e) {
 			UMLDiagramEditorPlugin.getInstance().logError("Exception while loading object: " + selectedModelElementURI.toString(), e); //$NON-NLS-1$
 			return null;
 		}
 
 		if (selectedElement == null) {
 			return null;
 		}
 
 		IOperationHistory history = OperationHistoryFactory.getOperationHistory();
 		IStatus status = createShortcut(editingDomain, history, selectedElement, selectedDiagramPart, diagramEditor);
 
 		if (!status.isOK()) {
 			UMLDiagramEditorPlugin.getInstance().logError(status.getMessage(), status.getException());
 		}
 
 		return null;
 	}
 
 	/**
 	 * @NOT-GENERATED 
 	 * FIXME: custom template should make this method generated again [256496]
 	 */
 	public static IStatus createShortcut(TransactionalEditingDomain editingDomain, IOperationHistory history, EObject selectedElement, EditPart editPart, IEditorPart diagramEditor) {
 
 		final View view = (View) editPart.getModel();
		final EditPart parentPart = editPart.getParent();
 		final Diagram diagram = view.getDiagram();
 
 		CreateViewRequest.ViewDescriptor viewDescriptor = new CreateViewRequest.ViewDescriptor(new EObjectAdapter(selectedElement), Node.class, null, UMLDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
 
 		ICommand command = new CreateCommand(editingDomain, viewDescriptor, diagram);
 		command = command.compose(new UMLCreateShortcutDecorationsCommand(editingDomain, diagram, viewDescriptor));
 		IStatus status = null;
 		try {
 			status = OperationHistoryFactory.getOperationHistory().execute(command, new NullProgressMonitor(), null);
		} catch (ExecutionException e) {
 			status = new Status(IStatus.ERROR, UMLDiagramEditorPlugin.ID, IStatus.OK, "Unable to create shortcut", e); //$NON-NLS-1$
 		}
 
 		CanonicalEditPolicy policy = (CanonicalEditPolicy) parentPart.getEditPolicy(EditPolicyRoles.CANONICAL_ROLE);
 		if (policy != null) {
 			policy.refresh();
 		}
 
 		return status;
 	}
 }

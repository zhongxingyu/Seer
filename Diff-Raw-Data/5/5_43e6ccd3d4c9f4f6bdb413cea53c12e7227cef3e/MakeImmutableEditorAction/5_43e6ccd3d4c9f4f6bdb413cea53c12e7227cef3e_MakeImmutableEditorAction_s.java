 package edu.uiuc.immutability.ui;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
 import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorActionDelegate;
 import org.eclipse.ui.IEditorPart;
 
 import edu.uiuc.immutability.MakeImmutableRefactoring;
 
 public class MakeImmutableEditorAction implements IEditorActionDelegate{
 
 	private JavaEditor fEditor;
 	private ITextSelection fTextSelection;
 
	@Override
 	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
 		if (targetEditor instanceof JavaEditor) {
 			fEditor = (JavaEditor) targetEditor;
 		}
 	}
 
	@Override
 	public void run(IAction action) {
 		IJavaElement[] elements;
 		try {
 			elements = SelectionConverter.codeResolveForked(fEditor, true);
 			if (elements.length == 1 && (elements[0] instanceof IType)) {
 				IType targetClass= (IType) elements[0];
 				
 				if (isRefactoringAvailableFor(targetClass)) {
 					MakeImmutableRefactoring refactoring = new MakeImmutableRefactoring(targetClass);
 					run(new MakeImmutableWizard(refactoring, "Make Immutable Class"), getShell(), "Make Immutable Class");
 					return;
 				}
 			}
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 		MessageDialog.openError(getShell(), "Error MakeImmutable", "MakeImmutableRefactoring not applicable for current selection"); 
 	}
 
 	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
 		try {
 			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
 			operation.run(parent, dialogTitle);
 		} catch (InterruptedException exception) {
 			// Do nothing
 		}
 	}
 	
 	
 	private Shell getShell() {
 		return fEditor.getSite().getShell();
 	}
 
	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof ITextSelection) {
 			fTextSelection = (ITextSelection) selection;
 		}
 	}
 	
 	private boolean isRefactoringAvailableFor(IType targetClass) throws JavaModelException {
 		return targetClass != null
 			&& targetClass.exists()
 			&& targetClass.isStructureKnown()
 			&& !targetClass.isAnnotation();
 	}
 }

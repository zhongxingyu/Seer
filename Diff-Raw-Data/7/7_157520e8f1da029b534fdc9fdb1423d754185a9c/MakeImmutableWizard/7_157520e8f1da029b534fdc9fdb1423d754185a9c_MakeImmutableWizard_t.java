 package edu.uiuc.immutability.ui;
 
 import org.eclipse.ltk.core.refactoring.Refactoring;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
 
 import edu.uiuc.immutability.MakeImmutableRefactoring;
 
 public class MakeImmutableWizard extends RefactoringWizard {
 
 	public MakeImmutableWizard(Refactoring refactoring, int flags) {
 		super(refactoring, flags);
 	}
 
 	public MakeImmutableWizard(
 			MakeImmutableRefactoring refactoring, String string) {
 		super(refactoring, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
 		setDefaultPageTitle(string);
 	}
 
 	@Override
 	protected void addUserInputPages() {
 		addPage(new MakeImmutableInputPage("ConvertToParallelArray"));
 
 	}
 }

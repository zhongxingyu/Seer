 package de.fkoeberle.autocommit.event.refactor;
 
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.IUndoManager;
 import org.eclipse.ltk.core.refactoring.IUndoManagerListener;
 import de.fkoeberle.autocommit.AutoCommitPluginActivator;
 public class RefactoringListener implements IUndoManagerListener {
 	private boolean canCommit = false;
 	
 	@Override
 	public void undoStackChanged(IUndoManager manager) {
 		// ignore
 	}
 
 	@Override
 	public void redoStackChanged(IUndoManager manager) {
 		// ignore
 	}
 
 	private AutoCommitPluginActivator getAutoCommitPlugion() {
 		return AutoCommitPluginActivator.getDefault();
 	}
 	@Override
 	public void aboutToPerformChange(IUndoManager manager, Change change) {
		canCommit = getAutoCommitPlugion().noUncommittedChangesExists();
 	}
 
 	@Override
 	public void changePerformed(IUndoManager manager, Change change) {
 		if (canCommit) {
 			canCommit = false;
			getAutoCommitPlugion().commitIfPossible(change.getName());
 		}
 	}
 
 }

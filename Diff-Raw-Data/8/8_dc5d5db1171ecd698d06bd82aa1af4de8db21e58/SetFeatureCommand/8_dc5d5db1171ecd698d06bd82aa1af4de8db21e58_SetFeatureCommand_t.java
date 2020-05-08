 package org.eclipse.wazaabi.ide.ui.editparts.commands;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.commands.Command;
 
 public class SetFeatureCommand extends Command {
 
 	private EStructuralFeature feature = null;
 	private Object newValue = null;
 	private EObject target = null;
 	private Object previousValue = null;
 	private TransactionalEditingDomain editingDomain = null;
 
 	public EStructuralFeature getFeature() {
 		return feature;
 	}
 
 	public void setFeature(EStructuralFeature feature) {
 		this.feature = feature;
 	}
 
 	public Object getNewValue() {
 		return newValue;
 	}
 
 	public void setNewValue(Object newValue) {
 		this.newValue = newValue;
 	}
 
 	public EObject getTarget() {
 		return target;
 	}
 
 	public void setTarget(EObject target) {
 		this.target = target;
 		if (target == null)
 			return;
 		EditingDomain editingDomain = AdapterFactoryEditingDomain
 				.getEditingDomainFor(getTarget());
 		if (editingDomain instanceof TransactionalEditingDomain)
 			this.editingDomain = (TransactionalEditingDomain) editingDomain;
 	}
 
 	@Override
 	public boolean canExecute() {
 		return getTarget() != null && getEditingDomain() != null
 				&& getFeature() != null;
 	}
 
 	@Override
 	public boolean canUndo() {
 		return true;
 	}
 
 	@Override
 	public void execute() {
 		getEditingDomain().getCommandStack().execute(
 				new RecordingCommand(getEditingDomain()) {
 					protected void doExecute() {
						SetFeatureCommand.this.doExecute();
 					}
 				});
 	}
 
 	protected void doExecute() {
 		setPreviousValue(getTarget().eGet(getFeature()));
 		doRedo();
 	}
 
 	@Override
 	public void redo() {
 		getEditingDomain().getCommandStack().execute(
 				new RecordingCommand(getEditingDomain()) {
 					protected void doExecute() {
						SetFeatureCommand.this.doRedo();
 					}
 				});
 	}
 
 	protected void doRedo() {
 		getTarget().eSet(getFeature(), getNewValue());
 	}
 
 	@Override
 	public void undo() {
 		getEditingDomain().getCommandStack().execute(
 				new RecordingCommand(getEditingDomain()) {
 					protected void doExecute() {
						SetFeatureCommand.this.doUndo();
 					}
 				});
 	}
 
 	protected void doUndo() {
 		getTarget().eSet(getFeature(), getPreviousValue());
 	}
 
 	public Object getPreviousValue() {
 		return previousValue;
 	}
 
 	public void setPreviousValue(Object previousValue) {
 		this.previousValue = previousValue;
 	}
 
 	protected TransactionalEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 }

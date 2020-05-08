 package org.eclipse.uml2.diagram.activity.edit.commands;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.diagram.activity.providers.ElementInitializers;
 import org.eclipse.uml2.uml.Activity;
 import org.eclipse.uml2.uml.ActivityPartition;
 import org.eclipse.uml2.uml.MergeNode;
 import org.eclipse.uml2.uml.UMLFactory;
 
 /**
  * @generated
  */
 
 public class ActivityPartition_MergeNodeCreateCommand extends EditElementCommand {
 
 	/**
 	 * @generated
 	 */
 	public ActivityPartition_MergeNodeCreateCommand(CreateElementRequest req) {
 		super(req.getLabel(), null, req);
 	}
 
 	/**
 	 * FIXME: replace with setElementToEdit()
 	 * @generated
 	 */
 	protected EObject getElementToEditGen() {
 		EObject container = ((CreateElementRequest) getRequest()).getContainer();
 		if (container instanceof View) {
 			container = ((View) container).getElement();
 		}
 		return container;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected EObject getElementToEdit() {
		EObject container = ((CreateElementRequest) getRequest()).getContainer();
		if (container instanceof View) {
			container = ((View) container).getElement();
		}
		return container;
 	}
 
 	/**
 	 * @generated
 	 */
 	public boolean canExecute() {
 		return true;
 
 	}
 
 	/**
 	 * @generated
 	 */
 	protected CommandResult doExecuteWithResultGen(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		MergeNode newElement = UMLFactory.eINSTANCE.createMergeNode();
 
 		Activity owner = (Activity) getElementToEdit();
 		owner.getNodes().add(newElement);
 		ActivityPartition childHolder = (ActivityPartition) getElementToEdit();
 		childHolder.getNodes().add(newElement);
 
 		ElementInitializers.getInstance().init_MergeNode_3063(newElement);
 
 		doConfigure(newElement, monitor, info);
 
 		((CreateElementRequest) getRequest()).setNewElement(newElement);
 		return CommandResult.newOKCommandResult(newElement);
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		MergeNode newElement = UMLFactory.eINSTANCE.createMergeNode();
 
 		Activity owner = (Activity) getElementToEdit();
 		owner.getNodes().add(newElement);
 
 		ActivityPartition partition = (ActivityPartition) getElementToEditGen();
 		newElement.getInPartitions().add(partition);
 
 		ElementInitializers.getInstance().init_MergeNode_3063(newElement);
 
 		doConfigure(newElement, monitor, info);
 
 		((CreateElementRequest) getRequest()).setNewElement(newElement);
 		return CommandResult.newOKCommandResult(newElement);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void doConfigure(MergeNode newElement, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		IElementType elementType = ((CreateElementRequest) getRequest()).getElementType();
 		ConfigureRequest configureRequest = new ConfigureRequest(getEditingDomain(), newElement, elementType);
 		configureRequest.setClientContext(((CreateElementRequest) getRequest()).getClientContext());
 		configureRequest.addParameters(getRequest().getParameters());
 		ICommand configureCommand = elementType.getEditCommand(configureRequest);
 		if (configureCommand != null && configureCommand.canExecute()) {
 			configureCommand.execute(monitor, info);
 		}
 	}
 
 }

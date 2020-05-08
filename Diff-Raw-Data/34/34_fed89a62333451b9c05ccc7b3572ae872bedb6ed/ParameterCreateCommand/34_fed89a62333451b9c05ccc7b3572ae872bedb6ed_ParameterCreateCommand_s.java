 package org.eclipse.uml2.diagram.activity.edit.commands;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.emf.type.core.commands.CreateElementCommand;
 import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
 import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.uml2.uml.Activity;
 import org.eclipse.uml2.uml.Behavior;
 import org.eclipse.uml2.uml.Parameter;
 import org.eclipse.uml2.uml.ParameterSet;
 import org.eclipse.uml2.uml.UMLFactory;
 import org.eclipse.uml2.uml.UMLPackage;
 
 /**
  * @generated
  */
 
 public class ParameterCreateCommand extends EditElementCommand {
 
 	/**
 	 * @generated
 	 */
 	public ParameterCreateCommand(CreateElementRequest req) {
 		super(req.getLabel(), null, req);
 	}
 
 	/**
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
 		return getElementToEditGen().eContainer();
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
	protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		Parameter newElement = UMLFactory.eINSTANCE.createParameter();
 
 		Behavior owner = (Behavior) getElementToEdit();
 		owner.getOwnedParameters().add(newElement);
 		ParameterSet childHolder = (ParameterSet) getElementToEdit();
 		childHolder.getParameters().add(newElement);
 
 		doConfigure(newElement, monitor, info);
 
 		((CreateElementRequest) getRequest()).setNewElement(newElement);
 		return CommandResult.newOKCommandResult(newElement);
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void doConfigure(Parameter newElement, IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		IElementType elementType = ((CreateElementRequest) getRequest()).getElementType();
 		ConfigureRequest configureRequest = new ConfigureRequest(getEditingDomain(), newElement, elementType);
 		configureRequest.setClientContext(((CreateElementRequest) getRequest()).getClientContext());
 		configureRequest.addParameters(getRequest().getParameters());
 		ICommand configureCommand = elementType.getEditCommand(configureRequest);
 		if (configureCommand != null && configureCommand.canExecute()) {
 			configureCommand.execute(monitor, info);
 		}
 	}

	/**
	 * @generated NOT
	 */
	protected EObject doDefaultElementCreation() {
		Parameter newElement = UMLFactory.eINSTANCE.createParameter();

		Behavior owner = (Behavior) getElementToEdit();
		owner.getOwnedParameters().add(newElement);
		ParameterSet childHolder = (ParameterSet) getElementToEditGen();
		childHolder.getParameters().add(newElement);
		return newElement;
	}
 }

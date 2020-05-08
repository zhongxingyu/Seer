 package org.eclipse.uml2.diagram.clazz.action;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.eclipse.gef.commands.UnexecutableCommand;
 import org.eclipse.gmf.runtime.common.core.command.CommandResult;
 import org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction;
 import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
 import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
 import org.eclipse.gmf.runtime.emf.type.core.requests.AbstractEditCommandRequest;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.uml2.uml.Element;
 import org.eclipse.uml2.uml.Stereotype;
 
 public class ApplyStereotypeAction extends DiagramAction {
 
 	private static final String EMPTY_NAME = "<null>";
 
 	private Element myElement;
 
 	private Stereotype myStereotype;
 
 	public ApplyStereotypeAction(IWorkbenchPage workbenchPage, Element element, Stereotype stereotype) {
 		super(workbenchPage);
 		myElement = element;
 		myStereotype = stereotype;
 	}
 
 	@Override
 	protected Request createTargetRequest() {
 		return null;
 	}
 
 	@Override
 	protected boolean isSelectionListener() {
 		return true;
 	}
 
 	@Override
 	protected Command getCommand() {
 		final GraphicalEditPart elementEditPart = getElementEditPart();
 		if (elementEditPart == null) {
 			return UnexecutableCommand.INSTANCE;
 		}
 		TransactionalEditingDomain editingDomain = elementEditPart.getEditingDomain();
 		StereotypeRequest request = new StereotypeRequest(editingDomain);
 		request.setApplyNotUnapply(!isStereotypeAppliedTo(myElement, myStereotype));
 		request.setElement(myElement);
 		request.setStereotype(myStereotype);
 
 		CompoundCommand command = new CompoundCommand();
 		command.add(new ICommandProxy(new StereotypeCommand(request)));
 		command.add(new Command() {
 
 			@Override
 			public void execute() {
 				// there are no clear ways for parser to locate this change and
 				// update label
 				for (Object nextChildEP : elementEditPart.getChildren()){
 					if (nextChildEP instanceof ITextAwareEditPart){
 						((ITextAwareEditPart)nextChildEP).refresh();
 					}
 				}
 			}
 		});
 		return command;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return getElementEditPart() != null;
 	}
 
 	@Override
 	public void refresh() {
 		super.refresh();
 		setText(calculateText());
 		setChecked(calculateChecked());
 	}
 
 	private String calculateText() {
 		String name = myStereotype.getName();
 		return name != null ? name : EMPTY_NAME;
 	}
 
 	private boolean calculateChecked() {
 		return isStereotypeAppliedTo(myElement, myStereotype);
 	}
 
 	private GraphicalEditPart getElementEditPart() {
 		for (Object next : getSelectedObjects()) {
 			if (next instanceof GraphicalEditPart) {
 				GraphicalEditPart elementEditPart = (GraphicalEditPart) next;
 				return elementEditPart;
 			}
 		}
 		return null;
 	}
 
 	private boolean isStereotypeAppliedTo(Element element, Stereotype stereotype) {
 		return element.isStereotypeApplied(stereotype);
 	}
 
 	private class StereotypeCommand extends EditElementCommand {
 
 		protected StereotypeCommand(StereotypeRequest request) {
 			super(request.isApplyNotUnapply() ? "Apply Stereotype" : "Unapply Stereotype", request.getElement(), request);
 		}
 
 		@Override
 		protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 			StereotypeRequest request = (StereotypeRequest) getRequest();
 			if (request.isApplyNotUnapply()) {
 				request.getElement().applyStereotype(request.getStereotype());
 			} else {
 				request.getElement().unapplyStereotype(request.getStereotype());
 			}
 			return CommandResult.newOKCommandResult(request.getElement());
 		}
 	}
 
 	private static class StereotypeRequest extends AbstractEditCommandRequest {
 
 		private Element myElement;
 
 		private boolean myApplyNotUnapply;
 
 		private Stereotype myStereotype;
 
 		public StereotypeRequest(TransactionalEditingDomain domain) {
 			super(domain);
 		}
 
 		public void setApplyNotUnapply(boolean applyNotUnapply) {
 			myApplyNotUnapply = applyNotUnapply;
 		}
 
 		public void setElement(Element element) {
 			myElement = element;
 		}
 
 		public void setStereotype(Stereotype stereotype) {
 			myStereotype = stereotype;
 		}
 
 		public Stereotype getStereotype() {
 			return myStereotype;
 		}
 
 		public Element getElement() {
 			return myElement;
 		}
 
 		public boolean isApplyNotUnapply() {
 			return myApplyNotUnapply;
 		}

		@Override
 		public Object getEditHelperContext() {
			// we are not going to use edit helpers infrastructure
 			return null;
 		}
 
 	}
 }

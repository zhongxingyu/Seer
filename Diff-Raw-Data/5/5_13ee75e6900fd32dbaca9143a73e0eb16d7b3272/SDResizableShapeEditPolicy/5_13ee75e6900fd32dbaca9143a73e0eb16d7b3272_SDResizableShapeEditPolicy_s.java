 package org.eclipse.uml2.diagram.sequence.edit.policies;
 
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.RootEditPart;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CompoundCommand;
 import org.eclipse.gef.requests.ChangeBoundsRequest;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.ResizableShapeEditPolicy;
 import org.eclipse.uml2.diagram.sequence.edit.parts.InteractionEditPart;
 
 
public class SDResizableShapeEditPolicy extends ResizableShapeEditPolicy {
 	private final boolean myDoubledSession;
 
 	public SDResizableShapeEditPolicy(){
 		this(false);
 	}
 	
 	public SDResizableShapeEditPolicy(boolean doubledSession){
 		myDoubledSession = doubledSession;
 	}
 	
 	@Override
 	protected Command getResizeCommand(ChangeBoundsRequest request) {
 		Command basic = super.getResizeCommand(request);
 		return chainWithLayout(basic);
 	}
 	
 	@Override
 	protected Command getMoveCommand(ChangeBoundsRequest request) {
 		Command basic = super.getMoveCommand(request);
 		return chainWithLayout(basic);
 	}
 	
 	private Command chainWithLayout(Command command){
 		if (command == null){
 			return command;
 		}
 		
 		InteractionNestedLayoutRequest layoutReq = new InteractionNestedLayoutRequest();
 		if (myDoubledSession){
 			layoutReq.setRepeatSessionsCount(2);
 		}
 		
 		layoutReq.addReshapedElement((IGraphicalEditPart)getHost());
 		Command layout = getLayoutCommand(layoutReq);
 		if (layout != null){
 			CompoundCommand chained = new CompoundCommand();
 			chained.add(command);
 			chained.add(layout);
 			return chained;
 		} else {
 			return command;
 		}
 	}
 	
 	protected Command getLayoutCommand(InteractionNestedLayoutRequest request){
 		InteractionEditPart interactionEditPart = findInteractionEditPart(getHost());
 		if (interactionEditPart == null){
 			return null;
 		}
 		return interactionEditPart.getCommand(request);
 	}
 	
 	protected InteractionEditPart findInteractionEditPart(EditPart ep){
 		RootEditPart root = ep.getRoot();
 		while (ep != root && ep != null){
 			if (ep instanceof InteractionEditPart){
 				return (InteractionEditPart)ep;
 			}
 			ep = ep.getParent();
 		}
 		return null;
 	}
 	
 
 }

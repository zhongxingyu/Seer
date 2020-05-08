 package org.ow2.mindEd.adl.editor.graphic.ui.custom.providers;
 
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.emf.transaction.impl.TransactionImpl;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gmf.runtime.diagram.ui.tools.DragEditPartsTrackerEx;
 import org.eclipse.gmf.runtime.notation.View;
 
 import org.ow2.mindEd.adl.InterfaceDefinition;
 import org.ow2.mindEd.adl.Role;
 
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.layouts.InterfaceBorderItemLocator;
 import org.ow2.mindEd.adl.editor.graphic.ui.edit.parts.InterfaceDefinitionEditPart;
 import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorPlugin;
 
 public class BorderItemDragTracker extends DragEditPartsTrackerEx {
 
 	public BorderItemDragTracker(EditPart sourceEditPart) {
 		super(sourceEditPart);
 	}
 	
 	protected boolean isMove() {
 		return true;
 	}
 	
 	@Override
 	public void performDrag() {
 		if (getSourceEditPart() instanceof InterfaceDefinitionEditPart) {
 			InterfaceDefinitionEditPart editPart = (InterfaceDefinitionEditPart)getSourceEditPart();
 			InterfaceDefinition model = (InterfaceDefinition) ((View)getSourceEditPart().getModel()).getElement();
 			if (!model.isMerged())
 			{
 				try {
 					InterfaceBorderItemLocator locator = (InterfaceBorderItemLocator) editPart.getBorderItemLocator();
 					int newSide = locator.getSide(getLocation(), editPart.getPrimaryShape());
					int currentSide = locator.getCurrentSideOfParent();
					if (newSide != currentSide) {
						setInterfaceRole(editPart, newSide);
						locator.setCurrentSideOfParent(newSide);
						locator.setPreferredSideOfParent(newSide);
					}
 				} catch (Exception e) {
 					MindDiagramEditorPlugin.getInstance().logError("Unable to change interface's role", e);
 				}
 			}
 		}
 		super.performDrag();
 	}
 	
 	/**
 	 * Revert interface's role : <ul>
 	 * <li>if role was provides, set to requires</li>
 	 * <li>if role was requires, set to provides</li>
 	 * </ul>
 	 * @param editPart
 	 */
 	protected void revertInterfaceRole(InterfaceDefinitionEditPart editPart) {
 		try {
 			InterfaceDefinition element = (InterfaceDefinition)((View)editPart.getModel()).getElement();
 			TransactionImpl transaction = new TransactionImpl(editPart.getEditingDomain(),false);
 			transaction.start();
 			if (element.getRole() == Role.PROVIDES)
 				element.setRole(Role.REQUIRES);
 			else element.setRole(Role.PROVIDES);
 			transaction.commit();
 		} catch (NullPointerException e) {
 			MindDiagramEditorPlugin.getInstance().logError("Null exception when trying to revert interface's role",e);
 		} catch (Exception e) {
 			MindDiagramEditorPlugin.getInstance().logError("Undefined error when trying to revert interface's role",e);
 		}
 	}
 	
 	protected void setInterfaceRole(InterfaceDefinitionEditPart editPart, int newSide) {
 		
 		try {
 			InterfaceDefinition element = (InterfaceDefinition)((View)editPart.getModel()).getElement();
 			TransactionImpl transaction = new TransactionImpl(editPart.getEditingDomain(),false);
 			transaction.start();
 			if (newSide == PositionConstants.WEST)
 				element.setRole(Role.PROVIDES);
 			else element.setRole(Role.REQUIRES);
 			transaction.commit();
 		} catch (NullPointerException e) {
 			MindDiagramEditorPlugin.getInstance().logError("Null exception when trying to revert interface's role",e);
 		} catch (Exception e) {
 			MindDiagramEditorPlugin.getInstance().logError("Undefined error when trying to revert interface's role",e);
 		}
 		
 		
 		
 	}
 
 }

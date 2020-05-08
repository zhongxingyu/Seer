 package org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.generic;
 
 import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.eclipse.gmf.runtime.notation.View;
import org.ow2.mindEd.adl.AdlPackage;
 import org.ow2.mindEd.adl.ComponentReference;
import org.ow2.mindEd.adl.editor.graphic.ui.part.MindDiagramEditorPlugin;
 
 public class MindSubReferenceEditPart extends MindReferenceEditPart {
 
 	
 	
 	public MindSubReferenceEditPart(GraphicalEditPart editPart, int vID) {
 		super(editPart, vID, TYPE_REFERENCE);
 	}
 	
 	public MindSubReferenceEditPart(GraphicalEditPart editPart, int vID, int mindType) {
 		super(editPart, vID, mindType);
 	}
 	
 	@Override
 	public IFigure setupContentPane(IFigure nodeShape) {
 		if (nodeShape.getLayoutManager() == null) {
 			ConstrainedToolbarLayout layout = new ConstrainedToolbarLayout();
 			nodeShape.setLayoutManager(layout);
 		}
 		return nodeShape; // use nodeShape itself as contentPane
 	}
 	
 	
 	public ComponentReference resolveSemanticElement() {
 		View view = ((View)realEditPart.getModel());
 		return ((ComponentReference)view.getElement());
 	}
	
	public void handleNotificationEvent(Notification notification) {
		if (notification.getFeatureID(ComponentReference.class)
				== AdlPackage.COMPONENT_REFERENCE__REFERENCE_NAME) {
			try {
				// Refresh the parent body of this reference's component
				CanonicalEditPolicy ep = 
					(CanonicalEditPolicy) realEditPart.getParent().getParent()
						.getEditPolicy(EditPolicyRoles.CANONICAL_ROLE);
				ep.refresh();
			}
			catch (Exception e) {
				MindDiagramEditorPlugin.getInstance().logError("Refresh failed", e);
			}
		}
	}
 
 }

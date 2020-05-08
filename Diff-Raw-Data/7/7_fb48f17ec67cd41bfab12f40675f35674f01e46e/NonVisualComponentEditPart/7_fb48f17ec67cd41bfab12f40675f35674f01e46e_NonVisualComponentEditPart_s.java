 package org.eclipse.jst.pagedesigner.parts;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.ImageFigure;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.jst.pagedesigner.converter.ITagConverter;
 import org.eclipse.jst.pagedesigner.editpolicies.NonVisualChildGraphicalEditPolicy;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class NonVisualComponentEditPart extends NodeEditPart 
 {
     protected IFigure createFigure() 
     {
         IFigure figure = new ImageFigure(getTagConverter().getVisualImage())
         {
 
             protected void paintFigure(Graphics graphics) {
                 super.paintFigure(graphics);
                 
                 if (getImage() == null)
                     return;
 
                 Rectangle srcRect = new Rectangle(getImage().getBounds());
                 graphics.drawImage(getImage(), srcRect, getClientArea());
             }
             
         };
         
         figure.setMinimumSize(new Dimension(0,0));
         return figure;
     }
 
     public void notifyChanged(INodeNotifier notifier, int eventType,
             Object changedFeature, Object oldValue, Object newValue, int pos) {
         // for now, do nothing
     }
 
     protected void createEditPolicies() {
         super.createEditPolicies();
         installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE
                           , new NonVisualChildGraphicalEditPolicy());
         installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
                 null);
     }
 
     protected ITagConverter getTagConverter()
     {
        return (ITagConverter) getModel();
     }
     
     protected Element getModelElement()
     {
         return getTagConverter().getHostElement();
     }
 
     public IDOMNode getIDOMNode() 
     {
         return (IDOMNode) getModelElement();
     }
 
     public Node getDOMNode() {
         return getModelElement();
     }
 
 //    public DragTracker getDragTracker(Request request) {
 //        // TODO: need to define drag semantics for these
 //        // Also, right now edit part dragging causes bad behaviour
 //        // in the non-visual decorator
 //        return null;//new ObjectModeDragTracker(this);
 //    }
 }

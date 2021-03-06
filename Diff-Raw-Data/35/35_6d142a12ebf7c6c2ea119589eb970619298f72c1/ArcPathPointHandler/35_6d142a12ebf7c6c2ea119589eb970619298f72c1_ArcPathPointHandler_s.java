 /*
  * Created on 28-Feb-2004
  * Author is Michael Camacho
  *
  */
 package pipe.handlers;
 
 import pipe.actions.SplitArcPointAction;
 import pipe.actions.ToggleArcPointAction;
 import pipe.actions.edit.DeleteArcPathPointAction;
 import pipe.controllers.ArcController;
 import pipe.controllers.PetriNetController;
 import pipe.gui.ApplicationSettings;
 import pipe.models.component.arc.ArcPoint;
 import pipe.views.viewComponents.ArcPathPoint;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 
 
 public class ArcPathPointHandler extends PetriNetObjectHandler<ArcPoint, ArcPathPoint> {
 
 
     private final ArcController<?, ?> arcController;
 
     public ArcPathPointHandler(Container contentpane, ArcPathPoint arcPathPoint, PetriNetController controller,
                                ArcController<?, ?> arcController) {
         super(arcPathPoint, contentpane, arcPathPoint.getModel(), controller);
         this.arcController = arcController;
         enablePopup = true;
     }
 
     /**
      * Creates the popup menu that the user will see when they right click on a component
      */
     @Override
     public JPopupMenu getPopup(MouseEvent e) {
         JPopupMenu popup = new JPopupMenu();
         JMenuItem menuItem =
                 new JMenuItem(new DeleteArcPathPointAction(component, arcController));
         menuItem.setText("Delete");
         popup.add(menuItem);
 
         if (!(viewComponent.isDeleteable())) {
             popup.getComponent(0).setEnabled(false);
         }
 
         popup.insert(new JPopupMenu.Separator(), 0);
 
         if (viewComponent.getIndex() > 0) {
             menuItem = new JMenuItem(new ToggleArcPointAction(component, arcController));
             if (!viewComponent.isCurved()) {
                 menuItem.setText("Change to Curved");
             } else {
                 menuItem.setText("Change to Straight");
             }
             popup.insert(menuItem, 0);
         }
 
         menuItem = new JMenuItem(new SplitArcPointAction(component, arcController));
         menuItem.setText("Split Point");
         popup.add(menuItem, 1);
         return popup;
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         ((ArcPathPoint) e.getComponent()).setVisibilityLock(true);
         petriNetController.select(component);
         super.mousePressed(e);
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         ((ArcPathPoint) e.getComponent()).setVisibilityLock(false);
         super.mouseReleased(e);
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
        //        int transX = e.getX() - dragInit.x;//Grid.getModifiedValue(e.getX() - dragInit.x);
        //        int transY = e.getY() - dragInit.y;//Grid.getModifiedY(e.getY() - dragInit.y);
        //        petriNetController.translateSelected(new Point2D.Double(transX, transY));
        super.mouseDragged(e);
     }
 
     @Override
     public void mouseWheelMoved(MouseWheelEvent e) {
 
         if (!ApplicationSettings.getApplicationModel().isEditionAllowed() ||  //NOU-PERE
                 e.isControlDown()) {
             return;
         }
 
         if (e.isShiftDown()) {
             petriNetController.getHistoryManager().addNewEdit(viewComponent.togglePointType());
         }
     }
 
 }

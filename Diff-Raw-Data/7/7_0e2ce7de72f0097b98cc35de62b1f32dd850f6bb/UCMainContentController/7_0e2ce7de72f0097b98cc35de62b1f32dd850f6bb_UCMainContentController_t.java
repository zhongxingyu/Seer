 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BT.modules.UC.mainContent;
 
 import BT.interfaces.DrawingClicks;
 import BT.models.CoordinateModel;
 import BT.modules.UC.places.UCActor;
 import BT.modules.UC.places.UCJoinEdge.UCJoinEdgeController;
 import BT.modules.UC.places.UCUseCase;
 import java.awt.event.MouseEvent;
 import javax.swing.JOptionPane;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author Karel Hala
  */
 public class UCMainContentController extends UCMainContentModel implements DrawingClicks{
 
     /**
      * 
      */
     public UCMainContentController()
     {
         super();
     }
     
     /**
      * 
      * @param e
      * @param dragged 
      */
     @Override
     public void drawingMouseDragged(MouseEvent e, CoordinateModel dragged)
     {
         UCDrawingPane UCdrawing = this.mainContent.getDrawingPane();
         if (dragged instanceof UCActor || dragged instanceof UCUseCase)
         {
             if (this.newJoinEdge != null)
             {
                 this.newJoinEdge = null;
                 UCdrawing.setNewLine(null);
             }
             dragged.setX(e.getX());
             dragged.setY(e.getY());
         }
         else if (dragged instanceof UCJoinEdgeController)
         {
             UCJoinEdgeController draggedJoinEdge = (UCJoinEdgeController) dragged;
             if (!draggedJoinEdge.isInRange(e.getX(), e.getY()))
             {
                 this.LeftBottomContent.getButtonWithName(draggedJoinEdge.getJoinEdgeType().name()).setSelected(true);
                 removeLineFromArrayListAndSetNewLine(draggedJoinEdge);
                 drawingPanecheckMove(e);
             }
         }
         UCdrawing.getDrawing().repaint();
     }
     
     /**
      * 
      * @param evt 
      */
     @Override
     public void drawingPaneClicked(MouseEvent evt) 
     {
         JToggleButton selectedItemButton = this.LeftTopContent.getSelectedButton();
         if (selectedItemButton!=null){
             UCDrawingPane UCdrawing = this.mainContent.getDrawingPane();
             switch (selectedItemButton.getName()){
                 case "ACTOR":
                         UCActor actor = new UCActor(evt.getX(), evt.getY());
                         this.places.addObject(actor);
                      break;
                     
                case "USECASE":
                         UCUseCase useCase = new UCUseCase(evt.getX(), evt.getY());
                         this.places.addObject(useCase);
                      break;
                 }
             UCdrawing.getDrawing().repaint();
         }
     }
     
     /**
      * 
      * @param evt 
      */
     @Override
     public void drawingPanecheckMove(MouseEvent evt) 
     {
         UCDrawingPane UCdrawing = this.mainContent.getDrawingPane();
         if (this.newJoinEdge != null)
         {
             if (this.newJoinEdge.getSecondObject() == null)
             {
                 this.newJoinEdge.setEndPoint(evt.getPoint());
             }
             UCdrawing.setNewLine(this.newJoinEdge);
         }
         UCObjectChecker objectUnderMouse = new UCObjectChecker(places);
         CoordinateModel coordModel = objectUnderMouse.getObjectUnderMouse(evt.getPoint());
         if (coordModel != null)
         {
             coordModel.setHowerColor();
         }
         
         UCdrawing.getDrawing().repaint();
     }
 
     /**
      * 
      * @param clickedObject 
      */
     @Override
     public void setSelectedObject(CoordinateModel clickedObject) 
     {
         this.places.setAllObjectDiselected();
         places.setSelectedLinesOnObject(clickedObject);
         if (clickedObject!=null)
         {
             clickedObject.setSelected(true);
         }
 
         if (this.LeftBottomContent.getSelectedButton()!=null)
         {
             clickedOnObject(clickedObject);
         }
         this.mainContent.getDrawingPane().getDrawing().repaint();
     }
     
     /**
      * 
      * @param pressedObject 
      */
     @Override
     public void drawingPaneDoubleCliked(CoordinateModel pressedObject) 
     {
         String name = (String) JOptionPane.showInputDialog("Enter name of the object",pressedObject.getName());
         if (name!= null && !"".equals(name))
             pressedObject.setName(name);
         this.mainContent.getDrawingPane().getDrawing().repaint();
     }
     
     /**
      * 
      * @param clickedObject 
      */
     private void drawJoinEdge(CoordinateModel clickedObject)
     {
         this.newJoinEdge = UCJoinEdgeManipulator.createJoinEdge(this.newJoinEdge,clickedObject);
         UCJoinEdgeManipulator.changeLineTypeByButton(this.LeftBottomContent.getSelectedButton(),this.newJoinEdge);
 
         if (this.newJoinEdge.getfirstObject() != null && this.newJoinEdge.getSecondObject() != null)
         {
             if (this.newJoinEdge.getfirstObject().equals(clickedObject))
             {
                 this.newJoinEdge.setSelected(true);
             }
             else
             {
                 this.newJoinEdge.setSelected(false);
             }
             this.places.addObject(this.newJoinEdge);
             this.newJoinEdge = null;
         }
         UCDrawingPane UCdrawing = this.mainContent.getDrawingPane();
         UCdrawing.setNewLine(newJoinEdge);
     }
     
     /**
      * 
      * @param clickedObject 
      */
     private void clickedOnObject(CoordinateModel clickedObject) {
         if (clickedObject == null || clickedObject instanceof UCJoinEdgeController)
         {
             this.newJoinEdge = null;
             this.mainContent.getDrawingPane().setNewLine(null);
         }
         else
         {
             if (this.newJoinEdge!=null && this.newJoinEdge.getfirstObject().equals(clickedObject))
             {
                 this.newJoinEdge = null;
             }
             drawJoinEdge(clickedObject);
         }
     }
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BT.modules.UC.mainContent;
 
 import BT.BT;
 import BT.models.CoordinateModel;
 import BT.modules.UC.places.UCActor;
 import BT.modules.UC.places.UCJoinEdge.UCJoinEdgeController;
 import BT.modules.UC.places.UCUseCase;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author Karel Hala
  */
 public class UCJoinEdgeManipulator {
     
     /**
      * 
      * @param selectedJoinEdgeButton 
      */
     public static void changeLineTypeByButton(JToggleButton selectedButton, UCJoinEdgeController joinEdge)
     {
         switch (selectedButton.getName())
         {
            case "ASSOCIATION":
                     joinEdge.setJoinEdgeType(BT.UCLineType.ASSOCIATION);
                  break;
 
            case "USES":  
                     joinEdge.setJoinEdgeType(BT.UCLineType.USES);
                  break;
 
            case "EXTENDS":  
                     joinEdge.setJoinEdgeType(BT.UCLineType.EXTENDS);
                  break;
         }
     }
     
     /**
      * 
      * @param clickedObject 
      */
     public static UCJoinEdgeController createJoinEdge(UCJoinEdgeController joinEdge, CoordinateModel clickedObject)
     {   
         if (joinEdge == null)
         {
             joinEdge = new UCJoinEdgeController();
         }
         if (joinEdge.getfirstObject() == null)
         {
             joinEdge.setFirstObject(clickedObject);
         }
         else if (joinEdge.getSecondObject() == null)
         {
             joinEdge.setSecondObject(clickedObject);
         }       
         return joinEdge;
     }
     
     public static void setLineTypeBySecondObject(UCJoinEdgeController joinEdge)
     {
         if (joinEdge.getfirstObject() instanceof UCUseCase && joinEdge.getSecondObject() instanceof UCActor)
         {
             joinEdge.setJoinEdgeType(BT.UCLineType.ASSOCIATION);
         }
     }
 }

 /**
  * 
  */
 package fr.nantes1900.control.isletprocess;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JOptionPane;
 
 import fr.nantes1900.constants.ActionTypes;
 import fr.nantes1900.constants.Characteristics;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.islets.buildings.exceptions.InvalidCaseException;
 import fr.nantes1900.view.isletprocess.CharacteristicsStep2View;
 import fr.nantes1900.view.isletprocess.CharacteristicsStep3ElementsView;
import fr.nantes1900.view.isletprocess.CharacteristicsStep3TrianglesView;
 
 /**
  * Characteristics panel for the second step of process of an islet. User can
  * select one or more triangles and modifies the type they belong to : building
  * or ground.
  * @author Camille
  * @author Luc
  */
 public class CharacteristicsStep3ElementsController extends
         CharacteristicsController
 {
     public Mesh element;
 
     /**
      * Constructor.
      * @param parentController
      * @param triangleSelected
      */
     public CharacteristicsStep3ElementsController(
             IsletProcessController parentController, Mesh elementSelected)
     {
         super(parentController);
         element = elementSelected;
         this.cView = new CharacteristicsStep3ElementsView();
         this.cView.getValidateButton().addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(ActionEvent arg0)
             {
                String typeChosen = ((CharacteristicsStep3TrianglesView) cView)
                        .getElementSelected();
 
                 int actionType = -1;
                 switch (typeChosen)
                 {
                     case Characteristics.TYPE_NOISE:
                         actionType = ActionTypes.TURN_TO_NOISE;
                     break;
 
                     case Characteristics.TYPE_BUILDING:
                         actionType = ActionTypes.TURN_TO_BUILDING;
                     break;
                 }
 
                 try
                 {
                     CharacteristicsStep3ElementsController.this.parentController
                             .getBiController().action3(element, actionType);
                 } catch (InvalidCaseException e)
                 {
                     JOptionPane.showMessageDialog(cView,
                             "Le type choisi est incorrrect",
                             "Validation impossible", JOptionPane.ERROR_MESSAGE);
                 }
             }
         });
     }
 
     public void setElementSelected(Mesh elementSelected)
     {
         this.element = elementSelected;
         ((CharacteristicsStep2View) this.cView).setType("");
     }
 
     public Mesh getElement()
     {
         return this.element;
 
     }
 }

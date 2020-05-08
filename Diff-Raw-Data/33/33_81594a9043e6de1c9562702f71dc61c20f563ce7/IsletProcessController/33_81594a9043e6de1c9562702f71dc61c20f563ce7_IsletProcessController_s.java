 /**
  * 
  */
 package fr.nantes1900.control.isletprocess;
 
 import java.io.File;
 
 import fr.nantes1900.constants.ActionTypes;
 import fr.nantes1900.control.BuildingsIsletController;
 import fr.nantes1900.control.GlobalController;
 import fr.nantes1900.control.display3d.Universe3DController;
 import fr.nantes1900.view.isletprocess.IsletProcessView;
 
 /**
  * @author Camille
  */
 // FIXME : Javadoc
 public class IsletProcessController
 {
     private GlobalController         parentController;
     private IsletProcessView         ipView;
     private CaracteristicsController cController;
     private IsletTreeController      itController;
     private NavigationBarController  nbController;
     private ParametersController     pController;
     private Universe3DController     u3DController;
     private BuildingsIsletController biController;
     private int                      progression;
 
     public IsletProcessController(GlobalController parentController,
            File isletFile, BuildingsIsletController biController)
     {
         this.parentController = parentController;
 
         this.cController = new CaracteristicsController(this);
         this.itController = new IsletTreeController(this);
         this.nbController = new NavigationBarController(this);
         this.pController = new ParametersController(this);
         this.u3DController = new Universe3DController();
         this.biController = biController;
         this.biController.setUniverse3DController(u3DController);
         this.biController.display();
 
         this.ipView = new IsletProcessView(cController.getView(),
                itController.getView(), nbController.getView(),
                pController.getView(), u3DController.getUniverse3DView());
         this.ipView.setVisible(true);
     }
 
     /**
     * Launches an action depending of the actual step with the given action type.
     * 
      * @param actionType
     *          Type of the action to execute
      * @see ActionTypes, {@link BuildingsIsletController}
      */
     public void launchAction(int actionType)
     {
         switch (progression)
         {
             case 2:
                this.biController.action2(u3DController.getTrianglesSelected(), actionType);
                break;
         }
     }
 }

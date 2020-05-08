 package fr.nantes1900.control;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.vecmath.Vector3d;
 
 import fr.nantes1900.constants.ActionTypes;
 import fr.nantes1900.control.display3d.Universe3DController;
 import fr.nantes1900.control.isletselection.IsletSelectionController;
 import fr.nantes1900.models.basis.Mesh;
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.extended.Building;
 import fr.nantes1900.models.extended.Surface;
 import fr.nantes1900.models.islets.AbstractIslet.UnCompletedParametersException;
 import fr.nantes1900.models.islets.buildings.AbstractBuildingsIslet;
 import fr.nantes1900.models.islets.buildings.steps.BuildingsIsletStep1;
 import fr.nantes1900.utils.ParserSTL;
 import fr.nantes1900.view.display3d.MeshView;
 import fr.nantes1900.view.display3d.PolygonView;
 
 /**
  * Implements the controller of a building islet. Used to visualize the islets,
  * to launch the treatments.
  * @author Daniel
  */
 public class BuildingsIsletController
 {
 
     /**
      * The buildings islet containing the model.
      */
     private AbstractBuildingsIslet   islet;
     /**
      * The islet selection controller, which is the parent of this.
      */
     private IsletSelectionController parentController;
     /**
      * The universe 3D controller to interact with the universe 3D.
      */
     private Universe3DController     u3DController;
     /**
      * The normal of the gravity.
      */
     private Vector3d                 gravityNormal;
 
     /**
      * Constructor.
      * @param isletSelectionController
      *            the controller of the islet selection
      * @param universe3DControllerIn
      *            the universe 3D controller
      */
     public BuildingsIsletController(
             final IsletSelectionController isletSelectionController,
             final Universe3DController universe3DControllerIn)
     {
         this.parentController = isletSelectionController;
         this.u3DController = universe3DControllerIn;
     }
 
     /**
      * TODO.
      * @return TODO.
      */
     public final Vector3d computeNormalWithTrianglesSelected()
     {
         Mesh mesh = new Mesh(this.u3DController.getTrianglesSelected());
         return mesh.averageNormal();
     }
 
     /**
      * TODO.
      */
     public final void display()
     {
         this.u3DController.getUniverse3DView().clearAllMeshes();
 
         switch (this.islet.getProgression())
         {
             case 0:
            // TODO : error
             break;
             case 1:
                 this.viewStep1();
             break;
             case 2:
                 this.viewStep2();
             break;
             case 3:
                 this.viewStep3();
             break;
             case 4:
                 this.viewStep4();
             break;
             case 5:
                 this.viewStep5();
             break;
             case 6:
                 this.viewStep6();
             break;
             case 7:
                 this.viewStep7();
             break;
             case 8:
                 this.viewStep8();
             break;
             default:
             // TODO : error
             break;
         }
     }
 
     /**
      * Getter.
      * @return the gravity normal
      */
     public final Vector3d getGravityNormal()
     {
         return this.gravityNormal;
     }
 
     /**
      * Getter.
      * @return the buildings islet
      */
     public final AbstractBuildingsIslet getIslet()
     {
         return this.islet;
     }
 
     /**
      * Getter.
      * @return the controller of the islet selection
      */
     public final IsletSelectionController getIsletSelectionController()
     {
         return this.parentController;
     }
 
     /**
      * Getter.
      * @return the parent controller
      */
     public final IsletSelectionController getParentController()
     {
         return this.parentController;
     }
 
     /**
      * TODO.
      */
     public final void putGravityNormal()
     {
         this.islet.setGravityNormal(this.gravityNormal);
     }
 
     /**
      * TODO.
      * @param fileName
      *            TODO.
      * @throws IOException
      *             TODO.
      */
     public final void readGravityNormal(final String fileName)
             throws IOException
     {
         ParserSTL parser = new ParserSTL(fileName);
         Mesh mesh = parser.read();
         this.setGravityNormal(mesh.averageNormal());
     }
 
     /**
      * Setter.
      * @param gravityNormalIn
      *            the gravity normal
      */
     public final void setGravityNormal(final Vector3d gravityNormalIn)
     {
         this.gravityNormal = gravityNormalIn;
     }
 
     /**
      * Setter.
      * @param groundNormal
      *            the ground normal
      */
     public final void setGroundNormal(final Vector3d groundNormal)
     {
         this.islet.setGroundNormal(groundNormal);
     }
 
     /**
      * Setter.
      * @param isletIn
      *            the new buildings islet
      */
     public final void setIslet(final AbstractBuildingsIslet isletIn)
     {
         this.islet = isletIn;
     }
 
     /**
      * Setter.
      * @param isletSelectionControllerIn
      *            the controller of the islet selection
      */
     public final void setIsletSelectionController(
             final IsletSelectionController isletSelectionControllerIn)
     {
         this.parentController = isletSelectionControllerIn;
     }
 
     /**
      * Setter.
      * @param parentControllerIn
      *            the parent controller
      */
     public final void setParentController(
             final IsletSelectionController parentControllerIn)
     {
         this.parentController = parentControllerIn;
     }
 
     /**
      * Setter.
      * @param u3dcontrollerIn
      *            the universe 3D controller
      */
     public final void setUniverse3DController(
             final Universe3DController u3dcontrollerIn)
     {
         this.u3DController = u3dcontrollerIn;
     }
 
     /**
      * TODO.
      */
     public final void useGravityNormalAsGroundNormal()
     {
         this.islet.setGroundNormal(this.islet.getGravityNormal());
     }
 
     /**
      * Getter.
      * @return the universe 3D controller
      */
     public final Universe3DController getU3DController()
     {
         return this.u3DController;
     }
 
     /**
      * Progression incrementation.
      */
     private void incProgression()
     {
         this.islet.incProgression();
     }
 
     /**
      * Launch the treatment, considering the progression.
      */
     public final void launchTreatment()
     {
         switch (this.islet.getProgression())
         {
             case 0:
                 this.launchTreatment0();
             break;
             case 1:
                 this.launchTreatment1();
             break;
             case 2:
                 this.launchTreatment2();
             break;
             case 3:
                 this.launchTreatment3();
             break;
             case 4:
                 this.launchTreatment4();
             break;
             case 5:
                 this.launchTreatment5();
             break;
             case 6:
                 this.launchTreatment6();
             break;
             case 7:
                 this.launchTreatment7();
             break;
             default:
             // It shouldn't happen.
             break;
         }
 
         this.incProgression();
         this.display();
     }
 
     public final void launchTreatment0()
     {
         try
         {
             this.islet.changeBase();
             this.islet.setBiStep1(new BuildingsIsletStep1(this.islet
                     .getInitialTotalMesh()));
         } catch (UnCompletedParametersException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     public final void launchTreatment2()
     {
         this.islet.setBiStep3(this.islet.getBiStep2().launchTreatment());
     }
 
     public final void launchTreatment3()
     {
         this.islet.setBiStep4(this.islet.getBiStep3().launchTreatment());
     }
 
     public final void launchTreatment4()
     {
         this.islet.setBiStep5(this.islet.getBiStep4().launchTreatment());
     }
 
     public final void launchTreatment5()
     {
         this.islet.setBiStep6(this.islet.getBiStep5().launchTreatment());
     }
 
     public final void launchTreatment6()
     {
         this.islet.setBiStep7(this.islet.getBiStep6().launchTreatment());
     }
 
     public final void launchTreatment7()
     {
         this.islet.setBiStep8(this.islet.getBiStep7().launchTreatment());
     }
 
     public final void viewStep1()
     {
         this.getU3DController().getUniverse3DView().addMesh(
                 new MeshView(this.islet.getBiStep1()
                         .getInitialTotalMeshAfterBaseChange()));
     }
 
     public final void launchTreatment1()
     {
         this.islet.setBiStep2(this.islet.getBiStep1().launchTreatment());
     }
 
     /**
      * Changes the type of a list of triangles.
      * @param trianglesSelected
      *            the list of triangles
      * @param type
      *            the new type of these triangles
      */
     public final void action2(final List<Triangle> trianglesSelected,
             final int type)
     {
         if (type == ActionTypes.TURN_TO_BUILDING)
         {
             // The user wants these triangles to turn building.
             this.islet.getBiStep2().getInitialGrounds().removeAll(
                     trianglesSelected);
             this.islet.getBiStep2().getInitialBuildings().addAll(
                     trianglesSelected);
         } else if (type == ActionTypes.TURN_TO_GROUND)
         {
             // The user wants these triangles to turn ground.
             this.islet.getBiStep2().getInitialBuildings().removeAll(
                     trianglesSelected);
             this.islet.getBiStep2().getInitialGrounds().addAll(
                     trianglesSelected);
         }
     }
 
     public final void viewStep2()
     {
         this.getU3DController().getUniverse3DView().addMesh(
                 new MeshView(this.islet.getBiStep2().getInitialBuildings()));
         this.getU3DController().getUniverse3DView().addMesh(
                 new MeshView(this.islet.getBiStep2().getInitialGrounds()));
 
         // Also display the noise.
     }
 
     public final void action3(Mesh mesh, int actionType)
     {
         if (actionType == ActionTypes.TURN_TO_NOISE)
         {
             this.islet.getBiStep3().getBuildings().remove(
                     this.returnBuildingContaining3(mesh));
             this.islet.getBiStep3().getNoise().addAll(mesh);
         } else if (actionType == ActionTypes.TURN_TO_BUILDING)
         {
             this.islet.getBiStep3().getBuildings().add(new Building(mesh));
             this.islet.getBiStep3().getNoise().removeAll(mesh);
         } else
         {
             // LOOK : error.
         }
     }
 
     public final Building returnBuildingContaining3(Mesh mesh)
     {
         for (Building building : this.islet.getBiStep3().getBuildings())
         {
             if (building.getbStep3().getInitialTotalMesh() == mesh)
             {
                 return null;
             }
         }
         return null;
     }
 
     public final void action3(List<Triangle> trianglesSelected, int actionType)
     {
         if (actionType == ActionTypes.REMOVE)
         {
             for (Building building : this.islet.getBiStep3().getBuildings())
             {
                 building.getbStep3().getInitialTotalMesh().removeAll(
                         trianglesSelected);
             }
             this.islet.getBiStep3().getGrounds().removeAll(trianglesSelected);
         } else if (actionType == ActionTypes.TURN_TO_BUILDING)
         {
             this.islet.getBiStep3().getBuildings().add(
                     new Building(new Mesh(trianglesSelected)));
             this.islet.getBiStep3().getNoise().removeAll(trianglesSelected);
         } else
         {
             // LOOK : error.
         }
     }
 
     public final void viewStep3()
     {
         this.getU3DController().getUniverse3DView().addMesh(
                 new MeshView(this.islet.getBiStep3().getGrounds()));
 
         for (Building building : this.islet.getBiStep3().getBuildings())
         {
             this.getU3DController().getUniverse3DView().addMesh(
                     new MeshView(building.getbStep3().getInitialTotalMesh()));
         }
     }
 
     public final void action4(List<Triangle> trianglesSelected, int actionType)
     {
         Building building = this
                 .searchForBuildingContaining4(trianglesSelected);
         if (building != null)
         {
             if (actionType == ActionTypes.TURN_TO_WALL)
             {
                 building.getbStep4().getInitialWall().addAll(trianglesSelected);
                 building.getbStep4().getInitialRoof().remove(trianglesSelected);
             } else if (actionType == ActionTypes.TURN_TO_ROOF)
             {
                 building.getbStep4().getInitialRoof().addAll(trianglesSelected);
                 building.getbStep4().getInitialWall().remove(trianglesSelected);
             }
         } else
         {
             // TODO : error.
         }
 
     }
 
     private Building searchForBuildingContaining4(
             final List<Triangle> trianglesSelected)
     {
         for (Building building : this.islet.getBiStep4().getBuildings())
         {
             if (building.getbStep4().getInitialWall().containsAll(
                     trianglesSelected)
                     || building.getbStep4().getInitialRoof().containsAll(
                             trianglesSelected))
             {
                 return building;
             }
         }
         return null;
     }
 
     public final void viewStep4()
     {
         for (Building building : this.islet.getBiStep4().getBuildings())
         {
             this.getU3DController().getUniverse3DView().addMesh(
                     new MeshView(building.getbStep4().getInitialWall()));
             this.getU3DController().getUniverse3DView().addMesh(
                     new MeshView(building.getbStep4().getInitialRoof()));
         }
     }
 
     public final void viewStep5()
     {
         for (Building building : this.islet.getBiStep5().getBuildings())
         {
             for (Surface wall : building.getbStep5().getWalls())
             {
                 this.getU3DController().getUniverse3DView().addMesh(
                         new MeshView(wall.getMesh()));
             }
             for (Surface roof : building.getbStep5().getRoofs())
             {
                 this.getU3DController().getUniverse3DView().addMesh(
                         new MeshView(roof.getMesh()));
             }
         }
     }
 
     public final void viewStep6()
     {
         for (Building building : this.islet.getBiStep6().getBuildings())
         {
             for (Surface wall : building.getbStep6().getWalls())
             {
                 this.getU3DController().getUniverse3DView().addMesh(
                         new MeshView(wall.getMesh()));
             }
             for (Surface roof : building.getbStep6().getRoofs())
             {
                 this.getU3DController().getUniverse3DView().addMesh(
                         new MeshView(roof.getMesh()));
             }
         }
     }
 
     public final void viewStep7()
     {
         for (Building building : this.islet.getBiStep7().getBuildings())
         {
             for (Surface wall : building.getbStep7().getWalls())
             {
                 this.getU3DController().getUniverse3DView().addMesh(
                         new MeshView(wall.getMesh()));
             }
             for (Surface roof : building.getbStep7().getRoofs())
             {
                 this.getU3DController().getUniverse3DView().addMesh(
                         new MeshView(roof.getMesh()));
             }
         }
     }
 
     public final void viewStep8()
     {
         for (Building building : this.islet.getBiStep8().getBuildings())
         {
             for (Surface wall : building.getbStep8().getWalls())
             {
                 this.getU3DController().getUniverse3DView().addPolygonView(
                         new PolygonView(wall.getPolygone()));
             }
             for (Surface roof : building.getbStep8().getRoofs())
             {
                 this.getU3DController().getUniverse3DView().addPolygonView(
                         new PolygonView(roof.getPolygone()));
             }
         }
     }
 }

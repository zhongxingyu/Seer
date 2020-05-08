 /**
  * 
  */
 package fr.nantes1900.control.isletselection;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.vecmath.Vector3d;
 
 import fr.nantes1900.control.BuildingsIsletController;
 import fr.nantes1900.control.GlobalController;
 import fr.nantes1900.control.display3d.Universe3DController;
 import fr.nantes1900.models.basis.Edge;
 import fr.nantes1900.models.basis.Point;
 import fr.nantes1900.models.basis.Triangle;
 import fr.nantes1900.models.islets.buildings.AbstractBuildingsIslet;
 import fr.nantes1900.models.islets.buildings.ResidentialIslet;
 import fr.nantes1900.models.middle.Mesh;
 import fr.nantes1900.utils.FileTools;
 import fr.nantes1900.utils.ParserSTL;
 import fr.nantes1900.utils.WriterSTL;
 import fr.nantes1900.view.isletselection.GlobalTreeView.FileNode;
 import fr.nantes1900.view.isletselection.IsletSelectionView;
 
 /**
  * @author Camille
  */
 public class IsletSelectionController
 {
 
     /**
      * The controller of the panel containing buttons to perform the different
      * actions.
      */
     private ActionsController        aController;
 
     /**
      * The controller of the tree used to select an islet.
      */
     private GlobalTreeController     gtController;
 
     /**
      * The controller of the 3D view which shows a selected islet.
      */
     private Universe3DController     u3DController;
 
     /**
      * The controller of the selected islet.
      */
     private BuildingsIsletController biController;
 
     /**
      * View allowing to select an islet and launch a treatment.
      */
     private IsletSelectionView       isView;
 
     /**
      * The opened directory corresponding.
      */
     private File                     openedDirectory;
 
     /**
      * The selected file in the tree.
      */
     private File                     selectedFile;
 
     /**
      * The parent controller which handles this one.
      */
     private GlobalController         parentController;
 
     /**
      * Creates a new controller to handle the islet selection window.
      */
     public IsletSelectionController(GlobalController parentController)
     {
         this.parentController = parentController;
         this.gtController = new GlobalTreeController(this);
         this.aController = new ActionsController(this);
         this.u3DController = new Universe3DController(this);
 
         this.isView = new IsletSelectionView(this.aController.getActionsView(),
                 this.gtController.getGlobalTreeView(),
                 this.u3DController.getUniverse3DView());
         this.isView.setVisible(true);
     }
 
     /**
      * Updates the directory containing the files of islets.
      * @param newDirectory
      *            The new directory.
      */
     public void updateMockupDirectory(File newDirectory)
     {
         this.openedDirectory = newDirectory;
         this.gtController.updateDirectory(this.openedDirectory);
 
         // checks if the gravity normal already exists
         File gravityNormal = new File(openedDirectory.getPath()
                 + "/gravity_ground.stl");
         if (!gravityNormal.exists())
         {
             JOptionPane
                     .showMessageDialog(
                             isView,
                             "La normale oriente selon la gravit n'a pas t trouv dans le dossier ouvert.\nVeuillez en crer une nouvelle.",
                             "Normale oriente selon la gravit inexistante",
                             JOptionPane.INFORMATION_MESSAGE);
             aController.setComputeNormalMode();
             this.isView.setStatusBarText(FileTools.readHelpMessage(
                     "ISGravityNormal", FileTools.MESSAGETYPE_STATUSBAR));
         } else
         {
             this.isView.setStatusBarText(FileTools.readHelpMessage(
                     "ISLaunchProcess", FileTools.MESSAGETYPE_STATUSBAR));
             aController.setLaunchMode();
         }
     }
 
     /**
      * Launches the treatment of the selected file which is an islet file. The
      * verification that the selected file is an islet file is made at the
      * selection in the tree.
      */
     public boolean launchIsletTreatment()
     {
         boolean processLaunched = false;
 
         if ((!this.u3DController.getTrianglesSelected().isEmpty() || this.aController
                 .getActionsView().isGravityGroundCheckBoxSelected())
                 && selectedFile != null)
         {
             if (!this.aController.getActionsView()
                     .isGravityGroundCheckBoxSelected())
             {
                 computeGroundNormal();
             } else
             {
                 this.biController.useGravityNormalAsGroundNormal();
             }
             this.parentController.launchIsletTreatment(this.selectedFile, this.biController);
             processLaunched = true;
         } else
         {
             JOptionPane
                     .showMessageDialog(
                             isView,
                             "Veuillez slectionner un lot et une normale pour lancer le traitement",
                             "Traitement impossible", JOptionPane.ERROR_MESSAGE);
         }
 
         return processLaunched;
     }
 
    private Vector3d readGravityNormal()
    {
        // TODO Auto-generated method stub
        return null;
    }

     public void displayFile(DefaultMutableTreeNode node)
     {
         // Reads the file object of the Tree
         FileNode fileNode = (FileNode) node.getUserObject();
 
         if (fileNode.isFile())
         {
             this.biController = new BuildingsIsletController(this,
                     this.u3DController);
 
             ParserSTL parser = new ParserSTL(fileNode.getEntireName());
             this.selectedFile = (File) fileNode;
 
             AbstractBuildingsIslet resIslet;
             try
             {
                 resIslet = new ResidentialIslet(parser.read());
                 this.getBiController().setIslet(resIslet);
                 this.getBiController().display();
             } catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
 
     public boolean computeGravityNormal()
     {
         boolean normalSaved = false;
         if (selectedFile != null
                 && !this.u3DController.getTrianglesSelected().isEmpty())
         {
             WriterSTL writer = new WriterSTL(this.openedDirectory.getPath()
                    + "gravity_normal.stl");
             Point point = new Point(1, 1, 1);
             Edge edge = new Edge(point, point);
             Triangle triangle = new Triangle(point, point, point, edge, edge,
                     edge,
                     this.biController.computeNormalWithTrianglesSelected());
             Mesh mesh = new Mesh();
             mesh.add(triangle);
             writer.setMesh(mesh);
             writer.write();
             System.out.println("Enregistr");
             normalSaved = true;
         } else
         {
             JOptionPane
                     .showMessageDialog(
                             isView,
                             "Slectionnez un lot dans l'arbre\npuis slectionnez des triangles pour crer la normale\nou slectionnez \"Utiliser la normale oriente selon la gravit\n",
                             "Aucun lot ouvert", JOptionPane.ERROR_MESSAGE);
         }
 
         return normalSaved;
     }
 
     public BuildingsIsletController getBiController()
     {
         return this.biController;
     }
 
     public void computeGroundNormal()
     {
         this.biController.setGroundNormal(this.biController
                 .computeNormalWithTrianglesSelected());
     }
 }

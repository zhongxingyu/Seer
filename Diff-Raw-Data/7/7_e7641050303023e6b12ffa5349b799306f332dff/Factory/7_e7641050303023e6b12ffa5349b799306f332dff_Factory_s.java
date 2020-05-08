 package simulation;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Scanner;
 import util.Vector;
 
 
 /**
  * @author Robert C. Duvall
  */
 public class Factory {
     // data file keywords
     private static final String MASS_KEYWORD = "mass";
     private static final String SPRING_KEYWORD = "spring";
     private static final String GRAVITY_KEYWORD = "gravity";
     private static final String VISCOSITY_KEYWORD = "viscosity";
     private static final String CENTERMASS_KEYWORD = "centermass";
     private static final String WALL_REPULSION_KEYWORD = "wall";
 
     // mass IDs
     private Map<Integer, Mass> myMasses = new HashMap<Integer, Mass>();
 
     /**
      * Loads the model information 
      * @param model the current Model that information is being loaded into.
      * @param modelFile the file the information about the Model is being read from.
      */
     public void loadModel (Model model, File modelFile) {
         Assembly currentAssembly = new Assembly();
         try {
             Scanner input = new Scanner(modelFile);
             while (input.hasNext()) {
                 Scanner line = new Scanner(input.nextLine());
                 if (line.hasNext()) {
                     String type = line.next();
                     if (MASS_KEYWORD.equals(type)) {
                         currentAssembly.add(massCommand(line));
                     }
                     else if (SPRING_KEYWORD.equals(type)) {
                         currentAssembly.add(springCommand(line));
                     }
                 }
             }
             model.add(currentAssembly);
             input.close();
         }
         catch (FileNotFoundException e) {
             // should not happen because File came from user selection
             e.printStackTrace();
         }
     }
 
     /**
      * Loads the Forces information 
      * @param model the current Model that information is being loaded into.
      * @param modelFile the file the information about the Model is being read from.
      */
     public void loadForces (Model model, File modelFile) {
         try {
             Scanner input = new Scanner(modelFile);
             while (input.hasNext()) {
                 Scanner line = new Scanner(input.nextLine());
                 if (line.hasNext()) {
                     String type = line.next();
                     if (GRAVITY_KEYWORD.equals(type)) {
                         model.add(gravityCommand(line));
                     }
                     else if (VISCOSITY_KEYWORD.equals(type)) {
                         model.add(viscosityCommand(line));
                     }
                     else if (CENTERMASS_KEYWORD.equals(type)) {
                         model.add(centerMassCommand(line));
                     }
                     else if (WALL_REPULSION_KEYWORD.equals(type)) {
                         model.add(wallRepulsionCommand(line));
                     }                    
                 }                
             }
             addDefaultForces(model);
             input.close();
         }
         catch (FileNotFoundException e) {
             // should not happen because File came from user selection
             e.printStackTrace();
         }
     }
 
     // create mass from formatted data
     private Mass massCommand (Scanner line) {
         int id = line.nextInt();
         double x = line.nextDouble();
         double y = line.nextDouble();
         double mass = line.nextDouble();
         Mass result = new Mass(x, y, mass);
         if (mass < 0) {
             result = new FixedMass(x, y, mass);
         }
         myMasses.put(id,  result);
         return result;
     }
 
     // create spring from formatted data
     private Spring springCommand (Scanner line) {
         Mass m1 = myMasses.get(line.nextInt());
         Mass m2 = myMasses.get(line.nextInt());
         double restLength = line.nextDouble();
         double ks = line.nextDouble();
         return new Spring(m1, m2, restLength, ks);
     }
     // create gravity force from formatted data
     private Force gravityCommand (Scanner line) {
         double angle = line.nextDouble();
         double magnitude = line.nextDouble();
         GravityForce result = new GravityForce(new Vector(angle, magnitude));
         return result;
     }
 
     // create viscosity force from formatted data
     private Force viscosityCommand (Scanner line) {
         double viscosity = line.nextDouble();
         ViscosityForce result = new ViscosityForce(viscosity);
         return result;
     }
 
     // create centerMass force from formatted data
     private Force centerMassCommand(Scanner line) {
         double magnitude = line.nextDouble();
         double exponent = line.nextDouble();
         CenterMassForce result = new CenterMassForce(magnitude, exponent);
         return result;
     }
 
     // create wallRepulsion force from formatted data
     private Force wallRepulsionCommand(Scanner line) {
         int wallID = (int) line.nextDouble();
         double magnitude = line.nextDouble();
         double exponent = line.nextDouble();
         WallRepulsionForce result = new WallRepulsionForce(wallID, magnitude, exponent);
         return result;
     }
 
     private void addDefaultForces(Model model) {
         HashSet<Class> forcesPresent = new HashSet<Class>();
         for (Force f : model.getForces()) {
             forcesPresent.add(f.getClass());
         }
         if (!forcesPresent.contains(CenterMassForce.class)) {
             model.add(new CenterMassForce());
         }
        else if (!forcesPresent.contains(GravityForce.class)) {
             model.add(new GravityForce());
         }
        else if (!forcesPresent.contains(ViscosityForce.class)) {
             model.add(new ViscosityForce());
         }
         HashSet<Integer> wallsPresent = new HashSet<Integer>();
         for (WallRepulsionForce f: model.getWallRepulsionForces()) {
             wallsPresent.add(f.getID());
         }
         if (!wallsPresent.contains(WallRepulsionForce.TOP_WALL_ID)) {
             model.add(new WallRepulsionForce(WallRepulsionForce.TOP_WALL_ID));
         }
         if (!wallsPresent.contains(WallRepulsionForce.BOTTOM_WALL_ID)) {
             model.add(new WallRepulsionForce(WallRepulsionForce.BOTTOM_WALL_ID));
         }
         if (!wallsPresent.contains(WallRepulsionForce.RIGHT_WALL_ID)) {
             model.add(new WallRepulsionForce(WallRepulsionForce.RIGHT_WALL_ID));
         }
         if (!wallsPresent.contains(WallRepulsionForce.LEFT_WALL_ID)) {
             model.add(new WallRepulsionForce(WallRepulsionForce.LEFT_WALL_ID));
         }
     }
 }

/** SPL Assignment #3
 * http://www.cs.bgu.ac.il/~spl131/Assignments/Assignment_3
 *
 * @author Ory Band oryb@post.bgu.ac.il; Eldar Damari damariel@post.bgu.ac.il
*/
 
 import company.*;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 
 
 public class Driver {
     public static void main(String args[]) {
 
         Util u = Util.INSTANCE;  // File reader.
 
         Statistics statistics = new Statistics();
         Repository repository = new Repository();
         ArrayList<HeadOfLaboratory> laboratories = new ArrayList<HeadOfLaboratory>();
         ArrayList<Experiment> experiments;
         HashMap<String, ArrayList<EquipmentPackage>> equipmentForSale;
         HashMap<String, ArrayList<Laboratory>> laboratoryForSale;
         HashMap<String, ArrayList<Scientist>> scientistsForSale;
 
         u.readInitialData("InitialData.txt", statistics, repository, laboratories);
 
         experiments = u.readExperimentsList("ExperimentsList.txt");
         equipmentForSale = u.readEquipmentForSale("EquipmentForSale.txt");
         laboratoryForSale = u.readLaboratoriesForSale("LaboratoriesForSale.txt");
         scientistsForSale = u.readScientistsForSale("ScientistsForPurchase.txt");
 
         ScienceStore store = new ScienceStore(
                 equipmentForSale, scientistsForSale, laboratoryForSale);
  
 
         ChiefScientist chief = new ChiefScientist(
                 laboratories, experiments, statistics, store, repository);
 
         chief.simulateCompany();  // Run the show.
     }
 }

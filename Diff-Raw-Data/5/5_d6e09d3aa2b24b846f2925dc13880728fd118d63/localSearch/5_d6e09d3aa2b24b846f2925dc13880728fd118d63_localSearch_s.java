 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package logic.searches;
 
 import data.Solution;
 import logic.searches.neighborhoods.INeighborhood;
 import logic.searches.stepfunctions.IStepFunction;
 
 /**
  * @author Christian
  */
 
 
 public class LocalSearch {
 
 
     public Solution search(IStepFunction fs, INeighborhood n) {
         GraspSearch gs = new GraspSearch("", "", "");
         Solution s = gs.run();
 
         do {
            Solution tmp = fs.run(s, n);
 
             if (s.calculate_costs() > tmp.calculate_costs())
                 s = tmp;
        } while (fs.breakup());
 
 
         return s;
     }
 
 }

 import java.util.Iterator;
 import java.util.List;
 
 public class run_solver {
 
     public static void main(String [ ] args)
     {
         double p_s = .01;
         double t = 0;
         double c_r = .5;
         double c_w = .5;
         double L_r = 1;
         double L_w = 1;
         int k = 5;
        int n = 20;
         int w_min = 2;
 
         nrw_solver solver = new nrw_solver(p_s, t, k, c_r, c_w, L_r, L_w, w_min, n);
 
         List<nrw_solution> results = solver.get_solutions();
         Iterator<nrw_solution> it = results.iterator();
 
         while(it.hasNext())
         {
             nrw_solution cur = it.next();
 
            System.out.printf("\nN: %d\nR: %d\nW: %d\np_s: %f\nFIT: %f\nr_L: %f\nw_L: %f", cur.getN(), cur.getR(),
                                cur.getW(), cur.getP_s(), cur.getFitness(), L_r*cur.getR(), L_w*cur.getW());
         }
     }
 }

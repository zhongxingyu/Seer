 
 import java.util.List;
 import java.util.Vector;
 
 public class nrw_solver {
 
     private double c_r;
     private double c_w;
     private double L_r;
     private double L_w;
     private int w_min;
     private int n;
     private double t;
     private double p_s;
     private int k;
 
     private boolean solved = false;
 
     private List<nrw_solution> solutions;
 
     public nrw_solver(double p_s, double t, int k, double c_r, double c_w, double L_r, double L_w, int w_min, int n)
     {
         this.c_r = c_r;
         this.c_w = c_w;
         this.L_r = L_r;
         this.L_w = L_w;
         this.w_min = w_min;
         this.n  = n;
         this.p_s = p_s;
         this.t = t;
         this.k = k;
     }
 
     private double eval_func(int r, int w)
     {
         return c_r*L_r*r+c_w*L_w*w;
 
     }
 
     private int w_calc(double t, int wstart)
     {
         //unimplemeted
 
         return wstart;
     }
 
     private double calc_p_s(int rc, int wc)
     {
        return Math.pow(((double)choose(n-w_calc(t, wc), rc))/choose(n, rc), k);
     }
 
     private void solve()
     {
         double cur_min = Double.POSITIVE_INFINITY;
 
         solutions = new Vector<nrw_solution>();
 
        for(int rc = 0; rc <= n; rc++)
         {
             for(int wc = w_min; wc <= n; wc++)
             {
                 double config_fitness = eval_func(rc, wc);
 
                 double this_ps = calc_p_s(rc, wc);
 
                 if(this_ps > p_s)
                 {
                     continue;
                 }
 
                 if(config_fitness < cur_min)
                 {
                     solutions.clear();
                 }
                 if(config_fitness <= cur_min)
                 {
                     solutions.add(new nrw_solution(n, rc, wc, config_fitness, this_ps));
                     cur_min = config_fitness;
                 }
 
             }
         }
 
         solved = true;
     }
 
     public List<nrw_solution> get_solutions()
     {
         if(!solved)
             solve();
         return solutions;
     }

    private static long choose(long total, long choose){
        if(total < choose)
            return 0;
        if(choose == 0 || choose == total)
            return 1;
        return choose(total-1,choose-1)+choose(total-1,choose);
    }
 }

 
 import com.sun.security.auth.login.ConfigFile;
 
 import java.io.FileReader;
 import java.util.Iterator;
 import java.util.List;
 
 import java.io.IOException;
 import java.util.Properties;
 
 public class run_solver {
 
     public static void main(String [ ] args) throws IOException
     {
         String propertiesFile = "solver.properties";
 
         if (args.length == 1)
         {
             propertiesFile = args[0];
         }
         else
         {
           System.out.printf("no solver file specified, using default (%s)\n", propertiesFile);
         }
 
         Properties configFile = new Properties();
         configFile.load(new FileReader(propertiesFile));
 
         String desiredAction = "optimize";
 
         //number of replicas
         int n = Integer.parseInt(configFile.getProperty("n"));
 
         int r = 1;
         String r_str= configFile.getProperty("r");
        if(r_str.length() != 0)
         {
             r = Integer.parseInt(r_str);
         }
 
         //minimum number of writes to commit (keep w_min < n)
         int w_min = Integer.parseInt(configFile.get("wmin").toString());
 
         //maximum probability of staler-than-promised-ness
         double p_s = Double.parseDouble(configFile.get("p_s").toString());
 
         //RT-staleness, in seconds
         double t = Double.parseDouble(configFile.getProperty("t-stale"));
         //k-staleness
         int k = Integer.parseInt(configFile.get("k-stale").toString());
 
         //relative weighting of read latency
         double c_r = Double.parseDouble(configFile.getProperty("c_r"));
         //relative weighting of write latency
         double c_w = Double.parseDouble(configFile.getProperty("c_w"));
 
         /*
         We require three single-replica latency models (IID, remember!):
             a model for read operation completion,
             a model for write operation completion,
             and a model for how fast writes get to a replica
             (the last is called wmodelnoack).
          */
 
         LatencyModel rmodel = new FileLatencyModel((String)configFile.getProperty("r-latency-model"));
         LatencyModel wmodel = new FileLatencyModel((String)configFile.getProperty("w-latency-model"));
        LatencyModel ackmodel = new FileLatencyModel((String)configFile.getProperty("ack-latency-model"));
 
         try{
             LatencyModelValidator.ValidateModel(rmodel);
             LatencyModelValidator.ValidateModel(wmodel);
             LatencyModelValidator.ValidateModel(ackmodel);
         }
         catch (Exception e)
         {
             System.out.println("BAD LATENCY MODEL; EXITING");
             System.out.println(e.getMessage());
             System.exit(-1);
         }
 
         String operation = configFile.getProperty("actiontype");
 
         nrw_solver solver = new nrw_solver(p_s, t, k, c_r, c_w, rmodel, wmodel, ackmodel, n, r, w_min);
 
 
         if(operation.equals("optimize"))
         {
             List<nrw_solution> results = solver.get_solutions();
             Iterator<nrw_solution> it = results.iterator();
 
 
             while(it.hasNext())
             {
                 nrw_solution cur = it.next();
 
                 System.out.printf("\nN: %d\nR: %d\nW: %d\np_s: %f\nFIT: %f\nr_L: %f\nw_L: %f\n", cur.getN(), cur.getR(),
                                     cur.getW(), cur.getP_s(), cur.getFitness(), cur.getReadLatency(), cur.getWriteLatency());
             }
         }
         else if(operation.equals("calc_staleness"))
         {
             System.out.println(solver.calc_p_s(r, w_min, t, k));
         }
         else if(operation.equals("sweep_t"))
         {
             for(int tval = 0; tval < 1000; ++tval)
             {
                 System.out.printf("%d, %f\n", tval, solver.calc_p_s(r, w_min, tval, k));
             }
         }
         else if(operation.equals("sweep_r_w_t_fixed_k"))
         {
             for(int rc = 1; rc <= n; rc++)
             {
                 for(int wc = 1; wc <= n; wc++)
                 {
                     if(rc+wc>n+1)
                         continue;
                     for(int tc = 0; tc < 1000; tc++)
                     {
                         System.out.printf("%d %d %d %f %f %f %f\n", n, rc, wc, t,
                                         solver.calc_p_s(rc, wc, tc, k),
                                         solver.calcReadLatency(rc),
                                         solver.calcWriteLatency(wc));
                     }
 
                 }
             }
         }
     }
 }

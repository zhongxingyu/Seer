 package ernst.simulator;
 
 import java.lang.Math;
 import java.lang.reflect.Array;
 import java.text.DecimalFormat;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CountDownLatch;
 
 import ernst.solver.FileLatencyModel;
 import ernst.solver.LatencyModel;
 import ernst.solver.LatencyModelValidator;
 
 interface DelayModel
 {
     public double getWriteSendDelay();
     public double getReadSendDelay();
     public double getWriteAckDelay();
     public double getReadAckDelay();
 }
 
 class ParetoDelayModel implements DelayModel
 {
     double wmin, walpha, arsmin, arsalpha;
     Random rand;
     public ParetoDelayModel(double wmin, double walpha, double arsmin, double arsalpha)
     {
         this.wmin = wmin;
         this.walpha = walpha;
         this.arsmin = arsmin;
         this.arsalpha = arsalpha;
         this.rand = new Random();
     }
 
     double getNextPareto(double m, double a)
     {
         return m / Math.pow(rand.nextDouble(), 1/a);
     }
 
     public double getWriteSendDelay()
     {
         return getNextPareto(wmin, walpha);
     }
 
     public double getReadSendDelay()
     {
         return getNextPareto(arsmin, arsalpha);
     }
 
     public double getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public double getReadAckDelay()
     {
         return getReadSendDelay();
     }
 }
 
 
 class ExponentialDelayModel implements DelayModel
 {
     double wlambda, arslambda;
     Random rand;
     public ExponentialDelayModel(double wlambda, double arslambda)
     {
         this.wlambda = wlambda;
         this.arslambda = arslambda;
         this.rand = new Random();
     }
 
     double getNextExponential(double lambda)
     {
         return Math.log(1-rand.nextDouble())/(-lambda);
     }
 
     public double getWriteSendDelay()
     {
         return getNextExponential(wlambda);
     }
 
     public double getReadSendDelay()
     {
         return getNextExponential(arslambda);
     }
 
     public double getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public double getReadAckDelay()
     {
         return getReadSendDelay();
     }
 }
 
 class YammerDelayModel implements DelayModel
 {
     Random rand;
 
     public YammerDelayModel()
     {
         this.rand = new Random();
     }
 
     double getNextExponential(double lambda)
     {
         return Math.log(1-rand.nextDouble())/(-lambda);
     }
 
     double getNextPareto(double m, double a)
     {
         return m / Math.pow(rand.nextDouble(), 1/a);
     }
 
     double getNextGaussian(double mean, double std)
     {
         return rand.nextGaussian()*std+mean;
     }
 
     public double getWriteSendDelay()
     {
         if(rand.nextDouble() < .061)
         {
             return getNextExponential(.0028);
         }
         else
         {
             return getNextPareto(3, 3.5);
         }
     }
 
     public double getReadSendDelay()
     {
         if(rand.nextDouble() < .018)
         {
             return getNextExponential(.0217);
         }
         else
         {
             return getNextPareto(1.5, 3.8);
         }
     }
 
     public double getWriteAckDelay()
     {
         return getReadSendDelay();
     }
 
     public double getReadAckDelay()
     {
         return getReadSendDelay();
     }
 }
 
 class LinkedSSDDelay implements DelayModel
 {
     Random rand;
 
     public LinkedSSDDelay()
     {
         this.rand = new Random();
     }
 
     double getNextExponential(double lambda)
     {
         return Math.log(1-rand.nextDouble())/(-lambda);
     }
 
     double getNextPareto(double m, double a)
     {
         return m / Math.pow(rand.nextDouble(), 1/a);
     }
 
     public double getWriteSendDelay()
     {
         if(rand.nextDouble() < .0878)
         {
             return getNextExponential(1.66);
         }
         else
         {
             return getNextPareto(.235, 10.0);
         }
     }
 
     public double getReadSendDelay()
     {
         return getWriteSendDelay();
     }
 
     public double getWriteAckDelay()
     {
         return getWriteSendDelay();
     }
 
     public double getReadAckDelay()
     {
         return getWriteSendDelay();
     }
 }
 
 class LinkedDiskDelay implements DelayModel
 {
     Random rand;
     LinkedSSDDelay ssd;
 
     public LinkedDiskDelay()
     {
         this.rand = new Random();
         ssd = new LinkedSSDDelay();
     }
 
     double getNextExponential(double lambda)
     {
         return Math.log(1-rand.nextDouble())/(-lambda);
     }
 
     double getNextPareto(double m, double a)
     {
         return m / Math.pow(rand.nextDouble(), 1/a);
     }
 
     public double getWriteSendDelay()
     {
         if(rand.nextDouble() < .62)
         {
             return getNextExponential(.183);
         }
         else
         {
             return getNextPareto(1.05, 1.51);
         }
     }
 
     public double getReadSendDelay()
     {
         return ssd.getReadSendDelay();
     }
 
     public double getWriteAckDelay()
     {
         return ssd.getWriteAckDelay();
     }
 
     public double getReadAckDelay()
     {
         return ssd.getReadAckDelay();
     }
 }
 
 
 class EmpiricalDelayModel implements DelayModel
 {
     LatencyModel ackLatencyModel;
     LatencyModel sendLatencyModel;
 
     Random rand;
 
     //empirical distribution
     public EmpiricalDelayModel(String sendF, String writeF)
     {
       rand = new Random();
 
       try{
         sendLatencyModel = new FileLatencyModel(sendF);
         ackLatencyModel = new FileLatencyModel(writeF);
         LatencyModelValidator.ValidateModel(sendLatencyModel);
         LatencyModelValidator.ValidateModel(ackLatencyModel);
        }
        catch (Exception e)
        {
         System.out.println("BAD LATENCY MODEL; EXITING");
         System.out.println(e.getMessage());
         System.exit(-1);
        }
     }
 
     public double getWriteSendDelay() {
       return sendLatencyModel.getInverseCDF(1,
               rand.nextDouble());
     }
 
     public double getReadSendDelay() { return getWriteAckDelay(); };
 
     public double getWriteAckDelay() {
       return ackLatencyModel.getInverseCDF(1,
               rand.nextDouble());
     }
 
     public double getReadAckDelay() { return getWriteAckDelay(); };
 }
 
 class ReadResult
 {
     double rlat;
     double wlat;
     boolean stale;
 
     public double getRlat() {
         return rlat;
     }
 
     public double getWlat() {
         return wlat;
     }
 
     public boolean isStale() {
         return stale;
     }
 
     public ReadResult(double rlat, double wlat, boolean stale)
     {
         this.rlat = rlat;
         this.wlat = wlat;
         this.stale = stale;
     }
 }
 
 class StaleCalc
 {
     public static ReadResult calc_stale(int N, int R, int W, DelayModel delay, double t, boolean multidc, double dcdelay)
     {
         Vector<Double> Ws = new Vector<Double>();
         Vector<Double> writelats = new Vector<Double>();
         Vector<Double> Rs = new Vector<Double>();
         Vector<Double> readlats = new Vector<Double>();
 
         int chosenRDC = -1, chosenWDC = -1;
         if(multidc)
         {
             chosenRDC = (new Random()).nextInt(N);
             chosenWDC = (new Random()).nextInt(N);
         }
 
         for(int rep = 0; rep < N; ++rep)
         {
 
             double thisW = delay.getWriteSendDelay();
             double thisA = delay.getWriteAckDelay();
 
             if(multidc && rep != chosenWDC)
             {
                 thisW += dcdelay;
                 thisA += dcdelay;
             }
 
             Ws.add(thisW);
             writelats.add(thisW+thisA);
 
             double thisR = delay.getReadSendDelay();
             double thisS = delay.getReadAckDelay();
 
             if(multidc && rep != chosenRDC)
             {
                 thisR += dcdelay;
                 thisS += dcdelay;
             }
 
             Rs.add(thisR);
             readlats.add(thisR+thisS);
         }
 
        Vector<Double> sortedwrites = (Vector<Double>)writelats.clone();
        Collections.sort(sortedwrites);
        double w_t = sortedwrites.get(W-1);
 
         Vector<Double> sortedreads = (Vector<Double>)readlats.clone();
         Collections.sort(sortedreads);
 
         //total hack, terrible big O, but N is small...
 
         boolean current = false;
         for(int rep = 0; rep < R; ++rep)
         {
             //find the ith fastest read
             int repNo = readlats.indexOf(sortedreads.get(rep));
 
             if(w_t + Rs.get(repNo)+t >= Ws.get(repNo))
             {
                 current = true;
             }
 
             //in the unlikely event of dups, delete
             readlats.set(repNo, -1.0);
         }
 
         return new ReadResult(sortedreads.get(R-1), w_t, !current);
     }
 }
 
 public class Simulator {
 
   public static void main (String [] args) {
       assert args.length > 5;
 
       int N = 3, R = 1, W = 1, K = 1, ITERATIONS = 1000, writespacing = 1, readsperwrite = 10;
       DelayModel delaymodel = null;
       boolean multidc = false;
       double dcdelay = 0;
 
       try
       {
           N = Integer.parseInt(args[0]);
           R = Integer.parseInt(args[1]);
           W = Integer.parseInt(args[2]);
           K = Integer.parseInt(args[3]);
           assert K >= 1;
           ITERATIONS = Integer.parseInt(args[4]);
           writespacing = Integer.parseInt(args[5]);
           readsperwrite = Integer.parseInt(args[6]);
 
           delaymodel = null;
 
           if(args[7].equals("FILE"))
           {
               String sendDelayFile = args[8];
               String ackDelayFile = args[9];
 
               delaymodel = new EmpiricalDelayModel(sendDelayFile, ackDelayFile);
           }
           else if(args[7].equals("PARETO") || args[7].equals("MULTIDC"))
           {
               delaymodel = new ParetoDelayModel(Double.parseDouble(args[8]),
                                            Double.parseDouble(args[9]),
                                            Double.parseDouble(args[10]),
                                            Double.parseDouble(args[11]));
 
               if(args[7].equals("MULTIDC"))
               {
                   multidc = true;
                   dcdelay = Double.parseDouble(args[12]);
               }
           }
           else if(args[7].equals("EXPONENTIAL"))
           {
               delaymodel = new ExponentialDelayModel(Double.parseDouble(args[8]),
                                                 Double.parseDouble(args[9]));
           }
           else if(args[7].equals("YMMR"))
           {
               delaymodel = new YammerDelayModel();
           }
           else if(args[7].equals("LNKD-SSD"))
           {
               delaymodel = new LinkedSSDDelay();
           }
           else if(args[7].equals("LNKD-DISK"))
           {
               delaymodel = new LinkedDiskDelay();
           }
           else if(args[7].equals("WAN"))
           {
               delaymodel = new LinkedDiskDelay();
               multidc = true;
               dcdelay = 75.0;
           }
       }
       catch(Exception e)
       {
           e.printStackTrace();
           System.err.println(
              "Usage: Simulator <N> <R> <W> <k> <iters> <write spacing> <readsperwrite> FILE <sendF> <ackF> OPT\n" +
                      "Usage: Simulator <N> <R> <W> <k> <iters> <write spacing> <readsperwrite> PARETO <W-min> <W-alpha> <ARS-min> <ARS-alpha> OPT\n" +
                      "Usage: Simulator <N> <R> <W> <k> <iters> <write spacing> <readsperwrite> EXPONENTIAL <W-lambda> <ARS-lambda> OPT\n" +
                      "Usage: Simulator <N> <R> <W> <k> <iters> <write spacing> <readsperwrite> MULTIDC <W-min> <W-alpha> <ARS-min> <ARS-alpha> <DC-delay> OPT\n" +
                      "OPT= O <SWEEP|LATS|BESTCASE|WORSTCASE>");
           System.exit(1);
       }
 
       String optsinput = "";
 
       for(int i = 0; i < args.length; ++i)
       {
           if(args[i].equals("O"))
           {
               optsinput = args[i+1];
               assert optsinput.equals("SWEEP") || optsinput.equals("LATS") || optsinput.equals("BESTCASE")||optsinput.equals("WORSTCASE");
               break;
           }
       }
 
       boolean fine_time = false;
 
       for(int i = 0; i < args.length; ++i)
       {
           if(args[i].equals("F"))
           {
               fine_time = true;
               break;
           }
       }
 
       boolean long_time = false;
 
       for(int i = 0; i < args.length; ++i)
       {
           if(args[i].equals("L"))
           {
               long_time = true;
               break;
           }
       }
 
       boolean medium_time = false;
 
       for(int i = 0; i < args.length; ++i)
       {
           if(args[i].equals("M"))
           {
               medium_time = true;
               break;
           }
       }
 
       if(optsinput.equals("LATS"))
       {
           Vector<Double> reads = new Vector<Double>();
           Vector<Double> writes = new Vector<Double>();
           for(int i = 0; i < ITERATIONS; ++i)
           {
               ReadResult r = StaleCalc.calc_stale(N, R, W, delaymodel,  0, multidc, dcdelay);
               reads.add(r.getRlat());
               writes.add(r.getWlat());
           }
 
           System.out.println("WRITE");
           Collections.sort(writes);
           for(double p = 0; p < 1; p += .01)
           {
               int index = (int)Math.round(p*writes.size());
               if(index >= writes.size())
                   break;
               System.out.printf("%f %f\n", p, writes.get(index));
           }
 
           double lastp = .99;
           for(int i = 3; i < 7; ++i)
           {
               lastp += 9*Math.pow(10, -i);
               int index = (int)Math.round(lastp*writes.size());
               if(index >= writes.size())
                   break;
               System.out.printf("%f %f\n", lastp, writes.get(index));
           }
 
           System.out.println("READ");
           Collections.sort(reads);
           for(double p = 0; p < 1; p += .01)
           {
               int index = (int)Math.round(p*reads.size());
               if(index >= reads.size())
                   break;
               System.out.printf("%f %f\n", p, reads.get(index));
           }
 
           lastp = .99;
           for(int i = 3; i < 7; ++i)
           {
               lastp += 9*Math.pow(10, -i);
               int index = (int)Math.round(lastp*reads.size());
               if(index >= reads.size())
                   break;
               System.out.printf("%f %f\n", lastp, reads.get(index));
           }
       }
 
       else if(optsinput.equals("SWEEP"))
       {
           List<Double> times = new Vector<Double>();
 
           if(fine_time)
           {
               for(double t = 0; t < 4; t+=.01)
               {
                   times.add(t);
               }
           }
           else if(long_time)
           {
               for(double t = 1; t < 1500; t++)
               {
                   times.add(t);
               }
           }
           else if(medium_time)
           {
               for(double t = 0; t < 150; t+=.1)
               {
                   times.add(t);
               }
           }
           else
           {
               for(double t = 0; t < 200; t+=1)
               {
                   times.add(t);
               }
           }
 
           for(double ts: times)
           {
               int stales = 0;
               for(int i = 0; i < ITERATIONS; ++i)
               {
                   ReadResult r = StaleCalc.calc_stale(N, R, W, delaymodel, ts, multidc, dcdelay);
 
                   if(r.stale)
                       stales++;
               }
 
               double ps = 1-(float)(stales)/(ITERATIONS);
               System.out.println(ps+" "+ts);
 
               if(ps > 1-(10/ITERATIONS))
               {
                   break;
               }
           }
       }
   }
 }

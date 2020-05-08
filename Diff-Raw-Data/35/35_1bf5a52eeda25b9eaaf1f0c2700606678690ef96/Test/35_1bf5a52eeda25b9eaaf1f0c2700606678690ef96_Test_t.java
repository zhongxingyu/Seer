 package sisop;
 
 import sisop.FairSemaphore;
 import sisop.Prova;
 import sisop.SynchPort;
 import sisop.logging.Log;
 import java.lang.Thread;
 import java.lang.Integer;
 
 public class Test{
     public static void main(String[] args) {
         // FairSemaphore sem = new FairSemaphore(0);
         // Prova[] p = new Prova[5];
         // for (int i = 0; i < 5; ++i) {
         //     p[i] = new Prova(sem, "Thread"+i);
         // }
 
         // for (int i = 0; i < 5; ++i) {
         //     try {
         //         p[i].start();
         //         Thread.sleep(100); 
         //     }
         //     catch (Exception e) {
                 
         //     }
         // }
         // try {
         //     System.out.println(sem.isEmpty());
         //     for (int i = 0; i < 5; ++i) {
         //         Thread.sleep(100);
         //         sem.V();
         //     }
         // }
         // catch (Exception e) {
             
         // }
         try {
            Log.setup("../test.log");
             Dest dest = new Dest("Dest1");
            Mitt[] mitt = new Mitt[10];
             dest.start();
             Thread.sleep(100);
             
            for (int i = 0; i < 10; ++i) {
                 mitt[i] = new Mitt(dest, "Mitt" + i);
                 mitt[i].start();
             }
                       
         }
         catch (Exception e) {
             
         }
         
         
     }
 }

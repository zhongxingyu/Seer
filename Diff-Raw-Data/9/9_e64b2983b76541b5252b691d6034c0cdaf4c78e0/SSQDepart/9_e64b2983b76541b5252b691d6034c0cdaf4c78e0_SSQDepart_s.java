 package ssq;
 
 import simulation.Event;
 
 public class SSQDepart implements Event<SingleServerQueue> {
 
     @Override
     public final void invoke(SingleServerQueue simulation) {
         double time = simulation.getTime();
 
         simulation.setLength(simulation.getLength() - 1);
        System.out.println("Departure at: " + time + ", new population = "
                 + simulation.getLength());
 
         if (simulation.getLength() > 0) {
             simulation.schedule(new SSQDepart(), time
                     + SingleServerQueue.SERVICETIME);
         }
     }
 }

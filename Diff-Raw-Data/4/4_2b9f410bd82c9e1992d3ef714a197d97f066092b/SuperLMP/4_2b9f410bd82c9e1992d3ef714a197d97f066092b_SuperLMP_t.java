 /*
  * Like LMP, but super
  */
 package lmp;
 
 /**
  *
  * @author Adrian
  */
 public class SuperLMP {
     public static Double result;
    public static final int SLOT_LIMIT=20000; // Run until this many time slots have passed
     public static final int AVG_PACKET_SIZE = 8; // Average packet size
     public static void main(String[] args) {
         Simulator mario = new Simulator(20, 20.0);
         double sum = 0;
         while(mario.getSuccessfulPacketsSent() * AVG_PACKET_SIZE <=20000)
         {
         result = mario.send();
         if(result==-1)
         {
              System.out.println("Hooray! You sent at time: "+mario.tryAgain());
         }
         else
             System.out.println("Hooray! You sent at time: "+result);
         
         sum=+mario.getSentTime(); // Add the sent times together
         System.out.println("Packets sent successfully: "+mario.getSuccessfulPacketsSent());
         }
        System.out.println("Calculated throughput is: " + (mario.getSuccessfulPacketsSent() * AVG_PACKET_SIZE * 512) / (SLOT_LIMIT * 51.2 * Math.pow(10, -6))); //Throughput!
 }
 }

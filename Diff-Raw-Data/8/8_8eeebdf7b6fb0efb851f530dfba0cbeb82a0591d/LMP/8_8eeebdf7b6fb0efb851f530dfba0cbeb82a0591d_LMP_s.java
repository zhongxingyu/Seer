 package lmp;
 
 /**
  *
  * @author firen
  */
 public class LMP {
     private static final int NUM_OF_NODES = 100;
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        Node aNode = new Node(NUM_OF_NODES);
         
         for(int i = 0; i < NUM_OF_NODES; i++)
         {
            aNode.clear();
         }
         
        // aNode.transmit();
     }
 }

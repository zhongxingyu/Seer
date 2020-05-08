 package lmp;
 
import java.util.ArrayList;

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
        ArrayList<Node> network = new ArrayList(); // Store the Nodes here
         
         for(int i = 0; i < NUM_OF_NODES; i++)
         {
           network.add(new Node()); //Add nodes into the ArrayList
         }
         
        // aNode.transmit();
     }
 }

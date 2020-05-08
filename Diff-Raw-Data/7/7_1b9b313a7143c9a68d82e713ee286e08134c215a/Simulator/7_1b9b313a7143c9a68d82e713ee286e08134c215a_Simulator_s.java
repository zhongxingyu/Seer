 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lmp;
 
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 /**
  *
  * @author T-CATS
  */
 public class Simulator {
     private ArrayList<Node2> fred;
     private Double lambda;
     Simulator(int numOfStations, Double lambda)
     {
         fred = new ArrayList(); // Hello fred!
         this.lambda = lambda;
         for(int i=1;i<=numOfStations;i++)
             fred.add(new Node2(lambda)); // Create new Nodes!
     }
     
     /**
      * Attempts to send.
      * @return Returns the time of the successful transmission, else, returns -1
      */
     public Double send()
     {
        ArrayList<Integer> trans = new ArrayList();
         for(int i=0;i<fred.size();i++)
         {
            ArrayList<Node2> collision = new ArrayList();
             fred.get(i).send(lambda); // Send!
             for(int j= i+1;j<fred.size();j++)
             {
                 fred.get(j).send(lambda); // Send!
                if(Math.abs(fred.get(i).getTime()-fred.get(j).getTime())<=1)
                 {
                     System.out.println("Collision!"); // Oh noes!
                     collision.add(fred.get(j)); // Add to a running list of collisions
                 }
             }
             if(collision.isEmpty())
                 return fred.get(i).getTime();
         }
         return -1.0;
     }
 }

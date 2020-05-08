 package edu.mason.insf.gc;
 
 import edu.mason.insf.gc.utils.GCHelper;
 
 import java.util.LinkedList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jamaaltaylor
  * Date: 5/25/13
  * Time: 11:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Gene {
 
     LinkedList<Integer> geneBits = new LinkedList<Integer>();
     Integer numberOfBits;
 
     public Gene(int numberOfBits)
     {
         super();
 
         for(int i=0; i< numberOfBits; i++)
         {
             geneBits.add(GCHelper.generateRandomInteger(0,1));
         }
     }
 
 
 }

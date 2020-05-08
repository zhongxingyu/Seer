 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.philinko.bundesliga.utility;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 /**
  *
  * @author philippe
  */
 public class CommonFunctions {
     
     public static List<Integer> initializeIntList(int size) {
         List<Integer> result = new ArrayList<Integer>(34);
         for (int i = 0; i < size; ++i) {
             result.add(i + 1);
         }
         return Collections.unmodifiableList(result);
     }
 
     public static int aktuellerSpieltag(EntityManager em) {
        Query query = em.createQuery("Select max(spieltag) from Bewertung");
         return (Integer) query.getSingleResult();
     }
 }

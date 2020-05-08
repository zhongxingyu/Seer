 package com.g4.java.reproduction;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.g4.java.model.Individual;
 
 public class MonogamousReproduction implements Reproduction {
 
   public List<Individual[]> getParents(List<Individual> populations) {
     int size = populations.size();
 
    if ( size % 2 != 0 ) {
      populations.add(populations.get(size-1));
      size++;
    }

     List<Individual[]> families = new ArrayList<Individual[]>(size * 2);
 
     for (int i = 0; i < size / 2; ++i) {
       Individual[] parents = new Individual[2];
       parents[0] = populations.get(i);
       parents[1] = populations.get(i + size / 2);
       families.add(parents);
     }
 
     return families;
   }
 
 }

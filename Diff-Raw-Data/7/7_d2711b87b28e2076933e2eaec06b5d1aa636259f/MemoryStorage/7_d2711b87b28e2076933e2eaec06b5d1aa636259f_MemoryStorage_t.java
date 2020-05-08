 package com.stoyan.archery;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Created by Stoyan Gaydarov on 8/1/13.
  */
 public class MemoryStorage implements Storage {
 
     private static long currentId = 1;
 
     private HashMap<Practice, ArrayList<Score[]>> scores;
     private HashMap<Long, Practice> practices;
 
     public MemoryStorage() {
         scores = new HashMap<Practice, ArrayList<Score[]>>();
         practices = new HashMap<Long, Practice>();
     }
 
     private static synchronized final long nextId() {
         return currentId++;
     }
 
     @Override
     public synchronized Practice startNewPracticeRound() {
        long id = nextId();
        Practice ret = practices.get(id);
         if (ret != null) {
             return startNewPracticeRound();
         }
 
        ret = new Practice(id);
        practices.put(id, ret);

         return ret;
     }
 
     @Override
     public synchronized Practice getPracticeRound(long id) {
         Practice ret = practices.get(id);
         if (ret == null) {
             ret = new Practice(id);
             practices.put(id, ret);
         }
 
         return ret;
     }
 
     @Override
     public List<Score[]> getScores(Practice practice) {
         return scores.get(practice);
     }
 
     @Override
     public boolean store(Practice practice, Score[] arrows) {
         if (practice == null || arrows == null || arrows.length == 0) {
             return false;
         }
 
         ArrayList<Score[]> target = scores.get(practice);
         if(target == null) {
             target = new ArrayList<Score[]>();
             scores.put(practice, target);
         }
 
         return target.add(arrows);
     }
 
     private static MemoryStorage instance;
     public static synchronized MemoryStorage getInstance() {
         if (instance == null) {
             instance = new MemoryStorage();
         }
 
         return instance;
     }
 }

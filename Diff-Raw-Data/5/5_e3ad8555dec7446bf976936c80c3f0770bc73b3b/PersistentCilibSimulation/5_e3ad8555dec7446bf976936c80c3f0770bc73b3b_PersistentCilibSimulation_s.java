  /* Pleiades
  * Copyright (C) 2011 - 2012
  * Computational Intelligence Research Group (CIRG@UP)
  * Department of Computer Science
  * University of Pretoria
  * South Africa
  */
 package net.pleiades.persistence;
 
 import com.mongodb.BasicDBObject;
 import java.util.ArrayList;
 import java.util.List;
 import net.pleiades.simulations.Simulation;
 
 /**
  *
  * @author bennie
  */
 public class PersistentCilibSimulation extends BasicDBObject {
     
     public PersistentCilibSimulation() {
     }
     
     public PersistentCilibSimulation(Simulation s) {
        put("cilibInput", s.getCilibInput());
         put("fileKey", s.getFileKey());
         put("outputFileName", s.getOutputFileName());
         put("outputPath", s.getOutputPath());
         put("samples", s.getSamples());
         put("owner", s.getOwner());
         put("ownerEmail", s.getOwnerEmail());
         put("id", s.getID());
         put("jobName", s.getJobName());
         put("unfinishedTasks", s.unfinishedCount());
        //put("results", s.getResults());
     }
     
     public String cilibInput() {
         return (String) get("cilibInput");
     }
     
     public String fileKey() {
         return (String) get("fileKey");
     }
     
     public String outputFileName() {
         return (String) get("outputFileName");
     }
     
     public String outputPath() {
         return (String) get("outputPath");
     }
     
     public String owner() {
         return (String) get("owner");
     }
     
     public String ownerEmail() {
         return (String) get("ownerEmail");
     }
     
     public String id() {
         return (String) get("id");
     }
     
     public String jobName() {
         return (String) get("jobName");
     }
     
     public int samples() {
         return (Integer) get("samples");
     }
     
     public int unfinishedTasks() {
         return (Integer) get("unfinishedTasks");
     }
     
     public List<String> results() {
         return (ArrayList<String>) get("results");
     }
 }
